package com.ftpos.ftappdemo;


import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ftpos.library.crypto.CryptoUtil;
import com.ftpos.library.smartpos.datautils.BytesTypeValue;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.keymanager.AlgName;
import com.ftpos.library.smartpos.keymanager.EncryptionMode;
import com.ftpos.library.smartpos.keymanager.EncryptionPaddingMode;
import com.ftpos.library.smartpos.keymanager.KeyManager;
import com.ftpos.library.smartpos.keymanager.KeyType;
import com.ftpos.library.smartpos.keymanager.KeyUsage;
import com.ftpos.library.smartpos.keymanager.MacMode;
import com.ftpos.library.smartpos.keymanager.RsaAlgPaddingMode;
import com.ftpos.library.smartpos.keymanager.RsaKeyPairType;
import com.ftpos.library.smartpos.util.BytesUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class KeyManagerActivity extends BaseActivity {

    private TextView mShowResultTv;
    private KeyManager keyManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_keymanage);
        this.keyManager = MainActivity.keyManager;
        initView();

    }

    private void initView() {
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText("Key Manage View");

        findViewById(R.id.ukey_navbar_left_btn).setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        findViewById(R.id.register).setOnClickListener(this);
        findViewById(R.id.install_protect_key).setOnClickListener(this);
        findViewById(R.id.rl_install_ipek).setOnClickListener(this);
        findViewById(R.id.rl_install_ipek_enc).setOnClickListener(this);
        findViewById(R.id.rl_install_ipek_enc_dek_p).setOnClickListener(this);
        findViewById(R.id.rl_add_ksn).setOnClickListener(this);
        findViewById(R.id.rl_install_mk).setOnClickListener(this);
        findViewById(R.id.rl_install_mk_enc).setOnClickListener(this);
        findViewById(R.id.rl_install_dek).setOnClickListener(this);
        findViewById(R.id.rl_install_pek).setOnClickListener(this);
        findViewById(R.id.rl_install_mek).setOnClickListener(this);
        findViewById(R.id.rl_install_fk).setOnClickListener(this);
        findViewById(R.id.rl_install_fk_enc).setOnClickListener(this);
        findViewById(R.id.rl_install_ipek_aes).setOnClickListener(this);
        findViewById(R.id.rl_install_tr31).setOnClickListener(this);
        findViewById(R.id.rl_generate_rsa).setOnClickListener(this);
        findViewById(R.id.rl_rsa_sign).setOnClickListener(this);

        mShowResultTv = findViewById(R.id.function_return_result_tv);

    }

    @Override
    public void onClick(View v) {
        int viewId = v.getId();

        if (viewId == R.id.ukey_navbar_left_btn) {
            finish();
            return;
        }
        if (viewId == R.id.ukey_navbar_right_btn) {
            mShowResultTv.setText("");
            return;
        }
        if (viewId == R.id.register) {
            initKeyManager();
            return;
        }
        if (viewId == R.id.install_protect_key) {
            installProtectKeyAesFix();
            return;
        }
        if (viewId == R.id.rl_install_ipek) {
            installIpekAddTest();
            return;
        }
        if (viewId == R.id.rl_install_ipek_enc) {
            installIpekEncAddTest();
            return;
        }
        if (viewId == R.id.rl_install_ipek_enc_dek_p) {
            installIpekAndEncryptTest();
            return;
        }
        if (viewId == R.id.rl_add_ksn) {
            ipekAddKsnTest();
            return;
        }
        if (viewId == R.id.rl_install_mk) {
            installMk();
            return;
        }
        if (viewId == R.id.rl_install_mk_enc) {
            installMkEnc();
            return;
        }
        if (viewId == R.id.rl_install_dek) {
            installMkDekAddTest(1);
            return;
        }
        if (viewId == R.id.rl_install_pek) {
            installMkPekAddTest(1);
            return;
        }
        if (viewId == R.id.rl_install_mek) {
            installMkMekAddTest(1);
            return;
        }
        if (viewId == R.id.rl_install_fk) {
            installFixKeyAddTest();
            return;
        }
        if (viewId == R.id.rl_install_fk_enc) {
            installFixKeyEncAddTest();
            return;
        }
        if (viewId == R.id.rl_install_ipek_aes) {
            installIpekAesAddTest();
            return;
        }
        if (viewId == R.id.rl_install_tr31) {
            test_install_tr31();
            return;
        }
        if (viewId == R.id.rl_generate_rsa) {
            generateRsaAddTest();
            return;
        }
        if (viewId == R.id.rl_rsa_sign) {
            rsaSignTest();
        }

    }

    /**
     * Generates a set of RSA keys and performs encryption and decryption authentication.
     */
    private void generateRsaAddTest() {

        int ret;
        int keyIndex;

        keyIndex = 1;
        RsaKeyPairType rsaKeyPairType = new RsaKeyPairType();

        ret = keyManager.genRsaKeyPair(KeyType.KEY_TYPE_RSA, keyIndex, 1024, rsaKeyPairType);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("genRsaKeyPair fail errCode =0x%x\n", ret));
            return;
        }
        logMsg("genRsaKeyPair success \n");

        byte[] data = BytesUtils.hexStringToBytes("88E1AB2A2E3DD38C1FA039A536500CC8A87AB9D62DC92C01");
        BytesTypeValue bytesTypeValue = new BytesTypeValue();

        ret = keyManager.rsaEncryptByIndex(KeyType.KEY_TYPE_RSA,
                keyIndex,
                data,
                data.length,
                RsaAlgPaddingMode.RSA_PADDING_MODE_PKCS_V15,
                bytesTypeValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("rsaEncryptByIndex fail errCode =0x%x\n", ret));
            return;
        }
        logMsg("rsaEncryptByIndex success \n");

        ret = keyManager.rsaDecryptByIndex(KeyType.KEY_TYPE_RSA,
                keyIndex,
                bytesTypeValue.getData(),
                bytesTypeValue.getData().length,
                RsaAlgPaddingMode.RSA_PADDING_MODE_PKCS_V15,
                bytesTypeValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("rsaDecryptByIndex fail errCode =0x%x\n", ret));
            return;
        }

        if (!Arrays.equals(data, bytesTypeValue.getData())) {
            logMsg("rsaDecryptByIndex success but result error\n");
            return;
        }
        logMsg("rsaDecryptByIndex success:");
        logMsg("plaintext:\n" + BytesUtils.byte2HexStr(data) + "\n");
        logMsg("encrypted result:\n " + BytesUtils.byte2HexStr(bytesTypeValue.getData()) + "\n");
    }


    private void rsaSignTest() {

        String modulus = "9AFBAEDDBAB4F244E3DE1DD729B7B36EAB19B22D2D3F3DCD8CDC1C6DB7366F82924B32F1451D54AFB2DA24200AA4F203A3880CA21EDEB0739AF628276C3944F3A729F7DAA273CC5F7AF3803A46003EE4424AD4CCE6DBD1BD39E95F002660E4D44553319F15C3E5DAEAA06AF04B95F17EBF072CEDBF879E96C471542CD32D9F08AA47B0BEFFBB4F0D2735AD62EC22B3F685C71DF7A23662E2D210547EB070414FA9D751E70C2222D5D9BD11DD80FA9620E2579AEF8799100BEF816B4CF3FC791F2D485CB6E193B2B1E2CC69D22477C14CE30D020653FD60214BF905377E239B177950CE09A272C5E51C2D7AC3BE4BA12B46FEF309E63322B70456439CAA20500B";
        int pubEXP = 65537;
        String p = "DE845D8E58C314D69ACB335FEA1AAED933E795486F35437333F27B293BD8FCB1239441DFA407E85808CA32915B063AA03FB3E537547CC59495519369DA8AD7C45784ECA53D0AB9FA3EF6EA2EBC54D1F8693E1F0E43D441B4E50D1C14FEB393D2766A0048B317FCD44293679F1B8C611BEAC81D09152EE556E9852CB207B88611";
        String q = "B24DD3B09DAFC6227877DA5BA46451B5A168AD7F9735C18752154254EBBB2A0270F03AB1E923354090B9D06079B8DF4E1B1765C180AA542EB87F802EEF2476BE4B5B2EFCF43EA61B56CE2528529528786FC530FEACFC7C3835603DEFEA2E70A3B988CB446BB8ED983FCA228644488AA8E7A2CAF050772540C673E8CADF36285B";
        String DP = "D9AA498A3131C3749A3F23A6336E9E9341BECBBCEB6C02EAF269C42875AEB0A0E6D8E4074C819FBA5DFECCB07B86005B39755447679216045E7C49E346308318413479F8B95980CB56445A98150A33CAF7C818C0EF80F63569CA052A546C7DB166BC12430B4DEFB802D80689117DA490967DE2493C3B7F84213270A246032EC1";
        String DQ = "568E1D435168F09FEF270F63094C81B5D0230686379B2AD54BED7203D3634A23613BDCE25CB070BB65062EB7869F4E179663274C65EF496A5CB9D839F0711D0B877D3E3421450051A933ED29C3DD1086568BBD1B045294E29C5699FA8B0DFC341E6EDF6DD9752E318855D1A8A82126DE93839D4C47636F3C209A47D9A3BDA09D";
        String Qinv = "B092DDE7C7B285410AB4A08B0B4E3186430AD48746E28EE01510D1C4A18671F046BFAC8C9284DCC182138620E8DB149AA7E7E2A5E7F0404CF712C2DDAC5F4EA9F8A7B001581CE8BA764EE04C801012272EF982FCFC1E2E39415324E4FA9C162E7BE3AB1491F0F0A33F53B770E59F3B501F8D122F833CEDA2D32EE1E2033E559D";

        int ret;
        int keyIndex = 1;

        ret = keyManager.loadRsaKeyPair(KeyType.KEY_TYPE_RSA,
                keyIndex,
                2048,
                BytesUtils.hexStringToBytes(modulus),
                pubEXP,
                BytesUtils.hexStringToBytes(p),
                BytesUtils.hexStringToBytes(q),
                BytesUtils.hexStringToBytes(DP),
                BytesUtils.hexStringToBytes(DQ),
                BytesUtils.hexStringToBytes(Qinv));
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("genRsaKeyPair fail errCode =0x%x\n", ret));
            return;
        }
        logMsg("genRsaKeyPair success \n");

        byte[] data = "31323334353637383930313233343536".getBytes(StandardCharsets.UTF_8);
        byte[] resp = new byte[512];
        byte[] respLen = new byte[]{(byte) 512};

        ret = CryptoUtil.hashData((byte) CryptoUtil.HASH_ALG_SHA256,
                data,
                data.length,
                resp,
                respLen);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("hashData fail," + String.format(" errCode = 0x%x\n", ret));
            return;
        }
        logMsg("\nhashData success");
        byte[] signdata = new byte[respLen[0]];
        System.arraycopy(resp, 0, signdata, 0, respLen[0]);
        logMsg("\nhashData:" + signdata.length + ":" + BytesUtils.byte2HexStr(signdata));

        BytesTypeValue bytesTypeValue = new BytesTypeValue();
        ret = keyManager.rsaSingByIndex(keyIndex,
                CryptoUtil.HASH_ALG_SHA256,
                signdata,
                RsaAlgPaddingMode.RSA_PADDING_MODE_PKCS_V15,
                bytesTypeValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("rsasign fail errCode =0x%x\n", ret));
            return;
        }
        logMsg("rsasign success \n");
        logMsg("rsasign result:\n " + BytesUtils.byte2HexStr(bytesTypeValue.getData()) + "\n");

        ret = keyManager.rsaVerifyByIndex(keyIndex,
                CryptoUtil.HASH_ALG_SHA256,
                signdata,
                bytesTypeValue.getData(),
                bytesTypeValue.getData().length,
                RsaAlgPaddingMode.RSA_PADDING_MODE_PKCS_V15);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("rsaVerify fail errCode =0x%x\n", ret));
            return;
        }
        logMsg("rsaVerify success \n");

    }


    /**
     * tr31 install usage routines.
     */
    private void test_install_tr31() {
        int ret;
        int kbpkKeyIndex;
        int TR31KeyIndex;
        byte[] kbpkData;
        byte[] TR31keyData;
        BytesTypeValue kcvValue = new BytesTypeValue();

        //Install an KBPK key
        kbpkData = BytesUtils.hexStringToBytes("88E1AB2A2E3DD38C1FA039A536500CC8A87AB9D62DC92C01");
        //load plaintext key，protect KeyType And Index must be 0x00！
        BytesTypeValue kcvBytesTypeValue = new BytesTypeValue();
        kbpkKeyIndex = 0x01;
        ret = keyManager.loadSymKey(KeyType.KEY_TYPE_KBPK,
                kbpkKeyIndex,
                AlgName.SYM_ARITH_3DES,
                0x00,
                0x00,
                kbpkData,
                kbpkData.length,
                kcvBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("loadSymKey(KBPK)  success \n");
        } else {
            logMsg(String.format("loadSymKey(KBPK) fail errCode =0x%x\n", ret));
        }

        // Install an TR-31 key
        TR31KeyIndex = 0x01;
        TR31keyData = "B0120B1TX00E0300KS080101IK1C0FFFF9876543210E00000000PB04E963ADA1047BB2A59282AF6B750C028797E52C7FBB27EE6C4FCB509D1298B182".getBytes();
        ret = keyManager.loadTr31KeyByIndex(KeyType.KEY_TYPE_IPEK,
                TR31KeyIndex,
                kbpkKeyIndex,
                TR31keyData,
                TR31keyData.length,
                kcvValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("loadTr31KeyByIndex(IPEK)  success \n");
        } else {
            logMsg(String.format("loadTr31KeyByIndex(IPEK) fail errCode =0x%x\n", ret));
        }

        //==========================================================================================
        // Install an KBPK key
        kbpkData = BytesUtils.hexStringToBytes("88E1AB2A2E3DD38C1FA039A536500CC8A87AB9D62DC92C01");
        //load plaintext key，protect KeyType And Index must be 0x00！
        kbpkKeyIndex = 0x02;
        ret = keyManager.loadSymKey(KeyType.KEY_TYPE_KBPK,
                kbpkKeyIndex,
                AlgName.SYM_ARITH_3DES,
                0x00,
                0x00,
                kbpkData,
                kbpkData.length,
                kcvBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("loadSymKey(KBPK)  success \n");
        } else {
            logMsg(String.format("loadSymKey(KBPK) fail errCode =0x%x\n", ret));
        }

        // Install an TR-31 key
        TR31KeyIndex = 0x02;
        TR31keyData = "B0152B1AX00E0300KS080101IK1C0FFFF9876543210E00000000PB04A38A49B13BFFC955CBCB9112F2B5B5EB32E87752DAD6CF1922BEF7BD0222F5F55A7241AE2F9008A71CD7FB21C55338EF".getBytes();
        ret = keyManager.loadTr31KeyByIndex(KeyType.KEY_TYPE_IPEK,
                TR31KeyIndex,
                kbpkKeyIndex,
                TR31keyData,
                TR31keyData.length,
                kcvValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("loadTr31KeyByIndex(IPEK)  success \n");
        } else {
            logMsg(String.format("loadTr31KeyByIndex(IPEK) fail errCode =0x%x\n", ret));
        }

        //==========================================================================================
        // Install an KBPK key
        kbpkData = BytesUtils.hexStringToBytes("88E1AB2A2E3DD38C1FA039A536500CC8A87AB9D62DC92C01");
        //load plaintext key，protect KeyType And Index must be 0x00！
        kbpkKeyIndex = 0x03;
        ret = keyManager.loadSymKey(KeyType.KEY_TYPE_KBPK,
                kbpkKeyIndex,
                AlgName.SYM_ARITH_3DES,
                0x00,
                0x00,
                kbpkData,
                kbpkData.length,
                kcvBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("loadSymKey(KBPK)  success \n");
        } else {
            logMsg(String.format("loadSymKey(KBPK) fail errCode =0x%x\n", ret));
        }

        // Install an TR-31 key
        TR31KeyIndex = 0x03;
        TR31keyData = "B0088D0TB00E0100KS080201683232CE25697F3B209A0DBAEAB64FBA95AAC6C1D3045AD00B2871CEF17F1F25".getBytes();
        ret = keyManager.loadTr31KeyByIndex(KeyType.KEY_TYPE_MK,
                TR31KeyIndex,
                kbpkKeyIndex,
                TR31keyData,
                TR31keyData.length,
                kcvValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("loadTr31KeyByIndex(MK)  success \n");
        } else {
            logMsg(String.format("loadTr31KeyByIndex(MK) fail errCode =0x%x\n", ret));
        }

        //==========================================================================================
        // Install an KBPK key
        kbpkData = BytesUtils.hexStringToBytes("88E1AB2A2E3DD38C1FA039A536500CC8A87AB9D62DC92C01");
        //load plaintext key，protect KeyType And Index must be 0x00！
        kbpkKeyIndex = 0x04;
        ret = keyManager.loadSymKey(KeyType.KEY_TYPE_KBPK,
                kbpkKeyIndex,
                AlgName.SYM_ARITH_3DES,
                0x00,
                0x00,
                kbpkData,
                kbpkData.length,
                kcvBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("loadSymKey(KBPK)  success \n");
        } else {
            logMsg(String.format("loadSymKey(KBPK) fail errCode =0x%x\n", ret));
        }

        // Install an TR-31 key
        TR31KeyIndex = 0x04;
        TR31keyData = "B0104D0TB00E0100KS0802017CE7B47C1C46BBB15793A06FBDFDD997EE12950D758D9DA28413B4C103B035574B7A760F2B4364FC".getBytes();
        ret = keyManager.loadTr31KeyByIndex(KeyType.KEY_TYPE_MK,
                TR31KeyIndex,
                kbpkKeyIndex,
                TR31keyData,
                TR31keyData.length,
                kcvValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("loadTr31KeyByIndex(MK)  success \n");
        } else {
            logMsg(String.format("loadTr31KeyByIndex(MK) fail errCode =0x%x\n", ret));
        }

        //==========================================================================================

        // Install an KBPK key
        kbpkData = BytesUtils.hexStringToBytes("88E1AB2A2E3DD38C1FA039A536500CC8");
        //load plaintext key，protect KeyType And Index must be 0x00！
        kbpkKeyIndex = 0x05;
        ret = keyManager.loadSymKey(KeyType.KEY_TYPE_KBPK,
                kbpkKeyIndex,
                AlgName.SYM_ARITH_AES,
                0x00,
                0x00,
                kbpkData,
                kbpkData.length,
                kcvBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("loadSymKey(KBPK)  success \n");
        } else {
            logMsg(String.format("loadSymKey(KBPK) fail errCode =0x%x\n", ret));
        }

        // Install an TR-31 key
        TR31KeyIndex = 0x05;
        TR31keyData = "D0128D0AB00E0200KS080301PB0800001A01EBC02A3CAFC24171B926BBAE6FAC48243920CD09087F09F8325326AF912180795211C5814E6681E5DB57D286A191".getBytes();
        ret = keyManager.loadTr31KeyByIndex(KeyType.KEY_TYPE_FIXEDKEY,
                TR31KeyIndex,
                kbpkKeyIndex,
                TR31keyData,
                TR31keyData.length,
                kcvValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("loadTr31KeyByIndex(FIXEDKEY)  success \n");
        } else {
            logMsg(String.format("loadTr31KeyByIndex(FIXEDKEY) fail errCode =0x%x\n", ret));
        }

        //==========================================================================================
        // Install an KBPK key
        kbpkData = BytesUtils.hexStringToBytes("88E1AB2A2E3DD38C1FA039A536500CC8A87AB9D62DC92C01058FA79F44657DE6");
        //load plaintext key，protect KeyType And Index must be 0x00！
        kbpkKeyIndex = 0x06;
        ret = keyManager.loadSymKey(KeyType.KEY_TYPE_KBPK,
                kbpkKeyIndex,
                AlgName.SYM_ARITH_AES256,
                0x00,
                0x00,
                kbpkData,
                kbpkData.length,
                kcvBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("loadSymKey(KBPK)  success \n");
        } else {
            logMsg(String.format("loadSymKey(KBPK) fail errCode =0x%x\n", ret));
        }

        // Install an TR-31 key
        TR31KeyIndex = 0x06;
        TR31keyData = "D0160D0AB00E0200KS080301PB080000EB7989F9D51FCCD4C77584A4EF0C506A388C7B7D4811F7DB789772B307032583194AB934046B20E423293F96ACE035976CE0C7F773F5871A4B9AA046309D69AE".getBytes();
        ret = keyManager.loadTr31KeyByIndex(KeyType.KEY_TYPE_FIXEDKEY,
                TR31KeyIndex,
                kbpkKeyIndex,
                TR31keyData,
                TR31keyData.length,
                kcvValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("loadTr31KeyByIndex(FIXEDKEY)  success \n");
        } else {
            logMsg(String.format("loadTr31KeyByIndex(FIXEDKEY) fail errCode =0x%x\n", ret));
        }

        logMsg("retrieveTr31KeyInfo  success \n");
    }

    /**
     * Set the name of the key group.
     * Keys installed with the same group name can be shared.
     */
    private void initKeyManager() {
        //After connecting to the Service, it must be called once to init the KeyManager, no need to call repeatedly
        //just test here
        String packageName = getApplicationContext().getPackageName();
        int ret = keyManager.setKeyGroupName(packageName);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("setKeyGroupName(" + packageName + String.format(")failed,  errCode =0x%x", ret));
            return;
        }
        logMsg("setKeyGroupName success\n");
    }

    /**
     * Install a set of AES FIX keys to use as protection keys.
     */
    private void installProtectKeyAesFix() {

        int sfKeyIndex = 0x01;
        int ret;

        // Install the protection key.
        byte[] aesKeyData = BytesUtils.hexStringToBytes("01020304050607080102030405060708");
        byte[] aesKcv = new byte[3];

        ret = keyManager.loadSymKey(KeyType.KEY_TYPE_FIXEDKEY,
                sfKeyIndex,
                AlgName.SYM_ARITH_AES,
                0x00,
                0x00,
                aesKeyData,
                aesKeyData.length,
                aesKcv);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("Install the protection key failed\n");
            return;
        }
        if (!Arrays.equals(BytesUtils.hexStringToBytes("D2DD9D"), aesKcv)) {
            logMsg("Install the protection key success but kcv check failed\n");
            return;
        }
        logMsg("Install the protection key success and kcv check success\n");

    }

    /**
     * Install a set of plaintext FIX keys, and decryption, MAC test.
     */
    private void installFixKeyAddTest() {
        int ret;
        int keyIndex = 0x02;

        byte[] aesKeyData = BytesUtils.hexStringToBytes("08070605040302010807060504030201");
        byte[] aesKcv = new byte[3];
        ret = keyManager.loadSymKey(KeyType.KEY_TYPE_FIXEDKEY,
                keyIndex,
                AlgName.SYM_ARITH_AES,
                0x00,
                0x00,
                aesKeyData,
                aesKeyData.length,
                aesKcv);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("Install the protection key failed\n");
            return;
        }

        if (!Arrays.equals(BytesUtils.hexStringToBytes("9B7D21"), aesKcv)) {
            logMsg("Install the protection key success but kcv check failed\n");
            return;
        }
        logMsg("Install the protection key success and kcv check success\n");

        // encrypt/decrypt data
        // plaintext : AC292FAA1315B4D858AB3A3D7D5933A9
        // encrypt text :  D9569F0E5DD600A7A467DD5B504EEF08
        byte[] data = BytesUtils.hexStringToBytes("AC292FAA1315B4D858AB3A3D7D5933A9");
        BytesTypeValue result = new BytesTypeValue();

        ret = keyManager.symEncryptByIndex(KeyType.KEY_TYPE_FIXEDKEY,
                keyIndex,
                AlgName.SYM_ARITH_AES,
                EncryptionMode.SYM_ARITH_ECB,
                null,
                0,
                data,
                data.length,
                EncryptionPaddingMode.PADDING_MODE_NONE,
                result);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("symEncryptByIndex(FIX KEY) fail errCode = 0x%x\n", ret));
            return;
        }

        if (!Arrays.equals(BytesUtils.hexStringToBytes("D9569F0E5DD600A7A467DD5B504EEF08"), result.getData())) {
            logMsg("Install the protection key success but kcv check failed\n");
            return;
        }
        logMsg("symEncryptByIndex(fix) success:");
        logMsg("plaintext:\n" + BytesUtils.byte2HexStr(data) + "\n");
        logMsg("encrypted result:\n " + BytesUtils.byte2HexStr(result.getData()) + "\n");

        // macByIndex(IPEK) test
        byte[] initVector = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,};

        BytesTypeValue macBytesTypeValue = new BytesTypeValue();
        ret = keyManager.macByIndex(KeyType.KEY_TYPE_FIXEDKEY,
                keyIndex,
                AlgName.SYM_ARITH_AES,
                initVector,
                initVector.length,
                data,
                data.length,
                MacMode.MAC_MODE_9797_1_M2,
                0,
                macBytesTypeValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("macByIndex(ipek) fail errCode = 0x%x\n", ret));
            return;
        }
        // After import, First use
        //current ksn:FFFF9876543210E00001(hex format)
        //current dukpt MEK_P:042666B46E84CFA368DE96282F397BC9(hex format)
        //plaintext:0102030405060708 (hex format)
        //mac result:5DF2DF7DC090F8F0(hex format,encryption key:dukpt MEK_P alg: 3desecb paddingMode:PADDING_MODE_9797_1_M2)
        byte[] macData = macBytesTypeValue.getData();
        logMsg("macByIndex(ipek) success:");
        logMsg("result:" + BytesUtils.byte2HexStr(macData) + "\n");

    }

    /**
     * Install a set of ciphertext Fix keys, and perform encryption, decryption,
     * and MAC test.
     */
    private void installFixKeyEncAddTest() {
        int ret;
        int keyIndex = 0x03;
        int sfKeyIndex = 1;

//         Install an FIX KEY (With fix Protection)
//         fix plaintext:01020304050607080102030405060708 (hex format)
//         ipek plaintext:123456FAA1315B4D858AB3A3D7D5933A (hex format)
//         ipek kcv:DBF398(hex format)
//         ipek ciphertext:92397AE8BD4EAEF836A7FFC859A8D6D1 (hex format,encryption key:fix alg: aesecb)
        byte[] aesKeyData = BytesUtils.hexStringToBytes("92397AE8BD4EAEF836A7FFC859A8D6D1");
        byte[] aesKcv = new byte[3];
        ret = keyManager.loadSymKey(KeyType.KEY_TYPE_FIXEDKEY,
                keyIndex,
                AlgName.SYM_ARITH_AES,
                KeyType.KEY_TYPE_FIXEDKEY,
                sfKeyIndex,
                aesKeyData,
                aesKeyData.length,
                aesKcv);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("Install the protection key failed\n");
            return;
        }

        if (!Arrays.equals(BytesUtils.hexStringToBytes("DBF398"), aesKcv)) {
            logMsg("Install the protection key success but kcv check failed\n");
            return;
        }
        logMsg("Install the protection key success and kcv check success\n");

        // encrypt/decrypt data
        // plaintext : AC292FAA1315B4D858AB3A3D7D5933A9
        // encrypt text :  0B5681557A8CC446A7314F5C2F2FBE23
        byte[] data = BytesUtils.hexStringToBytes("AC292FAA1315B4D858AB3A3D7D5933A9");
        BytesTypeValue result = new BytesTypeValue();

        ret = keyManager.symEncryptByIndex(KeyType.KEY_TYPE_FIXEDKEY,
                keyIndex,
                AlgName.SYM_ARITH_AES,
                EncryptionMode.SYM_ARITH_ECB,
                null,
                0,
                data,
                data.length,
                EncryptionPaddingMode.PADDING_MODE_NONE,
                result);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("symEncryptByIndex(FIX KEY) fail errCode = 0x%x\n", ret));
            return;
        }
        if (!Arrays.equals(BytesUtils.hexStringToBytes("0B5681557A8CC446A7314F5C2F2FBE23"), result.getData())) {
            logMsg("Install the protection key success but kcv check failed\n");
            return;
        }
        logMsg("symEncryptByIndex(fix) success:");
        logMsg("plaintext:\n" + BytesUtils.byte2HexStr(data) + "\n");
        logMsg("encrypted result:\n " + BytesUtils.byte2HexStr(result.getData()) + "\n");

        // macByIndex(IPEK) test
        byte[] initVector = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,};

        BytesTypeValue macBytesTypeValue = new BytesTypeValue();
        ret = keyManager.macByIndex(KeyType.KEY_TYPE_FIXEDKEY,
                keyIndex,
                AlgName.SYM_ARITH_AES,
                initVector,
                initVector.length,
                data,
                data.length,
                MacMode.MAC_MODE_9797_1_M2,
                0,
                macBytesTypeValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("macByIndex(ipek) fail errCode = 0x%x\n", ret));
            return;
        }
        // After import, First use
        //current ksn:FFFF9876543210E00001(hex format)
        //current dukpt MEK_P:042666B46E84CFA368DE96282F397BC9(hex format)
        //plaintext:0102030405060708 (hex format)
        //mac result:5DF2DF7DC090F8F0(hex format,encryption key:dukpt MEK_P alg: 3desecb paddingMode:PADDING_MODE_9797_1_M2)
        byte[] macData = macBytesTypeValue.getData();
        logMsg("macByIndex(ipek) success:");
        logMsg("result:" + BytesUtils.byte2HexStr(macData) + "\n");
    }

    /**
     * Install plaintext a plaintext key, and decrypt and decrypt plaintext keys.
     */
    private void installIpekAddTest() {
        int ret;
        int ipekKeyIndex;

//          Install an IPEK
//          ipek plaintext:6AC292FAA1315B4D858AB3A3D7D5933A (hex format)
//          ipek kcv:AF8C07(hex format)

        byte[] ipekData = BytesUtils.hexStringToBytes("6AC292FAA1315B4D858AB3A3D7D5933A");
        byte[] iKsnData = BytesUtils.hexStringToBytes("FFFF9876543210E00000");
        byte[] ipekKcv = BytesUtils.hexStringToBytes("AF8C07");

        //load plaintext key，protect KeyType and Index must be 0x00！
        BytesTypeValue kcvBytesTypeValue = new BytesTypeValue();
        ipekKeyIndex = 0x01;
        ret = keyManager.loadDukptIpek(KeyType.KEY_TYPE_IPEK,
                ipekKeyIndex,
                AlgName.SYM_ARITH_3DES,
                0x00,
                0x00,
                ipekData,
                iKsnData,
                kcvBytesTypeValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("loadDukptIpek(plaintext) fail errCode =0x%x\n", ret));
            return;
        }
        byte[] ipekCheckKcv = kcvBytesTypeValue.getData();
        if (!Arrays.equals(ipekKcv, ipekCheckKcv)) {
            logMsg("loadDukptIpek(plaintext) success but kcv check failed\n");
            return;
        }
        logMsg("loadDukptIpek(plaintext) success and kcv check success\n");
        //Use it to encrypt and calculate mac
        test_dukpt_encrypt(ipekKeyIndex);
        test_dukpt_mac(ipekKeyIndex);
    }

    /**
     * Install the IPEK AES 256 plaintext key and verify it.
     */
    private void installIpekAesAddTest() {
        int ret;
        int ipekIndex = 0x03;

//         key FEDCBA9876543210F1F1F1F1F1F1F1F101020304050607080FFFF9876543210E
//         ksn 0FFFF9876543210E00000001
//         kcv 4E8047
        byte[] ipek = BytesUtils.hexStringToBytes("FEDCBA9876543210F1F1F1F1F1F1F1F101020304050607080FFFF9876543210E");
        byte[] ksn = BytesUtils.hexStringToBytes("0FFFF9876543210E00000001");

        BytesTypeValue bytesTypeValue = new BytesTypeValue();
        ret = keyManager.loadDukptAES(KeyType.KEY_TYPE_IPEK,
                ipekIndex,
                AlgName.SYM_ARITH_AES256,
                0,
                0,
                ipek,
                ksn,
                bytesTypeValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("loadDukptAES-256：failed！ errCode =0x%x\n", ret));
            return;
        }
        if (!"4E8047".equals(BytesUtils.byte2HexStr(bytesTypeValue.getData()))) {
            logMsg("loadDukptAES-256：KCV error！ " + BytesUtils.byte2HexStr(bytesTypeValue.getData()) + "\n");
            return;
        }
        logMsg("loadDukptAES-256：success！ \n");

        install_Dukpt_AES_ENDE(ipekIndex);
        install_Dukpt_AES_MAC(ipekIndex);
    }

    /**
     * Install a set of IPEK ciphertext keys, and perform encryption, decryption,
     * and MAC authentication.
     */
    private void installIpekEncAddTest() {
        int ret;
        int ipekKeyIndex = 0x02;
        int sfKeyIndex = 0x01;
        BytesTypeValue kcvBytesTypeValue = new BytesTypeValue();

//        Install an IPEK (With fix Protection)
//
//        fix plaintext:01020304050607080102030405060708 (hex format)
//        ipek plaintext:6AC292FAA1315B4D858AB3A3D7D5933A (hex format)
//        ipek kcv:AF8C07(hex format)
//        ipek ciphertext:AD9E29D5D1EA8E42B028289E05CA6D19 (hex format,encryption key:fix alg: aesecb)

        String encIpek = "AD9E29D5D1EA8E42B028289E05CA6D19";
        byte[] encIpekKcv = BytesUtils.hexStringToBytes("AF8C07");
        String ksn = "FFFF9876543210E00000";
        byte[] encIpekData = BytesUtils.hexStringToBytes(encIpek);
        byte[] encKsnData = BytesUtils.hexStringToBytes(ksn);

        ret = keyManager.loadDukptIpek(KeyType.KEY_TYPE_IPEK,
                ipekKeyIndex,
                AlgName.SYM_ARITH_3DES,
                KeyType.KEY_TYPE_FIXEDKEY,
                sfKeyIndex,
                encIpekData,
                encKsnData,
                kcvBytesTypeValue);

        if (ret == ErrCode.ERR_SUCCESS) {
            byte[] ipekCheckKcv = kcvBytesTypeValue.getData();
            if (Arrays.equals(encIpekKcv, ipekCheckKcv)) {
                logMsg("loadDukptIpek(enc)  success and kcv check success\n");
                //Use it to encrypt and calculate mac
                test_dukpt_encrypt(ipekKeyIndex);
                test_dukpt_mac(ipekKeyIndex);
            } else {
                logMsg("loadDukptIpek(enc) success but kcv check failed\n");
            }
        } else {
            logMsg(String.format("loadDukptIpek(enc) fail errCode = 0x%x\n", ret));
        }

    }


    /**
     *
     */
    private void installIpekAndEncryptTest() {
        String encIpek = "3FAA07DF07A77DD93721D789E556FF03";
        String ksn = "FFFFAB00010031E00002";
        byte[] encIpekData = BytesUtils.hexStringToBytes(encIpek);
        byte[] encKsnData = BytesUtils.hexStringToBytes(ksn);
        int ipekKeyIndex = 0x02;
        int sfKeyIndex = 0x00;
        BytesTypeValue kcvBytesTypeValue = new BytesTypeValue();
        int ret = keyManager.loadDukptIpek(KeyType.KEY_TYPE_IPEK,
                ipekKeyIndex,
                AlgName.SYM_ARITH_3DES,
                0x00,
                sfKeyIndex,
                encIpekData,
                encKsnData,
                kcvBytesTypeValue);
        keyManager.increaseKSN(KeyType.KEY_TYPE_IPEK,ipekKeyIndex);
        if(ret == 0){
            byte[] initVector = BytesUtils.hexStringToBytes("11546A899DF8C8B3");
            byte[] encData = BytesUtils.hexStringToBytes("97BB55502BC8151733CB98D8F770A33E");
            BytesTypeValue resultBytesTypeValue = new BytesTypeValue();
            ret = keyManager.symDecryptByIndex(KeyType.KEY_TYPE_IPEK,
                    ipekKeyIndex,
                    AlgName.SYM_ARITH_3DES,
                    EncryptionMode.SYM_ARITH_CBC,
                    initVector,
                    initVector.length,
                    encData,
                    encData.length,
                    EncryptionPaddingMode.PADDING_MODE_DEK_P,
                    resultBytesTypeValue);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg(String.format("install Ipek And Encrypt (DEK_P) Test:failed！ errCode =0x%x\n", ret));
            } else {
                logMsg("install Ipek And Encrypt (DEK_P) Test:success！ \n"+BytesUtils.byte2HexStr(resultBytesTypeValue.getData()));
            }
        }else {
            logMsg("loadDukptIpek:failed！ \n");
        }
    }


    /**
     * Ipek Plaintext key added KSN.
     */
    private void ipekAddKsnTest() {
        int ret;
        BytesTypeValue bytesTypeValue = new BytesTypeValue();

        ret = keyManager.exportDukptKsn(KeyType.KEY_TYPE_IPEK, 0x01, bytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            logMsg("exportDukptKsn success ksn:" + BytesUtils.byte2HexStr(bytesTypeValue.getData()) + "\n");
        } else {
            logMsg(String.format("exportDukptKsn：failed！ errCode =0x%x\n", ret));
        }

        ret = keyManager.increaseKSN(KeyType.KEY_TYPE_IPEK, 0x01);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("increaseKSN：failed！ errCode =0x%x\n", ret));
        } else {
            logMsg("increaseKSN：success！ \n");
        }

        byte[] ksn = bytesTypeValue.getData();
        ksn[ksn.length - 1] += 1;
        ret = keyManager.exportDukptKsn(KeyType.KEY_TYPE_IPEK, 0x01, bytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            if (BytesUtils.byte2HexStr(ksn).equals(BytesUtils.byte2HexStr(bytesTypeValue.getData()))) {
                logMsg("exportDukptKsn success ksn:" + BytesUtils.byte2HexStr(bytesTypeValue.getData()) + "\n");
            } else {
                logMsg("exportDukptKsn success but data error！" + BytesUtils.byte2HexStr(bytesTypeValue.getData()) + "\n");
            }
        } else {
            logMsg(String.format("exportDukptKsn：failed！ errCode =0x%x\n", ret));
        }

    }

    /**
     * Install a set of clear text MK keys.
     */
    private void installMk() {
        int ret;
        int mkKeyIndex = 0x01;

//         mk plaintext:01020304050607080102030405060708 (hex format)
//         mk kcv:B073DC(hex format)
        String mk = "01020304050607080102030405060708";
        byte[] mkKcv = BytesUtils.hexStringToBytes("B073DC");

        // Install an MK key
        byte[] keyData = BytesUtils.hexStringToBytes(mk);
        //load plaintext key，protect KeyType And Index must be 0x00！
        ret = keyManager.loadSymKeyWithKCV(KeyType.KEY_TYPE_MK,
                mkKeyIndex,
                AlgName.SYM_ARITH_3DES,
                0x00,
                0x00,
                keyData,
                keyData.length,
                mkKcv,
                mkKcv.length);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("loadSymKeyWithKCV(MK) fail errCode =0x%x\n", ret));
            return;
        }
        logMsg("loadSymKeyWithKCV(MK) success. \n");
    }

    /**
     * Install a set of ciphertext MK keys.
     */
    private void installMkEnc() {
        int ret;
        int mkKeyIndex = 0x02;
        int sfKeyIndex = 0x01;

//         fixedKey plaintext:01020304050607080102030405060708 (hex format)
//         fixedKey kcv:D2DD9D(AES CKCV  hex format)
//         mk plaintext:01020304050607080102030405060708 (hex format)
//         mk kcv:B073DC(hex format)
//         mk ciphertext:E66AC2F214E2AF137922C1291C57F00D (hex format)(encryption key:fixedKey alg: aesecb)

        String encMk = "E66AC2F214E2AF137922C1291C57F00D";
        byte[] encMkKcv = BytesUtils.hexStringToBytes("B073DC");
        byte[] keyData = BytesUtils.hexStringToBytes(encMk);

        //load plaintext key，protect KeyType And Index must be 0x00！
        ret = keyManager.loadSymKeyWithKCV(KeyType.KEY_TYPE_MK,
                mkKeyIndex,
                AlgName.SYM_ARITH_3DES,
                KeyType.KEY_TYPE_FIXEDKEY,
                sfKeyIndex,
                keyData,
                keyData.length,
                encMkKcv,
                encMkKcv.length);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("loadSymKeyWithKCV(MK) fail errCode =0x%x\n", ret));
            return;
        }
        logMsg("loadSymKeyWithKCV(MK)  success and kcv check success\n");
    }

    /**
     * Install a group of session key DEK, and do encryption and decryption, MAC test.
     *
     * @param mkKeyIndex Protect key index.
     */
    private void installMkDekAddTest(int mkKeyIndex) {
        int ret;
        int dekKeyIndex = 0x01;


//        Install an DEK key (With MK Protection)
//
//         mk plaintext:01020304050607080102030405060708(hex format)
//         dek plaintext: 08070605040302010807060504030201(hex format)
//         dek kcv:741D55(hex format)
//         dek ciphertext:8561EF6C37B0C9C48561EF6C37B0C9C4 (hex format,encryption key:mk alg: 3desecb)

        String dek = "8561EF6C37B0C9C48561EF6C37B0C9C4";
        byte[] keyData = BytesUtils.hexStringToBytes(dek);
        byte[] dekKcv = BytesUtils.hexStringToBytes("741D55");

        ret = keyManager.loadSymKeyWithKCV(KeyType.KEY_TYPE_DEK,
                dekKeyIndex,
                AlgName.SYM_ARITH_3DES,
                KeyType.KEY_TYPE_MK,
                mkKeyIndex,
                keyData,
                keyData.length,
                dekKcv,
                dekKcv.length);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("loadSymKeyWithKCV(DEK) fail errCode =0x%x\n", ret));
            return;
        }
        logMsg("loadSymKeyWithKCV(DEK)  success\n");
        //Use it to encrypt
        test_sym_encrypt(KeyType.KEY_TYPE_DEK, dekKeyIndex);
    }

    /**
     * Install a group of session key MEK, and do encryption and decryption, MAC test.
     *
     * @param mkKeyIndex Protect key index.
     */
    private void installMkMekAddTest(int mkKeyIndex) {

        int ret;
        int mekKeyIndex = 0x01;

//         Install an MEK key (With MK Protection)
//
//         mek plaintext: 08070605040302010807060504030201(hex format)
//         mek kcv:741D55(hex format)
//         mek ciphertext:8561EF6C37B0C9C48561EF6C37B0C9C4 (hex format,encryption key:tlk alg: 3desecb)

        String mek = "8561EF6C37B0C9C48561EF6C37B0C9C4";
        byte[] keyData = BytesUtils.hexStringToBytes(mek);
        byte[] mekKcv = BytesUtils.hexStringToBytes("741D55");
        ret = keyManager.loadSymKeyWithKCV(KeyType.KEY_TYPE_MEK,
                mekKeyIndex,
                AlgName.SYM_ARITH_3DES,
                KeyType.KEY_TYPE_MK,
                mkKeyIndex,
                keyData,
                keyData.length,
                mekKcv,
                mekKcv.length);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("loadSymKeyWithKCV(MEK) fail errCode =0x%x\n", ret));
            return;
        }
        logMsg("loadSymKeyWithKCV(MEK)  success\n");
        //Use it to calculate mac
        test_sym_mac(KeyType.KEY_TYPE_MEK, mekKeyIndex);

    }

    /**
     * Install a group of session key MEK.
     *
     * @param mkKeyIndex Protect key index.
     */
    private void installMkPekAddTest(int mkKeyIndex) {
        int ret;
        int pekKeyIndex = 0x01;

//         Install an PEK key (With MK Protection)
//
//         mk plaintext:01020304050607080102030405060708(hex format)
//         dek plaintext: 08070605040302010807060504030201(hex format)
//         dek kcv:741D55(hex format)
//         dek ciphertext:8561EF6C37B0C9C48561EF6C37B0C9C4 (hex format,encryption key:fx alg: 3desecb)

        String pek = "8561EF6C37B0C9C48561EF6C37B0C9C4";
        byte[] keyData = BytesUtils.hexStringToBytes(pek);
        byte[] dekKcv = BytesUtils.hexStringToBytes("741D55");

        ret = keyManager.loadSymKeyWithKCV(KeyType.KEY_TYPE_PEK,
                pekKeyIndex,
                AlgName.SYM_ARITH_3DES,
                KeyType.KEY_TYPE_MK,
                mkKeyIndex,
                keyData,
                keyData.length,
                dekKcv,
                dekKcv.length);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("loadSymKeyWithKCV(PEK) fail errCode =0x%x\n", ret));
            return;
        }
        logMsg("loadSymKeyWithKCV(PEK)  success\n");
    }

    private void install_Dukpt_AES_ENDE(int ipekIndex) {
        int ret;
        String initVector;
        String data;

        initVector = "00000000000000000000000000000000";
        BytesTypeValue bytesTypeValue = new BytesTypeValue();

        data = "900D314BF59C1E4A25BFD725E12E547F52EEFCFF5C4848591FF8ADB050ADF220E4745D3566503ADFA2A0ECC7D597F6B73D079928E27EFE1C1C59AC4F0A99C9D5";
        ret = keyManager.DukptAES_EncryptByIndex(0,
                ipekIndex,
                KeyUsage.KEY_USAGE_DATA_ENCRYPTION_ENCRYPT,
                AlgName.SYM_ARITH_AES256,
                EncryptionMode.SYM_ARITH_ECB,
                BytesUtils.hexStringToBytes(initVector), 16,
                BytesUtils.hexStringToBytes(data),
                BytesUtils.hexStringToBytes(data).length,
                EncryptionPaddingMode.PADDING_MODE_NONE,
                bytesTypeValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("DukptAES_EncryptByIndex-256：failed！ errCode =0x%x\n", ret));
            return;
        }
        logMsg("DukptAES_EncryptByIndex-256：success！ \n");
        ret = keyManager.DukptAES_DecryptByIndex(0,
                ipekIndex,
                KeyUsage.KEY_USAGE_DATA_ENCRYPTION_ENCRYPT,
                AlgName.SYM_ARITH_AES256,
                EncryptionMode.SYM_ARITH_ECB,
                BytesUtils.hexStringToBytes(initVector),
                16,
                bytesTypeValue.getData(),
                bytesTypeValue.getData().length,
                EncryptionPaddingMode.PADDING_MODE_NONE,
                bytesTypeValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("DukptAES_DecryptByIndex-256：failed！ errCode =0x%x\n", ret));
            return;
        }
        if (data.equals(BytesUtils.byte2HexStr(bytesTypeValue.getData()))) {
            logMsg("DukptAES_DecryptByIndex-256：success！ \n");
        } else {
            logMsg("DukptAES_DecryptByIndex-256：WERROR！" + BytesUtils.byte2HexStr(bytesTypeValue.getData()) + "\n");
        }
    }

    private void install_Dukpt_AES_MAC(int ipekIndex) {
        int ret;
        String initVector;
        String data;
        initVector = "00000000000000000000000000000000";
        BytesTypeValue bytesTypeValue = new BytesTypeValue();

        data = "30313030f23e069529e081800000000030303730353730303330303330303039393939393939393939393930363239313332363233303030";
        ret = keyManager.DukptAES_macByIndex(0,
                ipekIndex,
                KeyUsage.KEY_USAGE_MESSAGE_AUTHENTICATION_GENERATION,
                AlgName.SYM_ARITH_AES256,
                BytesUtils.hexStringToBytes(initVector),
                16,
                BytesUtils.hexStringToBytes(data),
                BytesUtils.hexStringToBytes(data).length,
                MacMode.MAC_MODE_9797_1_M2,
                bytesTypeValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("DukptAES_macByIndex-256：failed！ errCode =0x%x\n", ret));
            return;
        }
        logMsg("DukptAES_macByIndex-256：success！" + BytesUtils.byte2HexStr(bytesTypeValue.getData()) + "\n");
    }

    private void test_dukpt_mac(int ipekKeyIndex) {
        int ret;
        byte[] initVector = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};

        // ret = keyManager.increaseKSN(KEY_TYPE_IPEK,ipekKeyIndex); // After import If execute it , exportDukptKsn will be:FFFF9876543210E00002(hex format)
        BytesTypeValue ksnBytesTypeValue = new BytesTypeValue();
        ret = keyManager.exportDukptKsn(KeyType.KEY_TYPE_IPEK, ipekKeyIndex, ksnBytesTypeValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            // After import , exportDukptKsn will be FFFF9876543210E00001(hex format)
            logMsg(String.format("export ksn failed！ errCode =0x%x\n", ret));
            return;
        }
        logMsg("current ksn:" + BytesUtils.byte2HexStr(ksnBytesTypeValue.getData()));

        // macByIndex(IPEK) test
        BytesTypeValue macBytesTypeValue = new BytesTypeValue();
        ret = keyManager.macByIndex(KeyType.KEY_TYPE_IPEK,
                ipekKeyIndex,
                AlgName.SYM_ARITH_3DES,
                initVector,
                initVector.length,
                data,
                data.length,
                MacMode.MAC_MODE_9797_1_M1,
                0,
                macBytesTypeValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg(String.format("macByIndex(ipek) fail errCode = 0x%x\n", ret));
            return;
        }

        // After import, First use
        //current ksn:FFFF9876543210E00001(hex format)
        //current dukpt MEK_P:042666B46E84CFA368DE96282F397BC9(hex format)
        //plaintext:0102030405060708 (hex format)
        //mac result:5DF2DF7DC090F8F0(hex format,encryption key:dukpt MEK_P alg: 3desecb paddingMode:PADDING_MODE_9797_1_M2)
        byte[] macData = macBytesTypeValue.getData();
        logMsg("macByIndex(ipek) success:");
        logMsg("result:" + BytesUtils.byte2HexStr(macData) + "\n");
    }

    private void test_dukpt_encrypt(int ipekKeyIndex) {
        int ret;
        byte[] initVector = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,};
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};

        BytesTypeValue ksnBytesTypeValue = new BytesTypeValue();
        ret = keyManager.exportDukptKsn(KeyType.KEY_TYPE_IPEK, ipekKeyIndex, ksnBytesTypeValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            // After import , exportDukptKsn will be FFFF9876543210E00001(hex format)
            logMsg(String.format("export ksn failed！ errCode =0x%x\n", ret));
            return;
        }
        logMsg("current ksn:" + BytesUtils.byte2HexStr(ksnBytesTypeValue.getData()));

        // symEncryptByIndex(IPEK) test
        BytesTypeValue resultBytesTypeValue = new BytesTypeValue();
        ret = keyManager.symEncryptByIndex(KeyType.KEY_TYPE_IPEK,
                ipekKeyIndex,
                AlgName.SYM_ARITH_3DES,
                EncryptionMode.SYM_ARITH_CBC,
                initVector,
                initVector.length,
                data,
                data.length,
                EncryptionPaddingMode.PADDING_MODE_9797_1_M2,
                resultBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            // After import, First use
            //current ksn:FFFF9876543210E00001(hex format)
            //current dukpt DEK_Q:448D3F076D8304036A55A3D7E0055A78(hex format)
            //plaintext:0102030405060708 (hex format)
            //encrypted result:124F7A32F3F841872EA993AA4FEC1BCA(hex format,encryption key:dukpt DEK_Q alg: 3desecb paddingMode:PADDING_MODE_9797_1_M2)
            byte[] encData = resultBytesTypeValue.getData();
            //byte[] encCheckData = hexStringToBytes("124F7A32F3F841872EA993AA4FEC1BCA");
            logMsg("symEncryptByIndex(ipek) success:");
            logMsg("initVector:\n" + BytesUtils.byte2HexStr(initVector));
            logMsg("plaintext:\n" + BytesUtils.byte2HexStr(data));
            logMsg("encrypted result:\n" + BytesUtils.byte2HexStr(encData) + "\n");

            ret = keyManager.symDecryptByIndex(KeyType.KEY_TYPE_IPEK,
                    ipekKeyIndex,
                    AlgName.SYM_ARITH_3DES,
                    EncryptionMode.SYM_ARITH_CBC,
                    initVector,
                    initVector.length,
                    encData,
                    encData.length,
                    EncryptionPaddingMode.PADDING_MODE_9797_1_M2,
                    resultBytesTypeValue);
            if (ret == ErrCode.ERR_SUCCESS) {
                byte[] plaintextData = resultBytesTypeValue.getData();
                logMsg("symDecryptByIndex(ipek) success:");
                logMsg("initVector:\n" + BytesUtils.byte2HexStr(initVector));
                logMsg("encrypted:\n" + BytesUtils.byte2HexStr(encData));
                logMsg("decrypt result:\n" + BytesUtils.byte2HexStr(plaintextData) + "\n");
            } else {
                logMsg(String.format("symDecryptByIndex(ipek) fail errCode = 0x%x\n", ret));
            }
        } else {
            logMsg(String.format("symEncryptByIndex(ipek) fail errCode = 0x%x\n", ret));
        }
    }

    private void test_sym_encrypt(int keyType, int keyIndex) {
        int ret;
        byte[] initVector = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,};
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};

        // symEncryptByIndex(SYM) test
        BytesTypeValue resultBytesTypeValue = new BytesTypeValue();
        ret = keyManager.symEncryptByIndex(keyType,
                keyIndex,
                AlgName.SYM_ARITH_3DES,
                EncryptionMode.SYM_ARITH_CBC,
                initVector,
                initVector.length,
                data,
                data.length,
                EncryptionPaddingMode.PADDING_MODE_9797_1_M2,
                resultBytesTypeValue);

        if (ret == ErrCode.ERR_SUCCESS) {
            //current key:08070605040302010807060504030201(hex format)
            //plaintext:0102030405060708 (hex format)
            //encrypted result:7A64CD1B4FEEB592600F7955DC914657(hex format,encryption key:dek  alg: 3desecb paddingMode:PADDING_MODE_9797_1_M2)
            byte[] encData = resultBytesTypeValue.getData();
            //byte[] encCheckData = hexStringToBytes("7A64CD1B4FEEB592600F7955DC914657");
            logMsg("symEncryptByIndex(sym) success:");
            logMsg("initVector:\n" + BytesUtils.byte2HexStr(initVector));
            logMsg("plaintext:\n" + BytesUtils.byte2HexStr(data));
            logMsg("encrypted result:\n" + BytesUtils.byte2HexStr(encData) + "\n");
            ret = keyManager.symDecryptByIndex(keyType,
                    keyIndex,
                    AlgName.SYM_ARITH_3DES,
                    EncryptionMode.SYM_ARITH_CBC,
                    initVector,
                    initVector.length,
                    encData,
                    encData.length,
                    EncryptionPaddingMode.PADDING_MODE_9797_1_M2,
                    resultBytesTypeValue);

            if (ret == ErrCode.ERR_SUCCESS) {
                byte[] plaintextData = resultBytesTypeValue.getData();
                logMsg("symDecryptByIndex(sym) success:");
                logMsg("initVector:\n" + BytesUtils.byte2HexStr(initVector));
                logMsg("encrypted:\n" + BytesUtils.byte2HexStr(encData));
                logMsg("decrypt result:\n" + BytesUtils.byte2HexStr(plaintextData) + "\n");
            } else {
                logMsg(String.format("symDecryptByIndex(sym) fail errCode = 0x%x\n", ret));
            }
        } else {
            logMsg(String.format("symEncryptByIndex(sym) fail errCode = 0x%x\n", ret));
        }
    }

    void test_sym_mac(int keyType, int keyIndex) {
        int ret;
        byte[] initVector = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,};
        byte[] data = new byte[]{0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08};
        // macByIndex(MEK) test
        BytesTypeValue macBytesTypeValue = new BytesTypeValue();
        ret = keyManager.macByIndex(keyType,
                keyIndex,
                AlgName.SYM_ARITH_3DES,
                initVector,
                initVector.length,
                data,
                data.length,
                MacMode.MAC_MODE_9797_1_M1,
                0,
                macBytesTypeValue);
        if (ret == ErrCode.ERR_SUCCESS) {
            //test MEK:08070605040302010807060504030201(hex format)
            //plaintext:0102030405060708 (hex format)
            //mac result:7A64CD1B4FEEB592(hex format,mac key:mek alg: 3desecb paddingMode:PADDING_MODE_9797_1_M2)
            byte[] macData = macBytesTypeValue.getData();
            logMsg("macByIndex(mek) success:");
            logMsg("result:" + BytesUtils.byte2HexStr(macData) + "\n");
        } else {
            logMsg(String.format("macByIndex(mek) fail errCode = 0x%x\n", ret));
        }
    }

    public synchronized void logMsg(String msg) {
        runOnUiThread(() -> {
            String oldMsg = mShowResultTv.getText().toString();
            mShowResultTv.setText(String.format("%s\n%s", oldMsg, msg));
        });

    }
}