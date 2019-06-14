package readsense.face.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.io.File;

import readsense.face.R;
import readsense.face.base.BaseActivity;

public class SettingActivity extends BaseActivity {

    private TextView page_title;
    private TextView page_right;

    public static void main(String args[]) {
        for (int i = 1; i < 20; i++) {
            String path = i + "";
            if (path.length() == 1)
                path = "0" + path;
            File o = new File("/" + path);
            if (!o.exists()) o.mkdirs();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        page_title = (TextView) findViewById(R.id.page_title);
        page_title.setText(R.string._settings);
        page_right = (TextView) findViewById(R.id.page_right);
        page_right.setVisibility(View.GONE);


    }


    public void topClick(View v) {
        onBackPressed();
    }
}
