package id.uniflo.uniedc.database.dao;

// Temporarily disabled Room imports
// import androidx.room.Dao;
// import androidx.room.Insert;
// import androidx.room.Query;
// import androidx.room.Update;

import id.uniflo.uniedc.database.entity.Card;

// @Dao
public interface CardDao {
    
    // @Insert
    long insertCard(Card card);
    
    // @Update
    void updateCard(Card card);
    
    // @Query("SELECT * FROM cards WHERE isActive = 1 LIMIT 1")
    Card getActiveCard();
    
    // @Query("SELECT * FROM cards WHERE maskedPan = :maskedPan LIMIT 1")
    Card getCardByMaskedPan(String maskedPan);
    
    // @Query("UPDATE cards SET isActive = 0")
    void deactivateAllCards();
    
    // @Query("DELETE FROM cards")
    void deleteAllCards();
}