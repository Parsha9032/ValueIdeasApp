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

    @Query("SELECT * FROM ideas ORDER BY isPriority DESC, dateModified DESC")
    LiveData<List<Idea>> getAllIdeas();

    @Query("SELECT * FROM ideas WHERE id = :id")
    LiveData<Idea> getIdeaById(int id);

    @Query("SELECT * FROM ideas WHERE id = :id")
    Idea getIdeaByIdSync(int id);

    @Query("SELECT * FROM ideas WHERE title LIKE '%' || :query || '%' ORDER BY isPriority DESC, dateModified DESC")
    LiveData<List<Idea>> searchByTitle(String query);

    @Query("SELECT * FROM ideas WHERE tags LIKE '%' || :tag || '%' ORDER BY isPriority DESC, dateModified DESC")
    LiveData<List<Idea>> searchByTag(String tag);

    @Query("SELECT * FROM ideas WHERE (title LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') ORDER BY isPriority DESC, dateModified DESC")
    LiveData<List<Idea>> searchIdeas(String query);

    @Query("SELECT * FROM ideas WHERE status = :status ORDER BY isPriority DESC, dateModified DESC")
    LiveData<List<Idea>> getIdeasByStatus(String status);

    @Query("SELECT * FROM ideas WHERE isPriority = 1 ORDER BY dateModified DESC")
    LiveData<List<Idea>> getPriorityIdeas();

    @Query("DELETE FROM ideas WHERE id = :id")
    void deleteById(int id);

    @Query("SELECT COUNT(*) FROM ideas")
    int getTotalCount();

    @Query("SELECT COUNT(*) FROM ideas WHERE status = 'Completed'")
    int getCompletedCount();
}
