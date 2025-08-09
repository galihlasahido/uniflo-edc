package id.uniflo.uniedc.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import id.uniflo.uniedc.R;

public class PinVerificationDialog {
    
    private static final String DEMO_PIN = "123456"; // Demo PIN for testing
    
    public interface OnPinVerifiedListener {
        void onPinVerified();
        void onPinCancelled();
    }
    
    public static void show(Context context, String message, OnPinVerifiedListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_pin_verification, null);
        
        TextView tvMessage = dialogView.findViewById(R.id.tv_pin_message);
        TextView tvError = dialogView.findViewById(R.id.tv_error);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnVerify = dialogView.findViewById(R.id.btn_verify);
        
        EditText etPin1 = dialogView.findViewById(R.id.et_pin_1);
        EditText etPin2 = dialogView.findViewById(R.id.et_pin_2);
        EditText etPin3 = dialogView.findViewById(R.id.et_pin_3);
        EditText etPin4 = dialogView.findViewById(R.id.et_pin_4);
        EditText etPin5 = dialogView.findViewById(R.id.et_pin_5);
        EditText etPin6 = dialogView.findViewById(R.id.et_pin_6);
        
        EditText[] pinFields = {etPin1, etPin2, etPin3, etPin4, etPin5, etPin6};
        
        if (message != null && !message.isEmpty()) {
            tvMessage.setText(message);
        }
        
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setCancelable(false)
                .create();
        
        // Set up PIN field navigation
        for (int i = 0; i < pinFields.length; i++) {
            final int index = i;
            pinFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && index < pinFields.length - 1) {
                        pinFields[index + 1].requestFocus();
                    }
                    tvError.setVisibility(View.GONE);
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            });
            
            // Handle backspace
            pinFields[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && 
                    event.getAction() == android.view.KeyEvent.ACTION_DOWN &&
                    ((EditText) v).getText().length() == 0 && index > 0) {
                    pinFields[index - 1].requestFocus();
                    pinFields[index - 1].setText("");
                    return true;
                }
                return false;
            });
        }
        
        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            listener.onPinCancelled();
        });
        
        btnVerify.setOnClickListener(v -> {
            // Collect PIN
            StringBuilder pin = new StringBuilder();
            for (EditText field : pinFields) {
                pin.append(field.getText().toString());
            }
            
            // Verify PIN
            if (pin.toString().equals(DEMO_PIN)) {
                dialog.dismiss();
                listener.onPinVerified();
            } else {
                // Clear fields and show error
                for (EditText field : pinFields) {
                    field.setText("");
                }
                pinFields[0].requestFocus();
                tvError.setVisibility(View.VISIBLE);
            }
        });
        
        dialog.show();
        
        // Focus first field
        pinFields[0].requestFocus();
    }
}