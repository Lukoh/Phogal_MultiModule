package com.goforer.phogal.di.module

import android.content.Context
import androidx.room.Room
import com.goforer.phogal.data.datasource.local.room.PhogalDatabase
import com.goforer.phogal.data.datasource.local.room.converter.PhogalTypeConverters
import com.goforer.phogal.data.datasource.local.room.dao.PhotoFeedDao
import com.goforer.phogal.data.datasource.local.room.dao.PictureDao
import com.goforer.phogal.data.datasource.local.room.dao.RemoteKeyDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

/**
 * Hilt wiring for the Room database — the local Single Source of Truth (SSOT).
 *
 * The [PhogalTypeConverters] is a *provided* converter: it is constructed here
 * with the app-wide [Json] from [NetworkModule.provideJson] and handed to Room via
 * `addTypeConverter(...)`, so DB (de)serialization uses the exact configuration
 * the REST layer uses.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Singleton
    @Provides
    fun providePhogalDatabase(context: Context, json: Json): PhogalDatabase =
        Room.databaseBuilder(context, PhogalDatabase::class.java, PhogalDatabase.NAME)
            .addTypeConverter(PhogalTypeConverters(json))
            // Cache-only DB: on a schema bump it is always safe to rebuild from the network.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun providePhotoFeedDao(database: PhogalDatabase): PhotoFeedDao = database.photoFeedDao()

    @Provides
    fun providePictureDao(database: PhogalDatabase): PictureDao = database.pictureDao()

    @Provides
    fun provideRemoteKeyDao(database: PhogalDatabase): RemoteKeyDao = database.remoteKeyDao()
}
