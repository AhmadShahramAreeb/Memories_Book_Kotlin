package com.example.memories_book_kotlin.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.memories_book_kotlin.model.Relation_Character

@Database(entities = [Relation_Character::class], version = 1)
abstract class CharacterDatabase : RoomDatabase() {
    //bu sınıfta yapılan tek işlem characterDao döndürmek
    abstract fun characterDao(): CharacterDao
}