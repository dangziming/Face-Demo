package readsense.face.sample;

import java.util.List;

import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;

public class DeepLivenessTest {

    void test_is_face_clear(List<YMFace> faces, YMFaceTrack faceTrack) {

        int bestFaceIndex = 0;//从faces中挑选best index（根据人脸位置，人脸大小挑选最佳人脸）

        final YMFace face = faces.get(bestFaceIndex);
        float[] headposes = face.getHeadpose();
        final float[] rect = face.getRect();

        boolean next = true;

        boolean ori_bool = !(Math.abs(headposes[0]) <= 20 && Math.abs(headposes[1]) <= 20
                && Math.abs(headposes[2]) <= 20);

        if (!ori_bool) {
            //角度不佳
            next = false;
        }

        if (next) {//判定人脸质量不佳
            int faceQuality = faceTrack.getFaceQuality(bestFaceIndex);
            next = (faceQuality >= 92);

            if (!next) {
                //人脸质量差
            }
        }

        if(next){
            //do next
        }

    }
}
