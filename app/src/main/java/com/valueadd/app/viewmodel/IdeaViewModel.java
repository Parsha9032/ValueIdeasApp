package com.valueadd.app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.valueadd.app.data.entity.Idea;
import com.valueadd.app.data.repository.IdeaRepository;

import java.util.Date;
import java.util.List;

public class IdeaViewModel extends AndroidViewModel {

    private final IdeaRepository repository;
    private final LiveData<List<Idea>> allIdeas;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> filterStatus = new MutableLiveData<>("All");
    private final LiveData<List<Idea>> filteredIdeas;

    public IdeaViewModel(@NonNull Application application) {
        super(application);
        repository = new IdeaRepository(application);
        allIdeas = repository.getAllIdeas();

        filteredIdeas = Transformations.switchMap(searchQuery, query -> {
            if (query == null || query.trim().isEmpty()) {
                return repository.getAllIdeas();
            } else {
                return repository.searchIdeas(query.trim());
            }
        });
    }

    public LiveData<List<Idea>> getAllIdeas() {
        return allIdeas;
    }

    public LiveData<List<Idea>> getFilteredIdeas() {
        return filteredIdeas;
    }

    public LiveData<Idea> getIdeaById(int id) {
        return repository.getIdeaById(id);
    }

    public void insert(Idea idea, IdeaRepository.InsertCallback callback) {
        idea.setDateCreated(new Date());
        idea.setDateModified(new Date());
        repository.insert(idea, callback);
    }

    public void update(Idea idea) {
        update(idea, null);
    }

    public void update(Idea idea, IdeaRepository.UpdateCallback callback) {
        idea.setDateModified(new Date());
        repository.update(idea, callback);
    }

    public void delete(Idea idea) {
        repository.delete(idea);
    }

    public void deleteById(int id) {
        repository.deleteById(id);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setFilterStatus(String status) {
        filterStatus.setValue(status);
    }

    public LiveData<String> getFilterStatus() {
        return filterStatus;
    }

    public LiveData<List<Idea>> getIdeasByStatus(String status) {
        return repository.getIdeasByStatus(status);
    }

    public LiveData<List<Idea>> getPriorityIdeas() {
        return repository.getPriorityIdeas();
    }

    public void setArchived(int id, boolean archive) {
        repository.setArchived(id, archive);
    }

    public LiveData<List<Idea>> getArchivedIdeas() {
        return repository.getArchivedIdeas();
    }

    public LiveData<List<Idea>> getIdeasByCategory(String category) {
        return repository.getIdeasByCategory(category);
    }

    public String getMotivationalMessage(int progress) {
        if (progress == 100) {
            return "🎉 Amazing! You've completed this idea! Keep crushing it!";
        } else if (progress >= 75) {
            return "🔥 Almost there! You're in the final stretch!";
        } else if (progress >= 50) {
            return "💪 Halfway done! You're on a roll, keep going!";
        } else if (progress >= 25) {
            return "⭐ Great start! Every step forward counts!";
        } else if (progress > 0) {
            return "🚀 You've started! The hardest step is always the first!";
        } else {
            return "💡 Ready to make this idea happen? Let's go!";
        }
    }

    public String getStatusFromProgress(int progress) {
        if (progress == 0) return "Not Started";
        if (progress == 100) return "Completed";
        return "In Progress";
    }
}
