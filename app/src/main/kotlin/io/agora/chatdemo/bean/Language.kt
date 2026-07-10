package io.agora.chatdemo.bean

data class Language(
    val type:LanguageType,
    val tag:String
)

enum class LanguageType(val value:String){
    ZH("zh"),
    EN("en");

    companion object {
        fun from(value: String): LanguageType {
            val types = LanguageType.values()
            val length = types.size
            for (i in 0 until length) {
                val type = types[i]
                if (type.value == value) {
                    return type
                }
            }
            return ZH
        }
    }
}

