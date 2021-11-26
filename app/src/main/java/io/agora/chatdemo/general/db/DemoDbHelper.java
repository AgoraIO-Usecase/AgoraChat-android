package io.agora.chatdemo.general.db;

import android.content.Context;
import android.text.TextUtils;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;

import io.agora.chatdemo.general.db.dao.EmUserDao;
import io.agora.chatdemo.general.utils.MD5;
import io.agora.util.EMLog;

public class DemoDbHelper {
    private static final String TAG = "DemoDbHelper";
    private static DemoDbHelper instance;
    private Context mContext;
    private String currentUser;
    private AppDatabase mDatabase;
    private final MutableLiveData<Boolean> mIsDatabaseCreated = new MutableLiveData<>();

    private DemoDbHelper(Context context){
        this.mContext = context.getApplicationContext();
    }

    public static DemoDbHelper getInstance(Context context) {
        if(instance == null) {
            synchronized (DemoDbHelper.class) {
                if(instance == null) {
                    instance = new DemoDbHelper(context);
                }
            }
        }
        return instance;
    }

    /**
     * Initialize the database
     * @param user
     */
    public void initDb(String user) {
        if(currentUser != null) {
            if(TextUtils.equals(currentUser, user)) {
                EMLog.i(TAG, "you have opened the db");
                return;
            }
            closeDb();
        }
        this.currentUser = user;
        String userMd5 = MD5.encrypt2MD5(user);
        // The following database upgrade settings, in order to upgrade the database will clear the previous data,
        // if you want to keep the data, use this method carefully
        // You can use addMigrations() to upgrade the database
        String dbName = String.format("em_%1$s.db", userMd5);
        EMLog.i(TAG, "db name = "+dbName);
        mDatabase = Room.databaseBuilder(mContext, AppDatabase.class, dbName)
                        .allowMainThreadQueries()
                        .fallbackToDestructiveMigration()
                        .build();
        mIsDatabaseCreated.postValue(true);
    }

    public LiveData<Boolean> getDatabaseCreatedObservable() {
        return mIsDatabaseCreated;
    }

    /**
     * Close database
     */
    public void closeDb() {
        if(mDatabase != null) {
            mDatabase.close();
            mDatabase = null;
        }
        currentUser = null;
    }

    public EmUserDao getUserDao() {
        if(mDatabase != null) {
            return mDatabase.userDao();
        }
        EMLog.i(TAG, "get userDao failed, should init db first");
        return null;
    }

}
