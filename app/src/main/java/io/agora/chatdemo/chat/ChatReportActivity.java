package io.agora.chatdemo.chat;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.agora.CallBack;
import io.agora.chat.ChatClient;
import io.agora.chat.ChatMessage;
import io.agora.chat.FileMessageBody;
import io.agora.chat.ImageMessageBody;
import io.agora.chat.NormalFileMessageBody;
import io.agora.chat.TextMessageBody;
import io.agora.chat.VideoMessageBody;
import io.agora.chat.VoiceMessageBody;
import io.agora.chat.uikit.EaseUIKit;
import io.agora.chat.uikit.activities.EaseShowNormalFileActivity;
import io.agora.chat.uikit.constants.EaseConstant;
import io.agora.chat.uikit.player.EasyVideoPlayer;
import io.agora.chat.uikit.utils.EaseCompat;
import io.agora.chat.uikit.utils.EaseDateUtils;
import io.agora.chat.uikit.utils.EaseFileUtils;
import io.agora.chat.uikit.utils.EaseImageUtils;
import io.agora.chat.uikit.utils.EaseSmileUtils;
import io.agora.chat.uikit.utils.EaseUserUtils;
import io.agora.chat.uikit.widget.EaseImageView;
import io.agora.chat.uikit.widget.EaseTitleBar;
import io.agora.chatdemo.R;
import io.agora.chatdemo.base.BaseActivity;
import io.agora.chatdemo.general.dialog.SimpleDialog;
import io.agora.chatdemo.general.widget.EaseProgressDialog;
import io.agora.exceptions.ChatException;

