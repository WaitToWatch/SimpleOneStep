package cn.gavinliu.simpleonestep;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import cn.gavinliu.simpleonestep.widget.DragDropHelper;

public class MainActivity extends AppCompatActivity {

    private static final String SHARED_FILE_NAME = "shared.png";

    private DragDropHelper mDragDropHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        copyPrivateRawResourceToPubliclyAccessibleFile();

        mDragDropHelper = new DragDropHelper();//拖动初始化
        //拖动监听
        mDragDropHelper.setOnDropedListener(new DragDropHelper.OnDropedListener() {
            @Override
            public void onDroped() {
                share(pkgName, className);
            }
        });

        ImageView imageView = (ImageView) findViewById(R.id.icon);//朋友圈

        mDragDropHelper.setDropView(imageView);//设置拖动对象

        findViewById(R.id.text).setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                mDragDropHelper.startDrag(v);//开始拖动
                return true;
            }
        });

        findViewById(R.id.text).setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDragDropHelper.onTouchEvent(event);//拖动触摸
                return false;
            }
        });
//        Log.e("log", System.currentTimeMillis()+"");
        Intent intent = new Intent(Intent.ACTION_SEND, null);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("image/*");
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> mApps = packageManager.queryIntentActivities(intent, PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);

        for (ResolveInfo info : mApps) {
            if ("com.tencent.mm.ui.tools.ShareToTimeLineUI".equals(info.activityInfo.name)) {
                imageView.setImageDrawable(info.loadIcon(packageManager));
                pkgName = info.activityInfo.packageName;
                className = info.activityInfo.name;
//                Log.e("log", System.currentTimeMillis()+"");
                break;
            }
        }
    }

    private String pkgName;
    private String className;

    public void share(String pkgName, String classname) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setComponent(new ComponentName(pkgName, classname));
        shareIntent.setType("image/*");
        Uri uri = Uri.fromFile(getFileStreamPath(SHARED_FILE_NAME));
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(shareIntent);
    }


    private void copyPrivateRawResourceToPubliclyAccessibleFile() {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = getResources().openRawResource(R.raw.ic_launcher);
            outputStream = openFileOutput(SHARED_FILE_NAME, Context.MODE_WORLD_READABLE | Context.MODE_APPEND);
            byte[] buffer = new byte[1024];
            int length = 0;
            try {
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
            } catch (IOException ioe) {
                /* ignore */
            }
        } catch (FileNotFoundException fnfe) {
            /* ignore */
        } finally {
            try {
                inputStream.close();
            } catch (IOException ioe) {
               /* ignore */
            }
            try {
                outputStream.close();
            } catch (IOException ioe) {
               /* ignore */
            }
        }
    }
}
