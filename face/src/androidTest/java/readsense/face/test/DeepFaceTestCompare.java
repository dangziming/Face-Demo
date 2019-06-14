package readsense.face.test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.io.File;

import mobile.ReadFace.YMFaceTrack;


@RunWith(AndroidJUnit4.class)
public class DeepFaceTestCompare {
    private YMFaceTrack faceTrack;
    private static final String db_path = "/sdcard/img/";
    Context context;

    @Before
    public void initFaceTrack() {
        context = InstrumentationRegistry.getTargetContext();
        faceTrack = TestUtil.getFaceTrack(context, db_path, 75);
        faceTrack.setOrientation(0);
    }

    @org.junit.Test
    public void testMethod() throws Exception {

        File test_path = new File("/sdcard/img/误识别图片/");

        final File[] files = test_path.listFiles();

        for (File item : files) {
            TestUtil.compareImage(faceTrack, "/sdcard/img/注册图片/李沛然/251.jpg", item.getAbsolutePath());
            TestUtil.compareImage(faceTrack, "/sdcard/img/注册图片/李沛然/252.jpg", item.getAbsolutePath());
            TestUtil.compareImage(faceTrack, "/sdcard/img/注册图片/李沛然/253.jpg", item.getAbsolutePath());
            TestUtil.compareImage(faceTrack, "/sdcard/img/注册图片/李沛然/254.jpg", item.getAbsolutePath());
            TestUtil.compareImage(faceTrack, "/sdcard/img/注册图片/李沛然/255.jpg", item.getAbsolutePath());
        }

    }

    @After
    public void releaseSource() {
    }
}
