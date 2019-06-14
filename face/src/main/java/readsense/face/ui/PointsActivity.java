package readsense.face.ui;

import android.os.Bundle;
import android.support.v4.util.SimpleArrayMap;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import dou.utils.DLog;
import mobile.ReadFace.YMFace;
import readsense.face.R;
import readsense.face.base.BaseApplication;
import readsense.face.base.BaseCameraActivity;
import readsense.face.util.Accelerometer;
import readsense.face.util.RetrofitHelp;
import readsense.face.util.TrackUtil;


/**
 * Created by mac on 16/7/4.
 */
public class PointsActivity extends BaseCameraActivity {


    public static final String SHOW_TAG = "show";
    private TextView page_title, page_right;
    boolean showPoint = false;
    private View pop_rotate, _logo;
    private Accelerometer acc;
    boolean preFrame = false;
    private boolean threadStart;

    Button get;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.points_activity);
        get = findViewById(R.id.get);



        setCamera_max_width(-1);
        initCamera();
        showFps(true);
        savePath = getCacheDir().getAbsolutePath();
        trackingMap = new SimpleArrayMap<>();
        trackingIdMap = new SimpleArrayMap<>();
        initView();

        File output = new File("/sdcard/img/output/");
        if (!output.exists()) {
            output.mkdirs();
        }
    }

    public void initView() {

        pop_rotate = findViewById(R.id.pop_rotate);
        _logo = findViewById(R.id._logo);
        if (BaseApplication.useLogo) _logo.setVisibility(View.VISIBLE);
        page_title = (TextView) findViewById(R.id.page_title);
        page_right = (TextView) findViewById(R.id.page_right);
        page_right.setText(R.string.points_switch);
//        page_right.setVisibility(View.GONE);
        if (getIntent().getBooleanExtra(SHOW_TAG, false)) {
            page_title.setText(R.string.points);
            showPoint = true;
        } else {
            page_title.setText(R.string.start_4);
            showPoint = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        start = false;
    }

    boolean start = false;

    @Override
    protected void onResume() {
        super.onResume();
        start = true;
    }

    public void cropNormFace(YMFace ymFace, byte[] yuvBytes) {
        //camera id = CAMERA_FACING_FRONT
        //screenOritation landscape
        //人脸角度作为判定条件 , 可进行调整
        float facialOri[] = ymFace.getHeadpose();
        int x = (int) facialOri[0];
        int y = (int) facialOri[1];
        int z = (int) facialOri[2];

        boolean notCompare = false;
        if (Math.abs(z) >= 25) notCompare = true;
        if (y > 10 || y < -30) notCompare = true;
        if (Math.abs(x) > 15) notCompare = true;
        if (notCompare) return;
        long time = System.currentTimeMillis();
        int imgQuality = faceTrack.getFaceQuality(0);
        DLog.d(x + " : " + y + " : " + z + "  imgQuality : " + imgQuality + " cost" + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();

        if (imgQuality > 85) {
            File file = new File("/sdcard/img/head");
            if (!file.exists()) file.mkdirs();
            faceTrack.cropFace(yuvBytes, iw, ih, ymFace.getRect(), "/sdcard/img/head/" + System.currentTimeMillis() + ".bmp");
            DLog.d("save image cost : " + (System.currentTimeMillis() - time));
        }
    }

    private SimpleArrayMap<Integer, YMFace> trackingMap;
    private SimpleArrayMap<Integer, Integer> trackingIdMap;


    @Override
    protected List<YMFace> analyse(byte[] bytes, int iw, final int ih) {
        if (faceTrack == null) return null;
//        List<YMFace> faces = null;
        List<YMFace> faces = faceTrack.trackMulti(bytes, iw, ih);


        final List<YMFace> preFaces = faces;
//        if (!threadStart) {
//            if (preFaces != null && preFaces.size() > 0) {
//                final byte[] yuvData = new byte[iw * ih * 2];
//                System.arraycopy(bytes, 0, yuvData, 0, bytes.length);
//                threadStart = true;
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        //测试
//                        File catchDir = new File("/sdcard/img");
//                        if (!catchDir.exists()) catchDir.mkdirs();
//                        long time = System.currentTimeMillis();
//                        for (int i = 0; i < preFaces.size(); i++) {
//                            YMFace ymFace = preFaces.get(i);
//                            float[] rect = ymFace.getRect();
//                            RectF rectF = new RectF(rect[0], rect[1], rect[0] + rect[2], rect[1] + rect[3]);
//
//                            faceTrack.cropFace(yuvData, iw, ih, ymFace.getRect(), "/sdcard/img/" + System.currentTimeMillis() + "__" + ymFace.getTrackConfidence() + ".bmp");
//                            faceTrack.cropFace(yuvData, iw, ih, new float[]{0, 0, iw, ih}, "/sdcard/img/" + System.currentTimeMillis() + "__" + ymFace.getTrackConfidence() + ".bmp");
//                        }
//                        threadStart = false;
//
//                        DLog.d("save time = " + (System.currentTimeMillis() - time));
//                    }
//                }).start();
//            }
//        }


        if (faces != null && faces.size() > 0) {

            //扣脸接口示例
//            for (int i = 0; i < faces.size(); i++) {
//                YMFace ymFace = faces.get(i);
//                int trackId = ymFace.getTrackId();
//
//                if (trackingIdMap.containsKey(trackId))
//                    trackingIdMap.put(trackId, trackingIdMap.get(trackId) + 1);
//                else {
//                    trackingIdMap.put(trackId, +1);
//                }
//
//                if (trackingIdMap.get(trackId) % 10 == 0) {
////                    boolean b = faceTrack.nativeCropFaceAndBackgroundByTrackId("/sdcard/img/output/" + System.currentTimeMillis()
////                            + "_" + (trackId) + "_.jpg","/sdcard/img/output/" + System.currentTimeMillis()
////                            + "_" + (trackId) + "_background_.jpg", trackId, 0);
//
////                    boolean b = faceTrack.nativeCropFaceByTrackId(
////                            "/sdcard/img/output/" + System.currentTimeMillis() + "_" + (trackId) + "_.jpg",
////                            trackId, 0);
////
//                    float[] rect = faceTrack.nativeCropFaceBackgroundRectByTrackId(
//                            "/sdcard/img/output/" + System.currentTimeMillis()
//                                    + "_" + (trackId) + "_.jpg",
//                            "/sdcard/img/output/" + System.currentTimeMillis() + "_" + (trackId) + "_bg.jpg",
//                            trackId, 0);
//
//                    DLog.d("crop_result: " + Arrays.toString(rect));
//                }
//            }

            if (!preFrame) {
                if (!threadStart) {
                    threadStart = true;

                    if (trackingMap.size() > 50) trackingMap.clear();
                    //找到最大人脸框
                    int maxIndex = 0;
                    for (int i = 1; i < faces.size(); i++) {
                        if (faces.get(maxIndex).getRect()[2] <= faces.get(i).getRect()[2]) {
                            maxIndex = i;
                        }
                    }

//                    /**
//                     * eyeOpen==1 -> 当前为睁眼
//                     */
//                    int eyeOpen = faceTrack.isEyeOpen(maxIndex);
//
//                    /**
//                     * score==1 -> 当前为微笑
//                     */
//                    int score = faceTrack.getHappyScore(maxIndex);
//                    /**
//                     * mouthOpen==true -> 当前为张嘴
//                     */
//                    boolean mouthOpen = faceTrack.isMouthOpen(maxIndex);
//
//                    DLog.d(eyeOpen+" : "+score+" : "+mouthOpen);

                    final YMFace ymFace = faces.get(maxIndex);
                    final int anaIndex = maxIndex;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                final int trackId = ymFace.getTrackId();
                                if (!trackingMap.containsKey(trackId)) {
                                    float[] headposes = ymFace.getHeadpose();
                                    if (!(Math.abs(headposes[0]) > 30
                                            || Math.abs(headposes[1]) > 30
                                            || Math.abs(headposes[2]) > 30)) {


                                        int gender = faceTrack.getGender(anaIndex);

                                        int gender_confidence = faceTrack.getGenderConfidence(anaIndex);
                                        //有可能获取性别可信度不够高，需重新获取
                                        DLog.d(gender + " ：" + gender_confidence);
                                        if (gender_confidence >= 90) {
                                            ymFace.setAge(faceTrack.getAge(anaIndex));
                                            ymFace.setGender(gender);

                                            trackingMap.put(trackId, ymFace);
                                            long time = System.currentTimeMillis();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                threadStart = false;
                            }
                        }
                    }).start();
                }

                for (int i = 0; i < faces.size(); i++) {
                    YMFace ymFace = faces.get(i);
                    int trackId = ymFace.getTrackId();
                    if (trackingMap.containsKey(trackId)) {
                        YMFace face = trackingMap.get(trackId);
                        ymFace.setAge(face.getAge());
                        ymFace.setGender(face.getGender());
                    }
                }
            } else {
                for (int i = 0; i < faces.size(); i++) {
                    YMFace ymFace = faces.get(i);
                    ymFace.setAge(faceTrack.getAge(i));
                    ymFace.setGender(faceTrack.getGender(i));
                    ymFace.setGenderConfidence(faceTrack.getGenderConfidence(i));
                }
            }
        }


        return faces;
    }

    String savePath = "";

    boolean startRecord = false;

    public void topClick(View view) {
        switch (view.getId()) {
            case R.id.page_cancle:
                finish();
                break;
            case R.id.get:
                if (!startRecord) {
                    startRecord = true;
                    mCameraHelper.startRecord("/sdcard/img/" + System.currentTimeMillis() + "_record.mp4");
                    get.setText("停止");
                } else {
                    startRecord = false;
                    mCameraHelper.stopRecord();
                    get.setText("拍视频");
                }
                break;
            case R.id.page_right:
                switchCamera();
                break;
        }
    }

    @Override
    protected void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, int cameraId, String fps) {
        TrackUtil.drawAnim(faces, draw_view, scale_bit, cameraId, fps, showPoint);
    }

