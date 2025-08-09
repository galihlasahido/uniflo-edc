package id.uniflo.uniedc.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import id.uniflo.uniedc.R;
import id.uniflo.uniedc.BuildConfig;

public class AppInfoActivity extends Activity {
    
    private ImageView backButton;
    private TextView titleText;
    private TextView appVersionText;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        
        initViews();
    }
    
    private void initViews() {
        backButton = findViewById(R.id.back_button);
        titleText = findViewById(R.id.title_text);
        appVersionText = findViewById(R.id.app_version_text);
        
        titleText.setText("About");
        appVersionText.setText("Version " + BuildConfig.VERSION_NAME);
        
        backButton.setOnClickListener(v -> finish());
    }
}