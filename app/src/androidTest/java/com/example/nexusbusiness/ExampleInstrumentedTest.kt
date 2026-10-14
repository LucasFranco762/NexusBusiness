package com.example.nexusbusiness

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.nexusbusiness.data.local.AppDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.nexusbusiness", appContext.packageName)
    }

    @Test
    fun migrationFrom5To6PreservesProductAndAddsPhotoPath() {
        runBlocking {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val databaseName = "migration_5_6_test.db"
            context.deleteDatabase(databaseName)

            val legacyDatabase = SQLiteDatabase.openOrCreateDatabase(
                context.getDatabasePath(databaseName),
                null
            )
            legacyDatabase.execSQL(
            "CREATE TABLE products (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, nome TEXT NOT NULL, qtd INTEGER NOT NULL, custo TEXT NOT NULL, margem REAL NOT NULL, precoVenda TEXT NOT NULL, rateio TEXT NOT NULL, updatedAt INTEGER NOT NULL)"
        )
            legacyDatabase.execSQL(
            "CREATE TABLE routes (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, cidade TEXT NOT NULL, custo TEXT NOT NULL, ativa INTEGER NOT NULL, updatedAt INTEGER NOT NULL)"
        )
            legacyDatabase.execSQL(
            "CREATE TABLE sales (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, customerName TEXT NOT NULL, productsSummary TEXT NOT NULL, paymentMethod TEXT NOT NULL, totalValue TEXT NOT NULL, totalProfit REAL NOT NULL, timestamp INTEGER NOT NULL)"
        )
            legacyDatabase.execSQL(
            "CREATE TABLE orders (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, customerName TEXT NOT NULL, productsSummary TEXT NOT NULL, processedItems TEXT NOT NULL, totalValue TEXT NOT NULL, status TEXT NOT NULL, timestamp INTEGER NOT NULL)"
        )
            legacyDatabase.execSQL(
            "INSERT INTO products (id, nome, qtd, custo, margem, precoVenda, rateio, updatedAt) VALUES (1, 'Produto legado', 3, 'R$ 10,00', 20.0, 'R$ 12,00', 'R$ 0,00', 1)"
        )
            legacyDatabase.version = 5
            legacyDatabase.close()

            val migratedDatabase = Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                databaseName
            ).addMigrations(AppDatabase.MIGRATION_5_6).build()

            val migratedProduct = migratedDatabase.productDao().getProductById(1)
            assertNotNull(migratedProduct)
            assertEquals("Produto legado", migratedProduct?.nome)
            assertEquals("", migratedProduct?.photoPath)

            migratedDatabase.close()
            context.deleteDatabase(databaseName)
        }
    }
}
