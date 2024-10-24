package io.agora.chatdemo.page.me

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import com.yalantis.ucrop.UCrop
import io.agora.chatdemo.BuildConfig
import io.agora.chatdemo.utils.CameraAndCropFileUtils
import io.agora.uikit.common.ChatImageUtils
import io.agora.uikit.common.ChatLog
import io.agora.uikit.common.extensions.isSdcardExist
import java.io.File

class CameraAndCroppingController(
    var context: Context
) {
    companion object {
        const val AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileProvider"
    }
    private var cameraFile: File? = null
    private var imageCropFile:File? = null

    private var resultImageUri:Uri? = null
    private var cropImageUri:Uri? = null

    fun selectPicFromCamera(launcher: ActivityResultLauncher<Intent>?){
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2){
            if (takePicture.resolveActivity(context.packageManager) != null) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.TITLE, "MyPic")
                values.put(
                    MediaStore.Images.Media.DESCRIPTION,
                    "Photo taken on " + System.currentTimeMillis()
                )
                resultImageUri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, resultImageUri)
            }
        }else{
            if (!isSdcardExist()) {
                return
            }
            cameraFile = CameraAndCropFileUtils.createImageFile( context, false)
            cameraFile?.let {
                takePicture.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                    // 如果是 11 以上系统 通过MediaStore获取 uri
                    ChatLog.e("CameraAndCroppingController","selectPicFromCamera version >= 11 putExtra: ${CameraAndCropFileUtils.uri}")
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT,CameraAndCropFileUtils.uri)
                }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                    // 如果是 7.0 且低于 11 系统 需要使用FileProvider
                    takePicture.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    val imgUri = FileProvider.getUriForFile(context, AUTHORITY, it)
                    ChatLog.e("CameraAndCroppingController","selectPicFromCamera 7 <= version < 11 putExtra: $imgUri")
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, imgUri)
                }else{
                    // 低于 7.0 系统
                    ChatLog.e("CameraAndCroppingController","selectPicFromCamera version < 7 putExtra: ${Uri.fromFile(it)}")
                    takePicture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(it))
                }
            }
        }
        launcher?.launch(takePicture)
    }

    fun resultForCamera(data: Intent?):Uri?{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2){
            return resultImageUri
        }else{
            cameraFile?.let {
                //判断文件是否存在
                if (it.exists()){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                        // 如果是 11 以上系统 通过MediaStore获取 uri
                        resultImageUri = CameraAndCropFileUtils.uri
                        ChatLog.e("CameraAndCroppingController","resultForCamera version >= 11 putExtra: $resultImageUri")
                    }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                        // 如果是 7.0 且低于 11 系统 需要使用FileProvider 创建一个content类型的Uri
                        resultImageUri = FileProvider.getUriForFile(context, AUTHORITY, it)
                        ChatLog.e("CameraAndCroppingController","resultForCamera 7 <= version < 11 putExtra: $resultImageUri")
                    }else{
                        resultImageUri = Uri.fromFile(it)
                        ChatLog.e("CameraAndCroppingController","resultForCamera version < 7 putExtra: $resultImageUri")
                    }
                }
            }
        }
        return resultImageUri
    }

    fun gotoCrop(sourceUri: Uri){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "MyCrop")
        values.put(
            MediaStore.Images.Media.DESCRIPTION,
            "Crop taken on " + System.currentTimeMillis()
        )
        cropImageUri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
        cropImageUri?.let {
            UCrop.of(sourceUri, it)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(500, 500)
                .start(context as Activity)
        }
    }

    fun resultForCropFile(data: Intent?):File?{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2){
            CameraAndCropFileUtils.getCropFile(context,cropImageUri)
        }else{
            if (imageCropFile != null && imageCropFile?.absolutePath != null){
                imageCropFile?.let {
                    return it
                }
            }
        }
        return null
    }

    fun getImageCropUri():Uri?{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2){
            return cropImageUri
        }else{
            imageCropFile?.let {
                val uri = Uri.parse(it.absolutePath)
                return ChatImageUtils.checkDegreeAndRestoreImage(context, uri)
            }
        }
       return null
    }

}