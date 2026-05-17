package com.valueadd.app.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.valueadd.app.data.entity.Idea;

import java.util.List;

@Dao
public interface IdeaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Idea idea);

    @Update
    void update(Idea idea);

    @Delete
    void delete(Idea idea);

    @Query("SELECT * FROM ideas WHERE isArchived = 0 ORDER BY isPriority DESC, dateModified DESC")
    LiveData<List<Idea>> getAllIdeas();

    @Query("SELECT * FROM ideas WHERE isArchived = 1 ORDER BY dateModified DESC")
    LiveData<List<Idea>> getArchivedIdeas();

    @Query("SELECT * FROM ideas WHERE id = :id")
    LiveData<Idea> getIdeaById(int id);

    @Query("SELECT * FROM ideas WHERE id = :id")
    Idea getIdeaByIdSync(int id);

    @Query("SELECT * FROM ideas WHERE title LIKE '%' || :query || '%' AND isArchived = 0 ORDER BY isPriority DESC, dateModified DESC")
    LiveData<List<Idea>> searchByTitle(String query);

    @Query("SELECT * FROM ideas WHERE (tags LIKE '%|' || :tag || '|%' OR tags LIKE :tag || '|%' OR tags LIKE '%|' || :tag OR tags = :tag) AND isArchived = 0 ORDER BY isPriority DESC, dateModified DESC")
    LiveData<List<Idea>> searchByTag(String tag);

    @Query("SELECT * FROM ideas WHERE (title LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') AND isArchived = 0 ORDER BY isPriority DESC, dateModified DESC")
    LiveData<List<Idea>> searchIdeas(String query);

    @Query("SELECT * FROM ideas WHERE status = :status AND isArchived = 0 ORDER BY isPriority DESC, dateModified DESC")
    LiveData<List<Idea>> getIdeasByStatus(String status);

    @Query("SELECT * FROM ideas WHERE category = :category AND isArchived = 0 ORDER BY isPriority DESC, dateModified DESC")
    LiveData<List<Idea>> getIdeasByCategory(String category);

    @Query("SELECT * FROM ideas WHERE isPriority = 1 AND isArchived = 0 ORDER BY dateModified DESC")
    LiveData<List<Idea>> getPriorityIdeas();

    @Query("UPDATE ideas SET isArchived = :archive WHERE id = :id")
    void setArchived(int id, boolean archive);

    @Query("DELETE FROM ideas WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT COUNT(*) FROM ideas")
    int getTotalCount();

    @Query("SELECT COUNT(*) FROM ideas WHERE status = 'Completed'")
    int getCompletedCount();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Idea> ideas);

    @Query("SELECT * FROM ideas ORDER BY dateCreated DESC")
    List<Idea> getAllIdeasSync();

    @Query("DELETE FROM ideas")
    void deleteAll();
}
