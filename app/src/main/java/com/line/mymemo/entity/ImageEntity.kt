package com.line.mymemo.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "image_table",
    foreignKeys = arrayOf(ForeignKey(
        entity = MemoEntity::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("memoId"),
        onDelete = ForeignKey.CASCADE)
    )
)
data class ImageEntity (
    @PrimaryKey(autoGenerate = true) val id: Long?,
    val memoId: Long?,
    var path: String?
) : Serializable {
    constructor():this(0, 0, "")
}