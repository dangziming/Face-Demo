package readsense.face.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import org.apache.maven.artifact.ant.shaded.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.Key;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import dou.utils.BitmapUtil;
import dou.utils.DLog;
import dou.utils.FileUtil;
import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

import static org.junit.Assert.*;

 /*
  * 对比两张照片的相似度，并进行正确率和错误率的统计
  * 选择一个文件夹
  * 根据文件名格式，例如：3_0，进行判断
  */
public class CompareTestTest {

     private String out_path, path,out_feature_path;
     private float STARTING_VALUE = 20.0f;
     private float INCREASE_VALUE = 0.5f;
     private Map<String, float[]> feature_map;
     private Map<String, Float> A_A_result;
     private Map<String, Float> A_B_result;
     private List<Float> compare_value_array;
     private Integer[] compare_right;
     private Integer[] compare_wrong;
     private int all_count_a = 0;
     private int all_count_b = 0;
     private int track_count = 0;



     private YMFaceTrack faceTrack;
     private Context context;
     private static final String db_path = "/sdcard/img/";


     @Before
     public void initValues() {
         out_path = "/sdcard/test/out_file_1/" + "ID_test2_2254_1000"+ ".txt";

         out_feature_path = "/sdcard/test/out_file_1/" + "ID_test2_feature_mi_black_32_1000"+ ".txt";

         path = "/sdcard/test/ID_test2";

         context = InstrumentationRegistry.getTargetContext();
         feature_map = new HashMap<String, float[]>();
         A_A_result = new HashMap<String, Float>();
         A_B_result = new HashMap<String, Float>();
         faceTrack = TestUtil.getFaceTrack(context, db_path, 75);
         compare_value_array = new ArrayList<>();

         for (float i = STARTING_VALUE; i < 100; ) {
             if (!compare_value_array.equals(i)){
                 compare_value_array.add(i);
             }
             i += INCREASE_VALUE;
         }
         compare_right = new Integer[compare_value_array.size()];
         compare_wrong = new Integer[compare_value_array.size()];

         for (int i = 0 ; i < compare_value_array.size();i++){
             compare_right[i] = 0;
             compare_wrong[i] = 0;
         }

     }


     /*
      * 存储特征
      * map ： key为String 图片名，value 为float[] 特征值
      */
     private void saveFeature(Map<String,float[]> map){
         StringBuffer stringBuffer = new StringBuffer();
         for (String key : map.keySet()){
             stringBuffer.append(key);
             stringBuffer.append(" ");
             for (int i = 0;i < map.get(key).length;i++){
                 stringBuffer.append(map.get(key)[i]).append(" ");
             }
             stringBuffer.append("\n");

         }
         FileUtil.writeFile(out_feature_path,stringBuffer.toString());
         DLog.d("*********************** save finish *************************");

     }

     /*
      * 获取特征
      * 存入feature_map 中
      * path : 图片文件夹所在路径
      */
     private void getFeature(String path){
         DLog.d("*********************** start get feature *************************");

         File dir = new File(path);
         if (dir.exists()) {
             if (dir.isDirectory()) {
                 File[] files = dir.listFiles();
                 DLog.d("start track!");
                 for (int i = 0; i < files.length; i++) {
                     if (i % 1000 == 0) {

                         DLog.d("track this : " + i + files.length);
                     }
                     // 根据图片路径获取图片检测人脸
                     File currFile = files[i];
                     String currName = files[i].getName();
                     if (currName.contains(".jpg") || currName.contains(".png") || currName.contains(".jpeg") || currName.contains(".JPG")) {
                         Bitmap targetBitmap = BitmapUtil.decodeScaleImage(currFile.getAbsolutePath(), 1000, 1000);
                         List<YMFace> ymFaces = faceTrack.detectMultiBitmap(targetBitmap);
                         // 检测到人脸获取特征值并存储在feature_map中
                         if (ymFaces != null && ymFaces.size() != 0) {
                             Bitmap current = Bitmap.createBitmap(targetBitmap.getWidth(), targetBitmap.getHeight(), Bitmap.Config.RGB_565);
                             Canvas canvas = new Canvas(current);
                             canvas.drawBitmap(targetBitmap, 0, 0, new Paint());

                             for (int j = 0; j < ymFaces.size(); j++) {

                                 float[] faceFeature = faceTrack.getFaceFeature(j);
                                 feature_map.put(currName, faceFeature);
                                 track_count++;
                             }
                         } else {
                             Log.d("no face in : ", currFile.getName());
                         }
                     }
                 }
             }
         }
         DLog.d("track count : " + track_count);

     }

