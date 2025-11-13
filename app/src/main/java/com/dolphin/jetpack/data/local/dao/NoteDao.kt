package com.dolphin.jetpack.data.local.dao

import androidx.room.*
import com.dolphin.jetpack.data.local.entity.NoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Query("SELECT * FROM offline_notes WHERE topicId = :topicId ORDER BY orderIndex ASC")
    suspend fun getNotesByTopicId(topicId: Int): List<NoteEntity>

    @Query("SELECT * FROM offline_notes WHERE chapterId = :chapterId ORDER BY orderIndex ASC")
    suspend fun getNotesByChapterId(chapterId: Int): List<NoteEntity>

    @Query("SELECT * FROM offline_notes ORDER BY chapterId ASC, topicId ASC, orderIndex ASC")
    fun getAllOfflineNotes(): Flow<List<NoteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM offline_notes WHERE topicId = :topicId)")
    suspend fun hasNotesForTopic(topicId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<NoteEntity>)

    @Delete
    suspend fun deleteNote(note: NoteEntity)

    @Query("DELETE FROM offline_notes WHERE topicId = :topicId")
    suspend fun deleteNotesByTopicId(topicId: Int)

    @Query("DELETE FROM offline_notes WHERE chapterId = :chapterId")
    suspend fun deleteNotesByChapterId(chapterId: Int)

    @Query("DELETE FROM offline_notes")
    suspend fun deleteAllNotes()

    @Query("SELECT COUNT(*) FROM offline_notes")
    suspend fun getOfflineNoteCount(): Int
}
