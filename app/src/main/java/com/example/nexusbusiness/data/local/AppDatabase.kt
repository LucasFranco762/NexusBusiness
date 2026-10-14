package com.example.nexusbusiness.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.nexusbusiness.data.model.Product
import com.example.nexusbusiness.data.model.Route
import com.example.nexusbusiness.data.model.Sale
import com.example.nexusbusiness.data.model.Order

@Database(entities = [Product::class, Route::class, Sale::class, Order::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun routeDao(): RouteDao
    abstract fun saleDao(): SaleDao
    abstract fun orderDao(): OrderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nexus_business_database"
                )
                .addMigrations(MIGRATION_5_6)
                .build()
                INSTANCE = instance
                instance
            }
        }

        internal val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE products ADD COLUMN photoPath TEXT NOT NULL DEFAULT ''"
                )
            }
        }
    }
}
