package id.uniflo.uniedc.bean;

import android.text.TextUtils;

import java.util.regex.Pattern;

public abstract class XmlDataBean {
    public abstract void setTagValue(String name, String... value);

    public abstract byte[] getBytes();

    public abstract int getTlvLens();

    protected String trimSpace(String tmp) {
        return (!TextUtils.isEmpty(tmp)) ? Pattern.compile("\\s*|\t|\r|\n").matcher(tmp).replaceAll("") : "";
    }



}
