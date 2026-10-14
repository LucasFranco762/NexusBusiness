package com.example.nexusbusiness

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.nexusbusiness.data.model.Product
import com.example.nexusbusiness.ui.components.NexusHeader
import com.example.nexusbusiness.ui.screens.*
import com.example.nexusbusiness.ui.theme.*
import com.example.nexusbusiness.viewmodel.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NexusBusinessTheme {
                val viewModel: MainViewModel = viewModel()
                val isDataReady by viewModel.isDataReady.collectAsState()
                if (isDataReady) {
                    NexusBusinessApp(viewModel)
                } else {
                    NexusBusinessSplash()
                }
            }
        }
    }
}

@Composable
private fun NexusBusinessSplash() {
    Image(
        painter = painterResource(R.drawable.splash),
        contentDescription = "Inicialização do NexusBusiness",
        contentScale = ContentScale.FillBounds,
        modifier = Modifier.fillMaxSize()
    )
}

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Products : Screen("products", "Produtos", Icons.Default.Inventory2)
    object Sales : Screen("sales", "Vendas", Icons.Default.MonetizationOn)
    object History : Screen("history", "Histórico", Icons.Default.History)
    object Route : Screen("route", "Rota", Icons.Default.Map)
    
    // Sub-screens
    object ProductRegistration : Screen("registration", "Cadastro", Icons.Default.Add)
    object Inventory : Screen("inventory", "Estoque", Icons.Default.Layers)
    object MakeSale : Screen("make_sale", "Realizar Venda", Icons.Default.AddShoppingCart)
    object CartReview : Screen("cart_review", "Carrinho", Icons.Default.ShoppingCart)
    object SalesHistory : Screen("sales_history", "Histórico de Vendas", Icons.Default.History)
    object MakeOrder : Screen("make_order", "Pedidos", Icons.AutoMirrored.Filled.List)
    object Orders : Screen("orders", "Lista de Pedidos", Icons.Default.ViewList)
    object CashFlow : Screen("cash_flow", "Fluxo de Caixa", Icons.Default.AccountBalanceWallet)
    
    // Container for Pager
    object MainPager : Screen("main_pager", "Main", Icons.Default.Home)
}

