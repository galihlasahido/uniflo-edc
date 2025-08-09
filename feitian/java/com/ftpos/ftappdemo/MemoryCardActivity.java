package com.ftpos.ftappdemo;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ftpos.library.smartpos.datautils.BytesTypeValue;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.memoryreader.MemoryCardType;
import com.ftpos.library.smartpos.memoryreader.MemoryReader;
import com.ftpos.library.smartpos.util.BytesUtils;

import java.util.Arrays;

public class MemoryCardActivity extends BaseActivity {

    private TextView mShowResultTv;
    private MemoryReader memoryReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_card);

        initView();

        this.memoryReader = MainActivity.memoryReader;
    }

    private void initView() {
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText("Memory Card");
        findViewById(R.id.ukey_navbar_left_btn).setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        findViewById(R.id.m4442_demo).setOnClickListener(this);
        findViewById(R.id.m4428_demo).setOnClickListener(this);
        findViewById(R.id.at24_demo).setOnClickListener(this);

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
            case R.id.m4442_demo: {
                memoryCard4442Demo();
            }
            break;
            case R.id.m4428_demo: {
                memoryCard4428Demo();
            }
            break;
            case R.id.at24_demo: {
                AT24CardDemo();
            }
            break;
            default:
                break;
        }
    }

    private void AT24CardDemo() {
        int ret = memoryReader.openAT24Card();
        if (ret == 0) {
            logMsg("Open AT24 Card Success");

            byte[] data = new byte[1024];
            int[] len = new int[]{5};
            ret = memoryReader.readAT24Card(2, data, len);
            if (ret == 0) {
                logMsg("Read AT24 Card Success");
                logMsg("Read AT24 Card :" + BytesUtils.byte2HexStr(data, len[0]) + "\n");

                byte[] send = new byte[]{0x12, 0x34, 0x56, 0x21};
                ret = memoryReader.writeAT24Card(2, send, 4);
                logMsg("Write AT24 Card ,Code:" + String.format(" errCode = 0x%x\n", ret));

                ret = memoryReader.readAT24Card(2, data, len);
                logMsg("Read AT24 Card ,Code:" + String.format(" errCode = 0x%x", ret));
                if (ret == 0) {
                    logMsg("Read AT24 Card :" + BytesUtils.byte2HexStr(data, len[0]) + "\n");
                }
            } else {
                logMsg("Read AT24 Card Failed" + String.format(" errCode = 0x%x\n", ret));
            }
            ret = memoryReader.closeAT24Card();
            logMsg("Close AT24 Card ,Code:" + String.format(" errCode = 0x%x\n", ret));
        } else {
            logMsg("Open AT24 Card Failed" + String.format(" errCode = 0x%x\n", ret));
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
     * 4428 card operation case.Including open card, read and write storage,
     * read and write record, password verification and other operations.
     */
    private void memoryCard4428Demo() {

        int ret;
        int pos;
        byte[] sendData = new byte[1024];
        byte[] recvData = new byte[1024];
        int[] recvLen = new int[1];

        try {
            BytesTypeValue atrBytesValue = new BytesTypeValue();

            ret = memoryReader.openCard(MemoryCardType.MEMORY_SLE_44x8, atrBytesValue);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("openCard failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("openCard success\n");

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb0;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x10;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read the storage. Address = 0x00, length = 0x10 fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Read the storage Address = 0x00, length = 0x10 success\n");

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb0;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x80;
            sendData[pos++] = (byte) 0x10;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read the storage Address = 0x80, length = 0x10 fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Read the storage Address = 0x80, length = 0x10 success\n");

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb0;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0x01;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read the storage Address = 0xff, length = 0x01 fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Read the storage Address = 0x00, length = 0x10 success\n");

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb1;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x03;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read EC fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Read EC success\n");

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb2;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x80;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read PROTECT fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Read PROTECT success\n");

            //************password verification*******************************************************
            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0x20;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x02;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xff;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Password verification fail " + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Password verification success\n");

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb1;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x03;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read PROTECT 1 fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Read PROTECT 1 success\n");

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0x20;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x02;
            sendData[pos++] = (byte) 0x12;
            sendData[pos++] = (byte) 0x34;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("read_PROTECT 2 fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Read PROTECT 2 success\n");

            //********write_memory****************************************************************************
            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xd0;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x80;
            sendData[pos++] = (byte) 0x03;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xff;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Write memory fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Write memory success\n");

            //Read back after writing to determine whether the writing was successful.
            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb0;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x80;
            sendData[pos++] = (byte) 0x03;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read memory fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Read memory success\n");

            byte[] temp = {(byte) 0xff, (byte) 0xff, (byte) 0xff};
            byte[] temp2 = new byte[3];

            System.arraycopy(recvData, 0, temp2, 0, 3);

            if (!Arrays.equals(temp2, temp)) {
                logMsg("Inconsistency of data. fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xd0;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x80;
            sendData[pos++] = (byte) 0x03;
            sendData[pos++] = (byte) 0xAA;
            sendData[pos++] = (byte) 0xAA;
            sendData[pos++] = (byte) 0xAA;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Write memory 0xAA fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Write memory success\n");

            //Read back after writing to determine whether the writing was successful.
            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb0;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x80;
            sendData[pos++] = (byte) 0x03;
            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read memory fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Read memory success\n");

            byte[] temp3 = {(byte) 0xAA, (byte) 0xAA, (byte) 0xAA};
            byte[] temp4 = new byte[3];

            System.arraycopy(recvData, 0, temp4, 0, 3);

            if (!Arrays.equals(temp3, temp4)) {
                logMsg("Inconsistency of data. fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            ret = memoryReader.close();
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("close fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            logMsg("SLE4428 demo success\n");

        } catch (Exception e) {
            e.printStackTrace();
            logMsg("" + e.toString());
        }
    }

    /**
     * 4442 card operation case.Including open card, read and write storage, read and write record, password verification and other operations.
     */
    private void memoryCard4442Demo() {

        int ret;

        try {
            BytesTypeValue atrBytesValue = new BytesTypeValue();

            ret = memoryReader.openCard(MemoryCardType.MEMORY_SLE_44x2, atrBytesValue);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("openCard fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Open card success\n");

            int pos;
            byte[] sendData = new byte[1024];
            byte[] recvData = new byte[1024];
            int[] recvLen = new int[1];

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb0;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x10;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read memory. Address = 0x00, length = 0x10 fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Read memory success\n");

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb0;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x80;
            sendData[pos++] = (byte) 0x10;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read memory. Address = 0x80, length = 0x10 fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Read memory success\n");

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb0;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0x01;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("read memory. Address = 0xff, length = 0x01 fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb1;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x04;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read EC fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb2;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x04;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read PROTECT fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            //****************password verification*******************************************************

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0x20;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x03;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xff;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Password verification. fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Password verification. success\n");

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb1;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x04;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read EC fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            //Checking the correct password and then deliberately using the wrong password to check, no error
            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0x20;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x03;
            sendData[pos++] = (byte) 0x12;
            sendData[pos++] = (byte) 0x34;
            sendData[pos++] = (byte) 0xff;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Verification fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb1;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x04;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read EC fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            //********write_memory (erasure)****************************************************************************
            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xd0;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x80;
            sendData[pos++] = (byte) 0x03;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xff;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Write memory fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            logMsg("Write memory success\n");
            //Read back after writing to determine whether the writing was successful
            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb0;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x80;
            sendData[pos++] = (byte) 0x03;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read memory fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            byte[] temp = {(byte) 0xff, (byte) 0xff, (byte) 0xff};
            byte[] temp2 = new byte[3];

            System.arraycopy(recvData, 0, temp2, 0, 3);

            if (!Arrays.equals(temp2, temp)) {
                logMsg("inconsistency of data.\n");
                return;
            }

            //write_memory
            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xd0;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x80;
            sendData[pos++] = (byte) 0x03;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x0;
            sendData[pos++] = (byte) 0x0;

            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Write memory fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Write memory success\n");

            //Read back after writing to determine whether the writing was successful
            pos = 0;
            sendData[pos++] = (byte) 0xff;
            sendData[pos++] = (byte) 0xb0;
            sendData[pos++] = (byte) 0x00;
            sendData[pos++] = (byte) 0x80;
            sendData[pos++] = (byte) 0x03;
            ret = memoryReader.sendApdu(sendData, pos, recvData, recvLen);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Read memory fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("Read memory success\n");

            ret = memoryReader.close();
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("close fail" + String.format(" errCode = 0x%x\n", ret));
                return;
            }
            logMsg("close success\n");
            logMsg("SLE4442 demo success\n");

        } catch (Exception e) {
            e.printStackTrace();
            logMsg("" + e.toString());
        }
    }
}
