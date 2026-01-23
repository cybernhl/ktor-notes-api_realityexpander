package com.realityexpander.data.collections

import com.realityexpander.data.table.NoteTable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Note(
    @BsonId // binary json object id marker
    val id: String? = ObjectId().toString(),

    val title: String,
    val content: String,
    val date: String,
    val dateMillis: Long = 0,
    val owners: List<String>,  // list of owner id's that have access to this note
    val color: String,
    var createdAt: Long = 0,  // Milliseconds since epoch, 0 if not set
    var updatedAt: Long = 0,  // Milliseconds since epoch, 0 if not set
) {
    fun toNoteTable(): NoteTable = NoteTable(
        id = this.id ?: ObjectId().toString(),
        title = this.title,
        content = this.content,
        date = this.date,
        dateMillis = this.dateMillis,
        color = this.color,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
