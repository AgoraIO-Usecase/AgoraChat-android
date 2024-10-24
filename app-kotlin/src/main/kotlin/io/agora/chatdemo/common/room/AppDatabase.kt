package io.agora.chatdemo.common.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import io.agora.chatdemo.BuildConfig
import io.agora.chatdemo.common.extensions.MD5
import io.agora.chatdemo.common.room.dao.DemoUserDao
import io.agora.chatdemo.common.room.entity.DemoUser

@Database(entities = [DemoUser::class], version = 3)
abstract class AppDatabase: RoomDatabase() {

    /**
     * Get the user data access object.
     */
    abstract fun userDao(): DemoUserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // 以下数据库升级设置，为升级数据库将清掉之前的数据，如果要保留数据，慎重采用此种方式
        // 可以采用addMigrations()的方式，进行数据库的升级
        fun getDatabase(context: Context, userId: String): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbName = (BuildConfig.AGORA_CHAT_APPKEY + userId).MD5()
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        dbName
                    )
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}