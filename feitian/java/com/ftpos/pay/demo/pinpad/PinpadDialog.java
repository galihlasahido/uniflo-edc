package com.ftpos.pay.demo.pinpad;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.ftpos.library.smartpos.emv.Emv;
import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.pin.OnPinInputListener;
import com.ftpos.library.smartpos.pin.PinSeting;
import com.ftpos.pay.demo.R;
import com.ftpos.pay.demo.SvrHelper;
import com.ftpos.pay.demo.utils.BytesUtil;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.Arrays;

import static com.ftpos.apiservice.aidl.emv.KeyRegion.KEY_REGION_TYPE_LONG;
import static com.ftpos.apiservice.aidl.emv.KeyRegion.KEY_REGION_TYPE_SHORT;
import static com.ftpos.library.smartpos.emv.IPinBlockFormat.BLOCK_FORMAT_0;
import static com.ftpos.library.smartpos.keymanager.KeyType.KEY_TYPE_PEK;
import static com.ftpos.pay.demo.view.BaseEmvActivity.TIMEOUT_PINPAD;

/**
 * @author GuoJirui.
 * @date 2021/5/11.
 * @desc Dialog style interface
 */
public class PinpadDialog implements Handler.Callback {
    private Activity activity;
    private WeakReference<Dialog> mDialog;

    private TextView[] tvDigits = new TextView[10];
    private Button btnCancel;
    private View tvDelete;
    private TextView tvOk;
    private View keyboard;

    private TextView mTvTitle;
    private TextView mTvAmount;
    private TextView mTvTips;
    private TextView mTvPassword;

    private PinSeting pinSeting = null;
    private Emv emv = null;

    private final PinpadResultListener listener;
    private Handler handler = null;

    /**
     * initialize pinpad dialog
     *
     * @param activity activity
     * @param intent   data
     * @param listener listener
     */
    public PinpadDialog(Activity activity, Intent intent, PinpadResultListener listener) {
        this.activity = activity;
        this.listener = listener;
        handler = new Handler(activity.getMainLooper(), this);

        mDialog = new WeakReference<>(new Dialog(activity, R.style.BaseDialog));
        View view = LayoutInflater.from(activity)
                .inflate(R.layout.activity_pinpad, null);
//        mDialog.get().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        mDialog.get().getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
//            @Override
//            public void onSystemUiVisibilityChange(int visibility) {
//                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
//                        View.SYSTEM_UI_FLAG_FULLSCREEN |
//                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
//                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//                uiOptions |= 0x00001000;
//                mDialog.get().getWindow().getDecorView().setSystemUiVisibility(uiOptions);
//            }
//        });

        emv = SvrHelper.instance().getEmv();
        pinSeting = SvrHelper.instance().getPinSetting();

        //Initialization interface
        initView(view);
        //Update the pan of the pinSeting
        updatePan();
        //Handle intent
        handleIntent(intent);

        if (mDialog != null && mDialog.get() != null) {
            mDialog.get().setContentView(view);
            mDialog.get().setCancelable(false);
            mDialog.get().onAttachedToWindow();
            mDialog.get().setCanceledOnTouchOutside(false);
            mDialog.get().setOnShowListener(dialog -> {
                if (mDialog.get().isShowing()) {
                    doStartInputPin();
                }
            });
        }
    }

    /**
     * show dialog
     */
    public void show() {
        initStyle();
        mDialog.get().show();
    }

    /**
     * dismiss dialog
     */
    public void dismiss() {
        if (mDialog.get().isShowing()) {
            mDialog.get().dismiss();
        }
    }

