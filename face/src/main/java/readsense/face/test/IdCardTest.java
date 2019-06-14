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
import readsense.face.base.BaseApplication;

/**
 * Created by dou on 2017/7/14.
 */

public class IdCardTest implements Test {

    public String test_path = "/sdcard/img/test_idcard/test_";
    public String register_path = "/sdcard/img/test_idcard/register";
    YMFaceTrack faceTrack;
    private Context context;
    StringBuffer stringBuffer;

    public IdCardTest(Context context) {
        this.context = context;
    }

    @Override
    public void initTest() {
        faceTrack = new YMFaceTrack();
        faceTrack.initTrack(BaseApplication.getAppContext()
                .getApplicationContext(), YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640);
        faceTrack.setRecognitionConfidence(10);
        faceTrack.resetAlbum();
        stringBuffer = new StringBuffer();
    }

    @Override
    public void startTest() {
        for (int i = 0; i < 429; i++) {
            faceTrack.resetAlbum();
            String extra_String = "";
            if (String.valueOf(i).length() == 1) extra_String = "00";
            if (String.valueOf(i).length() == 2) extra_String = "0";

            File register_File = new File(register_path + "/" + extra_String + i);
            File[] files = register_File.listFiles();
            if (files == null) continue;
            for (int j = 0; j < files.length; j++) {
                File image = files[j];
                if (image.getName().contains(".JPG") || image.getName().contains(".jpg") ||
                        image.getName().contains(".png") || image.getName().contains(".jpeg")
                        || image.getName().contains(".bmp")) {
                    int personId = addPerson(image);
                    DLog.d(i + "  personId: " + personId +
                            " faceCountbyId: " + faceTrack.getFaceCountByPersonId(personId) +
                            "  all_size : " + faceTrack.getAlbumSize());
                }
            }
            File test_File = new File(test_path + "/" + extra_String + i);
            files = test_File.listFiles();
            if (files == null) continue;
            for (int j = 0; j < files.length; j++) {
                File image = files[j];
                if (image.getName().contains(".JPG") || image.getName().contains(".jpg") ||
                        image.getName().contains(".png") || image.getName().contains(".jpeg")
                        || image.getName().contains(".bmp")) {

                    if (detect(image)) {
                        faceTrack.identifyPerson(0);
                        int recognitionConfidence = faceTrack.getRecognitionConfidence();

                        if (recognitionConfidence >= 60) count++;
                        stringBuffer.append(i + " : " + image.getName() + "  con: " + recognitionConfidence + "  success :" + count + "\n");
                        DLog.d(i + " : " + image.getName() + " : " + recognitionConfidence + " : success :" + count);
                    }
                }
            }
        }
    }

    int count = 0;

    @Override
    public void finishTest() {
        FileUtil.writeFile("/sdcard/img/output/out.txt", stringBuffer.toString());
    }

    private int addPerson(File image) {
        if (detect(image)) {
            int personId = faceTrack.addPerson(0);
            DLog.d(image.getName() + " add : " + personId + " con ：" + faceTrack.getRecognitionConfidence());
            return personId;
        }
        return -1;
    }

    private boolean detect(File image) {
        Bitmap bitmap = BitmapUtil.decodeScaleImage(image.getAbsolutePath(), 1000, 1000);
        long time = System.currentTimeMillis();
        List<YMFace> ymFaces = faceTrack.detectMultiBitmap(bitmap);
        if (ymFaces != null && ymFaces.size() != 0) {
            return true;
        }
        return false;
    }
}
