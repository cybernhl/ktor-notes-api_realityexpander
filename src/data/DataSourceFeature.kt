package com.realityexpander.data

import io.ktor.util.AttributeKey
import io.ktor.server.application.*
/**
 * Ktor Feature to install a NotesDataSource instance, making it available
 * throughout the application via `call.attributes.get(NotesDataSource.key)`.
 */
class DataSourceFeature(configuration: Configuration) {
    val dataSource: NotesDataSource = configuration.dataSource

    class Configuration {
        lateinit var dataSource: NotesDataSource
    }

    companion object Feature :
        BaseApplicationPlugin<Application, Configuration, DataSourceFeature> {
        override val key = AttributeKey<DataSourceFeature>("DataSourceFeature")

        override fun install(pipeline: Application,configure: Configuration.() -> Unit): DataSourceFeature {
            val configuration = Configuration().apply(configure)
            val feature = DataSourceFeature(configuration)
            pipeline.attributes.put(key, feature)
            return feature
        }
    }
}