package com.ftpos.ftappdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ftpos.library.smartpos.bluetoothDock.BluetoothDock;
import com.ftpos.library.smartpos.bluetoothDock.OnBluetoothCallback;
import com.ftpos.library.smartpos.bluetoothDock.OnResponseCallback;
import com.ftpos.library.smartpos.bluetoothDock.OnScanBlueCallback;
import com.ftpos.library.smartpos.bluetoothDock.OnUpGradeCallback;
import com.ftpos.library.smartpos.util.BytesUtils;

import java.util.ArrayList;

public class BtScreenActivity extends BaseActivity {

    private ProgressDialog progressDialog = null;
    private TextView mShowResultTv;
    private EditText amount;
    private EditText balance;
    private BluetoothDock bluetoothDock;
    private boolean connectResult = false;
    private String[] data = new String[]{"00", "01", "02", "03"};
    private String[] data1 = new String[]{"00", "01"};
    private ArrayList<String> data2 = new ArrayList<>();
    private int positions = 0;
    private int positions1 = 0;
    private int positions2 = 0;
    private int tiems = 0;


    private final String TAG = BtScreenActivity.class.getSimpleName();
    private Spinner spinner, spinner1, mac_id;
    private ArrayAdapter<String> adapter2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btscreen);
        initView();

        bluetoothDock = BluetoothDock.getInstance(this);
        progressDialog = new ProgressDialog(this);
    }

    private void initView() {
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText("BtScreen View");
        findViewById(R.id.displayamount).setOnClickListener(this);
        findViewById(R.id.scanBluetoohDock).setOnClickListener(this);
        findViewById(R.id.connectBlueTooth).setOnClickListener(this);
        findViewById(R.id.getID).setOnClickListener(this);
        findViewById(R.id.upGrade).setOnClickListener(this);
        findViewById(R.id.setBacklight).setOnClickListener(this);
        findViewById(R.id.restorationScreen).setOnClickListener(this);
        findViewById(R.id.disconnectBluetooth).setOnClickListener(this);
        mac_id = findViewById(R.id.mac_id);
        adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, data2);
        mac_id.setAdapter(adapter2);
        amount = findViewById(R.id.amount);
        balance = findViewById(R.id.balance);
        findViewById(R.id.ukey_navbar_left_btn).setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, data);
        spinner.setAdapter(adapter);
        //spinner.setSelection(1);
        spinner.setPrompt("Brightness mode");
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: =============== position:" + position);
                positions = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinner1 = findViewById(R.id.spinner1);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, data1);
        spinner1.setAdapter(adapter1);
        //spinner.setSelection(1);
        spinner1.setPrompt("Brightness mode");
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: =============== position:" + position);
                positions1 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mShowResultTv = findViewById(R.id.function_return_result_tv);
        mac_id.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: =============== position:" + position);
                positions2 = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ukey_navbar_left_btn: {
                finish();
            }
            break;
            case R.id.ukey_navbar_right_btn: {
                mShowResultTv.setText("");
            }
            break;
            case R.id.displayamount: {
                if (!connectResult) {
                    Toast.makeText(this, "Please connect Bluetooth first", Toast.LENGTH_SHORT).show();
                    return;
                }
                String amountText = amount.getText().toString().replace(".", "");
                String balancetext = balance.getText().toString().replace(".", "");
                if (TextUtils.isEmpty(amountText) || TextUtils.isEmpty(balancetext)) {
                    Toast.makeText(this, "Please enter the amount and balance", Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] amountA = formatStr(amountText);
                byte[] balanceB = formatStr(balancetext);
                if (amountA == null || balanceB == null) {
                    Toast.makeText(this, "Amount input error", Toast.LENGTH_SHORT).show();
                    return;
                }
                bluetoothDock.displayAmount(00, 20, amountA, balanceB, new OnResponseCallback() {
                    @Override
                    public void onResponseResult(String s, String s1, String s2) {
                        Log.d(TAG, "onResponseResult  ggggs: " + s);
                        Log.d(TAG, "onResponseResult  gggs1: " + s1);
                        Log.d(TAG, "onResponseResult  ggggs2: " + s2);
                    }
                });
            }
            break;
            case R.id.scanBluetoohDock: {
                showDialog();
                data2.clear();
                bluetoothDock.scanBluetoohDock(new OnScanBlueCallback() {
                    @Override
                    public void onScanResult(String name, String address) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mShowResultTv.setText(name + "-------" + address);
                                adapter2.add(address);
                            }
                        });

                        Log.d(TAG, "onScanResult  name: " + name);
                        Log.d(TAG, "onScanResult  name: " + address);
                    }
                });
            }
            break;
            case R.id.connectBlueTooth: {
                if (data2 == null || data2.isEmpty()) {
                    Toast.makeText(this, "No connectable device found,Please scan the bluetooth device first", Toast.LENGTH_SHORT).show();
                    return;
                }
                String mac = data2.get(positions2);
                bluetoothDock.connectBlueTooth(mac, new OnBluetoothCallback() {
                    @Override
                    public void onConnectSuccess() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mShowResultTv.setText("connect success");
                            }
                        });

                        connectResult = true;
                    }

                    @Override
                    public void onConnectFailed(String s, int i) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mShowResultTv.setText("connect fail");
                            }
                        });
                        connectResult = false;
                    }

                    @Override
                    public void onReceive(int i, String s, String s1) {

                    }
                });
            }
            break;
            case R.id.getID: {
                if (!connectResult) {
                    Toast.makeText(this, "Please connect Bluetooth first", Toast.LENGTH_SHORT).show();
                    return;
                }
                bluetoothDock.getID(new OnResponseCallback() {
                    @Override
                    public void onResponseResult(String s, String s1, String s2) {
                        Log.d(TAG, "onResponseResult  $$$$s: " + s);
                        Log.d(TAG, "onResponseResult  ￥￥￥￥1: " + s1);
                        Log.d(TAG, "onResponseResult  %%%%s2: " + s2);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (TextUtils.isEmpty(s2)) {
                                    mShowResultTv.setText("ID:");
                                    return;
                                }
                                mShowResultTv.setText("ID:" + s2.substring(s2.length() - 12));
                            }
                        });
                    }
                });
            }
            break;
            case R.id.restorationScreen: {
                if (!connectResult) {
                    Toast.makeText(this, "Please connect Bluetooth first", Toast.LENGTH_SHORT).show();
                    return;
                }
                bluetoothDock.restorationScreen(Byte.parseByte(data1[positions1]), new OnResponseCallback() {
                    @Override
                    public void onResponseResult(String s, String s1, String s2) {
                        Log.d(TAG, "onResponseResult  !!!s: " + s);
                        Log.d(TAG, "onResponseResult  !!!s1: " + s1);
                        Log.d(TAG, "onResponseResult  !!!s2: " + s2);
                    }
                });


            }
            break;
            case R.id.disconnectBluetooth: {
                if (connectResult) {
                    bluetoothDock.disconnectBluetooth();
                    connectResult = false;
                }
            }
            break;
            case R.id.setBacklight: {
                if (!connectResult) {
                    Toast.makeText(this, "Please connect Bluetooth first", Toast.LENGTH_SHORT).show();
                    return;
                }
                bluetoothDock.setBacklight(Byte.parseByte(data[positions]), new OnResponseCallback() {
                    @Override
                    public void onResponseResult(String s, String s1, String s2) {
                        Log.d(TAG, "onResponseResult  @@@s: " + s);
                        Log.d(TAG, "onResponseResult  @@@s1: " + s1);
                        Log.d(TAG, "onResponseResult  @@@@s2: " + s2);
                    }
                });
            }
            break;
            case R.id.upGrade: {
                showProgressDialog();
                progressDialog.setProgress(0);
                if (!connectResult) {
                    Toast.makeText(this, "Please connect Bluetooth first", Toast.LENGTH_SHORT).show();
                    return;
                }
                byte[] bytes = BytesUtils.hexStringToBytes("0A9DAC7F4CC08B6F5D9874C0BF74F7430088FD6B0F49174777E53B705407A9278773DD21A31F88D391A0966F163E444ED99CE805E95636B9669357FE531EB79B");
                bluetoothDock.upGrade(bytes, new OnUpGradeCallback() {
                    @Override
                    public void onResponseResult(String s, String s1, String s2) {
                        Log.d(TAG, "onResponseResult  ========s: " + s);
                        Log.d(TAG, "onResponseResult ========== s1: " + s1);
                        Log.d(TAG, "onResponseResult  =============s2: " + s2);
                        if (!s.equals("9000")) {
                            if (progressDialog != null) {
                                progressDialog.cancel();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mShowResultTv.setText("failed");
                                }
                            });
                        }


                    }

                    @Override
                    public void onProgress(int i) {
                        Log.d(TAG, "onProgress  i: " + i);
                        progressDialog.setProgress(i);
                        if (i == 100) {
                            Log.d(TAG, "onProgress  ==============i: " + i);
                            progressDialog.cancel();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mShowResultTv.setText("Upgrade Successful");
                                }
                            });
                        }
                    }
                });
            }
            break;
            default:
                break;
        }

    }

    private byte[] formatStr(String amountText) {

        try {
            String hexString = Integer.toHexString(Integer.parseInt(amountText));
            String format = String.format("%8s", hexString).replace(" ", "0");
            byte[] bytes = BytesUtils.hexStringToBytes(format);
            Log.d(TAG, "balancetext bytes: " + bytes.length);
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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


    /**
     * 进度条dailog
     *
     * @setProgress 设置初始进度
     * @setProgressStyle 设置样式（水平进度条）
     * @setMax 设置进度最大值
     */
    public void showProgressDialog() {
        final int MAX_PROGRESS = 100;
        progressDialog.setProgress(0);
        progressDialog.setTitle("Up grade");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(MAX_PROGRESS);
        progressDialog.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bluetoothDock.disconnectBluetooth();
    }

    private void showDialog(){
        ProgressDialog pgd;
        pgd = new ProgressDialog(this);
        pgd.setCancelable(false);
        pgd.setMessage("Please wait...");
        if (!pgd.isShowing()) {
            pgd.show();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(6000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                pgd.dismiss();
            }
        }).start();
    }
}