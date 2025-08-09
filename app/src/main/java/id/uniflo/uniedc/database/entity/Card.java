package id.uniflo.uniedc.database.entity;

// Temporarily disabled Room imports
// import androidx.room.Entity;
// import androidx.room.PrimaryKey;

// @Entity(tableName = "cards")
public class Card {
    
    // @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String maskedPan;
    public String encryptedPin;
    public boolean isActive;
    public String createdAt;
    public String updatedAt;
    
    public Card() {
        // Default constructor
    }
}