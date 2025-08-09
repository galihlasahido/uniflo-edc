package com.ftpos.pay.demo.view;

import static com.ftpos.library.smartpos.errcode.ErrCode.ERR_PIN_BYPASS;
import static com.ftpos.library.smartpos.errcode.ErrCode.ERR_SUCCESS;
import static com.ftpos.library.smartpos.keymanager.AlgName.SYM_ARITH_3DES;
import static com.ftpos.library.smartpos.keymanager.KeyType.KEY_TYPE_IPEK;
import static com.ftpos.library.smartpos.keymanager.KeyType.KEY_TYPE_MK;
import static com.ftpos.library.smartpos.keymanager.KeyType.KEY_TYPE_PEK;
import static com.ftpos.library.smartpos.printer.AlignStyle.PRINT_STYLE_CENTER;
import static com.ftpos.library.smartpos.printer.AlignStyle.PRINT_STYLE_LEFT;
import static com.ftpos.pay.demo.tlv.EMVTag.EMV_TAG95_MAP;
import static com.ftpos.pay.demo.tlv.EMVTag.EMV_TAG_MAP;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.ftpos.apiservice.aidl.led.LedConfig;
import com.ftpos.library.smartpos.datautils.BytesTypeValue;
import com.ftpos.library.smartpos.device.Device;
import com.ftpos.library.smartpos.emv.Amount;
import com.ftpos.library.smartpos.emv.CAPublicKeyInfo;
import com.ftpos.library.smartpos.emv.CandidateAIDInfo;
import com.ftpos.library.smartpos.emv.Emv;
import com.ftpos.library.smartpos.emv.IActionFlag;
import com.ftpos.library.smartpos.emv.IKernelINSInfo;
import com.ftpos.library.smartpos.emv.OnEmvResponse;
import com.ftpos.library.smartpos.emv.OnSearchCardCallback;
import com.ftpos.library.smartpos.emv.TrackData;
import com.ftpos.library.smartpos.emv.TransRequest;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.icreader.IcReader;
import com.ftpos.library.smartpos.keymanager.KeyManager;
import com.ftpos.library.smartpos.led.Led;
import com.ftpos.library.smartpos.magreader.MagReader;
import com.ftpos.library.smartpos.nfcreader.NfcReader;
import com.ftpos.library.smartpos.posSystem.PosSystem;
import com.ftpos.library.smartpos.printer.OnPrinterCallback;
import com.ftpos.library.smartpos.printer.PrintStatus;
import com.ftpos.library.smartpos.printer.Printer;
import com.ftpos.pay.demo.FtApplication;
import com.ftpos.pay.demo.SvrHelper;
import com.ftpos.pay.demo.bean.CAPublicKeyBean;
import com.ftpos.pay.demo.bean.CRLBean;
import com.ftpos.pay.demo.bean.EMVAcquirerParamsBean;
import com.ftpos.pay.demo.bean.EMVAppParamsBean;
import com.ftpos.pay.demo.bean.EMVCLAppParamsBean;
import com.ftpos.pay.demo.bean.EMVCLDRLBean;
import com.ftpos.pay.demo.bean.ExceptionListBean;
import com.ftpos.pay.demo.bean.XmlDataBean;
import com.ftpos.pay.demo.constants.ICardType;
import com.ftpos.pay.demo.constants.IParamType;
import com.ftpos.pay.demo.pinpad.IPinpadCode;
import com.ftpos.pay.demo.pinpad.PinpadDialog;
import com.ftpos.pay.demo.pinpad.RFLogoDialog;
import com.ftpos.pay.demo.tlv.EMVTag;
import com.ftpos.pay.demo.tlv.TLV;
import com.ftpos.pay.demo.tlv.TLVElement;
import com.ftpos.pay.demo.tlv.TLVList;
import com.ftpos.pay.demo.utils.BytesUtil;
import com.ftpos.pay.demo.utils.XmlParser;
import com.jirui.logger.Logger;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;

/**
 * @author GuoJirui.
 * @date 2021/4/25.
 * @desc
 */
public abstract class BaseEmvActivity extends BaseActivity implements SvrHelper.ServiceListener {
    public static final int TIMEOUT_SEARCH_CARD = 10;   //Suggestion: Set the timeout period to be smaller than that of the EMV process
    public static final int TIMEOUT_PINPAD = 20;        //Suggestion: Set the timeout period to be smaller than that of the EMV process

    public static final int REQUEST_CODE_PIN = 1001;
    protected Led led;
    protected Emv iemv;
    protected Printer printer = null;
    protected IcReader icReader = null;
    protected NfcReader nfcReader = null;
    protected MagReader magReader = null;
    protected KeyManager ikey;

    protected Device device = null;
    protected boolean hasInitEmv = false;

    private TransRequest transRequest = null;
    private Amount amount = null;

    //Handle from the start of the transaction to the end of inputting PIN
    boolean prePINPhase = false;
    //EMV Status， true running； false stop
    boolean emvStatus = false;

    boolean isSeePhone = false;

    private PinpadDialog mPinpadDialog;
    private RFLogoDialog mRFLogoDialog;
    private Timer timer;

