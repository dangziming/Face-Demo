package readsense.face.test;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.util.List;

import dou.utils.BitmapUtil;
import dou.utils.DLog;
import dou.utils.FileUtil;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;
import readsense.face.base.SenseConfig;

/**
 * Created by dou on 2017/11/27.
 */

public class NomalTest {

    public static void test_compare_image(Context context, String path1, String path2) {

        StringBuffer save1 = new StringBuffer();
        StringBuffer save2 = new StringBuffer();
        // 存储比对结果
        StringBuffer save3 = new StringBuffer();
        File file1 = new File(path1);
        File file2 = new File(path2);

        YMFaceTrack faceTrack = new YMFaceTrack();
        faceTrack.setDistanceType(YMFaceTrack.DISTANCE_TYPE_FARTHESTER);
        faceTrack.initTrack(context, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640,
                SenseConfig.appid, SenseConfig.appsecret);

        List<YMFace> faces = faceTrack.detectMultiBitmap(BitmapUtil.decodeScaleImage(path1, 1000, 1000));

        float[] feature1, feature2;
        if (faces != null && faces.size() == 1) {
            feature1 = faceTrack.getFaceFeature(0);
            final float[] rects = faces.get(0).getRect();
            final float[] landmarks = faces.get(0).getLandmarks();

            for (int i = 0; i < rects.length; i++) {
                save1.append(rects[i] + "\n");
            }
            for (int i = 0; i < landmarks.length; i++) {
                save1.append(landmarks[i] + "\n");
            }
            for (int i = 0; i < feature1.length; i++) {
                save1.append(feature1[i] + "\n");
            }
            FileUtil.writeFile("/sdcard/img/output/" + file1.getName() + ".txt", save1.toString().trim());
        } else {
            DLog.d("image1 input error");
            return;
        }

        faces = faceTrack.detectMultiBitmap(BitmapUtil.decodeScaleImage(path2, 1000, 1000));

        if (faces != null && faces.size() == 1) {
            feature2 = faceTrack.getFaceFeature(0);

            final float[] rects = faces.get(0).getRect();
            final float[] landmarks = faces.get(0).getLandmarks();

            for (int i = 0; i < rects.length; i++) {
                save2.append(rects[i] + "\n");
            }
            for (int i = 0; i < landmarks.length; i++) {
                save2.append(landmarks[i] + "\n");
            }
            for (int i = 0; i < feature1.length; i++) {
                save2.append(feature1[i] + "\n");
            }
            FileUtil.writeFile("/sdcard/img/output/" + file2.getName() + ".txt", save2.toString().trim());
        } else {
            DLog.d("image1 input error");
            return;
        }

        float confidence = faceTrack.compareFaceFeatureMix(feature1, feature2);

        save3.append("end :" + file1.getPath() + " compare " + file2.getPath() + " = " + confidence + "\n");
        FileUtil.writeFile("/sdcard/img/output/output.txt", save3.toString().trim(), true);


        //人证93 ， 大模型86 , 小模型 89， ，测试环境： mi5 arm32
        //人证82-92 ， 大模型81-85 , 小模型 79-86， ，测试环境： mi5 arm32
        //人证84-93 ， 大模型83-86 , 小模型 81-88，测试环境： mi5 arm64

        DLog.d("end :" + file1.getName() + " compare " + file2.getName() + " = " + confidence);

    }


    public static void test_compare_file(Context context, String path1, String path2) {

        String feature_str1 = FileUtil.readFile(path1, "utf-8").toString().trim();
        String feature_str2 = FileUtil.readFile(path2, "utf-8").toString().trim();

        String[] split1 = feature_str1.split(" ");
        String[] split2 = feature_str2.split(" ");

        float[] feature1 = new float[split1.length];
        float[] feature2 = new float[split1.length];
        for (int i = 0; i < split1.length; i++) {
            feature1[i] = Float.parseFloat(split1[i]);
            feature2[i] = Float.parseFloat(split2[i]);
        }

        YMFaceTrack faceTrack = new YMFaceTrack();
        faceTrack.setDistanceType(YMFaceTrack.DISTANCE_TYPE_FARTHESTER);
        faceTrack.initTrack(context, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640);

        float confidence = faceTrack.compareFaceFeatureMix(feature1, feature2);

        DLog.d("end : compare confidence = " + confidence);

        faceTrack.onRelease();
    }


    public static void test_gender(Context context, String female_path, String male_path) {

        YMFaceTrack faceTrack = new YMFaceTrack();
        faceTrack.setDistanceType(YMFaceTrack.DISTANCE_TYPE_FARTHESTER);
        faceTrack.initTrack(context, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640);

        File[] femaleFiles = new File(female_path).listFiles();

        int femaleCount = 0;

        for (File femaleFile : femaleFiles) {
            Bitmap tar = BitmapUtil.decodeScaleImage(femaleFile.getAbsolutePath(), 1000, 1000);
            List<YMFace> faces = faceTrack.detectMultiBitmap(tar);

            if (faces != null && faces.size() > 0) {
                faceTrack.initFaceAttr(0, faces);
                if (faces.get(0).getGender() == 0) femaleCount++;
            }
        }
        DLog.d("all female: " + femaleFiles.length + " ,get male: " + femaleCount);

        File[] maleFiles = new File(male_path).listFiles();
        int maleCount = 0;

        for (File maleFile : maleFiles) {

            Bitmap tar = BitmapUtil.decodeScaleImage(maleFile.getAbsolutePath(), 1000, 1000);
            List<YMFace> faces = faceTrack.detectMultiBitmap(tar);

            if (faces != null && faces.size() > 0) {
                faceTrack.initFaceAttr(0, faces);
                if (faces.get(0).getGender() == 1) maleCount++;
            }
        }
        DLog.d("all male: " + maleFiles.length + " ,get male: " + maleCount);
        faceTrack.onRelease();
    }

    public static void test_face_quality(Context context, String image) {
        YMFaceTrack faceTrack = new YMFaceTrack();
        faceTrack.setDistanceType(YMFaceTrack.DISTANCE_TYPE_NEAR);
        faceTrack.initTrack(context, YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640);
        List<YMFace> faces = faceTrack.detectMultiBitmap(BitmapUtil.decodeScaleImage(image, 1000, 1000));

        if (faces != null && faces.size() >= 0) {
            int faceQuality = faceTrack.getFaceQuality(0);
            DLog.d(image + " : " + faceQuality);
        }
    }


}
