package com.realityexpander.data.table

import androidx.room.Entity
import androidx.room.ForeignKey

/*
原有的 Note 類別結構較為複雜，包含 id, title, content, date, owners 等。在關聯式資料庫 (SQLite) 中，owners 這種多對多關係需要透過一個中間表來處理。
為了處理筆記和用戶之間的多對多關係（一個筆記可以有多個擁有者，一個用戶可以擁有多個筆記），我們需要建立一個關聯交叉引用 (cross-reference) 表。
 */

@Entity(
    tableName = "note_owner_cross_ref",
    primaryKeys = ["noteId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = NoteTable::class,
            parentColumns = ["id"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE // 如果筆記被刪除，相關的擁有者關聯也一併刪除
        ),
        ForeignKey(
            entity = UserTable::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE // 如果用戶被刪除，相關的擁有者關聯也一併刪除
        )
    ]
)
data class NoteOwnerCrossRef(
    val noteId: String,
    val userId: String
)