     /*
      * 读取特征
      * 存入feature_map 中
      * path : 特征值所在文件路径
      */
     private void readFeature(String path){
         DLog.d("*********************** start read feature *************************");

         File file = new File(path);
         if (!file.exists()) return;
         if (!file.isFile()) return;

         try {
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
             String line_feature = null;
             String[] str;
             String pic_name;
             float[] feature;
             while ((line_feature = bufferedReader.readLine())!= null){

                 str = line_feature.trim().split(" ");
                 pic_name = str[0];
                 feature = new float[(str.length - 1)];
                 for (int i = 1;i < str.length ; i++){
                     feature[i-1] =Float.parseFloat(str[i]);
                 }
                 feature_map.put(pic_name,feature);
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }

         DLog.d("*********************** finish read feature *************************");

     }

     /*
      * 对比特征 1:1
      */
     private void compareFeature(Map<String,float[]> map){

//          根据特征值进行对比并存储比对结果
         DLog.d("start compare!");
         float compare_result = 0.0f;
         StringBuilder stringBuilder_a = new StringBuilder();
         StringBuilder stringBuilder_b = new StringBuilder();
         String[] str_name_a, str_name_b, str_personId_a, str_personId_b;
         for (Map.Entry<String,float[]> str : map.entrySet()) {
             // 根据命名规则，例如3_0.jpg，判断是否是同一人
             String key = str.getKey();
             str_name_a = StringUtils.split(String.valueOf(key), ".");
             str_personId_a = StringUtils.split(str_name_a[0],"_");
             // 是注册照片
             if (str_personId_a[1].equals("1")){

                 for (Map.Entry<String,float[]> str2 : map.entrySet()) {

                     String key2 = str2.getKey();
                     str_name_b = StringUtils.split(String.valueOf(key2), ".");
                     str_personId_b = StringUtils.split(str_name_b[0],"_");
                     // 判断是不是注册照片
                     if (!str_personId_b[1].equals("1")) {

                         // 判断是不是本人
                         if (str_personId_a[0].equals(str_personId_b[0])) {
                             // 是本人
                             all_count_a++;
                             compare_result = faceTrack.compareFaceFeatureMix(str.getValue(), str2.getValue());

                             stringBuilder_a.append(str.getKey()+" "+str2.getKey()+" " + compare_result +  " " + all_count_a + "\n");

                             for (int i = 0;i < compare_value_array.size();i++){
                                 if (compare_result > compare_value_array.get(i)){
                                     compare_right[i]++;
                                 }
                             }
                         }else {
                             // 非本人
                             all_count_b++;
                             compare_result = faceTrack.compareFaceFeatureMix(str.getValue(), str2.getValue());
//                             stringBuilder_b.append(str.getKey()+" "+str2.getKey()+" " + compare_result + " " + all_count_b);
                             for (int i = 0;i<compare_value_array.size();i++){
                                 if (compare_result > compare_value_array.get(i)){
                                     compare_wrong[i]++;
                                 }
                             }
                         }
                     }
                 }
             }
         }
         DLog.d("now compare  count: " + all_count_a + " " + all_count_b);
         FileUtil.writeFile("/sdcard/test/out_file_1/1_1_out_2314.txt", stringBuilder_a.toString(), true);
//         FileUtil.writeFile("/sdcard/test/out_file_1/1_n_out_1331.txt", stringBuilder_b.toString(), true);

         outputValue(compare_value_array,compare_right,compare_wrong,all_count_a,all_count_b);
     }

     /*
      * 通过特征文件对比特征获取相似度并将结果存储下来
      */
     @Test
     public void compareFeatureByFile() throws Exception{
//         getFeature(path);
//         saveFeature(feature_map);

         readFeature(out_feature_path);
         compareFeature(feature_map);
     }

     @Test
     public void testCase() throws Exception {
//         // 根据path获取图片路径
//         getFeature(path);
//         // 存储特征
//         saveFeature(feature_map);

         readFeature(out_feature_path);
         compareFeature(feature_map);

//         // 根据特征值进行对比并存储比对结果
//         DLog.d("start compare!");
//         float compare_result = 0.0f;
//         String[] str_name_a, str_name_b, str_personId_a, str_personId_b;
//         for (String key : feature_map.keySet()) {
//             // 根据命名规则，例如3_0.jpg，判断是否是同一人
//             str_name_a = key.split("[.]");
//             str_personId_a = str_name_a[0].split("_");
//
//             for (String key2 : feature_map.keySet()) {
//                 str_name_b = key2.split("[.]");
//                 str_personId_b = str_name_b[0].split("_");
//
//                 // 同一个人进行对比 注册图和实拍图
//                 if (str_personId_a[0].equals(str_personId_b[0])) {
//
//                     if (str_personId_a[1].equals(String.valueOf(0)) && !str_personId_b[1].equals(String.valueOf(0))) {
//                         all_count_a++;
//                         compare_result = faceTrack.compareFaceFeatureMix(feature_map.get(key), feature_map.get(key2));
//
//                         for (int i = 0;i<compare_value_array.size();i++){
//                             if (compare_result > compare_value_array.get(i)){
//                                 compare_right[i] = compare_right[i] + 1;
//                             }
//                         }
//                     }
//                 }else {
//
//                     if (str_personId_a[1].equals(String.valueOf(0)) && !str_personId_b[1].equals(String.valueOf(0))) {
//                        all_count_b++;
//                         compare_result = faceTrack.compareFaceFeatureMix(feature_map.get(key), feature_map.get(key2));
//
//                         for (int i = 0;i<compare_value_array.size();i++){
//                             if (compare_result > compare_value_array.get(i)){
//                                 compare_wrong[i] = compare_wrong[i] + 1;
//                             }
//                         }
//                     }
//                 }
//
//             }
//         }
//
//         outputValue(compare_value_array,compare_right,"正确率：",all_count_a);
//         outputValue(compare_value_array,compare_wrong,"错误率：",all_count_b);

//         // 进行正确率的计算
//         caculateRate(A_A_result,"正确率：");
//
//         // 进行错误率的计算
//         caculateRate(A_B_result,"错误率：");
     }


     @Test
     public void testCase1() throws Exception {
         // 根据path获取图片路径
         File dir = new File(path);
         if (dir.exists()) {
             if (dir.isDirectory()) {
                 File[] files = dir.listFiles();
                 DLog.d("start track!");
                 for (int i = 0; i < files.length; i++) {
                     if (i % 1000 == 0) {

                         DLog.d("track this : " + i + files.length);
                     }
                     // 根据图片路径获取图片检测人脸
                     File currFile = files[i];
                     String currName = files[i].getName();
                     if (currName.contains(".jpg") || currName.contains(".png") || currName.contains(".jpeg") || currName.contains(".JPG")) {
                         Bitmap targetBitmap = BitmapUtil.decodeScaleImage(currFile.getAbsolutePath(), 800, 800);
                         List<YMFace> ymFaces = faceTrack.detectMultiBitmap(targetBitmap);
                         // 检测到人脸获取特征值并存储在feature_map中
                         if (ymFaces != null && ymFaces.size() != 0) {
                             Bitmap current = Bitmap.createBitmap(targetBitmap.getWidth(), targetBitmap.getHeight(), Bitmap.Config.RGB_565);
                             Canvas canvas = new Canvas(current);
                             canvas.drawBitmap(targetBitmap, 0, 0, new Paint());

                             for (int j = 0; j < ymFaces.size(); j++) {

                                 float[] faceFeature = faceTrack.getFaceFeature(j);
                                 feature_map.put(currName, faceFeature);
                             }
                         } else {
                             Log.d("no face in : ", currFile.getName());
                         }
                     }
                 }
             }
         }

         // 根据特征值进行对比并存储比对结果
         DLog.d("start compare!");
         float compare_result = 0.0f;
         String[] str_name_a, str_name_b, str_personId_a, str_personId_b;
         for (String key : feature_map.keySet()) {
             // 根据命名规则，例如3_0.jpg，判断是否是同一人
             str_name_a = key.split("[.]");
             str_personId_a = str_name_a[0].split("_");

             for (String key2 : feature_map.keySet()) {
                 str_name_b = key2.split("[.]");
                 str_personId_b = str_name_b[0].split("_");

                 // 同一个人进行对比 注册图和实拍图
                 if (str_personId_a[0].equals(str_personId_b[0])) {
                     all_count_a++;
                     compare_result = faceTrack.compareFaceFeatureMix(feature_map.get(key), feature_map.get(key2));

                     for (int i = 0;i<compare_value_array.size();i++){
                         if (compare_result > compare_value_array.get(i)){
                             compare_right[i] = compare_right[i] + 1;
                         }
                     }


                 }else {
                     all_count_b++;
                     compare_result = faceTrack.compareFaceFeatureMix(feature_map.get(key), feature_map.get(key2));

                     for (int i = 0;i<compare_value_array.size();i++){
                         if (compare_result > compare_value_array.get(i)){
                             compare_wrong[i] = compare_wrong[i] + 1;
                         }
                     }
                 }
             }

         }

//         outputValue(compare_value_array,compare_right,"正确率：",all_count_a);
//         outputValue(compare_value_array,compare_wrong,"错误率：",all_count_b);

//         // 进行正确率的计算
//         caculateRate(A_A_result,"正确率：");
//
//         // 进行错误率的计算
//         caculateRate(A_B_result,"错误率：");
     }



     @After
     public void releaseValue() {
         faceTrack.onRelease();
     }

     private void caculateRate(Map<String, Float> map, String string) {

         Map<Float, Integer> caculate_result = new HashMap<Float, Integer>();
         DLog.d("start caculate ! " + map.size());

         for (String key : map.keySet()) {
             float keyValue = map.get(key);
             for (float i = STARTING_VALUE; i < 100; ) {
                 if (!caculate_result.containsKey(i)) {
                     caculate_result.put(i, 0);
                 }
                 if (keyValue > i) {
                     caculate_result.put(i, caculate_result.get(i) + 1);
                 }
                 i += INCREASE_VALUE;
             }
         }
         DLog.d("end caculate : " + caculate_result.size());

         StringBuffer stringBuffer = new StringBuffer();
         stringBuffer.append(string + "\n");
         TreeMap treemap = new TreeMap(caculate_result);

         for (Iterator it = treemap.keySet().iterator(); it.hasNext(); ) {
             Float key = (Float) it.next();
             stringBuffer.append(key + " : " + (Integer.valueOf(treemap.get(key).toString()) * 1000 / map.size() / 10f) + "%\n");
         }

         FileUtil.writeFile(out_path, stringBuffer.toString(), true);
         DLog.d("all count : " + map.size());
         DLog.d("*********************** End Write *************************");
     }

     private void outputValue(List<Float> list,Integer[] array_right, Integer[] array_wrong,int all_count_right,int all_count_wrong){
         DLog.d("*********************** Start Write *************************");

         StringBuilder stringBuilder = new StringBuilder();
         for (int i = 0;i<list.size();i++){
             stringBuilder.append(list.get(i) + "/" + ((float)array_right[i]/ (float) (all_count_right)) + " : "+ ((float) array_wrong[i]/ (float) (all_count_wrong)) + " " + array_right[i] + " " + array_wrong[i] + "\n");
         }
         FileUtil.writeFile(out_path, stringBuilder.toString(),true);
         DLog.d("all_count_right : " + all_count_right);
         DLog.d("all_count_wrong : " + all_count_wrong);

         DLog.d("*********************** End Write *************************");
     }

 }
