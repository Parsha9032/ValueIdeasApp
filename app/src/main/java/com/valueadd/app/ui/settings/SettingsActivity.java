package com.valueadd.app.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.valueadd.app.data.entity.Idea;
import com.valueadd.app.databinding.ActivitySettingsBinding;
import com.valueadd.app.viewmodel.IdeaViewModel;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private IdeaViewModel viewModel;

    private final ActivityResultLauncher<String> createDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("text/csv"), this::handleExportUri);

    private final ActivityResultLauncher<String[]> openDocumentLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::handleImportUri);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(IdeaViewModel.class);

        binding.btnExport.setOnClickListener(v -> exportIdeas());
        binding.btnImport.setOnClickListener(v -> importIdeas());
        binding.btnClearData.setOnClickListener(v -> showClearDataConfirmation());
    }

    private void exportIdeas() {
        createDocumentLauncher.launch("value_ideas_backup.csv");
    }

    private void importIdeas() {
        openDocumentLauncher.launch(new String[]{"text/comma-separated-values", "text/csv"});
    }

    private void handleImportUri(Uri uri) {
        if (uri == null) return;

        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {

            List<Idea> importedIdeas = new ArrayList<>();
            String line = reader.readLine(); // Skip header

            while ((line = reader.readLine()) != null) {
                try {
                    Idea idea = parseCsvLine(line);
                    if (idea != null) {
                        importedIdeas.add(idea);
                    }
                } catch (Exception e) {
                    // Skip malformed lines
                }
            }

            if (!importedIdeas.isEmpty()) {
                viewModel.insertAll(importedIdeas, () -> {
                    runOnUiThread(() -> Toast.makeText(this, "Successfully imported " + importedIdeas.size() + " ideas!", Toast.LENGTH_SHORT).show());
                });
            } else {
                Toast.makeText(this, "No valid ideas found in file", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private Idea parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(currentToken.toString());
                currentToken.setLength(0);
            } else {
                currentToken.append(c);
            }
        }
        tokens.add(currentToken.toString());

        if (tokens.size() < 10) return null;

        Idea idea = new Idea();
        idea.setTitle(tokens.get(1));
        idea.setDescription(tokens.get(2));
        idea.setCategory(tokens.get(3));
        idea.setStatus(tokens.get(4));
        try {
            idea.setProgressPercentage(Integer.parseInt(tokens.get(5)));
        } catch (Exception e) {
            idea.setProgressPercentage(0);
        }
        idea.setPriority(Boolean.parseBoolean(tokens.get(6)));
        idea.setArchived(Boolean.parseBoolean(tokens.get(7)));
        
        String tagsStr = tokens.get(8);
        if (!tagsStr.isEmpty()) {
            idea.setTags(Arrays.asList(tagsStr.split("\\|")));
        }

        idea.setDateCreated(new Date());
        idea.setDateModified(new Date());
        return idea;
    }

    private void handleExportUri(Uri uri) {
        if (uri == null) return;

        viewModel.getAllIdeasForExport(ideas -> {
            try {
                StringBuilder csvContent = new StringBuilder();
                csvContent.append("ID,Title,Description,Category,Status,Progress,Priority,Archived,Tags,Date Created\n");

                for (Idea idea : ideas) {
                    csvContent.append(idea.getId()).append(",")
                            .append(escapeCsv(idea.getTitle())).append(",")
                            .append(escapeCsv(idea.getDescription())).append(",")
                            .append(escapeCsv(idea.getCategory())).append(",")
                            .append(escapeCsv(idea.getStatus())).append(",")
                            .append(idea.getProgressPercentage()).append(",")
                            .append(idea.isPriority()).append(",")
                            .append(idea.isArchived()).append(",")
                            .append(escapeCsv(idea.getTagsAsString())).append(",")
                            .append(idea.getDateCreated()).append("\n");
                }

                try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                    if (outputStream != null) {
                        outputStream.write(csvContent.toString().getBytes(StandardCharsets.UTF_8));
                        runOnUiThread(() -> Toast.makeText(this, "Ideas exported successfully!", Toast.LENGTH_SHORT).show());
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void showClearDataConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Clear All Data?")
                .setMessage("This will permanently delete all your ideas. This action cannot be undone.")
                .setPositiveButton("Clear All", (dialog, which) -> {
                    viewModel.deleteAll();
                    Toast.makeText(this, "All data cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
