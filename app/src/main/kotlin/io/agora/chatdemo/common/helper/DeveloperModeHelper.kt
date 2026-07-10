package io.agora.chatdemo.common.helper

import io.agora.chatdemo.DemoHelper

object DeveloperModeHelper {

    fun isDeveloperMode():Boolean{
        return DemoHelper.getInstance().getDataModel().isDeveloperMode()
    }

    fun setDeveloperMode(developerMode:Boolean){
        DemoHelper.getInstance().getDataModel().setDeveloperMode(developerMode)
    }

    fun setEnableCustom(enable:Boolean){
        DemoHelper.getInstance().getDataModel().enableCustomSet(enable)
    }

    fun isCustomSetEnable():Boolean{
        return DemoHelper.getInstance().getDataModel().isCustomSetEnable()
    }
}