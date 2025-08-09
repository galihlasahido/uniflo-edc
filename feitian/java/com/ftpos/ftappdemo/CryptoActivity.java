package com.ftpos.ftappdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.ftpos.library.crypto.CryptoUtil;
import com.ftpos.library.smartpos.crypto.Crypto;
import com.ftpos.library.smartpos.datautils.BytesTypeValue;
import com.ftpos.library.smartpos.datautils.IntArrayTypeValue;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.keymanager.KeyManager;
import com.ftpos.library.smartpos.util.BytesUtils;

public class CryptoActivity extends BaseActivity {

    private EditText mRandomNum;
    private TextView mShowResultTv;
    private Crypto crypto;
    private KeyManager keyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crypto);

        initView();
        this.crypto = MainActivity.crypto;
        this.keyManager = MainActivity.keyManager;
    }

    private void initView() {
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText("Crypto View");
        findViewById(R.id.ukey_navbar_left_btn).setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        findViewById(R.id.get_rand).setOnClickListener(this);
        findViewById(R.id.enum_key_index).setOnClickListener(this);
        findViewById(R.id.crypto_hash).setOnClickListener(this);
        findViewById(R.id.crypto_sym).setOnClickListener(this);
        mShowResultTv = findViewById(R.id.function_return_result_tv);

        mRandomNum = findViewById(R.id.randomNum);
        mRandomNum.setText("8");

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
            case R.id.get_rand: {
                getRandNumDemo();
            }
            break;
            case R.id.enum_key_index: {
                enumKeyIndex();
            }
            break;
            case R.id.crypto_hash: {
                hashSample();
            }
            break;
            case R.id.crypto_sym: {
                symAlgoSample();
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

    void symAlgoSample() {
//        DES/3DES operation finished
//                ****************************************
//        Key:			3DAA54F3F34E5B48F09BE2AA2FF5CF2D
//        Algorithm:		3DES ECB
//        Crypto operation:	Encryption
//        Data:			42FCF05D26CE3C6C602D3E4F3F38634C
//        Padding Method:	None
//                ----------------------------------------
//        Encrypted data:	3847D2C9DBF4450C02D750BB7915BD4A
//        DES operations count:	6


//        DES/3DES operation finished
//                ****************************************
//        Key:			3DAA54F3F34E5B48F09BE2AA2FF5CF2D
//        Algorithm:		3DES ECB
//        Crypto operation:	Decoding
//        Data:			3847D2C9DBF4450C02D750BB7915BD4A
//        Padding Method:	None
//                ----------------------------------------
//        Decoded data:		42FCF05D26CE3C6C602D3E4F3F38634C
//        DES operations count:	6

//        AES operation finished
//                ****************************************
//        Key:			3DAA54F3F34E5B48F09BE2AA2FF5CF2D
//        Algorithm:		AES-128
//        Mode:			ECB
//        Crypto operation:	Encryption
//        Data:			42FCF05D26CE3C6C602D3E4F3F38634C
//
//                ----------------------------------------
//        Encrypted data:	A5D07C50821E0789BB569FBE142AD799
//
//
//        AES operation finished
//                ****************************************
//        Key:			3DAA54F3F34E5B48F09BE2AA2FF5CF2D
//        Algorithm:		AES-128
//        Mode:			ECB
//        Crypto operation:	Decoding
//        Data:			A5D07C50821E0789BB569FBE142AD799
//
//                ----------------------------------------
//        Decoded data:		42FCF05D26CE3C6C602D3E4F3F38634C
        int ret = 0;
        byte[] data = BytesUtils.hexStringToBytes("42FCF05D26CE3C6C602D3E4F3F38634C");
        byte[] key = BytesUtils.hexStringToBytes("3DAA54F3F34E5B48F09BE2AA2FF5CF2D");
        byte[] iv = BytesUtils.hexStringToBytes("00000000000000000000000000000000");
        byte[] resp = new byte[512];
        int[] respLen = new int[]{(int) 512};


        ret = CryptoUtil.symEncrypt(
                (byte) CryptoUtil.SYM_ARITH_DES,
                (byte) CryptoUtil.SYM_ARITH_ECB,
                key,
                (byte) 8,
                iv,
                iv.length,
                data,
                data.length,
                resp,
                respLen,
                (byte) CryptoUtil.PADDING_MODE_NONE);
        if (ret == 0) {
            logMsg("\nsymEncrypt [DES] success," + BytesUtils.byte2HexStr(resp, respLen[0]));
        } else {
            logMsg("symEncrypt [DES] fail," + String.format(" errCode = 0x%x\n", ret));
        }


        byte[] resp2 = new byte[512];
        int[] respLen2 = new int[]{(int) 512};
        ret = CryptoUtil.symDecrypt(
                (byte) CryptoUtil.SYM_ARITH_DES,
                (byte) CryptoUtil.SYM_ARITH_ECB,
                key,
                (byte) 8,
                iv,
                iv.length,
                resp,
                respLen[0],
                resp2,
                respLen2,
                (byte) CryptoUtil.PADDING_MODE_NONE);
        if (ret == 0) {
            logMsg("\nsymDecrypt [DES] success," + BytesUtils.byte2HexStr(resp2, respLen2[0]));
        } else {
            logMsg("symDecrypt [DES] fail," + String.format(" errCode = 0x%x\n", ret));
        }


        ret = CryptoUtil.symEncrypt(
                (byte) CryptoUtil.SYM_ARITH_3DES,
                (byte) CryptoUtil.SYM_ARITH_ECB,
                key,
                (byte) key.length,
                iv,
                iv.length,
                data,
                data.length,
                resp,
                respLen,
                (byte) CryptoUtil.PADDING_MODE_NONE);
        if (ret == 0) {
            logMsg("\nsymEncrypt [3DES] success," + BytesUtils.byte2HexStr(resp, respLen[0]));
        } else {
            logMsg("symEncrypt [3DES] fail," + String.format(" errCode = 0x%x\n", ret));
        }


        respLen2 = new int[]{(int) 512};
        ret = CryptoUtil.symDecrypt(
                (byte) CryptoUtil.SYM_ARITH_3DES,
                (byte) CryptoUtil.SYM_ARITH_ECB,
                key,
                (byte) key.length,
                iv,
                iv.length,
                resp,
                respLen[0],
                resp2,
                respLen2,
                (byte) CryptoUtil.PADDING_MODE_NONE);
        if (ret == 0) {
            logMsg("\nsymDecrypt [3DES] success," + BytesUtils.byte2HexStr(resp2, respLen2[0]));
        } else {
            logMsg("symDecrypt [3DES] fail," + String.format(" errCode = 0x%x\n", ret));
        }


        ret = CryptoUtil.symEncrypt(
                (byte) CryptoUtil.SYM_ARITH_AES,
                (byte) CryptoUtil.SYM_ARITH_ECB,
                key,
                (byte) key.length,
                iv,
                iv.length,
                data,
                data.length,
                resp,
                respLen,
                (byte) CryptoUtil.PADDING_MODE_NONE);
        if (ret == 0) {
            logMsg("\nsymEncrypt [AES] success," + BytesUtils.byte2HexStr(resp, respLen[0]));
        } else {
            logMsg("symEncrypt [AES] fail," + String.format(" errCode = 0x%x\n", ret));
        }


        ret = CryptoUtil.symDecrypt(
                (byte) CryptoUtil.SYM_ARITH_AES,
                (byte) CryptoUtil.SYM_ARITH_ECB,
                key,
                (byte) key.length,
                iv,
                iv.length,
                resp,
                respLen[0],
                resp2,
                respLen2,
                (byte) CryptoUtil.PADDING_MODE_NONE);
        if (ret == 0) {
            logMsg("\nsymDecrypt [AES] success," + BytesUtils.byte2HexStr(resp2, respLen2[0]));
        } else {
            logMsg("symDecrypt [AES] fail," + String.format(" errCode = 0x%x\n", ret));
        }


        ret = CryptoUtil.symEncrypt(
                (byte) 0x10,
                (byte) CryptoUtil.SYM_ARITH_ECB,
                key,
                (byte) key.length,
                iv,
                iv.length,
                data,
                data.length,
                resp,
                respLen,
                (byte) CryptoUtil.PADDING_MODE_NONE);
        if (ret == 0) {
            logMsg("\nsymEncrypt [SM4] success," + BytesUtils.byte2HexStr(resp, respLen[0]));
        } else {
            logMsg("symEncrypt [SM4] fail," + String.format(" errCode = 0x%x\n", ret));
        }


        ret = CryptoUtil.symDecrypt(
                (byte) 0x10,
                (byte) CryptoUtil.SYM_ARITH_ECB,
                key,
                (byte) key.length,
                iv,
                iv.length,
                resp,
                respLen[0],
                resp2,
                respLen2,
                (byte) CryptoUtil.PADDING_MODE_NONE);
        if (ret == 0) {
            logMsg("\nsymDecrypt [SM4] success," + BytesUtils.byte2HexStr(resp2, respLen2[0]));
        } else {
            logMsg("symDecrypt [SM4] fail," + String.format(" errCode = 0x%x\n", ret));
        }

    }


    void hashSample() {

//        Hashes: Hashing operation finished
//                ****************************************
//        Data:			123456789ABCDEF123456789ABCDEF123456789ABCDEF123456789ABCDEF
//        Hash type:		SHA-1
//                ----------------------------------------
//        Hash:			17A6EF04C131E68F2C96B1DE9B379FB640FB58ED
//
//                [2022-08-12 13:37:05]
//        Hashes: Hashing operation finished
//                ****************************************
//        Data:			123456789ABCDEF123456789ABCDEF123456789ABCDEF123456789ABCDEF
//        Hash type:		SHA-256
//                ----------------------------------------
//        Hash:			CB75239856C383CFB1AEC172B3409B87EDFD34784F4365785EBA825248B0C44F
//
//                [2022-08-12 13:37:24]
//        Hashes: Hashing operation finished
//                ****************************************
//        Data:			123456789ABCDEF123456789ABCDEF123456789ABCDEF123456789ABCDEF
//        Hash type:		SHA-512
//                ----------------------------------------
//        Hash:			5629E80CA5D318BB445F2F916476E9D23CD97D365A7FA073FEEB64ECE3551E463F728F273313FB897D14477F99694C4AB9FC9D5776CA70C584EA18CE5D5C332D

        int ret = 0;
        byte[] data = BytesUtils.hexStringToBytes("123456789ABCDEF123456789ABCDEF123456789ABCDEF123456789ABCDEF");
        byte[] resp = new byte[512];
        byte[] respLen = new byte[]{(byte) 512};
        ret = CryptoUtil.hashData((byte) CryptoUtil.HASH_ALG_SHA1, data, data.length, resp, respLen);
        if (ret == 0) {
            logMsg("\nhashData [SHA1] success," + BytesUtils.byte2HexStr(resp, respLen[0]));
        } else {
            logMsg("hashData [SHA1] fail," + String.format(" errCode = 0x%x\n", ret));
        }


        ret = CryptoUtil.hashData((byte) CryptoUtil.HASH_ALG_SHA256, data, data.length, resp, respLen);
        if (ret == 0) {
            logMsg("\nhashData [SHA256] success," + BytesUtils.byte2HexStr(resp, respLen[0]));
        } else {
            logMsg("hashData [SHA256] fail," + String.format(" errCode = 0x%x\n", ret));
        }


        ret = CryptoUtil.hashData((byte) CryptoUtil.HASH_ALG_SHA512, data, data.length, resp, respLen);
        if (ret == 0) {
            logMsg("\nhashData [SHA512] success," + BytesUtils.byte2HexStr(resp, respLen[0]));
        } else {
            logMsg("hashData [SHA512] fail," + String.format(" errCode = 0x%x\n", ret));
        }

        ret = CryptoUtil.hashData((byte) CryptoUtil.HASH_ALG_SM3, data, data.length, resp, respLen);
        if (ret == 0) {
            logMsg("\nhashData [SM3] success," + BytesUtils.byte2HexStr(resp, respLen[0]));
        } else {
            logMsg("hashData [SM3] fail," + String.format(" errCode = 0x%x\n", ret));
        }
    }

    /**
     * Gets a random number of the specified length from the security chip.
     */
    void getRandNumDemo() {
        try {
            int randomLen = 0;
            int ret;
            BytesTypeValue randNum = new BytesTypeValue();

            String str = mRandomNum.getText().toString();

            randomLen = (int) Long.parseLong(str);

            if (randomLen < 1 || randomLen > 256) {
                logMsg("Parameter invalid (1-256)" + String.format(" frequency = %d\n", randomLen));
                return;
            }

            ret = crypto.genRand(randomLen, randNum);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("genRand failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            logMsg("genRand success. \nRandom data:" + BytesUtils.byte2HexStr(randNum.getData()));
        } catch (Exception e) {
            logMsg("Exception " + e.toString() + "\n");
        }

    }

    /**
     * Get the key of the container corresponding to this App from the device.
     */
    void enumKeyIndex() {
        String packageName = getApplicationContext().getPackageName();

        int ret = keyManager.setKeyGroupName(packageName);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("setKeyGroupName(" + packageName + String.format(")failed,  errCode =0x%x", ret));
            return;
        }

        IntArrayTypeValue keyIndex = new IntArrayTypeValue();

        ret = keyManager.getInstalledKey(255, keyIndex);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("setKeyGroupName(" + packageName + String.format(")failed,  errCode =0x%x", ret));
            return;
        }

        logMsg("enumKeyIndex success.\n");

        int keyNum;
        if (keyIndex.getmData() == null) {
            keyNum = 0;
            logMsg("Key num: " + keyNum);
            return;
        }

        keyNum = keyIndex.getmData().length;
        logMsg("Key num: " + keyNum);

        for (int i = 0; i < keyIndex.getmData().length; i++) {
            logMsg("" + String.format(" %04x \n", keyIndex.getmData()[i]));
        }

    }
}
