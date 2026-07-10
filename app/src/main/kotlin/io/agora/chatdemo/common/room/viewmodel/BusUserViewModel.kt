package io.agora.chatdemo.common.room.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.agora.chatdemo.common.room.dao.DemoUserDao
import io.agora.chatdemo.common.room.entity.DemoUser

class BusUserViewModel(private val userDao: DemoUserDao): ViewModel() {

    /**
     * Query all users from the database.
     * @return All users in the database.
     */
    fun getAllUsers() = userDao.getAll()

    /**
     * Query user by id from the database.
     * @param userId The id of the user.
     * @return The user with the specified id.
     */
    fun getUserById(userId: String) = userDao.getUserById(userId)

    /**
     * Query users by ids from the database.
     * @param userIds The ids of the users.
     * @return The users with the specified ids.
     */
    fun getUsersByIds(userIds: List<String>) = userDao.getUsersByIds(userIds)

    /**
     * Query users by name from the database.
     * @param name The name of the user.
     * @return The users with the specified name.
     */
    fun getUsersByName(name: String?) = userDao.getUsersByName(name)

    /**
     * Insert a user into the database.
     * @param user The user to be inserted.
     */
    fun insertUser(user: DemoUser) = userDao.insertUser(user)

    /**
     * Insert a list of users into the database.
     * @param users The list of users to be inserted.
     */
    fun insertUsers(users: List<DemoUser>) = userDao.insertUsers(users)

    /**
     * Update a user in the database.
     * @param userId The id of the user.
     * @param name The name of the user.
     * @param avatar The avatar of the user.
     * @param remark The remark of the user.
     */
    fun updateUser(userId: String, name: String, avatar: String, remark: String) = userDao.updateUser(userId, name, avatar, remark)

    /**
     * Update a user in the database.
     * @param user The user to be updated.
     */
    fun updateUser(user: DemoUser) = userDao.updateUser(user)

    /**
     * Update a list of users in the database.
     * @param users The list of users to be updated.
     */
    fun updateUsers(users: List<DemoUser>) = userDao.updateUsers(users)

    /**
     * Update the name of a user in the database.
     * @param userId The id of the user.
     * @param name The name of the user.
     */
    fun updateUserName(userId: String, name: String) = userDao.updateUserName(userId, name)

    /**
     * Update the avatar of a user in the database.
     * @param userId The id of the user.
     * @param avatar The avatar of the user.
     */
    fun updateUserAvatar(userId: String, avatar: String) = userDao.updateUserAvatar(userId, avatar)

    /**
     * Update the remark of a user in the database.
     * @param userId The id of the user.
     * @param remark The remark of the user.
     */
    fun updateUserRemark(userId: String, remark: String) = userDao.updateUserRemark(userId, remark)

    /**
     * Delete a user from the database.
     * @param user The user to be deleted.
     */
    fun deleteUser(user: DemoUser) = userDao.deleteUser(user)

    /**
     * Delete a list of users from the database.
     * @param users The list of users to be deleted.
     */
    fun deleteUsers(users: List<DemoUser>) = userDao.deleteUsers(users)

    /**
     * Delete a user from the database.
     * @param userId The id of the user to be deleted.
     */
    fun deleteUserById(userId: String) = userDao.deleteUserById(userId)

    /**
     * Delete a list of users from the database.
     * @param userIds The ids of the users to be deleted.
     */
    fun deleteUsersByIds(userIds: List<String>) = userDao.deleteUsersByIds(userIds)

    /**
     * Delete all users from the database.
     */
    fun deleteAllUsers() = userDao.deleteAll()
}

class BusUserViewModelFactory(private val userDao: DemoUserDao): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BusUserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BusUserViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}