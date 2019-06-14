package readsense.face.test;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.File;
import java.util.List;

import dou.utils.BitmapUtil;
import dou.utils.DLog;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

/**
 * Created by mac on 16/7/28.
 */
public class FaceTest {

    //要测试的图片目录
    public String test_path = "/sdcard/img/test";
    //数据库目录
    public String db_path = "/sdcard/img";


    public void initTest() {
        faceTrack = new YMFaceTrack();
        faceTrack.setDistanceType(YMFaceTrack.DISTANCE_TYPE_FARTHESTER);
        faceTrack.initTrack(context, YMFaceTrack.FACE_270, YMFaceTrack.RESIZE_WIDTH_640, db_path);
        faceTrack.setRecognitionConfidence(75);
    }

    public Context context;
    private YMFaceTrack faceTrack;

    public FaceTest(Context context) {
        this.context = context;
    }


    public void startTest() {
        File test_file = new File(test_path);
        if (!test_file.exists()) {
            DLog.d("model path is missing");
            return;
        }
        plistFile(test_file);
    }

    private void plistFile(File file) {
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file1 = files[i];
            if (file1.isDirectory()) plistFile(file1);
            else {
                File curr = files[i];
                String currName = curr.getName();
                DLog.d("current name = " + currName + "  " + i + " : " + files.length);
                if (currName.contains(".jpg") || currName.contains(".png") || currName.contains(".jpeg")) {
                    Bitmap targetBitmap = BitmapUtil.decodeScaleImage(curr.getAbsolutePath(), 1000, 1000);

                    List<YMFace> ymFaces = faceTrack.detectMultiBitmap(targetBitmap);

                    if (ymFaces != null && ymFaces.size() != 0) {

                        for (int j = 0; j < ymFaces.size(); j++) {

                            int identifyPerson = faceTrack.identifyPerson(j);
                            int confidence = faceTrack.getRecognitionConfidence();

                            DLog.d("identify end " + identifyPerson + " con = " + confidence);
                        }

                    } else {
                        DLog.d("not detect");
                    }
                }
            }
        }
    }
}
