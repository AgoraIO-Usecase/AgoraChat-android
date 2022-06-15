package io.agora.chatdemo.general.repositories;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.agora.chatdemo.DemoApplication;

public class TestAvatarRepository {
    static List<Integer> avatarlist = new ArrayList<>();
    List<Integer> indexList = new ArrayList<>();
    static int SIZE = 12;
    static {
        Context context = DemoApplication.getInstance().getApplicationContext();
        for(int i = 1; i <= SIZE; i++){
            String name = "agora_avatar_"+i;
            int resId = context.getResources().getIdentifier(name,"drawable",context.getPackageName());
            avatarlist.add(resId);
        }
    }

    public TestAvatarRepository(){
        fillIndexList();
    }

    private void fillIndexList(){
        for(int i = 0; i < SIZE; i++){
            indexList.add(i);
        }
    }

    public int getAvatar(){
        if(indexList.size() != 0) {
            int index = new Random().nextInt(indexList.size());
            int gotIndex = indexList.remove(index);
            return avatarlist.get(gotIndex);
        }else{
            fillIndexList();
            return getAvatar();
        }
    }

    public String getAvatarUrl() {
        return String.valueOf(getAvatar());
    }

    public List<Integer> getAvatarList() {
        return avatarlist;
    }
}
