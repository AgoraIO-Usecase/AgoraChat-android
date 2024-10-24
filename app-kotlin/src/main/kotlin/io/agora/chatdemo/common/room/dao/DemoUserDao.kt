package io.agora.chatdemo.common.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import io.agora.chatdemo.common.room.entity.DemoUser
import kotlinx.coroutines.flow.Flow

@Dao
interface DemoUserDao {

    @Query("SELECT * FROM DemoUser")
    fun getAll(): List<DemoUser>

    @Query("SELECT * FROM DemoUser WHERE userId = :userId")
    fun getUserById(userId: String): Flow<DemoUser?>

    @Query("SELECT * FROM DemoUser WHERE userId = :userId")
    fun getUser(userId: String): DemoUser?

    @Query("SELECT * FROM DemoUser WHERE userId IN (:userIds)")
    fun getUsersByIds(userIds: List<String>): Flow<List<DemoUser>>

    @Query("SELECT * FROM DemoUser WHERE name LIKE :name")
    fun getUsersByName(name: String?): Flow<List<DemoUser>>

    // Insert by DemoUser
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(user: DemoUser)

    // Insert DemoUser list
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUsers(users: List<DemoUser>)

    // Update
    @Query("UPDATE DemoUser SET name = :name, avatar = :avatar, remark = :remark WHERE userId = :userId")
    fun updateUser(userId: String, name: String, avatar: String, remark: String)

    // Update by DemoUser
    @Update
    fun updateUser(user: DemoUser)

    // Update DemoUser list
    @Update
    fun updateUsers(users: List<DemoUser>)

    // Update name
    @Query("UPDATE DemoUser SET name = :name WHERE userId = :userId")
    fun updateUserName(userId: String, name: String)

    // Update avatar
    @Query("UPDATE DemoUser SET avatar = :avatar WHERE userId = :userId")
    fun updateUserAvatar(userId: String, avatar: String)

    // Update remark
    @Query("UPDATE DemoUser SET remark = :remark WHERE userId = :userId")
    fun updateUserRemark(userId: String, remark: String)

    // Update update times
    @Query("UPDATE DemoUser SET update_times = update_times + 1 WHERE userId = :userId")
    fun updateUserTimes(userId: String)

    // Update users update times
    @Query("UPDATE DemoUser SET update_times = update_times + 1 WHERE userId IN (:userIds)")
    fun updateUsersTimes(userIds: List<String>)

    /**
     * Reset the update times of all users.
     */
    @Query("UPDATE DemoUser SET update_times = 0")
    fun resetUsersTimes()

    // Delete
    @Delete
    fun deleteUser(user: DemoUser)

    // Delete user list
    @Delete
    fun deleteUsers(users: List<DemoUser>)

    // Delete by userId
    @Query("DELETE FROM DemoUser WHERE userId = :userId")
    fun deleteUserById(userId: String)

    // Delete by userId list
    @Query("DELETE FROM DemoUser WHERE userId IN (:userIds)")
    fun deleteUsersByIds(userIds: List<String>)


    // Delete all users
    @Query("DELETE FROM DemoUser")
    fun deleteAll()
}