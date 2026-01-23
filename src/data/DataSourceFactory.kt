package com.realityexpander.data

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


