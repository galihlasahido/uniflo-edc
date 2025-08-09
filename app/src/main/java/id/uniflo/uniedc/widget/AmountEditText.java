package id.uniflo.uniedc.widget;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;

import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class AmountEditText extends TextInputEditText {
    
    private String current = "";
    private boolean isFormatting = false;
    
    public AmountEditText(Context context) {
        super(context);
        init();
    }
    
    public AmountEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public AmountEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        // Set max length to prevent overflow
        setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) {
                    return;
                }
                
                isFormatting = true;
                
                String str = s.toString();
                
                // Remove all non-digits
                String cleanString = str.replaceAll("[^\\d]", "");
                
                if (!cleanString.equals(current)) {
                    current = cleanString;
                    
                    if (!cleanString.isEmpty()) {
                        try {
                            // Parse the number
                            long parsed = Long.parseLong(cleanString);
                            
                            // Format with thousand separators
                            DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
                            symbols.setGroupingSeparator('.');
                            DecimalFormat formatter = new DecimalFormat("#,###", symbols);
                            formatter.setDecimalFormatSymbols(symbols);
                            
                            String formatted = formatter.format(parsed);
                            
                            // Update the text
                            setText(formatted);
                            setSelection(formatted.length());
                        } catch (NumberFormatException e) {
                            // Handle error
                            setText("");
                        }
                    } else {
                        setText("");
                    }
                }
                
                isFormatting = false;
            }
        });
    }
    
    /**
     * Get the raw amount value without formatting
     * @return the amount as a long value
     */
    public long getAmount() {
        String text = getText().toString();
        String cleanString = text.replaceAll("[^\\d]", "");
        
        if (cleanString.isEmpty()) {
            return 0;
        }
        
        try {
            return Long.parseLong(cleanString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Set the amount value programmatically
     * @param amount the amount to set
     */
    public void setAmount(long amount) {
        if (amount > 0) {
            setText(String.valueOf(amount));
        } else {
            setText("");
        }
    }
    
    /**
     * Check if the amount field has a valid value
     * @return true if the amount is greater than 0
     */
    public boolean hasValidAmount() {
        return getAmount() > 0;
    }
}