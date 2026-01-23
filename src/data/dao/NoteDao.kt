package com.realityexpander.data.dao

import androidx.room.*
import com.realityexpander.data.table.NoteOwnerCrossRef
import com.realityexpander.data.table.NoteTable

@Dao
interface NoteDao {
    // --- Note 操作 ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveNote(note: NoteTable)

    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteById(noteId: String): NoteTable?

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: String)

    @Update
    suspend fun updateNote(note: NoteTable)

    // --- Owner 關聯操作 ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOwnerToNote(crossRef: NoteOwnerCrossRef)

    @Delete
    suspend fun removeOwnerFromNote(crossRef: NoteOwnerCrossRef)

    @Query("SELECT * FROM note_owner_cross_ref WHERE noteId = :noteId")
    suspend fun getOwnersForNote(noteId: String): List<NoteOwnerCrossRef>

    // --- 複合查詢 ---
    @Transaction
    @Query("""
        SELECT * FROM notes 
        WHERE id IN (
            SELECT noteId FROM note_owner_cross_ref WHERE userId = :userId
        )
    """)
    suspend fun getNotesForUser(userId: String): List<NoteTable>
}
