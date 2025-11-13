package com.dolphin.jetpack.data.local.dao

import androidx.room.*
import com.dolphin.jetpack.data.local.entity.ChapterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChapterDao {

    @Query("SELECT * FROM offline_chapters WHERE isOffline = 1 ORDER BY orderIndex ASC")
    fun getAllOfflineChapters(): Flow<List<ChapterEntity>>

    @Query("SELECT * FROM offline_chapters WHERE isOffline = 1 ORDER BY orderIndex ASC")
    suspend fun getAllOfflineChaptersList(): List<ChapterEntity>

    @Query("SELECT * FROM offline_chapters WHERE id = :chapterId")
    suspend fun getChapterById(chapterId: Int): ChapterEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM offline_chapters WHERE id = :chapterId AND isOffline = 1)")
    suspend fun isChapterOffline(chapterId: Int): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM offline_chapters WHERE id = :chapterId AND manuallyDownloaded = 1)")
    suspend fun isChapterManuallyDownloaded(chapterId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: ChapterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<ChapterEntity>)

    @Query("UPDATE offline_chapters SET isOffline = :isOffline WHERE id = :chapterId")
    suspend fun updateOfflineStatus(chapterId: Int, isOffline: Boolean)

    @Delete
    suspend fun deleteChapter(chapter: ChapterEntity)

    @Query("DELETE FROM offline_chapters WHERE id = :chapterId")
    suspend fun deleteChapterById(chapterId: Int)

    @Query("DELETE FROM offline_chapters")
    suspend fun deleteAllChapters()

    @Query("SELECT COUNT(*) FROM offline_chapters WHERE isOffline = 1")
    suspend fun getOfflineChapterCount(): Int
}
