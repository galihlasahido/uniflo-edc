package com.ftpos.ftappdemo;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ftpos.library.smartpos.led.Led;

public class LedActivity extends BaseActivity {

    private TextView mShowResultTv;
    private RelativeLayout indicatorOn;
    private RelativeLayout indicatorOff;
    private Led led;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led);
        initView();
        this.led = MainActivity.led;
    }


    private void initView() {
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText("Led View");

        findViewById(R.id.ukey_navbar_left_btn).setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        findViewById(R.id.set_led_status_on).setOnClickListener(this);
        findViewById(R.id.set_led_status_off).setOnClickListener(this);

        mShowResultTv = findViewById(R.id.function_return_result_tv);

        indicatorOn = findViewById(R.id.set_led_indicator_on);
        indicatorOff = findViewById(R.id.set_led_indicator_off);

        if ("F310".equals(Build.MODEL)) {
            String hardware = android.os.Build.HARDWARE;
            if (hardware.matches("qcom")) {  // SYSMODEL_310_PLUS
                indicatorOn.setVisibility(View.VISIBLE);
                indicatorOff.setVisibility(View.VISIBLE);
                indicatorOn.setOnClickListener(this);
                indicatorOff.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.ukey_navbar_left_btn: {
                finish();
            }
            break;
            case R.id.ukey_navbar_right_btn: {
                mShowResultTv.setText("");
            }
            break;
            case R.id.set_led_status_on: {
                led.ledStatus(true, true, true, true);
                logMsg("set Led status on success\n");
            }
            break;
            case R.id.set_led_status_off: {
                led.ledStatus(false, false, false, false);
                logMsg("set Led status off success\n");
            }
            break;
            case R.id.set_led_indicator_on: {
                int ret = led.ledCardIndicator(0x03, 10, 100, 100);
                if (ret == 0) {
                    logMsg("set Led indicator on success\n");
                } else {
                    logMsg("set Led indicator on fail: " + ret + "\n");
                }
            }
            break;
            case R.id.set_led_indicator_off: {
                int ret = led.ledCardIndicator(0x00, 0, 0, 0);
                if (ret == 0) {
                    logMsg("set Led indicator off success\n");
                } else {
                    logMsg("set Led indicator off fail: " + ret + "\n");
                }
            }
            break;
            default:
                break;
        }
    }

    public synchronized void logMsg(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String oldMsg = mShowResultTv.getText().toString();
                mShowResultTv.setText(oldMsg + "\n" + msg);
            }
        });
    }
}
