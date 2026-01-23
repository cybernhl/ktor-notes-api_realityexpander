package com.realityexpander.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.realityexpander.data.dao.NoteDao
import com.realityexpander.data.dao.UserDao
import com.realityexpander.data.table.NoteOwnerCrossRef
import com.realityexpander.data.table.NoteTable
import com.realityexpander.data.table.UserTable

@Database(
    entities = [UserTable::class, NoteTable::class, NoteOwnerCrossRef::class], // 註冊所有資料表
    version = 1,
    exportSchema = true // 建議開啟以便於版本遷移
)
abstract class NotesSqliteDatabase : RoomDatabase() {
    // 提供 DAO 的抽象方法
    abstract fun userDao(): UserDao
    abstract fun noteDao(): NoteDao
}