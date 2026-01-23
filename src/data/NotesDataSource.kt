package com.realityexpander.data

import com.realityexpander.data.collections.Note
import com.realityexpander.data.collections.User

interface NotesDataSource {
    suspend fun registerUser(user: User): Boolean
    suspend fun ifUserEmailExists(email: String): Boolean
    suspend fun ifUserIdExists(id: String): Boolean
    suspend fun getUserByEmail(email: String): User?
    suspend fun getEmailForUserId(userId: String): String?
    suspend fun checkPasswordForEmail(email: String, passwordToCheck: String): Boolean
    suspend fun getNotesForUserByEmail(email: String): List<Note>
    suspend fun saveNote(note: Note): Boolean
    suspend fun deleteNoteForUser(userId: String, noteId: String): Boolean
    suspend fun getNote(noteId: String): Note?
    suspend fun addOwnerToNote(userId: String, noteId: String): Boolean
    suspend fun isOwnerOfNote(userId: String, noteId: String): Boolean
}