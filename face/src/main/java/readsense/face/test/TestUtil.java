package readsense.face.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import dou.utils.BitmapUtil;
import dou.utils.DLog;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

import static junit.framework.Assert.assertTrue;

/**
 * Created by dou on 2018/3/6.
 */

public class TestUtil {

    public static YMFaceTrack getFaceTrack(Context context, String db_path, int confidence)

    {
        DLog.time("getFaceTrack");
        YMFaceTrack facetrack = new YMFaceTrack();
        facetrack.setDistanceType(YMFaceTrack.DISTANCE_TYPE_FARTHESTER);
        int result = facetrack.initTrack(context, 0, 0, db_path);

        assertTrue(result == 0);

        facetrack.setRecognitionConfidence(confidence);
        DLog.time("getFaceTrack");
        return facetrack;
    }

    public static List<YMFace> detect(Bitmap bitmap, YMFaceTrack faceTrack) {
        DLog.time("detect");
        List<YMFace> faces = faceTrack.detectMultiBitmap(bitmap);
        DLog.time("detect");
        return faces;
    }


    public static List<YMFace> detect(String image_path, YMFaceTrack faceTrack) {
        DLog.time("get bitmap from path ");
        final Bitmap bitmap = BitmapUtil.decodeScaleImage(image_path, 1000, 1000);
        DLog.time("get bitmap from path");
        return detect(bitmap, faceTrack);
    }


    public static List<YMFace> detect(Context context, int res, YMFaceTrack faceTrack) {
        DLog.time("get bitmap from res ");
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), res);
        DLog.time("get bitmap from res ");
        return detect(bitmap, faceTrack);
    }


    public static List<YMFace> track(Bitmap bitmap, YMFaceTrack faceTrack) {
        DLog.time("track");
        List<YMFace> faces = faceTrack.trackMulti(bitmap);
        DLog.time("track");
        return faces;
    }

    public static List<YMFace> track(String image_path, YMFaceTrack faceTrack) {

        DLog.time("get bitmap from path ");
        final Bitmap bitmap = BitmapUtil.decodeScaleImage(image_path, 1000, 1000);
        DLog.time("get bitmap from path");

        return track(bitmap, faceTrack);
    }

    public static List<YMFace> track(Context context, int res, YMFaceTrack faceTrack) {
        DLog.time("get bitmap from res ");
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), res);
        DLog.time("get bitmap from res ");
        return track(bitmap, faceTrack);
    }

    public static void compareImage(YMFaceTrack faceTrack, String path1, String path2) {

        File file1 = new File(path1);
        File file2 = new File(path2);

        List<YMFace> faces = faceTrack.detectMultiBitmap(BitmapUtil.decodeScaleImage(path1, 1000, 1000));

        float[] feature1, feature2;
        if (faces != null && faces.size() == 1) {
            feature1 = faceTrack.getFaceFeature(0);
        } else {
            DLog.d("image1 input error");
            return;
        }

        faces = faceTrack.detectMultiBitmap(BitmapUtil.decodeScaleImage(path2, 1000, 1000));

        if (faces != null && faces.size() == 1) {
            feature2 = faceTrack.getFaceFeature(0);
        } else {
            DLog.d("image2 input error");
            return;
        }

        float confidence = faceTrack.compareFaceFeatureMix(feature1, feature2);

        DLog.d("end :" + file1.getName() + " compare " + file2.getName() + " = " + confidence);
    }

    public static void compareFeatureImage(YMFaceTrack faceTrack, float[] feature1, String path2) {

        File file2 = new File(path2);

        List<YMFace> faces = faceTrack.detectMultiBitmap(BitmapUtil.decodeScaleImage(path2, 1000, 1000));

        float[] feature2;
        if (faces != null && faces.size() == 1) {
            feature2 = faceTrack.getFaceFeature(0);
        } else {
            DLog.d("image1 input error");
            return;
        }

        float confidence = faceTrack.compareFaceFeatureMix(feature1, feature2);

        DLog.d("end : compare " + file2.getName() + " = " + confidence);
    }

    public static void capture(Bitmap bitmap, YMFace face) {

//        List<YMFace> faces = faceTrack.detectMultiBitmap(bitmap);
//
//        if(faces!=null&&faces.size()>0){

//            YMFace face = faces.get(0);
        float[] rect = face.getRect();
        final float[] landmarks = face.getLandmarks();
        float new_landmarks[] = new float[landmarks.length];

        //外扩宽度到1/2
        int width_add = (int) (rect[2] / 2);
        //计算以避免扩到边界
        while (rect[0] - width_add < 0 ||
                rect[1] - width_add < 0 ||
                rect[0] + rect[2] + width_add > bitmap.getWidth() ||
                rect[1] + rect[3] + width_add > bitmap.getHeight()) {
            width_add--;
        }

        //基于原图到新框
        int dst[] = new int[]{(int) (rect[0] - width_add), (int) (rect[1] - width_add),
                (int) (rect[2] + 2 * width_add), (int) (rect[3] + 2 * width_add)};

        //new bitmap
        final Bitmap capture_bitmap = Bitmap.createBitmap(bitmap, dst[0], dst[1], dst[2], dst[3]);

        saveBitmap(capture_bitmap, new File("/sdcard/capture.jpg"));
        //new landmarks
        //基于新框的landmarks

        for (int i = 0; i < landmarks.length / 2; i++) {
            new_landmarks[2 * i] = landmarks[2 * i] - dst[0];
            new_landmarks[2 * i + 1] = landmarks[2 * i + 1] - dst[1];
        }

        if (capture_bitmap.getWidth() > 300) {
            //压缩bitmap
            final float scale_bit = 300f / (float) capture_bitmap.getWidth();
            Matrix matrix = new Matrix();
            matrix.postScale(scale_bit, scale_bit);
            //输出压缩后的bitmap
            Bitmap out = Bitmap.createBitmap(capture_bitmap, 0, 0,
                    capture_bitmap.getWidth(), capture_bitmap.getHeight(), matrix, true);

            //归一化关键点
            for (int i = 0; i < new_landmarks.length; i++) {
                new_landmarks[i] = new_landmarks[i] * scale_bit;
            }

        }

    }


    public static boolean saveBitmap(Bitmap bitmap, File file) {
        if (bitmap == null) {
            return false;
        } else {
            FileOutputStream fos = null;

            try {
                fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                boolean var3 = true;
                return var3;
            } catch (Exception var13) {
                var13.printStackTrace();
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException var12) {
                        var12.printStackTrace();
                    }
                }

            }

            return false;
        }
    }

}
