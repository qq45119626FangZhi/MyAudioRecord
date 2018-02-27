package com.fz.myrecord;

import android.Manifest;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lqr.audio.AudioPlayManager;
import com.lqr.audio.AudioRecordManager;
import com.lqr.audio.IAudioPlayListener;
import com.lqr.audio.IAudioRecordListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import kr.co.namee.permissiongen.PermissionGen;

public class MainActivity extends AppCompatActivity {
    private static final int TV_NO_CHRCK_HINT = 0;
    private static final int START_RECORD_TIME = 1;

    @Bind(R.id.tv_no_check_hint)
    TextView tvNoCheckHint;
    @Bind(R.id.btn_record)
    Button btnRecord;
    @Bind(R.id.btn_close_record)
    Button btnCloseRecord;
    @Bind(R.id.iv_voice_play_pause)
    ImageView ivVoicePlayPause;
    //    @Bind(R.id.iv_wave1)
//    ImageView ivWave1;
    @Bind(R.id.tv_recrod_time)
    TextView tvRecrodTime;
    //    @Bind(R.id.iv_wave2)
//    ImageView ivWave2;
    @Bind(R.id.ll_record_container)
    LinearLayout llRecordContainer;
    @Bind(R.id.root)
    LinearLayout mRoot;
    @Bind(R.id.spectrumViewLeft)
    SpectrumView spectrumViewLeft;
    private File mAudioDir;
    private MainActivity instance;
    private Uri audioPath;
    private int duration;
    private int startRecordTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        instance = this;
        init();
        initData();
        initListener();
    }

    /**
     * 权限初始化和录音文件路径初始化
     */
    private void init() {
        PermissionGen.with(instance)
                .addRequestCode(100)
                .permissions(Manifest.permission.RECORD_AUDIO
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE
                        , Manifest.permission.WAKE_LOCK
                        , Manifest.permission.READ_EXTERNAL_STORAGE)
                .request();
        AudioRecordManager.getInstance(instance).setMaxVoiceDuration(60);
//        mAudioDir = new File(Environment.getExternalStorageDirectory(), "LQR_AUDIO");
        mAudioDir = new File(instance.getFilesDir(), "eip_audio");
        deleteFile(mAudioDir);
        if (!mAudioDir.exists()) {
            mAudioDir.mkdirs();
        }
        AudioRecordManager.getInstance(instance).setAudioSavePath(mAudioDir.getAbsolutePath());
        File[] files = mAudioDir.listFiles();
        Log.e("TAG", "文件初始个数 : " + files.length);
//        File[] files = mAudioDir.listFiles();
//        for (File file : files) {
//            Log.e("TAG", "file.getPath() : " + file.getPath());
//        }
        spectrumViewLeft.setItemCount(50);

    }

    private void initData() {
    }


    private void readAllAudio() {
        File[] files = mAudioDir.listFiles();
        Log.e("TAG", "录音后文件个数 : " + files.length);
        File file = new File("my_record.amr");
        deleteFile(file.getName());
        try {
            // 新建文件输入流并对它进行缓冲
            FileInputStream input = null;
            BufferedInputStream inBuff = null;
            // 新建文件输出流并对它进行缓冲
            FileOutputStream output = this.openFileOutput(file.getPath(), Context.MODE_PRIVATE);
            BufferedOutputStream outBuff = new BufferedOutputStream(output);

            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                input = new FileInputStream(f);
                inBuff = new BufferedInputStream(input);
                // 缓冲数组
                byte[] b = new byte[1024];
                int len;
                while ((len = inBuff.read(b)) != -1) {
                    outBuff.write(b, 0, len);
                }
            }
            // 刷新此缓冲的输出流
            outBuff.flush();
            //关闭流
            inBuff.close();
            outBuff.close();
            output.close();
            input.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
        File file2 = new File(getFilesDir(), "");
        File[] files1 = file2.listFiles();
        for (File file1 : files1) {
            Log.e("TAG", "file1.path : " + file1.getPath());
            if (file1.isFile()) {
                Log.e("TAG", "file1.size : " + getFileSize(file1.getPath()));
                uri = Uri.parse(file1.getPath());
            }
        }
    }

    Uri uri = null;

    private void initListener() {

        btnRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        AudioRecordManager.getInstance(instance).startRecord();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (isCancelled(v, event)) {
                            AudioRecordManager.getInstance(MainActivity.this).willCancelRecord();

                        } else {
                            AudioRecordManager.getInstance(MainActivity.this).continueRecord();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        AudioRecordManager.getInstance(instance).stopRecord();
                        break;
                }
                return false;
            }
        });


        AudioRecordManager.getInstance(instance).setAudioRecordListener(new IAudioRecordListener() {


            @Override
            public void initTipView() {
            }

            @Override
            public void setTimeoutTipView(int counter) {

            }

            @Override
            public void setRecordingTipView() {
                Toast.makeText(instance, "手指上滑，取消发送", Toast.LENGTH_SHORT);
                llRecordContainer.setVisibility(View.GONE);
            }

            @Override
            public void setAudioShortTipView() {
                Toast.makeText(instance, "录音时间太短", Toast.LENGTH_SHORT);
                llRecordContainer.setVisibility(View.GONE);
                spectrumViewLeft.reset();
            }

            @Override
            public void setCancelTipView() {
                handler.removeMessages(START_RECORD_TIME);
                Toast.makeText(instance, "松开手指，取消发送", Toast.LENGTH_SHORT);
                startRecordTime = 0;
                String time = TimeUtils.secToTime(0);
                tvRecrodTime.setText(time);
                llRecordContainer.setVisibility(View.GONE);
                spectrumViewLeft.reset();
            }

            @Override
            public void destroyTipView() {
//                if (mRecordWindow != null) {
//                    mRecordWindow.dismiss();
//                    this.mRecordWindow = null;
//                    this.btnConfirm = null;
//                    this.btnContinue = null;
//                }
            }

            @Override
            public void onStartRecord() {
                //开始录制
                startRecordTime = 0;
                llRecordContainer.setVisibility(View.VISIBLE);
                handler.sendEmptyMessage(START_RECORD_TIME);
            }

            @Override
            public void onFinish(Uri audioPath, int duration) {

                instance.audioPath = audioPath;
                instance.duration = duration;
                Log.e("TAG", "audioPath : " + audioPath.getPath());
                Log.e("TAG", "file.size : " + getFileSize(audioPath.getPath()));

                if (audioPath != null) {
                    ivVoicePlayPause.setVisibility(View.VISIBLE);
                    btnCloseRecord.setVisibility(View.VISIBLE);
                    Toast.makeText(getApplicationContext(), "录制成功", Toast.LENGTH_SHORT).show();
                }
                handler.removeMessages(START_RECORD_TIME);
//                //发送文件
//                File file = new File(audioPath.getPath());
//                if (file.exists()) {
//                    ivVoicePlayPause.setVisibility(View.VISIBLE);
//                    btnCloseRecord.setVisibility(View.VISIBLE);
//                    Toast.makeText(getApplicationContext(), "录制成功", Toast.LENGTH_SHORT).show();
//                }
            }

            @Override
            public void onAudioDBChanged(int db) {
                spectrumViewLeft.updateForward(db);
//                switch (db / 5) {
//                    case 0:
//                        ivWave1.setImageResource(R.drawable.ar_sound_item_volume_1);
//                        ivWave2.setImageResource(R.drawable.ar_sound_item_volume_1);
//                        break;
//                    case 1:
//                        ivWave1.setImageResource(R.drawable.ar_sound_item_volume_2);
//                        ivWave2.setImageResource(R.drawable.ar_sound_item_volume_2);
//                        break;
//                    case 2:
//                        ivWave1.setImageResource(R.drawable.ar_sound_item_volume_3);
//                        ivWave2.setImageResource(R.drawable.ar_sound_item_volume_3);
//                        break;
//                    case 3:
//                        ivWave1.setImageResource(R.drawable.ar_sound_item_volume_4);
//                        ivWave2.setImageResource(R.drawable.ar_sound_item_volume_4);
//                        break;
//                    case 4:
//                        ivWave1.setImageResource(R.drawable.ar_sound_item_volume_5);
//                        ivWave2.setImageResource(R.drawable.ar_sound_item_volume_5);
//                        break;
//                    case 6:
//                        ivWave1.setImageResource(R.drawable.ar_sound_item_volume_6);
//                        ivWave2.setImageResource(R.drawable.ar_sound_item_volume_6);
//                        break;
//                }
            }

        });
    }


    private boolean isCancelled(View view, MotionEvent event) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        if (event.getRawX() < location[0] || event.getRawX() > location[0] + view.getWidth() || event.getRawY() < location[1] - 40) {
            return true;
        }
        return false;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TV_NO_CHRCK_HINT:
                    tvNoCheckHint.setVisibility(View.GONE);
                    break;
                case START_RECORD_TIME:
                    String time = TimeUtils.secToTime(startRecordTime);
                    tvRecrodTime.setText(time);
                    ++startRecordTime;
                    handler.sendEmptyMessageDelayed(START_RECORD_TIME, 1000);
                    break;
            }
        }
    };

    @OnClick({R.id.btn_record, R.id.btn_close_record, R.id.iv_voice_play_pause})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_record:
                tvNoCheckHint.setVisibility(View.VISIBLE);
                handler.sendEmptyMessageDelayed(TV_NO_CHRCK_HINT, 1000);
                break;
            case R.id.btn_close_record:
                ivVoicePlayPause.setVisibility(View.INVISIBLE);
                btnCloseRecord.setVisibility(View.INVISIBLE);
                llRecordContainer.setVisibility(View.GONE);
                AudioRecordManager.getInstance(instance).destroyRecord();
                break;
            case R.id.iv_voice_play_pause:
                if (flag) {
                    playRecord();
                } else {
                    ivVoicePlayPause.setBackgroundResource(R.drawable.ic_of_msg_record_play);
                    AudioPlayManager.getInstance().stopPlay();
                }
                flag = !flag;
                break;
        }
    }

    private boolean flag = true;

    private void playRecord() {
        ivVoicePlayPause.setBackgroundResource(R.drawable.ic_of_msg_record_pause);
//        ivWave1.setBackgroundResource(R.drawable.play_ationanim);
//        ivWave2.setBackgroundResource(R.drawable.play_ationanim);

        AudioPlayManager.getInstance().startPlay(MainActivity.this, uri, new IAudioPlayListener() {

            private AnimationDrawable animation2;
            private AnimationDrawable animation1;

            @Override
            public void onStart(Uri var1) {
//                animation1 = (AnimationDrawable) ivWave1.getBackground();
//                animation2 = (AnimationDrawable) ivWave2.getBackground();
//                animation1.start();
//                animation2.start();

                startRecordTime = 0;
                handler.sendEmptyMessage(START_RECORD_TIME);
            }

            @Override
            public void onStop(Uri var1) {
//                animation1.stop();
//                animation2.stop();
            }

            @Override
            public void onComplete(Uri var1) {
                handler.removeMessages(START_RECORD_TIME);
                String time = TimeUtils.secToTime(0);
                tvRecrodTime.setText(time);
                ivVoicePlayPause.setBackgroundResource(R.drawable.ic_of_msg_record_play);
                AudioPlayManager.getInstance().stopPlay();
                flag = true;
//                animation1.stop();
//                animation2.stop();
//                animation1.selectDrawable(0);
//                animation2.selectDrawable(0);

            }
        });
    }

    /**
     * 删除文件（包括目录）
     *
     * @param delFile
     */
    public void deleteFile(File delFile) {
        //如果是目录递归删除
        if (delFile.isDirectory()) {
            File[] files = delFile.listFiles();
            for (File file : files) {
                deleteFile(file);
            }
        } else {
            delFile.delete();
        }
        //如果不执行下面这句，目录下所有文件都删除了，但是还剩下子目录空文件夹
        delFile.delete();
    }

    /**
     * 获取文件fpath的大小
     *
     * @return String path的大小
     **/
    public String getFileSize(String fpath) {
        File path = new File(fpath);
        if (path.exists()) {
            DecimalFormat df = new DecimalFormat("#.00");
            String sizeStr = "";
            long size = 0;
            try {
                FileInputStream fis = new FileInputStream(path);
                size = fis.available();
                fis.close();
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "未知大小";
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return "未知大小";
            }
            if (size < 1024) {
                sizeStr = size + "B";
            } else if (size < 1048576) {
                sizeStr = df.format(size / (double) 1024) + "KB";
            } else if (size < 1073741824) {
                sizeStr = df.format(size / (double) 1048576) + "MB";
            } else {
                sizeStr = df.format(size / (double) 1073741824) + "GB";
            }
            return sizeStr;
        } else {
            return "未知大小";
        }
    }

}
