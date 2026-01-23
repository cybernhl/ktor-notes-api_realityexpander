package com.realityexpander.data

import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.util.AttributeKey

/**
 * Ktor Feature to install a NotesDataSource instance, making it available
 * throughout the application via `call.attributes.get(NotesDataSource.key)`.
 */
class DataSourceFeature(configuration: Configuration) {
    val dataSource: NotesDataSource = configuration.dataSource

    class Configuration {
        lateinit var dataSource: NotesDataSource
    }

    companion object Feature : ApplicationFeature<Application, Configuration, DataSourceFeature> {
        override val key = AttributeKey<DataSourceFeature>("DataSourceFeature")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): DataSourceFeature {
            val configuration = Configuration().apply(configure)
            return DataSourceFeature(configuration)
        }
    }
}