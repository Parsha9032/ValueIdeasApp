package com.valueadd.app.ui.detail;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.valueadd.app.R;
import com.valueadd.app.data.entity.Idea;
import com.valueadd.app.databinding.ActivityIdeaDetailBinding;
import com.valueadd.app.ui.add.AddEditIdeaActivity;
import com.valueadd.app.viewmodel.IdeaViewModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IdeaDetailActivity extends AppCompatActivity {

    private ActivityIdeaDetailBinding binding;
    private IdeaViewModel viewModel;
    private Idea currentIdea;
    private int ideaId;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIdeaDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(IdeaViewModel.class);

        ideaId = getIntent().getIntExtra("IDEA_ID", -1);
        if (ideaId == -1) {
            finish();
            return;
        }

        viewModel.getIdeaById(ideaId).observe(this, idea -> {
            if (idea != null) {
                currentIdea = idea;
                populateUI(idea);
            }
        });

        setupClickListeners();
    }

    private void populateUI(Idea idea) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(idea.isPriority() ? "⭐ " + idea.getTitle() : idea.getTitle());
        }

        binding.textTitle.setText(idea.getTitle());
        binding.textDescription.setText(idea.getDescription());

        // Category
        if (idea.getCategory() != null && !idea.getCategory().isEmpty()) {
            binding.textCategory.setVisibility(View.VISIBLE);
            binding.textCategory.setText(getString(R.string.category_format, idea.getCategory()));
        } else {
            binding.textCategory.setVisibility(View.GONE);
        }

        if (idea.getDateCreated() != null)
            binding.textDateCreated.setText(getString(R.string.created_label, dateFormat.format(idea.getDateCreated())));
        if (idea.getDateModified() != null)
            binding.textDateModified.setText(getString(R.string.updated_label, dateFormat.format(idea.getDateModified())));

        // Status
        binding.textStatus.setText(idea.getStatus());
        binding.textStatus.setTextColor(idea.getStatusColor());

        // Progress
        int progress = idea.getProgressPercentage();
        binding.progressBarDetail.setProgress(progress);
        binding.textProgressPercent.setText(getString(R.string.percent_format, progress));
        binding.seekBarProgress.setProgress(progress);
        binding.textMotivational.setText(viewModel.getMotivationalMessage(progress));

        // Tags
        binding.chipGroupTags.removeAllViews();
        List<String> tags = idea.getTags();
        if (tags != null && !tags.isEmpty()) {
            binding.tagsSection.setVisibility(View.VISIBLE);
            for (String tag : tags) {
                Chip chip = new Chip(this);
                chip.setText(tag);
                chip.setClickable(false);
                chip.setChipBackgroundColorResource(R.color.tag_chip_background);
                binding.chipGroupTags.addView(chip);
            }
        } else {
            binding.tagsSection.setVisibility(View.GONE);
        }

        // Milestones
        setupMilestones(idea);

        // Priority button
        binding.btnTogglePriority.setText(idea.isPriority() ? R.string.priority_label : R.string.set_priority_label);
        binding.btnTogglePriority.setIconResource(
                idea.isPriority() ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
    }

    private void setupMilestones(Idea idea) {
        boolean hasMilestones = idea.getMilestone1() != null || idea.getMilestone2() != null || idea.getMilestone3() != null;
        binding.milestonesSection.setVisibility(hasMilestones ? View.VISIBLE : View.GONE);

        if (idea.getMilestone1() != null) {
            binding.checkMilestone1.setVisibility(View.VISIBLE);
            binding.checkMilestone1.setText(idea.getMilestone1());
            binding.checkMilestone1.setChecked(idea.isMilestone1Done());
        } else {
            binding.checkMilestone1.setVisibility(View.GONE);
        }

        if (idea.getMilestone2() != null) {
            binding.checkMilestone2.setVisibility(View.VISIBLE);
            binding.checkMilestone2.setText(idea.getMilestone2());
            binding.checkMilestone2.setChecked(idea.isMilestone2Done());
        } else {
            binding.checkMilestone2.setVisibility(View.GONE);
        }

        if (idea.getMilestone3() != null) {
            binding.checkMilestone3.setVisibility(View.VISIBLE);
            binding.checkMilestone3.setText(idea.getMilestone3());
            binding.checkMilestone3.setChecked(idea.isMilestone3Done());
        } else {
            binding.checkMilestone3.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // SeekBar for progress
        binding.seekBarProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    binding.progressBarDetail.setProgress(progress);
                    binding.textProgressPercent.setText(getString(R.string.percent_format, progress));
                    binding.textMotivational.setText(viewModel.getMotivationalMessage(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (currentIdea != null) {
                    int progress = seekBar.getProgress();
                    currentIdea.setProgressPercentage(progress);
                    currentIdea.setStatus(viewModel.getStatusFromProgress(progress));
                    viewModel.update(currentIdea);
                    Snackbar.make(binding.getRoot(),
                            viewModel.getMotivationalMessage(progress),
                            Snackbar.LENGTH_LONG).show();
                }
            }
        });

        // Status buttons
        binding.btnNotStarted.setOnClickListener(v -> updateStatus(getString(R.string.status_not_started), 0));
        binding.btnInProgress.setOnClickListener(v -> {
            int current = currentIdea != null ? currentIdea.getProgressPercentage() : 0;
            updateStatus(getString(R.string.status_in_progress), current == 0 ? 10 : current);
        });
        binding.btnCompleted.setOnClickListener(v -> updateStatus(getString(R.string.status_completed), 100));

        // Milestone checkboxes
        binding.checkMilestone1.setOnClickListener(v -> {
            if (currentIdea != null) {
                currentIdea.setMilestone1Done(binding.checkMilestone1.isChecked());
                autoUpdateProgress();
                viewModel.update(currentIdea);
            }
        });
        binding.checkMilestone2.setOnClickListener(v -> {
            if (currentIdea != null) {
                currentIdea.setMilestone2Done(binding.checkMilestone2.isChecked());
                autoUpdateProgress();
                viewModel.update(currentIdea);
            }
        });
        binding.checkMilestone3.setOnClickListener(v -> {
            if (currentIdea != null) {
                currentIdea.setMilestone3Done(binding.checkMilestone3.isChecked());
                autoUpdateProgress();
                viewModel.update(currentIdea);
            }
        });

        // Edit button
        binding.fabEdit.setOnClickListener(v -> openEditActivity());

        // Delete button
        binding.btnDelete.setOnClickListener(v -> {
            new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.delete_idea_title)
                    .setMessage(R.string.delete_confirm_msg)
                    .setPositiveButton(R.string.delete, (dialog, which) -> {
                        viewModel.deleteById(ideaId);
                        finish();
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });

        // Priority toggle
        binding.btnTogglePriority.setOnClickListener(v -> {
            if (currentIdea != null) {
                currentIdea.setPriority(!currentIdea.isPriority());
                viewModel.update(currentIdea);
                int msgRes = currentIdea.isPriority() ? R.string.priority_added : R.string.priority_removed;
                Snackbar.make(binding.getRoot(), msgRes, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void autoUpdateProgress() {
        if (currentIdea == null) return;
        int milestoneCount = 0;
        int doneMilestones = 0;
        if (currentIdea.getMilestone1() != null) { milestoneCount++; if (currentIdea.isMilestone1Done()) doneMilestones++; }
        if (currentIdea.getMilestone2() != null) { milestoneCount++; if (currentIdea.isMilestone2Done()) doneMilestones++; }
        if (currentIdea.getMilestone3() != null) { milestoneCount++; if (currentIdea.isMilestone3Done()) doneMilestones++; }

        if (milestoneCount > 0) {
            int progress = (doneMilestones * 100) / milestoneCount;
            currentIdea.setProgressPercentage(progress);
            currentIdea.setStatus(viewModel.getStatusFromProgress(progress));
            binding.seekBarProgress.setProgress(progress);
        }
    }

    private void updateStatus(String status, int progress) {
        if (currentIdea != null) {
            currentIdea.setStatus(status);
            currentIdea.setProgressPercentage(progress);
            viewModel.update(currentIdea);
            Snackbar.make(binding.getRoot(),
                    viewModel.getMotivationalMessage(progress),
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.action_edit) {
            openEditActivity();
            return true;
        } else if (item.getItemId() == R.id.action_share) {
            shareIdea();
            return true;
        } else if (item.getItemId() == R.id.action_archive) {
            toggleArchive();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareIdea() {
        if (currentIdea == null) return;

        String shareBody = getString(R.string.share_body,
                currentIdea.getTitle(),
                currentIdea.getDescription(),
                currentIdea.getStatus(),
                currentIdea.getProgressPercentage(),
                currentIdea.getCategory() != null ? currentIdea.getCategory() : getString(R.string.general_category),
                currentIdea.getTagsAsString()
        );

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject, currentIdea.getTitle()));
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent, getString(R.string.share_via)));
    }

    private void toggleArchive() {
        if (currentIdea == null) return;
        boolean newArchiveStatus = !currentIdea.isArchived();
        currentIdea.setArchived(newArchiveStatus);
        
        viewModel.update(currentIdea, () -> {
            runOnUiThread(() -> {
                int msgRes = newArchiveStatus ? R.string.idea_archived : R.string.idea_restored;
                Toast.makeText(this, msgRes, Toast.LENGTH_SHORT).show();
                if (newArchiveStatus) finish(); // Go back if archived
            });
        });
    }

    private void openEditActivity() {
        Intent intent = new Intent(this, AddEditIdeaActivity.class);
        intent.putExtra("IDEA_ID", ideaId);
        startActivity(intent);
    }
}
