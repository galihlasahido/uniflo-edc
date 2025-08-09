package com.ftpos.ftappdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.icreader.IcReader;
import com.ftpos.library.smartpos.icreader.OnIcReaderCallback;
import com.ftpos.library.smartpos.util.BytesUtils;


public class IcCardActivity extends BaseActivity {

    private TextView mShowResultTv;

    IcReader icReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ic_card);

        initView();

        this.icReader = MainActivity.icReader;
    }

    private void initView() {
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText("Ic Card View");
        findViewById(R.id.ukey_navbar_left_btn).setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        findViewById(R.id.ic_card_demo).setOnClickListener(this);
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
            case R.id.ic_card_demo: {
                icReaderDemo();
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
     * IC card example，include open card,send APDU,close card.
     */
    void icReaderDemo() {
        try {
            ProgressDialog pgd;
            pgd = new ProgressDialog(this);
            pgd.setCancelable(false);
            pgd.setMessage("Please insert the card...");
            if (!pgd.isShowing()) {
                pgd.show();
            }

            icReader.openCard(10, new OnIcReaderCallback() {
                @Override
                public void onCardATR(byte[] bytes) {

                    logMsg("IC card open success\n");
                    logMsg("ATR data:" + BytesUtils.byte2HexStr(bytes) + "\n");

                    try {
                        byte[] sendData = new byte[]{0x00, (byte) 0xA4, 0x04, 0x00, 0x07, (byte) 0xA0, 0x00, 0x00, 0x00, 0x03, 0x10, 0x10};
                        byte[] recvData = new byte[256];
                        int[] len = new int[1];
                        int ret;

                        ret = icReader.sendApduCustomer(sendData, sendData.length, recvData, len);
                        if (ret != ErrCode.ERR_SUCCESS) {
                            logMsg("close failed" + String.format(" errCode = 0x%x\n", ret));
                            pgd.dismiss();
                            return;
                        }

                        logMsg("IC card send APDU success\n");
                        logMsg("Send data:" + BytesUtils.byte2HexStr(sendData) + "\n");
                        logMsg("Receiver data:" + BytesUtils.byte2HexStr(recvData, len[0]) + "\n");

                        ret = icReader.sendApduCustomer(sendData, sendData.length, recvData, len);
                        if (ret != ErrCode.ERR_SUCCESS) {
                            logMsg("close failed" + String.format(" errCode = 0x%x\n", ret));
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

                        logMsg("icReaderDemo success\n");

                    } catch (Exception e) {
                        e.printStackTrace();
                        logMsg(e.toString() + "\n");
                    }
                    pgd.dismiss();
                }

                @Override
                public void onError(int i) {
                    logMsg("close failed" + String.format(" errCode = 0x%x\n", i));
                    pgd.dismiss();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            logMsg(e.toString());
        }


    }
}
