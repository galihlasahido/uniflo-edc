package com.ftpos.pay.demo.view;

import static com.ftpos.library.smartpos.util.EncodeConversionUtil.EncodeConversion;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ftpos.library.smartpos.emv.Amount;
import com.ftpos.library.smartpos.emv.CandidateAIDInfo;
import com.ftpos.library.smartpos.emv.TransRequest;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.pay.demo.BuildConfig;
import com.ftpos.pay.demo.R;
import com.ftpos.pay.demo.SvrHelper;
import com.ftpos.pay.demo.constants.ICardType;
import com.ftpos.pay.demo.pinpad.IPinpadCode;
import com.jirui.logger.Logger;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * @author GuoJirui.
 * @date 2021/4/25.
 * @desc
 */
public class MainActivity extends BaseEmvActivity {

    private EditText etAmount;

    private CheckBox insertCBox;
    private CheckBox passCBox;
    private CheckBox swipeCBox;

    private ListView appList;
    private LinearLayout layoutList;

    private int transType = 0;
    private int selectedPosition = 0;

    @Override
    protected void onCreateView(Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        initView();
        Log.d(TAG, "onCreateView: " + Build.MODEL);
    }

    private void initView() {
        TextView tvTitle = bindViewById(R.id.tvTitle);
        tvTitle.setText(String.format("%s\r\n%s", getString(R.string.app_name), BuildConfig.VERSION_NAME));

        Spinner spTransType = bindViewById(R.id.sp_trans);
        etAmount = bindViewById(R.id.etAmount);

        insertCBox = bindViewById(R.id.insertCBox);
        passCBox = bindViewById(R.id.passCBox);
        swipeCBox = bindViewById(R.id.swipeCBox);
        bindViewById(R.id.initBtn).setOnClickListener(V -> initTransaction());
        bindViewById(R.id.startBtn).setOnClickListener(v -> transaction());
        bindViewById(R.id.stopBtn).setOnClickListener(v -> cancelTransaction());

        appList = bindViewById(R.id.lv_apps);
        layoutList = bindViewById(R.id.ll_list_app);
        layoutList.setVisibility(View.GONE);

        spTransType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                if (position == 0) {
                    transType = 0;
                } else if (position == 1) {
                    transType = 1;
                } else if (position == 2) {
                    transType = 9;
                } else if (position == 3) {
                    transType = 20;
                } else {
                    transType = -1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        insertCBox.setChecked(true);
        passCBox.setChecked(true);
        swipeCBox.setChecked(true);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int angle = ((WindowManager) this
                .getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        Log.e("angle", "angle:" + angle);

        setContentView(R.layout.activity_main);
        initView();

        super.onConfigurationChanged(newConfig);
    }

    private void transaction() {
        hideSoftKeyboard();
        if (!hasInitEmv) {
            initTransaction();
        }
        doTransaction();
    }

    @Override
    protected void cancelTransaction() {
        hideSoftKeyboard();
        stopEMV();
        SvrHelper.instance().cancelOperation();
    }

    private void initTransaction() {
        if (emvStatus) {
            SvrHelper.instance().cancelOperation();
            stopEmv(IPinpadCode.OTHER_UNKNOWN, null);
            emvStatus = false;
        }
        Logger.v("Init transaction");
        if (!clearAllParameters()) return;
        if (!updateAllParameters()) return;
        if (!loadSymKey()) return;

        //The following method is used to load IPEK (Initial PIN Encryption Key) to the key store
        if (!loadDukptKey()) return;
        hasInitEmv = true;
    }

    private void doTransaction() {
        if (emvStatus) {
            SvrHelper.instance().cancelOperation();
            stopEmv(IPinpadCode.OTHER_UNKNOWN, null);
            emvStatus = false;
        }
        Logger.v("Start transaction");
        String amt = etAmount.getText().toString();
        // Logger.i("Amount:" + amt);
        int cardSupport = 0x00;
        if (swipeCBox.isChecked()) {
            cardSupport |= ICardType.TYPE_CARD_MAGNETIC;
        }
        if (insertCBox.isChecked()) {
            cardSupport |= ICardType.TYPE_CARD_CONTACT;
        }
        if (passCBox.isChecked()) {
            cardSupport |= ICardType.TYPE_CARD_CONTACT_LESS;
        }
        // Logger.i("CardType:" + cardSupport);
        if (cardSupport == 0) {
            Logger.w("Please select the card type to support");
            return;
        }
        // Check the card reader
        //if (!checkReader(cardSupport)) return;

        Logger.v("Create the trade request");

        Amount amount = null;
        String formatAmount = "";
        if (!TextUtils.isEmpty(amt) && TextUtils.isDigitsOnly(amt)) {
            // unsigned integer: min 0 ,max value  4,294,967,295
            // java long :max value  0x7fffffffffffffffL
            long lAmt = -1L;
            try {
                lAmt = Long.parseLong(amt);
            } catch (NumberFormatException e) {
                Logger.e("Please enter the correct amount");
                Log.d(TAG, "doTransaction: ",e);
            }
            if (lAmt < 0L) {
                Logger.e("Please enter the correct amount");
                return;
            }
            amount = new Amount(Long.parseLong(amt), 0);

            //have currency exponent
            // formatAmount = formatAmount(lAmt, 2);
            //ignore currency exponent
            formatAmount = String.format(Locale.getDefault(),"%d", lAmt);

        }

        TransRequest transRequest = new TransRequest(transType)
                .setmCurrencyCode("0784")
                .setCardType(cardSupport)
                .setVerifyPinSkip(false)
                .setMagTransQuickPass(false)
                .setMagTransServiceCodeProcess(true)
                .setMaxTimeoutEMVThreadWait(30)
                .setReadRecordCallback(true)
                .setEnableAppSelectCallback(true)
                .setNeedBeep(false)
                .setSeePhoneContinueTrans(isSeePhone);
        transRequest.setAdditionalTlvData("1F300101");

        String[] titles = this.getResources().getStringArray(R.array.trans_type);
        updateDockLCD(((String) Objects.requireNonNull(Array.get(titles, selectedPosition))).toUpperCase(), "Amt:" + formatAmount, "in trading ...");
        startEMV(amount, transRequest);
    }

    @Override
    protected void doEndProcess(int code, String data) {
        Logger.d("onEndProcess: 0x" + Integer.toHexString(code) + " - " + ErrCode.toString(code));
//            iemv.respondEvent(null);
        Logger.d("Trans Card Type: 0x%02X", iemv.getTransCardtype());
        emvStatus = false;
        outputResult(code, data);

        if (code == ErrCode.ERR_RESTART_B || code == ErrCode.ERR_END_APP_B) {
            isSeePhone = false;
            doTransaction();
        } else if (code == ErrCode.ERR_SEE_PHONE) {
            isSeePhone = true;
            doTransaction();
        }
        //if ("F360".equals(Build.MODEL)) {
            //led.ledDefault();
            led.ledCardIndicator(0x00, 0x00, 0x00, 0x00);
        //}
    }

    @Override
    protected void doAppSelect(List<CandidateAIDInfo> list) {
        if (list == null || list.isEmpty()) return;
        if (appList == null) return;
        new Handler(Looper.getMainLooper())
                .post(() -> {
                    layoutList.setVisibility(View.VISIBLE);
                    List<String> listApp = new ArrayList<>();
                    //Get the application name that needs to be displayed from CandidateAID
                    for (int i = 0; i < list.size(); i++) {
                        listApp.add(EncodeConversion(list.get(i).getApplicationLabel_tag50(), list.get(i).getCodeTableIndex_tag9F11()));
                        Logger.v(i + " " + EncodeConversion(list.get(i).getApplicationLabel_tag50(), list.get(i).getCodeTableIndex_tag9F11()));
                    }
                    Logger.d("Please select application");
                    appList.setOnItemClickListener(listListener);
                    appList.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listApp));
                });
    }

    private final AdapterView.OnItemClickListener listListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            layoutList.setVisibility(View.GONE);
            Logger.d("Select application: " + position);
            String tlvData = String.format("1F6601%02x", (byte) position);
            iemv.respondEvent(tlvData);
        }
    };
}
