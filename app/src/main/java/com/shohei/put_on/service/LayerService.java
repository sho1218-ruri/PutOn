package com.shohei.put_on.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.IBinder;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.shohei.put_on.R;
import com.shohei.put_on.controller.utils.Logger;
import com.shohei.put_on.controller.utils.ServiceRunningDetector;
import com.shohei.put_on.model.Memo;
import com.shohei.put_on.view.widget.OverlayMemoView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nakayamashohei on 15/08/29.
 */
public class LayerService extends Service implements View.OnTouchListener {
    public final static int NOTIFICATION_ID = 001;

    private Memo mMemo;
    private OverlayMemoView mOverlayMemoView;
    private ServiceRunningDetector mServiceRunningDetector;

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;

    private FrameLayout mMemoFrameLayout;
    private EditText mMemoEditText;
    private AutoCompleteTextView mTagEditText;
    private View mSaveButton;
    private View mFab;
    private ImageView mScrollBarImageView;

    private int mPositionX;
    private int mPositionY;
    private int mDisplayHeight;

    private boolean mIsOpen = true;
    private boolean mIsClicked = true;

//    Viewの関連づけ
    private void findViews() {
        mMemoFrameLayout = (FrameLayout) mOverlayMemoView.findViewById(R.id.memoCreate_FrameLayout_Overlay);
        mMemoEditText = (EditText) mOverlayMemoView.findViewById(R.id.memo_EditText_Overlay);
        mTagEditText = (AutoCompleteTextView) mOverlayMemoView.findViewById(R.id.tag_EditText_Overlay);
        mSaveButton = mOverlayMemoView.findViewById(R.id.save_FAB_Overlay);
        mScrollBarImageView = (ImageView) mOverlayMemoView.findViewById(R.id.scrollBar_ImageView);
        mFab = mOverlayMemoView.findViewById(R.id.fab_Overlay);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mOverlayMemoView = (OverlayMemoView) LayoutInflater.from(this).inflate(R.layout.overlay_memo_view, null);
        mOverlayMemoView.setOnTouchListener(this);
        appearOverlayView();
    }

    @Override
    public void onDestroy() {
        mWindowManager.removeView(mOverlayMemoView);
    }

    @RequiresPermission(android.Manifest.permission.SYSTEM_ALERT_WINDOW)
    public void appearOverlayView() {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mMemo = new Memo();
        mServiceRunningDetector = new ServiceRunningDetector(this);

        findViews();

        mDisplayHeight = getDisplaySize().y;

        //Layoutを設定
        mLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        mLayoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
        mWindowManager.addView(mOverlayMemoView, mLayoutParams);

        firstStartHint();
        setAutoComplete();
    }

    // Layoutのパラメータの設定
    private void updateLayoutParams(int widthParam, int flagParam, View view) {
        mLayoutParams.width = widthParam;
        mLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParams.flags = flagParam;
        mWindowManager.updateViewLayout(view, mLayoutParams);
    }

