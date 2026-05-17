package com.valueadd.app.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.valueadd.app.data.AppDatabase;
import com.valueadd.app.data.dao.IdeaDao;
import com.valueadd.app.data.entity.Idea;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IdeaRepository {

    private final IdeaDao ideaDao;
    private final ExecutorService executorService;

    public IdeaRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        ideaDao = db.ideaDao();
        executorService = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<Idea>> getAllIdeas() {
        return ideaDao.getAllIdeas();
    }

    public LiveData<Idea> getIdeaById(int id) {
        return ideaDao.getIdeaById(id);
    }

    public void insert(Idea idea, InsertCallback callback) {
        executorService.execute(() -> {
            long id = ideaDao.insert(idea);
            if (callback != null) callback.onInserted((int) id);
        });
    }

    public void update(Idea idea, UpdateCallback callback) {
        executorService.execute(() -> {
            ideaDao.update(idea);
            if (callback != null) callback.onUpdated();
        });
    }

    public interface UpdateCallback {
        void onUpdated();
    }

    public void delete(Idea idea) {
        executorService.execute(() -> ideaDao.delete(idea));
    }

    public void deleteById(int id) {
        executorService.execute(() -> ideaDao.deleteById(id));
    }

    public LiveData<List<Idea>> searchIdeas(String query) {
        return ideaDao.searchIdeas(query);
    }

    public LiveData<List<Idea>> getIdeasByStatus(String status) {
        return ideaDao.getIdeasByStatus(status);
    }

    public LiveData<List<Idea>> getIdeasByCategory(String category) {
        return ideaDao.getIdeasByCategory(category);
    }

    public LiveData<List<Idea>> getArchivedIdeas() {
        return ideaDao.getArchivedIdeas();
    }

    public void setArchived(int id, boolean archive) {
        executorService.execute(() -> ideaDao.setArchived(id, archive));
    }

    public LiveData<List<Idea>> getPriorityIdeas() {
        return ideaDao.getPriorityIdeas();
    }

    public void insertAll(List<Idea> ideas, Runnable callback) {
        executorService.execute(() -> {
            ideaDao.insertAll(ideas);
            if (callback != null) callback.run();
        });
    }

    public void deleteAll() {
        executorService.execute(ideaDao::deleteAll);
    }

    public void getAllIdeasForExport(ExportCallback callback) {
        executorService.execute(() -> {
            List<Idea> ideas = ideaDao.getAllIdeasSync();
            if (callback != null) callback.onDataReady(ideas);
        });
    }

    public interface ExportCallback {
        void onDataReady(List<Idea> ideas);
    }

    public interface InsertCallback {
        void onInserted(int id);
    }
}
