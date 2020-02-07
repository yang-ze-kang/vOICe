package com.example.lbstest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.example.lbstest.controller.CameraImageController;
import com.example.lbstest.controller.ObjectDetectionController;
import com.example.lbstest.model.DetectedResult;
import com.example.lbstest.model.DirectedResult;
import com.example.lbstest.util.rx.OnlyNextObserver;
import com.example.lbstest.util.rx.RxTimerUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class VisualProcess {
    private MyApplication app;
    private MyTtsService ttsService;
    private CameraImageInterval cameraImageInterval;
    private CameraImageController cameraImageController;
    private ObjectDetectionController objectDetectionController;
    private Map<String, String> dict;
    private Set<String> blockSet;
    public VisualProcess(MyTtsService ttsService) {
        this.ttsService = ttsService;
        initData();
    }
    public void startVisualProcess() {
        RxTimerUtil.interval(5000, cameraImageInterval);
    }
    public void stopVisualProcess() {
        RxTimerUtil.cancel();
    }
    public class CameraImageInterval implements RxTimerUtil.IRxNext {
        @Override
        public void doNext(long number) {
            getImageWithRx();
        }
    }
    private String cfDict(String key) {
        String transName = dict.get(key);
        return transName == null ? key : transName;
    }
    private String convertToString(List<DetectedResult> detectionList, String header) {
        StringBuilder builder = new StringBuilder();
        if (detectionList.size() > 0) {
            builder.append(header);
            for (int i = 0; i < detectionList.size(); i++) {
                if (i > 0) {
                    builder.append("，");
                }
                builder.append(detectionList.get(i).number);
                builder.append(cfDict(detectionList.get(i).typeName));
                if (i == detectionList.size() - 1) {
                    if (blockSet.contains(detectionList.get(i).typeName)) {
                        builder.append("，请注意避让。");
                    } else {
                        builder.append("，请留意。");
                    }
                }
            }
        }
        return builder.toString();
    }
    private void getImageWithRx() {
        cameraImageController.getCameraImageBitmap()
                .map(responseBody
                        -> BitmapFactory.decodeStream(responseBody.byteStream()))
                .subscribe(new OnlyNextObserver<Bitmap>() {

                    @Override
                    public void onNext(Bitmap bitmap) {
                        objectDetectionController.uploadImageBitmap(bitmap).subscribe(new OnlyNextObserver<DirectedResult>() {
                            @Override
                            public void onNext(DirectedResult directedResult) {
                                String res = convertToString(directedResult.left, "您的左前方有") +
                                        convertToString(directedResult.center, "您的正前方有") +
                                        convertToString(directedResult.right, "您的右前方有");
                                Log.e("TAG",directedResult.left.toString());
                                new Thread(() -> {
                                    Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
                                    ttsService.speakText(res);
                                }).start();
                                Log.e("TAG", res);
                            }
                        });
                    }
                });
    }
    private void initData() {
        cameraImageInterval = new CameraImageInterval();
        cameraImageController = new CameraImageController();
        objectDetectionController = new ObjectDetectionController();
        dict = new HashMap<>();
        dict.put("person", "名行人");
        dict.put("car", "辆汽车");
        dict.put("bus", "辆公交车");
        dict.put("chair", "把椅子");
        dict.put("dining-table", "张餐桌");
        dict.put("bottle", "个瓶子");
        dict.put("cup", "个水杯");
        dict.put("cell phone", "部手机");
        dict.put("mouse", "个鼠标");
        dict.put("tvmonitor", "台显示器");
        dict.put("umbrella", "把伞");
        dict.put("backpack", "个双肩包");
        dict.put("laptop", "台笔记本电脑");
        dict.put("keyboard", "副键盘");
        dict.put("remote", "部遥控器");
        dict.put("motorbike","辆摩托车");
        dict.put("aeroplane","架飞机");
        dict.put("truck","辆卡车");
        dict.put("boat","艘轮船");
        dict.put("traffic light","个交通灯");
        dict.put("fire hydrant","个消防栓");
        dict.put("stop sign","个站牌");
        dict.put("parking meter","个停车收费器");
        dict.put("bench","个长椅");
        dict.put("bird","只小鸟");
        dict.put("cat","只小猫");
        dict.put("dog","只小狗");
        dict.put("backpack","个包");
        dict.put("umbrella","把雨伞");
        dict.put("hangbag","个手提包");
        dict.put("tie","个领带");
        dict.put("suitcase","个行李箱");
        dict.put("skis","个滑雪板");
        dict.put("snowboard","个滑雪板");
        dict.put("sports ball","个运动球");
        dict.put("kite","个风筝");
        dict.put("baseball bat","个棒球棒");
        dict.put("baseball glove","副手套");
        dict.put("skateboard","个滑板");
        dict.put("surfboard","个冲浪板");
        dict.put("tennis racket","副网球拍");
        dict.put("bottle","个瓶子");
        dict.put("wine glass","只酒杯");
        dict.put("cup","只杯子");
        dict.put("fork","把叉子");
        dict.put("knife","把刀");
        dict.put("spoon","把勺子");
        dict.put("bowl","个碗");
        dict.put("banana","个香蕉");
        dict.put("apple","个苹果");
        dict.put("sandwich","个三明治");
        dict.put("orange","个橙子");
        dict.put("broccoli","个西兰花");
        dict.put("carrot","个胡萝卜");
        dict.put("hot dog","个热狗");
        dict.put("pizza","个披萨");
        dict.put("donut","个炸面圈");
        dict.put("cake","个蛋糕");
        dict.put("chair","把椅子");
        dict.put("sofa","座沙发");
        dict.put("pottedplant","个盆栽");
        dict.put("bed","座床");
        dict.put("diningtable","个餐桌");
        dict.put("toilet","座厕所");
        dict.put("tvmonitor","个电视");
        dict.put("microwave","个微波炉");
        dict.put("oven","个烤箱");
        dict.put("toaster","个烤箱");
        dict.put("sink","个水槽");
        dict.put("refrigerator","个冰箱");
        dict.put("book","本书");
        dict.put("clock","个钟表");
        dict.put("vase","个花瓶");
        dict.put("scissors","副剪刀");
        dict.put("teddy bear","个泰迪熊");
        dict.put("hair drier","个吹风机");
        dict.put("toothbrush","个牙刷");
        blockSet = new HashSet<>();
        blockSet.add("person");
        blockSet.add("car");
        blockSet.add("bus");
        blockSet.add("chair");
        blockSet.add("dining-table");
//        messSet = new HashSet<>();
//        messSet.add("bottle");
//        messSet.add("cup");
//        messSet.add("cell phone");
//        messSet.add("mouse");
//        messSet.add("tvmonitor");
//        messSet.add("umbrella");
//        messSet.add("backpack");
//        messSet.add("laptop");
//        messSet.add("keyboard");
//        messSet.add("remote");
    }
}


