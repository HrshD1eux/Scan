package com.HrshD1eux.Scan.history

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history_table")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val primaryValue: String,
    val timestamp: Long = System.currentTimeMillis()
)
