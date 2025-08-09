package com.ftpos.ftappdemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.ftpos.apiservice.aidl.led.LedConfig;
import com.ftpos.ftappdemo.util.TestHelper;
import com.ftpos.library.smartpos.device.Device;
import com.ftpos.library.smartpos.led.Led;
import com.ftpos.ftappdemo.R;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RingLightActivity extends AppCompatActivity {
    private static final String TAG = RingLightActivity.class.getSimpleName();

    private Led led;
    private Device device;

    private String tapeType;
    private int redValue = 0;
    private int greenValue = 0;
    private int blueValue = 0;
    private int brightnessValue = 0;
    private int brightnessValueMax = 0;
    private int brightnessStep = 0;
    private int brightnessGap = 0;

    private Spinner ledType;
    private EditText lightness;
    private EditText maxLightness;
    private EditText lightnessStep;
    private EditText lightnessGap;
    private Button btnOn;
    private Button btnOff;
    private Button btnCardIndicatorOn;
    private Button btnCardIndicatorOff;
    private EditText red;
    private EditText green;
    private EditText blue;
    private LinearLayoutCompat rgbSetContainer;
    private LinearLayoutCompat stepAndGapView;

    private int powerLightValue = 0x00;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        TestHelper.setFullscreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ring_light);

        this.led = MainActivity.led;
        this.device = MainActivity.device;
        initView();
    }

    @Override
    protected void onDestroy() {
        led.ledDefault();
        super.onDestroy();
    }

    private void initView() {
        Button mBack = findViewById(R.id.ukey_navbar_left_btn);
        mBack.setOnClickListener(v -> finish());

        Button mClear = findViewById(R.id.ukey_navbar_right_btn);
        mClear.setText("Analog");
        mClear.setOnClickListener(view -> {
//            Intent intent = new Intent(this, AnalogLedActivity.class);
//            startActivity(intent);
        });

        ((TextView) findViewById(R.id.ukey_navbar_title)).setText("Led View");

        ledType = findViewById(R.id.tape_lamp_type);
        red = findViewById(R.id.rgb_red_value);
        green = findViewById(R.id.rgb_green_value);
        blue = findViewById(R.id.rgb_blue_value);
        lightness = findViewById(R.id.lightness_value);
        maxLightness = findViewById(R.id.lightness_max_value);
        lightnessStep = findViewById(R.id.lightness_step_value);
        lightnessGap = findViewById(R.id.lightness_gap_value);
        btnOn = findViewById(R.id.tape_lamp_on);
        btnOff = findViewById(R.id.tape_lamp_off);
        rgbSetContainer = findViewById(R.id.tap_lamp_rgb_set);
        btnCardIndicatorOn = findViewById(R.id.card_indicator_on);
        btnCardIndicatorOff = findViewById(R.id.card_indicator_off);
        stepAndGapView = findViewById(R.id.tape_lamp_step_gap);
        ((CheckBox)findViewById(R.id.power_light_red)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                powerLightValue |= 0x01;
            } else {
                powerLightValue &= 0xFE;
            }
        });

        ((CheckBox)findViewById(R.id.power_light_green)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                powerLightValue |= 0x02;
            } else {
                powerLightValue &= 0xFD;
            }
        });

        ((CheckBox)findViewById(R.id.power_light_blue)).setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                powerLightValue |= 0x04;
            } else {
                powerLightValue &= 0xFB;
            }
        });

        findViewById(R.id.power_indicator_on).setOnClickListener(v -> powerIndicator(true));
        findViewById(R.id.power_indicator_off).setOnClickListener(v -> powerIndicator(false));

        if ("F360".equalsIgnoreCase(device.getProductModel())) {
            initTapeLampValue();
            initTapeLampButton();
        }
    }

    private void powerIndicator(boolean flag) {
        if (flag) {
            powerLightValue |= 0x80;
            led.ledCardIndicator(powerLightValue, 1, 1000, 1000);
        } else {
            powerLightValue &= 0x07;
            if ((powerLightValue & 0x01) == 0x01) {
                led.ledCardIndicator(0x41, 1, 1000, 1000);
            }
            if ((powerLightValue & 0x02) == 0x02) {
                led.ledCardIndicator(0x42, 1, 1000, 1000);
            }
            if ((powerLightValue & 0x04) == 0x04) {
                led.ledCardIndicator(0x44, 1, 1000, 1000);
            }
        }
    }

    private void initTapeLampValue() {
        tapeType = "Tape lamp";
        redValue = 255;
        red.setText("" + redValue);
        greenValue = 0;
        green.setText("" + greenValue);
        blueValue = 0;
        blue.setText("" + blueValue);
        brightnessValue = 20;
        lightness.setText("" + brightnessValue);
        brightnessValueMax = 100;
        maxLightness.setText("" + brightnessValueMax);
        brightnessStep = 10;
        lightnessStep.setText("" + brightnessStep);
        brightnessGap = 100;
        lightnessGap.setText("" + brightnessGap);
        setTapeLampType(tapeType);
    }

    private void setTapeLampType(String type) {
        switch (type) {
            case "Marquee lamp":
                tapeType = "Marquee lamp";
                showRGBSetView(false);
                showLightnessSetView(false);
                showLightnessSetStepAndGap(true);
                lightnessStep.setEnabled(false);
                break;
            case "Breath lamp":
                tapeType = "Breath lamp";
                showRGBSetView(true);
                showLightnessSetView(true);
                showLightnessSetStepAndGap(true);
                break;
            case "Tape lamp":
            default:
                tapeType = "Tape lamp";
                showRGBSetView(true);
                showLightnessSetView(false);
                showLightnessSetStepAndGap(false);
                break;
        }
    }

    private void initTapeLampButton() {
        ledType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String type = adapterView.getItemAtPosition(i).toString();
                setTapeLampType(type);

                try {
                    Field field = AdapterView.class.getDeclaredField("mOldSelectedPosition");
                    field.setAccessible(true);  //设置mOldSelectedPosition可访问
                    field.setInt(ledType, AdapterView.INVALID_POSITION); //设置mOldSelectedPosition的值
                } catch (Exception e) {
                    //e.printStackTrace();
                    Logger.getGlobal().log(Level.SEVERE, "set mOldSelectedPosition error", e);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                setTapeLampType(tapeType);
            }
        });

        btnOn.setOnClickListener(v -> {
            led.tapeLampOff();
            getControlValue();
            switch (tapeType) {
                case "Marquee lamp":
                    // Fixed 3 byte in a group, and a total of 17 groups of 51 bytes;
                    List<LedConfig> colors = new ArrayList<LedConfig>();
                    colors.add(new LedConfig(255, 0, 0));
                    colors.add(new LedConfig(0, 255, 0));
                    colors.add(new LedConfig(0, 0, 255));
                    int res = led.marqueeOn(colors, brightnessValue, 0, brightnessGap);
                    break;
                case "Breath lamp":
                    Log.d(TAG, redValue + " " + greenValue + " " + blueValue + " " + brightnessValueMax
                            + " " + brightnessValue + " " + brightnessStep + " " + brightnessGap);
                    LedConfig color1 = new LedConfig(255, 0, 0);
                    LedConfig color2 = new LedConfig(0, 255, 0);
                    LedConfig color3 = new LedConfig(0, 0, 255);
                    LedConfig color4 = new LedConfig(0, 255, 255);
                    List<LedConfig> list = new ArrayList<>();
                    list.add(color1);
                    list.add(color2);
                    list.add(color3);
                    list.add(color4);
                    led.breathOn(list, brightnessValueMax, brightnessValue, brightnessStep, brightnessGap);
                    break;
                case "Tape lamp":
                default:
                    LedConfig ledConfig = new LedConfig(redValue, greenValue, blueValue);
                    led.tapeLampOn(ledConfig, brightnessValue);
                    break;
            }
        });

        btnOff.setOnClickListener((v) -> {
            led.tapeLampOff();
        });

        btnCardIndicatorOn.setOnClickListener(v -> {
            led.ledCardIndicator(3, 10, 200, 200);
        });

        btnCardIndicatorOff.setOnClickListener(v -> {
            led.ledCardIndicator(0, 0, 0, 0);
        });
    }

    private void getControlValue() {
        String tValue = red.getText().toString();
        Log.d(TAG, "rgbRedValue: " + tValue);
        if (tValue.isEmpty()) {
            tValue = "0";
        }
        redValue = Integer.parseInt(tValue);
        if (redValue > 255) {
            redValue = 255;
        } else if (redValue < 0) {
            redValue = 0;
        }

        tValue = green.getText().toString();
        Log.d(TAG, "rgbGreenValue: " + tValue);
        if (tValue.isEmpty()) {
            tValue = "0";
        }
        greenValue = Integer.parseInt(tValue);
        if (greenValue > 255) {
            greenValue = 255;
        } else if (greenValue < 0) {
            greenValue = 0;
        }

        tValue = blue.getText().toString();
        Log.d(TAG, "rgbBlueValue: " + tValue);
        if (tValue.isEmpty()) {
            tValue = "0";
        }
        blueValue = Integer.parseInt(tValue);
        if (blueValue > 255) {
            blueValue = 255;
        } else if (blueValue < 0) {
            blueValue = 0;
        }

        tValue = lightness.getText().toString();
        Log.d(TAG, "lightnessValue:" + tValue);
        if (tValue.isEmpty()) {
            tValue = "0";
        }
        brightnessValue = Integer.parseInt(tValue);
        if (brightnessValue > 100) {
            brightnessValue = 100;
        } else if (brightnessValue < 0) {
            brightnessValue = 0;
        }

        tValue = maxLightness.getText().toString();
        Log.d(TAG, "lightnessMaxValue:" + tValue);
        if (tValue.length() < 1) {
            tValue = "0";
        }
        brightnessValueMax = Integer.parseInt(tValue);
        if (brightnessValueMax > 100) {
            brightnessValueMax = 100;
        } else if (brightnessValueMax < 0) {
            brightnessValueMax = 0;
        }

        if (brightnessValue > brightnessValueMax) {
            brightnessValue = brightnessValueMax;
        }

        tValue = lightnessStep.getText().toString();
        Log.d(TAG, "lightnessStepValue:" + tValue);
        if (tValue.length() < 1) {
            tValue = "0";
        }
        brightnessStep = Integer.parseInt(tValue);
        if (brightnessStep < 1) {
            brightnessStep = 1;
        }

        tValue = lightnessGap.getText().toString();
        Log.d(TAG, "lightnessGapValue:" + tValue);
        if (tValue.length() < 1) {
            tValue = "0";
        }
        brightnessGap = Integer.parseInt(tValue);
        if (brightnessGap < 1) {
            brightnessGap = 1;
        }
    }

    private void showRGBSetView(boolean flag) {
        if (flag) {
            rgbSetContainer.setVisibility(View.VISIBLE);
        } else {
            rgbSetContainer.setVisibility(View.GONE);
        }
    }

    private void showLightnessSetView(boolean flag) {
        if (flag) {
            maxLightness.setVisibility(View.VISIBLE);
        } else {
            maxLightness.setVisibility(View.GONE);
        }
    }

    private void showLightnessSetStepAndGap(boolean flag) {
        if (flag) {
            stepAndGapView.setVisibility(View.VISIBLE);
        } else {
            stepAndGapView.setVisibility(View.GONE);
        }
    }
}
