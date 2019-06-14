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
import readsense.face.util.RetrofitHelp;
import readsense.face.util.TrackUtil;

public class DetectPersonActivity extends BaseCameraActivity {

    private TextView page_title, page_right;
    private View pop_rotate, _logo;
    private boolean threadStart;

    Button get;
    int live_arr[] = new int[3];
    int frame_count = 0;
    int mem_trackingId = -1;

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
        page_title.setText(R.string.more_detect_person);
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

    private SimpleArrayMap<Integer, YMFace> trackingMap;
    private SimpleArrayMap<Integer, Integer> trackingIdMap;


    @Override
    protected List<YMFace> analyse(byte[] bytes, int iw, final int ih) {
        if (faceTrack == null) return null;
        List<YMFace> faces = faceTrack.trackMulti(bytes, iw, ih);


        if (faces != null && faces.size() > 0) {

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

                if (maxIndex != mem_trackingId) {
                    mem_trackingId = maxIndex;
                    live_arr = new int[3];
                    frame_count = 0;
                }
                final YMFace ymFace = faces.get(maxIndex);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final int trackId = ymFace.getTrackId();


                            int faceQuality = faceTrack.getFaceQuality(0);
                            ymFace.setFaceQuality(faceQuality);
                            if (faceQuality >= 92) {
                                int[] ints = faceTrack.livenessDetect(0, 0.7f);
                                if (ints != null) {

                                    //在人脸质量允许的情况下，投票3帧取2， 或自定义
                                    live_arr[frame_count % 3] = ints[0];
                                    if (frame_count <= 2) {

                                    } else {
                                        int suc_count = 0;
                                        for (int i : live_arr) {
                                            if (i == 1) suc_count++;
                                        }
                                        if (suc_count >= 2)
                                            ymFace.setLiveness(1);
                                    }
                                    frame_count++;

                                    /**
                                     * 不使用投票 ymFace.setLiveness(ints[0]);
                                     */
                                }
                            }

                            trackingMap.put(trackId, ymFace);

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
                    ymFace.setLiveness(face.getLiveness());
                    ymFace.setFaceQuality(face.getFaceQuality());
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
        TrackUtil.drawAnimLiveness(faces, draw_view, scale_bit, cameraId, fps);
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
