package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import id.uniflo.uniedc.R;

/**
 * AID Settings Activity
 * Allows users to enable/disable Application Identifiers (AIDs) for card reading
 * AIDs are grouped by issuer/network for better organization
 */
public class AidSettingsActivity extends Activity {
    
    private static final String PREFS_NAME = "AidSettings";
    private static final String KEY_ENABLED_AIDS = "enabled_aids_";
    
    private SharedPreferences prefs;
    private LinearLayout aidContainer;
    private Map<String, CheckBox> aidCheckBoxes = new HashMap<>();
    
    // Comprehensive AID database for Indonesian market
    private static final List<AidGroup> AID_GROUPS = new ArrayList<>();
    
    static {
        // NSICCS (National Standard of Indonesia Chip Card Specification)
        AidGroup nsiccs = new AidGroup("NSICCS - National Standard");
        nsiccs.addAid(new Aid("A0000006021010", "NSICCS Debit Primary", true)); // Default enabled
        nsiccs.addAid(new Aid("A0000006021020", "NSICCS Debit Secondary", false));
        nsiccs.addAid(new Aid("A0000006023010", "NSICCS ATM Bersama", false));
        nsiccs.addAid(new Aid("A0000006024010", "NSICCS Prima", false));
        nsiccs.addAid(new Aid("A0000006025010", "NSICCS Alto", false));
        AID_GROUPS.add(nsiccs);
        
        // GPN (Gerbang Pembayaran Nasional - National Payment Gateway)
        AidGroup gpn = new AidGroup("GPN - National Payment Gateway");
        gpn.addAid(new Aid("A000000602", "GPN Primary", false));
        gpn.addAid(new Aid("A0000006020000", "GPN Debit", false));
        gpn.addAid(new Aid("A0000006020100", "GPN Credit", false));
        AID_GROUPS.add(gpn);
        
        // Indonesian Bank Networks
        AidGroup networks = new AidGroup("Indonesian Networks");
        networks.addAid(new Aid("A0000006023010", "ATM Bersama", false));
        networks.addAid(new Aid("A0000006024010", "Prima", false));
        networks.addAid(new Aid("A0000006025010", "Alto", false));
        networks.addAid(new Aid("A0000006026010", "Link", false));
        AID_GROUPS.add(networks);
        
        // Artajasa
        AidGroup artajasa = new AidGroup("Artajasa");
        artajasa.addAid(new Aid("A0000006723010", "Artajasa ATM", false));
        artajasa.addAid(new Aid("A0000006723020", "Artajasa Debit", false));
        artajasa.addAid(new Aid("A0000006723030", "Artajasa Credit", false));
        AID_GROUPS.add(artajasa);
        
        // Rintis
        AidGroup rintis = new AidGroup("Rintis");
        rintis.addAid(new Aid("A0000006991010", "Rintis Primary", false));
        rintis.addAid(new Aid("A0000006991020", "Rintis Debit", false));
        rintis.addAid(new Aid("A0000006991030", "Rintis Credit", false));
        AID_GROUPS.add(rintis);
        
        // International Card Schemes
        AidGroup visa = new AidGroup("Visa");
        visa.addAid(new Aid("A0000000031010", "Visa Debit/Credit", false));
        visa.addAid(new Aid("A0000000032010", "Visa Electron", false));
        visa.addAid(new Aid("A0000000032020", "Visa V Pay", false));
        visa.addAid(new Aid("A0000000033010", "Visa Interlink", false));
        visa.addAid(new Aid("A0000000038010", "Visa Plus", false));
        AID_GROUPS.add(visa);
        
        AidGroup mastercard = new AidGroup("Mastercard");
        mastercard.addAid(new Aid("A0000000041010", "Mastercard Credit/Debit", false));
        mastercard.addAid(new Aid("A0000000042010", "Mastercard Specific", false));
        mastercard.addAid(new Aid("A0000000043010", "Mastercard International", false));
        mastercard.addAid(new Aid("A0000000043060", "Mastercard Maestro", false));
        mastercard.addAid(new Aid("A0000000044010", "Mastercard US", false));
        mastercard.addAid(new Aid("A0000000046000", "Mastercard Cirrus", false));
        AID_GROUPS.add(mastercard);
        
        AidGroup jcb = new AidGroup("JCB");
        jcb.addAid(new Aid("A0000000651010", "JCB Credit/Debit", false));
        jcb.addAid(new Aid("A0000000651020", "JCB Specific", false));
        AID_GROUPS.add(jcb);
        
        AidGroup unionpay = new AidGroup("UnionPay");
        unionpay.addAid(new Aid("A000000333010101", "UnionPay Debit", false));
        unionpay.addAid(new Aid("A000000333010102", "UnionPay Credit", false));
        unionpay.addAid(new Aid("A000000333010103", "UnionPay Quasi-Credit", false));
        AID_GROUPS.add(unionpay);
        
        // Indonesian Banks Specific
        AidGroup banks = new AidGroup("Indonesian Banks");
        banks.addAid(new Aid("A0000006581010", "Bank Mandiri", false));
        banks.addAid(new Aid("A0000006581020", "Bank Mandiri Syariah", false));
        banks.addAid(new Aid("A0000006582010", "BCA", false));
        banks.addAid(new Aid("A0000006583010", "BRI", false));
        banks.addAid(new Aid("A0000006583020", "BRI Syariah", false));
        banks.addAid(new Aid("A0000006584010", "BNI", false));
        banks.addAid(new Aid("A0000006585010", "BTN", false));
        banks.addAid(new Aid("A0000006586010", "Bank Danamon", false));
        banks.addAid(new Aid("A0000006587010", "Bank Permata", false));
        banks.addAid(new Aid("A0000006588010", "Bank CIMB Niaga", false));
        banks.addAid(new Aid("A0000006589010", "Bank Maybank", false));
        AID_GROUPS.add(banks);
        
        // E-Money / E-Wallet
        AidGroup emoney = new AidGroup("E-Money / E-Wallet");
        emoney.addAid(new Aid("A0000006701010", "Flazz BCA", false));
        emoney.addAid(new Aid("A0000006702010", "Mandiri E-Money", false));
        emoney.addAid(new Aid("A0000006703010", "BRI Brizzi", false));
        emoney.addAid(new Aid("A0000006704010", "BNI TapCash", false));
        emoney.addAid(new Aid("A0000006705010", "Telkomsel T-Cash", false));
        emoney.addAid(new Aid("A0000006706010", "Jakcard Bank DKI", false));
        AID_GROUPS.add(emoney);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aid_settings);
        
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        initViews();
        loadAidGroups();
        setupButtons();
    }
    
    private void initViews() {
        aidContainer = findViewById(R.id.aid_container);
        
        // Setup header
        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());
        
        TextView titleText = findViewById(R.id.title_text);
        titleText.setText("AID Settings");
        
        TextView subtitleText = findViewById(R.id.subtitle_text);
        subtitleText.setText("Select Application Identifiers for card reading");
    }
    
    private void loadAidGroups() {
        LayoutInflater inflater = LayoutInflater.from(this);
        
        for (AidGroup group : AID_GROUPS) {
            // Create group header
            View groupHeader = inflater.inflate(R.layout.item_aid_group_header, aidContainer, false);
            TextView groupTitle = groupHeader.findViewById(R.id.group_title);
            TextView groupCount = groupHeader.findViewById(R.id.group_count);
            LinearLayout groupItems = groupHeader.findViewById(R.id.group_items);
            ImageView expandIcon = groupHeader.findViewById(R.id.expand_icon);
            
            groupTitle.setText(group.name);
            groupCount.setText(group.aids.size() + " AIDs");
            
            // Add click listener to expand/collapse
            View headerClickArea = groupHeader.findViewById(R.id.header_click_area);
            headerClickArea.setOnClickListener(v -> {
                if (groupItems.getVisibility() == View.VISIBLE) {
                    groupItems.setVisibility(View.GONE);
                    expandIcon.setRotation(0);
                } else {
                    groupItems.setVisibility(View.VISIBLE);
                    expandIcon.setRotation(90);
                }
            });
            
            // Add AIDs for this group
            for (Aid aid : group.aids) {
                View aidItem = inflater.inflate(R.layout.item_aid_checkbox, groupItems, false);
                CheckBox checkBox = aidItem.findViewById(R.id.aid_checkbox);
                TextView aidName = aidItem.findViewById(R.id.aid_name);
                TextView aidValue = aidItem.findViewById(R.id.aid_value);
                
                aidName.setText(aid.name);
                aidValue.setText(formatAid(aid.aid));
                
                // Load saved state or use default
                boolean isEnabled = prefs.getBoolean(KEY_ENABLED_AIDS + aid.aid, aid.defaultEnabled);
                checkBox.setChecked(isEnabled);
                
                // Store reference
                aidCheckBoxes.put(aid.aid, checkBox);
                
                groupItems.addView(aidItem);
            }
            
            // Start collapsed except for NSICCS
            if (!group.name.contains("NSICCS")) {
                groupItems.setVisibility(View.GONE);
            } else {
                expandIcon.setRotation(90);
            }
            
            aidContainer.addView(groupHeader);
        }
    }
    
    private void setupButtons() {
        Button btnSave = findViewById(R.id.btn_save);
        Button btnReset = findViewById(R.id.btn_reset);
        Button btnSelectAll = findViewById(R.id.btn_select_all);
        Button btnDeselectAll = findViewById(R.id.btn_deselect_all);
        
        btnSave.setOnClickListener(v -> saveSettings());
        btnReset.setOnClickListener(v -> resetToDefaults());
        btnSelectAll.setOnClickListener(v -> selectAll(true));
        btnDeselectAll.setOnClickListener(v -> selectAll(false));
    }
    
    private void saveSettings() {
        SharedPreferences.Editor editor = prefs.edit();
        
        for (Map.Entry<String, CheckBox> entry : aidCheckBoxes.entrySet()) {
            editor.putBoolean(KEY_ENABLED_AIDS + entry.getKey(), entry.getValue().isChecked());
        }
        
        editor.apply();
        Toast.makeText(this, "AID settings saved", Toast.LENGTH_SHORT).show();
        finish();
    }
    
    private void resetToDefaults() {
        for (AidGroup group : AID_GROUPS) {
            for (Aid aid : group.aids) {
                CheckBox checkBox = aidCheckBoxes.get(aid.aid);
                if (checkBox != null) {
                    checkBox.setChecked(aid.defaultEnabled);
                }
            }
        }
        Toast.makeText(this, "Reset to defaults", Toast.LENGTH_SHORT).show();
    }
    
    private void selectAll(boolean select) {
        for (CheckBox checkBox : aidCheckBoxes.values()) {
            checkBox.setChecked(select);
        }
    }
    
    private String formatAid(String aid) {
        // Format AID for display (add spaces every 2 chars)
        StringBuilder formatted = new StringBuilder();
        for (int i = 0; i < aid.length(); i += 2) {
            if (i > 0) formatted.append(" ");
            formatted.append(aid.substring(i, Math.min(i + 2, aid.length())));
        }
        return formatted.toString();
    }
    
    // Helper method to get enabled AIDs
    public static List<String[]> getEnabledAids(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        List<String[]> enabledAids = new ArrayList<>();
        
        for (AidGroup group : AID_GROUPS) {
            for (Aid aid : group.aids) {
                boolean isEnabled = prefs.getBoolean(KEY_ENABLED_AIDS + aid.aid, aid.defaultEnabled);
                if (isEnabled) {
                    enabledAids.add(new String[]{aid.name, aid.aid});
                }
            }
        }
        
        // If no AIDs are enabled, return default NSICCS
        if (enabledAids.isEmpty()) {
            enabledAids.add(new String[]{"NSICCS Debit Primary", "A0000006021010"});
        }
        
        return enabledAids;
    }
    
    // Data classes
    private static class AidGroup {
        String name;
        List<Aid> aids = new ArrayList<>();
        
        AidGroup(String name) {
            this.name = name;
        }
        
        void addAid(Aid aid) {
            aids.add(aid);
        }
    }
    
    private static class Aid {
        String aid;
        String name;
        boolean defaultEnabled;
        
        Aid(String aid, String name, boolean defaultEnabled) {
            this.aid = aid;
            this.name = name;
            this.defaultEnabled = defaultEnabled;
        }
    }
}