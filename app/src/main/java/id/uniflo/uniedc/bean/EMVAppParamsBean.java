package id.uniflo.uniedc.bean;

import android.text.TextUtils;
import android.util.Log;
import id.uniflo.uniedc.tlv.TLV;
import id.uniflo.uniedc.util.BytesUtil;
import java.util.HashMap;


public class EMVAppParamsBean extends XmlDataBean {
    private static HashMap<String, String> TAG_LIST = new HashMap<>();

    static {
        TAG_LIST.put("AID", "9F06");
        TAG_LIST.put("ASI", "1F14");
        TAG_LIST.put("TerminalActionCodeDefault", "1F04");
        TAG_LIST.put("TerminalActionCodeDenial", "1F05");
        TAG_LIST.put("TerminalActionCodeOnline", "1F06");
        TAG_LIST.put("TransCurrencyCode", "5F2A");
        TAG_LIST.put("TerminalCountryCode", "9F1A");
        TAG_LIST.put("TerminalCapabilities", "9F33");
        TAG_LIST.put("AdditionalTerminalCapabilities", "9F40");
        TAG_LIST.put("MaxTargetPercentBiasedRandSelection", "1F07");
        TAG_LIST.put("TargetPercentBiasedRandSelection", "1F08");
        TAG_LIST.put("ThresholdValueBiasedRandSelection", "1F09");
        TAG_LIST.put("TerminalFloorLimit", "9F1B");
        TAG_LIST.put("TransCurrencyExponent", "5F36");
        TAG_LIST.put("AcquirerID", "9F01");
        TAG_LIST.put("AppVersionNumber", "9F09");
        TAG_LIST.put("MerchantCategoryCode", "9F15");
        TAG_LIST.put("MerchantID", "9F16");
        TAG_LIST.put("TerminalID", "9F1C");
        TAG_LIST.put("TerminalRiskManageData", "9F1D");
        TAG_LIST.put("IFDSerial", "9F1E");
        TAG_LIST.put("TransRefCurrencyCode", "9F3C");
        TAG_LIST.put("TransRefCurrencyExponent", "9F3D");
        TAG_LIST.put("MerchantNameLocation", "9F4E");
        TAG_LIST.put("DefaultDDOL", "1F02");
        TAG_LIST.put("DefaultTDOL", "1F03");
        TAG_LIST.put("TransSupportFloorlimitCheck", "1F12");
        TAG_LIST.put("TransSupportTransLog", "1F13");
        TAG_LIST.put("TransSupportRandomTransSelection", "1F15");
        TAG_LIST.put("TransSupportVelocityCheck", "1F16");
        TAG_LIST.put("TransForceOnline", "1F0A");
        TAG_LIST.put("ExtraTags", null);
    }


    private byte[] tlvBuff = new byte[1024];
    private int[] tlvLen = new int[1];

    @Override
    public void setTagValue(String name, String... value) {
        byte[] dataBuf = new byte[256];
        byte[] tagBuf = new byte[3];
        int valueLen = 0;

        if (TextUtils.isEmpty(value[0])) return;
        String tag = TAG_LIST.get(name);
        String val = trimSpace(value[0]);
        if (name.equalsIgnoreCase("Parameters")) {
            Log.d("AID", "setTagValue: "+val);
            tagBuf = BytesUtil.hexString2Bytes(TAG_LIST.get("AID"));
            dataBuf = BytesUtil.hexString2Bytes(val);
            valueLen = dataBuf.length;
            TLV.addTlvData(tlvBuff, tagBuf, dataBuf, valueLen, tlvLen);
        } else {
            if (name.equalsIgnoreCase("ExtraTags")) {
                dataBuf = BytesUtil.hexString2Bytes(val);
                System.arraycopy(dataBuf, 0, tlvBuff, tlvLen[0], dataBuf.length);
                tlvLen[0] += dataBuf.length;
            } else if (!TextUtils.isEmpty(tag)) {
                tagBuf = BytesUtil.hexString2Bytes(tag);
                if ((tag.equals("9F16")) || (tag.equals("9F1C")) || (tag.equals("9F1E")) || (tag.equals("9F4E"))) {
                    if ((tag.equals("9F1C")) || (tag.equals("9F1E"))) {
                        if (val.length() > 8) {
                            //TODO:Throw Exception
                        } else {
                            TLV.paddingData(1, val.getBytes(), 8, (byte) 0x00, dataBuf);
                            valueLen = 8;
                        }
                    }
                    if (tag.equals("9F16")) {
                        if (val.length() > 15) {
                            //TODO:Throw Exception
                        } else {
                            TLV.paddingData(1, val.getBytes(), 15, (byte) 0x00, dataBuf);
                            valueLen = 15;
                        }
                    }
                    if (tag.equals("9F4E")) {
                        dataBuf = val.getBytes();
                        valueLen = dataBuf.length;
                    }
                } else {
                    if (val.length() % 2 == 1) {
                        //TODO:Throw Exception
                    } else {
                        dataBuf = BytesUtil.hexString2Bytes(val);
                        valueLen = dataBuf.length;
                    }
                }
                TLV.addTlvData(tlvBuff, tagBuf, dataBuf, valueLen, tlvLen);
            }

        }
    }


    @Override
    public byte[] getBytes() {
        return tlvBuff;
    }

    @Override
    public int getTlvLens() {
        return tlvLen[0];
    }
}