    protected abstract void cancelTransaction();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SvrHelper.instance().setServiceListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //The EMV process is running and before the cardholder is authenticated
        //If the current Activity stop, the EMV process is stopped
        if (prePINPhase & emvStatus) {
            emvStatus = false;
            SvrHelper.instance().cancelOperation();
            stopEmv(IPinpadCode.USER_TURN_OFF, null);
        }
    }

    @Override
    public void onBackPressed() {
        //Disable the back key during a transaction
        if (!emvStatus) {
            super.onBackPressed();
            if ("F360".equals(Build.MODEL)) {
                led.ledDefault();
            }
        } else {
            showTransactionBar();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Release resource
        if (emvStatus) {
            emvStatus = false;
            SvrHelper.instance().cancelOperation();
            stopEmv(IPinpadCode.OTHER_UNKNOWN, null);
        }
        if (mPinpadDialog != null) {
            mPinpadDialog.dismiss();
        }

        if (mRFLogoDialog != null) {
            mRFLogoDialog.dismiss();
        }
    }

    @Override
    public void onServerBinded() {
        led = SvrHelper.instance().getLED();
        iemv = SvrHelper.instance().getEmv();
        ikey = SvrHelper.instance().getKey();
        printer = SvrHelper.instance().getPrinter();
        icReader = SvrHelper.instance().getIcReader();
        nfcReader = SvrHelper.instance().getNfcReader();
        magReader = SvrHelper.instance().getMagReader();

        device = SvrHelper.instance().getDevice();

        updateDockLCD("WELCOME", " ", " ");
    }

    protected void updateDockLCD(String title, String amt, String tips) {
        if (printer == null) return;
        if (title != null) printer.showLineText(0, title, PRINT_STYLE_CENTER);
        if (amt != null) printer.showLineText(1, amt, PRINT_STYLE_CENTER);
        if (tips != null) printer.showLineText(2, tips, PRINT_STYLE_LEFT);
    }


    /**
     * Start the EMV process
     *
     * @param amount       amount
     * @param transRequest Transaction request body
     */
    protected void startEMV(Amount amount, TransRequest transRequest) {
        // F360 灯带在交易成功时会有定时任务，如果在任务结束前再次开始交易，会出现灯带显示错误，在这里先关闭
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        this.amount = amount;
        this.transRequest = transRequest;

        try {
            Logger.i("Start the EMV process");
            //start transaction
            SvrHelper.instance().setLed(false, false, false, true);

            iemv.startEMV(amount, transRequest, emvHandler);
            emvStatus = true;
            prePINPhase = true;
        } catch (Exception e) {
            Logger.e("The exception occurs in the EMV process ");
            Log.d(TAG, "startEMV: ", e);
        }
    }

    /**
     * Stop the EMV process
     */
    protected void stopEMV() {
        try {
            Logger.i("Stop the EMV process");
            iemv.stopEMV();
        } catch (Exception e) {
            Logger.e("The exception occurs in the EMV process ");
            Log.d(TAG, "stopEMV: ", e);
        }
    }

    /**
     * Clear all parameters
     *
     * @return true：success；false：fail；
     */
    protected boolean clearAllParameters() {
        if (iemv == null) {
            Logger.e("Clear All Parameters Fail, Emv is null");
            return false;
        }
        int ret = 0;
        ret = iemv.manageEmvAppParameters(IActionFlag.CLEAR, null);
        Logger.v("Clear application parameters of EMV contact card transaction: " + ret);
        ret = iemv.manageDRL(IActionFlag.CLEAR, null);
        Logger.v("Clear DRL parameters: " + ret);
        ret = iemv.manageCAPubKey(IActionFlag.CLEAR, null);
        Logger.v("Clear CA public key parameters: " + ret);
        ret = iemv.manageEmvclAppParameters(IActionFlag.CLEAR, null);
        Logger.v("Clear application parameters of EMV contactless card transaction: " + ret);
        ret = iemv.setCRL(IActionFlag.CLEAR, null);
        Logger.v("Clear certificate revocation list : " + ret);
        ret = iemv.setExceptionList(IActionFlag.CLEAR, null);
        Logger.v("Clear black list : " + ret);
        Logger.i("Clear All Parameters Successfully");
        return true;
    }

    /**
     * Update all parameters
     *
     * @return true：success；false：fail；
     */
    protected boolean updateAllParameters() {
        if (iemv == null) {
            Logger.e("Update All Parameters Fail, Emv is null");
            return false;
        }
        boolean ret = false;
        //
        ret = updateParametersByFile(IParamType.TYPE_EMV_ACQUIRER_PARAM);
        if (ret) {
            Logger.v("Set default parameters for EMV transactions success");
        } else {
            Logger.e("Set default parameters for EMV transactions fail");
        }
        ret = updateParametersByFile(IParamType.TYPE_APP_PARAM_EMV);
        if (ret) {
            Logger.v("Add EMV contact application parameters successfully");
        } else {
            Logger.e("Add EMV contact application parameters fail");
        }
        ret = updateParametersByFile(IParamType.TYPE_APP_PARAM_EMVCL);
        if (ret) {
            Logger.v("Add EMV contactless application parameters successfully");
        } else {
            Logger.e("Add EMV contactless application parameters fail");
        }
        ret = updateParametersByFile(IParamType.TYPE_CA_PUBKEY);
        if (ret) {
            Logger.v("Add CA public key parameters successfully");
        } else {
            Logger.e("Add CA public key parameters fail");
        }
        ret = updateParametersByFile(IParamType.TYPE_EMVCL_DRL);
        if (ret) {
            Logger.v("Add DRL parameters of EMV contactless successfully");
        } else {
            Logger.e("Add DRL parameters of EMV contactless fail");
        }
        ret = updateParametersByFile(IParamType.TYPE_CRL);
        if (ret) {
            Logger.v("Add certificate revocation list successfully");
        } else {
            Logger.e("Add certificate revocation list fail");
        }
        ret = updateParametersByFile(IParamType.TYPE_EXCEPTION_LIST);
        if (ret) {
            Logger.v("Add exceptionList parameters successfully");
        } else {
            Logger.e("Add exceptionList parameters fail");
        }
        Logger.i("Add All Parameters Successfully");

        //deleteEMVParameter();
        //deleteEMVCLParameter();
        return true;

    }

    private void deleteEMVParameter() {
        //9F06 07 A0000003330101
        //1F14 01 00
        byte[] data = BytesUtil.hexString2Bytes("9F0607A00000033301011F140100");
        int iRet = iemv.manageEmvAppParameters(IActionFlag.DELETE, data);
        if (iRet != 0) {
            Logger.e("Failed to delete EMV contact application parameters , Code [" + Integer.toHexString(iRet) + " ]：" + ErrCode.toString(iRet));
        }
    }


    private void deleteEMVCLParameter() {
        //9F0608A000000333010101 1F600107 1F140100 9C0100
        byte[] data = BytesUtil.hexString2Bytes("9F0608A0000003330101011F6001071F1401009C0100");
        int iRet = iemv.manageEmvclAppParameters(IActionFlag.DELETE, data);
        if (iRet != 0) {
            Logger.e("Failed to delete EMV contact application parameters , Code [" + Integer.toHexString(iRet) + " ]：" + ErrCode.toString(iRet));
        }
    }

    /**
     * Update parameter file via XML file
     *
     * @param type Parameter file definition
     * @return true：success；false：fail；
     */
    private boolean updateParametersByFile(IParamType type) {
        InputStream inputStream = null;
        List<XmlDataBean> xmlList = null;

        try {
            // Read parameter file
            inputStream = getAssets().open(type.getPath());
            byte[] rsvBuffer = new byte[inputStream.available()];
            int read = inputStream.read(rsvBuffer);
            if (read <= 0) {
                Logger.e("Read file fail , path : " + type.getPath());
                return false;
            }
            // Parse XML file data into  a list of objects
            xmlList = XmlParser.parseXmlFile(type, rsvBuffer);
        } catch (IOException | XmlPullParserException e) {
            Logger.e("IOException: " + e.getMessage());
            Log.d(TAG, "updateParametersByFile: ", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.d(TAG, "updateParametersByFile: ", e);
                }
            }
        }

        if (xmlList == null || xmlList.isEmpty()) {
            Logger.e("Parse file data fail , xml list is null ");
            return false;
        }
        boolean ret = false;
        int iRet;
        //The list of objects is loaded into the kernel separately according to the parameter file
        switch (type) {
            case TYPE_EMV_ACQUIRER_PARAM:
                for (XmlDataBean bean : xmlList) {
                    byte[] appData = ((EMVAcquirerParamsBean) bean).getBytes();

                    byte[] desData = new byte[((EMVAcquirerParamsBean) bean).getTlvLens()];
                    java.lang.System.arraycopy(appData, 0, desData, 0, ((EMVAcquirerParamsBean) bean).getTlvLens());
                    iRet = iemv.setDefaultAppParameters(desData);
                    if (iRet != 0) {
                        Logger.e("Set default parameters for EMV transactions fail, Code [" + Integer.toHexString(iRet) + " ]：" + ErrCode.toString(iRet));
                    }
                }
                break;
            case TYPE_APP_PARAM_EMV:
                for (XmlDataBean bean : xmlList) {
                    byte[] appData = ((EMVAppParamsBean) bean).getBytes();

                    byte[] desData = new byte[((EMVAppParamsBean) bean).getTlvLens()];
                    java.lang.System.arraycopy(appData, 0, desData, 0, ((EMVAppParamsBean) bean).getTlvLens());
                    iRet = iemv.manageEmvAppParameters(IActionFlag.ADD, desData);
                    if (iRet != 0) {
                        Logger.e("Failed to add EMV contact application parameters , Code [" + Integer.toHexString(iRet) + " ]：" + ErrCode.toString(iRet));
                    }
                }
                break;
            case TYPE_APP_PARAM_EMVCL:
                for (XmlDataBean bean : xmlList) {
                    byte[] appData = ((EMVCLAppParamsBean) bean).getBytes();

                    byte[] desData = new byte[((EMVCLAppParamsBean) bean).getTlvLens()];
                    java.lang.System.arraycopy(appData, 0, desData, 0, ((EMVCLAppParamsBean) bean).getTlvLens());
                    iRet = iemv.manageEmvclAppParameters(IActionFlag.ADD, desData);
                    if (iRet != 0) {
                        Logger.v("Failed to add EMV contactless application parameters , Code [" + Integer.toHexString(iRet) + " ]：" + ErrCode.toString(iRet));
                    }
                }
                break;
            case TYPE_CA_PUBKEY:
                CAPublicKeyInfo caPublicKey = new CAPublicKeyInfo();
                for (XmlDataBean bean : xmlList) {
                    byte[] appData = ((CAPublicKeyBean) bean).getBytes();

                    caPublicKey = ((CAPublicKeyBean) bean).getCAPublicKey();
                    iRet = iemv.manageCAPubKey(IActionFlag.ADD, caPublicKey);
                    if (iRet != 0) {
                        Logger.e("Failed to add  CA public key parameters , Code [" + Integer.toHexString(iRet) + " ]：" + ErrCode.toString(iRet));
                    }
                }
                break;
            case TYPE_EMVCL_DRL:
                for (XmlDataBean bean : xmlList) {
                    byte[] appData = ((EMVCLDRLBean) bean).getBytes();

                    byte[] desData = new byte[((EMVCLDRLBean) bean).getTlvLens()];
                    System.arraycopy(appData, 0, desData, 0, ((EMVCLDRLBean) bean).getTlvLens());
                    iRet = iemv.manageDRL(IActionFlag.ADD, desData);
                    if (iRet != 0) {
                        Logger.e("Failed to add DRL parameters of EMV contactless , Code [" + Integer.toHexString(iRet) + " ]：" + ErrCode.toString(iRet));
                    }
                }
                break;
            case TYPE_CRL:
                for (XmlDataBean bean : xmlList) {
                    byte[] appData = ((CRLBean) bean).getBytes();

                    byte[] desData = new byte[((CRLBean) bean).getTlvLens()];
                    System.arraycopy(appData, 0, desData, 0, ((CRLBean) bean).getTlvLens());
                    iRet = iemv.setCRL(IActionFlag.ADD, desData);
                    if (iRet != 0) {
                        Logger.e("Failed to add certificate revocation list , Code [" + Integer.toHexString(iRet) + " ]：" + ErrCode.toString(iRet));
                    }
                    ret = true;
                }
                break;
            case TYPE_EXCEPTION_LIST:
                for (XmlDataBean bean : xmlList) {
                    byte[] appData = ((ExceptionListBean) bean).getBytes();

                    byte[] desData = new byte[((ExceptionListBean) bean).getTlvLens()];
                    System.arraycopy(appData, 0, desData, 0, ((ExceptionListBean) bean).getTlvLens());
                    iRet = iemv.setExceptionList(IActionFlag.ADD, desData);
                    if (iRet != 0) {
                        Logger.e("Failed to add exceptionList parameters , Code [" + Integer.toHexString(iRet) + " ]：" + ErrCode.toString(iRet));
                    }
                }
                break;
        }
        return true;
    }

    /**
     * Check the card reader
     *
     * @param code Supported card types
     * @return true：success；false：fail；
     */
    protected boolean checkReader(int code) {
        Logger.i("Check the card reader: 0x" + BytesUtil.byte2Hex((byte) code));
        if ((code & ICardType.TYPE_CARD_MAGNETIC) == ICardType.TYPE_CARD_MAGNETIC) {
            Logger.v("Check the magnetic card reader...");

            if (magReader == null || magReader.checkMagCardreader() != 0) {
                Logger.e("The magnetic reader is not working");
                return false;
            }
        }
        if ((code & ICardType.TYPE_CARD_CONTACT) == ICardType.TYPE_CARD_CONTACT) {
            Logger.v("Check the IC card reader...");
            if (icReader == null || icReader.checkICReader() != 0) {
                Logger.e("The IC reader is not working");
                return false;
            }
        }
        if ((code & ICardType.TYPE_CARD_CONTACT_LESS) == ICardType.TYPE_CARD_CONTACT_LESS) {
            Logger.v("Check the NFC card reader...");
            if (nfcReader == null || nfcReader.checkNFCCardreader() != 0) {
                Logger.e("The NFC reader is not working");
                return false;
            }
        }
        return true;

    }


    /**
     * load IPEK (Initial PIN Encryption Key) to the key store.
     *
     * @return true：success；false：fail；
     */
    protected boolean loadDukptKey() {
        if (ikey == null) {
            Logger.e("Load Key Fail, key manager  is null");
            return false;
        }
        //Set the name of the key group.
        String packageName = getApplicationContext().getPackageName();
        int ret = ikey.setKeyGroupName(packageName);
        if (ret != 0) {
            Logger.e("Set the name of the key group fail, Code [" + Integer.toHexString(ret) + " ]：" + ErrCode.toString(ret));
            return false;
        }
        //Set the index of the key.
        int keyIndex = 0x01;

        String keyValue = "6AC292FAA1315B4D858AB3A3D7D5933A";
        byte[] bKeyValue = BytesUtil.hexString2Bytes(keyValue);

        String ksnValue = "FFFF9876543210E00000";
        byte[] bKsnValue = BytesUtil.hexString2Bytes(ksnValue);

        BytesTypeValue bValue = new BytesTypeValue();
        //This method is used to load IPEK (Initial PIN Encryption Key) to the key store.
        //in the case that ipek is in plaintext form, the protectKeyIndex must be 0x00.
        ret = ikey.loadDukptIpek(KEY_TYPE_IPEK, keyIndex, SYM_ARITH_3DES,
                0x00, 0x00, bKeyValue, bKsnValue, bValue);
        if (ret != ERR_SUCCESS) {
            Logger.e("load IPEK (Initial PIN Encryption Key) fail, Code [" + Integer.toHexString(ret) + " ]：" + ErrCode.toString(ret));
            return false;
        }

        Logger.i("Download dukpt key successfully");
        return true;
    }


    /**
     * load a symmetric key to the key store
     *
     * @return true：success；false：fail；
     */
    protected boolean loadSymKey() {
        if (ikey == null) {
            Logger.e("Load Key Fail, key manager  is null");
            return false;
        }
        //Set the name of the key group.
        String packageName = getApplicationContext().getPackageName();
        int ret = ikey.setKeyGroupName(packageName);
        if (ret != 0) {
            Logger.e("Set the name of the key group fail, Code [" + Integer.toHexString(ret) + " ]：" + ErrCode.toString(ret));
            return false;
        }
        BytesTypeValue bytesValue = new BytesTypeValue();
        int mkIndex = 0x02;
        String mkValue = "DF41169AA078D5AEE4C2F3B1ED0DFE56EE647CB6387ACDA5";
        byte[] bMkValue = BytesUtil.hexString2Bytes(mkValue);
        //This method is used to load a symmetric key to the key store.
        //There are two ways to load the key, one is to load the key in plaintext,
        //and another way is to load the ciphertext key under the protection of the protective key.
        ret = ikey.loadSymKey(KEY_TYPE_MK, mkIndex, SYM_ARITH_3DES, 0x00, 0x00, bMkValue, bMkValue.length, bytesValue);
        if (ret != ERR_SUCCESS) {
            Logger.e("load MK(Master Key) fail, Code [" + Integer.toHexString(ret) + " ]：" + ErrCode.toString(ret));
            return false;
        }
        //Import PEK: the key index uses 0x0001;
        //The imported PEK is ciphertext, which is encrypted by MK;
        //Plain text value:112233445566778899AABBCCDDEEFF00;
        //Cipher text value:592CA5782DE0EED7BB23037D2C79123E;
        int pekIndex = 0x0001;
        String pekValue = "592CA5782DE0EED7BB23037D2C79123E";
        byte[] bPekValue = BytesUtil.hexString2Bytes(pekValue);
        ret = ikey.loadSymKey(KEY_TYPE_PEK, pekIndex, SYM_ARITH_3DES,
                KEY_TYPE_MK, mkIndex, bPekValue, bPekValue.length, bytesValue);
        if (ret != ERR_SUCCESS) {
            Logger.e("load PEK fail, Code [" + Integer.toHexString(ret) + " ]：" + ErrCode.toString(ret));
            return false;
        }

        Logger.i("Download Symmetry key successfully");

        return true;
    }

    /**
     * Search card flow callback
     */
    private final OnSearchCardCallback callback = new OnSearchCardCallback() {
        /**
         * The card was successfully searched
         * @param type      Card type
         * @param trackData Track data, if swipe card
         */
        @Override
        public void onSuccess(int type, TrackData trackData) {
            led.ledCardIndicator(0x01, 0, 200, 200);
            if (mRFLogoDialog != null) {
                mRFLogoDialog.dismiss();
            }
            if (type == ICardType.TYPE_CARD_MAGNETIC) {
                Logger.d("Search card successful, type:" + "Magnetic card");
                Logger.i("TR1:" + trackData.getTrack1Data());
                Logger.i("TR2:" + trackData.getTrack2Data());
                Logger.i("TR3:" + trackData.getTrack3Data());

                String tr2 = trackData.getTrack2Data();
                if (tr2 != null) {
                    String[] data = tr2.split("=");
                    if (data.length > 0) {
                        Logger.i("PAN:" + data[0]);
                    }
                    if (data.length > 1 && data[1].length() > 7) {
                        Logger.i("EXPIRED_DATE:" + data[1].substring(0, 4));
                        Logger.i("SERVICE_CODE:" + data[1].substring(4, 7));
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.d(TAG, "onSuccess: ", e);
                }
                //search card success
                led.readerLedStatus(0x03, false, true, false);
                SvrHelper.instance().setLed(false, true, false, true);
                iemv.respondEvent(null);
            } else {
                if (type == 0x01) {
                    Logger.d("Search card successful, type:" + "Contact card");
                } else if (type == 0x02) {
                    Logger.d("Search card successful, type:" + "Contactless card");
                } else {
                    Logger.d("Search card successful, type:" + "unknown");
                }
                led.readerLedStatus(0x03, false, true, false);
                SvrHelper.instance().setLed(false, true, false, true);
                iemv.respondEvent(null);
            }
        }

        @Override
        public void onError(int errCode) {
            Logger.e("Search card failed, Error Code [" + Integer.toHexString(errCode) + " ]：" + ErrCode.toString(errCode));
            if (mRFLogoDialog != null) {
                mRFLogoDialog.dismiss();
            }
            //search card fail
            led.readerLedStatus(0x03, true, false, false);
            SvrHelper.instance().setLed(true, false, false, false);

            /**
             * If the card search fails, stopEMV is called to end the transaction execution
             */
            iemv.stopEMV();
        }
    };


    /**
     * Jump password keyboard interface
     *
     * @param type   Transaction Type
     * @param amount Amount
     */
    private void doInputPin(int cvmFlag, int type, Long amount) {
        if (!emvStatus) return;
        collapseStatusBar();
        PosSystem.getInstance(this)
                .enableTurningOffScreen(false, 60);

        int angle = ((WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        switch (angle) {
            case Surface.ROTATION_90:
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case Surface.ROTATION_270:
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
            case Surface.ROTATION_180:
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            default:
                this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
        }

        Logger.i("Inputting PIN...");
        Bundle bundle = new Bundle();
        //Customize configuration display information(no examples)
        bundle.putInt("Title", type);
        bundle.putLong("Amount", amount);
        bundle.putString("Tips", "Press OK if no PIN");
        if (cvmFlag == Emv.EMV_CVMFLAG_OLPIN_SIGN) {
            bundle.putString("PwdHint", "Enter Password");
        } else {
            bundle.putString("PwdHint", "Enter PIN");
        }
        updateDockLCD(null, null, "Enter pin ...");
        new Handler(Looper.getMainLooper())
                .post(() -> {
                    mPinpadDialog = new PinpadDialog(BaseEmvActivity.this, new Intent().putExtras(bundle), new PinpadDialog.PinpadResultListener() {
                        @Override
                        public void onPinpadResultListener(int code, String data) {
                            PosSystem.getInstance(BaseEmvActivity.this)
                                    .enableTurningOffScreen(true, 0);
                            BaseEmvActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                            handlePinpadResult(code, data);
                        }
                    });
                    mPinpadDialog.show();
                });

    }

    private CountDownLatch mCountDownLatch;

    void outputResult(int code, String data) {
        //Logger.d("outputResult: " + data);
        //Logger.i("Simulation: tips or print ");
        if (code == ErrCode.ERR_ONLINE_APPROVED
                || code == ErrCode.ERR_OFFLINE_APPROVED) {
            //transaction success
            if ("F360".equals(Build.MODEL)) {
                LedConfig ledConfig = new LedConfig(TapLampColor.Success.RED,
                        TapLampColor.Success.GREEN, TapLampColor.Success.BLUE);
                led.tapeLampOn(ledConfig, 100);
                FtApplication.getUiHandler().post(() -> {
                    if (timer != null) {
                        timer.cancel();
                        timer = null;
                    }
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            led.ledDefault();
                        }
                    }, 3 * 1000);
                });
            }
            SvrHelper.instance().setLed(false, true, true, true);
            updateDockLCD(null, null, "Txn. approved");
        } else {
            //transaction fail
            if ("F360".equals(Build.MODEL)) {
                LedConfig ledConfig = new LedConfig(TapLampColor.Failed.RED,
                        TapLampColor.Failed.GREEN, TapLampColor.Failed.BLUE);
                led.tapeLampOn(ledConfig, 100);
            }

            SvrHelper.instance().setLed(true, false, false, false);
            updateDockLCD(null, null, "Txn. fail");
        }
        mCountDownLatch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Log.d(TAG, "outputResult: ", e);
            }
            SvrHelper.instance().setLed(false, false, false, false);
            led.readerLedStatus(0x03, false, false, false);

            mCountDownLatch.countDown();
        }).start();

        if (code == ErrCode.ERR_ONLINE_APPROVED
                || code == ErrCode.ERR_OFFLINE_APPROVED
                || code == ErrCode.ERR_OFFLINE_DECLINED
                || code == ErrCode.ERR_ONLINE_DECLINED
                || code == ErrCode.ERR_ONLINE_END_CARD_DECLINED) {

            TLVList list = TLV.fromData(iemv.getTlvList("1F531F609C9A9F215A579F025F2A9F34959F339F409F669F1E"));
            Logger.tlv(list.toString(), EMV_TAG_MAP);
            if (list.contains("95")) {
//                Logger.bit(list.getTLV("95").getBytesValue());
                Logger.bit(list.getTLV("95").getBytesValue(), EMV_TAG95_MAP);
            }
            if (code == ErrCode.ERR_ONLINE_APPROVED
                    || code == ErrCode.ERR_OFFLINE_APPROVED
                    || code == ErrCode.ERR_OFFLINE_DECLINED) {

                try {
                    mCountDownLatch.await();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                printReceipt(list);
            }
        }
        Logger.d("Result: [" + Integer.toHexString(code) + "] " + ErrCode.toString(code));
        Logger.i("Transaction Finished");
        updateDockLCD("WELCOME", " ", " ");
    }


    protected abstract void doAppSelect(List<CandidateAIDInfo> list);

    protected abstract void doEndProcess(int code, String data);

    /**
     * EMV callback process
     */
    private final OnEmvResponse emvHandler = new OnEmvResponse() {
        /**
         * The EMV Kernel requires the cardholder to select an application from the list for the current transaction.
         * @param reselect  reselect
         * @param list AID candidate list.
         */
        @Override
        public void onAppSelect(boolean reselect, List<CandidateAIDInfo> list) {
            Logger.d("onAppSelect Whether to reselect:" + reselect);
            Logger.i("AID List: " + list.size());

            //Simulations: Select an application from the list of AID candidates list
            doAppSelect(list);
        }

        /**
         * The Kernel requires the cardholder to perform verification in accordance with the specified method.
         * @param cvm Card holder verification type
         */
        @Override
        public void onPinEntry(int cvm) {
            Logger.d("onPinEntry:" + cvm);
            if ((cvm & (Emv.EMV_CVMFLAG_PLOFFLINE_PIN_SIGN
                    | Emv.EMV_CVMFLAG_OLPIN_SIGN
                    | Emv.EMV_CVMFLAG_ENOFFLINE_PIN_SIGN)) == 0) {
                iemv.respondEvent(null);
                prePINPhase = false;
            } else {
                doInputPin(cvm, transRequest.getmTransType(), amount.getmAmount());
            }
        }


        /**
         * The Kernel requires the application to request authorization online from the card issuer.
         * @param data data
         */
        @Override
        public void onOnlineProcess(String data) {
            Logger.d("onOnlineProcess ");
            Logger.d("Trans Card Type: 0x%02X", iemv.getTransCardtype());
            updateDockLCD(null, null, "Online request ...");
            //Magnetic stripe card transaction is not to get cardholder verify method
            if (iemv.getTransCardtype() != ICardType.TYPE_CARD_MAGNETIC) {
                //get cardholder verify method
                int cvm = getCVM(iemv.getTlvList("9F34"));
                Logger.d("Cardholder verify method: 0x%02X", cvm);
            }

            //The data that the application layer organization needs to send to the server is obtained through the getTlvList interface, and the data is sent to the server.
            iemv.getTlvList("9C9A9F215A579F025F2A9F34959F339F409F669F1E");
            Logger.i("Simulated online interaction");
            //Simulation: Online request

            //Second card search process, callback to onSearchCardAgain()
            //The trigger condition：
            //First:You need to set up "setIssuerOnlineResponseData“ method  issuerAuthenticationData(91) or issuerScript71(71) or issuerScript72(72) parameters
            //Second:The card must support updating the issuer script data
            //Thirdly:   PayWave:9F66 in EMVCL_AppParameters.xml should support updating publisher script data
            //           Rupay:The terminal application version number 9F09 in EMVCL_AppParameters.xml is greater than or equal to 0002; In addition, if the update is Tag91, the kernel will use Tag91 to determine whether to do a secondary search
            iemv.setIssuerOnlineResponseData(0, null, "00", null, null, null);
            iemv.respondEvent(null);
        }

        /**
         * Kernel notifies the application that the transaction is over (no response required).
         * @param code The status of the transaction
         * @param data data
         */
        @Override
        public void onEndProcess(int code, String data) {
            doEndProcess(code, data);
        }

        /**
         * Display to the card number
         * @param s card number
         */
        @Override
        public void onDisplayPanInfo(String s) {
            Logger.d("onDisplayPanInfo, PAN:" + s);
            String tag6F = iemv.getTlvList("6F");
            Log.d(TAG, "onDisplayPanInfo_6F: " + tag6F);
            Logger.d("onDisplayPanInfo, 6F:" + tag6F);
        }

        /**
         * Search Card
         */
        @Override
        public void onSearchCard() {
            Logger.d("onSearchCard");
            // Perform the search card operation
            Logger.i("Build.MODEL:" + Build.MODEL);
            //
            if (Build.MODEL.equals("M200")
                    || Build.MODEL.equals("F310")
                    || Build.MODEL.equals("F310P")
                    || Build.MODEL.equals("F360")) {
                new Handler(Looper.getMainLooper())
                        .post(() -> {
                            mRFLogoDialog = new RFLogoDialog(BaseEmvActivity.this);
                            mRFLogoDialog.show();
                        });
            }
            List<LedConfig> colorList = new ArrayList<>();
            colorList.add(new LedConfig(TapLampColor.Breath.RED_0, TapLampColor.Breath.GREEN_0, TapLampColor.Breath.BLUE_0));
            colorList.add(new LedConfig(TapLampColor.Breath.RED_1, TapLampColor.Breath.GREEN_1, TapLampColor.Breath.BLUE_1));
            colorList.add(new LedConfig(TapLampColor.Breath.RED_2, TapLampColor.Breath.GREEN_2, TapLampColor.Breath.BLUE_2));
            led.breathOn(colorList, 100, 5, 5, 30);
            led.ledCardIndicator(0x03, 0, 500, 500);
            led.readerLedStatus(0x03, false, false, true);
            iemv.searchCard(TIMEOUT_SEARCH_CARD, callback);
            Logger.i("Searching Card...");
        }

        @Override
        public void onSearchCardAgain() {
            Logger.d("onSearchCardAgain");
            List<LedConfig> colorList = new ArrayList<>();
            colorList.add(new LedConfig(TapLampColor.Breath.RED_0, TapLampColor.Breath.GREEN_0, TapLampColor.Breath.BLUE_0));
            colorList.add(new LedConfig(TapLampColor.Breath.RED_1, TapLampColor.Breath.GREEN_1, TapLampColor.Breath.BLUE_1));
            colorList.add(new LedConfig(TapLampColor.Breath.RED_2, TapLampColor.Breath.GREEN_2, TapLampColor.Breath.BLUE_2));
            led.breathOn(colorList, 100, 5, 5, 30);

            led.ledCardIndicator(0x03, 0, 500, 500);
            /*
             * The EMV contactless transaction performs the update operation of the issuing bank.
             * The card search must be a contactless card. If a contact card or entry is used, an error will be reported.
             */
            if (Build.MODEL.equals("M200")
                    || Build.MODEL.equals("F310")
                    || Build.MODEL.equals("F310P")
                    || Build.MODEL.equals("F360")) {
                new Handler(Looper.getMainLooper())
                        .post(() -> {
                            mRFLogoDialog = new RFLogoDialog(BaseEmvActivity.this);
                            mRFLogoDialog.show();
                        });
            }
            led.readerLedStatus(0x03, false, false, true);
            updateDockLCD(null, null, "Present card");
            iemv.searchCard(TIMEOUT_SEARCH_CARD, callback);
            Logger.i("Searching Card Again...");
        }

        /**
         * Kernel returns to App after it encounters the interaction point set by App in advance.
         * @param step interaction point
         *       1:FinalSelect
         *       2:GetProcessOption
         *       3:ReadRecord
         *       4:OfflineDataAuthentication
         *       5:ProcessLimit
         *       6:CardHolderVerfy
         *       7:TerminalRisk
         *       8:TerminalActionAnalysis
         *       9:IssuerAuthentication
         */
        @Override
        public void onProcessInteractionPoint(int step) {
            Logger.d("onProcessInteractionPoint: " + step);
            if (step == 3) {     //ReadRecord
                //1.Check whether it matches the card BIN
                //1.1 If yes, set force online by setting the 1F0A label
                //iemv.setTLV("1F0A", "01");


                String df70 = iemv.getTlvList("9F4D");
                TextUtils.isEmpty(df70);
                Log.i("yaojm", "9F4D String " + df70);
            }
            iemv.respondEvent(null);
        }

        /**
         * The kernel requests the application to pass additional data elements.
         * @param coed     The types of operations that need to be performed.
         * @param data     request data
         * @param dataInformation    additional data
         */
        @Override
        public void onObtainData(int coed, byte[] data, byte[] dataInformation) {
            Logger.i("onObtainData, Kernel INS Info:" + coed);
            String tag = BytesUtil.bytes2HexString(data);
            if (coed == IKernelINSInfo.TAG_LIST) {
                //Currently used: Get whether the amount in the transaction log exceeds the limit 1F3E hexadecimal 4 bytes
                //Get the reference result of the issuing bank 1F10 0 rejected, 1 approved
                switch (tag) {
                    case "1F3E":
                        Logger.i("1F3E = Get the accumulated amount ");
                        iemv.setTLV("1F3E", "00000000");
                        break;
                    case "1F10":
                        Logger.i("1F10 = Execution result of the issuing bank ");
                        iemv.setTLV("1F10", "01");
                        break;
                    default:
                        break;
                }
            }
            if (coed == IKernelINSInfo.TLV_DATA) {
                Logger.v("paypass get DET ");
                //Currently only the DE function is used. Note that the DET data return format does not contain any DET data in TAG=1F6A, for example: a packet of DET data is 5A081122334455667788DF81100101,
                iemv.setTLV("1F6A", "5A081122334455667788DF81100101");
            }

            iemv.respondEvent(null);
        }

        /**
         * update transaction amount
         * @return amount
         */
        @Override
        public Amount onUpdateTransAmount() {
            Logger.v("onUpdateTransAmount:");
            Logger.i("Simulate update transaction amount");
            amount = new Amount(20, 0);
            return amount;
        }
    };

    /**
     * Require online PIN
     */
    public final static byte EMV_CVMFLAG_OLPIN_SIGN = 0x02;
    /**
     * Requires offline plaintext PIN
     */
    public final static byte EMV_CVMFLAG_PLOFFLINE_PIN_SIGN = 0x01;
    /**
     * Requires offline plaintext PIN with Signature
     */
    public final static byte EMV_CVMFLAG_PLOFFLINE_PIN_SIGNATURE_SIGN = 0x03;
    /**
     * Request offline PIN
     */
    public final static byte EMV_CVMFLAG_ENOFFLINE_PIN_SIGN = 0x04;
    /**
     * Requires offline PIN with Signature
     */
    public final static byte EMV_CVMFLAG_ENOFFLINE_PIN_SIGNATURE_SIGN = 0x05;
    /**
     * Request to signature
     */
    public final static byte EMV_CVMFLAG_SIGNATURE = 0x1E;
    /**
     * No CVM
     */
    public final static byte EMV_CVMFLAG_NO_CVM = 0x1F;

    private int getCVM(String tlvList) {
        TLVList tlvs = TLV.fromData(tlvList);
        byte[] value9F34 = tlvs.getTLV("9F34").getBytesValue();
        switch (value9F34[0] & 0x3F) {
            case 0x01:
                return EMV_CVMFLAG_PLOFFLINE_PIN_SIGN;
            case 0x02:
                return EMV_CVMFLAG_OLPIN_SIGN;
            case 0x03:
                return EMV_CVMFLAG_PLOFFLINE_PIN_SIGNATURE_SIGN;
            case 0x04:
                return EMV_CVMFLAG_ENOFFLINE_PIN_SIGN;
            case 0x05:
                return EMV_CVMFLAG_ENOFFLINE_PIN_SIGNATURE_SIGN;
            case 0x01E:
                return EMV_CVMFLAG_SIGNATURE;
            case 0x01F:
                return EMV_CVMFLAG_NO_CVM;
        }
        return 0;
    }

    /**
     * Stop the EMV process
     *
     * @param code Error code
     * @param data Error data
     */
    public void stopEmv(int code, String data) {
        switch (code) {
            case IPinpadCode.PINPAD_EXCEPTION:
                SvrHelper.instance().cancelOperation();
                Logger.e("Input PIN exception:" + data);
                break;
            case IPinpadCode.PINPAD_TIMEOUT:
                Logger.e("Input PIN timeout");
                break;
            case IPinpadCode.PINPAD_CANCEL:
                Logger.e("Input PIN cancel");
                break;
            case IPinpadCode.PINPAD_SCREEN_OFF:
                Logger.e("Screen off when create a PINPad");
                break;
            case IPinpadCode.USER_TURN_OFF:
                Logger.e("The transaction stops, because the user closes the screen or switches the application during the transaction  ");
                break;
            default:
                Logger.e("An unknown error occurred in the EMV ");
                break;
        }
        iemv.stopEMV();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_PIN) {
            PosSystem.getInstance(this).enableTurningOffScreen(true, 0);
            BaseEmvActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            //Handle password keyboard callbacks
            if (resultCode == RESULT_OK) {
                int code = IPinpadCode.PINPAD_UNKNOWN;
                if (data != null) {
                    String pinData = null;
                    if (data.hasExtra(IPinpadCode.PINPAD_BACK_CODE)) {
                        code = data.getIntExtra(IPinpadCode.PINPAD_BACK_CODE, IPinpadCode.PINPAD_UNKNOWN);
                    }
                    if (data.hasExtra(IPinpadCode.PINPAD_BACK_DATA)) {
                        pinData = data.getStringExtra(IPinpadCode.PINPAD_BACK_DATA);
                    }
                    Logger.i("PINPad \nResultCode：" + code + "\nData：" + pinData);
                    handlePinpadResult(code, pinData);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * handle the result of Pinpad
     *
     * @param code    code
     * @param pinData data
     */
    private void handlePinpadResult(int code, String pinData) {
        Logger.d("PinpadResult Code：" + code + " , Result：" + pinData);
        prePINPhase = false;
        if (code != IPinpadCode.PINPAD_SUCCESS && code != IPinpadCode.PINPAD_ERROR) {
            stopEmv(code, pinData);
        } else {
            String command = null;
            if (code == IPinpadCode.PINPAD_SUCCESS) {
                command = String.format("1F6301%02x", IPinpadCode.PIN_NORMAL);
            } else {
                if (pinData.length() > 18) { //Prevent conversion overflow
                    pinData = pinData.substring(0, 18);
                }

                long pinCode = 0L;
                try {
                    pinCode = Long.parseLong(pinData);
                } catch (Exception e) {
                    stopEmv(IPinpadCode.PINPAD_ERROR, pinData);
                    return;
                }
                if (pinCode == 0x63C0) {
                    command = String.format("1F6301%02x", IPinpadCode.OFFLINE_PIN_EXCEED_LIMIT);
                } else if (pinCode == 0x6983) {
                    command = String.format("1F6301%02x", IPinpadCode.OFFLINE_PIN_6983);
                } else if (pinCode == ERR_PIN_BYPASS) {
                    command = String.format("1F6301%02x", IPinpadCode.PIN_BYPASS);
                } else {
                    command = String.format("1F6301%02x", pinCode);
                }
            }

            iemv.respondEvent(command);
        }
    }


    /**
     * Print receipt demo, including gray setting, printing text, style setting, gray setting.
     * <p>
     * Function to demonstrate the call system interface and custom bitmap two ways to achieve
     * the function of printing receipts.
     * The advantage of calling the system interface is that it is easy to call without worrying
     * about the details of typesetting.
     * The advantage of the custom bitmap approach is that the display interface is completely
     * controlled by the user, with higher freedom.  If the receipt function is simple, the
     * first method is recommended to invoke the system interface. If the receipt content is
     * complex and the customized content is large, the second method is recommended.
     */
    void printReceipt(TLVList tlvs) {
        try {
            int ret;
            ret = printer.open();
            if (ret != ERR_SUCCESS) {
                Logger.i("open failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            ret = printer.startCaching();
            if (ret != ERR_SUCCESS) {
                Logger.i("startCaching failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            ret = printer.setGray(3);
            if (ret != ERR_SUCCESS) {
                Logger.i("startCaching failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            PrintStatus printStatus = new PrintStatus();
            ret = printer.getStatus(printStatus);
            if (ret != ERR_SUCCESS) {
                Logger.i("getStatus failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            Logger.i("Temperature = " + printStatus.getmTemperature() + "\n");
            Logger.i("Gray = " + printStatus.getmGray() + "\n");
            if (!printStatus.getmIsHavePaper()) {
                Logger.i("Printer out of paper\n");
                return;
            }

            printer.setAlignStyle(PRINT_STYLE_CENTER);
            printer.printStr("Receipt\n");

            printer.setAlignStyle(PRINT_STYLE_LEFT);
            printer.printStr("Please retain this receipt for your exchange.\n");
            printer.printStr("------------------------\n");

            for (int i = 0; i < tlvs.size(); i++) {
                TLVElement element = tlvs.getTLV(i);
                printer.printStr(EMVTag.toString(element.getTag()));
                printer.printStr(":");
                printer.printStr(element.getValue());
                printer.printStr("\n");
            }

            ret = printer.getUsedPaperLenManage();
            if (ret < 0) {
                Logger.i("getUsedPaperLenManage failed" + String.format(" errCode = 0x%x\n", ret));
            }

            Logger.i("UsedPaperLenManage = " + ret + "mm \n");
            printer.print(new OnPrinterCallback() {
                @Override
                public void onSuccess() {
                    Logger.i("print success\n");
                    printer.feed(32);
                }

                @Override
                public void onError(int i) {
                    Logger.i("printBmp failed" + String.format(" errCode = 0x%x\n", i));
                }
            });

        } catch (Exception e) {
            Logger.i("print failed" + e.toString() + "\n", e);
            Log.d(TAG, "printReceipt: ", e);
        }
    }


}
