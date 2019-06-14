package readsense.face.test;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import dou.utils.BitmapUtil;
import dou.utils.DLog;
import dou.utils.FileUtil;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;
import readsense.face.base.BaseApplication;

/**
 * Created by dou on 2018/1/29.
 */

public class CompareTest {

    static class FeatureMsg {
        String path;
        float[] feature;

        public FeatureMsg(String path, float[] feature) {
            this.path = path;
            this.feature = feature;
        }
    }

    static class OutIdCardMsg {
        String msg;
        int confidence;

        public OutIdCardMsg(String msg, int confidence) {
            this.msg = msg;
            this.confidence = confidence;
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void startRecoTest(boolean isIdcard) {
        String test_path = "/sdcard/test/ID_test2";

        if(!isIdcard)
            test_path = "/sdcard/test/pics_1k-5ID";

        YMFaceTrack faceTrack = new YMFaceTrack();
        final int initTrack = faceTrack.initTrack(BaseApplication.getAppContext(), 0, 0);

        if (initTrack != 0) {
            DLog.d("error init : " + initTrack);
            return;
        }
        int count = 0;
        frame = 0;

        frame_get_bitmap_time = 0;
        frame_detect_time = 0;
        frame_getfeature_time = 0;
        int featureSize = 512;

        File file = new File("/sdcard/test/output");
        if (file.exists()) try {
            FileUtil.cleanDirectory(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!file.exists())
            file.mkdirs();

        File testPath_file = new File(test_path);
        File[] testPath_files = testPath_file.listFiles();
        //get all feature

        File featurePath = new File("/sdcard/test/output/feature.txt");
        Map<String, float[]> featureMap = new HashMap<>();
        if (featurePath.exists()) {
            try {
                BufferedReader e = new BufferedReader(new FileReader("/sdcard/test/output/feature.txt"), 8192);
                String line = null;

                while ((line = e.readLine()) != null) {
                    String[] split = line.trim().split(" ");
                    float[] feature = new float[featureSize];
                    for (int i = 0; i < split.length - 1; i++) {
                        feature[i] = Float.parseFloat(split[i + 1]);
                    }

                    featureMap.put(new File(split[0]).getName(), feature);
                }

                e.close();
            } catch (IOException var4) {
                var4.printStackTrace();
            }
        }

        Map<String, Map<String, FeatureMsg>> allFileList = new HashMap();
        count = 0;
        for (File item : testPath_files) {
            String itemName = item.getName();
            String name_split[] = itemName.split("_");
            count++;
            DLog.d("get feature count: " + count + "  " + itemName);
            if (itemName.endsWith(".jpg") && name_split.length == 2) {
                String identify = name_split[0];
                String iden_count = name_split[1].substring(0, name_split[1].length() - 4);
                float feature_idcard[] = null;
                if (featureMap.containsKey(item.getName())) {
                    feature_idcard = featureMap.get(item.getName());
                } else {
                    feature_idcard = getFeature(item.getAbsolutePath(), faceTrack, isIdcard);
                }

                if (feature_idcard != null) {
                    if (allFileList.containsKey(identify)) {
                        allFileList.get(identify).put(iden_count, new FeatureMsg(item.getAbsolutePath(), feature_idcard));
                    } else {
                        Map<String, FeatureMsg> item_map = new HashMap<>();
                        item_map.put(iden_count, new FeatureMsg(item.getAbsolutePath(), feature_idcard));
                        allFileList.put(identify, item_map);
                    }
                }
            }
        }

        FileUtil.writeFile("/sdcard/test/output/time.txt",
                "\nframe: " + frame +
                        "\nframe_get_bitmap_time: " + (frame_get_bitmap_time / (float) frame) +
                        "\nframe_detect_time: " + (frame_detect_time / (float) frame) +
                        "\nframe_getfeature_time: " + (frame_getfeature_time / (float) frame)
                , true);

        count = 0;
        //save feature
        StringBuffer featureBuffer = new StringBuffer();
        for (Map.Entry<String, Map<String, FeatureMsg>> entry : allFileList.entrySet()) {
            Map<String, FeatureMsg> itemMap = entry.getValue();
            for (Map.Entry<String, FeatureMsg> itemEntry : itemMap.entrySet()) {
                FeatureMsg value = itemEntry.getValue();

                featureBuffer.append(value.path).append(" ");
                for (int i = 0; i < featureSize; i++) {
                    featureBuffer.append(value.feature[i]).append(" ");
                }
                featureBuffer.append("\n");
            }
            count++;

            if (count > 50) {
                FileUtil.writeFile("/sdcard/test/output/feature.txt", featureBuffer.toString(), true);
                featureBuffer = new StringBuffer();
            }
        }
        FileUtil.writeFile("/sdcard/test/output/feature.txt", featureBuffer.toString(), true);
        //compare
        count = 0;
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Map<String, FeatureMsg>> entry : allFileList.entrySet()) {
            Map<String, FeatureMsg> itemMap = entry.getValue();


            if (isIdcard) {

                //TODO idcard
                if (itemMap.containsKey("1")) {
                    FeatureMsg featureMsgCard = itemMap.get("1");
                    float feature_idcard[] = featureMsgCard.feature;
                    if (feature_idcard != null) {
                        for (Map.Entry<String, FeatureMsg> itemEntry : itemMap.entrySet()) {
                            if (!Objects.equals(itemEntry.getKey(), "1")) {
                                FeatureMsg featureLive = itemEntry.getValue();
                                float feature_live[] = featureLive.feature;
                                if (feature_live != null) {
                                    float confidence = faceTrack.compareFaceFeatureMix(feature_idcard, feature_live);
                                    count++;
                                    sb.append(featureMsgCard.path + " " + featureLive.path + " " + confidence + "\n");
                                }

                                if (count % 1000 == 0) {
                                    DLog.d("now compare 1-1 count: " + count);

                                    FileUtil.writeFile("/sdcard/test/output/1_1_out.txt", sb.toString(), true);

                                    sb = new StringBuilder();
                                }
                            }
                        }
                    }
                }
            } else {
                //TODO face
                for (Map.Entry<String, FeatureMsg> itemEntry : itemMap.entrySet()) {
                    for (Map.Entry<String, FeatureMsg> itemEntry1 : itemMap.entrySet()) {
                        if (Objects.equals(itemEntry.getKey(), itemEntry1.getKey())) continue;

                        float feature1[] = itemEntry.getValue().feature;
                        float feature2[] = itemEntry1.getValue().feature;
                        if (feature1 != null || feature2 != null) {
                            float confidence = faceTrack.compareFaceFeatureMix(feature1, feature2);
                            count++;
                            sb.append(itemEntry.getValue().path + " " + itemEntry1.getValue().path + " " + confidence + "\n");
                        }

                        if (count % 1000 == 0) {
                            DLog.d("now compare 1-1 count: " + count);
                            FileUtil.writeFile("/sdcard/test/output/1_1_out.txt", sb.toString(), true);
                            sb = new StringBuilder();
                        }

                    }
                }

            }
        }
        DLog.d("now compare 1-1 count: " + count);

        FileUtil.writeFile("/sdcard/test/output/1_1_out.txt", sb.toString(), true);

        count = 0;

        StringBuffer sbN = new StringBuffer();
        for (Map.Entry<String, Map<String, FeatureMsg>> entry : allFileList.entrySet()) {

            for (Map.Entry<String, Map<String, FeatureMsg>> entry_N : allFileList.entrySet()) {
                if (Objects.equals(entry_N.getKey(), entry.getKey())) continue;//同组不比较


                final Map<String, FeatureMsg> person_map_1 = entry_N.getValue();
                final Map<String, FeatureMsg> person_map_2 = entry.getValue();


                for (Map.Entry<String, FeatureMsg> itemEntry1 : person_map_1.entrySet()) {
                    for (Map.Entry<String, FeatureMsg> itemEntry2 : person_map_2.entrySet()) {

                        if (isIdcard) {//非人证百无禁忌
                            //身份证与身份证不需要比较
                            if (itemEntry1.getKey().equals("1") && itemEntry2.getKey().equals("1"))
                                continue;

                            //无身份证不需要比较
                            if (!itemEntry1.getKey().equals("1") && !itemEntry2.getKey().equals("1"))
                                continue;
                        }

                        float feature1[] = itemEntry1.getValue().feature;
                        float feature2[] = itemEntry2.getValue().feature;
                        if (feature1 != null || feature2 != null) {
                            float confidence = faceTrack.compareFaceFeatureMix(feature1, feature2);
                            count++;
                            sbN.append(itemEntry1.getValue().path + " " + itemEntry2.getValue().path + " " + confidence + "\n");
                        }
                        if (count % 10000 == 0) {
                            DLog.d("now compare 1-n count: " + count);
                            FileUtil.writeFile("/sdcard/test/output/1_n_out.txt", sbN.toString(), true);
                            sbN = new StringBuffer();
                        }
                    }
                }
            }
        }

        DLog.d("now compare 1-n count: " + count);
        FileUtil.writeFile("/sdcard/test/output/1_n_out.txt", sbN.toString(), true);
        DLog.d("end********");
    }

    static int frame = 0;
    static long frame_get_bitmap_time = 0;
    static long frame_detect_time = 0;
    static long frame_getfeature_time = 0;

    static float[] getFeature(String path, YMFaceTrack faceTrack, boolean isIdcard) {
        long get_bitmap_time = System.currentTimeMillis();
        DLog.time("get bitmap ");
        Bitmap idcard_bitmap = BitmapUtil.decodeScaleImage(path, 1000, 1000);
        get_bitmap_time = System.currentTimeMillis() - get_bitmap_time;
        DLog.time("get bitmap ");

        long detect_time = System.currentTimeMillis();
        DLog.time("detect ");
        List<YMFace> ymFaces = faceTrack.detectMultiBitmap(idcard_bitmap);
        detect_time = System.currentTimeMillis() - detect_time;
        DLog.time("detect ");


        float[] feature = null;
        if (ymFaces != null && ymFaces.size() > 0) {
            frame++;

            int maxIndex = 0;
            for (int i = 1; i < ymFaces.size(); i++) {
                if (ymFaces.get(maxIndex).getRect()[2] <= ymFaces.get(i).getRect()[2]) {
                    maxIndex = i;
                }
            }

            DLog.time("get feature ");
            long getfeature_time = System.currentTimeMillis();
            if (isIdcard)
                feature = faceTrack.getFaceFeatureCard(maxIndex);
            else
                feature = faceTrack.getFaceFeature(maxIndex);
            getfeature_time = System.currentTimeMillis() - getfeature_time;
            DLog.time("get feature ");

            frame_get_bitmap_time += get_bitmap_time;
            frame_detect_time += detect_time;
            frame_getfeature_time += getfeature_time;
        }

        return feature;
    }


//    public static void start_2000_2w_test() {
//
//        {
//            final File file = new File("/sdcard/img/output/1_n_out.txt");
//            if (file.exists()) file.deleteOnExit();
//            final File file1 = new File("/sdcard/img/output/1_1_out.txt");
//            if (file1.exists()) file1.deleteOnExit();
//        }
//        YMFaceTrack faceTrack = new YMFaceTrack();
//        faceTrack.initTrack(BaseApplication.getAppContext(), 0, 0);
//
//        File same_person_path = new File("/sdcard/img/tohongbing/TestSample4440");
//        File diff_person_path = new File("/sdcard/img/tohongbing/face20000");
//
//        Map<String, float[]> same_feature = get_feature(same_person_path, "same", faceTrack);
//        // do 1:1
//        List<String> alread_ana_list = new ArrayList<>();
//        StringBuilder sb = new StringBuilder();
//        int count = 0;
//        for (Map.Entry<String, float[]> entry : same_feature.entrySet()) {
//            final String file_path = entry.getKey();
//            final String[] split = new File(file_path).getName().split("_");
//            if (split.length != 2) continue;
//            final String substring = file_path.substring(0, file_path.length() - 5);
//            if (alread_ana_list.contains(substring)) continue;
//            String img_2 = (Integer.parseInt(split[1].substring(0, split[1].length() - 4)) == 1) ?
//                    substring + "2.jpg" : substring + "1.jpg";
//
//            if (!same_feature.containsKey(img_2)) continue;
//
//            float feature1[] = entry.getValue();
//            float feature2[] = same_feature.get(img_2);
//
//            int result = faceTrack.compareFaceFeature(feature1, feature2);
//            sb.append(file_path).append(" ").append(img_2)
//                    .append(" ").append(result).append("\n");
//
//            alread_ana_list.add(substring);
//            count++;
//            if (count % 1000 == 0) {
//                DLog.d("1:1 compare times: " + count);
//            }
//        }
//        FileUtil.writeFile("/sdcard/img/output/1_1_out.txt", sb.toString(), true);
//        DLog.d("end 1:1 mode");
//
//        alread_ana_list = new ArrayList<>();
//        sb = new StringBuilder();
//        count = 0;
//        Map<String, float[]> diff_feature = get_feature(diff_person_path, "diff", faceTrack);
//        // do 1:n
//
//
//        for (Map.Entry<String, float[]> entry : same_feature.entrySet()) {
//            final String file_path = entry.getKey();
//            final String substring = file_path.substring(0, file_path.length() - 5);
//            if (alread_ana_list.contains(substring)) continue;
//            String img_2 = substring + "1.jpg";
//            if (!same_feature.containsKey(img_2)) continue;
//
//            float feature1[] = same_feature.get(img_2);
//
//            for (Map.Entry<String, float[]> entry_2 : diff_feature.entrySet()) {
//
//                float feature2[] = entry_2.getValue();
//
//                int result = faceTrack.compareFaceFeature(feature1, feature2);
//                sb.append(img_2).append(" ").append(entry_2.getKey())
//                        .append(" ").append(result).append("\n");
//                count++;
//                if (count % 10000 == 0) {
//                    DLog.d("1:1 compare times: " + count);
//
//                    FileUtil.writeFile("/sdcard/img/output/1_n_out.txt", sb.toString(), true);
//                    sb = new StringBuilder();
//                }
//            }
//
//            alread_ana_list.add(substring);
//
//        }
//        FileUtil.writeFile("/sdcard/img/output/1_n_out.txt", sb.toString(), true);
//        DLog.d("end 1:1 mode");
//
//    }
//
//    private static Map<String, float[]> get_feature(File testPath, String mode, YMFaceTrack faceTrack) {
//        String feature_name = "/sdcard/img/output/feature_" + mode + ".txt";
//        File[] testPath_files = testPath.listFiles();
//        int featureSize = 512;
//        int count = 0;
//        File featurePath = new File(feature_name);
//        Map<String, float[]> featureMap = new HashMap<>();
//        if (featurePath.exists()) {
//            try {
//                BufferedReader e = new BufferedReader(new FileReader(feature_name), 8192);
//                String line = null;
//
//                while ((line = e.readLine()) != null) {
//                    count++;
//                    if (count % 50 == 0) {
//                        DLog.d("read feature times: " + count);
//                    }
//                    String[] split = line.trim().split(" ");
//                    float[] feature = new float[featureSize];
//                    for (int i = 0; i < featureSize; i++) {
//                        feature[i] = Float.parseFloat(split[i + 2]);
//                    }
////                    featureMap.put(new File(split[0]).getAbsolutePath(), feature);
//                    featureMap.put(split[0].split("\t")[0], feature);
//                }
//
//                e.close();
//            } catch (IOException var4) {
//                var4.printStackTrace();
//            }
//        } else {
//
//            count = 0;
//            for (File item : testPath_files) {
//                count++;
//                if (count % 50 == 0) {
//                    DLog.d("getfeature out times: " + count);
//                }
//                String itemName = item.getName();
//                if (itemName.endsWith(".jpg")) {
//                    float feature_idcard[] = getFeature(item.getAbsolutePath(), faceTrack);
//                    if (feature_idcard != null) {
//                        featureMap.put(item.getAbsolutePath(), feature_idcard);
//                    }
//                }
//            }
//
//            FileUtil.writeFile("/sdcard/img/output/time_" + mode + ".txt",
//                    "\nframe: " + frame +
//                            "\nframe_get_bitmap_time: " + (frame_get_bitmap_time / (float) frame) +
//                            "\nframe_detect_time: " + (frame_detect_time / (float) frame) +
//                            "\nframe_getfeature_time: " + (frame_getfeature_time / (float) frame)
//                    , true);
//
//            count = 0;
//            StringBuffer featureBuffer = new StringBuffer();
//            for (Map.Entry<String, float[]> entry : featureMap.entrySet()) {
//                featureBuffer.append(entry.getKey()).append(" ");
//
//                final float[] value1 = entry.getValue();
//                for (int i = 0; i < value1.length; i++) {
//                    featureBuffer.append(value1[i]).append(" ");
//                }
//                featureBuffer.append("\n");
//                count++;
//
//                if (count % 50 == 0) {
//                    DLog.d("save out times: " + count);
//
//                    FileUtil.writeFile(feature_name, featureBuffer.toString(), true);
//                    featureBuffer = new StringBuffer();
//                }
//            }
//            FileUtil.writeFile(feature_name, featureBuffer.toString(), true);
//        }
//
//        return featureMap;
//    }

}
