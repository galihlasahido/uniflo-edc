package com.ftpos.ftappdemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ftpos.library.smartpos.buzzer.Buzzer;
import com.ftpos.library.smartpos.buzzer.BuzzerMode;
import com.ftpos.library.smartpos.errcode.ErrCode;


public class BuzzerActivity extends BaseActivity {

    private TextView mShowResultTv;
    private RelativeLayout mSetBuzzerState;

    private EditText mFrequency;
    private EditText mTimes;
    private EditText mOnTimes;
    private EditText mOffTimes;
    private EditText mVolume;

    private Buzzer buzzer;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buzzer);
        initView();

        this.buzzer = MainActivity.buzzer;

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        mSetBuzzerState.setEnabled(true);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    private void initView() {
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText("Buzzer View");

        findViewById(R.id.ukey_navbar_left_btn).setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        findViewById(R.id.set_buzzer_frequency).setOnClickListener(this);
        findViewById(R.id.set_volume).setOnClickListener(this);

        mSetBuzzerState = findViewById(R.id.set_buzzer_status);
        mSetBuzzerState.setOnClickListener(this);

        mFrequency = findViewById(R.id.frequency);
        mFrequency.setText("2000");

        mTimes = findViewById(R.id.times);
        mTimes.setText("3");

        mOnTimes = findViewById(R.id.onTimes);
        mOnTimes.setText("200");

        mOffTimes = findViewById(R.id.offTimes);
        mOffTimes.setText("200");

        mVolume = findViewById(R.id.volume);
        mVolume.setText("4");

        mShowResultTv = findViewById(R.id.function_return_result_tv);
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
            case R.id.set_buzzer_frequency: {
                setBuzzerFrequency();
            }
            break;
            case R.id.set_buzzer_status: {
                buzzerBeep();
            }
            break;
            case R.id.set_volume: {
                setVolume();
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

    void setVolume() {
        int vol;
        int ret;

        try {
            String str = mVolume.getText().toString();
            vol = (int) Long.parseLong(str);

            ret = buzzer.setSystemVolume(vol);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("setVolume failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            logMsg("setVolume success\n");
        } catch (Exception e) {
            logMsg("Exception " + e.toString() + "\n");
        }
    }

    /**
     * Set the buzzer frequency.
     */
    void setBuzzerFrequency() {
        int frequency;
        int ret;

        try {
            String str = mFrequency.getText().toString();

            frequency = (int) Long.parseLong(str);

            if (frequency < 500 || frequency > 6000) {
                logMsg("Set fail, parameter invalid (500-6k)" + String.format(" frequency = %d\n", frequency));
                return;
            }

            ret = buzzer.setBuzzerFrequency(frequency);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("setBuzzerFrequency failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            logMsg("setBuzzerFrequency success\n");
        } catch (Exception e) {
            logMsg("Exception " + e.toString() + "\n");
        }
    }

    /**
     * Example of invoking a buzzer buzzer interface.
     */
    void buzzerBeep() {
        mSetBuzzerState.setEnabled(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                int times, onTimes, offtimes;
                int ret;

                try {
                    String str = mTimes.getText().toString();

                    times = (int) Long.parseLong(str);
                    if (times < 1) {
                        logMsg("Parameter invalid " + String.format(" times = %d\n", times));
                        return;
                    }

                    str = mOnTimes.getText().toString();
                    onTimes = (int) Long.parseLong(str);
                    if (onTimes < 0) {
                        logMsg("Parameter invalid " + String.format(" onTimes = %d\n", onTimes));
                        return;
                    }

                    str = mOffTimes.getText().toString();
                    offtimes = (int) Long.parseLong(str);
                    if (offtimes < 0) {
                        logMsg("Parameter invalid " + String.format(" offtimes = %d\n", offtimes));
                        return;
                    }

                    ret = buzzer.beep(times, onTimes, offtimes, BuzzerMode.BUZZER_MODE_SYNC);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("setBuzzerFrequency failed" + String.format(" errCode = 0x%x\n", ret));
                        return;
                    }

                    logMsg("buzzerBeep success\n");
                } catch (Exception e) {
                    logMsg("Exception " + e.toString() + "\n");
                } finally {
                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                }
            }
        }).start();
    }
}
