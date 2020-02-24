package com.line.mymemo.entity

import androidx.room.Embedded
import androidx.room.Relation
import java.io.Serializable

data class MemoWithImageEntity (
    @Embedded val memo: MemoEntity?,
    @Relation(parentColumn = "id", entityColumn = "memoId", entity = ImageEntity::class) val images: List<ImageEntity>?
) : Serializable {
    constructor():this(MemoEntity(), ArrayList())
}