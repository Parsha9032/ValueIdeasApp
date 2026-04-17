package com.valueadd.app.ui.home;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.valueadd.app.R;
import com.valueadd.app.data.entity.Idea;
import com.valueadd.app.databinding.ItemIdeaBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IdeaAdapter extends RecyclerView.Adapter<IdeaAdapter.IdeaViewHolder> {

    private List<Idea> ideas = new ArrayList<>();
    private final OnIdeaClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public IdeaAdapter(OnIdeaClickListener listener) {
        this.listener = listener;
    }

    public void setIdeas(List<Idea> newIdeas) {
        if (newIdeas == null) newIdeas = new ArrayList<>();
        List<Idea> finalNewIdeas = newIdeas;
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override public int getOldListSize() { return ideas.size(); }
            @Override public int getNewListSize() { return finalNewIdeas.size(); }

            @Override
            public boolean areItemsTheSame(int oldPos, int newPos) {
                return ideas.get(oldPos).getId() == finalNewIdeas.get(newPos).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldPos, int newPos) {
                Idea o = ideas.get(oldPos), n = finalNewIdeas.get(newPos);
                return o.getTitle().equals(n.getTitle())
                        && o.getStatus().equals(n.getStatus())
                        && o.getProgressPercentage() == n.getProgressPercentage()
                        && o.isPriority() == n.isPriority();
            }
        });
        ideas = finalNewIdeas;
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public IdeaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIdeaBinding binding = ItemIdeaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new IdeaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull IdeaViewHolder holder, int position) {
        holder.bind(ideas.get(position));
    }

    @Override
    public int getItemCount() {
        return ideas.size();
    }

    public interface OnIdeaClickListener {
        void onIdeaClick(Idea idea);
        void onPriorityToggle(Idea idea);
    }

    class IdeaViewHolder extends RecyclerView.ViewHolder {
        private final ItemIdeaBinding binding;

        IdeaViewHolder(ItemIdeaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Idea idea) {
            binding.textTitle.setText(idea.getTitle());
            binding.textDescription.setText(idea.getDescription());

            // Smooth fade-in animation
            itemView.setAlpha(0f);
            itemView.animate().alpha(1f).setDuration(300).start();

            // Date
            if (idea.getDateCreated() != null) {
                binding.textDate.setText(dateFormat.format(idea.getDateCreated()));
            }

            // Status chip
            binding.chipStatus.setText(idea.getStatus());
            binding.chipStatus.setChipBackgroundColor(
                    ColorStateList.valueOf(idea.getStatusColor()));

            // Progress bar
            binding.progressBar.setProgress(idea.getProgressPercentage());
            binding.textProgress.setText(idea.getProgressPercentage() + "%");

            // Tags
            String tags = idea.getTagsAsString();
            if (tags.isEmpty()) {
                binding.textTags.setVisibility(View.GONE);
            } else {
                binding.textTags.setVisibility(View.VISIBLE);
                binding.textTags.setText("🏷 " + tags);
            }

            // Priority star
            binding.btnPriority.setImageResource(
                    idea.isPriority() ? R.drawable.ic_star_filled : R.drawable.ic_star_outline);
            binding.btnPriority.setColorFilter(
                    idea.isPriority()
                            ? binding.getRoot().getContext().getColor(R.color.priority_yellow)
                            : binding.getRoot().getContext().getColor(R.color.gray));

            // Priority indicator
            binding.priorityIndicator.setVisibility(idea.isPriority() ? View.VISIBLE : View.GONE);

            // Click listeners
            binding.getRoot().setOnClickListener(v -> listener.onIdeaClick(idea));
            binding.btnPriority.setOnClickListener(v -> listener.onPriorityToggle(idea));
        }
    }
}
