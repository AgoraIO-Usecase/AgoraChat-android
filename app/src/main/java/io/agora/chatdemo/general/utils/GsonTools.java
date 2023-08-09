package io.agora.chatdemo.general.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class GsonTools {
   public GsonTools(){

   }

   public static String createGsonString(Object object){
      Gson gson = new Gson();
      return gson.toJson(object);
   }

   public static <T> T changeGsonToBean(String gsonString,Class<T> cls){
      Gson gson = new Gson();
      return gson.fromJson(gsonString,cls);
   }

   public static <T> List<T> changeGsonToList(String gsonString,Class<T> cls){
      Gson gson = new Gson();
      return gson.fromJson(gsonString, new TypeToken<List<T>>(){}.getType());
   }

}
