package com.line.mymemo.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "memo_table")
data class MemoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long?,
    var title: String?,
    var contents: String?,
    var regdate: String?
) : Serializable {
    constructor():this(0, "", "", Date().toString())
}