package com.ftpos.ftappdemo;

import static java.lang.Math.ceil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ftpos.library.smartpos.errcode.ErrCode;
import com.ftpos.library.smartpos.printer.AlignStyle;
import com.ftpos.library.smartpos.printer.OnPrinterCallback;
import com.ftpos.library.smartpos.printer.PrintStatus;
import com.ftpos.library.smartpos.printer.Printer;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class PrinterActivity extends BaseActivity {

    private TextView mShowResultTv;
    private Printer printer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);

        initView();

        this.printer = MainActivity.printer;
    }

    private void initView() {
        TextView mTitle = findViewById(R.id.ukey_navbar_title);
        mTitle.setText("Printer View");
        findViewById(R.id.ukey_navbar_left_btn).setOnClickListener(this);
        findViewById(R.id.ukey_navbar_right_btn).setOnClickListener(this);
        findViewById(R.id.print_demo).setOnClickListener(this);
        findViewById(R.id.print_picture_demo).setOnClickListener(this);
        findViewById(R.id.print_qr_code_demo).setOnClickListener(this);
        findViewById(R.id.set_font_demo).setOnClickListener(this);
        findViewById(R.id.print_customer_func).setOnClickListener(this);

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
            case R.id.print_demo: {
                printReceipt();
            }
            break;
            case R.id.print_picture_demo: {
                printPicture();
            }
            break;
            case R.id.print_qr_code_demo: {
                printQRCode();
            }
            break;
            case R.id.set_font_demo: {
                printSetFont();
            }
            break;
            case R.id.print_customer_func: {
                printCustomerFunc();
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
     * Print receipt demo, including gray setting, printing text, style setting, gray setting.
     * <p>
     * Function to demonstrate the call system interface and custom bitmap two ways to achieve
     * the function of printing receipts.
     * The advantage of calling the system interface is that it is easy to call without worrying
     * about the details of typesetting.
     * The advantage of the custom bitmap approach is that the display interface is completely
     * controlled by the user, with higher freedom.  If the receipt function is simple, the
     * first method is recommended to invoke the system interface. If the receipt content is
     * complex and the customized content is large, the second method is recommended.
     */
    void printReceipt() {
        try {

            int ret;
            ret = printer.open();
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("open failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            ret = printer.startCaching();
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("startCaching failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            ret = printer.setGray(3);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("startCaching failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            PrintStatus printStatus = new PrintStatus();
            ret = printer.getStatus(printStatus);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("getStatus failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            logMsg("Temperature = " + printStatus.getmTemperature() + "\n");
            logMsg("Gray = " + printStatus.getmGray() + "\n");
            if (!printStatus.getmIsHavePaper()) {
                logMsg("Printer out of paper\n");
                return;
            }

            logMsg("IsHavePaper = true\n");

            printer.setAlignStyle(AlignStyle.PRINT_STYLE_CENTER);
            printer.printStr("MICHAEL KORS\n");

            printer.setAlignStyle(AlignStyle.PRINT_STYLE_LEFT);
            printer.printStr("Please retain this receipt\n");
            printer.printStr("for your exchange.\n");
            printer.printStr("this gift was thoughtfully purchased\n");
            printer.printStr("for you at Michael Kors Chinook Centre.\n");

            ret = printer.getUsedPaperLenManage();
            if (ret < 0) {
                logMsg("getUsedPaperLenManage failed" + String.format(" errCode = 0x%x\n", ret));
            }

            logMsg("UsedPaperLenManage = " + ret + "mm \n");

            Bitmap bitmap = Bitmap.createBitmap(384, 400, Bitmap.Config.RGB_565);

            int k_CurX = 0;
            int k_CurY = 0;
            int k_TextSize = 24;
            paint = new Paint();
            paint.setTextSize(k_TextSize);
            paint.setColor(Color.BLACK);
            Canvas canvas = new Canvas(bitmap);
            bitmap.eraseColor(Color.parseColor("#FFFFFF"));

            Paint.FontMetrics fm = paint.getFontMetrics();
            int k_LineHeight = (int) ceil(fm.descent - fm.ascent);
            String displayStr = "MICHAEL KORS";
            int lineWidth = getTextWidth(displayStr);
            k_CurX = (384 - lineWidth) / 2;
            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
            k_CurY += k_LineHeight + 5;
            displayStr = "Please retain this receipt";
            k_CurX = 0;
            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
            k_CurY += k_LineHeight;
            displayStr = "for your exchange.";
            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
            k_CurY += k_LineHeight;

            displayStr = "this gift was thoughtfully purchased";
            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
            k_CurY += k_LineHeight;

            displayStr = "for you at Michael Kors Chinook ";
            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
            k_CurY += k_LineHeight;

            displayStr = "Centre.";
            canvas.drawText(displayStr, k_CurX, k_CurY + k_TextSize, paint);
            k_CurY += k_LineHeight;


            Bitmap newbitmap = Bitmap.createBitmap(bitmap, 0, 0, 384, k_CurY);

            ret = printer.printBmp(newbitmap);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("printBmp failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            if (!bitmap.isRecycled()) {
                Bitmap mFreeBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.RGB_565);
                canvas.setBitmap(mFreeBitmap);
                canvas = null;
                // canvas.drawColor(0, PorterDuff.Mode.CLEAR);
                bitmap.recycle();
                bitmap = null;
                paint.setTypeface(null);
                paint = null;
            }
            if (newbitmap != null && !newbitmap.isRecycled()) {
                newbitmap.recycle();
                newbitmap = null;
            }
            printer.print(new OnPrinterCallback() {
                @Override
                public void onSuccess() {
                    printer.feed(32);
                    logMsg("print success\n");
                }

                @Override
                public void onError(int i) {
                    logMsg("printBmp failed" + String.format(" errCode = 0x%x\n", i));
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            logMsg("print failed" + e.toString() + "\n");
        }
    }

    private static int getTextWidth(String str) {
        int iRet = 0;
        if (str != null && str.length() > 0) {
            int len = str.length();
            float[] widths = new float[len];
            paint.getTextWidths(str, widths);
            for (int j = 0; j < len; j++) {
                iRet += (int) ceil(widths[j]);
            }
        }

        return iRet;
    }

    private static Paint paint = null;


    /**
     * Example of a call to print a BMP picture
     */
    void printPicture() {
        try {
            int ret;

            ret = printer.open();
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("open failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            ret = printer.startCaching();
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("startCaching failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            ret = printer.setGray(3);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("startCaching failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            printer.setAlignStyle(AlignStyle.PRINT_STYLE_CENTER);
            Bitmap bmp = BitmapFactory.decodeResource(this.getResources(), R.mipmap.feitian);
            ret = printer.printBmp(bmp);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("printBmp failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            printer.print(new OnPrinterCallback() {
                @Override
                public void onSuccess() {
                    printer.feed(32);
                    logMsg("print success\n");
                }

                @Override
                public void onError(int i) {
                    logMsg("printBmp failed" + String.format(" errCode = 0x%x\n", i));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logMsg("print failed" + e.toString() + "\n");
        }
    }

    /**
     * Print the barcode.
     */
    void printQRCode() {
        try {

            int ret;
            ret = printer.open();
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("open failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            ret = printer.startCaching();
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("startCaching failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            ret = printer.setGray(3);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("startCaching failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            printer.setAlignStyle(AlignStyle.PRINT_STYLE_CENTER);
            String QRCode = "Content:1234567890";
            Bundle bundle = new Bundle();

            bundle.putInt("mode", 0);
            bundle.putInt("height", 300);

            ret = printer.printQRCodeEx(QRCode.getBytes(), bundle);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("printQRCode failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            bundle.putInt("mode", 1);
            bundle.putInt("height", 100);
            bundle.putInt("width", 363);
            ret = printer.printQRCodeEx(QRCode.getBytes(), bundle);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("printQRCode failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            printer.print(new OnPrinterCallback() {
                @Override
                public void onSuccess() {
                    printer.feed(32);
                    logMsg("print success\n");
                }

                @Override
                public void onError(int i) {
                    logMsg("printBmp failed" + String.format(" errCode = 0x%x\n", i));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logMsg("print failed" + e.toString() + "\n");
        }
    }

    /**
     * Set the print font, print style and font size.
     * The function will demonstrate three ways to set the font:
     * 1. Set the default font.
     * 2. Set the font for the system font library.
     * 3. Set a customized font.
     */
    void printSetFont() {

        int ret;
        ret = printer.open();
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("open failed" + String.format(" errCode = 0x%x\n", ret));
            return;
        }

        ret = printer.startCaching();
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("startCaching failed" + String.format(" errCode = 0x%x\n", ret));
            return;
        }


        // 1.Set the default font.
        Bundle bundle1 = new Bundle();
        bundle1.putString("font", "DEFAULT");
        bundle1.putInt("format", Typeface.NORMAL);
        bundle1.putInt("style", 0);
        bundle1.putInt("size", 32);
        ret = printer.setFont(bundle1);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("Set default font fail " + String.format(" errCode = 0x%x\n", ret));
            return;
        }

        printer.printStr("DEFAULT\n");
        printer.printStr("Please retain this receipt\n");
        printer.printStr("for your exchange.\n");
        printer.printStr("this gift was thoughtfully purchased\n");
        printer.printStr("for you at Michael Kors Chinook Centre.\n");


        // 2.Set the font for the system font library.
        Bundle bundle2 = new Bundle();
        bundle2.putString("systemFont", "DroidSans-Bold.ttf");
        bundle2.putInt("format", Typeface.NORMAL);
        bundle2.putInt("style", 0);
        bundle2.putInt("size", 32);
        ret = printer.setFont(bundle2);
        if (ret != ErrCode.ERR_SUCCESS) {
            logMsg("Set system font fail " + String.format(" errCode = 0x%x\n", ret));
            return;
        }

        printer.printStr("DroidSans-Bold.ttf\n");
        printer.printStr("Please retain this receipt\n");
        printer.printStr("for your exchange.\n");
        printer.printStr("this gift was thoughtfully purchased\n");
        printer.printStr("for you at Michael Kors Chinook Centre.\n");
        // 3.Set a customized font.
        Bundle bundle3 = new Bundle();

        InputStream ttfFile = null;
        try {
            ttfFile = this.getAssets().open("stsong.ttf");
            String Path = Environment.getExternalStorageDirectory().getAbsolutePath() +"/stsong.ttf" ;
            Log.e("Path","Path = " + Path);
            writeToLocal(Path, ttfFile);

            bundle3.putString("path",Path);
            bundle3.putInt("style", 0);
            bundle3.putInt("size", 32);
            ret = printer.setFont(bundle3);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("Set font fail " + String.format(" errCode = 0x%x\n", ret));
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        printer.printStr("RAGE.TTF\n");
        printer.printStr("Please retain this receipt\n");
        printer.printStr("for your exchange.\n");
        printer.printStr("this gift was thoughtfully purchased\n");
        printer.printStr("for you at Michael Kors Chinook Centre.\n");
//            printer.printStr("\n");
        printer.print(new OnPrinterCallback() {
            @Override
            public void onSuccess() {
                printer.feed(32);
                logMsg("print success\n");
            }

            @Override
            public void onError(int i) {
                logMsg("printBmp failed" + String.format(" errCode = 0x%x\n", i));
            }
        });

    }

    /**
     * Demonstration of functions requested by the customer
     * 1.Print Arabic.
     * 2.Single line print left justified, right justified.
     */
    void printCustomerFunc() {
        try {

            int ret;
            ret = printer.open();
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("open failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            ret = printer.startCaching();
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("startCaching failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            ret = printer.setGray(3);
            if (ret != ErrCode.ERR_SUCCESS) {
                logMsg("startCaching failed" + String.format(" errCode = 0x%x\n", ret));
                return;
            }

            //Print Arabic
            printer.setAlignStyle(AlignStyle.PRINT_STYLE_RIGHT);
            printer.printStr("هذا برنامج اختبار طباعة باللغة العربية");
            printer.printStr("\n\n");

            //Single line print left justified, right justified
            printer.setAlignStyle(AlignStyle.PRINT_STYLE_LEFT);
            printer.printStr("LEFT");

            printer.setAlignStyle(AlignStyle.PRINT_STYLE_RIGHT);
            printer.printStr("RIGHT");

            printer.setAlignStyle(AlignStyle.PRINT_STYLE_CENTER);
            printer.printStr("CENTER");

            printer.print(new OnPrinterCallback() {
                @Override
                public void onSuccess() {
                    printer.feed(32);
                    logMsg("print success\n");
                }

                @Override
                public void onError(int i) {
                    logMsg("printBmp failed" + String.format(" errCode = 0x%x\n", i));
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            logMsg("print failed" + e.toString() + "\n");
        }
    }


    private static void writeToLocal(String destination, InputStream input)
            throws IOException {

        byte[] bytes = new byte[input.available()];

        RandomAccessFile randomFile = null;
        input.read(bytes);
        try {
            randomFile = new RandomAccessFile(destination, "rw");
            randomFile.seek(0);
            randomFile.write(bytes);
            randomFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
