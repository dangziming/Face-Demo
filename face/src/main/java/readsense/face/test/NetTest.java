package readsense.face.test;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import dou.utils.DLog;
import mobile.ReadFace.net.ApiListener;
import mobile.ReadFace.net.NetFaceTrack;

/**
 * Created by mac on 2017/1/6 下午4:23.
 */

public class NetTest {

    static ApiListener listener = new ApiListener() {
        @Override
        public void onError(String s) {
            DLog.d(s);
        }

        @Override
        public void onCompleted(String s) {
            DLog.d(s);
        }
    };
    static NetFaceTrack netFaceTrack  = null;

    public static void startTest(final File file) {//同步测试
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = netFaceTrack.faceDetaction(file, "", null);
                    DLog.d(result);
                    String face_id = "";
                    try {
                        face_id = new JSONObject(result).getJSONArray("faces")
                                .getJSONObject(0).getString("face_id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    result = netFaceTrack.faceIdentification(face_id, "cc73f7cd31b6da801334268497fa9869", null);
                    DLog.d(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void startRegister(final File file) {//同步测试
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String result = netFaceTrack.faceDetaction(file, "", null);
                    DLog.d(result);
                    String face_id = "";
                    try {
                        face_id = new JSONObject(result).getJSONArray("faces")
                                .getJSONObject(0).getString("face_id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    result = netFaceTrack.peopleCreate(face_id, "窦红斌", null);
                    DLog.d(result);
                    String personId = "";
                    try {
                        personId = new JSONObject(result).getString("person_id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    String groupId = "";
                    result = netFaceTrack.groupsCreate(personId, "窦红斌", null);
                    try {
                        groupId = new JSONObject(result).getString("person_id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    DLog.d("groupId : " + groupId);
//                    result = netFaceTrack.groupsAddPerson("58b343ab06188d7ab65021adf0e6f2a4", personId, null);

                    DLog.d(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    public static void testDetection(String filePath) {
        netFaceTrack.faceDetaction(new File(filePath), "", listener);
    }

    public static void testfaceVerificationFace(String face_id, String face_id2) {
        netFaceTrack.faceVerificationFace(face_id, face_id2, listener);
    }

    public static void testfaceVerificationPerson(String face_id, String person_id) {
        netFaceTrack.faceVerificationPerson(face_id, person_id, listener);
    }


    public static void testCreatePeople(String face_id, String name) {
        netFaceTrack.peopleCreate(face_id, name, listener);
    }

    public static void testPeopleAddFace(String person_id, String face_id) {
        netFaceTrack.peopleAddFace(person_id, face_id, listener);
    }

    public static void testPeopleEmpty(String person_id) {
        netFaceTrack.peopleEmpty(person_id, listener);
    }

    public static void testPeopleDelete(String person_id) {
        netFaceTrack.peopleDelete(person_id, listener);
    }

    public static void testPeopleRemoveFace(String person_id, String face_id) {
        netFaceTrack.peopleRemoveFace(person_id, face_id, listener);
    }


    public static void testfaceIdentification(String face_id, String group_id) {
        netFaceTrack.faceIdentification(face_id, group_id, listener);
    }

    public static void testGroupCreate(String person_id, String name) {
        netFaceTrack.groupsCreate(person_id, name, listener);
    }

    public static void testGroupAddPerson(String group_id, String person_id) {
        netFaceTrack.groupsAddPerson(group_id, person_id, listener);
    }

    public static void testGroupRemovePerson(String group_id, String person_id) {
        netFaceTrack.groupsRemovePerson(group_id, person_id, listener);
    }

    public static void testgroupsEmpty(String group_id, String person_id) {
        netFaceTrack.groupsEmpty(group_id, listener);
    }

    public static void testgroupsDelete(String group_id, String person_id) {
        netFaceTrack.groupsDelete(group_id, listener);
    }


//    public String testIdentify(File file, String groupid) {
//
//        String result = netFaceTrack.faceDetaction(file, "", null);
//        //get faceid from result
//        String faceid = "";
//
//        result = netFaceTrack.faceIdentification(faceid, groupid, null);
//
//        //根据识别结果，判断是否认识
//        if (认识) {
//            return personid;
//        } else {
//            result = netFaceTrack.peopleCreate(faceid, "peoplename", null);
//            //get personid from result 可调用add_face接口来为personid继续添加人脸
//            String personid = "";
//            result = netFaceTrack.groupsAddPerson(groupid, personid, null);
//        }
//        return personid;
//
//    }


}
