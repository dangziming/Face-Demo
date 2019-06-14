package readsense.face.base;

import android.content.res.Configuration;
import android.support.multidex.MultiDexApplication;


/**
 * Created by mac on 16/8/15.
 */
public class BaseApplication extends MultiDexApplication {

    private static BaseApplication instence;

    //绘制左右翻转
    public static final boolean yu = false;

    public static boolean reverse_180 = false;

    //是否显示logo
    public static boolean useLogo = true;
    public static int screenOri = Configuration.ORIENTATION_LANDSCAPE;
//    public static int  screenOri = Configuration.ORIENTATION_PORTRAIT;

    @Override
    public void onCreate() {
        super.onCreate();
        instence = this;
//        DLog.mSwitch = false;
//        DLog.mWrite = true;
    }

    public static BaseApplication getAppContext() {
        return instence;
    }

}
