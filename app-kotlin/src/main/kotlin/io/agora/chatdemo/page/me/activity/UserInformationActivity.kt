package io.agora.chatdemo.page.me.activity

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.load
import com.yalantis.ucrop.UCrop
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.R
import io.agora.chatdemo.common.DemoConstant
import io.agora.chatdemo.common.helper.DeveloperModeHelper
import io.agora.chatdemo.databinding.DemoActivityMeInformationBinding
import io.agora.chatdemo.page.me.CameraAndCroppingController
import io.agora.chatdemo.utils.CameraAndCropFileUtils
import io.agora.chatdemo.viewmodel.ProfileInfoViewModel
import io.agora.uikit.EaseIM
import io.agora.uikit.base.EaseBaseActivity
import io.agora.uikit.common.ChatImageUtils
import io.agora.uikit.common.ChatLog
import io.agora.uikit.common.bus.EaseFlowBus
import io.agora.uikit.common.dialog.SimpleListSheetDialog
import io.agora.uikit.common.extensions.catchChatException
import io.agora.uikit.common.extensions.dpToPx
import io.agora.uikit.common.extensions.mainScope
import io.agora.uikit.common.extensions.showToast
import io.agora.uikit.common.permission.PermissionCompat
import io.agora.uikit.common.utils.EaseCompat
import io.agora.uikit.common.utils.EaseFileUtils
import io.agora.uikit.configs.setAvatarStyle
import io.agora.uikit.interfaces.SimpleListSheetItemClickListener
import io.agora.uikit.model.EaseEvent
import io.agora.uikit.model.EaseMenuItem
import io.agora.uikit.model.EaseProfile
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File


