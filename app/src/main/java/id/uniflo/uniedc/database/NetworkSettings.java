package id.uniflo.uniedc.database;

public class NetworkSettings {
    private String connectionType;
    private String primaryHost;
    private int primaryPort;
    private String secondaryHost;
    private int secondaryPort;
    private int timeout;
    private int retryCount;
    private boolean useSsl;
    private boolean keepAlive;
    private String protocol;
    
    public NetworkSettings() {
        // Default constructor
    }
    
    // Getters and Setters
    public String getConnectionType() {
        return connectionType;
    }
    
    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }
    
    public String getPrimaryHost() {
        return primaryHost;
    }
    
    public void setPrimaryHost(String primaryHost) {
        this.primaryHost = primaryHost;
    }
    
    public int getPrimaryPort() {
        return primaryPort;
    }
    
    public void setPrimaryPort(int primaryPort) {
        this.primaryPort = primaryPort;
    }
    
    public String getSecondaryHost() {
        return secondaryHost;
    }
    
    public void setSecondaryHost(String secondaryHost) {
        this.secondaryHost = secondaryHost;
    }
    
    public int getSecondaryPort() {
        return secondaryPort;
    }
    
    public void setSecondaryPort(int secondaryPort) {
        this.secondaryPort = secondaryPort;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    public int getRetryCount() {
        return retryCount;
    }
    
    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }
    
    public boolean isUseSsl() {
        return useSsl;
    }
    
    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }
    
    public boolean isKeepAlive() {
        return keepAlive;
    }
    
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }
    
    public String getProtocol() {
        return protocol;
    }
    
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}