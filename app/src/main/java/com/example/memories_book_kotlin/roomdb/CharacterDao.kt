package com.example.memories_book_kotlin.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.memories_book_kotlin.model.Relation_Character
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable


@Dao
interface CharacterDao {
    @Query("SELECT * FROM Relation_Character")
    fun getAll() : Flowable<List<Relation_Character>>

    @Insert
    fun insert(relationCharacter : Relation_Character) : Completable

    @Delete
    fun delete(relationCharacter : Relation_Character) : Completable
}