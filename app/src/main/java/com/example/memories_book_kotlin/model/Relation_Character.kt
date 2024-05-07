package com.example.memories_book_kotlin.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
class Relation_Character (

    @ColumnInfo(name = "image")
    var image : ByteArray?  ,

    @ColumnInfo(name = "full name")
    var fullName : String,

    @ColumnInfo(name = "relation")
    var relation : String,

    @ColumnInfo(name = "first memory")
    var firstMemory : String,

    @ColumnInfo(name = "second memory")
        var secondMemory : String,

    @ColumnInfo(name = "poem")
    var poem : String
    ) : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id=0


}