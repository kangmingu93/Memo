package com.line.mymemo.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.line.mymemo.entity.ImageEntity

@Dao
interface ImageDao {

    @Query("SELECT * FROM image_table WHERE memoId=:memoId")
    fun getAllImages(memoId: Long?): List<ImageEntity>

    @Query("DELETE FROM image_table")
    fun deleteAllImages()

    @Query("DELETE FROM image_table WHERE memoId=:memoId")
    fun deleteAllImages(memoId: Long?)

    @Insert(onConflict = REPLACE)
    fun insertImage(vararg image: ImageEntity)

    @Insert(onConflict = REPLACE)
    fun insertAllImages(images: List<ImageEntity>)

    @Update
    fun updateImage(vararg image: ImageEntity)

    @Delete
    fun deleteImage(vararg image: ImageEntity)

}