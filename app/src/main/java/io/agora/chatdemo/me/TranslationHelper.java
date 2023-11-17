package io.agora.chatdemo.me;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.general.constant.DemoConstant;

public class TranslationHelper {

    public static String[] getLanguageByType(int type,String conversationId){
        String[] language = new String[2];
        String json = "";
        if (type == DemoConstant.TRANSLATION_TYPE_MESSAGE){
            json = DemoHelper.getInstance().getModel().getTargetLanguage();
        }else if (type == DemoConstant.TRANSLATION_TYPE_AUTO){
            json = DemoHelper.getInstance().getModel().getAutoTargetLanguage();
        }else {
            json = DemoHelper.getInstance().getModel().getPushLanguage();
        }
        try {
            if (!TextUtils.isEmpty(json)){
                JSONObject jsonObject = new JSONObject(json);
                if (type == DemoConstant.TRANSLATION_TYPE_AUTO){
                    if (jsonObject.has(conversationId)){
                        JSONObject s = jsonObject.getJSONObject(conversationId);
                        for (Iterator<String> it = s.keys(); it.hasNext(); ) {
                            String key = it.next();
                            language[0] = key;
                            language[1] = s.getString(key);
                        }
                    }
                }else {
                    for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
                        String key = it.next();
                        language[0] = key;
                        language[1] = jsonObject.getString(key);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return language;
        }
        return language;
    }

}
