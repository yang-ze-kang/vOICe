package com.example.lbstest;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;
import com.chibde.visualizer.CircleBarVisualizer;
import com.chibde.visualizer.CircleVisualizer;
import com.example.lbstest.speech.util.FucUtil;
import com.example.lbstest.speech.util.JsonParser;
import com.example.lbstest.view.CircleImageView;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.GrammarListener;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
public class OneShot extends FragmentActivity {
    static public MyTtsService ttsService;
    static boolean finishAll = false;
    /**
     * 语音唤醒
     **/
    private TextView text;
    // 语音唤醒对象
    private VoiceWakeuper mIvw = null;
    // 唤醒结果内容
    private String resultString;
    //阈值
    private int curThresh = 1450;
    private String TAG = "ivw";
    /**
     * 语音识别
     **/
    // 语音识别对象
    private SpeechRecognizer mAsr;
    // 识别结果内容
    // private String resultString;
    // 本地语法id
    private String mLocalGrammarID;
    // 本地语法文件
    private String mLocalGrammar;
    // 本地语法构建路径
    private String grmPath = Environment.getExternalStorageDirectory().getAbsolutePath()
            + "/msc/test";
    //语法格式
    private final String GRAMMAR_TYPE_BNF = "bnf";
    String mContent;// 语法、词典临时变量
    int ret = 0;// 函数调用返回值
    /**
     * 语音听写
     */
    // 语音听写对象
    private SpeechRecognizer mVw;
    // 用HashMap存储听写结果
    private HashMap<String, String> mRecognizeResults = new LinkedHashMap<String, String>();
    /**
     * function
     */
    private static boolean isPermissionRequested = false;
    private static boolean isStartNavigation=false;
    // object detection
    private VisualProcess visualProcess;
    static CircleBarVisualizer circleBarVisualizer;
    static CircleVisualizer circleVisualizer;
    private me.yugy.github.residelayout.ResideLayout resideLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpeechUtility.createUtility(this, SpeechConstant.APPID + "=5dcd36b1");
        setContentView(R.layout.activity_one_shot);
        //申请权限
        requestPermission();
        //初始化组件
        initView();
        initTTsService();
        // 创建语音唤醒、识别、合成对象
        mIvw = VoiceWakeuper.createWakeuper(this, null);
        mAsr = SpeechRecognizer.createRecognizer(this, mInitListener);
        mVw = SpeechRecognizer.createRecognizer(this, mInitListener);
        //语音识别语法
        buildGrammer();
        //设置语音唤醒参数
        setIvwParam();
        mIvw.startListening(mWakeuperListener);
    }
    private void initTTsService() {
        Intent intent = new Intent(this, MyTtsService.class);
        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ttsService = ((MyTtsService.LocalBinder) service).getService();//获取服务对象
            visualProcess = new VisualProcess(ttsService);
            if (!finishAll)
                ttsService.speakText("欢迎使用voice");
            else
                finishAll = false;
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            ttsService = null;
        }
    };
    //初始化组件
    void initView() {
        //绑定组件
        resideLayout = (me.yugy.github.residelayout.ResideLayout) findViewById(R.id.reside_layout);
        text = (TextView) findViewById(R.id.text);
        circleVisualizer = (CircleVisualizer) findViewById(R.id.circle);
        circleBarVisualizer = (CircleBarVisualizer) findViewById(R.id.visualizer);
        circleBarVisualizer.setColor(ContextCompat.getColor(this, R.color.deepskyblue));
        circleVisualizer.setColor(ContextCompat.getColor(this, R.color.aqua));
        circleVisualizer.setRadiusMultiplier(1.5f);
        circleVisualizer.setStrokeWidth(2);
        circleVisualizer.setVisibility(View.GONE);
        CircleImageView circleImageView =(CircleImageView) findViewById(R.id.circle_image_view);
        circleImageView.playAnim();

//        ttsService.setCircleBarVisualizer((CircleBarVisualizer) findViewById(R.id.visualizer));
//        ttsService.setCircleVisualizer((CircleVisualizer) findViewById(R.id.circle));
    }
    //功能
    public void myFunction(String function) {
        switch (function) {
            case "开启导航":
                ttsService.speakText("您想要去哪？");
                mIvw.stopListening();
                setmVoiceWriteParam();
                mVw.startListening(mVoiceWriteListener);
                break;
            case "我要读书":
                ttsService.ifReadingBook = true;
                ttsService.readBook("life.txt");
                break;
            case "继续读书":
                ttsService.ifReadingBook = true;
                ttsService.continueReadBook();
                break;
            case "徐绍越":
                ttsService.speakText("徐绍越是头猪");
                break;
            case "退出导航":
                finishAll = true;
                mIvw.stopListening();
                WNaviGuideActivity.wNaviGuideActivity.finish();
                break;
            case "放首歌":
                ttsService.playMusic();
                break;
            case "开启识别":
                visualProcess.startVisualProcess();
                showTip("开启识别");
                break;
            case "退出识别":
                visualProcess.stopVisualProcess();
                showTip("退出识别");
                break;
            case "没有匹配结果":
                ttsService.speakText("没有匹配结果");
                break;
        }
    }
    /**
     * 语音唤醒
     */
    //初始化唤醒引擎
    private void setIvwParam() {
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            resultString = "";
            text.setText(resultString);
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            // 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:" + curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "1");
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, "0");
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getResource());
            // 设置唤醒录音保存路径，保存最近一分钟的音频
            mIvw.setParameter(SpeechConstant.IVW_AUDIO_PATH, Environment.getExternalStorageDirectory().getPath() + "/msc/ivw.wav");
            mIvw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        } else {
            showTip("唤醒未初始化");
        }
    }
    //唤醒监听器
    private WakeuperListener mWakeuperListener = new WakeuperListener() {
        @Override
        public void onResult(WakeuperResult result) {
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 " + text);
                buffer.append("\n");
                buffer.append("【操作类型】" + object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】" + object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】" + object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】" + object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】" + object.optString("eos"));
                resultString = buffer.toString();
            } catch (JSONException e) {
                resultString = "结果解析出错";
                e.printStackTrace();
            }
            if (ttsService.ifReadingBook == true) {
                ttsService.ifReadingBook = false;
                ttsService.StopSpeaking();
            }
            ttsService.pauseMusic();
            mIvw.stopListening();
            //语音识别语法
            setParam();
            mAsr.startListening(mRecognizerListener);
        }
        @Override
        public void onError(SpeechError error) {
            Log.i(TAG, error.getPlainDescription(true));
            resultString += "无录音权限！\n请同意或设置权限后重启。";
            ttsService.speakText("没有录音权限");
            text.setText(resultString);
            showTip(error.getPlainDescription(true));
        }
        @Override
        public void onBeginOfSpeech() {
        }
        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {
        }
        @Override
        public void onVolumeChanged(int volume) {
            // TODO Auto-generated method stub
        }

    };
    //获取唤醒资源路径
    private String getResource() {
        final String resPath = ResourceUtil.generateResourcePath(OneShot.this, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + getString(R.string.app_id) + ".jet");
        Log.d("ivw", "resPath: " + resPath);
        return resPath;
    }
    /**
     * 语音识别
     */
    //构建语法
    private void buildGrammer() {
        mLocalGrammar = FucUtil.readFile(OneShot.this, "function.bnf", "utf-8");
        // 本地-构建语法文件，生成语法id
        text.setText(FucUtil.readFile(OneShot.this, "myfunction", "utf-8"));
        mContent = new String(mLocalGrammar);
        //mAsr.setParameter(SpeechConstant.PARAMS, null);
        // 设置文本编码格式
        mAsr.setParameter(SpeechConstant.TEXT_ENCODING, "utf-8");
        // 设置引擎类型
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        // 设置语法构建路径
        mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
        // 设置资源路径
        mAsr.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
        ret = mAsr.buildGrammar(GRAMMAR_TYPE_BNF, mContent, grammarListener);
    }
    //初始化语法构建监听器
    GrammarListener grammarListener = new GrammarListener() {
        @Override
        public void onBuildFinish(String grammarId, SpeechError error) {
            if (error == null) {
                mLocalGrammarID = grammarId;
                //showTip("语法构建成功：" + grammarId);
                //设置识别参数开始识别
                setParam();
            } else {
                showTip("语法构建失败,错误码：" + error.getErrorCode() + ",请点击网址https://www.xfyun.cn/document/error-code查询解决方案");
            }
        }
    };
    //设置语音识别参数
    public void setParam() {
        boolean result = true;
        // 清空参数
        mAsr.setParameter(SpeechConstant.PARAMS, null);
        // 设置识别引擎
        mAsr.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        // 设置本地识别资源
        mAsr.setParameter(ResourceUtil.ASR_RES_PATH, getResourcePath());
        // 设置语法构建路径
        mAsr.setParameter(ResourceUtil.GRM_BUILD_PATH, grmPath);
        // 设置返回结果格式
        mAsr.setParameter(SpeechConstant.RESULT_TYPE, "json");
        // 设置本地识别使用语法id
        mAsr.setParameter(SpeechConstant.LOCAL_GRAMMAR, mLocalGrammarID);
        mAsr.setParameter(SpeechConstant.KEY_SPEECH_TIMEOUT, "-1");
        mAsr.setParameter(SpeechConstant.VAD_BOS, "4000");
        mAsr.setParameter(SpeechConstant.VAD_EOS, "1000");
        // 设置识别的门限值
        mAsr.setParameter(SpeechConstant.MIXED_THRESHOLD, "30");
        //mediaPlayer.release();
        //ret = mAsr.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            showTip("识别失败,错误码: " + ret);
        }
    }
    //初始化识别监听器。
    private InitListener mInitListener = code -> {
        Log.d(TAG, "SpeechRecognizer init() code = " + code);
        if (code != ErrorCode.SUCCESS) {
            showTip("初始化失败,错误码：" + code);
        }
    };
    //识别监听器。
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
            Log.d(TAG, "返回音频数据：" + data.length);
        }
        @Override
        public void onResult(final RecognizerResult result, boolean isLast) {
            if (null != result && !TextUtils.isEmpty(result.getResultString())) {
                myFunction(JsonParser.function(result.getResultString()));
            }
        }
        @Override
        public void onEndOfSpeech() {
            mAsr.stopListening();
            setIvwParam();
            mIvw.startListening(mWakeuperListener);
        }
        @Override
        public void onBeginOfSpeech() {
            ttsService.speakText("voice为您服务！");
        }
        @Override
        public void onError(SpeechError error) {
            if (error.getErrorCode() == 20005) {
                ttsService.speakText("没有匹配结果");
            } else if (error.getErrorCode() == 10118) {//没有数据
                text.setText("没有匹配结果");
                ttsService.speakText("没有匹配结果");
            } else {
                showTip("onError Code：" + error.getErrorCode());
            }
            showTip("onError Code：" + error.getErrorCode());
            mAsr.stopListening();
            //mIvw.startListening(mWakeuperListener);
        }
        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {
        }
    };

    //获取识别资源路径
    private String getResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        //识别通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "asr/common.jet"));
        return tempBuffer.toString();
    }
    /**
     * 语音听写
     */
    //参数设置
    public void setmVoiceWriteParam() {
        // 清空参数
        mVw.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎。TYPE_LOCAL表示本地，TYPE_CLOUD表示云端，TYPE_MIX 表示混合
        mVw.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
        // 设置返回结果格式
        mVw.setParameter(SpeechConstant.RESULT_TYPE, "json");

        String lag = "mandarin";
        if (lag.equals("en_us")) {  // 设置语言
            mVw.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            mVw.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mVw.setParameter(SpeechConstant.ACCENT, lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mVw.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音

        mVw.setParameter(SpeechConstant.VAD_EOS, "1000");
        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mVw.setParameter(SpeechConstant.ASR_PTT, "0");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mVw.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mVw.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/recognize.wav");
    }
    //听写监听器
    private RecognizerListener mVoiceWriteListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            showTip(error.getPlainDescription(true));
        }
        @Override
        public void onEndOfSpeech() {
        }
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            setIvwParam();
            mIvw.startListening(mWakeuperListener);
            getPoisition(results);
            if (isLast) {
                // TODO 最后的结果
            }
        }
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {

        }
    };
    private void getPoisition(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        mRecognizeResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mRecognizeResults.keySet()) {
            resultBuffer.append(mRecognizeResults.get(key));
        }
        Intent intent = new Intent(OneShot.this, MainActivity.class);
        intent.putExtra("yang", resultBuffer.toString());
        showTip(resultBuffer.toString());
        startActivity(intent);
    }
    //权限
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= 23 && !isPermissionRequested) {
            isPermissionRequested = true;
            ArrayList<String> permissionsList = new ArrayList<>();
            String[] permissions = {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.WRITE_SETTINGS,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                    Manifest.permission.VIBRATE
            };
            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm);
                    // 进入到这里代表没有权限.
                }
            }
            if (permissionsList.isEmpty()) {
                return;
            } else {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), 0);
            }
        }
    }
    //吐司
    private void showTip(final String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }
}