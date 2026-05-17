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
import com.valueadd.app.ui.archive.ArchiveActivity;
import com.valueadd.app.ui.detail.IdeaDetailActivity;
import com.valueadd.app.ui.settings.SettingsActivity;
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
                .setTitle(R.string.delete_idea_title)
                .setMessage(getString(R.string.delete_idea_confirm, idea.getTitle()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.delete(idea);
                    Snackbar.make(binding.getRoot(), R.string.idea_deleted, Snackbar.LENGTH_LONG)
                            .setAction(R.string.undo, v -> viewModel.insert(idea, null))
                            .show();
                })
                .setNegativeButton(R.string.cancel, null)
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
        String[] filters = {
                getString(R.string.filter_all),
                getString(R.string.status_not_started),
                getString(R.string.status_in_progress),
                getString(R.string.status_completed),
                getString(R.string.priority)
        };
        for (String filter : filters) {
            Chip chip = new Chip(this);
            chip.setText(filter);
            chip.setCheckable(true);
            chip.setChecked(filter.equals(getString(R.string.filter_all)));
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
        if (filter.equals(getString(R.string.status_not_started)) ||
                filter.equals(getString(R.string.status_in_progress)) ||
                filter.equals(getString(R.string.status_completed))) {
            viewModel.getIdeasByStatus(filter).observe(this, ideas -> {
                currentList = ideas != null ? ideas : new ArrayList<>();
                updateUI(currentList);
            });
        } else if (filter.equals(getString(R.string.priority))) {
            viewModel.getPriorityIdeas().observe(this, ideas -> {
                currentList = ideas != null ? ideas : new ArrayList<>();
                updateUI(currentList);
            });
        } else {
            observeIdeas();
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
                binding.emptyStateText.setText(getString(R.string.no_ideas_found, query));
                binding.emptyStateSubtext.setText(R.string.try_different_search);
            } else {
                binding.emptyStateText.setText(R.string.no_ideas);
                binding.emptyStateSubtext.setText(R.string.no_ideas_sub);
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
        int msgRes = idea.isPriority() ? R.string.priority_added : R.string.priority_removed;
        Snackbar.make(binding.getRoot(), msgRes, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_archive) {
            Intent intent = new Intent(this, ArchiveActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        } else if (item.getItemId() == R.id.action_about) {
            String aboutMessage = getString(R.string.app_description) + "\n\n" +
                    getString(R.string.version_label, "1.3") + "\n\n" +
                    getString(R.string.developed_by, getString(R.string.developer_name)) + "\n" +
                    getString(R.string.contact, getString(R.string.developer_contact));

            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.app_name)
                    .setMessage(aboutMessage)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
