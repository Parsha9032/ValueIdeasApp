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

    public void deleteAll() {
        repository.deleteAll();
    }

    public void insertAll(List<Idea> ideas, Runnable callback) {
        repository.insertAll(ideas, callback);
    }

    public void getAllIdeasForExport(IdeaRepository.ExportCallback callback) {
        repository.getAllIdeasForExport(callback);
    }

    public String getMotivationalMessage(int progress) {
        if (progress == 100) {
            return getApplication().getString(com.valueadd.app.R.string.motivation_100);
        } else if (progress >= 75) {
            return getApplication().getString(com.valueadd.app.R.string.motivation_75);
        } else if (progress >= 50) {
            return getApplication().getString(com.valueadd.app.R.string.motivation_50);
        } else if (progress >= 25) {
            return getApplication().getString(com.valueadd.app.R.string.motivation_25);
        } else if (progress > 0) {
            return getApplication().getString(com.valueadd.app.R.string.motivation_0_plus);
        } else {
            return getApplication().getString(com.valueadd.app.R.string.motivation_0);
        }
    }

    public String getStatusFromProgress(int progress) {
        if (progress == 0) return getApplication().getString(com.valueadd.app.R.string.status_not_started);
        if (progress == 100) return getApplication().getString(com.valueadd.app.R.string.status_completed);
        return getApplication().getString(com.valueadd.app.R.string.status_in_progress);
    }
}
