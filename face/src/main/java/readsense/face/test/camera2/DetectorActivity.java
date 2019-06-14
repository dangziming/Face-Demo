/*
 * Copyright 2016 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package readsense.face.test.camera2;

import android.graphics.Canvas;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Build;
import android.os.Trace;
import android.support.annotation.RequiresApi;
import android.util.Size;
import android.view.Display;

import dou.utils.DLog;
import readsense.face.R;
import readsense.face.view.OverlayView;

/**
 * An activity that uses a TensorFlowMultiBoxDetector and ObjectTracker to detect and then track
 * objects.
 */

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class DetectorActivity extends CameraActivity implements OnImageAvailableListener {


    private static final Size DESIRED_PREVIEW_SIZE = new Size(640, 480);


    private Integer sensorOrientation;

    private byte[][] yuvBytes;



    @Override
    public void onPreviewSizeChosen(final Size size, final int rotation) {

        final Display display = getWindowManager().getDefaultDisplay();
        final int screenOrientation = display.getRotation();


        sensorOrientation = rotation + screenOrientation;


        yuvBytes = new byte[3][];


        trackingOverlay = (OverlayView) findViewById(R.id.tracking_overlay);
        trackingOverlay.addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {
                    }
                });

        addCallback(
                new OverlayView.DrawCallback() {
                    @Override
                    public void drawCallback(final Canvas canvas) {

                    }
                });
    }

    OverlayView trackingOverlay;

    @Override
    public void onImageAvailable(final ImageReader reader) {
        Image image = null;

        try {
            image = reader.acquireLatestImage();

            if (image == null) {
                return;
            }

            DLog.d("read camera2 frame");
            final Plane[] planes = image.getPlanes();
            fillBytes(planes, yuvBytes);

            int iw = 640;
            int ih = 480;

            byte[] y = yuvBytes[0];
            byte[] u = yuvBytes[1];
            byte[] v = yuvBytes[2];

            byte[] yuv = new byte[iw * ih * 2];

            System.arraycopy(y, 0, yuv, 0, iw * ih);
            for (int i = 0; i < iw * ih / 4; i++) {
                yuv[iw * ih + i * 2] = v[i];
                yuv[iw * ih + i * 2 + 1] = u[i];
            }
//            System.arraycopy(v, 0, yuv, iw * ih, iw * ih / 4);
//            System.arraycopy(u, 0, yuv, iw * ih + iw * ih / 4, iw * ih / 4);


//            CameraUtil.saveFromPreview(yuv, "/sdcard/test.jpg", iw, ih);
            image.close();
        } catch (final Exception e) {
            if (image != null) {
                image.close();
            }
        }


        Trace.endSection();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.camera_connection_fragment_tracking;
    }

    @Override
    protected Size getDesiredPreviewFrameSize() {
        return DESIRED_PREVIEW_SIZE;
    }

}