public class ChatReportActivity extends BaseActivity implements
        MediaPlayer.OnBufferingUpdateListener{
    private String reportMsgId;
    private EaseTitleBar titleBar;
    private EaseImageView easeImageView;
    private TextView userName;
    private TextView mTime;
    private TextView mTextContent;
    private TextView report_type;
    private TextView content_count;
    private TextView mDuration;
    private TextView mFileName;
    private RelativeLayout report_type_layout;
    private LinearLayout report_voice_layout;
    private LinearLayout report_file_layout;
    private RelativeLayout report_video_layout;
    private EditText easeInputEditText;
    private ImageView mImgContent;
    private ImageView mPlay;
    private EaseProgressDialog progressDialog;
    private SeekBar report_SeekBar;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;
    private EasyVideoPlayer videoPlayer;
    private ArrayList<String> labels = new ArrayList<>();
    private ChatMessage message;
    private int mFileDuration;
    private ScrollView scrollView;
    private Handler mHandler;
    private static final int UPDATE_INTERVAL = 100;

    private final Runnable mUpdateCounters =
            new Runnable() {
                @Override
                public void run() {
                    int pos =  mediaPlayer.getCurrentPosition();
                    final int dur  =  mediaPlayer.getDuration();
                    report_SeekBar.setProgress(pos);
                    report_SeekBar.setMax(dur);
                    if (pos != 0){
                        mDuration.setText(getDurationString(pos,false));
                    }
                    if (mHandler != null) mHandler.postDelayed(this, UPDATE_INTERVAL);
                }
            };

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
        intent.putExtra(EaseConstant.REPORT_MESSAGE_ID, report_msg_id);
        context.startActivity(intent);
    }

    public void initArguments() {
        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            reportMsgId = bundle.getString(EaseConstant.REPORT_MESSAGE_ID);
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
        report_voice_layout = findViewById(R.id.voice_content_layout);
        report_type = findViewById(R.id.report_type);
        content_count = findViewById(R.id.content_count);
        mDuration = findViewById(R.id.report_duration);
        mFileName = findViewById(R.id.report_file_name);
        mPlay = findViewById(R.id.report_player);
        report_type_layout = findViewById(R.id.report_type_layout);
        report_file_layout = findViewById(R.id.report_file_layout);
        report_video_layout = findViewById(R.id.report_video_layout);
        easeInputEditText = findViewById(R.id.edit_report_reason);
        report_SeekBar = findViewById(R.id.report_progress);
        videoPlayer = findViewById(R.id.evp_player);
        scrollView = findViewById(R.id.scroll);
        easeImageView.setShapeType(1);

        mediaPlayer = new MediaPlayer();


        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);

        setTint(report_SeekBar, Color.WHITE);

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
                Spannable span = EaseSmileUtils.getSmiledText(ChatReportActivity.this,((TextMessageBody)message.getBody()).getMessage());
                mTextContent.setText(span, TextView.BufferType.SPANNABLE);
            }else if (message.getBody() instanceof ImageMessageBody){
                mImgContent.setVisibility(View.VISIBLE);
                EaseImageUtils.showImage(this, mImgContent, message);
            }else if (message.getBody() instanceof VoiceMessageBody){
                report_voice_layout.setVisibility(View.VISIBLE);
                mFileDuration = ((VoiceMessageBody) message.getBody()).getLength();
                mDuration.setText(getTimer(mFileDuration));
                report_SeekBar.setProgress(0);
                report_SeekBar.setSecondaryProgress(0);
            }else if (message.getBody() instanceof VideoMessageBody){
                report_video_layout.setVisibility(View.VISIBLE);
                if(EaseFileUtils.isFileExistByUri(this, ((VideoMessageBody) message.getBody()).getLocalUri())) {
                    videoPlayer.setSource(((VideoMessageBody) message.getBody()).getLocalUri());
                }
            }else if (message.getBody() instanceof FileMessageBody){
                report_file_layout.setVisibility(View.VISIBLE);
                mFileName.setText(((FileMessageBody) message.getBody()).getFileName());
            }
        }
    }

    public void initListener(){
        report_type_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2022/6/1 show Choose type
                new ReportDialogFragment.Builder((BaseActivity) mContext)
                        .setData(labels)
                        .setCancelColorRes(R.color.black)
                        .setWindowAnimations(R.style.dialog_from_bottom_anim)
                        .setOnItemClickListener(new ReportDialogFragment.OnDialogItemClickListener() {
                            @Override
                            public void OnItemClick(View view, int position) {
                                report_type.setText(labels.get(position));
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
                                progressDialog = new EaseProgressDialog.Builder(ChatReportActivity.this)
                                        .setLoadingMessage("Loading...")
                                        .show();

                                ChatClient.getInstance().chatManager().asyncReportMessage(reportMsgId, report_type.getText().toString(), easeInputEditText.getText().toString(), new CallBack() {
                                    @Override
                                    public void onSuccess() {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!isFinishing() && !isDestroyed()) {
                                                    if (progressDialog != null) {
                                                        progressDialog.dismiss();
                                                    }
                                                    ChatReportCompleteActivity.actionStart(ChatReportActivity.this);
                                                    finish();
                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onError(int code, String error) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (progressDialog != null) {
                                                    progressDialog.dismiss();
                                                }
                                            }
                                        });
                                    }
                                });
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
            }
        });

        easeInputEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                easeInputEditText.requestFocus();
            }
        });

        mPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    // Stop the voice play first, no matter the playing voice item is this or others.
                    mediaPlayer.stop();

                }
                String localPath = ((VoiceMessageBody) message.getBody()).getLocalUrl();
                File file = new File(localPath);
                if (file.exists() && file.isFile()) {
                    play(message);
                }
            }
        });

        report_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(seekBar.getProgress());
            }
        });

        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                mp.seekTo(0);
            }
        });

        report_file_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (message.getBody() instanceof NormalFileMessageBody){
                    Uri filePath = ((NormalFileMessageBody) message.getBody()).getLocalUri();
                    EaseFileUtils.takePersistableUriPermission(ChatReportActivity.this, filePath);
                    if(EaseFileUtils.isFileExistByUri(ChatReportActivity.this, filePath)){
                        EaseCompat.openFile(ChatReportActivity.this, filePath);
                    } else {
                        // download the file
                        startActivity(new Intent(ChatReportActivity.this, EaseShowNormalFileActivity.class).putExtra("msg", message));
                    }
                    if (message.direct() == ChatMessage.Direct.RECEIVE && !message.isAcked() && message.getChatType() == ChatMessage.ChatType.Chat) {
                        try {
                            ChatClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
                        } catch (ChatException e) {
                            e.printStackTrace();
                        }
                    }
                }
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

    }


    private String getTimer(long durationMs){
            long seconds = durationMs % 60;
            long minutes = (durationMs/60)%60;
            return new Formatter().format("%02d:%02d",minutes,seconds).toString();
    }

    private static String getDurationString(long durationMs, boolean negativePrefix) {
        return String.format(
                Locale.getDefault(),
                "%s%02d:%02d",
                negativePrefix ? "-" : "",
                TimeUnit.MILLISECONDS.toMinutes(durationMs),
                TimeUnit.MILLISECONDS.toSeconds(durationMs)
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMs)));
    }

    private void play(ChatMessage msg) {
        setSpeaker();
        if ((msg.getBody() instanceof VoiceMessageBody)){
            VoiceMessageBody voiceBody = (VoiceMessageBody) msg.getBody();
            try {
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.stop();
                }else {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(this, voiceBody.getLocalUri());
                    mediaPlayer.prepare();
                    report_SeekBar.setMax(mediaPlayer.getDuration());
                    mediaPlayer.start();
                }

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.seekTo(0);
                        report_SeekBar.setProgress(0);
                        mDuration.setText(getTimer(mFileDuration));
                        if (mHandler != null) mHandler.removeCallbacks(mUpdateCounters);
                    }
                });

                if (mHandler == null) mHandler = new Handler();
                mHandler.post(mUpdateCounters);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if(videoPlayer != null) {
            videoPlayer.release();
            videoPlayer = null;
        }
        if (mHandler != null) {
            mHandler.removeCallbacks(mUpdateCounters);
            mHandler = null;
        }
    }

    private void setSpeaker() {
        boolean speakerOn = EaseUIKit.getInstance().getSettingsProvider().isSpeakerOpened();
        if (speakerOn) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(true);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        } else {
            audioManager.setSpeakerphoneOn(false);// Turn off speaker
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
        }
    }

    private static void setTint(@NonNull SeekBar seekBar, @ColorInt int color) {
        ColorStateList s1 = ColorStateList.valueOf(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            seekBar.setThumbTintList(s1);
            seekBar.setProgressTintList(s1);
            seekBar.setSecondaryProgressTintList(s1);
        } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
            Drawable progressDrawable = DrawableCompat.wrap(seekBar.getProgressDrawable());
            seekBar.setProgressDrawable(progressDrawable);
            DrawableCompat.setTintList(progressDrawable, s1);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                Drawable thumbDrawable = DrawableCompat.wrap(seekBar.getThumb());
                DrawableCompat.setTintList(thumbDrawable, s1);
                seekBar.setThumb(thumbDrawable);
            }
        } else {
            PorterDuff.Mode mode = PorterDuff.Mode.SRC_IN;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
                mode = PorterDuff.Mode.MULTIPLY;
            }
            if (seekBar.getIndeterminateDrawable() != null)
                seekBar.getIndeterminateDrawable().setColorFilter(color, mode);
            if (seekBar.getProgressDrawable() != null)
                seekBar.getProgressDrawable().setColorFilter(color, mode);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mHandler != null) {
            mHandler.removeCallbacks(mUpdateCounters);
            mHandler = null;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mHandler != null) {
            mHandler.removeCallbacks(mUpdateCounters);
            mHandler = null;
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        if (report_SeekBar != null) {
            if (percent == 100) report_SeekBar.setSecondaryProgress(0);
            else report_SeekBar.setSecondaryProgress(report_SeekBar.getMax() * (percent / 100));
        }
    }

}
