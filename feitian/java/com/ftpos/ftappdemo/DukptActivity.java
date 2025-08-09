package com.ftpos.ftappdemo;

import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ftpos.library.smartpos.datautils.BytesTypeValue;
import com.ftpos.library.smartpos.emv.Emv;
import com.ftpos.library.smartpos.emv.IPinBlockFormat;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.keymanager.AlgName;
import com.ftpos.library.smartpos.keymanager.EncryptionMode;
import com.ftpos.library.smartpos.keymanager.EncryptionPaddingMode;
import com.ftpos.library.smartpos.keymanager.KeyManager;
import com.ftpos.library.smartpos.keymanager.KeyType;
import com.ftpos.library.smartpos.keymanager.MacMode;
import com.ftpos.library.smartpos.pin.OnPinInputListener;
import com.ftpos.library.smartpos.pin.PinSeting;
import com.ftpos.library.smartpos.util.BytesUtils;

import java.util.Arrays;

public class DukptActivity extends BaseActivity {

    private TextView mShowResultTv;
    private Dialog mPinPadDialog;
    private KeyManager keyManager;
    private Emv emv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dukpt);
        initView();
        this.keyManager = MainActivity.keyManager;
        this.emv = MainActivity.emv;

    }

    private void initView() {
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText("Dukpt View");
        findViewById(R.id.ukey_navbar_left_btn).setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        findViewById(R.id.register).setOnClickListener(this);
        findViewById(R.id.rl_install_ipek).setOnClickListener(this);
        findViewById(R.id.rl_calculate_pin).setOnClickListener(this);
        findViewById(R.id.rl_calculate_mac).setOnClickListener(this);
        findViewById(R.id.rl_encrypt_data).setOnClickListener(this);
        findViewById(R.id.rl_add_ksn).setOnClickListener(this);
        mShowResultTv = findViewById(R.id.function_return_result_tv);
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
            case R.id.register: {
                initKeyManager();
            }
            break;
            case R.id.rl_install_ipek: {
                installIpekTest();
            }
            break;
            case R.id.rl_calculate_pin: {
                inputPIN();
            }
            break;
            case R.id.rl_calculate_mac: {
                calculateMac();
            }
            break;
            case R.id.rl_encrypt_data: {
                encryptData();
            }
            break;
            case R.id.rl_add_ksn: {
                ipekAddKsnTest();
            }
            break;
            default:
                break;
        }

    }

    /**
     * PIN Input Demo
     *
     * @return 0: Success; Other values: Error code.
     */
    int inputPIN() {
        int ret;
        int ipekKeyIndex = 0x01;
        //The current KSN is displayed
        ipekKeyIndex = 0x01;
        BytesTypeValue ksnBytesTypeValue = new BytesTypeValue();
        ret = keyManager.exportDukptKsn(KeyType.KEY_TYPE_IPEK, ipekKeyIndex, ksnBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            // After import , exportDukptKsn will be FFFF9876543210E00001(hex format)
            logMsg("Current KSN:" + BytesUtils.byte2HexStr(ksnBytesTypeValue.getData()));
        }

        //The following start the PINPAD interface
        LinearLayout keyboard;
        Button mBtnCancel;
        View mLlDelete;
        Button mTvOK;
        Button[] mTvDigits;
        if (mPinPadDialog == null) {
            mPinPadDialog = new Dialog(DukptActivity.this, R.style.BaseDialog);
        }
        View view = LayoutInflater.from(this).inflate(R.layout.layout_pin_fetian, null);//haozi  for TextView layout test
        mTvDigits = new Button[10];
        mTvDigits[0] = (Button) view.findViewById(R.id.button0);
        mTvDigits[1] = (Button) view.findViewById(R.id.button1);
        mTvDigits[2] = (Button) view.findViewById(R.id.button2);
        mTvDigits[3] = (Button) view.findViewById(R.id.button3);
        mTvDigits[4] = (Button) view.findViewById(R.id.button4);
        mTvDigits[5] = (Button) view.findViewById(R.id.button5);
        mTvDigits[6] = (Button) view.findViewById(R.id.button6);
        mTvDigits[7] = (Button) view.findViewById(R.id.button7);
        mTvDigits[8] = (Button) view.findViewById(R.id.button8);
        mTvDigits[9] = (Button) view.findViewById(R.id.button9);

        keyboard = (LinearLayout) view.findViewById(R.id.container);
        mBtnCancel = (Button) view.findViewById(R.id.btn_cancel);
        mLlDelete = (View) view.findViewById(R.id.btn_clean);
        mTvOK = (Button) view.findViewById(R.id.btn_confirm);

        mPinPadDialog.setContentView(view);

        PinSeting pinSeting = new PinSeting(this, emv);
        pinSeting.setButtonCancel(mBtnCancel);
        pinSeting.setButtonNum(mTvDigits);
        pinSeting.setButtonDel(mLlDelete);
        pinSeting.setButtonOK(mTvOK);
        pinSeting.setButtonKeyboard(keyboard);

        pinSeting.setOnlinePinBlockFormat(IPinBlockFormat.BLOCK_FORMAT_0);  //Format 0
        pinSeting.setOnlinePinKeyIndex(ipekKeyIndex);       //Key index
        pinSeting.setOnlinePinKeyType(KeyType.KEY_TYPE_IPEK);       //Compared with other manufacturer codes
        pinSeting.setTimeout(30);
        pinSeting.setOnlinePinByPass(false);

        String pan = "4012345678909";
        pinSeting.setPan(pan);
        logMsg("Assumed Primary Account Number:" + pan);
        Window dialogWindow = mPinPadDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.x = 0;
        lp.y = 0;
        view.measure(0, 0);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        lp.height = view.getMeasuredHeight();
        lp.alpha = 0.8f;
        dialogWindow.setAttributes(lp);
        mPinPadDialog.setOnShowListener(dialog -> emv.StartPinInput(pinSeting, new OnPinInputListener() {
            @Override
            public void onDispalyPin(int i, int i1) {

            }

            @Override
            public void onSuccess(byte[] bytes) {
                logMsg(String.format("PIN:\r%s", BytesUtils.byte2HexStr(bytes)));
                logMsg("---------- StartPinInput Success ----------\n");
                mPinPadDialog.dismiss();
            }

            @Override
            public void onError(int i) {
                logMsg(String.format("---------- StartPinInput Error[%d] ----------\n", i));
                mPinPadDialog.dismiss();
            }

            @Override
            public void onTimeout() {
                logMsg("---------- StartPinInput Timeout ----------\n");
                mPinPadDialog.dismiss();
            }

            @Override
            public void onCancel() {
                logMsg("---------- StartPinInput Cancel ----------\n");
                mPinPadDialog.dismiss();
            }

            @Override
            public void onSetDigits(Object o, char c) {

            }
        }));
        mPinPadDialog.show();
        return ErrCode.ERR_SUCCESS;
    }

    /**
     * Calculate Mac Demo
     *
     * @return 0: Success; Other values: Error code.
     */
    int calculateMac() {
        int ret;
        int ipekKeyIndex;

        ipekKeyIndex = 0x01;
        BytesTypeValue ksnBytesTypeValue = new BytesTypeValue();
        ret = keyManager.exportDukptKsn(KeyType.KEY_TYPE_IPEK, ipekKeyIndex, ksnBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            // After import , exportDukptKsn will be FFFF9876543210E00001(hex format)
            logMsg("Current KSN:" + BytesUtils.byte2HexStr(ksnBytesTypeValue.getData()));
        }

        byte[] initVector = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,};
        String ascii = "4012345678909D987";
        String hex = "343031323334353637383930394439383700000000000000";
        byte[] data = ascii.getBytes();
        //data = BytesUtils.hexStringToBytes(hex);
        logMsg(String.format("MAC and Data Encryption Input (ASCII):\r%s", ascii));
        /**
         * macByIndex(IPEK) test
         */
        BytesTypeValue macBytesTypeValue = new BytesTypeValue();
        ret = keyManager.macByIndex(KeyType.KEY_TYPE_IPEK, ipekKeyIndex, AlgName.SYM_ARITH_3DES, initVector, initVector.length,
                data, data.length, MacMode.MAC_MODE_9797_1_M1, 1, macBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            // After import, First use
            //current ksn:FFFF9876543210E00001(hex format)
            //current dukpt MEK_REQ:042666B4918430A368DE9628D03984C9(hex format)
            //plaintext:4012345678909D987 (ascii format)
            //MAC (Request):9CCC78173FC4FB64(hex format,encryption key:dukpt MEK_P alg: 3desecb paddingMode:MAC_MODE_9797_1_M1)
            byte[] macData = macBytesTypeValue.getData();
            logMsg("MAC (Request):" + BytesUtils.byte2HexStr(macData));
            logMsg("---------- macByIndex Success ----------\n");

        } else {
            logMsg(String.format("macByIndex(ipek) fail errCode = 0x%x\n", ret));
        }

        macBytesTypeValue = new BytesTypeValue();
        ret = keyManager.macByIndex(KeyType.KEY_TYPE_IPEK, ipekKeyIndex, AlgName.SYM_ARITH_3DES, initVector, initVector.length,
                data, data.length, MacMode.MAC_MODE_9797_1_M1, 0, macBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            // After import, First use
            //current ksn:FFFF9876543210E00001(hex format)
            //current dukpt MEK_RES:042666B46E84CFA368DE96282F397BC9(hex format)
            //plaintext:4012345678909D987 (ascii format)
            //MAC (Response):20364223C1FF00FA(hex format,encryption key:dukpt MEK_P alg: 3desecb paddingMode:MAC_MODE_9797_1_M1)
            byte[] macData = macBytesTypeValue.getData();
            //logMsg("macByIndex(ipek) success:");
            logMsg("MAC (Response):" + BytesUtils.byte2HexStr(macData));
            logMsg("---------- macByIndex Success ----------\n");
        } else {
            logMsg(String.format("macByIndex(ipek) fail errCode = 0x%x\n", ret));
        }
        return ErrCode.ERR_SUCCESS;
    }


    /**
     * Encrypt Data Demo
     *
     * @return 0: Success; Other values: Error code.
     */
    int encryptData() {
        int ret;
        int ipekKeyIndex;

        ipekKeyIndex = 0x01;
        BytesTypeValue ksnBytesTypeValue = new BytesTypeValue();
        ret = keyManager.exportDukptKsn(KeyType.KEY_TYPE_IPEK, ipekKeyIndex, ksnBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            // After import , exportDukptKsn will be FFFF9876543210E00001(hex format)
            logMsg("Current KSN:" + BytesUtils.byte2HexStr(ksnBytesTypeValue.getData()));
        }

        byte[] initVector = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,};
        String ascii = "4012345678909D987";
        String hex = "343031323334353637383930394439383700000000000000";
        //Padding, PADDING_MODE_9797_1_M1
        byte[] data = ascii.getBytes();
        data = BytesUtils.hexStringToBytes(hex);
        logMsg(String.format("MAC and Data Encryption Input (ASCII):\r%s", ascii));

        int size = data.length;
        int allSize = size % 8 == 0 ? size : ((size / 8 + 1) * 8);
        byte[] paddingData = new byte[allSize];
        System.arraycopy(data, 0, paddingData, 0, size);
        for (int i = size; i < allSize; i++) {
            paddingData[i] = 0x00;
        }
        logMsg(String.format("MAC and Data Encryption Input (Hex):\r%s", BytesUtils.byte2HexStr(paddingData)));


        BytesTypeValue resultBytesTypeValue = new BytesTypeValue();
        ret = keyManager.symEncryptByIndex(KeyType.KEY_TYPE_IPEK, ipekKeyIndex, AlgName.SYM_ARITH_3DES, EncryptionMode.SYM_ARITH_CBC, initVector, initVector.length,
                paddingData, paddingData.length, EncryptionPaddingMode.PADDING_MODE_NONE, resultBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            // After import, First use
            //current ksn:FFFF9876543210E00001(hex format)
            //current dukpt DEK:448D3F076D8304036A55A3D7E0055A78(hex format)
            //plaintext:4012345678909D987 (ascii format)
            //encrypted result:FC0D53B7EA1FDA9E E68AAF2E70D9B950 6229BE2AA993F04F(hex format,encryption key:dukpt DEK_Q alg: 3des ecb paddingMode:MAC_MODE_9797_1_M1)
            byte[] encData = resultBytesTypeValue.getData();
            logMsg("Encrypted DATA:\n" + BytesUtils.byte2HexStr(encData));
            logMsg("---------- symEncryptByIndex Success ----------\n");

        } else {
            logMsg(String.format("symEncryptByIndex(ipek) fail errCode = 0x%x\n", ret));
        }
        return ErrCode.ERR_SUCCESS;
    }

    /**
     * Install plaintext a plaintext key
     *
     * @return 0: Success; Other values: Error code. Refer to{@link ErrCode}
     */
    int installIpekTest() {
        int ret;
        int ipekKeyIndex;

        /**
         * Install an IPEK
         *  ipek plaintext:6AC292FAA1315B4D858AB3A3D7D5933A (hex format)
         *  ipek kcv:AF8C07(hex format)
         */
        String ipek = "6AC292FAA1315B4D858AB3A3D7D5933A";
        String ksn = "FFFF9876543210E00000";

        byte[] ipekData = BytesUtils.hexStringToBytes(ipek);
        byte[] iKsnData = BytesUtils.hexStringToBytes(ksn);
        byte[] ipekKcv = BytesUtils.hexStringToBytes("AF8C07");

        logMsg(String.format("Initially Loaded Key Serial Number (KSN):\r%s", ksn));
        logMsg(String.format("Initially Loaded PIN Entry Device Key:\r%s", ipek));
        BytesTypeValue kcvBytesTypeValue = new BytesTypeValue();
        ipekKeyIndex = 0x01;
        ret = keyManager.loadDukptIpek(KeyType.KEY_TYPE_IPEK, ipekKeyIndex, AlgName.SYM_ARITH_3DES, 0x00, 0x00, ipekData, iKsnData, kcvBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            byte[] ipekCheckKcv = kcvBytesTypeValue.getData();
            if (Arrays.equals(ipekKcv, ipekCheckKcv)) {
                logMsg("---------- loadDukptIpek Success ----------\n");
            } else {
                logMsg("loadDukptIpek(plaintext) success but kcv check failed\n");
                return -1;
            }
        } else {
            logMsg(String.format("loadDukptIpek(plaintext) fail errCode =0x%x\n", ret));
            return -2;
        }
        return ErrCode.ERR_SUCCESS;
    }

    /**
     * Ipek Plaintext key added KSN.
     *
     * @return 0: Success; Other values: Error code. Refer to{@link ErrCode}
     */
    int ipekAddKsnTest() {
        int ret;
        int ipekKeyIndex;

        ipekKeyIndex = 0x01;
        BytesTypeValue bytesTypeValue = new BytesTypeValue();
        ret = keyManager.exportDukptKsn(KeyType.KEY_TYPE_IPEK, ipekKeyIndex, bytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("Current KSN:" + BytesUtils.byte2HexStr(bytesTypeValue.getData()));
        } else {
            logMsg(String.format("exportDukptKsn：failed！ errCode =0x%x", ret));
        }

        ret = keyManager.increaseKSN(KeyType.KEY_TYPE_IPEK, ipekKeyIndex);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("increaseKSN：failed！ errCode =0x%x", ret));
        } else {
            logMsg("---------- increaseKSN Success ----------\n");
        }

        byte[] ksn = bytesTypeValue.getData();
        ksn[ksn.length - 1] += 1;
        ret = keyManager.exportDukptKsn(KeyType.KEY_TYPE_IPEK, ipekKeyIndex, bytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            if (BytesUtils.byte2HexStr(ksn).equals(BytesUtils.byte2HexStr(bytesTypeValue.getData()))) {
                logMsg("Current KSN:" + BytesUtils.byte2HexStr(bytesTypeValue.getData()));
            } else {
                logMsg("exportDukptKsn success but data error！" + BytesUtils.byte2HexStr(bytesTypeValue.getData()) + "\n");
            }
        } else {
            logMsg(String.format("exportDukptKsn：failed！ errCode =0x%x\n", ret));
        }
        return ErrCode.ERR_SUCCESS;
    }

    /**
     * Set the name of the key group.
     * Keys installed with the same group name can be shared.
     *
     * @return 0: Success; Other values: Error code. Refer to{@link ErrCode}
     */
    int initKeyManager() {
        //After connecting to the Service, it must be called once to init the KeyManager, no need to call repeatedly
        //just test here
        String packageName = getApplicationContext().getPackageName();

        int ret = keyManager.setKeyGroupName(packageName);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("setKeyGroupName(" + packageName + String.format(")failed,  errCode =0x%x", ret));
        }
        logMsg("---------- setKeyGroupName Success ----------\n");
        return ret;
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