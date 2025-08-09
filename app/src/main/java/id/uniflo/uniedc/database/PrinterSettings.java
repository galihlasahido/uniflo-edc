package id.uniflo.uniedc.database;

public class PrinterSettings {
    private int printDensity;
    private boolean printLogo;
    private boolean printMerchantCopy;
    private boolean printCustomerCopy;
    private String headerLine1;
    private String headerLine2;
    private String footerLine1;
    private String footerLine2;
    
    public PrinterSettings() {
        // Default constructor
    }
    
    // Getters and Setters
    public int getPrintDensity() {
        return printDensity;
    }
    
    public void setPrintDensity(int printDensity) {
        this.printDensity = printDensity;
    }
    
    public boolean isPrintLogo() {
        return printLogo;
    }
    
    public void setPrintLogo(boolean printLogo) {
        this.printLogo = printLogo;
    }
    
    public boolean isPrintMerchantCopy() {
        return printMerchantCopy;
    }
    
    public void setPrintMerchantCopy(boolean printMerchantCopy) {
        this.printMerchantCopy = printMerchantCopy;
    }
    
    public boolean isPrintCustomerCopy() {
        return printCustomerCopy;
    }
    
    public void setPrintCustomerCopy(boolean printCustomerCopy) {
        this.printCustomerCopy = printCustomerCopy;
    }
    
    public String getHeaderLine1() {
        return headerLine1;
    }
    
    public void setHeaderLine1(String headerLine1) {
        this.headerLine1 = headerLine1;
    }
    
    public String getHeaderLine2() {
        return headerLine2;
    }
    
    public void setHeaderLine2(String headerLine2) {
        this.headerLine2 = headerLine2;
    }
    
    public String getFooterLine1() {
        return footerLine1;
    }
    
    public void setFooterLine1(String footerLine1) {
        this.footerLine1 = footerLine1;
    }
    
    public String getFooterLine2() {
        return footerLine2;
    }
    
    public void setFooterLine2(String footerLine2) {
        this.footerLine2 = footerLine2;
    }
}