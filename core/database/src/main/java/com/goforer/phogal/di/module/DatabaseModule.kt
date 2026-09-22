package com.goforer.phogal.di.module

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            recreateCacheTables(db)
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            recreateCacheTables(db)
        }
    }

    private fun recreateCacheTables(db: SupportSQLiteDatabase) {
        db.execSQL("DROP TABLE IF EXISTS photo_feed")
        db.execSQL("DROP TABLE IF EXISTS picture")
        db.execSQL("DROP TABLE IF EXISTS remote_key")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS photo_feed (
                feed_key TEXT NOT NULL,
                photo_id TEXT NOT NULL,
                photo TEXT NOT NULL,
                position INTEGER NOT NULL,
                cached_at INTEGER NOT NULL,
                PRIMARY KEY(feed_key, photo_id)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS picture (
                id TEXT NOT NULL PRIMARY KEY,
                picture TEXT NOT NULL,
                cached_at INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS remote_key (
                feed_key TEXT NOT NULL PRIMARY KEY,
                next_page INTEGER,
                last_refreshed_at INTEGER NOT NULL
            )
            """.trimIndent(),
        )
    }

    @Singleton
    @Provides
    fun providePhogalDatabase(context: Context, json: Json): PhogalDatabase =
        Room.databaseBuilder(context, PhogalDatabase::class.java, PhogalDatabase.NAME)
            .addTypeConverter(PhogalTypeConverters(json))
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            // Cache-only DB: on a schema bump it is always safe to rebuild from the network.
            .fallbackToDestructiveMigration(dropAllTables = true)
            .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
            .build()

    @Provides
    fun providePhotoFeedDao(database: PhogalDatabase): PhotoFeedDao = database.photoFeedDao()

    @Provides
    fun providePictureDao(database: PhogalDatabase): PictureDao = database.pictureDao()

    @Provides
    fun provideRemoteKeyDao(database: PhogalDatabase): RemoteKeyDao = database.remoteKeyDao()
}
