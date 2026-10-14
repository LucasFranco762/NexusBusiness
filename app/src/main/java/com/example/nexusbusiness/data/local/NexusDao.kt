package com.example.nexusbusiness.data.local

import androidx.room.*
import com.example.nexusbusiness.data.model.Product
import com.example.nexusbusiness.data.model.Route
import com.example.nexusbusiness.data.model.Sale
import com.example.nexusbusiness.data.model.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY nome ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT photoPath FROM products WHERE photoPath != ''")
    suspend fun getProductPhotoPaths(): List<String>

    @Query("SELECT COUNT(*) FROM products WHERE photoPath = :photoPath")
    suspend fun countProductsUsingPhoto(photoPath: String): Int
}

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes ORDER BY cidade ASC")
    fun getAllRoutes(): Flow<List<Route>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: Route)

    @Delete
    suspend fun deleteRoute(route: Route)

    @Query("UPDATE routes SET ativa = 0")
    suspend fun deactivateAllRoutes()

    @Transaction
    suspend fun toggleRouteActive(routeId: Long) {
        val route = getRouteById(routeId) ?: return
        if (route.ativa) {
            insertRoute(route.copy(ativa = false))
        } else {
            deactivateAllRoutes()
            insertRoute(route.copy(ativa = true))
        }
    }

    @Query("SELECT * FROM routes WHERE id = :id")
    suspend fun getRouteById(id: Long): Route?
}

@Dao
interface SaleDao {
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale)

    @Query("DELETE FROM sales")
    suspend fun deleteAllSales()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)

    @Update
    suspend fun updateOrder(order: Order)
}
