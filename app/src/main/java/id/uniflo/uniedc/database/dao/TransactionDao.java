package id.uniflo.uniedc.database.dao;

// Temporarily disabled Room imports
// import androidx.room.Dao;
// import androidx.room.Insert;
// import androidx.room.Query;
import java.util.List;

import id.uniflo.uniedc.database.entity.Transaction;

// @Dao
public interface TransactionDao {
    
    // @Insert
    long insertTransaction(Transaction transaction);
    
    // @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    List<Transaction> getAllTransactions();
    
    // @Query("SELECT * FROM transactions WHERE type = :type ORDER BY createdAt DESC")
    List<Transaction> getTransactionsByType(String type);
    
    // @Query("SELECT * FROM transactions WHERE id = :id")
    Transaction getTransactionById(long id);
    
    // @Query("DELETE FROM transactions")
    void deleteAllTransactions();
}