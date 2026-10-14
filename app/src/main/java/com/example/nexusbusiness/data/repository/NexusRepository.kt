package com.example.nexusbusiness.data.repository

import com.example.nexusbusiness.data.local.ProductDao
import com.example.nexusbusiness.data.local.OrderDao
import com.example.nexusbusiness.data.local.RouteDao
import com.example.nexusbusiness.data.local.SaleDao
import com.example.nexusbusiness.data.model.Product
import com.example.nexusbusiness.data.model.Route
import com.example.nexusbusiness.data.model.Sale
import com.example.nexusbusiness.data.model.Order
import kotlinx.coroutines.flow.Flow

class NexusRepository(
    private val productDao: ProductDao,
    private val routeDao: RouteDao,
    private val saleDao: SaleDao,
    private val orderDao: OrderDao
) {
    // Produtos
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()

    suspend fun saveProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun deleteProduct(product: Product) {
        productDao.deleteProduct(product)
    }

    suspend fun getProductPhotoPaths(): List<String> = productDao.getProductPhotoPaths()

    suspend fun isPhotoReferenced(photoPath: String): Boolean =
        productDao.countProductsUsingPhoto(photoPath) > 0

    // Rotas
    val allRoutes: Flow<List<Route>> = routeDao.getAllRoutes()

    suspend fun saveRoute(route: Route) {
        routeDao.insertRoute(route)
    }

    suspend fun deleteRoute(route: Route) {
        routeDao.deleteRoute(route)
    }

    suspend fun toggleRouteActive(routeId: Long) {
        routeDao.toggleRouteActive(routeId)
    }

    // Vendas
    val allSales: Flow<List<Sale>> = saleDao.getAllSales()

    suspend fun saveSale(sale: Sale) {
        saleDao.insertSale(sale)
    }

    suspend fun deleteAllSales() {
        saleDao.deleteAllSales()
    }

    // Pedidos
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()

    suspend fun saveOrder(order: Order) {
        orderDao.insertOrder(order)
    }

    suspend fun deleteOrder(order: Order) {
        orderDao.deleteOrder(order)
    }

    suspend fun updateOrder(order: Order) {
        orderDao.updateOrder(order)
    }
}
