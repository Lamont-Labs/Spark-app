package com.lamontlabs.spark.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReflectionDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(reflection: ReflectionEntity)

    @Query("SELECT * FROM reflections WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): ReflectionEntity?

    @Query("SELECT * FROM reflections ORDER BY reflection_date DESC, created_at_utc DESC, id DESC")
    fun observeAll(): Flow<List<ReflectionEntity>>

    @Query("SELECT * FROM reflections WHERE reflection_date = :date ORDER BY created_at_utc DESC, id DESC")
    fun observeByDate(date: String): Flow<List<ReflectionEntity>>

    @Query("DELETE FROM reflections WHERE id = :id")
    suspend fun deleteById(id: String): Int
}
