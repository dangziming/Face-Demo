package readsense.face.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dou.utils.BitmapUtil;
import dou.utils.DLog;
import dou.utils.FileUtil;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;
import readsense.face.base.BaseApplication;
import readsense.face.model.User;
import readsense.face.util.DataSource;

/**
 * Created by dou on 2017/5/16.
 */

public class RecoTest implements Test {
    YMFaceTrack faceTrack;
    private int detect_count = 0;
    private int all_count = 0;
    private int identify_error_count = 0;
    private int identify_count = 0;
    public String test_path = "/sdcard/img/identify/test";
    public String register_path = "/sdcard/img/identify/register";
    public Context context;
    public StringBuffer stringBuffer;
    public StringBuffer errorBuffer;
    private Map<Integer, Integer> resultMap;
    private long all_time = 0;
    private long detect_time = 0;
    private long identify_time = 0;
    private Paint paint;

    private Map<Integer, Integer> keyMap;

    public RecoTest(Context context) {
        this.context = context;
    }

    public void initTest() {
        keyMap = new HashMap<>();
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(2);
        paint.setTextSize(15);
        faceTrack = new YMFaceTrack();

        faceTrack.setDistanceType(YMFaceTrack.DISTANCE_TYPE_FARTHESTER);

        faceTrack.initTrack(BaseApplication.getAppContext()
                .getApplicationContext(), YMFaceTrack.FACE_0, YMFaceTrack.RESIZE_WIDTH_640);
        faceTrack.setRecognitionConfidence(41);
        faceTrack.resetAlbum();

        DataSource dataSource = new DataSource(context);
        dataSource.clearTable();

        detect_count = 0;
        all_count = 0;
        identify_error_count = 0;
        identify_count = 0;
        stringBuffer = new StringBuffer();
        errorBuffer = new StringBuffer();
        resultMap = new HashMap<>();
        all_time = System.currentTimeMillis();

        File out = new File("/sdcard/img/output");
        if (!out.exists()) out.mkdirs();
        else {
            try {
                FileUtil.cleanDirectory(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void startTest() {
        File register_file = new File(register_path);
        if (!register_file.exists()) {
            DLog.d("register path is missing");
            return;
        }
        test(register_file, true);

//        File test_file = new File(test_path);
//        if (!test_file.exists()) {
//            DLog.d("test path is missing");
//            return;
//        }
//        test(test_file, false);
    }

    @Override
    public void finishTest() {
        for (Map.Entry o : resultMap.entrySet()) {
            int key = (int) o.getKey();
            int value = (int) o.getValue();
            stringBuffer.append("found person ").append(key).append(", ").append(value).append(" times\n");
        }

        stringBuffer.append("\nall image count      :  " + all_count + "\n");
        stringBuffer.append("detect_count         :  " + detect_count + "\n");
        stringBuffer.append("identify_count       :  " + identify_count + "\n");
        stringBuffer.append("identify_error_count :  " + identify_error_count + "\n");
        stringBuffer.append("detect prob          :  " + detect_count * 1000 / all_count / 10f + " %\n");
        stringBuffer.append("identify prob        :  " + identify_count * 1000 / detect_count / 10f + " %\n");
        stringBuffer.append("all_time             :  " + (System.currentTimeMillis() - all_time) + " ms\n");
        stringBuffer.append("detect_time          :  " + (detect_time * 10 / all_count / 10f) + " ms\n");
        stringBuffer.append("identify_time          :  " + (identify_time * 10 / detect_count / 10f) + " ms\n");

        stringBuffer.append("\n\nerror result:\n" + errorBuffer.toString());
        FileUtil.writeFile("/sdcard/img/output/out.txt", stringBuffer.toString());

        DLog.d("*********end*********   " + detect_count + " / " + detect_count * 1000 / all_count / 10f + "%|"
                + identify_count + " / " + identify_count * 1000 / detect_count / 10f + "%|"
                + identify_error_count + "|" + (detect_time * 10 / all_count / 10f) + " / " + (identify_time * 10 / detect_count / 10f));
    }

    private void test(File test_file, boolean register) {
        if (register) {
            File[] fileNames = test_file.listFiles();
            for (int i = 0; i < fileNames.length; i++) {
                //数个文件夹
                File dic = fileNames[i];
                if (dic.isDirectory()) {
                    File[] images = dic.listFiles();
                    boolean isAdd = false;
                    int personId = -1;
                    for (int j = 0; j < images.length; j++) {

                        File image = images[j];
                        if (image.getName().contains(".JPG") || image.getName().contains(".jpg")
                                || image.getName().contains(".png") || image.getName().contains(".jpeg")
                                || image.getName().contains(".bmp")) {
                            if (j == 0 || !isAdd) {
                                personId = addPerson(image);
                                if (personId > 0) {
                                    isAdd = true;
                                    User user = new User("" + personId, " " + personId, "1", "1");
                                    user.setScore("1");
                                    DataSource dataSource = new DataSource(context);
                                    dataSource.insert(user);
                                }
                            } else {
                                updatePerson(image, personId);
                            }
                        }
                    }

                    keyMap.put(Integer.parseInt(dic.getName()), personId);

                    DLog.d(dic.getName() + "  personId: " + personId +
                            " faceCountbyId: " + faceTrack.getFaceCountByPersonId(personId) +
                            "  all_size : " + faceTrack.getAlbumSize());
                }
            }
        } else {

            File[] fileNames = test_file.listFiles();
            for (int i = 0; i < fileNames.length; i++) {
                //数个文件夹
                File dic = fileNames[i];
                if (dic.isDirectory()) {
                    int dic_name = Integer.parseInt(dic.getName());
                    File[] images = dic.listFiles();
                    for (int j = 0; j < images.length; j++) {

//                        images[j].renameTo(new File(test_path+"/"+dic.getName()+"_"+(j+1)+".jpg"));
                        File image = images[j];
//                        File image = new File("/sdcard/img/identify/test/21/IMG_3568.JPG");
                        int personId = -1;
                        if (image.getName().contains(".JPG") || image.getName().contains(".jpg") ||
                                image.getName().contains(".png") || image.getName().contains(".jpeg")
                                || image.getName().contains(".bmp")) {
                            all_count++;
                            if (detect(image)) {
                                detect_count++;
                                long time = System.currentTimeMillis();
                                personId = faceTrack.identifyPerson(0);

                                identify_time += (System.currentTimeMillis() - time);

                                int recognitionConfidence = faceTrack.getRecognitionConfidence();

//                                StringBuffer save2 = new StringBuffer();
//                                final float[] rects = curr.getRect();
//                                final float[] landmarks = curr.getLandmarks();
//                                float[] feature1 = faceTrack.getFaceFeature(0);

//                                for (int g = 0; g < rects.length; g++) {
//                                    save2.append(rects[g] + " ");
//                                }
//                                for (int g = 0; g < landmarks.length; g++) {
//                                    save2.append(landmarks[g] + " ");
//                                }
//                                for (int g = 0; g < feature1.length; g++) {
//                                    save2.append(feature1[g] + " ");
//                                }
//                                FileUtil.writeFile("/sdcard/img/output/" + image.getName() + ".txt", save2.toString().trim());


                                DLog.d("image: " + image.getName() + "  dic_name " + dic_name + "  personId: " + personId + " con:" + recognitionConfidence);

//                                if (personId > 0) {
//                                    identify_error_count++;
//
//                                    DrawUtil.getNameFromPersonId(personId);
//
//                                    errorBuffer.append(image.getAbsolutePath() + " : " + DrawUtil.getNameFromPersonId(personId) +
//                                            " : " + recognitionConfidence + "\n");
////                                    drawAndSave(image.getName());
//                                }

                                if (personId > 0) {
                                    if (keyMap.containsKey(dic_name) && keyMap.get(dic_name) != personId) {
                                        identify_error_count++;
                                        errorBuffer.append(image.getName() + ":" + keyMap.get(dic_name) + ":" + personId + ":" + recognitionConfidence + "\n");
//                                        drawAndSave(image.getName());
                                    } else {
                                        identify_count++;
                                        int count = 1;
                                        if (resultMap.containsKey(personId)) {
                                            count += resultMap.get(personId);
                                        }
                                        resultMap.put(personId, count);
                                    }
                                } else {
                                    errorBuffer.append(image.getName() + ":" + dic_name + ":" + personId + ":" + recognitionConfidence + "\n");
//                                    drawAndSave(image.getName());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void updatePerson(File image, int personId) {
        if (detect(image)) {
            int updateResult = faceTrack.updatePerson(personId, 0);
            DLog.d(image.getName() + " update : " + updateResult + " con ：" + faceTrack.getRecognitionConfidence());
        }
    }

    private int addPerson(File image) {
        if (detect(image)) {
            int personId = faceTrack.addPerson(0);
            DLog.d(image.getName() + " add : " + personId + " con ：" + faceTrack.getRecognitionConfidence());
            return personId;
        }
        return -1;
    }

    int basicPointIndex[] = {7, 10, 12, 16, 18};

    YMFace curr = null;
    Bitmap targetBitmap;

    private boolean detect(File image) {
        if (targetBitmap != null && !targetBitmap.isRecycled()) targetBitmap.recycle();
        targetBitmap = BitmapUtil.decodeScaleImage(image.getAbsolutePath(), 1000, 1000);
        long time = System.currentTimeMillis();
        List<YMFace> ymFaces = faceTrack.detectMultiBitmap(targetBitmap);


        detect_time += (System.currentTimeMillis() - time);

        if (ymFaces != null && ymFaces.size() != 0) {
            curr = ymFaces.get(0);
            return true;
        }
        return false;
    }

    void drawAndSave(String currName) {
        Bitmap current = Bitmap.createBitmap(targetBitmap.getWidth(), targetBitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(current);
        canvas.drawBitmap(targetBitmap, 0, 0, new Paint());

        float[] landmarks = curr.getLandmarks();

        for (int k = 0; k < landmarks.length / 2; k++) {
            canvas.drawPoint(landmarks[2 * k], landmarks[2 * k + 1], paint);
        }

        float[] rect = curr.getRect();
        paint.setStyle(Paint.Style.STROKE);
        RectF rectF = new RectF(rect[0], rect[1], rect[0] + rect[2], rect[1] + rect[3]);
        canvas.drawRect(rectF, paint);

        BitmapUtil.saveBitmap(current, "/sdcard/img/output/" + currName);
    }
}
