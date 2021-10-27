package io.agora.chatdemo.general.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import io.agora.chatdemo.general.db.converter.DateConverter;
import io.agora.chatdemo.general.db.dao.EmUserDao;
import io.agora.chatdemo.general.db.dao.InviteMessageDao;
import io.agora.chatdemo.general.db.dao.MsgTypeManageDao;
import io.agora.chatdemo.general.db.entity.EmUserEntity;


@Database(entities = {EmUserEntity.class},
        version = 1)
@TypeConverters(DateConverter.class)
public abstract class AppDatabase extends RoomDatabase {

    public abstract EmUserDao userDao();

    public abstract InviteMessageDao inviteMessageDao();

    public abstract MsgTypeManageDao msgTypeManageDao();
}
