package com.line.mymemo.dao

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.line.mymemo.entity.MemoEntity
import com.line.mymemo.entity.MemoWithImageEntity
import io.reactivex.Completable

@Dao
interface MemoDao {

    @Transaction
    @Query("SELECT * FROM memo_table ORDER BY id DESC")
    fun getAllMemos(): List<MemoWithImageEntity>

    @Transaction
    @Query("SELECT * FROM memo_table WHERE id=:id LIMIT 1")
    fun getMemo(id: Long?): MemoWithImageEntity

    @Transaction
    @Query("DELETE FROM memo_table")
    fun deleteAllMemos()

    @Insert(onConflict = REPLACE)
    fun insertMemo(memo: MemoEntity): Long

    @Update
    fun updateMemo(memo: MemoEntity): Int

    @Delete
    fun deleteMemo(memo: MemoEntity): Int

}