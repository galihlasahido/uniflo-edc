package com.ftpos.ftappdemo;

import static com.ftpos.library.smartpos.errcode.ErrCode.ERR_SUCCESS;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.ftpos.library.smartpos.buzzer.Buzzer;
import com.ftpos.library.smartpos.crypto.Crypto;
import com.ftpos.library.smartpos.device.Device;
import com.ftpos.library.smartpos.emv.Emv;
import com.ftpos.library.smartpos.icreader.IcReader;
import com.ftpos.library.smartpos.keymanager.KeyManager;
import com.ftpos.library.smartpos.led.Led;
import com.ftpos.library.smartpos.magreader.MagReader;
import com.ftpos.library.smartpos.memoryreader.MemoryReader;
import com.ftpos.library.smartpos.nfcreader.NfcReader;
import com.ftpos.library.smartpos.printer.Printer;
import com.ftpos.library.smartpos.psamreader.PsamReader;
import com.ftpos.library.smartpos.serialport.SerialPort;
import com.ftpos.library.smartpos.servicemanager.OnServiceConnectCallback;
import com.ftpos.library.smartpos.servicemanager.ServiceManager;

import java.lang.reflect.Method;


public class MainActivity extends BaseActivity {

    private Context mContext;

    private TextView mShowResultTv;

    public static KeyManager keyManager = null;
    public static Led led = null;
    public static Buzzer buzzer = null;
    public static PsamReader psamReader = null;
    public static NfcReader nfcReader = null;
    public static IcReader icReader = null;
    public static MagReader magReader = null;
    public static Printer printer = null;
    public static Device device = null;
    public static Crypto crypto = null;
    public static MemoryReader memoryReader = null;
    public static Emv emv = null;
    public static SerialPort serialport = null;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getDeviceModel();
        Log.d("getDeviceModel", "mDeviceModel:" + mDeviceModel);

        mContext = this;

        ServiceManager.bindPosServer(this, new OnServiceConnectCallback() {
            @Override
            public void onSuccess() {
                led = Led.getInstance(mContext);
                buzzer = Buzzer.getInstance(mContext);
                psamReader = PsamReader.getInstance(mContext);
                nfcReader = NfcReader.getInstance(mContext);
                icReader = IcReader.getInstance(mContext);
                magReader = MagReader.getInstance(mContext);
                printer = Printer.getInstance(mContext);
                device = Device.getInstance(mContext);
                crypto = Crypto.getInstance(mContext);
                memoryReader = MemoryReader.getInstance(mContext);

                keyManager = KeyManager.getInstance(mContext);
                emv = Emv.getInstance(mContext);
                serialport = SerialPort.getInstance(mContext);
                String packageName = getApplicationContext().getPackageName();

                if (mDeviceModel != DEVICE_MODE_UNKOWN && mDeviceModel != DEVICE_MODE_F100) {
                    //After connecting to the Service, it must be called once to init the KeyManager, no need to call repeatedly
                    int ret = keyManager.setKeyGroupName(packageName);
                    if (ret != ERR_SUCCESS) {
                        logMsg("setKeyGroupName(" + packageName + String.format(")failed,  errCode =0x%x", ret));
                    }
                }
            }

            @Override
            public void onFail(int var1) {
                Log.e("binding", "onFail");
            }
        });


        initView();

        mShowResultTv.setText("");
        logMsg("AppVersion: " + BuildConfig.VERSION_NAME);
        logMsg("AppBuildData: " + BuildConfig.BUILD_TIMESTAMP);
        //logMsg("LibVersion: " + strLibVer);

