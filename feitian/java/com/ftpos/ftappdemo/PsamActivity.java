package com.ftpos.ftappdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ftpos.library.smartpos.datautils.BytesTypeValue;
import com.ftpos.library.smartpos.datautils.IntTypeValue;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.icreader.IcReader;
import com.ftpos.library.smartpos.icreader.OnIcReaderCallback;
import com.ftpos.library.smartpos.psamreader.PsamIndex;
import com.ftpos.library.smartpos.psamreader.PsamReader;
import com.ftpos.library.smartpos.util.BytesUtils;

public class PsamActivity extends BaseActivity {
    private static final String TAG = PsamActivity.class.getSimpleName();

    private TextView mShowResultTv;

    private RelativeLayout mPsamAndIcSendApduDemmo;

    private PsamReader psamReader;
    private IcReader icReader;
    int DeviceModel;
    private RadioGroup radgroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_psam);

        DeviceModel = getIntent().getIntExtra("DeviceModel", -1);
        initView();

        this.psamReader = MainActivity.psamReader;
        this.icReader = MainActivity.icReader;
    }

    private void initView() {
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText("Psam card View");
        findViewById(R.id.ukey_navbar_left_btn).setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        findViewById(R.id.psam_card_demo).setOnClickListener(this);
        mPsamAndIcSendApduDemmo = findViewById(R.id.psam_ic_card_demo);
        mPsamAndIcSendApduDemmo.setOnClickListener(this);
        if (DeviceModel == 1) {
            mPsamAndIcSendApduDemmo.setVisibility(View.GONE);
        }

        mShowResultTv = findViewById(R.id.function_return_result_tv);
        radgroup = (RadioGroup) findViewById(R.id.radioGroup);

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
            case R.id.psam_card_demo: {
                psamCardDemo();
            }
            break;
            case R.id.psam_ic_card_demo: {
                psamCardAndIcCardSendAPDUDemo();
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

    /**
     * Psam card send APDU demo.
     */
    void psamCardDemo() {

        int ret;
        int psamIndex;
        psamIndex = PsamIndex.PSAM_INDEX_1;
        for (int i = 0; i < radgroup.getChildCount(); i++) {
            RadioButton rd = (RadioButton) radgroup.getChildAt(i);
            if (rd.isChecked()) {
                if ("PSAM1".equals(rd.getText())) {
                    psamIndex =  PsamIndex.PSAM_INDEX_1;
                }else if ("PSAM2".equals(rd.getText())){
                    psamIndex =  PsamIndex.PSAM_INDEX_2;
                }
                break;
            }
        }

        IntTypeValue samStatus = new IntTypeValue();
        ret = psamReader.getCardStatus(psamIndex, samStatus);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("getCardStatus failed" + String.format(" errCode = 0x%x\n", ret));
            return;
        }
        logMsg("PSAM Status:" + samStatus.getData());

        BytesTypeValue bytesValue = new BytesTypeValue();
        ret = psamReader.openCard(psamIndex, bytesValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("openCard failed" + String.format(" errCode = 0x%x\n", ret));
            return;
        }
        logMsg("Open PSAM card success\n");

//        String strComm = "00A4040000";  //
        String strComm = "0084000004";
        byte[] selectApp = BytesUtils.hexStringToBytes(strComm);
        byte[] recvData = new byte[256];
        int[] len = new int[1];

        ret = psamReader.sendApduCustomer(psamIndex, selectApp, selectApp.length, recvData, len);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("sendApduCustomer failed" + String.format(" errCode = 0x%x\n", ret));
            return;
        }
        logMsg("PSAM card send APDU success\n");
        logMsg("Send data:" + strComm + "\n");
        logMsg("Receiver data:" + BytesUtils.byte2HexStr(recvData, len[0]) + "\n");


        BytesTypeValue artBytesValue = new BytesTypeValue();
        ret = psamReader.reset(psamIndex,artBytesValue);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("reset failed" + String.format(" errCode = 0x%x\n", ret));
        }else{
            logMsg("reset PSAM card success\n");
        }

        ret = psamReader.close(psamIndex);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("close failed" + String.format(" errCode = 0x%x\n", ret));
            return;
        }

        logMsg("psamCardDemo success\n");
    }

    /**
     * psam card and ic card send APDU demo.
     */
    void psamCardAndIcCardSendAPDUDemo() {

        try {
            int ret;

            IntTypeValue intValue = new IntTypeValue();
            ret = icReader.getCardStatus(intValue);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("getCardStatus failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            logMsg("getCardStatus success" + String.format(" status = 0x%x\n", intValue.getData()));

            ProgressDialog pgd;
            pgd = new ProgressDialog(this);
            pgd.setCancelable(false);
            pgd.setMessage("Please wait...");
            if (!pgd.isShowing()) {
                pgd.show();
            }

            icReader.openCard(10, new OnIcReaderCallback() {
                @Override
                public void onCardATR(byte[] bytes) {

                    logMsg("IC card open success\n");

                    try {
                        byte[] sendData = new byte[]{0x00, (byte) 0xA4, 0x04, 0x00, 0x07, (byte) 0xA0, 0x00, 0x00, 0x00, 0x03, 0x10, 0x10};
                        byte[] recvData = new byte[256];
                        int[] len = new int[1];
                        int ret;
                        int psamIndex;

                        psamIndex = PsamIndex.PSAM_INDEX_1;
                        BytesTypeValue bytesValue = new BytesTypeValue();
                        ret = psamReader.openCard(psamIndex, bytesValue);
                        if (ret != ErrCode.ERR_SUCCESS) {
                            logMsg("openCard failed" + String.format(" errCode = 0x%x\n", ret));
                            pgd.dismiss();
                            return;
                        }

                        logMsg("PSAM card open success\n");
                        logMsg("ATR data:" + BytesUtils.byte2HexStr(bytesValue.getData()) + "\n");

                        byte[] selectApp = new byte[]{0x00, (byte) 0xa4, 0x04, 0x00, 0x07, (byte) 0xa0, 0x00, 0x00, 0x03, 0x33, 0x01, 0x01,};

                        ret = psamReader.sendApduCustomer(psamIndex, selectApp, selectApp.length, recvData, len);
                        if (ret != ErrCode.ERR_SUCCESS) {
                            logMsg("sendApduCustomer failed" + String.format(" errCode = 0x%x\n", ret));
                            pgd.dismiss();
                            return;
                        }
                        logMsg("PSAM card send APDU success\n");
                        logMsg("Send data:" + BytesUtils.byte2HexStr(selectApp) + "\n");
                        logMsg("Receiver data:" + BytesUtils.byte2HexStr(recvData, len[0]) + "\n");

                        ret = icReader.sendApduCustomer(sendData, sendData.length, recvData, len);
                        if (ret != ErrCode.ERR_SUCCESS) {
                            logMsg("IC card sendApduCustomer failed" + String.format(" errCode = 0x%x\n", ret));
                            pgd.dismiss();
                            return;
                        }
                        logMsg("IC card send APDU success\n");
                        logMsg("Send data:" + BytesUtils.byte2HexStr(sendData) + "\n");
                        logMsg("Receiver data:" + BytesUtils.byte2HexStr(recvData, len[0]) + "\n");

                        ret = icReader.close();
                        if (ret != ErrCode.ERR_SUCCESS) {
                            logMsg("close failed" + String.format(" errCode = 0x%x\n", ret));
                            pgd.dismiss();
                            return;
                        }

                        logMsg("psamCardAndIcCardSendAPDUDemo success\n");
                    } catch (Exception e) {
                        e.printStackTrace();
                        logMsg(e.toString() + "\n");
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
            logMsg(e.toString());
        }
    }
}
