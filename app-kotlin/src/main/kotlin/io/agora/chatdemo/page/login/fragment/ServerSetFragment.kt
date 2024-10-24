package io.agora.chatdemo.page.login.fragment

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import io.agora.chatdemo.DemoHelper
import io.agora.chatdemo.R
import io.agora.chatdemo.common.dialog.SimpleDialog
import io.agora.chatdemo.common.extensions.internal.addDefaultTextChangedListener
import io.agora.chatdemo.common.helper.DeveloperModeHelper
import io.agora.chatdemo.databinding.DemoFragmentServerSetBinding
import io.agora.uikit.base.EaseBaseFragment
import kotlin.system.exitProcess

class ServerSetFragment: EaseBaseFragment<DemoFragmentServerSetBinding>() {

    private val changeArray = BooleanArray(4)
    private var isEnableCustomServer = false
    override fun getViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DemoFragmentServerSetBinding? {
        return DemoFragmentServerSetBinding.inflate(inflater)
    }

    override fun initView(savedInstanceState: Bundle?) {
        super.initView(savedInstanceState)
        binding?.toolbarServer?.let {
            it.inflateMenu(R.menu.demo_server_set_menu)
            enableSaveMenu(false)
        }
        binding?.etAppkey?.inputType = InputType.TYPE_CLASS_TEXT
    }

    override fun initListener() {
        super.initListener()
        binding?.toolbarServer?.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        binding?.toolbarServer?.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.action_server_set_save -> {
                    saveSettings()
                    true
                }
                else -> false
            }
        }
        binding?.etAppkey?.addDefaultTextChangedListener {
            it?.let { s ->
                changeArray[0] = s.isNotEmpty()
                changeSaveMenu(s)
            }
        }
        binding?.etServerAddress?.addDefaultTextChangedListener {
            it?.let { s ->
                changeArray[1] = s.isNotEmpty()
                changeSaveMenu(s)
            }
        }
        binding?.etServerPort?.addDefaultTextChangedListener {
            it?.let { s ->
                changeArray[2] = s.isNotEmpty()
                changeSaveMenu(s)
            }
        }
        binding?.etServerRest?.addDefaultTextChangedListener {
            it?.let { s ->
                changeArray[3] = s.isNotEmpty()
                changeSaveMenu(s)
            }
        }
        binding?.switchSpecifyServer?.setOnCheckedChangeListener { _, isChecked ->
            isEnableCustomServer = isChecked
            makeCustomServerItemEnable(isChecked)
        }
    }

    private fun saveSettings() {
        if (checkChange()) {
            DemoHelper.getInstance().getDataModel().enableCustomSet(true)
            DemoHelper.getInstance().getDataModel().enableCustomServer(isEnableCustomServer)
            binding?.etAppkey?.text?.let {
                if (it.isNotEmpty()) {
                    val appkey = it.toString().trim()
                    if (appkey.contains("#")) {
                        DemoHelper.getInstance().getDataModel().setCustomAppKey(it.toString().trim())
                    } else {
                        checkAppkeyDialog()
                        return
                    }

                }
            }
            binding?.etServerAddress?.text?.let {
                if (it.isNotEmpty()) {
                    DemoHelper.getInstance().getDataModel().setIMServer(it.toString().trim())
                }
            }
            binding?.etServerPort?.text?.let {
                if (it.isNotEmpty()) {
                    DemoHelper.getInstance().getDataModel().setIMServerPort(it.toString().trim().toInt())
                }
            }
            binding?.etServerRest?.text?.let {
                if (it.isNotEmpty()) {
                    DemoHelper.getInstance().getDataModel().setRestServer(it.toString().trim())
                }
            }
            if (isEnableCustomServer && checkServerSettingChange()) {
                showAlertDialog()
            } else {
                mContext.onBackPressed()
            }
        } else {
            DemoHelper.getInstance().getDataModel().enableCustomSet(false)
            mContext.onBackPressed()
        }
    }

    private fun showAlertDialog() {
        SimpleDialog.Builder(mContext)
            .setTitle(getString(R.string.server_set_dialog_title))
            .setSubtitle(getString(R.string.server_set_dialog_content))
            .setPositiveButton(getString(R.string.server_set_dialog_confirm_button_text)) {
                exitProcess(1)
            }
            .build()
            .show()
    }

    private fun checkAppkeyDialog() {
        SimpleDialog.Builder(mContext)
            .setTitle(getString(R.string.server_set_illegal_appkey))
            .setPositiveButton {
                // do nothing
            }
            .dismissNegativeButton()
            .build()
            .show()
    }

    override fun initData() {
        super.initData()
        DeveloperModeHelper.isCustomSetEnable().let {
            if (it) {
                DemoHelper.getInstance().getDataModel().getCustomAppKey()?.let { appKey ->
                    binding?.etAppkey?.setText(appKey)
                }
                DemoHelper.getInstance().getDataModel().isCustomServerEnable().let { enable ->
                    isEnableCustomServer = enable
                    binding?.switchSpecifyServer?.isChecked = enable
                    makeCustomServerItemEnable(enable)
                }
                DemoHelper.getInstance().getDataModel().getIMServer()?.let { server ->
                    if (server.isEmpty().not()) {
                        binding?.etServerAddress?.setText(server)
                    }

                }
                DemoHelper.getInstance().getDataModel().getIMServerPort().let { port ->
                    if (port != 0) {
                        binding?.etServerPort?.setText(port.toString())
                    }
                }
                DemoHelper.getInstance().getDataModel().getRestServer()?.let { rest ->
                    if (rest.isEmpty().not()) {
                        binding?.etServerRest?.setText(rest)
                    }
                }
            }
        }
        makeCustomServerItemEnable(binding?.switchSpecifyServer?.isChecked ?: false)
    }

    private fun makeCustomServerItemEnable(enable: Boolean) {
        binding?.etServerAddress?.isEnabled = enable
        binding?.etServerPort?.isEnabled = enable
        binding?.etServerRest?.isEnabled = enable
    }

    private fun changeSaveMenu(s: Editable) {
        if (s.isNotEmpty()) {
            enableSaveMenu()
        } else {
            enableSaveMenu(checkChange())
        }
    }

    private fun checkChange(): Boolean {
        changeArray.forEach {
            if (it) {
                return true
            }
        }
        return false
    }

    private fun checkServerSettingChange(): Boolean {
        changeArray.forEachIndexed { index, b ->
            if (index > 0 && b) {
                return true
            }
        }
        return false
    }

    private fun enableSaveMenu(enable: Boolean = true) {
        binding?.toolbarServer?.let {
            if (enable) {
                it.setMenuTitleColor(ContextCompat.getColor(mContext, R.color.color_primary))
            } else {
                it.setMenuTitleColor(ContextCompat.getColor(mContext, R.color.demo_on_background_high))
            }
        }

    }
}