@Composable
fun NexusBusinessApp(viewModel: MainViewModel = viewModel()) {
    val navController = rememberNavController()
    val isOnline by viewModel.isOnline.collectAsState()
    val routes by viewModel.routes.collectAsState()
    val activeRoute = routes.find { it.ativa }
    
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var forceShowNav by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val isMainPagerActive = currentRoute == Screen.MainPager.route || currentRoute == null
    
    // PagerState para as 4 telas primárias
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()

    // Efeito Slide para o BottomNav
    val shouldShowNav = isMainPagerActive || forceShowNav

    // Lógica de timeout de 5 segundos para o menu expandido manualmente
    LaunchedEffect(forceShowNav, currentRoute) {
        if (forceShowNav && !isMainPagerActive) {
            delay(5000)
            forceShowNav = false
        }
    }

    Scaffold(
        topBar = {
            NexusHeader(isOnline, activeRoute?.cidade)
        },
        bottomBar = {
                Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .navigationBarsPadding(),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Seta de expansão centralizada
                if (!isMainPagerActive && !forceShowNav) {
                    ArrowExpandTrigger(onExpand = { forceShowNav = true })
                }

                AnimatedVisibility(
                    visible = shouldShowNav,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it })
                ) {
                    NexusBottomNavigation(
                        currentSelectedIndex = pagerState.currentPage,
                        onItemSelected = { index ->
                            // Reset do timer ao interagir
                            forceShowNav = false 
                            scope.launch {
                                if (!isMainPagerActive) {
                                    navController.navigate(Screen.MainPager.route) {
                                        popUpTo(Screen.MainPager.route) { inclusive = true }
                                    }
                                }
                                pagerState.animateScrollToPage(index)
                            }
                        }
                    )
                }
            }
        },
        containerColor = DarkBackground
    ) { innerPadding ->
        
        NavHost(
            navController = navController,
            startDestination = Screen.MainPager.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.MainPager.route) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                    beyondViewportPageCount = 0 // OTIMIZAÇÃO: Carrega apenas a tela visível
                ) { page ->
                    when (page) {
                        0 -> ProductsScreen(
                            onNavigateToCadastro = {
                                editingProduct = null
                                navController.navigate(Screen.ProductRegistration.route)
                            },
                            onNavigateToEstoque = {
                                navController.navigate(Screen.Inventory.route)
                            }
                        )
                        1 -> SalesScreen(
                            onNavigateToRealizarVenda = { 
                                navController.navigate(Screen.MakeSale.route)
                            },
                            onNavigateToPedidos = {
                                navController.navigate(Screen.MakeOrder.route)
                            },
                            onNavigateToListaPedidos = {
                                navController.navigate(Screen.Orders.route)
                            }
                        )
                        2 -> HistoryScreen(
                            onNavigateToFluxoCaixa = { 
                                navController.navigate(Screen.CashFlow.route)
                            },
                            onNavigateToHistoricoVendas = {
                                navController.navigate(Screen.SalesHistory.route)
                            }
                        )
                        3 -> RoutesScreen(viewModel = viewModel)
                    }
                }
            }
            
            composable(Screen.ProductRegistration.route) {
                ProductRegistrationScreen(
                    viewModel = viewModel,
                    productToEdit = editingProduct,
                    onBack = { 
                        forceShowNav = false
                        navController.popBackStack() 
                    }
                )
            }
            
            composable(Screen.Inventory.route) {
                InventoryScreen(
                    viewModel = viewModel,
                    onBack = { 
                        forceShowNav = false
                        navController.popBackStack() 
                    },
                    onEdit = { product ->
                        editingProduct = product
                        navController.navigate(Screen.ProductRegistration.route)
                    }
                )
            }

            composable(Screen.MakeSale.route) {
                MakeSaleScreen(
                    viewModel = viewModel,
                    onBack = {
                        forceShowNav = false
                        navController.popBackStack()
                    },
                    onOpenCartReview = {
                        navController.navigate(Screen.CartReview.route)
                    }
                )
            }

            composable(Screen.CartReview.route) {
                CartReviewScreen(
                    viewModel = viewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.SalesHistory.route) {
                SalesHistoryScreen(
                    viewModel = viewModel,
                    onBack = {
                        forceShowNav = false
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.MakeOrder.route) {
                MakeOrderScreen(
                    viewModel = viewModel,
                    onBack = {
                        forceShowNav = false
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Orders.route) {
                OrdersScreen(
                    viewModel = viewModel,
                    onBack = {
                        forceShowNav = false
                        navController.popBackStack()
                    },
                    onNavigateToVenda = {
                        navController.navigate(Screen.MakeSale.route)
                    }
                )
            }

            composable(Screen.CashFlow.route) {
                CashFlowScreen(
                    viewModel = viewModel,
                    onBack = {
                        forceShowNav = false
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun ArrowExpandTrigger(onExpand: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .clickable { onExpand() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.KeyboardDoubleArrowUp,
            contentDescription = "Expandir Menu",
            tint = PurplePrimary,
            modifier = Modifier
                .size(40.dp)
                .alpha(alpha)
        )
    }
}

@Composable
fun NexusBottomNavigation(
    currentSelectedIndex: Int,
    onItemSelected: (Int) -> Unit
) {
    val items = listOf(Screen.Products, Screen.Sales, Screen.History, Screen.Route)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .height(86.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEachIndexed { index, screen ->
            val selected = currentSelectedIndex == index
            val glassGradient = if (selected) {
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.18f),
                        Color(0xFF312E81).copy(alpha = 0.62f),
                        CyanSecondary.copy(alpha = 0.12f)
                    )
                )
            } else {
                Brush.verticalGradient(
                    listOf(
                        Color.White.copy(alpha = 0.10f),
                        CardBackground.copy(alpha = 0.62f),
                        PurplePrimary.copy(alpha = 0.08f)
                    )
                )
            }
            val glassBorder = if (selected) {
                BorderStroke(2.dp, Color(0xFF1E3A8A))
            } else {
                BorderStroke(1.dp, Color.White.copy(alpha = 0.14f))
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onItemSelected(index) },
                shape = RoundedCornerShape(16.dp),
                color = Color.Transparent,
                border = glassBorder,
                tonalElevation = 0.dp,
                shadowElevation = if (selected) 8.dp else 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(glassGradient)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 6.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            screen.icon,
                            contentDescription = screen.label,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = screen.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = name, color = TextPrimary, fontSize = 24.sp)
    }
}
