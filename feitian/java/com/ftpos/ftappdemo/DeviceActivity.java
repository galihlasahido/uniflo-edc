package com.ftpos.ftappdemo;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ftpos.library.smartpos.device.Device;
import com.ftpos.library.smartpos.errcode.ErrCode;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DeviceActivity extends BaseActivity {

    private final String TAG = DeviceActivity.class.getSimpleName();

    private TextView mShowResultTv;

    private Device device;
    private int selectedDeviceTypePosition = 0;
    private int selectedDeviceStatePosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        initView();

        this.device = MainActivity.device;
    }

    private void initView() {
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText(R.string.Device_View);
        findViewById(R.id.ukey_navbar_left_btn).setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        findViewById(R.id.get_device_info).setOnClickListener(this);
        findViewById(R.id.set_power_control).setOnClickListener(this);
        mShowResultTv = findViewById(R.id.function_return_result_tv);

        Spinner spDeviceType = findViewById(R.id.sp_device_type);
        Spinner spDeviceState = findViewById(R.id.sp_device_state);


        spDeviceType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                logMsg("DeviceType:" + position);
                selectedDeviceTypePosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spDeviceState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                logMsg("DeviceState:" + position);
                selectedDeviceStatePosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.ukey_navbar_left_btn) {
            finish();
        } else if (id == R.id.ukey_navbar_right_btn) {
            mShowResultTv.setText("");
        } else if (id == R.id.get_device_info) {
            getDeviceInfo();
        } else if (id == R.id.set_power_control) {
            setPowerControl();
        }
    }

    public synchronized void logMsg(String msg) {
        runOnUiThread(() -> {
            String oldMsg = mShowResultTv.getText().toString();
            mShowResultTv.setText(oldMsg + "\n" + msg);
        });
    }

    /**
     * Get device information and display it.
     */
    void getDeviceInfo() {
        String str;
        StringBuilder display;

        str = device.getHardwareVersion();
        display = new StringBuilder("HardwareVersion:" + str + "\n");

        str = device.getPosServerVersion();
        display.append("PosServerVersion:").append(str).append("\n");

        str = device.getSDKVersionName();
        display.append("SDKVersionName:").append(str).append("\n");

        str = device.getSecureFirmwareVersion();
        display.append("SecureFirmwareVersion:").append(str).append("\n");

        str = device.getSerialNumber();
        display.append("SerialNumber:").append(str).append("\n");

        str = device.getProductModel();
        display.append("ProductModel:").append(str).append("\n");

        //get emv kernel version.
        display.append("EMVKernelNameVersion:\n");
        Map<String, String> map = new HashMap<>();
        map = device.getEMVKernelNameVersion();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            display.append("    ").append(entry.getKey()).append(" :").append(entry.getValue()).append("\n");
        }

        //get system modules version.
        display.append("SystemModulesVersion:\n");
        map = device.getSystemModulesVersion();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            display.append("    ").append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
        }

        logMsg("getDeviceInfo success\n" + display);
    }

    public String getSystemModel() {
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method getter = clazz.getDeclaredMethod("get", String.class);
            String value = (String) getter.invoke(null, "ro.ft.product.model");
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
            value = (String) getter.invoke(null, "ro.product.model");
            if (!TextUtils.isEmpty(value)) {
                return value;
            }
        } catch (Exception e) {
            Log.e("SDK", "getSystemProperty: Unable to read system properties");
            Log.e(TAG, "getSystemModel: ", e);
        }
        return android.os.Build.MODEL;
    }

    private void setPowerControl() {
        int ret = device.setPowerControl(selectedDeviceTypePosition, selectedDeviceStatePosition);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("Power control success\n");
        } else {
            logMsg("Power control fail: " + ret + " " + ErrCode.toString(ret) + "\n");
        }
    }
}
