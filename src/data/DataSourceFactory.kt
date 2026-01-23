package com.realityexpander.data

import com.realityexpander.routes.loginRoute
import com.realityexpander.routes.notesRoute
import com.realityexpander.routes.registerRoute
import com.realityexpander.routes.styleRoute
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*
import io.ktor.util.*

//highlight-start
/**
 * Factory object to create the appropriate NotesDataSource based on environment.
 */
object DataSourceFactory {
    enum class DataSourceType {
        MONGO, SQLITE
    }

    fun create(type: DataSourceType): NotesDataSource {
        return when (type) {
            DataSourceType.MONGO -> {
                val mongoDataSource = MongoNotesDataSource()
                // Print mongo environment variables - For debugging only
                mongoDataSource.printMongoEnv()
                mongoDataSource
            }
            DataSourceType.SQLITE -> SqliteNotesDataSource()
        }
    }
}


