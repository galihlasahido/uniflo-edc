package com.ftpos.pay.demo.constants;

/**
 * @author GuoJirui.
 * @date 2021/4/26.
 * @desc Define parameter file mappings
 */
public enum IParamType {
    TYPE_EMV_ACQUIRER_PARAM("EMV_DefaultParams.xml"),
    TYPE_APP_PARAM_EMV("EMV_AppParameters.xml"),
    TYPE_APP_PARAM_EMVCL("EMVCL_AppParameters.xml"),
    TYPE_CA_PUBKEY("CAPUBKEY.xml"),
    TYPE_EMVCL_DRL("EMVCL_DRL.xml"),
    TYPE_CRL("CRL.xml"),
    TYPE_EXCEPTION_LIST("ExceptionList.xml");

    private final String path;

    IParamType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
