package com.realityexpander.routes

import com.realityexpander.dataSource
import com.realityexpander.data.collections.Note
import com.realityexpander.data.requests.AddOwnerIdToNoteIdRequest
import com.realityexpander.data.requests.DeleteNoteRequest
import com.realityexpander.data.responses.SimpleResponse
import com.realityexpander.data.responses.SimpleResponseWithData
import io.ktor.http.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.notesRoute() {

    // Authenticated get request to get all notes for a user
    route("/getNotes") {
        //highlight-start
        // Specify the provider name configured in Application.kt
        authenticate("auth-basic") {
            //highlight-end
            get {
                // get the email from the authenticated user object
                val principal = call.principal<UserIdPrincipal>()
                if(principal == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }
                val email = principal.name
                val notes = call.dataSource.getNotesForUserByEmail(email)

                call.respond(
                    OK,
                    SimpleResponseWithData<List<Note>>(
                        isSuccessful = true, statusCode = OK,
                        message = "${notes.size} note${addPostfixS(notes)} found",
                        data = notes
                    )
                )
            }
        }
    }

    route("/saveNote") {
        //highlight-start
        authenticate("auth-basic") {
            //highlight-end
            post {
                val note = try {
                    call.receive<Note>()
                } catch (e: Exception) {
                    call.respond(
                        OK,
                        SimpleResponse(
                            isSuccessful = false,
                            statusCode = BadRequest,
                            message = "Invalid note format"
                        )
                    )
                    return@post
                }

                val principal = call.principal<UserIdPrincipal>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }
                val email = principal.name
                val userExists = call.dataSource.ifUserEmailExists(email)
                if (userExists) {
                    val acknowledged = call.dataSource.saveNote(note)

                    if (acknowledged) {
                        call.respond(
                            OK,
                            SimpleResponseWithData(
                                isSuccessful = true,
                                statusCode = OK,
                                message = "Note added",
                                data = note
                            )
                        )
                    } else {
                        call.respond(
                            OK,
                            SimpleResponseWithData(
                                isSuccessful = false,
                                statusCode = InternalServerError,
                                message = "Note not added",
                                data = note
                            )
                        )
                    }
                } else {
                    call.respond(
                        BadRequest,
                        SimpleResponse(
                            isSuccessful = false,
                            statusCode = BadRequest,
                            message = "User not found"
                        )
                    )
                }
            }
        }
    }

    route("/deleteNote") {
        //highlight-start
        authenticate("auth-basic") { // Authenticated post request to delete a note
            //highlight-end
            post {
                val principal = call.principal<UserIdPrincipal>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }
                val email = principal.name

                val request = try {
                    call.receive<DeleteNoteRequest>()
                } catch (e: Exception) {
                    call.respond(
                        BadRequest,
                        SimpleResponse(
                            isSuccessful = false,
                            statusCode = BadRequest,
                            message = "Invalid delete note format"
                        )
                    )
                    return@post
                }

                val userExists = call.dataSource.ifUserEmailExists(email)
                if (userExists) {
                    val userId = call.dataSource.getUserByEmail(email)!!.id

                    val acknowledged = call.dataSource.deleteNoteForUser(userId, request.id)

                    if (acknowledged) {
                        val note = call.dataSource.getNote(request.id)

                        if (note != null) {
                            call.respond(
                                OK,
                                SimpleResponseWithData(
                                    isSuccessful = true,
                                    statusCode = OK,
                                    message = "Owner removed from note",
                                    data = note
                                )
                            )
                        } else {
                            call.respond(
                                OK,
                                SimpleResponseWithData<Note?>(
                                    isSuccessful = true,
                                    statusCode = OK,
                                    message = "Note deleted",
                                    data = null
                                )
                            )
                        }
                    } else {
                        call.respond(
                            InternalServerError,
                            SimpleResponse(
                                isSuccessful = false,
                                statusCode = InternalServerError,
                                message = "Note not deleted (Owner not authorized)"
                            )
                        )
                    }
                } else {
                    call.respond(
                        BadRequest,
                        SimpleResponse(
                            isSuccessful = false,
                            statusCode = BadRequest,
                            message = "User not found"
                        )
                    )
                }
            }
        }
    }

    route("/addOwnerIdToNoteId") {
        //highlight-start
        authenticate("auth-basic") { // Authenticated post request to add an owner to a note
            //highlight-end
            post {
                val principal = call.principal<UserIdPrincipal>()
                if (principal == null) {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@post
                }
                // val email = principal.name // This variable is not used in this block

                val request = try {
                    call.receive<AddOwnerIdToNoteIdRequest>()
                } catch (e: Exception) {
                    call.respond(
                        BadRequest,
                        SimpleResponse(
                            isSuccessful = false,
                            statusCode = BadRequest,
                            message = "Invalid 'add owner to note' format"
                        )
                    )
                    return@post
                }

                if (call.dataSource.ifUserIdExists(request.ownerIdToAdd)) {

                    if (call.dataSource.isOwnerOfNote(request.ownerIdToAdd, request.noteId)) {
                        val note = call.dataSource.getNote(request.noteId)!!
                        call.respond(
                            OK,
                            SimpleResponseWithData<Note?>(
                                isSuccessful = false,
                                statusCode = OK,
                                message = "${call.dataSource.getEmailForUserId(request.ownerIdToAdd)} is already an owner of this note",
                                data = note
                            )
                        )
                        return@post
                    }

                    val acknowledged = call.dataSource.addOwnerToNote(request.ownerIdToAdd, request.noteId)

                    if (acknowledged) {
                        val note = call.dataSource.getNote(request.noteId)

                        if (note != null) {
                            call.respond(
                                OK,
                                SimpleResponseWithData(
                                    isSuccessful = true,
                                    statusCode = OK,
                                    message = "Owner added to note, " +
                                            "${call.dataSource.getEmailForUserId(request.ownerIdToAdd)} can now access this note",
                                    data = note
                                )
                            )
                        } else {
                            call.respond(
                                InternalServerError,
                                SimpleResponseWithData<Note?>(
                                    isSuccessful = false,
                                    statusCode = InternalServerError,
                                    message = "Note not updated - cant find note",
                                    data = null
                                )
                            )
                        }
                    } else {
                        call.respond(
                            InternalServerError,
                            SimpleResponse(
                                isSuccessful = false,
                                statusCode = InternalServerError,
                                message = "Note not updated - Update failed"
                            )
                        )
                    }
                } else {
                    call.respond(
                        BadRequest,
                        SimpleResponse(
                            isSuccessful = false,
                            statusCode = BadRequest,
                            message = "User was not found - Can't add owner to note"
                        )
                    )
                }
            }
        }
    }

    get("/getOwnerIdForEmail") {
        val email = call.parameters["email"]!!
        val user = call.dataSource.getUserByEmail(email)
        if (user != null) {
            call.respond(
                OK,
                SimpleResponseWithData(
                    isSuccessful = true,
                    statusCode = OK,
                    message = "User found",
                    data = user.id
                )
            )
        } else {
            call.respond(
                BadRequest,
                SimpleResponse(
                    isSuccessful = false,
                    statusCode = BadRequest,
                    message = "User not found"
                )
            )
        }
    }

    get("/getEmailForOwnerId") {
        val ownerId = call.parameters["ownerId"]!!
        val email = call.dataSource.getEmailForUserId(ownerId)
        if (email != null) {
            call.respond(
                OK,
                SimpleResponseWithData(
                    isSuccessful = true,
                    statusCode = OK,
                    message = "User found",
                    data = email
                )
            )
        } else {
            call.respond(
                BadRequest,
                SimpleResponse(
                    isSuccessful = false,
                    statusCode = BadRequest,
                    message = "User not found"
                )
            )
        }
    }
}

// utility function
fun addPostfixS(notes: List<Note>): String {
    return if (notes.size > 1) "s" else ""
}
