package com.valueadd.app.ui.add;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.valueadd.app.data.entity.Idea;
import com.valueadd.app.databinding.ActivityAddEditIdeaBinding;
import com.valueadd.app.viewmodel.IdeaViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddEditIdeaActivity extends AppCompatActivity {

    private ActivityAddEditIdeaBinding binding;
    private IdeaViewModel viewModel;
    private Idea existingIdea;
    private int editIdeaId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditIdeaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(IdeaViewModel.class);

        editIdeaId = getIntent().getIntExtra("IDEA_ID", -1);

        if (editIdeaId != -1) {
            getSupportActionBar().setTitle("Edit Idea");
            loadExistingIdea(editIdeaId);
        } else {
            getSupportActionBar().setTitle("New Idea");
        }

        binding.btnSave.setOnClickListener(v -> saveIdea());
    }

    private void loadExistingIdea(int id) {
        viewModel.getIdeaById(id).observe(this, idea -> {
            if (idea != null && existingIdea == null) {
                existingIdea = idea;
                populateFields(idea);
            }
        });
    }

    private void populateFields(Idea idea) {
        binding.editTitle.setText(idea.getTitle());
        binding.editDescription.setText(idea.getDescription());
        binding.editTags.setText(idea.getTagsAsString());
        binding.switchPriority.setChecked(idea.isPriority());
        binding.editMilestone1.setText(idea.getMilestone1());
        binding.editMilestone2.setText(idea.getMilestone2());
        binding.editMilestone3.setText(idea.getMilestone3());
    }

    private void saveIdea() {
        String title = binding.editTitle.getText().toString().trim();
        String description = binding.editDescription.getText().toString().trim();
        String tagsRaw = binding.editTags.getText().toString().trim();
        boolean isPriority = binding.switchPriority.isChecked();
        String milestone1 = binding.editMilestone1.getText().toString().trim();
        String milestone2 = binding.editMilestone2.getText().toString().trim();
        String milestone3 = binding.editMilestone3.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(title)) {
            binding.editTitleLayout.setError("Title is required");
            return;
        }
        if (title.length() > 100) {
            binding.editTitleLayout.setError("Title must be under 100 characters");
            return;
        }
        binding.editTitleLayout.setError(null);

        if (TextUtils.isEmpty(description)) {
            binding.editDescriptionLayout.setError("Description is required");
            return;
        }
        binding.editDescriptionLayout.setError(null);

        // Parse tags
        List<String> tags = new ArrayList<>();
        if (!tagsRaw.isEmpty()) {
            String[] tagArray = tagsRaw.split("[,;]+");
            for (String tag : tagArray) {
                String trimmed = tag.trim();
                if (!trimmed.isEmpty()) {
                    tags.add(trimmed);
                }
            }
        }

        if (editIdeaId != -1 && existingIdea != null) {
            // Update existing
            existingIdea.setTitle(title);
            existingIdea.setDescription(description);
            existingIdea.setTags(tags);
            existingIdea.setPriority(isPriority);
            existingIdea.setMilestone1(milestone1.isEmpty() ? null : milestone1);
            existingIdea.setMilestone2(milestone2.isEmpty() ? null : milestone2);
            existingIdea.setMilestone3(milestone3.isEmpty() ? null : milestone3);
            viewModel.update(existingIdea);
            Toast.makeText(this, "Idea updated!", Toast.LENGTH_SHORT).show();
        } else {
            // Create new
            Idea idea = new Idea();
            idea.setTitle(title);
            idea.setDescription(description);
            idea.setTags(tags);
            idea.setPriority(isPriority);
            idea.setMilestone1(milestone1.isEmpty() ? null : milestone1);
            idea.setMilestone2(milestone2.isEmpty() ? null : milestone2);
            idea.setMilestone3(milestone3.isEmpty() ? null : milestone3);
            viewModel.insert(idea, insertedId -> {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Idea saved! 💡", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
            return;
        }
        finish();
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