    private void initView(View view) {
        try {

            tvDigits[0] = (TextView) view.findViewById(R.id.id_tv_0);
            tvDigits[1] = (TextView) view.findViewById(R.id.id_tv_1);
            tvDigits[2] = (TextView) view.findViewById(R.id.id_tv_2);
            tvDigits[3] = (TextView) view.findViewById(R.id.id_tv_3);
            tvDigits[4] = (TextView) view.findViewById(R.id.id_tv_4);
            tvDigits[5] = (TextView) view.findViewById(R.id.id_tv_5);
            tvDigits[6] = (TextView) view.findViewById(R.id.id_tv_6);
            tvDigits[7] = (TextView) view.findViewById(R.id.id_tv_7);
            tvDigits[8] = (TextView) view.findViewById(R.id.id_tv_8);
            tvDigits[9] = (TextView) view.findViewById(R.id.id_tv_9);

            keyboard = (View) view.findViewById(R.id.layout_root);
            btnCancel = (Button) view.findViewById(R.id.id_btn_cancel);
            tvDelete = (View) view.findViewById(R.id.id_ll_delete);
            tvOk = (TextView) view.findViewById(R.id.id_tv_ok);
        } catch (Exception e) {
            notifyFinish(IPinpadCode.PINPAD_EXCEPTION, "Exception in initializing Pinpad interface  ");
            return;
        }

        if (pinSeting == null) {
            notifyFinish(IPinpadCode.PINPAD_EXCEPTION, "PinSetting is null ");
            return;
        }
        pinSeting.setMinPinLen(4);
        pinSeting.setMaxPinLen(12);

        pinSeting.setButtonCancel(btnCancel);
        pinSeting.setButtonNum(tvDigits);
        pinSeting.setButtonDel(tvDelete);
        pinSeting.setButtonOK(tvOk);
        pinSeting.setButtonKeyboard(keyboard);
        pinSeting.setRandomkeyboard(true); //
        pinSeting.setOnlinePinKeyIndex(0x0001);
        //The key system using encrypted online PIN here is MK/SK
        pinSeting.setOnlinePinKeyType(KEY_TYPE_PEK);
        //If you need to use the IPEK key system to encrypt the online PIN, use the code commented below,
        //and call loadDukptKey() when the transaction is initialized to complete the import of the IPEK key.
//        pinSeting.setOnlinePinKeyType(KEY_TYPE_IPEK);
        pinSeting.setOnlinePinBlockFormat(BLOCK_FORMAT_0);
        pinSeting.setTimeout(TIMEOUT_PINPAD);

        mTvTitle = (TextView) view.findViewById(R.id.id_tv_title);
        mTvAmount = (TextView) view.findViewById(R.id.id_tv_amount);
        mTvTips = (TextView) view.findViewById(R.id.id_tv_tips);
        mTvPassword = (TextView) view.findViewById(R.id.id_tv_pass);
    }

    private void updatePan() {
        byte[] data = new byte[36];
        int[] len = new int[1];
        emv.getTLV("5A", data, len);
        if (len[0] != 0) {
            byte[] pan = new byte[len[0] - 2];
            System.arraycopy(data, 2, pan, 0, len[0] - 2);
            pinSeting.setPan(BytesUtil.bytes2HexString(pan));
        } else {
            emv.getTLV("57", data, len);
            if (len[0] != 0) {
                String pan = BytesUtil.bytes2HexString(data);
                pinSeting.setPan(pan.substring(4, pan.indexOf("D")));
            } else {
                pinSeting.setPan(null);
            }
        }
    }


    private void handleIntent(Intent intent) {
        if (intent != null) {
            int transType = intent.getIntExtra("Title", -1);
            int position;
            if (transType == 0) {
                position = 0;
            } else if (transType == 1) {
                position = 1;
            } else if (transType == 9) {
                position = 2;
            } else if (transType == 20) {
                position = 3;
            } else {
                position = -1;
            }
            if (position < 0 ) {
                notifyFinish(IPinpadCode.PINPAD_EXCEPTION, "parameter error");
                return;
            }
            String[] titles = activity.getResources().getStringArray(R.array.trans_type);
            mTvTitle.setText((String) Array.get(titles, position));
            long amt = intent.getLongExtra("Amount", -1);
            if (amt < 0) {
                notifyFinish(IPinpadCode.PINPAD_EXCEPTION, "parameter error");
                return;
            }
            mTvAmount.setText(String.valueOf(amt));
            String tips = intent.getStringExtra("Tips");
            mTvTips.setText(tips);
            String hint = intent.getStringExtra("PwdHint");
            mTvPassword.setText(hint);
            if ("F360".equalsIgnoreCase(Build.MODEL)) {
                mTvTips.setVisibility(View.GONE);
                mTvPassword.setTextSize(12);
            }
        }
    }

    private void initStyle() {
        Window dialogWindow = mDialog.get().getWindow();
        WindowManager m = activity.getWindowManager();
        Display d = m.getDefaultDisplay();
        WindowManager.LayoutParams p = dialogWindow.getAttributes();

        Configuration cf = activity.getResources().getConfiguration();
        int ori = cf.orientation;
        if (ori == cf.ORIENTATION_LANDSCAPE) {
            p.width = (int) (d.getWidth() * 0.6);
            p.height = (int) (d.getHeight() * 0.8);
            p.gravity = Gravity.CENTER;
            p.alpha = 0.8f;
        } else if (ori == cf.ORIENTATION_PORTRAIT) {
            if ("F360".equalsIgnoreCase(Build.MODEL)) {
                p.height = (int) (d.getHeight());
            }else {
                p.height = (int) (d.getHeight() * 0.8);
            }
            p.gravity = Gravity.CENTER;
            p.alpha = 0.8f;
        }
        dialogWindow.setAttributes(p);
    }


