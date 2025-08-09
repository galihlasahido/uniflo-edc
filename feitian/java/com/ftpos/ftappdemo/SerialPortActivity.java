package com.ftpos.ftappdemo;

import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import com.ftpos.library.smartpos.serialport.SerialPort;
import com.ftpos.library.smartpos.util.BytesUtils;

public class SerialPortActivity extends BaseActivity {

    private SerialPort serialPort = null;

    private TextView mShowResultTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_port);
        initView();
        this.serialPort = MainActivity.serialport;
    }

    private void initView() {
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText("Serial port View");
        findViewById(R.id.ukey_navbar_left_btn).setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        findViewById(R.id.rl_serialport_open).setOnClickListener(this);
        findViewById(R.id.rl_serialport_send).setOnClickListener(this);
        findViewById(R.id.rl_serialport_receive).setOnClickListener(this);
        findViewById(R.id.rl_serialport_close).setOnClickListener(this);

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
            case R.id.rl_serialport_open: {
                try {
                    String portPath = serialPort.getPortPath();
                    logMsg("portPathP:->" + portPath);
                    serialPort.open(portPath, 115200);
                    logMsg("open success");
                } catch (Exception e) {
                    e.printStackTrace();
                    logMsg("open fail");

                }
            }
            break;
            case R.id.rl_serialport_send: {
                new Thread(() -> {
                    try {
                        byte[] writeBytes = BytesUtils.hexStringToBytes("112233445566778899AABBCCDDEEFF");
                        serialPort.write(writeBytes, writeBytes.length);
                        logMsg("write success");
                    } catch (Exception e) {
                        e.printStackTrace();
                        logMsg("write fail");
                    }
                }).start();
            }
            break;
            case R.id.rl_serialport_receive: {
                new Thread(() -> {
                    try {
                        byte[] bytesValue = new byte[1024];
                        while (true) {
                            int length = serialPort.read(bytesValue, 500);
                            if (length > 0) {
                                byte[] dispValue = new byte[length];
                                System.arraycopy(bytesValue, 0, dispValue, 0, length);
                                logMsg("read success:->" + BytesUtils.byte2HexStr(dispValue));
                                break;
                            }
                            SystemClock.sleep(500);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        logMsg("read fail");
                    }
                }).start();
            }
            break;
            case R.id.rl_serialport_close: {
                try {
                    serialPort.close();
                    logMsg("close success");
                } catch (Exception e) {
                    e.printStackTrace();
                    logMsg("close fail");
                }
            }
            break;
            default:
                break;
        }
    }

    public synchronized void logMsg(String msg) {
        runOnUiThread(() -> {
            String oldMsg = mShowResultTv.getText().toString();
            mShowResultTv.setText(oldMsg + "\n" + msg);
        });
    }
}