    private void firstStartHint() {
        SharedPreferences sharedPreferences = getSharedPreferences("LayerService", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        if (sharedPreferences.getBoolean("OverlayView", false) == false) {
            mScrollBarImageView.setVisibility(View.VISIBLE);

            editor.putBoolean("OverlayView", true);
            editor.commit();
        } else {
            mScrollBarImageView.setVisibility(View.INVISIBLE);
        }
    }

    private Point getDisplaySize() {
        Display display = mWindowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    private void setAutoComplete() {
        List<Memo> list = mMemo.searchMemo();
        List<String> tags = new ArrayList<>();
        for (int i = 0; i < list.size(); i++){
            tags.add(list.get(i).tag);
        }
        String[] stringArray = tags.toArray(new String[tags.size()]);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stringArray);
        mTagEditText.setAdapter(adapter);
    }

    private void setNotification() {
        float[] hsv = new float[3];
        Color.colorToHSV(ContextCompat.getColor(this, R.color.primary), hsv);

        Intent intent = new Intent(this, LayerService.class);
        PendingIntent contentIntent = PendingIntent.getService(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext())
                .setSmallIcon(R.mipmap.ic_notification)
                .setContentTitle(getResources().getText(R.string.app_name))
                .setContentText(getResources().getText(R.string.text_content_notification))
                .setColor(Color.HSVToColor(hsv))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        float mInitialTouchX;
        float mInitialTouchY;
        // Viewを動かす
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mInitialTouchX = event.getRawX();
                mInitialTouchY = event.getRawY();
                mPositionX = (int) mInitialTouchX;
                mPositionY = (int) mInitialTouchY;

                if (!mIsOpen) mIsClicked = true;
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                if (mIsOpen) {
                    mScrollBarImageView.setVisibility(View.VISIBLE);
                    mScrollBarImageView.setImageResource(R.drawable.scroll_bar);
                    final int y = mDisplayHeight - (int) event.getRawY() - (mOverlayMemoView.getHeight() / 2);
                    mLayoutParams.y = y;
                } else {
                    final int x = mPositionX - (int) event.getRawX();
                    final int y = mPositionY - (int) event.getRawY();
                    mLayoutParams.x -= x;
                    mLayoutParams.y += y;
                    mPositionX = (int) event.getRawX();
                    mPositionY = (int) event.getRawY();
                    mIsClicked = false;
                }
                Logger.d(this.getClass(), "X:" + mLayoutParams.x + " Y:" + mLayoutParams.y);
                mWindowManager.updateViewLayout(view, mLayoutParams);
                break;
            }
            case MotionEvent.ACTION_UP: {
                if (!mIsOpen && mIsClicked) {
                    mIsOpen = true;
                    mMemoFrameLayout.setVisibility(View.VISIBLE);
                    mFab.setVisibility(View.GONE);

                    updateLayoutParams(
                            WindowManager.LayoutParams.MATCH_PARENT,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                            view
                    );
                } else {
                    mScrollBarImageView.setVisibility(View.INVISIBLE);
                }
                break;
            }
        }
        return false;
    }

    private AnimationSet buttonAnimation(final float size) {
        AnimationSet buttonAnim = new AnimationSet(true);
        ScaleAnimation startAnim = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f, size / 2, size / 2);
        startAnim.setDuration(500);
        buttonAnim.addAnimation(startAnim);
        ScaleAnimation imageEndAnim = new ScaleAnimation(0.9f, 1.1f, 0.9f, 1.1f, size / 2, size / 2);
        imageEndAnim.setDuration(300);
        buttonAnim.addAnimation(imageEndAnim);
        return buttonAnim;
    }

    //    SaveButtonの処理
    public void saveButtonOverlay(View v) {
        final String memo = mMemoEditText.getText().toString();
        final String tag = mTagEditText.getText().toString();

        mMemo.saveMemo(memo, tag);
        if (!memo.isEmpty()) {
            mSaveButton.startAnimation(buttonAnimation(getResources().getDimension(R.dimen.fab_size_small)));
            Toast.makeText(this, R.string.text_save_toast, Toast.LENGTH_SHORT).show();
            stopSelf();
            setNotification();

            Intent intent = new Intent();
            intent.setAction("ACTION_MEMO_SAVED");
            sendBroadcast(intent);
        }
    }

    //    CloseButtonの処理
    public void closeButtonOverlay(View v) {
        Logger.d(this.getClass(), "Close");

        if (mServiceRunningDetector.isServiceRunning()) {
            stopSelf();
            setNotification();
        }
    }

    //    MinimizeButtonの処理
    public void minimizeButtonOverlay(View v) {
        Logger.d(this.getClass(), "Minimize");

        mIsOpen = false;
        mMemoFrameLayout.setVisibility(View.GONE);
        mFab.setVisibility(View.VISIBLE);

        updateLayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                mOverlayMemoView
        );
    }
}