        try {
            //Check if write permission is available.
            int permission = ActivityCompat.checkSelfPermission(this,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // No permission to write, to apply for permission to write, will pop up a dialog box.
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final static int DEVICE_MODE_UNKOWN = -1;
    public final static int DEVICE_MODE_F100 = 1;
    public final static int DEVICE_MODE_F200 = 0;
    public final static int DEVICE_MODE_F600_300 = 2;
    private static int mDeviceModel = DEVICE_MODE_UNKOWN;

    public static int getDeviceModel() {
        if (mDeviceModel == DEVICE_MODE_UNKOWN) {
//            String deviceModel = android.os.Build.MODEL;
//            String deviceModel = getSystemProperty("ro.product.model", "null");
            String deviceModel = getSystemModel();
            if (deviceModel.equals("F100") || android.os.Build.MODEL.equals("full_k61v1_32_bsp_1g")) {
                mDeviceModel = DEVICE_MODE_F100;
            } else if (deviceModel.equals("F300") || deviceModel.equals("F600")) {
                mDeviceModel = DEVICE_MODE_F600_300;
            } else {
                mDeviceModel = DEVICE_MODE_F200;
            }
        }
        return mDeviceModel;
    }

    public static String getSystemModel() {
        try {

            Class clazz = Class.forName("android.os.SystemProperties");
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
        }

        return android.os.Build.MODEL;
    }


    private void initView() {
        Button mExit = findViewById(R.id.ukey_navbar_left_btn);
        mExit.setText("Exit");
        mExit.setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText("Main View");
        RelativeLayout mKeyManagement = (RelativeLayout) findViewById(R.id.key_management);
        mKeyManagement.setOnClickListener(this);
        RelativeLayout mBuzzer = findViewById(R.id.Buzzer);
        mBuzzer.setOnClickListener(this);
        RelativeLayout mLed = findViewById(R.id.Led);
        mLed.setOnClickListener(this);
        RelativeLayout mRingLight = findViewById(R.id.ring_light);
        mRingLight.setOnClickListener(this);
        if ("F360".equals(Build.MODEL)) {
            mLed.setVisibility(View.GONE);
        } else {
            mRingLight.setVisibility(View.GONE);
        }
        RelativeLayout mPsam = findViewById(R.id.Psam);
        mPsam.setOnClickListener(this);
        RelativeLayout mPrinter = findViewById(R.id.Printer);
        mPrinter.setOnClickListener(this);
        RelativeLayout mDevice = findViewById(R.id.Device);
        mDevice.setOnClickListener(this);
        RelativeLayout mCrypto = findViewById(R.id.Crypto);
        mCrypto.setOnClickListener(this);
        RelativeLayout mMagCard = findViewById(R.id.Mag_card);
        mMagCard.setOnClickListener(this);
        RelativeLayout mIcCard = findViewById(R.id.Ic_card);
        mIcCard.setOnClickListener(this);
        RelativeLayout mNfcCard = findViewById(R.id.Nfc_card);
        mNfcCard.setOnClickListener(this);
        RelativeLayout mMemoryCard = findViewById(R.id.Memory_card);
        mMemoryCard.setOnClickListener(this);
        RelativeLayout mDukpt = findViewById(R.id.key_dukpt_demo);
        mDukpt.setOnClickListener(this);
        RelativeLayout mSerialPort = findViewById(R.id.serial_port_demo);
        mSerialPort.setOnClickListener(this);

        RelativeLayout btscreen = findViewById(R.id.btscreen_demo);
        btscreen.setOnClickListener(this);

        if (mDeviceModel == DEVICE_MODE_F100) {
            mKeyManagement.setVisibility(View.GONE);
            mBuzzer.setVisibility(View.GONE);
            mLed.setVisibility(View.GONE);
            mDevice.setVisibility(View.GONE);
            mCrypto.setVisibility(View.GONE);
            mMagCard.setVisibility(View.GONE);
            mIcCard.setVisibility(View.GONE);
            mNfcCard.setVisibility(View.GONE);
            mMemoryCard.setVisibility(View.GONE);
            mDukpt.setVisibility(View.GONE);
        }

        mShowResultTv = findViewById(R.id.function_return_result_tv);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.key_management: {
                Intent intent = new Intent(this, KeyManagerActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.Buzzer: {
                Intent intent = new Intent(this, BuzzerActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.Led: {
                Intent intent = new Intent(this, LedActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.ring_light: {
                Intent intent = new Intent(this, RingLightActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.Psam: {
                Intent intent = new Intent(this, PsamActivity.class);
                intent.putExtra("DeviceModel", mDeviceModel);
                startActivity(intent);
            }
            break;
            case R.id.Printer: {

                if (mDeviceModel == DEVICE_MODE_F600_300) {
                    logMsg("Function not supported\n");
                    break;
                }
                Intent intent = new Intent(this, PrinterActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.Device: {
                Intent intent = new Intent(this, DeviceActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.Crypto: {
                Intent intent = new Intent(this, CryptoActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.Mag_card: {
                Intent intent = new Intent(this, MagCardActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.Ic_card: {
                Intent intent = new Intent(this, IcCardActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.Nfc_card: {
                Intent intent = new Intent(this, NfcCardActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.Memory_card: {
                Intent intent = new Intent(this, MemoryCardActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.key_dukpt_demo: {
                Intent intent = new Intent(this, DukptActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.serial_port_demo: {
                Intent intent = new Intent(this, SerialPortActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.ukey_navbar_right_btn: {
                mShowResultTv.setText("");
            }
            break;
            case R.id.btscreen_demo: {
                Intent intent = new Intent(this, BtScreenActivity.class);
                startActivity(intent);
            }
            break;
            case R.id.ukey_navbar_left_btn: {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("prompt")
                        .setMessage("Do you want to leave?")
                        .setPositiveButton("YES", (dialog, which) -> finish())
                        .setNegativeButton("NO", (dialog, which) -> {
                        }).show();
            }
            break;
            default:
                break;
        }

    }

    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(),
                                "Please press again to exit", Toast.LENGTH_SHORT)
                        .show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public synchronized void logMsg(String msg) {
        String oldMsg = mShowResultTv.getText().toString();
        mShowResultTv.setText(oldMsg + "\n" + msg);
    }


}