    /**
     * Start input pin
     */
    private void doStartInputPin() {
        if (emv == null) {
            notifyFinish(IPinpadCode.PINPAD_EXCEPTION, "Emv is null ");
            return;
        }
        new Thread(() -> {
            emv.StartPinInput(pinSeting, inputListener);
        }).start();
    }


    /**
     * Update display password
     *
     * @param len  Password length
     * @param type Key Type
     */
    private void updatePassword(int len, int type) {
        if (type == KEY_REGION_TYPE_SHORT) {
            Toast.makeText(activity, "The input PIN is smaller than the length limit", Toast.LENGTH_SHORT).show();
        } else if (type == KEY_REGION_TYPE_LONG) {
            Toast.makeText(activity, "The input PIN is greater than the length limit", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, "Key Type Value:" + type, Toast.LENGTH_SHORT).show();
        }

        char[] chars = new char[len];
        Arrays.fill(chars, '*');
        mTvPassword.setText(String.valueOf(chars));
    }

    /**
     * Re-input pin
     */
    private void reInputPin(int code) {
        mTvPassword.setText("");
        if (code == 0x63C1) {
            mTvPassword.setText("Last retry");
        } else if ((code & 0x63C0) == 0x63C0) {
            mTvPassword.setText("Enter PIN again");
        }
        new Thread(() -> {
            emv.StartPinInput(pinSeting, inputListener);
        }).start();

    }

    /**
     * Update the numeric keyboard
     *
     * @param view  View
     * @param value Value
     */
    private void updateKeyboard(TextView view, int value) {
        view.setText(String.valueOf(value));
    }


    /**
     * PIN input listener
     */
    private final OnPinInputListener inputListener = new OnPinInputListener() {
        @Override
        public void onDispalyPin(int pinLength, int type) {
            Message msg = Message.obtain();
            msg.what = 0;
            msg.arg1 = pinLength;
            msg.arg2 = type;
            handler.sendMessage(msg);
        }

        @Override
        public void onSuccess(byte[] bytes) {
            notifyFinish(IPinpadCode.PINPAD_SUCCESS, BytesUtil.bytes2HexString(bytes));
        }

        @Override
        public void onError(int errCode) {
            if (errCode != 0x63C0 && (errCode & 0x63C0) == 0x63C0) {
                Message msg = Message.obtain();
                msg.what = 2;
                msg.arg1 = errCode;
                handler.sendMessage(msg);
            } else if (errCode == ErrCode.ERR_JNI_SCERRN_OFF) {
                notifyFinish(IPinpadCode.PINPAD_SCREEN_OFF, String.valueOf(errCode));
            } else {
                notifyFinish(IPinpadCode.PINPAD_ERROR, String.valueOf(errCode));
            }
        }

        @Override
        public void onTimeout() {
            notifyFinish(IPinpadCode.PINPAD_TIMEOUT, null);
        }

        @Override
        public void onCancel() {
            notifyFinish(IPinpadCode.PINPAD_CANCEL, null);
        }

        @Override
        public void onSetDigits(Object o, char c) {
            Message msg = Message.obtain();
            msg.what = 1;
            msg.arg1 = Integer.parseInt(String.valueOf(c));
            msg.obj = o;
            handler.sendMessage(msg);
        }
    };

    private void notifyFinish(int code, String data) {
        Message msg = Message.obtain();
        msg.what = 3;
        msg.arg1 = code;
        msg.obj = data;
        handler.sendMessage(msg);
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        switch (msg.what) {
            case 0:
                updatePassword(msg.arg1, msg.arg2);
                break;
            case 1:
                updateKeyboard((TextView) msg.obj, msg.arg1);
                break;
            case 2:
                reInputPin(msg.arg1);
                break;
            case 3:
                if (listener != null) {
                    listener.onPinpadResultListener(msg.arg1, (String) msg.obj);
                }
                mDialog.get().dismiss();
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * Callback of pinpad
     */
    public interface PinpadResultListener {
        /**
         * the listener of the dialog pinpad
         *
         * @param code code
         * @param data data
         */
        void onPinpadResultListener(int code, String data);
    }
}
