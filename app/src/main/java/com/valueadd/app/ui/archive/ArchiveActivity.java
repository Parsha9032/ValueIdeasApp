package com.valueadd.app.ui.archive;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.valueadd.app.data.entity.Idea;
import com.valueadd.app.databinding.ActivityArchiveBinding;
import com.valueadd.app.ui.detail.IdeaDetailActivity;
import com.valueadd.app.ui.home.IdeaAdapter;
import com.valueadd.app.viewmodel.IdeaViewModel;

import java.util.ArrayList;

public class ArchiveActivity extends AppCompatActivity implements IdeaAdapter.OnIdeaClickListener {

    private ActivityArchiveBinding binding;
    private IdeaViewModel viewModel;
    private IdeaAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArchiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(IdeaViewModel.class);
        setupRecyclerView();
        observeArchivedIdeas();
    }

    private void setupRecyclerView() {
        adapter = new IdeaAdapter(this);
        binding.recyclerViewArchived.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewArchived.setAdapter(adapter);
    }

    private void observeArchivedIdeas() {
        viewModel.getArchivedIdeas().observe(this, ideas -> {
            if (ideas == null || ideas.isEmpty()) {
                binding.emptyStateLayout.setVisibility(View.VISIBLE);
                binding.recyclerViewArchived.setVisibility(View.GONE);
                adapter.setIdeas(new ArrayList<>());
            } else {
                binding.emptyStateLayout.setVisibility(View.GONE);
                binding.recyclerViewArchived.setVisibility(View.VISIBLE);
                adapter.setIdeas(ideas);
            }
        });
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