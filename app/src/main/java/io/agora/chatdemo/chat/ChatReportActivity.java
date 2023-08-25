package io.agora.chatdemo.chat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;


import java.util.ArrayList;
import java.util.Date;

import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.FileMessageBody;
import io.agora.chat.ImageMessageBody;
import io.agora.chat.NormalFileMessageBody;
import io.agora.chat.TextMessageBody;
import io.agora.chat.VideoMessageBody;
import io.agora.chat.VoiceMessageBody;
import io.agora.chat.uikit.manager.EaseActivityProviderHelper;
import io.agora.chat.uikit.utils.EaseCompat;
import io.agora.chat.uikit.utils.EaseDateUtils;
import io.agora.chat.uikit.utils.EaseFileUtils;
import io.agora.chat.uikit.utils.EaseImageUtils;
import io.agora.chat.uikit.utils.EaseSmileUtils;
import io.agora.chat.uikit.utils.EaseUserUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.DemoHelper;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.chat.viewmodel.ChatViewModel;
import io.agora.chatdemo.general.callbacks.OnResourceParseCallback;
import io.agora.chatdemo.general.constant.DemoConstant;
import io.agora.chatdemo.general.dialog.SimpleDialog;

public class ChatReportActivity extends BaseActivity {
    private String reportMsgId;
    private EaseTitleBar titleBar;
    private EaseImageView easeImageView;
    private ImageView mFileIcon;
    private TextView userName;
    private TextView mTime;
    private TextView mTextContent;
    private TextView report_type;
    private TextView content_count;
    private TextView mFileName;
    private RelativeLayout report_type_layout;
    private LinearLayout report_file_layout;
    private EditText easeInputEditText;
    private ImageView mImgContent;
    private ArrayList<String> labels = new ArrayList<>();
    private ChatMessage message;
    private ScrollView scrollView;
    private ChatViewModel viewModel;

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_chat_report);
        initView();
        initArguments();
        initListener();
    }

    public static void actionStart(Context context, String report_msg_id) {
        Intent intent = new Intent(context, ChatReportActivity.class);
        intent.putExtra(DemoConstant.REPORT_MESSAGE_ID, report_msg_id);
        context.startActivity(intent);
    }

    public void initArguments() {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            reportMsgId = bundle.getString(DemoConstant.REPORT_MESSAGE_ID);
            message = ChatClient.getInstance().chatManager().getMessage(reportMsgId);
        }
        initData();
    }

    public void initView(){
        titleBar = findViewById(R.id.title_bar);
        easeImageView = findViewById(R.id.user_avatar);
        userName = findViewById(R.id.user_name);
        mTime = findViewById(R.id.time);
        mTextContent = findViewById(R.id.text_content);
        mImgContent = findViewById(R.id.img_content);
        report_type = findViewById(R.id.report_type);
        content_count = findViewById(R.id.content_count);
        mFileName = findViewById(R.id.report_file_name);
        report_type_layout = findViewById(R.id.report_type_layout);
        report_file_layout = findViewById(R.id.report_file_layout);
        easeInputEditText = findViewById(R.id.edit_report_reason);
        mFileIcon = findViewById(R.id.file_icon);
        scrollView = findViewById(R.id.scroll);
        easeImageView.setShapeType(1);
    }

    public void initData(){
        labels.add(getString(R.string.report_label_adult));
        labels.add(getString(R.string.report_label_racy));
        labels.add(getString(R.string.report_label_other));
        if (null != message){
            mTime.setText(EaseDateUtils.getTimestampString(this, new Date(message.getMsgTime())));
            userName.setText(EaseUserUtils.getUserInfo(message.getFrom()).getNickname());
            EaseUserUtils.setUserAvatar(this,message.getFrom(),easeImageView);
            if (message.getBody() instanceof TextMessageBody){
                mTextContent.setVisibility(View.VISIBLE);
                report_file_layout.setVisibility(View.GONE);
                Spannable span = EaseSmileUtils.getSmiledText(ChatReportActivity.this,((TextMessageBody)message.getBody()).getMessage());
                mTextContent.setText(span, TextView.BufferType.SPANNABLE);
            }else if (message.getBody() instanceof ImageMessageBody){
                mImgContent.setVisibility(View.VISIBLE);
                report_file_layout.setVisibility(View.GONE);
                EaseImageUtils.showImage(this, mImgContent, message);
            }else if (message.getBody() instanceof VoiceMessageBody){
                mFileIcon.setImageResource(R.drawable.file_type_voice);
                mFileName.setText(((VoiceMessageBody) message.getBody()).getFileName());
            }else if (message.getBody() instanceof VideoMessageBody){
                mFileIcon.setImageResource(R.drawable.file_type_video);
                mFileName.setText(((VideoMessageBody) message.getBody()).getFileName());
            }else if (message.getBody() instanceof FileMessageBody){
                mFileIcon.setImageDrawable(DemoHelper.getFileDrawable(((FileMessageBody) message.getBody()).getFileName()));
                mFileName.setText(((FileMessageBody) message.getBody()).getFileName());
            }
        }
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.getChatManagerObservable().observe(this,response->{
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(@Nullable Boolean data) {
                    ChatReportCompleteActivity.actionStart(ChatReportActivity.this);
                    finish();
                }

                @Override
                public void onError(int code, String message) {
                    super.onError(code, message);
                    dismissLoading();
                    showToast(message);
                }

                @Override
                public void onLoading(@Nullable Boolean data) {
                    super.onLoading(data);
                    showLoading("Loading...");
                }

                @Override
                public void onHideLoading() {
                    super.onHideLoading();
                    dismissLoading();
                }
            });
        });
    }

    public void initListener(){
        report_type_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ReportDialogFragment.Builder(mContext)
                        .setData(labels)
                        .setCancelColorRes(R.color.black)
                        .setWindowAnimations(R.style.dialog_from_bottom_anim)
                        .setOnItemClickListener(new ReportDialogFragment.OnDialogItemClickListener() {
                            @Override
                            public void OnItemClick(View view, int position) {
                                report_type.setText(labels.get(position));
                                checkDone();
                            }
                        }).show();
            }
        });


        titleBar.setOnBackPressListener( new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                mContext.onBackPressed();
            }
        });

        titleBar.setOnRightClickListener(new EaseTitleBar.OnRightClickListener() {
            @Override
            public void onRightClick(View view) {
                if (TextUtils.equals(report_type.getText(),"Please Choose")){
                    new SimpleDialog.Builder(ChatReportActivity.this)
                            .setContent(R.string.check_report_type)
                            .hideConfirmButton(false)
                            .showCancelButton(false)
                            .show();
                    return;
                }
                new SimpleDialog.Builder(ChatReportActivity.this)
                        .setContent(R.string.is_confirm_report)
                        .showCancelButton(true)
                        .hideConfirmButton(false)
                        .setOnConfirmClickListener(R.string.dialog_btn_to_confirm,new SimpleDialog.OnConfirmClickListener() {
                            @Override
                            public void onConfirmClick(View view) {
                                viewModel.reportMessage(reportMsgId, report_type.getText().toString(), easeInputEditText.getText().toString());
                            }
                        }).show();
            }
        });

        easeInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = easeInputEditText.getText().toString();
                content_count.setText(String.valueOf(content.length()));
                checkDone();
            }
        });

        easeInputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                easeInputEditText.requestFocus();
            }
        });

        report_file_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri filePath = null;
                if (message.getBody() instanceof NormalFileMessageBody){
                    filePath = ((NormalFileMessageBody) message.getBody()).getLocalUri();
                }else if (message.getBody() instanceof VoiceMessageBody){
                    filePath = ((VoiceMessageBody) message.getBody()).getLocalUri();
                }else if (message.getBody() instanceof VideoMessageBody){
                    filePath = ((VideoMessageBody) message.getBody()).getLocalUri();
                }
                if (null != filePath) openFile(filePath);
            }
        });

        scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(View.FOCUS_DOWN);
                    }
                });
            }
        });

        mImgContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.getBody() instanceof ImageMessageBody){
                    Uri imgUri =((ImageMessageBody) message.getBody()).getLocalUri();
                    EaseFileUtils.takePersistableUriPermission(ChatReportActivity.this, imgUri);
                    if(EaseFileUtils.isFileExistByUri(mContext, imgUri)) {
                        EaseActivityProviderHelper.startToLocalImageActivity(mContext, imgUri);
                    } else{
                        // The local full size pic does not exist yet.
                        // ShowBigImage needs to download it from the server
                        // first
                        EaseActivityProviderHelper.startToLocalImageActivity(mContext, message.getMsgId(), ((ImageMessageBody) message.getBody()).getFileName());
                    }
                }
            }
        });

    }

    public void openFile(Uri filePath){
        EaseFileUtils.takePersistableUriPermission(ChatReportActivity.this, filePath);
        if(EaseFileUtils.isFileExistByUri(ChatReportActivity.this, filePath)){
            EaseCompat.openFile(ChatReportActivity.this, filePath);
        }else {
            EaseActivityProviderHelper.startToDownloadFileActivity(mContext, message);
        }
    }

    private void checkDone(){
        if (TextUtils.equals(report_type.getText(),"Please Choose") || TextUtils.isEmpty(easeInputEditText.getText())){
            titleBar.setRightTitleColor(R.color.color_light_gray_999999);
            titleBar.getRightText().setEnabled(false);
        }else {
            titleBar.setRightTitleColor(R.color.color_main_blue);
            titleBar.getRightText().setEnabled(true);
        }
    }

}
