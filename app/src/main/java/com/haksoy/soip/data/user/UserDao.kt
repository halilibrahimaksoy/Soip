package com.haksoy.soip.data.user

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM user_table WHERE uid=(:uid)")
    fun getUserData(uid: String): LiveData<User>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addUser(user: User)

}