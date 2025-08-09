package com.ftpos.ftappdemo;

import static com.ftpos.ftappdemo.util.Convection.Bytes2HexString;
import static com.ftpos.library.smartpos.errcode.ErrCode.ERR_SUCCESS;
import static com.ftpos.library.smartpos.nfcreader.NfcCardType.CARD_Mifare_M0;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ftpos.library.smartpos.datautils.BytesTypeValue;
import com.ftpos.library.smartpos.datautils.IntTypeValue;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.nfcreader.DesFireAuthenticateMode;
import com.ftpos.library.smartpos.nfcreader.DesFireCommMode;
import com.ftpos.library.smartpos.nfcreader.DesFireFileType;
import com.ftpos.library.smartpos.nfcreader.DesFireGetInfoFlag;
import com.ftpos.library.smartpos.nfcreader.DesFireResponse;
import com.ftpos.library.smartpos.nfcreader.DesFireValueFileOperationFlag;
import com.ftpos.library.smartpos.nfcreader.ISO14443_PollingInfo;
import com.ftpos.library.smartpos.nfcreader.MifareKey;
import com.ftpos.library.smartpos.nfcreader.NfcCardType;
import com.ftpos.library.smartpos.nfcreader.NfcReader;
import com.ftpos.library.smartpos.nfcreader.OnNfcPollingCallback;
import com.ftpos.library.smartpos.nfcreader.OnNfcReaderCallback;
import com.ftpos.library.smartpos.util.BytesUtils;

public class NfcCardActivity extends BaseActivity {

