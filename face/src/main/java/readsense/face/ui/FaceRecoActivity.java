package readsense.face.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.support.v4.util.SimpleArrayMap;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import dou.utils.BitmapUtil;
import dou.utils.DLog;
import dou.utils.FileUtil;
import dou.utils.StringUtils;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.net.NetFaceTrack;
import readsense.face.R;
import readsense.face.base.BaseCameraActivity;
import readsense.face.ui.icount.RegisterImageCameraActivity;
import readsense.face.util.DrawUtil;

public class FaceRecoActivity extends BaseCameraActivity {


    private SimpleArrayMap<Integer, YMFace> trackingMap;

    boolean threadBusy = false;
    boolean saveImage = false;

    private Thread thread;

    boolean pause = false;
    NetFaceTrack netFaceTrack;
    String ip;
    boolean net = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.face_reco_2);
        setCamera_max_width(-1);

        if (net) {
            initView();
            initCamera();
            showFps(true);
            initView();
            netFaceTrack = NetFaceTrack.getInstance(
                    "", "");
            netFaceTrack.setRetrofit("http://");
        } else {
            initView();
            initCamera();
            showFps(true);
            initView();
        }
    }

    void inputIp() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setCancelable(false);
        final EditText et = new EditText(mContext);
        et.setGravity(Gravity.CENTER);
        et.setHintTextColor(0xffc6c6c6);
        builder.setTitle(R.string.dalog_notice).setView(et)
                .setMessage("请输入本地Ip")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ip = et.getText().toString();
                        if (!StringUtils.isEmpty(ip.trim())) {
                        } else {
                            inputIp();
                            return;
                        }

                        FileUtil.writeFile("/sdcard/rsnet_config.txt", ip);

                        netFaceTrack = NetFaceTrack.getInstance("12345", "abcdefg");
                        netFaceTrack.setRetrofit(ip);
                        initView();
                        initCamera();
                        showFps(true);
                        initView();

                    }
                });
        builder.create().show();
    }

    public void initView() {
        TextView title = (TextView) findViewById(R.id.page_title);
        Button page_right = (Button) findViewById(R.id.page_right);
        title.setText(R.string.start_1);
        page_right.setText(R.string.unlock_insert_face);
        page_right.setVisibility(View.GONE);

    }


    @Override
    protected void drawAnim(List<YMFace> faces, SurfaceView draw_view, float scale_bit, int cameraId, String fps) {
        DrawUtil.drawAnim(faces, draw_view, scale_bit, cameraId, fps, false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (trackingMap != null && trackingMap.size() != 0) {
            trackingMap.clear();
        }
        trackingMap = new SimpleArrayMap<>();
        DrawUtil.updateDataSource();

    }

    int frame = 0;

    @Override
    protected List<YMFace> analyse(final byte[] bytes, final int iw, final int ih) {

        if (pause) return null;
        if (faceTrack == null) return null;
        frame++;
//        final List<YMFace> faces = new ArrayList<>();
//        YMFace face1 = faceTrack.track(bytes, iw, ih);
//        if (face1 != null) faces.add(face1);
        final List<YMFace> faces = faceTrack.trackMulti(bytes, iw, ih);


        if (faces != null && faces.size() > 0) {
            if (!threadBusy && !stop && frame >= 10) {
                frame = 0;

                if (trackingMap.size() > 50) trackingMap.clear();
                //只对最大人脸框进行识别
                int maxIndex = 0;
                for (int i = 1; i < faces.size(); i++) {
                    if (faces.get(maxIndex).getRect()[2] <= faces.get(i).getRect()[2]) {
                        maxIndex = i;
                    }
                }

                final YMFace ymFace = faces.get(maxIndex);
                final int anaIndex = maxIndex;
                final int trackId = ymFace.getTrackId();
                final float[] rect = ymFace.getRect();
                final float[] headposes = ymFace.getHeadpose();

                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            threadBusy = true;
                            final byte[] yuvData = new byte[bytes.length];
                            System.arraycopy(bytes, 0, yuvData, 0, bytes.length);

                            boolean next = true;

                            if ((Math.abs(headposes[0]) > 30
                                    || Math.abs(headposes[1]) > 30
                                    || Math.abs(headposes[2]) > 30)) {
                                //角度不佳不再识别
                                next = false;
                            }
                            int faceQuality = faceTrack.getFaceQuality(anaIndex);

                            if (faceQuality < 85) {
                                //人脸质量不佳，不再识别
                                next = false;
                            }
                            long time = System.currentTimeMillis();
                            int identifyPerson = -11;
                            if (next && !net) {
                                final int trackId = ymFace.getTrackId();
                                if (!trackingMap.containsKey(trackId) ||
                                        trackingMap.get(trackId).getPersonId() <= 0) {


                                    identifyPerson = faceTrack.identifyPerson(anaIndex);
                                    int confidence = faceTrack.getRecognitionConfidence();

                                    saveImageFromCamera(identifyPerson, yuvData);
                                    int gender_confidence = faceTrack.getGenderConfidence(anaIndex);
                                    int gender = faceTrack.getGender(anaIndex);
                                    if (gender_confidence >= 90) {
                                        ymFace.setAge(faceTrack.getAge(anaIndex));
                                        ymFace.setGender(gender);
                                    }
                                    ymFace.setIdentifiedPerson(identifyPerson, confidence);
                                    trackingMap.put(trackId, ymFace);
                                }
                                next = false;
                                //使用本地就不再使用云端,可直接删除云端部分
                            }

                            DLog.d("identify end " + identifyPerson + " time :" + (System.currentTimeMillis() - time)
                                    + "  faceQuality: " + faceQuality
                            );
                            //TODO for 云端api
                            if (next && !pause) {

                                int width_add = (int) (rect[2] / 4);

                                while (rect[0] - width_add < 0 || rect[1] - width_add < 0 ||
                                        rect[0] + rect[2] + width_add > iw ||
                                        rect[1] + rect[3] + width_add > ih) {
                                    width_add--;
                                    if (width_add == 0) break;
                                }

                                String name = null;
                                File bitmapFile = new File("/sdcard/cachebitmap.jpg");
                                Bitmap image = BitmapUtil.getBitmapFromYuvByte(yuvData, iw, ih);

                                Matrix matrix = new Matrix();
                                if ((int) rect[2] + 2 * width_add > 300) {
                                    float bit = 300 / (rect[2] + 2 * width_add);
                                    matrix.postScale(bit, bit);
                                }
                                Bitmap head_bmp = Bitmap.createBitmap(image, (int) rect[0] - width_add, (int) rect[1] - width_add,
                                        (int) rect[2] + 2 * width_add, (int) rect[3] + 2 * width_add, matrix, true);

                                BitmapUtil.saveBitmap(head_bmp, bitmapFile);

                                String result = netFaceTrack.faceDetaction(bitmapFile, "", null);
                                String face_id = "";
                                face_id = new JSONObject(result).getJSONArray("faces")
                                        .getJSONObject(0).getString("face_id");

                                result = netFaceTrack.faceIdentification(face_id, "1860f3add08c6d778b146106033fdcae", null);
                                DLog.d(result);

                                name = new JSONObject(result).
                                        getJSONArray("candidates").getJSONObject(0).getString("name");
                                DLog.d("name = " + name);
                                if (!StringUtils.isEmpty(name)) {
                                    pause = true;
                                    final String last_name = name;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (StringUtils.isEmpty(last_name)) {
                                            } else {
                                                final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                                                builder.setCancelable(false);
                                                builder.setTitle(R.string.dalog_notice)
                                                        .setMessage("你好：" + last_name)
                                                        .setPositiveButton("继续", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                pause = false;
                                                            }
                                                        });
                                                builder.create().show();
                                            }
                                        }
                                    });
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            threadBusy = false;
                        }
                    }
                });
                thread.start();
            }

            for (int i = 0; i < faces.size(); i++) {
                final YMFace ymFace = faces.get(i);
                final int trackId = ymFace.getTrackId();
                if (trackingMap.containsKey(trackId)) {
                    YMFace face = trackingMap.get(trackId);
                    ymFace.setAge(faceTrack.getAge(i));
                    ymFace.setGender(faceTrack.getGender(i));
                    ymFace.setIdentifiedPerson(face.getPersonId(), face.getConfidence());
                }
            }
        }
        return faces;
    }

    public void topClick(View view) {
        switch (view.getId()) {
            case R.id.page_cancle:
                finish();
                break;
            case R.id.page_right://TODO 录入人脸
                stopCamera();
                trackingMap.clear();
                startActivity(new Intent(this, RegisterImageCameraActivity.class));
                break;
        }
    }

    @Override
    protected void onPause() {
        //等待线程结束再执行super中释放检测器
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = null;
        }
        super.onPause();
    }

    public void saveImageFromCamera(int personId, byte[] yuvBytes) {
        if (!saveImage) return;
        File tmpFile = new File("/sdcard/img/fr/out");
        if (!tmpFile.exists()) tmpFile.mkdirs();
        tmpFile = new File("/sdcard/img/fr/out" + "/img_" + System.currentTimeMillis() + "_" + personId + ".jpg");
        saveImage(tmpFile, yuvBytes);
    }

    private void saveImage(File file, byte[] yuvBytes) {

        FileOutputStream fos = null;
        try {
            YuvImage image = new YuvImage(yuvBytes, ImageFormat.NV21, iw, ih, null);
            fos = new FileOutputStream(file);
            image.compressToJpeg(new Rect(0, 0, image.getWidth(), image.getHeight()), 90, fos);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert fos != null;
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
