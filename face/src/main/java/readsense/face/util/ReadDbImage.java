package readsense.face.util;

/**
 * Created by dou on 2018/2/23.
 */

public class ReadDbImage {


//    public void decodeImageFromDB(Context context) {
////        SqliteHelperManager sqliteHelperManager = new SqliteHelperManager(context);
//        SQLiteDatabase database = SQLiteDatabase.openOrCreateDatabase("/sdcard/img/faces.db", null);
//
//        Cursor cursor = database.query("faces",
//                new String[]{"id,face_image_blob"}, null, null, null, null, null, null);
//
//        cursor.moveToFirst();
//        while (!cursor.isAfterLast()) {
//
//            final int id_index = cursor.getColumnIndex("id");
//            final int face_image_blob_index = cursor.getColumnIndex("face_image_blob");
//            byte[] image_data = cursor.getBlob(face_image_blob_index);
//            int image_wide= 116*116;
//            int[] out = new int[116 * 116 * 4];
//
//            FaceAnalyze.nativeGetBGRAFromGray(image_data, 116, 116, out);
//
//            Bitmap show = Bitmap.createBitmap(116, 116, Bitmap.Config.ARGB_8888);
//
//            for (int i = 0; i < 116; i++) {
//                for (int j = 0; j < 116; j++) {
//
////                    show.setPixel(i,j,);
//                }
//            }
//            show.setPixels(out, 0, show.getWidth(), 0, 0, show.getWidth(), show.getHeight());
//
//            BitmapUtil.saveBitmap(show, "/sdcard/img/output/" + System.currentTimeMillis() + ".jpg");
//            cursor.moveToNext();
//        }
//
//        cursor.close();
//    }

}
