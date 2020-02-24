package com.line.mymemo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import com.line.mymemo.dao.ImageDao
import com.line.mymemo.dao.MemoDao
import com.line.mymemo.entity.ImageEntity
import com.line.mymemo.entity.MemoEntity

@Database(entities = [MemoEntity::class, ImageEntity::class], version = 1, exportSchema = false)
abstract class RoomDatabase : androidx.room.RoomDatabase() {

    abstract fun memoDao(): MemoDao
    abstract fun imageDao(): ImageDao

    companion object {

        @Volatile private var INSTANCE: RoomDatabase? = null

        @Synchronized
        fun getInstance(context: Context): RoomDatabase? {
            if (INSTANCE == null) {
                INSTANCE = buildDatabase(context)
            }
            return INSTANCE
        }

        fun destoryInstance() {
            INSTANCE = null
        }

        private fun buildDatabase(context: Context) : RoomDatabase {
            return Room.databaseBuilder(context.applicationContext, RoomDatabase::class.java, "memo.db").build()
        }

    }
}