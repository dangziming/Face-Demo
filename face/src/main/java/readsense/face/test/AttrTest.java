package readsense.face.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.io.File;
import java.util.List;

import dou.utils.BitmapUtil;
import dou.utils.DLog;
import dou.utils.FileUtil;
import dou.utils.StringUtils;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

/**
 * Created by mac on 16/8/2.
 */
public class AttrTest implements Test {


    public String test_path = "/sdcard/img/test";
    public String dir_path = "/sdcard/img";
    public String out_path = "/sdcard/img/output";
    public Context context;
    private YMFaceTrack faceTrack;
    private Paint paint;

    public AttrTest(Context context) {

        this.context = context;
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(5);
        paint.setTextSize(15);
    }

    StringBuffer stringBuffer = new StringBuffer();

    @Override
    public void initTest() {
        faceTrack = new YMFaceTrack();
        faceTrack.setDistanceType(YMFaceTrack.DISTANCE_TYPE_FARTHESTER);
        faceTrack.initTrack(context, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640, dir_path);
        faceTrack.setRecognitionConfidence(75);
    }

    public void plistFile(File file) {
        File[] files = file.listFiles();

        for (int i = 0; i < files.length; i++) {
            File file1 = files[i];
            if (file1.isDirectory()) plistFile(file1);
            else {
                File curr = files[i];
                String currName = curr.getName();
                DLog.d("current name = " + currName + "  " + i + ":" + files.length);
                if (currName.contains(".jpg") || currName.contains(".png") || currName.contains(".jpeg") || currName.contains(".JPG")) {
                    Bitmap targetBitmap = BitmapUtil.decodeScaleImage(curr.getAbsolutePath(), 800, 800);
                    List<YMFace> ymFaces = faceTrack.detectMultiBitmap(targetBitmap);
                    if (ymFaces != null && ymFaces.size() != 0) {
                        DLog.d("detect img");

                        Bitmap current = Bitmap.createBitmap(targetBitmap.getWidth(), targetBitmap.getHeight(), Bitmap.Config.RGB_565);
                        Canvas canvas = new Canvas(current);
                        canvas.drawBitmap(targetBitmap, 0, 0, new Paint());


                        for (int j = 0; j < ymFaces.size(); j++) {

                            float[] rect = ymFaces.get(j).getRect();
                            paint.setStyle(Paint.Style.STROKE);
                            RectF rectF = new RectF(rect[0], rect[1], rect[0] + rect[2], rect[1] + rect[3]);
                            canvas.drawRect(rectF, paint);

                            paint.setStyle(Paint.Style.FILL);
                            float[] landmarks = ymFaces.get(j).getLandmarks();
                            float[] faceFeature = faceTrack.getFaceFeature(j);
                            stringBuffer.append(curr.getAbsolutePath() + " ");
                            for (float v : rect) {
                                stringBuffer.append(v + " ");
                            }

                            for (float v : faceFeature) {
                                stringBuffer.append(v + " ");
                            }
                            stringBuffer.append("\n");
                            for (int k = 0; k < landmarks.length / 2; k++) {
                                canvas.drawPoint(landmarks[2 * k], landmarks[2 * k + 1], paint);
                            }
                        }
                        BitmapUtil.saveBitmap(current, out_path + "/" + currName);
                    } else {
                        DLog.d("not detect");
                    }
                    targetBitmap.recycle();
                }
            }
        }
    }

    @Override
    public void startTest() {
        if (!StringUtils.isEmpty(test_path)) {
            plistFile(new File(test_path));

            FileUtil.writeFile(out_path + "/out.txt", stringBuffer.toString());
        } else {
            DLog.d("test", "path is null");
        }
    }

    @Override
    public void finishTest() {
        faceTrack.onRelease();
    }


}