//    UploadManager uploadManager;
//
//    private String token = "";
//
//    public void upload(final String path) {
//
//        if (StringUtils.isNullOrEmpty(token)) {
//            getToken(path);
//            return;
//        }
//
//        if (uploadManager == null)
//            uploadManager = new UploadManager();
//
//        uploadManager.put(
//                path,
//                "alpha_face_" + System.currentTimeMillis() + ".jpg",
//                token,
//                new UpCompletionHandler() {
//                    @Override
//                    public void complete(String key, ResponseInfo info, JSONObject response) {
//                        DLog.d("qiniu", key + ",\r\n " + info + ",\r\n " + response);
//                        Toast.makeText(mContext, "上传成功", Toast.LENGTH_SHORT).show();
//                        File file = new File(path);
//                        file.delete();
//                    }
//                }, null);
//
//
//    }
//
//    private void getToken(final String data) {
//        String api = "http://test.fashionyear.net/uploads/uptoken";
//        VolleyHelper.doGet(api, new VolleyHelper.HelpListener() {
//            @Override
//            public void onResponse(String response) {
//                try {
//                    token = new JSONObject(response).getString("uptoken");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                upload(data);
//            }
//
//            @Override
//            public void onError(VolleyError error) {
//
//            }
//        });
//
//    }


    boolean isPost = false;

    void postImage(final YMFace ymFace, final byte[] bytes, final int iw, final int ih) {
        if (!isPost) {
            isPost = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        File file = new File("/sdcard/img/head");
                        if (!file.exists()) file.mkdirs();
                        String path_to_image = file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".bmp";
                        final File file_image = new File(path_to_image);
                        faceTrack.cropFace(bytes, iw, ih, ymFace.getRect(), path_to_image);


                        RetrofitHelp.getInstance("http://121.42.141.249:8899/").postHeadToServer(file_image
                                , new RetrofitHelp.ApiListener() {
                                    @Override
                                    public void onError(String var1) {
                                        DLog.d("error result = " + var1);
                                        isPost = false;
                                    }

                                    @Override
                                    public void onCompleted(String var1) {
                                        DLog.d("result = " + var1);
//                                file_image.delete();
                                        isPost = false;
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                        isPost = false;
                    }
                }
            }).start();

        }
    }
}