class UserInformationActivity: EaseBaseActivity<DemoActivityMeInformationBinding>(),
    View.OnClickListener {

    private val cameraAndCroppingController: CameraAndCroppingController by lazy {
        CameraAndCroppingController(mContext)
    }
    private var selfProfile: EaseProfile? = null
    private var showSelectDialog: SimpleListSheetDialog? = null
    private var imageUri:Uri?= null
    private lateinit var model: ProfileInfoViewModel

    override fun getViewBinding(inflater: LayoutInflater): DemoActivityMeInformationBinding? {
        return DemoActivityMeInformationBinding.inflate(inflater)
    }
    companion object {
        private const val REQUEST_CODE_STORAGE_PICTURE = 111
        private const val REQUEST_CODE_CAMERA = 112
        private const val REQUEST_CODE_LOCAL_EDIT = 113
        private const val RESULT_CODE_CAMERA = 114
        private const val RESULT_CODE_LOCAL = 115
        private const val RESULT_CODE_UPDATE_NAME = 116
        private const val RESULT_REFRESH = "isRefresh"
    }

    private val requestCameraPermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            onRequestResult(
                result,
                REQUEST_CODE_CAMERA
            )
        }

    private val requestImagePermission: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            onRequestResult(
                result,
                REQUEST_CODE_STORAGE_PICTURE
            )
        }

    private val launcherToCamera: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onActivityResult(result, RESULT_CODE_CAMERA)
    }
    private val launcherToAlbum: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onActivityResult(result, RESULT_CODE_LOCAL) }

    private val launcherToUpdateName: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> onActivityResult(result, RESULT_CODE_UPDATE_NAME) }


    private val launcherToMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        if (uri != null) {
            ChatLog.d("launcherToMedia", "Selected URI: $uri")
            cameraAndCroppingController.gotoCrop(uri)
        } else {
            ChatLog.d("launcherToMedia", "No media selected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initListener()
        initData()
    }

    private fun initView(){
        EaseIM.getConfig()?.avatarConfig?.setAvatarStyle(binding.ivAvatar)
        binding.run {
            ivAvatar.setRadius(8.dpToPx(this@UserInformationActivity))
            ivAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
        }
        updateLocalData()
    }

    private fun initListener(){
        binding.run {
            titleBar.setNavigationOnClickListener{ finish() }
            avatarLayout.setOnClickListener(this@UserInformationActivity)
            nickNameLayout.setOnClickListener(this@UserInformationActivity)
        }
    }

    private fun initData(){
        model = ViewModelProvider(this)[ProfileInfoViewModel::class.java]
    }

    private fun updateLocalData(){
        binding.run {
            selfProfile = EaseIM.getCurrentUser()
            selfProfile?.let { profile->
                val ph = AppCompatResources.getDrawable(this@UserInformationActivity, io.agora.uikit.R.drawable.ease_default_avatar)
                val ep = AppCompatResources.getDrawable(this@UserInformationActivity, io.agora.uikit.R.drawable.ease_default_avatar)
                ivAvatar.load(profile.avatar ?: ph) {
                    placeholder(ph)
                    error(ep)
                }
                tvNickName.text = profile.getNotEmptyName()
            }
        }
    }

    private fun showSelectDialog(){
        val context = this@UserInformationActivity
        showSelectDialog = SimpleListSheetDialog(
            context = context,
            itemList = mutableListOf(
                EaseMenuItem(
                    menuId = R.id.about_information_camera,
                    title = getString(R.string.main_about_me_information_camera),
                    titleColor = ContextCompat.getColor(context, R.color.color_primary)
                ),
                EaseMenuItem(
                    menuId = R.id.about_information_picture,
                    title = getString(R.string.main_about_me_information_picture),
                    titleColor = ContextCompat.getColor(context, R.color.color_primary)
                )
            ),
            itemListener = object : SimpleListSheetItemClickListener {
                override fun onItemClickListener(position: Int, menu: EaseMenuItem) {
                    simpleMenuItemClickListener(menu)
                    showSelectDialog?.dismiss()
                }
            })
        supportFragmentManager.let { showSelectDialog?.show(it,"image_select_dialog") }
    }

    fun simpleMenuItemClickListener(menu: EaseMenuItem){
        when(menu.menuId){
            R.id.about_information_camera -> {
                if (PermissionCompat.checkPermission(
                        mContext,
                        requestCameraPermission,
                        Manifest.permission.CAMERA,
                    )
                ) {
                    cameraAndCroppingController.selectPicFromCamera(launcherToCamera)
                }
            }
            R.id.about_information_picture -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2){
                    val mimeType = "image/*"
                    launcherToMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.SingleMimeType(mimeType)))
                }else{
                    if (PermissionCompat.checkMediaPermission(
                            mContext,
                            requestImagePermission,
                            Manifest.permission.READ_MEDIA_IMAGES
                        )
                    ) {
                        selectPicFromLocal(launcherToAlbum)
                    }
                }
            }
            else -> {}
        }
    }


    private fun onRequestResult(result: Map<String, Boolean>?, requestCode: Int) {
        if (!result.isNullOrEmpty()) {
            for ((key, value) in result) {
                ChatLog.e("UserInformationActivity", "onRequestResult: $key  $value")
            }
            if (PermissionCompat.getMediaAccess(mContext) !== PermissionCompat.StorageAccess.Denied) {
                if (requestCode == REQUEST_CODE_STORAGE_PICTURE) {
                    selectPicFromLocal(launcherToAlbum)
                }else if (requestCode == REQUEST_CODE_CAMERA){
                    cameraAndCroppingController.selectPicFromCamera(launcherToCamera)
                }else if (requestCode == REQUEST_CODE_LOCAL_EDIT){
                    imageUri?.let { cameraAndCroppingController.gotoCrop(it) }
                }
            }
        }
    }

    /**
     * It's the result from ActivityResultLauncher.
     * @param result
     * @param requestCode
     */
    private fun onActivityResult(result: ActivityResult, requestCode: Int) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            when (requestCode) {
                RESULT_CODE_CAMERA -> { // capture new image
                    onActivityResultForCamera(data)
                }
                RESULT_CODE_LOCAL -> {
                    onActivityResultForLocalPhotos(data)
                }
                RESULT_CODE_UPDATE_NAME -> {
                    data?.let {
                        if (it.hasExtra(RESULT_REFRESH) && it.getBooleanExtra(RESULT_REFRESH,false)){
                            it.getStringExtra("nickname")?.let { name ->
                                updateUsername(name)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        data?.let {
            if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
                val resultUri = UCrop.getOutput(data)
                resultUri?.let { it1 ->
                    val result = ChatImageUtils.checkDegreeAndRestoreImage(mContext,it1)
                    imageUri = result
                    val path = CameraAndCropFileUtils.getAbsolutePathFromUri(mContext, result)
                    ChatLog.e("UserInformationActivity","onActivityResult corp path $path")
                    path?.let {
                        uploadFile(it)
                    }
                }
            } else if (resultCode == UCrop.RESULT_ERROR) {
                val cropError = UCrop.getError(data)
                ChatLog.e("UserInformationActivity","onActivityResult corp error ${cropError?.message}")
            } else {

            }
        }
    }

    private fun onActivityResultForCamera(data: Intent?) {
        val imageUri = cameraAndCroppingController.resultForCamera(data)
        val result = ChatImageUtils.checkDegreeAndRestoreImage(mContext,imageUri)
        this.imageUri = result
        imageUri?.let {
            cameraAndCroppingController.gotoCrop(it)
        }
    }

    private fun onActivityResultForLocalPhotos(data: Intent?) {
        if (data != null) {
            val selectedImage = data.data
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S_V2){
                selectedImage?.let { cameraAndCroppingController.gotoCrop(it) }
            }else{
                if (selectedImage != null) {
                    var filePath: String = EaseFileUtils.getFilePath(mContext, selectedImage)
                    if (!TextUtils.isEmpty(filePath) && File(filePath).exists()) {
                        imageUri = Uri.parse(filePath)
                    } else {
                        imageUri = selectedImage
                        selectedImage.path?.let {
                            filePath = it
                        }
                    }
                    imageUri?.let { cameraAndCroppingController.gotoCrop(it) }
                }
            }
        }
    }

    /**
     * select local image
     */
    private fun selectPicFromLocal(launcher: ActivityResultLauncher<Intent>?) {
        EaseCompat.openImageByLauncher(launcher, mContext)
    }

    private fun updateUserAvatar(){
        selfProfile = EaseIM.getCurrentUser()
        EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE + EaseEvent.TYPE.CONTACT)
            .post(lifecycleScope, EaseEvent(DemoConstant.EVENT_UPDATE_SELF, EaseEvent.TYPE.CONTACT))
    }

    private fun updateUsername(nickname: String){
        lifecycleScope.launch {
            model.updateUserNickName(nickname)
                .onStart {
                    showLoading(true)
                }
                .onCompletion { dismissLoading() }
                .catchChatException { e ->
                    ChatLog.e("TAG", "updateUserNickName fail error message = " + e.description)
                    mainScope().launch {
                        mContext.showToast("updateUsername error ${e.errorCode} ${e.description}")
                    }
                }
                .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(5000), null)
                .collect {

                    EaseIM.getCurrentUser()?.let {profile ->
                        profile.name = nickname
                        DemoHelper.getInstance().getDataModel().insertUser(profile)
                        EaseIM.updateCurrentUser(profile)
                    }
                    binding.tvNickName.text = nickname
                    EaseFlowBus.with<EaseEvent>(EaseEvent.EVENT.UPDATE + EaseEvent.TYPE.CONTACT)
                        .post(lifecycleScope, EaseEvent(DemoConstant.EVENT_UPDATE_SELF, EaseEvent.TYPE.CONTACT))
                }
        }
    }

    private fun uploadFile(filePath:String?){
        ChatLog.e("UserInformationActivity","uploadFile filePath $filePath")
        val scaledImage = ChatImageUtils.getScaledImageByUri(this,filePath)
        lifecycleScope.launch {
            model.uploadAvatar(scaledImage)
                .onStart {
                    showLoading(true)
                }
                .onCompletion { dismissLoading() }
                .catchChatException { e ->
                    ChatLog.e("UserInformationActivity", "uploadAvatar fail error message = " + e.description)
                    mainScope().launch {
                        mContext.showToast("uploadFile error ${e.errorCode} ${e.description}")
                    }
                }
                .stateIn(lifecycleScope, SharingStarted.WhileSubscribed(5000), null)
                .collect {
                    it?.let {
                        binding.ivAvatar.setImageURI(imageUri)
                        updateUserAvatar()
                    }
                }
        }
    }

    override fun onClick(v: View?) {
       when(v?.id){
           R.id.avatar_layout -> {
               if (!DeveloperModeHelper.isDeveloperMode()){
                   showSelectDialog()
               }else{
                   mainScope().launch {
                       mContext.showToast(mContext.getString(R.string.main_information_checked_model))
                   }
               }
           }
           R.id.nick_name_layout -> {
               launcherToUpdateName.launch(Intent(
                   this@UserInformationActivity,
                   EditUserNicknameActivity::class.java))
           }
           else -> {}
       }
    }

}