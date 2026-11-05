package com.lamontlabs.spark.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ReflectionEntity::class], version = 1, exportSchema = true)
abstract class SparkDatabase : RoomDatabase() {
    abstract fun reflectionDao(): ReflectionDao

    companion object {
        @Volatile private var instance: SparkDatabase? = null

        fun get(context: Context): SparkDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SparkDatabase::class.java,
                    "spark-private.db"
                )
                    // Never use destructive migration: journal data must survive upgrades.
                    .build()
                    .also { instance = it }
            }
    }
}
