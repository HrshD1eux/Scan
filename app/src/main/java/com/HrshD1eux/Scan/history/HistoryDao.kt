package com.HrshD1eux.Scan.history

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_table ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(historyEntity: HistoryEntity)

    @Query("DELETE FROM history_table WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM history_table")
    suspend fun clearHistory()
}
