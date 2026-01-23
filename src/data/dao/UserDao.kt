package com.realityexpander.data.dao

import androidx.room.*
import com.realityexpander.data.table.UserTable

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun registerUser(user: UserTable)

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserTable?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: String): UserTable?
}
