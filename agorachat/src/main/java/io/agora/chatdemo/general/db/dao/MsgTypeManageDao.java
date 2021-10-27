package io.agora.chatdemo.general.db.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import io.agora.chatdemo.general.db.entity.MsgTypeManageEntity;

@Dao
public interface MsgTypeManageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    List<Long> insert(MsgTypeManageEntity... entities);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(MsgTypeManageEntity... entities);

    @Query("select * from em_msg_type")
    List<MsgTypeManageEntity> loadAllMsgTypeManage();

    @Query("select * from em_msg_type where type = :type")
    MsgTypeManageEntity loadMsgTypeManage(String type);

    @Delete
    void delete(MsgTypeManageEntity... entities);
}
