package com.realityexpander.data.table

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.realityexpander.data.collections.User

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)] // 確保 email 是唯一的，並為其建立索引
)
data class UserTable(
    @PrimaryKey
    val id: String,
    val email: String,
    val password: String // 儲存加鹽後的密碼 hash
) {
    fun toUser(): User = User(id, email, password)
}
