package id.uniflo.uniedc.sdk;

/**
 * Enum for supported SDK types
 */
public enum SDKType {
    FEITIAN("feitian", "Feitian SDK"),
    PAX("pax", "PAX SDK"),
    VERIFONE("verifone", "Verifone SDK"),
    EMULATOR("emulator", "Emulator/Mock SDK");
    
    private final String code;
    private final String displayName;
    
    SDKType(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public static SDKType fromCode(String code) {
        for (SDKType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return EMULATOR; // Default to emulator
    }
}