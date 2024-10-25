package io.agora.chatdemo.utils

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_PICTURES
import android.provider.MediaStore
import io.agora.uikit.common.ChatLog
import io.agora.uikit.common.ChatPathUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date


object CameraAndCropFileUtils {

    //用于获取与应用程序相关联的特定类型的文件目录，如果你不想将文件放在特定的子目录，可以传递 null 将文件放在根目录。
    // 在存储在这些目录的文件，不需要申请WRITE_EXTERNAL_STORAGE权限。
    private val rootSavePath = ChatPathUtils.getInstance().imagePath

    // 用于获取用户可以看到的公共目录 比如 "Music", "Pictures"等 在这个路径下的文件即使在应用卸载后依然存在。
    // 需要申请WRITE_EXTERNAL_STORAGE权限。
    private val rootPublicFolderPath = Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).absolutePath

    var uri: Uri? = null

    @SuppressLint("SimpleDateFormat")
    fun createImageFile(context: Context, isCrop: Boolean): File? {
        return try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            var fileName = ""
            fileName = if (isCrop) {
                "IMG_" + timeStamp + "_CROP.jpg"
            } else {
                "IMG_$timeStamp.jpg"
            }
            val imgFile: File
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                //如果 version >= 11 需要将图片文件创建到公共目录 比如 "Music", "Pictures"等
                ChatLog.e("CameraAndCropFileUtils","createImageFile version >= 11 需要将图片文件创建到公共目录 ${Environment.getExternalStoragePublicDirectory(DIRECTORY_PICTURES).toString() + File.separator + fileName}")
                imgFile = File(rootPublicFolderPath+ File.separator + fileName)
                // 通过 MediaStore API 插入file 为了拿到系统裁剪要保存到的uri（因为App没有权限不能访问公共存储空间，需要通过 MediaStore API来操作）
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, imgFile.absolutePath)
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ChatLog.e("CameraAndCropFileUtils","createImageFile version >= 11 创建成功 Uri: $uri")
            } else {
                imgFile = File(rootSavePath ,File.separator + fileName)
                ChatLog.e("CameraAndCropFileUtils","createImageFile version < 11 ${imgFile.absolutePath}")
            }
            imgFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getCropFile(context: Context, uri: Uri?): File? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri!!, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            val path = cursor.getString(columnIndex)
            cursor.close()
            return File(path)
        }
        return null
    }

    fun getAbsolutePathFromUri(context: Context, uri: Uri): String? {
        var absolutePath: String? = null
        val projection = arrayOf(MediaStore.MediaColumns.DATA)
        val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                absolutePath = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        return absolutePath
    }

}