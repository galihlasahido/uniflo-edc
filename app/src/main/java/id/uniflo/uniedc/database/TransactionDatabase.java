package id.uniflo.uniedc.database;

import android.content.Context;
// Temporarily disabled Room imports
// import androidx.room.Database;
// import androidx.room.Room;
// import androidx.room.RoomDatabase;

import id.uniflo.uniedc.database.dao.CardDao;
import id.uniflo.uniedc.database.dao.TransactionDao;
import id.uniflo.uniedc.database.entity.Card;
import id.uniflo.uniedc.database.entity.Transaction;

// @Database(entities = {Transaction.class, Card.class}, version = 1, exportSchema = false)
public abstract class TransactionDatabase { // extends RoomDatabase {
    
    private static TransactionDatabase instance;
    
    public abstract TransactionDao transactionDao();
    public abstract CardDao cardDao();
    
    public static synchronized TransactionDatabase getInstance(Context context) {
        // Temporarily return null - Room disabled
        return null;
        /*
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    TransactionDatabase.class, "transaction_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries() // Not recommended for production
                    .build();
        }
        return instance;
        */
    }
}