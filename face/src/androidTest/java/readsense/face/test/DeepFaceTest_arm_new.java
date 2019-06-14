package readsense.face.test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.List;

import mobile.ReadFace.YMFace;
import mobile.ReadFace.YMFaceTrack;
import readsense.face.R;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by dou on 2018/3/5.
 * <p>
 * 当测试使用JUnit4时，需要注解@RunWith(AndroidJUnit4.class)
 *
 * @Before：测试方法每次执行Test方法之前都会执行的方法注解，该注解替代了JUnit 3中的setUp()方法。
 * @Test：测试方法体注解
 * @After：测试方法每次执行完一个Test方法后都会执行的方法注解，该注解替代了JUnit 3中的tearDown()方法。
 * @Rule: 简单来说，是为各个测试方法提供一些支持。具体来说，比如我需要测试一个Activity，
 * 那么我可以在@Rule注解下面采用一个ActivityTestRule，该类提供了对相应Activity的功能测试的支持。
 * 该类可以在@Before和@Test标识的方法执行之前确保将Activity运行起来，并且在所有@Test和@After方法执行结束之后将Activity杀死。
 * 在整个测试期间，每个测试方法都可以直接对相应Activity进行修改和访问。
 * @BeforeClass: 为测试类标识一个static方法，在测试之前只执行一次。
 * @AfterClass: 为测试类标识一个static方法，在所有测试方法结束之后只执行一次。
 * @Test(timeout=<milliseconds>): 为测试方法设定超时时间。
 * @RequiresDevice：物理设备上运行。
 * @SdkSupress：限定最低SDK版本。例如@SDKSupress(minSdkVersion=18)。
 * @SmallTest，@MediumTest和@LargeTest：测试分级。 是比额
 */

@RunWith(AndroidJUnit4.class)
public class DeepFaceTest_arm_new {

    private YMFaceTrack faceTrack;
    private static final String test_path = "/sdcard/img/pics_1k-5ID";
    private static final String db_path = "/sdcard/img/";
    Context context;
    float rect_margain = 5f;
    float point_margain = 1f;
    float score_margain = 0.1f;

    @Before
    public void initFaceTrack() {
        context = InstrumentationRegistry.getTargetContext();
        faceTrack = TestUtil.getFaceTrack(context, db_path, 75);
    }

    @org.junit.Test
    public void testMethod() throws Exception {

        faceTrack.setOrientation(0);

        final List<YMFace> faces1 = TestUtil.detect(context, R.drawable.test_img1, faceTrack);
        assertTrue(faces1 != null && faces1.size() == 1);
        assertArrayEquals(faces1.get(0).getRect(), new float[]{69, 55, 65, 65}, rect_margain);


        assertArrayEquals(faces1.get(0).getLandmarks(), new float[]
                        {74.00496f, 68.34978f, 84.31389f, 63.405594f, 96.35324f, 66.36203f, 107.82597f, 66.208115f, 119.4314f, 63.006973f, 129.22845f, 67.70692f, 80.808395f, 74.89524f, 87.00452f, 74.25735f, 93.26643f, 75.38536f, 110.6881f, 75.16282f, 116.8656f, 73.85351f, 123.061264f, 74.34477f, 102.61396f, 91.67663f, 96.325645f, 95.744286f, 102.48019f, 97.59381f, 108.35631f, 95.60073f, 91.001564f, 108.01823f, 102.20569f, 105.03674f, 112.3907f, 107.8564f, 102.15886f, 112.473114f, 102.16023f, 108.12186f}
                , point_margain);
        float[] feature1 = faceTrack.getFaceFeature(0);
        assertTrue(feature1 != null);

        final List<YMFace> faces2 = TestUtil.detect(context, R.drawable.test_img2, faceTrack);
        assertTrue(faces2 != null && faces2.size() == 1);
        assertArrayEquals(faces2.get(0).getRect(), new float[]{123, 121, 113, 113}, rect_margain);
        assertArrayEquals(faces2.get(0).getLandmarks(), new float[]
                        {141.47452f, 144.97723f, 158.42879f, 140.11443f, 175.65233f, 147.34248f, 192.65205f, 149.25157f, 209.67943f, 145.84322f, 223.4375f, 153.57141f, 149.83325f, 158.0988f, 159.59747f, 158.19817f, 169.0651f, 161.10857f, 195.60376f, 163.87743f, 205.0653f, 162.90784f, 214.53574f, 164.7227f, 182.10197f, 183.03139f, 171.58138f, 190.33755f, 180.64041f, 193.59288f, 189.70914f, 192.31955f, 160.91449f, 209.70694f, 179.36803f, 204.88643f, 194.48788f, 213.84007f, 177.80838f, 218.29268f, 178.52548f, 210.92189f}
                , point_margain);

        float[] feature2 = faceTrack.getFaceFeature(0);
        assertTrue(feature2 != null);

        float compare_result = faceTrack.compareFaceFeatureMix(feature1, feature2);
        //date 201803060320_
        assertEquals(compare_result, 63.81565475463867, score_margain);
    }


    @After
    public void releaseSource() {
        faceTrack.onRelease();
    }
}
