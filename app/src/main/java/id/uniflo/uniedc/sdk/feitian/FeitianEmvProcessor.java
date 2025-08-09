package id.uniflo.uniedc.sdk.feitian;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import id.uniflo.uniedc.sdk.interfaces.IEmvProcessor;

/**
 * Feitian EMV Processor Wrapper using Reflection
 */
public class FeitianEmvProcessor implements IEmvProcessor {
    
    private static final String TAG = "FeitianEmvProcessor";
    private final Context context;
    private Object emv;
    private boolean initialized = false;
    private IEmvTransactionListener currentListener;
    
    public FeitianEmvProcessor(Context context) {
        this.context = context;
    }
    
    @Override
    public int init() {
        try {
            // Check if Feitian SDK is available
            if (!FeitianReflectionHelper.isFeitianSDKAvailable()) {
                Log.e(TAG, "Feitian SDK not available");
                return -1;
            }
            
            // Load EMV class
            Class<?> emvClass = FeitianReflectionHelper.loadClass("com.ftpos.library.smartpos.emv.Emv");
            
            if (emvClass == null) {
                Log.e(TAG, "Failed to load EMV class");
                return -1;
            }
            
            // Get EMV instance
            emv = FeitianReflectionHelper.invokeStaticMethod(
                emvClass, "getInstance", 
                new Class<?>[] { Context.class }, 
                new Object[] { context }
            );
            
            if (emv == null) {
                Log.e(TAG, "Failed to get EMV instance");
                return -1;
            }
            
            initialized = true;
            Log.d(TAG, "EMV processor initialized successfully");
            return 0;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize EMV processor", e);
            return -1;
        }
    }
    
    @Override
    public void startTransaction(Map<String, Object> transData, IEmvTransactionListener listener) {
        if (!initialized) {
            Log.e(TAG, "EMV processor not initialized");
            if (listener != null) {
                listener.onError(-1, "EMV processor not initialized");
            }
            return;
        }
        
        currentListener = listener;
        
        try {
            // Load TransRequest class
            Class<?> transRequestClass = FeitianReflectionHelper.loadClass(
                "com.ftpos.library.smartpos.emv.TransRequest"
            );
            
            if (transRequestClass == null) {
                Log.e(TAG, "TransRequest class not found");
                if (listener != null) {
                    listener.onError(-1, "TransRequest class not found");
                }
                return;
            }
            
            // Create TransRequest instance
            Object transRequest = transRequestClass.newInstance();
            
            // Set transaction type
            Integer transType = (Integer) transData.get("transType");
            if (transType != null) {
                // Map our transaction types to Feitian's
                int feitianTransType = 0x00; // Default to sale
                switch (transType) {
                    case TRANS_TYPE_SALE:
                        feitianTransType = 0x00;
                        break;
                    case TRANS_TYPE_REFUND:
                        feitianTransType = 0x20;
                        break;
                    case TRANS_TYPE_BALANCE_INQUIRY:
                        feitianTransType = 0x31;
                        break;
                }
                FeitianReflectionHelper.setField(transRequest, "transType", feitianTransType);
            }
            
            // Set amount
            Long amount = (Long) transData.get("amount");
            if (amount != null) {
                String amountStr = String.format(Locale.US, "%012d", amount);
                FeitianReflectionHelper.setField(transRequest, "amount", amountStr);
            }
            
            // Set other amount
            Long otherAmount = (Long) transData.get("otherAmount");
            if (otherAmount != null) {
                String otherAmountStr = String.format(Locale.US, "%012d", otherAmount);
                FeitianReflectionHelper.setField(transRequest, "otherAmount", otherAmountStr);
            }
            
            // Set date and time
            Date now = new Date();
            String transDate = new SimpleDateFormat("yyMMdd", Locale.US).format(now);
            String transTime = new SimpleDateFormat("HHmmss", Locale.US).format(now);
            FeitianReflectionHelper.setField(transRequest, "transDate", transDate);
            FeitianReflectionHelper.setField(transRequest, "transTime", transTime);
            
            // Load callback interface
            Class<?> callbackClass = FeitianReflectionHelper.loadClass(
                "com.ftpos.library.smartpos.emv.OnEmvResponse"
            );
            
            if (callbackClass == null) {
                Log.e(TAG, "OnEmvResponse class not found");
                if (listener != null) {
                    listener.onError(-1, "OnEmvResponse class not found");
                }
                return;
            }
            
            // Create callback proxy
            Object callback = createEmvCallback(callbackClass);
            
            // Start EMV process
            Object ret = FeitianReflectionHelper.invokeMethod(
                emv, "emvProcess",
                new Class<?>[] { transRequestClass, callbackClass },
                new Object[] { transRequest, callback }
            );
            
            if (ret == null || (Integer)ret != FeitianReflectionHelper.ERR_SUCCESS) {
                Log.e(TAG, "EMV process start failed: " + ret);
                if (listener != null) {
                    listener.onError(-1, "EMV process start failed");
                }
            }
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to start EMV transaction", e);
            if (listener != null) {
                listener.onError(-1, "Failed to start EMV transaction: " + e.getMessage());
            }
        }
    }
    
