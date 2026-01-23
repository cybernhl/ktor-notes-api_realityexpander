package com.realityexpander.data.table

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.realityexpander.data.collections.Note

@Entity(tableName = "notes")
data class NoteTable(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val dateMillis: Long,
    val color: String,
    var createdAt: Long,
    var updatedAt: Long
) {
    fun toNote(owners: List<String>): Note = Note(
        id = this.id,
        title = this.title,
        content = this.content,
        date = this.date,
        dateMillis = this.dateMillis,
        color = this.color,
        owners = owners,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
