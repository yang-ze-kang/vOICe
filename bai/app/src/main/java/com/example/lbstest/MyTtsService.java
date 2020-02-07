package com.example.lbstest;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.view.View;
import android.widget.Toast;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
public class MyTtsService extends Service {
    /**
     * 语音合成
     **/
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认云端发音人
    public static String voicerCloud = "xiaoyan";
    //缓冲进度
    private int mPercentForBuffering = 0;
    //播放进度
    private int mPercentForPlaying = 0;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private Toast mToast;
    //书的行数
    ArrayList<String> list;
    private int totalBookLine = 0;
    private int currentBookLine = 0;
    //字符串列表存书
    public boolean ifReadingBook;
    // 默认云端发音人
    static boolean ifPlayingTTs = false;
    private MediaPlayer mediaPlayer;
    private String ttsPath = Environment.getExternalStorageDirectory() + "/msc/tts.wav";
    // 音频可视化
//    CircleBarVisualizer circleBarVisualizer ;
//    CircleVisualizer circleVisualizer;
    @Override
    public void onCreate() {
        super.onCreate();
        ifReadingBook = false;
        ifPlayingTTs = false;
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "="+R.string.app_id);
        mTts = SpeechSynthesizer.createSynthesizer(this, mTtsInitListener);
        setTtsParam();
    }
    //返回获取该服务的对象
    private final IBinder localBinder = new LocalBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }
    public class LocalBinder extends Binder {
        MyTtsService getService() {
            return MyTtsService.this;
        }
    }
    /**
     * 语音合成
     */
    //发音
    public void speakText(String s) {
        if (!ifPlayingTTs) {
            mTts.synthesizeToUri(s, ttsPath, mTtsListener);
        }
    }
    //停止合成
    public void StopSpeaking() {
        mTts.stopSpeaking();
    }
    //读书
    public void readBook(String bookFilePath) {
        ifReadingBook = true;
        currentBookLine = 0;
        list = readFile(this, bookFilePath, "UTF-8");
        totalBookLine = list.size();
        speakText(list.get(currentBookLine));
    }
    //继续读书
    public void continueReadBook() {
        if (currentBookLine <= totalBookLine && ifReadingBook)
            speakText(list.get(currentBookLine));
    }
    private void playVoice(String mp3Path) {
        if (mp3Path.isEmpty())
            return;
        mediaPlayer = new MediaPlayer();
        OneShot.circleBarVisualizer.setPlayer(mediaPlayer.getAudioSessionId());
        try {
            mediaPlayer.setDataSource(mp3Path);//指定音频文件的路径
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.prepareAsync();//让mediaplayer进入准备状态
        } catch (IOException e) {
            e.printStackTrace();
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
        }
        mediaPlayer.setOnCompletionListener(mp -> {
            // 在播放完毕被回调
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            if (currentBookLine < totalBookLine && ifReadingBook) {
                speakText(list.get(++currentBookLine));
            }
        });
        mediaPlayer.setOnErrorListener((mp, what, extra) -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            return true;
        });
        mediaPlayer.setOnPreparedListener(mp -> {
            mp.start();//开始播放
            mp.seekTo(0);
        });
    }
    //语音合成参数设置
    private void setTtsParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        //设置合成
        //设置使用云端引擎
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicerCloud);
        //mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY,"1");//支持实时音频流抛出，仅在synthesizeToUri条件下支持
        //设置合成语速、音调、音量、音频流类型
        mTts.setParameter(SpeechConstant.SPEED, "50");
        mTts.setParameter(SpeechConstant.PITCH, "50");
        mTts.setParameter(SpeechConstant.VOLUME, "50");
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");
        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }
    //初始化监听
    private InitListener mTtsInitListener = code -> {
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败,错误码：" + code + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
        } else {
            // 初始化成功，之后可以调用startSpeaking方法
            // 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
            // 正确的做法是将onCreate中的startSpeaking调用移至这里
            //speakText("欢迎使用voice");
        }
    };
    //语音合成监听监听器
    private SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            //showTip("开始播放");
        }
        @Override
        public void onSpeakPaused() {
            //showTip("暂停播放");
        }
        @Override
        public void onSpeakResumed() {
            //showTip("继续播放");
        }
        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
//            mPercentForBuffering = percent;
//            showTip(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }
        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
//            mPercentForPlaying = percent;
//            showTip(String.format(getString(R.string.tts_toast_format),
//                    mPercentForBuffering, mPercentForPlaying));
        }
        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                playVoice(ttsPath);
            } else {
                ifReadingBook = false;
                showTip(error.getPlainDescription(true));
            }
            ifPlayingTTs = false;
        }
        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

//            //实时音频流输出参考
//            if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
//
//            }
        }
    };
    //语音合成读书
    public ArrayList readFile(Context mContext, String file, String code) {
        ArrayList<String> list = new ArrayList<>();
        try {
            InputStreamReader input = new InputStreamReader(
                    getResources().getAssets().open(file));
            BufferedReader buf = new BufferedReader(input);
            String line = "";
            while ((line = buf.readLine()) != null) {
                list.add(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    //吐司
    private void showTip(final String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
    void playMusic() {
//        OneShot.circleBarVisualizer.setVisibility(View.GONE);
//        OneShot.circleVisualizer.setVisibility(View.VISIBLE);
        mediaPlayer = MediaPlayer.create(this, R.raw.ballon);
        OneShot.circleBarVisualizer.setPlayer(mediaPlayer.getAudioSessionId());
        mediaPlayer.start();
    }
    void pauseMusic(){
        if(mediaPlayer!=null&&mediaPlayer.isPlaying())
            mediaPlayer.pause();
    }
}