    private Object createEmvCallback(Class<?> callbackClass) {
        return Proxy.newProxyInstance(
            callbackClass.getClassLoader(),
            new Class<?>[] { callbackClass },
            new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String methodName = method.getName();
                    
                    switch (methodName) {
                        case "onSelectApplication":
                            handleSelectApplication(args);
                            break;
                            
                        case "onConfirmCardInfo":
                            handleConfirmCardInfo(args);
                            break;
                            
                        case "onRequestInputPin":
                            handleRequestInputPin(args);
                            break;
                            
                        case "onRequestOnline":
                            handleRequestOnline();
                            break;
                            
                        case "onTransResult":
                            handleTransResult(args);
                            break;
                    }
                    
                    return null;
                }
            }
        );
    }
    
    private void handleSelectApplication(Object[] args) {
        if (currentListener != null && args != null && args.length > 0) {
            List<String> appList = (List<String>) args[0];
            currentListener.onSelectApplication(appList);
        }
    }
    
    private void handleConfirmCardInfo(Object[] args) {
        if (currentListener != null && args != null && args.length >= 2) {
            String cardNo = (String) args[0];
            String certType = (String) args[1];
            currentListener.onConfirmCardInfo(cardNo, certType);
        }
    }
    
    private void handleRequestInputPin(Object[] args) {
        if (currentListener != null && args != null && args.length >= 2) {
            boolean isOnlinePin = (Boolean) args[0];
            int retryTimes = (Integer) args[1];
            currentListener.onRequestPin(isOnlinePin, retryTimes);
        }
    }
    
    private void handleRequestOnline() {
        if (currentListener != null) {
            currentListener.onRequestOnline();
        }
    }
    
    private void handleTransResult(Object[] args) {
        if (currentListener != null && args != null && args.length > 0) {
            int result = (Integer) args[0];
            
            // Collect final TLV data
            Map<String, String> tlvData = new HashMap<>();
            try {
                // Try to get common TLV tags
                String[] tags = {
                    "9F26", // ARQC
                    "9F27", // CID
                    "9F10", // IAD
                    "9F36", // ATC
                    "5A",   // PAN
                    "95",   // TVR
                    "9A",   // Transaction Date
                    "9C",   // Transaction Type
                    "9F02", // Amount Authorized
                    "5F2A", // Transaction Currency Code
                    "82",   // AIP
                    "9F1A", // Terminal Country Code
                    "9F37", // Unpredictable Number
                    "84"    // DF Name (AID)
                };
                
                for (String tag : tags) {
                    String value = getTlvData(tag);
                    if (value != null) {
                        tlvData.put(tag, value);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error collecting TLV data", e);
            }
            
            currentListener.onTransactionResult(result, tlvData);
        }
    }
    
    @Override
    public void selectApplication(int index) {
        if (!initialized || emv == null) {
            Log.e(TAG, "EMV processor not initialized");
            return;
        }
        
        try {
            FeitianReflectionHelper.invokeMethod(
                emv, "importAppSelect",
                new Class<?>[] { int.class },
                new Object[] { index }
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to select application", e);
        }
    }
    
    @Override
    public void confirmCardInfo(boolean confirm) {
        if (!initialized || emv == null) {
            Log.e(TAG, "EMV processor not initialized");
            return;
        }
        
        try {
            FeitianReflectionHelper.invokeMethod(
                emv, "importCardConfirmResult",
                new Class<?>[] { boolean.class },
                new Object[] { confirm }
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to confirm card info", e);
        }
    }
    
    @Override
    public void inputPin(String pin) {
        if (!initialized || emv == null) {
            Log.e(TAG, "EMV processor not initialized");
            return;
        }
        
        try {
            byte[] pinData = pin != null ? pin.getBytes() : null;
            FeitianReflectionHelper.invokeMethod(
                emv, "importPinResult",
                new Class<?>[] { byte[].class },
                new Object[] { pinData }
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to input PIN", e);
        }
    }
    
    @Override
    public void cancelPin() {
        inputPin(null);
    }
    
    @Override
    public void importOnlineResult(boolean approved, Map<String, String> onlineData) {
        if (!initialized || emv == null) {
            Log.e(TAG, "EMV processor not initialized");
            return;
        }
        
        try {
            // Default response code and auth code
            byte[] respCode = new byte[]{0x30, 0x30}; // "00"
            byte[] authCode = new byte[0];
            
            if (onlineData != null) {
                String respCodeStr = onlineData.get("responseCode");
                if (respCodeStr != null && respCodeStr.length() >= 2) {
                    respCode = respCodeStr.substring(0, 2).getBytes();
                }
                
                String authCodeStr = onlineData.get("authCode");
                if (authCodeStr != null) {
                    authCode = authCodeStr.getBytes();
                }
            }
            
            FeitianReflectionHelper.invokeMethod(
                emv, "importOnlineResult",
                new Class<?>[] { boolean.class, byte[].class, byte[].class },
                new Object[] { approved, respCode, authCode }
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to import online result", e);
        }
    }
    
    @Override
    public String getTlvData(String tag) {
        if (!initialized || emv == null) {
            Log.e(TAG, "EMV processor not initialized");
            return null;
        }
        
        try {
            Object result = FeitianReflectionHelper.invokeMethod(
                emv, "getTlv",
                new Class<?>[] { String.class },
                new Object[] { tag }
            );
            
            if (result instanceof byte[]) {
                byte[] data = (byte[]) result;
                if (data != null && data.length > 0) {
                    return bytesToHex(data);
                }
            }
            
            return null;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to get TLV data for tag: " + tag, e);
            return null;
        }
    }
    
    @Override
    public void cancelTransaction() {
        if (!initialized || emv == null) {
            Log.e(TAG, "EMV processor not initialized");
            return;
        }
        
        try {
            FeitianReflectionHelper.invokeMethod(
                emv, "emvCancel",
                new Class<?>[0],
                new Object[0]
            );
        } catch (Exception e) {
            Log.e(TAG, "Failed to cancel transaction", e);
        }
    }
    
    @Override
    public void release() {
        initialized = false;
        emv = null;
        currentListener = null;
        Log.d(TAG, "EMV processor released");
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X", b));
        }
        return result.toString();
    }
}