package id.uniflo.uniedc.database.entity;

// Temporarily disabled Room imports
// import androidx.room.Entity;
// import androidx.room.PrimaryKey;

// @Entity(tableName = "transactions")
public class Transaction {
    
    // @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String type; // WITHDRAWAL, TRANSFER, PURCHASE, BALANCE_INQUIRY
    public double amount;
    public String currency;
    public String referenceNumber;
    public String accountNumber;
    public String destinationAccount;
    public String description;
    public String status; // SUCCESS, FAILED, PENDING
    public String createdAt;
    public String cardMaskedPan;
    
    public Transaction() {
        // Default constructor
    }
}