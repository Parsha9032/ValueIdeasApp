package com.valueadd.app.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.valueadd.app.R;
import com.valueadd.app.data.entity.Idea;
import com.valueadd.app.databinding.ActivityMainBinding;
import com.valueadd.app.ui.add.AddEditIdeaActivity;
import com.valueadd.app.ui.detail.IdeaDetailActivity;
import com.valueadd.app.viewmodel.IdeaViewModel;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements IdeaAdapter.OnIdeaClickListener {

    private ActivityMainBinding binding;
    private IdeaViewModel viewModel;
    private IdeaAdapter adapter;
    private List<Idea> currentList = new ArrayList<>();
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        viewModel = new ViewModelProvider(this).get(IdeaViewModel.class);

        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupFab();
        observeIdeas();
    }

    private void setupRecyclerView() {
        adapter = new IdeaAdapter(this);
        binding.recyclerViewIdeas.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewIdeas.setAdapter(adapter);
        binding.recyclerViewIdeas.setHasFixedSize(false);

        // Swipe to delete
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@androidx.annotation.NonNull RecyclerView recyclerView,
                                  @androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder,
                                  @androidx.annotation.NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@androidx.annotation.NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if (position >= 0 && position < currentList.size()) {
                    Idea deletedIdea = currentList.get(position);
                    showDeleteConfirmation(deletedIdea);
                    adapter.notifyItemChanged(position);
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewIdeas);
    }

    private void showDeleteConfirmation(Idea idea) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Idea")
                .setMessage("Are you sure you want to delete \"" + idea.getTitle() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.delete(idea);
                    Snackbar.make(binding.getRoot(), "Idea deleted", Snackbar.LENGTH_LONG)
                            .setAction("Undo", v -> viewModel.insert(idea, null))
                            .show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setupSearch() {
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                viewModel.setSearchQuery(query);
                binding.clearSearchButton.setVisibility(query.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.clearSearchButton.setOnClickListener(v -> {
            binding.searchEditText.setText("");
            binding.clearSearchButton.setVisibility(View.GONE);
        });
    }

    private void setupFilterChips() {
        String[] filters = {"All", "Not Started", "In Progress", "Completed", "Priority"};
        for (String filter : filters) {
            Chip chip = new Chip(this);
            chip.setText(filter);
            chip.setCheckable(true);
            chip.setChecked(filter.equals("All"));
            chip.setChipBackgroundColorResource(R.color.chip_background_selector);
            chip.setTextColor(getResources().getColorStateList(R.color.chip_text_selector, getTheme()));
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Uncheck all other chips
                    for (int i = 0; i < binding.filterChipGroup.getChildCount(); i++) {
                        Chip c = (Chip) binding.filterChipGroup.getChildAt(i);
                        if (c != chip) c.setChecked(false);
                    }
                    currentFilter = filter;
                    applyFilter(filter);
                }
            });
            binding.filterChipGroup.addView(chip);
        }
    }

    private void applyFilter(String filter) {
        switch (filter) {
            case "Not Started":
            case "In Progress":
            case "Completed":
                viewModel.getIdeasByStatus(filter).observe(this, ideas -> {
                    currentList = ideas != null ? ideas : new ArrayList<>();
                    updateUI(currentList);
                });
                break;
            case "Priority":
                viewModel.getPriorityIdeas().observe(this, ideas -> {
                    currentList = ideas != null ? ideas : new ArrayList<>();
                    updateUI(currentList);
                });
                break;
            default:
                observeIdeas();
                break;
        }
    }

    private void setupFab() {
        binding.fabAddIdea.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditIdeaActivity.class);
            startActivity(intent);
        });
    }

    private void observeIdeas() {
        viewModel.getFilteredIdeas().observe(this, ideas -> {
            currentList = ideas != null ? ideas : new ArrayList<>();
            updateUI(currentList);
        });
    }

    private void updateUI(List<Idea> ideas) {
        adapter.setIdeas(ideas);
        if (ideas == null || ideas.isEmpty()) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.recyclerViewIdeas.setVisibility(View.GONE);
            String query = binding.searchEditText.getText().toString();
            if (!query.isEmpty()) {
                binding.emptyStateText.setText("No ideas found for \"" + query + "\"");
                binding.emptyStateSubtext.setText("Try a different search term");
            } else {
                binding.emptyStateText.setText("No ideas yet!");
                binding.emptyStateSubtext.setText("Tap the + button to add your first idea");
            }
        } else {
            binding.emptyStateLayout.setVisibility(View.GONE);
            binding.recyclerViewIdeas.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onIdeaClick(Idea idea) {
        Intent intent = new Intent(this, IdeaDetailActivity.class);
        intent.putExtra("IDEA_ID", idea.getId());
        startActivity(intent);
    }

    @Override
    public void onPriorityToggle(Idea idea) {
        idea.setPriority(!idea.isPriority());
        viewModel.update(idea);
        String msg = idea.isPriority() ? "Marked as priority ⭐" : "Removed from priority";
        Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Value ideas")
                    .setMessage("Capture, track, and implement your best ideas.\n\n" +
                            "Version 1.0\n\n" +
                            "Developed by: Parsharamulu Mangol\n" +
                            "Mobile: +91-9032831306")
                    .setPositiveButton("OK", null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
