package com.ftpos.ftappdemo;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ftpos.library.smartpos.magreader.MagReader;
import com.ftpos.library.smartpos.magreader.OnMagReadCallback;
import com.ftpos.library.smartpos.magreader.TrackDataInfo;
import com.ftpos.library.smartpos.util.BytesUtils;

public class MagCardActivity extends BaseActivity {

    private TextView mShowResultTv;

    MagReader magReader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mag_card);

        initView();

        this.magReader = MainActivity.magReader;
    }

    private void initView() {
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText("Mag Card View");
        findViewById(R.id.ukey_navbar_left_btn).setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        findViewById(R.id.mag_card_demo).setOnClickListener(this);

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
            case R.id.mag_card_demo: {
                mag_demo();
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
     * Magnetic stripe card call example, get magnetic stripe card track information and display.
     */
    void mag_demo() {

        ProgressDialog pgd;
        pgd = new ProgressDialog(this);
        pgd.setCancelable(false);
        pgd.setMessage("Please swipe card...");
        if (!pgd.isShowing()) {
            pgd.show();
        }

        magReader.readMagCard(10, 0, new OnMagReadCallback() {
            @Override
            public void onTrackData(TrackDataInfo trackDataInfo) {
                String str;

                str = "trackData1:\n" + BytesUtils.byte2HexStr(trackDataInfo.getmTrack1Data()) + "\n"
                        + "trackData2:\n" + BytesUtils.byte2HexStr(trackDataInfo.getmTrack2Data()) + "\n"
                        + "trackData3:\n" + BytesUtils.byte2HexStr(trackDataInfo.getmTrack3Data()) + "\n";

                logMsg("readMagCard success\n" + str);

                pgd.dismiss();
            }


            @Override
            public void onError(int i) {
                logMsg("readMagCard failed" + String.format(" errCode = 0x%x\n", i));
                pgd.dismiss();
            }
        });
    }
}
