package com.example.nexusbusiness.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nexusbusiness.data.local.AppDatabase
import com.example.nexusbusiness.data.model.Product
import com.example.nexusbusiness.data.model.Route
import com.example.nexusbusiness.data.model.Sale
import com.example.nexusbusiness.data.model.Order
import com.example.nexusbusiness.data.model.SaleCartItem
import com.example.nexusbusiness.data.repository.NexusRepository
import com.example.nexusbusiness.util.CurrencyUtils
import com.example.nexusbusiness.util.ProductImageStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NexusRepository
    
    val products: StateFlow<List<Product>>
    val routes: StateFlow<List<Route>>
    val sales: StateFlow<List<Sale>>
    val orders: StateFlow<List<Order>>

    private val _isDataReady = MutableStateFlow(false)
    val isDataReady: StateFlow<Boolean> = _isDataReady.asStateFlow()

    private val _isStockLocked = MutableStateFlow(false)
    val isStockLocked: StateFlow<Boolean> = _isStockLocked.asStateFlow()

    private val _routeInvestment = MutableStateFlow(0.0)
    val routeInvestment: StateFlow<Double> = _routeInvestment.asStateFlow()

    private val _pendingOrderForSale = MutableStateFlow<Order?>(null)
    val pendingOrderForSale: StateFlow<Order?> = _pendingOrderForSale.asStateFlow()

    private val _saleDraftCustomerName = MutableStateFlow("")
    val saleDraftCustomerName: StateFlow<String> = _saleDraftCustomerName.asStateFlow()

    private val _saleDraftPaymentMethod = MutableStateFlow("")
    val saleDraftPaymentMethod: StateFlow<String> = _saleDraftPaymentMethod.asStateFlow()

    private val _saleDraftItems = MutableStateFlow<List<SaleCartItem>>(emptyList())
    val saleDraftItems: StateFlow<List<SaleCartItem>> = _saleDraftItems.asStateFlow()

    private val _saleDraftOrderToUpdate = MutableStateFlow<Order?>(null)

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var lockedProductsCache: List<Product>? = null

    init {
        val database = AppDatabase.getDatabase(application)
        repository = NexusRepository(
            database.productDao(), 
            database.routeDao(), 
            database.saleDao(),
            database.orderDao()
        )
        
        val baseProductsFlow = repository.allProducts.shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            replay = 1
        )
        val baseRoutesFlow = repository.allRoutes.shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            replay = 1
        )
        val baseSalesFlow = repository.allSales.shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            replay = 1
        )
        val baseOrdersFlow = repository.allOrders.shareIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            replay = 1
        )

        products = combine(baseProductsFlow, baseRoutesFlow, isStockLocked) { rawProducts, rawRoutes, locked ->
            if (locked) {
                lockedProductsCache?.let { cachedList ->
                    rawProducts.map { currentDbProduct ->
                        val cached = cachedList.find { it.id == currentDbProduct.id }
                        if (cached != null) {
                            currentDbProduct.copy(
                                rateio = cached.rateio,
                                precoVenda = cached.precoVenda
                            )
                        } else {
                            currentDbProduct
                        }
                    }
                } ?: calculateDistributedValues(rawProducts, rawRoutes).also { lockedProductsCache = it }
            } else {
                lockedProductsCache = null
                calculateDistributedValues(rawProducts, rawRoutes)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )
        
        routes = baseRoutesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

        sales = baseSalesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

        orders = baseOrdersFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

        viewModelScope.launch {
            combine(
                baseProductsFlow,
                baseRoutesFlow,
                baseSalesFlow,
                baseOrdersFlow
            ) { _, _, _, _ -> Unit }.first()
            val referencedPhotos = repository.getProductPhotoPaths().toSet()
            withContext(Dispatchers.IO) {
                ProductImageStorage.cleanupOrphans(application, referencedPhotos)
            }
            _isDataReady.value = true
        }
    }

    private fun calculateDistributedValues(rawProducts: List<Product>, rawRoutes: List<Route>): List<Product> {
        val activeRoute = rawRoutes.find { it.ativa }
        val inventario = rawProducts.sumOf {
            CurrencyUtils.parse(it.custo) * it.qtd
        }
        
        val routeCost = activeRoute?.let { CurrencyUtils.parse(it.custo) } ?: 0.0

        return rawProducts.map { product ->
            val productTotalCost = CurrencyUtils.parse(product.custo) * product.qtd
            val pesoPercentual = if (inventario > 0) productTotalCost / inventario else 0.0
            val totalRateioProduto = routeCost * pesoPercentual
            val rateioPerUnit = if (product.qtd > 0) totalRateioProduto / product.qtd else 0.0

            val costVal = CurrencyUtils.parse(product.custo)
            val marginVal = product.margem / 100.0
            val finalPrice = costVal + (costVal * marginVal) + rateioPerUnit
            
            product.copy(
                rateio = CurrencyUtils.format(rateioPerUnit),
                precoVenda = CurrencyUtils.format(finalPrice)
            )
        }
    }

    fun resetCashFlow() {
        viewModelScope.launch {
            // 1. Limpa as vendas do banco de dados
            repository.deleteAllSales()
            
            // 2. Reseta o valor de investimento
            _routeInvestment.value = 0.0
            
            // 3. Destrava o estoque para permitir novas configurações
            _isStockLocked.value = false
            
            // 4. Limpa o cache de produtos para forçar novo cálculo no futuro
            lockedProductsCache = null
        }
    }

    fun setPendingOrderForSale(order: Order?) {
        _pendingOrderForSale.value = order
    }

    fun setSaleDraftCustomerName(name: String) {
        _saleDraftCustomerName.value = name
    }

    fun setSaleDraftPaymentMethod(method: String) {
        _saleDraftPaymentMethod.value = method
    }

    fun setSaleDraftOrderToUpdate(order: Order?) {
        _saleDraftOrderToUpdate.value = order
    }

    fun addSaleDraftItem(product: Product, quantity: Int) {
        if (quantity <= 0) return

        _saleDraftItems.value = listOf(
            SaleCartItem(product = product, quantity = quantity)
        ) + _saleDraftItems.value

        saveProduct(product.copy(qtd = product.qtd - quantity))
    }

    fun removeSaleDraftItem(itemId: Long) {
        val item = _saleDraftItems.value.find { it.id == itemId } ?: return

        saveProduct(item.product.copy(qtd = item.product.qtd + item.quantity))
        _saleDraftItems.value = _saleDraftItems.value.filterNot { it.id == itemId }
    }

    fun clearSaleDraft() {
        _saleDraftItems.value = emptyList()
        _saleDraftCustomerName.value = ""
        _saleDraftPaymentMethod.value = ""
        _saleDraftOrderToUpdate.value = null
    }

    fun loadSaleDraftFromOrder(order: Order) {
        clearSaleDraft()
        _saleDraftCustomerName.value = order.customerName
        _saleDraftOrderToUpdate.value = order

        order.productsSummary.split("\n").forEach { line ->
            if (line.isNotBlank()) {
                val regex = """^(.*)\s\((\d+)\)$""".toRegex()
                val match = regex.find(line)
                val name = match?.groups?.get(1)?.value ?: line
                val qty = match?.groups?.get(2)?.value?.toIntOrNull() ?: 0

                val stockProduct = products.value.find { it.nome.equals(name, ignoreCase = true) }
                if (stockProduct != null) {
                    addSaleDraftItem(stockProduct, qty)
                }
            }
        }
    }

    fun finalizeSaleDraft() {
        val items = _saleDraftItems.value
        val customerName = _saleDraftCustomerName.value
        val paymentMethod = _saleDraftPaymentMethod.value

        if (items.isEmpty() || customerName.isBlank() || paymentMethod.isBlank()) return

        val totalValue = items.sumOf { item ->
            CurrencyUtils.parse(item.product.precoVenda) * item.quantity
        }
        val totalProfit = items.sumOf { item ->
            (CurrencyUtils.parse(item.product.precoVenda) - CurrencyUtils.parse(item.product.rateio) - CurrencyUtils.parse(item.product.custo)) * item.quantity
        }
        val productsSummary = items.joinToString(", ") { "${it.product.nome} (${it.quantity})" }
        val orderToUpdate = _saleDraftOrderToUpdate.value
        val sale = Sale(
            customerName = customerName,
            productsSummary = productsSummary,
            paymentMethod = paymentMethod,
            totalValue = CurrencyUtils.format(totalValue),
            totalProfit = totalProfit
        )

        viewModelScope.launch {
            repository.saveSale(sale)
            orderToUpdate?.let { order ->
                repository.saveOrder(order.copy(status = "VENDA REALIZADA"))
            }
        }
        clearSaleDraft()
    }

    fun toggleStockLock() {
        val newValue = !_isStockLocked.value
        _isStockLocked.value = newValue
        
        if (newValue) {
            val currentProducts = products.value
            val activeRoute = routes.value.find { it.ativa }
            
            val totalProductCost = currentProducts.sumOf { 
                CurrencyUtils.parse(it.custo) * it.qtd 
            }
            val operationalCost = activeRoute?.let { CurrencyUtils.parse(it.custo) } ?: 0.0
            
            _routeInvestment.value = totalProductCost + operationalCost
        } else {
            // Se destravar manualmente, zera o investimento acumulado
            _routeInvestment.value = 0.0
            lockedProductsCache = null
        }
    }

    fun saveProduct(
        product: Product,
        previousPhotoPath: String? = null,
        onSaved: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.saveProduct(product)
            if (!previousPhotoPath.isNullOrBlank() && previousPhotoPath != product.photoPath) {
                deletePhotoIfUnreferenced(previousPhotoPath)
            }
            onSaved()
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            deletePhotoIfUnreferenced(product.photoPath)
        }
    }

    private suspend fun deletePhotoIfUnreferenced(photoPath: String) {
        if (photoPath.isBlank() || repository.isPhotoReferenced(photoPath)) return
        withContext(Dispatchers.IO) {
            ProductImageStorage.deleteManagedPhoto(getApplication(), photoPath)
        }
    }

    fun saveRoute(route: Route) {
        viewModelScope.launch {
            repository.saveRoute(route)
        }
    }

    fun deleteRoute(route: Route) {
        viewModelScope.launch {
            repository.deleteRoute(route)
        }
    }

    fun toggleRouteActive(routeId: Long) {
        viewModelScope.launch {
            repository.toggleRouteActive(routeId)
        }
    }

    fun saveSale(sale: Sale) {
        viewModelScope.launch {
            repository.saveSale(sale)
        }
    }

    fun saveOrder(order: Order) {
        viewModelScope.launch {
            repository.saveOrder(order)
        }
    }

    fun deleteOrder(order: Order) {
        viewModelScope.launch {
            repository.deleteOrder(order)
        }
    }

    fun updateOrder(order: Order) {
        viewModelScope.launch {
            repository.updateOrder(order)
        }
    }

    fun clearOrdersHistory() {
        viewModelScope.launch {
            orders.value.filter { it.status == "VENDA REALIZADA" }.forEach { order ->
                repository.deleteOrder(order)
            }
        }
    }
}
