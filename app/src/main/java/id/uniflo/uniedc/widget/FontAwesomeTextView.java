package id.uniflo.uniedc.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatTextView;

public class FontAwesomeTextView extends AppCompatTextView {

    private static Typeface fontAwesome;

    public FontAwesomeTextView(Context context) {
        super(context);
        init(context);
    }

    public FontAwesomeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FontAwesomeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        if (fontAwesome == null) {
            try {
                fontAwesome = Typeface.createFromAsset(context.getAssets(), "fonts/fa-solid-900.ttf");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        if (fontAwesome != null) {
            setTypeface(fontAwesome);
        }
    }
}