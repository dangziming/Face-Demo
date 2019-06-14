package readsense.face.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.DisplayMetrics;

import java.util.List;

import mobile.ReadFace.YMFace;

public class Utils {

    public static int getMaxFromArr(float arr[]) {
        int position = 0;
        float max = 0;
        for (int j = 0; j < arr.length; j++) {
            if (max <= arr[j]) {
                max = arr[j];
                position = j;
            }
        }
        return position;
    }

    public static void drawFace(YMFace face, Canvas canvas, int width, boolean isFacing) {
        if (face == null) return;

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        int strokeWidth = Math.max(width / 240, 2);
        paint.setStrokeWidth(strokeWidth);
        paint.setStyle(Paint.Style.STROKE);



        float rect[] = face.getRect();
        float landmarks[] = face.getLandmarks();

        float x1 = rect[0];
        float y1 = rect[1];
        float rect_width = rect[2];
        if (isFacing)
            x1 = width - rect[0] - rect_width;
        RectF rectF = new RectF(x1, y1, x1 + rect_width, y1 + rect_width);
        canvas.drawRect(rectF, paint);

//        for (int j = 0; j < landmarks.length / 2; j++) {
//            float x = landmarks[j * 2];
//            if (isFacing)
//                x = width - landmarks[j * 2];
//            float y = landmarks[j * 2 + 1];
//            canvas.drawPoint(x, y, paint);
//        }
    }

    public static void drawFace(List<YMFace> faces, Canvas canvas, int width, boolean isFacing) {
        if (faces == null) return;
        for (YMFace face : faces) {
            drawFace(face, canvas, width, isFacing);
        }
    }

    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm;
    }
}
