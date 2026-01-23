package com.realityexpander.routes

import com.realityexpander.data.collections.User
import com.realityexpander.data.requests.AccountRequest
import com.realityexpander.data.responses.SimpleResponse
import com.realityexpander.dataSource
import com.realityexpander.security.getHashWithSaltForPassword
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.*

fun Route.registerRoute() {
    route("/") {
        get {
            call.respondText("Hello World!")
        }
    }

    route("/register") {
        post {
            var isFromWeb = false

            // Get the registration parameters
            val request = try {
                // From web or mobile app?
                if (call.request.contentType() == ContentType.Application.FormUrlEncoded) { // from web
                    isFromWeb = true
                    //highlight-start
                    // In Ktor 2.x, receiveParameters() is no longer a suspend function.
                    val formParameters = call.receiveParameters()
                    //highlight-end
                    formParameters.let {
                        AccountRequest(it["email"] ?: "", it["password"] ?: "")
                    }
                } else { // from mobile app
                    call.receive<AccountRequest>()
                }
            } catch (e: ContentTransformationException) {
                call.respondPlatform(isFromWeb,
                    SimpleResponse(false, HttpStatusCode.ExpectationFailed, message = "Error: ${e.localizedMessage}")
                )
                return@post
            } catch (e: Exception) {
                call.respondPlatform(isFromWeb,
                    SimpleResponse(false, HttpStatusCode.BadRequest, message = "Error: ${e.localizedMessage}")
                )
                return@post
            }

            val userExists = call.dataSource.ifUserEmailExists(request.email)
            if (!userExists) {

                if (request.email.isBlank() || request.password.isBlank()) {
                    call.respondPlatform(isFromWeb,
                        SimpleResponse(false, HttpStatusCode.PreconditionFailed, message = "Error: Email or password is blank")
                    )
                    return@post
                }

                if ( call.dataSource.registerUser(
                        User(
                            email = request.email,
                            password = getHashWithSaltForPassword(request.password)
                        )
                    )
                ) {
                    call.respondPlatform(isFromWeb,
                        SimpleResponse(true, HttpStatusCode.Created, message = "User registered successfully")
                    )
                } else {
                    call.respondPlatform(isFromWeb,
                        SimpleResponse(false, HttpStatusCode.InternalServerError, message = "Error: User could not be registered")
                    )
                }
            } else {
                call.respondPlatform(isFromWeb,
                    SimpleResponse(false, HttpStatusCode.Conflict, message = "Error: User/Email already exists")
                )
            }
        }

        // Add a GET endpoint to serve the HTML registration form
        get {
            call.respondRegisterRawHTML()
        }
    }
}

// Common function to respond to either web or mobile platform
private suspend fun ApplicationCall.respondPlatform(
    isFromWeb: Boolean,
    response: SimpleResponse
) {
    when (isFromWeb) {
        true -> respondRawHTML(response)
        false -> respond(response.statusCode, response)
    }
}

// Function to serve the raw HTML for the registration form
private suspend fun ApplicationCall.respondRegisterRawHTML() {
    respondHtml {
        head {
            title { +"Register" }
            style {
                unsafe {
                    raw(
                        """
                        form {
                            background-color: #f0f0f0;
                        }
                        input {
                            font-size: 24px;
                        }
                        """.trimIndent()
                    )
                }
            }
        }
        body {
            h1 { +"Register" }
            form(action = "/register", method = FormMethod.post) {
                br { }
                input(type = InputType.email, name = "email") {
                    placeholder = "Email"
                }
                br { }
                br { }
                input(type = InputType.password, name = "password") {
                    placeholder = "Password"
                }
                br { }
                br { }
                input(type = InputType.submit) {
                    value = "Register"
                }
                br { }
                br { }
            }
        }
    }
}

// Function to serve the raw HTML for the response message
private suspend fun ApplicationCall.respondRawHTML(response: SimpleResponse) {
    respondHtml {
        head {
            title { if (response.isSuccessful) "Success" else "Error" }
            style {
                unsafe {
                    raw(
                        """
                        .status {
                            background-color: ${if (response.isSuccessful) "#008800" else "#880000"};
                            color: white;
                            padding: 10px;
                        }
                        """.trimIndent()
                    )
                }
            }
        }
        body {
            h1 { if (response.isSuccessful) "Success" else "Error" }
            br { }
            h2 {
                div(classes = "status") {
                    br { }
                    p { +response.message }
                    if (!response.isSuccessful) {
                        br { }
                        p { +"Response code: ${response.statusCode}" }
                    }
                    br { }
                }
            }
            br { }
        }
    }
}