    private TextView mShowResultTv;
    private NfcReader nfcReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_card);

        initView();

        this.nfcReader = MainActivity.nfcReader;

    }

    private void initView() {
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText("Nfc Card View");
        findViewById(R.id.ukey_navbar_left_btn).setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        findViewById(R.id.nfc_demo).setOnClickListener(this);
        findViewById(R.id.iso15693_demo).setOnClickListener(this);
        findViewById(R.id.m0_demo).setOnClickListener(this);
        findViewById(R.id.m0_ev1_demo).setOnClickListener(this);
        findViewById(R.id.felica_demo).setOnClickListener(this);
        findViewById(R.id.m1_demo).setOnClickListener(this);
        findViewById(R.id.desfire_demo).setOnClickListener(this);
        findViewById(R.id.mifarePlus_demo).setOnClickListener(this);

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
            case R.id.nfc_demo: {
                nfc_demo();
            }
            break;
            case R.id.iso15693_demo: {
                iso15693_demo();
            }
            break;
            case R.id.m0_demo: {
                M0_demo();
            }
            break;
            case R.id.m0_ev1_demo:
                M0_ev1_demo();
                break;
            case R.id.m1_demo: {
                M1_demo();
            }
            break;
            case R.id.felica_demo: {
                nfc_felica_demo();
            }
            break;
            case R.id.desfire_demo: {
                desFire_demo();
            }
            break;
            case R.id.mifarePlus_demo: {
                mifarePlus_demo();
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

    void iso15693_demo() {
        ProgressDialog pgd;
        pgd = new ProgressDialog(this);
        pgd.setCancelable(false);
        pgd.setMessage("Please swing card...");
        if (!pgd.isShowing()) {
            pgd.show();
        }

        nfcReader.openCardEx(10, new OnNfcPollingCallback() {
            @Override
            public void onSuccess(ISO14443_PollingInfo iso14443_pollingInfo) {
                int cardType = iso14443_pollingInfo.getType();
                logMsg("CardType " + String.format(" = 0x%x\n", cardType));
                logMsg("UID = " + BytesUtils.byte2HexStr(iso14443_pollingInfo.getUID()) + "\n");

                int ret;
                BytesTypeValue readData = new BytesTypeValue();

                ret = nfcReader.iso15693Search();
                if (ret != ErrCode.ERR_SUCCESS) {
                    logMsg("iso15693Search failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }
                logMsg("iso15693Search success \n");

                ret = nfcReader.iso15693Select();
                if (ret != ErrCode.ERR_SUCCESS) {
                    logMsg("iso15693Select failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }
                logMsg("iso15693Select success \n");

                ret = nfcReader.iso15693Read(2, readData);
                if (ret != ErrCode.ERR_SUCCESS) {
                    logMsg("iso15693Read failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }
                logMsg("iso15693Read success \n");
                logMsg("readData:" + BytesUtils.byte2HexStr(readData.getData()) + "\n");


                ret = nfcReader.iso15693Write(2, new byte[]{0x01, 0x02, 0x03, 0x04,});
                if (ret != ErrCode.ERR_SUCCESS) {
                    logMsg("iso15693Write failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }
                logMsg("iso15693Write success \n");

                ret = nfcReader.iso15693Read(2, readData);
                if (ret != ErrCode.ERR_SUCCESS) {
                    logMsg("iso15693Read failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }
                logMsg("iso15693Read success \n");
                logMsg("readData:" + BytesUtils.byte2HexStr(readData.getData()) + "\n");

                logMsg("ISO15693_demo success \n");
                pgd.dismiss();
            }

            @Override
            public void onError(int i) {
                logMsg("openCard failed" + String.format(" errCode = 0x%x\n", i));
                pgd.dismiss();
            }
        });
    }

    /**
     * NFC card demo,include open card,send APDU, close.
     */
    void nfc_demo() {

        ProgressDialog pgd;
        pgd = new ProgressDialog(this);
        pgd.setCancelable(false);
        pgd.setMessage("Please swing card...");
        if (!pgd.isShowing()) {
            pgd.show();
        }

        try {

            nfcReader.openCard(10, new OnNfcReaderCallback() {
                @Override
                public void onCardATR(byte[] bytes) {

                    logMsg("NFC card open success\n");
                    logMsg("ATR data:" + BytesUtils.byte2HexStr(bytes) + "\n");

                    try {

                        byte[] sendData = {0x00, (byte) 0x84, 0x00, 0x00, 0x08};
                        byte[] recvData = new byte[256];
                        int[] recvLen = new int[1];
                        int ret;

                        recvLen[0] = recvData.length;
                        ret = nfcReader.sendApduCustomer(sendData, sendData.length, recvData, recvLen);
                        if (ret != ErrCode.ERR_SUCCESS) {
                            logMsg("sendApduCustomer failed" + String.format(" errCode = 0x%x\n", ret));
                            pgd.dismiss();
                            return;
                        }

                        logMsg("NFC card send APDU success\n");
                        logMsg("Send data:" + BytesUtils.byte2HexStr(sendData) + "\n");
                        logMsg("Receiver data:" + BytesUtils.byte2HexStr(recvData, recvLen[0]) + "\n");

                        ret = nfcReader.close();
                        if (ret != ErrCode.ERR_SUCCESS) {
                            logMsg("close failed" + String.format(" errCode = 0x%x\n", ret));
                            pgd.dismiss();
                            return;
                        }

                        logMsg("NFC card demo success\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                        logMsg("" + e.toString());
                    }
                    pgd.dismiss();
                }

                @Override
                public void onError(int i) {
                    logMsg("openCard failed" + String.format(" errCode = 0x%x\n", i));
                    pgd.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logMsg(e.toString() + "\n");
        }
    }

    /**
     * The M1 card SDK interface calls Demo.
     */
    void M1_demo() {
        int ret;

        ret = nfcReader.mifarePowerOn();
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("mifarePowerOn failed" + String.format(" errCode = 0x%x\n", ret));
            return;
        }

        ProgressDialog pgd;
        pgd = new ProgressDialog(this);
        pgd.setCancelable(false);
        pgd.setMessage("Please swing card...");
        if (!pgd.isShowing()) {
            pgd.show();
        }

        nfcReader.openCardEx(5, new OnNfcPollingCallback() {
            @Override
            public void onSuccess(ISO14443_PollingInfo iso14443_pollingInfo) {

                /* Determine the card type.*/

                int cardType = iso14443_pollingInfo.getType();
                if (cardType != NfcCardType.CARD_Mifare_M1_S70 && cardType != NfcCardType.CARD_Mifare_M1_S50) {
                    logMsg("Wrong card type " + String.format(" current type = 0x%x\n", cardType));
                    pgd.dismiss();
                    return;
                }

                logMsg("UID:" + BytesUtils.byte2HexStr(iso14443_pollingInfo.getUID()) + "\n");


                /* Default card Key.*/
                byte[] key = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,};
                int ret;
                logMsg("Key A:" + BytesUtils.byte2HexStr(key) + "\n");

                MifareKey mifareKey = new MifareKey(MifareKey.MifareKeyType.MifareKey_A, key);
                ret = nfcReader.mifareAuthentication(4, mifareKey, iso14443_pollingInfo.getUID());
                if (ret != ErrCode.ERR_SUCCESS) {
                    logMsg("mifarePowerOn failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }
                logMsg("mifareAuthentication 4 success \n");

                byte[] blockData = new byte[512];
                int[] blockDataLen = new int[1];

                blockDataLen[0] = blockData.length;
                ret = nfcReader.mifareBlockRead(5, blockData, blockDataLen);
                if (ret != ErrCode.ERR_SUCCESS) {
                    logMsg("mifareBlockRead failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }

                logMsg("mifareBlockRead 5 success \n");
                logMsg("blockData:" + BytesUtils.byte2HexStr(blockData, blockDataLen[0]) + "\n");

                byte[] writeData = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,};


                logMsg("writeData:" + BytesUtils.byte2HexStr(writeData) + "\n");
                ret = nfcReader.mifareBlockWrite(4, writeData);
                if (ret != ErrCode.ERR_SUCCESS) {
                    logMsg("mifareBlockWrite failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }
                logMsg("mifareBlockWrite success \n");

                ret = nfcReader.mifareIncrement(5, 2);
                if (ret != ErrCode.ERR_SUCCESS) {
                    logMsg("mifareIncrement failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }
                logMsg("mifareIncrement success \n");

                ret = nfcReader.mifareRestore(5, 6);
                if (ret != ErrCode.ERR_SUCCESS) {
                    logMsg("mifareRestore failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }
                logMsg("mifareRestore success \n");

                ret = nfcReader.mifareTransfer(6);
                if (ret != ErrCode.ERR_SUCCESS) {
                    logMsg("mifareTransfer failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }

                logMsg("M1_demo success \n");
                pgd.dismiss();
            }

            @Override
            public void onError(int i) {
                logMsg("openCard failed" + String.format(" errCode = 0x%x\n", i));
                pgd.dismiss();
            }
        });
    }

    /**
     * The M0 card SDK interface calls Demo.
     */
    void M0_demo() {
        int ret;

        ret = nfcReader.mifarePowerOn();
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("mifarePowerOn failed" + String.format(" errCode = 0x%x\n", ret));
            return;
        }

        ProgressDialog pgd;
        pgd = new ProgressDialog(this);
        pgd.setCancelable(false);
        pgd.setMessage("Please swing card...");
        if (!pgd.isShowing()) {
            pgd.show();
        }

        nfcReader.openCardEx(5, new OnNfcPollingCallback() {
            @Override
            public void onSuccess(ISO14443_PollingInfo iso14443_pollingInfo) {

                /* Determine the card type.*/
                int cardType = iso14443_pollingInfo.getType();
                if (cardType != NfcCardType.CARD_Mifare_M0) {
                    logMsg("Wrong card type " + String.format(" current type = 0x%x\n", cardType));
                    pgd.dismiss();
                    return;
                }

                int ret;

                byte[] blockData = new byte[512];
                int[] blockDataLen = new int[1];

                blockDataLen[0] = blockData.length;
                ret = nfcReader.mifareBlockRead(4, blockData, blockDataLen);
                if (ret != ErrCode.ERR_SUCCESS) {
                    logMsg("mifareBlockRead failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }
                logMsg("mifareBlockRead success\n");
                logMsg("block data:" + BytesUtils.byte2HexStr(blockData, blockDataLen[0]) + "\n");

                byte[] writeData = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,};
                logMsg("writeData:" + BytesUtils.byte2HexStr(writeData) + "\n");
                ret = nfcReader.mifareBlockWrite(4, writeData);
                if (ret != ErrCode.ERR_SUCCESS) {
                    logMsg("mifareTransfer failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }

                logMsg("M0_demo success\n");
                pgd.dismiss();
            }

            @Override
            public void onError(int i) {
                logMsg("openCard failed" + String.format(" errCode = 0x%x\n", i));
                pgd.dismiss();
            }
        });

    }

    /**
     * The M0 card SDK interface calls Demo.
     */
    void M0_ev1_demo() {
        int ret;

        ret = nfcReader.mifarePowerOn();
        if (ret != ERR_SUCCESS) {
            logMsg("mifarePowerOn failed" + String.format(" errCode = 0x%x\n", ret));
            return;
        }

        ProgressDialog pgd;
        pgd = new ProgressDialog(this);
        pgd.setCancelable(false);
        pgd.setMessage("Please swing card...");
        if (!pgd.isShowing()) {
            pgd.show();
        }

        nfcReader.openCardEx(5, new OnNfcPollingCallback() {
            @Override
            public void onSuccess(ISO14443_PollingInfo iso14443_pollingInfo) {
                /* Determine the card type.*/
                int cardType = iso14443_pollingInfo.getType();
                if (cardType != CARD_Mifare_M0) {
                    logMsg("Wrong card type " + String.format(" current type = 0x%x\n", cardType));
                    pgd.dismiss();
                    return;
                }

                int ret;

                byte[] blockData = new byte[512];
                int[] blockDataLen = new int[1];

                blockDataLen[0] = blockData.length;
                ret = nfcReader.mifareBlockRead(4, blockData, blockDataLen);
                if (ret != ERR_SUCCESS) {
                    logMsg("mifareBlockRead failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }
                logMsg("mifareBlockRead success\n");
                logMsg("block data:" + Bytes2HexString(blockData, blockDataLen[0]) + "\n");

                // ev1
                byte[] pwd = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
                BytesTypeValue bytesTypeValue = new BytesTypeValue();
                ret = nfcReader.mifare_ul_ev1_PwdAuth(pwd, bytesTypeValue);
                // c
                //byte[] pwd = new byte[] {0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f};
                //ret = nfcReader.mifare_ul_c_Authenticate(pwd);
                if (ret != ERR_SUCCESS) {
                    logMsg("mifareAuthentication failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }

                byte[] writeData = {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,};
                logMsg("writeData:" + Bytes2HexString(writeData) + "\n");
                ret = nfcReader.mifareBlockWrite(4, writeData);
                if (ret != ERR_SUCCESS) {
                    logMsg("mifareTransfer failed" + String.format(" errCode = 0x%x\n", ret));
                    pgd.dismiss();
                    return;
                }

                logMsg("M0_demo success\n");
                pgd.dismiss();
            }

            @Override
            public void onError(int i) {
                logMsg("openCard failed" + String.format(" errCode = 0x%x\n", i));
                pgd.dismiss();
            }
        });

    }

    /**
     * NFC felica card demo,include open card,send APDU, close.
     */
    void nfc_felica_demo() {

        ProgressDialog pgd;
        pgd = new ProgressDialog(this);
        pgd.setCancelable(false);
        pgd.setMessage("Please swing card...");
        if (!pgd.isShowing()) {
            pgd.show();
        }

        try {
            nfcReader.openFelica(30, (byte) 0x01, (byte) 0xFFFF, new OnNfcReaderCallback() {
                @Override
                public void onCardATR(byte[] bytes) {
                    logMsg("NFC felica card open success\n");
                    logMsg("ATR data:" + BytesUtils.byte2HexStr(bytes) + "\n");
                    byte[] sendData = new byte[9];
                    sendData[0] = 0x0C;
                    System.arraycopy(bytes, 0, sendData, 1, 8);
                    byte[] response = new byte[256];
                    int[] responselen = new int[1];
                    int ret = nfcReader.felicaSendApdu(30, sendData, sendData.length, response, responselen);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("sendApduCustomer failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("NFC Felica card send APDU success\n");
                    logMsg("Send data:" + BytesUtils.byte2HexStr(sendData) + "\n");
                    logMsg("Receiver data:" + BytesUtils.byte2HexStr(response, responselen[0]) + "\n");
                    ret = nfcReader.close();
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("close failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    pgd.dismiss();
                }

                @Override
                public void onError(int i) {
                    logMsg("openFelica failed" + String.format(" errCode = 0x%x\n", i));
                    nfcReader.close();
                    pgd.dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            logMsg(e.toString() + "\n");
        }
    }

    /**
     * The Desfire card SDK interface calls Demo.
     */
    void desFire_demo() {
        ProgressDialog pgd;
        pgd = new ProgressDialog(this);
        pgd.setCancelable(false);
        pgd.setMessage("Please swing card...");
        if (!pgd.isShowing()) {
            pgd.show();
        }

        try {

            nfcReader.openCardEx(5, new OnNfcPollingCallback() {
                @Override
                public void onSuccess(ISO14443_PollingInfo iso14443_pollingInfo) {
                    /* Determine the card type.*/
                    int cardType = iso14443_pollingInfo.getType();
                    if (cardType != NfcCardType.CARD_DesFire) {
                        logMsg("Wrong card type " + String.format(" current type = 0x%x\n", cardType));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    int ret;
                    byte[] dataCommon;
                    BytesTypeValue outPutBytesValue;
                    DesFireResponse desFireResponse;

                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireGetInfo(DesFireGetInfoFlag.DF_INFO_KEYVERSION, 0, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Get KeyType failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }

                    byte[] dataKeyType = desFireResponse.getRespData();
                    String strDataKeyType = BytesUtils.byte2HexStr(dataKeyType);
                    logMsg("DESFire Get KeyType success " + strDataKeyType);

                    byte[] key = BytesUtils.hexStringToBytes("00000000000000000000000000000000");
                    int mode = DesFireAuthenticateMode.DF_AUTHENTICATE_LEGACY;
                    if ("0000".equals(strDataKeyType)) {
                        mode = DesFireAuthenticateMode.DF_AUTHENTICATE_LEGACY;
                    } else if ("0001".equals(strDataKeyType)) {
                        mode = DesFireAuthenticateMode.DF_AUTHENTICATE_AES;
                    }
                    ret = nfcReader.desFireAuthenticate(0, key, key.length, mode);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Authenticate failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Authenticate success");

                    dataCommon = BytesUtils.hexStringToBytes("FC");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire format failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire format success");

                    dataCommon = BytesUtils.hexStringToBytes("000000");
                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireSelApp(dataCommon, dataCommon.length, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire select application failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire select application success , Data:" + desFireResponse.getCardState());

                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireGetInfo(DesFireGetInfoFlag.DF_INFO_VERSION, 0, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire version failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire version success , Data:" + BytesUtils.byte2HexStr(desFireResponse.getRespData()));

                    dataCommon = BytesUtils.hexStringToBytes("CA00DE160F02");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire create 3DES application failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire create 3DES application success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));


                    dataCommon = BytesUtils.hexStringToBytes("CA00DE240F42");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire create 3KDES application failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire create 3KDES application success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    dataCommon = BytesUtils.hexStringToBytes("CA00AE160F82");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire create AES application failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire create AES application success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    dataCommon = BytesUtils.hexStringToBytes("CACCBBAA0F01");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire create AABBCC application failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire create AABBCC application success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireGetInfo(DesFireGetInfoFlag.DF_INFO_APPIDS, 0, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Get application ID failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Get application ID success " + BytesUtils.byte2HexStr(desFireResponse.getRespData()));


                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireGetInfo(DesFireGetInfoFlag.DF_INFO_KEYVERSION, 0, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Get KeyType failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }

                    strDataKeyType = BytesUtils.byte2HexStr(desFireResponse.getRespData());
                    logMsg("DESFire Get KeyType success " + strDataKeyType);

                    if ("0000".equals(strDataKeyType)) {
                        mode = DesFireAuthenticateMode.DF_AUTHENTICATE_LEGACY;
                    } else if ("0001".equals(strDataKeyType)) {
                        mode = DesFireAuthenticateMode.DF_AUTHENTICATE_AES;
                    }
                    ret = nfcReader.desFireAuthenticate(0, key, key.length, mode);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Authenticate failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Authenticate success");


                    dataCommon = BytesUtils.hexStringToBytes("DACCBBAA");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire delete AABBCC application failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire delete AABBCC application success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireGetInfo(DesFireGetInfoFlag.DF_INFO_APPIDS, 0, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Get application ID failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Get application ID success " + BytesUtils.byte2HexStr(desFireResponse.getRespData()));

                    dataCommon = BytesUtils.hexStringToBytes("00DE16");
                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireSelApp(dataCommon, dataCommon.length, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire select application failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire select application success , Data:" + desFireResponse.getCardState());

                    dataCommon = BytesUtils.hexStringToBytes("CD05001100500000");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire create file failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire create file success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireGetInfo(DesFireGetInfoFlag.DF_INFO_FILEIDS, 0, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Get file ID failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Get file ID success " + BytesUtils.byte2HexStr(desFireResponse.getRespData()));

                    dataCommon = BytesUtils.hexStringToBytes("F505");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Get file configure failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Get file configure success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));
                    if ("0000".equals(strDataKeyType)) {
                        mode = DesFireAuthenticateMode.DF_AUTHENTICATE_LEGACY;
                    } else if ("0001".equals(strDataKeyType)) {
                        mode = DesFireAuthenticateMode.DF_AUTHENTICATE_ISO;
                    }
                    ret = nfcReader.desFireAuthenticate(0, key, key.length, mode);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Authenticate failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Authenticate success");

                    dataCommon = BytesUtils.hexStringToBytes("010203040506070801020304050607080102030405060708010203040506070801020304050607080102030405060708");
                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireWriteFile(DesFireFileType.DF_FILE_TYPE_STD_BACKFILE, 5, 0, dataCommon.length, dataCommon, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Write file failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Write file success " + desFireResponse.getCardState());

                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireReadFile(DesFireFileType.DF_FILE_TYPE_STD_BACKFILE, 5, 0, 48, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Read file failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Read file success " + BytesUtils.byte2HexStr(desFireResponse.getRespData()));

                    dataCommon = BytesUtils.hexStringToBytes("DF05");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire delete file failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire delete file success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireGetInfo(DesFireGetInfoFlag.DF_INFO_FILEIDS, 0, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Get file ID failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Get file ID success " + BytesUtils.byte2HexStr(desFireResponse.getRespData()));


                    //**************************************************Create Value File Type***************************************************
                    dataCommon = BytesUtils.hexStringToBytes("CC0600110000000000000000550200000001");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Create value file failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Create value file success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    ret = nfcReader.desFireAuthenticate(0, key, key.length, DesFireAuthenticateMode.DF_AUTHENTICATE_LEGACY);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Authenticate failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Authenticate success");

                    dataCommon = BytesUtils.hexStringToBytes("6C06");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Get value file data failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Get value file data success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    dataCommon = BytesUtils.hexStringToBytes("02000000");
                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireValueFileOp(DesFireValueFileOperationFlag.DF_FILE_OP_FLAG_CREDIT, 6, dataCommon, dataCommon.length, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire increase failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire increase success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireComfirmCancel(true, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Comfirm failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Comfirm success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));


                    dataCommon = BytesUtils.hexStringToBytes("6C06");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Get value file data failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Get value file data success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));


                    dataCommon = BytesUtils.hexStringToBytes("04000000");
                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireValueFileOp(DesFireValueFileOperationFlag.DF_FILE_OP_FLAG_DEBIT, 6, dataCommon, dataCommon.length, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire decrease failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire decrease success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireComfirmCancel(true, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Comfirm failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Comfirm success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    dataCommon = BytesUtils.hexStringToBytes("6C06");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Get value file data failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Get value file data success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));


                    dataCommon = BytesUtils.hexStringToBytes("01000000");
                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireValueFileOp(DesFireValueFileOperationFlag.DF_FILE_OP_FLAG_LIMITED_CREDIT, 6, dataCommon, dataCommon.length, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire limit increase failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire limit increase success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    desFireResponse = new DesFireResponse();
                    ret = nfcReader.desFireComfirmCancel(true, desFireResponse);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Comfirm failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Comfirm success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    dataCommon = BytesUtils.hexStringToBytes("6C06");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire Get value file data failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire Get value file data success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    dataCommon = BytesUtils.hexStringToBytes("DF06");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.desFireTransmit(dataCommon, dataCommon.length, outPutBytesValue, DesFireCommMode.DF_COMM_MODE_MDCM_PLAIN, DesFireFileType.DF_FILE_TYPE_RECORDFILE);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("DESFire delete file failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("DESFire delete file success " + BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    logMsg("DesFire_demo success\n");
                    nfcReader.close();
                    pgd.dismiss();
                }

                @Override
                public void onError(int i) {
                    logMsg("openCard failed" + String.format(" errCode = 0x%x\n", i));
                    pgd.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logMsg(e.toString() + "\n");
        }
    }

    /**
     * The Mifare Plus card SDK interface calls Demo.
     */
    void mifarePlus_demo() {
        ProgressDialog pgd;
        pgd = new ProgressDialog(this);
        pgd.setCancelable(false);
        pgd.setMessage("Please swing card...");
        if (!pgd.isShowing()) {
            pgd.show();
        }

        try {

            nfcReader.openCardEx(5, new OnNfcPollingCallback() {
                @Override
                public void onSuccess(ISO14443_PollingInfo iso14443_pollingInfo) {
                    /* Determine the card type.*/
                    int cardType = iso14443_pollingInfo.getType();
                    if (cardType != NfcCardType.CARD_Mifare_M2) {
                        logMsg("Wrong card type " + String.format(" current type = 0x%x\n", cardType));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    int ret;
                    byte[] dataCommon;
                    BytesTypeValue outPutBytesValue;

                    dataCommon = BytesUtils.hexStringToBytes("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF");
                    ret = nfcReader.mifarePlusAuthentication(1, 1, 1, dataCommon);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("MifarePlus Authentication failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("MifarePlus Authentication success ");

                    dataCommon = BytesUtils.hexStringToBytes("01020304050607080102030405060708");
                    ret = nfcReader.mifarePlusWriteData(1,1,1,dataCommon);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("MifarePlus Write failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("MifarePlus Write success ");

                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.mifarePlusReadData(2,1,1,1,outPutBytesValue);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("MifarePlus Read failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("MifarePlus Read success "+BytesUtils.byte2HexStr(outPutBytesValue.getData()));

                    dataCommon = BytesUtils.hexStringToBytes("01010100000000");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.mifarePlusOp(4, dataCommon,dataCommon.length,outPutBytesValue);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("MifarePlus Create wallet failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("MifarePlus Create wallet success ");

                    IntTypeValue intTypeValue = new IntTypeValue();
                    ret = nfcReader.mifarePlusReadBalance(1,1,1,1,intTypeValue);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("MifarePlus Read Balance failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("MifarePlus Read Balance success "+intTypeValue.getData());


                    ret = nfcReader.mifarePlusIncrement(1, 1, 3);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("MifarePlus Increment failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("MifarePlus Increment success ");

                    ret = nfcReader.mifarePlusDecrement(1, 1, 1);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("MifarePlus Decrement failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("MifarePlus Decrement success ");

                    //----Backup wallet----
                    dataCommon = BytesUtils.hexStringToBytes("0101");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.mifarePlusOp(8, dataCommon,dataCommon.length,outPutBytesValue);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("MifarePlus Copy original wallet failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("MifarePlus Copy original wallet success ");

                    dataCommon = BytesUtils.hexStringToBytes("02010100000000");
                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.mifarePlusOp(4, dataCommon,dataCommon.length,outPutBytesValue);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("MifarePlus create new wallet failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("MifarePlus create new wallet success ");

                    dataCommon = BytesUtils.hexStringToBytes("0201");
                    byte[] recBuff = new byte[256];
                    int[] recLen = new int[1];
                    ret = nfcReader.mifarePlusOp(7, dataCommon,dataCommon.length,recBuff,recLen);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("MifarePlus copy data to new wallet failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("MifarePlus copy data to new wallet success ");

                    outPutBytesValue = new BytesTypeValue();
                    ret = nfcReader.mifarePlusGetVersion(outPutBytesValue);
                    if (ret != ErrCode.ERR_SUCCESS) {
                        logMsg("MifarePlus GetVersion failed" + String.format(" errCode = 0x%x\n", ret));
                        nfcReader.close();
                        pgd.dismiss();
                        return;
                    }
                    logMsg("MifarePlus GetVersion success "+ BytesUtils.byte2HexStr(outPutBytesValue.getData()));


                    logMsg("Mifare Plus demo success\n");
                    nfcReader.close();
                    pgd.dismiss();
                }

                @Override
                public void onError(int i) {
                    logMsg("openCard failed" + String.format(" errCode = 0x%x\n", i));
                    pgd.dismiss();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logMsg(e.toString() + "\n");
        }
    }

}
