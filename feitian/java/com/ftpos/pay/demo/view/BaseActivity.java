package com.ftpos.pay.demo.view;

import static com.jirui.logger.Logger.RES_LOG_MSG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ftpos.pay.demo.R;
import com.ftpos.pay.demo.adapter.LogItemAdapter;
import com.ftpos.pay.demo.utils.DensityUtil;
import com.google.android.material.snackbar.Snackbar;
import com.jirui.logger.FormatStrategy;
import com.jirui.logger.Logger;
import com.jirui.logger.impl.logcat.LogcatAdapter;
import com.jirui.logger.impl.view.LogLine;
import com.jirui.logger.impl.view.ViewLogFormat;
import com.jirui.logger.impl.view.ViewLogStrategy;

import java.lang.reflect.Method;

/**
 * @author GuoJirui.
 * @date 2021/4/25.
 * @desc
 */
public abstract class BaseActivity extends FragmentActivity {
    public static final String TAG = "BaseActivity";
    private View rootView;
    private RecyclerView mLogRv;
    private LogItemAdapter mLogItemAdapter;
    private boolean mAutoScrollToBottom = true;

    private final Handler uiHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == RES_LOG_MSG) {
                if (mLogRv != null) {
                    mLogItemAdapter.append((LogLine) msg.obj);
                    scrollToBottom();
                }
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!"F360".equals(Build.MODEL)) {
            DensityUtil.setCustomDensity(this, this.getApplication());
        }
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        try {
            onCreateView(savedInstanceState);
        } catch (Exception e) {
            Log.d(TAG, "onCreate: Exception" + e.getMessage());
            return;
        }
        ViewLogStrategy logStrategy = new ViewLogStrategy(uiHandler);
        FormatStrategy viewFormat = ViewLogFormat.newBuilder()
                .localPriority(Logger.PRIORITY_OPEN)
                .logStrategy(logStrategy)
                .showLogHeader(false)
                .tag("Jirui")
                .build();
        Logger.addLogAdapter(new LogcatAdapter(viewFormat));
        mLogItemAdapter = new LogItemAdapter(this);
        initInfoBox();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        initInfoBox();
        super.onConfigurationChanged(newConfig);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    //Initializes the common message display box
    private void initInfoBox() {

        View btnClearMsg = bindViewById(R.id.btnClearMsg);
        if (btnClearMsg != null) {
            btnClearMsg.setOnClickListener(view -> clearLog());
        }

        mLogRv = findViewById(R.id.log_list);
        mLogRv.setLayoutManager(new LinearLayoutManager(this));
        mLogRv.setAdapter(mLogItemAdapter);
        mLogRv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                final LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                // if the bottom of the list isn't visible anymore, then stop autoscrolling
                mAutoScrollToBottom = (layoutManager.findLastCompletelyVisibleItemPosition() == recyclerView.getAdapter().getItemCount() - 1);
            }
        });
    }

    private void scrollToBottom() {
        if (mLogItemAdapter != null && mLogItemAdapter.getItemCount() > 5000) {
            mLogItemAdapter.clear();
        }
        mLogRv.scrollToPosition(mLogItemAdapter.getItemCount() - 1);
    }

    /*clear log*/
    public void clearLog() {
        if (mLogItemAdapter != null) {
            mLogItemAdapter.clear();
        }
    }


    protected abstract void onCreateView(Bundle savedInstanceState) throws Exception;

    protected <T extends View> T bindViewById(int id) {
        return findViewById(id);
    }

    protected void hideSoftKeyboard() {
        View focusView = getCurrentFocus();
        if (focusView != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(focusView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    //Collapse StatusBar
    protected void collapseStatusBar() {
        Object service = getSystemService("statusbar");
        if (null == service)
            return;
        try {
            Class<?> clazz = Class.forName("android.app.StatusBarManager");
            int sdkVersion = android.os.Build.VERSION.SDK_INT;
            Method collapse = null;
            if (sdkVersion <= 16) {
                collapse = clazz.getMethod("collapse");
            } else {
                collapse = clazz.getMethod("collapsePanels");
            }
            collapse.setAccessible(true);
            collapse.invoke(service);
        } catch (Exception e) {
            Log.d(TAG, "collapseStatusBar: ",e);
        }
    }

    @SuppressLint("ShowToast")
    public void showTransactionBar() {
        Snackbar.make(rootView, "The transaction is running, please wait.", Snackbar.LENGTH_INDEFINITE)
                .setDuration(3000)
                .show();
    }

}
