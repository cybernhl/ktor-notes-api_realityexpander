package com.realityexpander.data

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.realityexpander.data.collections.Note
import com.realityexpander.data.collections.User
import com.realityexpander.data.table.NoteOwnerCrossRef
import com.realityexpander.data.table.NoteTable
import com.realityexpander.data.table.UserTable
import com.realityexpander.security.isPasswordAndHashWithSaltMatching
import kotlinx.coroutines.Dispatchers
import org.bson.types.ObjectId
import java.io.File

class SqliteNotesDataSource : NotesDataSource {

    private val dbFile = File("build/db", "notes_room.db").apply {
        parentFile.mkdirs()
    }

    private val db: NotesSqliteDatabase by lazy {
        Room.databaseBuilder<NotesSqliteDatabase>(
            name = dbFile.absolutePath,
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }

    private val _userDao by lazy {  db.userDao() }
    private val _noteDao by lazy {  db.noteDao() }
    private val userDao = _userDao
    private val noteDao = _noteDao

    // User-related functions
    override suspend fun registerUser(user: User): Boolean {
        return try {
            userDao.registerUser(user.toUserTable())
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun ifUserEmailExists(email: String): Boolean {
        return userDao.getUserByEmail(email) != null
    }

    override suspend fun ifUserIdExists(id: String): Boolean {
        return userDao.getUserById(id) != null
    }

    override suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)?.toUser()
    }

    override suspend fun checkPasswordForEmail(email: String, passwordToCheck: String): Boolean {
        val userTable = userDao.getUserByEmail(email) ?: return false
        // For legacy passwords that are not hashed, we check them directly.
        if (passwordToCheck == userTable.password) return true

        return isPasswordAndHashWithSaltMatching(passwordToCheck, userTable.password)
    }

    // Note-related functions
    override suspend fun getNotesForUserByEmail(email: String): List<Note> {
        val user = userDao.getUserByEmail(email) ?: return emptyList()
        val noteTables = noteDao.getNotesForUser(user.id)

        return noteTables.map { noteTable ->
            val owners = noteDao.getOwnersForNote(noteTable.id).map { it.userId }
            noteTable.toNote(owners)
        }
    }

    override suspend fun saveNote(note: Note): Boolean {
        val noteId = note.id ?: ObjectId().toString()
        val noteTable = note.copy(id = noteId).toNoteTable()
        val noteExists = noteDao.getNoteById(noteId) != null

        return try {
            if (noteExists) {
                noteDao.updateNote(noteTable.copy(updatedAt = System.currentTimeMillis()))
            } else {
                noteDao.saveNote(noteTable.copy(createdAt = System.currentTimeMillis()))
                // Also add initial owners
                note.owners.forEach { ownerId ->
                    noteDao.addOwnerToNote(NoteOwnerCrossRef(noteId, ownerId))
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun deleteNoteForUser(userId: String, noteId: String): Boolean {
        val owners = noteDao.getOwnersForNote(noteId)
        if (owners.none { it.userId == userId }) {
            println("Note with id=$noteId not found for User with id=$userId")
            return false // User is not an owner
        }

        return try {
            // If only one owner, delete the note entirely.
            if (owners.size == 1) {
                noteDao.deleteNoteById(noteId)
            } else {
                // More than one owner, just remove this user's ownership.
                noteDao.removeOwnerFromNote(NoteOwnerCrossRef(noteId, userId))
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun getNote(noteId: String): Note? {
        val noteTable = noteDao.getNoteById(noteId) ?: return null
        val owners = noteDao.getOwnersForNote(noteId).map { it.userId }
        return noteTable.toNote(owners)
    }

    override suspend fun addOwnerToNote(userIdToAdd: String, noteId: String): Boolean {
        return try {
            noteDao.addOwnerToNote(NoteOwnerCrossRef(noteId, userIdToAdd))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun isOwnerOfNote(userId: String, noteId: String): Boolean {
        return noteDao.getOwnersForNote(noteId).any { it.userId == userId }
    }

    override suspend fun getEmailForUserId(userId: String): String? {
        return userDao.getUserById(userId)?.email
    }
}

