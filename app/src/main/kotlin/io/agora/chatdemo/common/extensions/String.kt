package io.agora.chatdemo.common.extensions

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * MD5 encryption.
 */
fun String.MD5(): String {
    if (this.isEmpty()) {
        return ""
    }
    var hexStr = ""
    try {
        val hash = MessageDigest.getInstance("MD5").digest(toByteArray(charset("utf-8")))
        val hex = StringBuilder(hash.size * 2)
        for (b in hash) {
            if (b.toInt() and 0xFF < 0x10) {
                hex.append("0")
            }
            hex.append(Integer.toHexString(b.toInt() and 0xFF))
        }
        hexStr = hex.toString()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: UnsupportedEncodingException) {
        e.printStackTrace()
    }

    return hexStr
}