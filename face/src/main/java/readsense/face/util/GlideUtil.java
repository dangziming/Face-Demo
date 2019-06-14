package readsense.face.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.File;

import readsense.face.base.BaseApplication;

/**
 * Created by mac on 16/9/30.
 */

public class GlideUtil {
    public static void load(int resId, ImageView view) {
        Glide.with(BaseApplication.getAppContext()).load(resId).placeholder(resId).dontAnimate().into(view);
    }

    public static void load(Context context, ImageView v, String path) {
        Glide.with(context).load(new File(path))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(v);
    }
}
