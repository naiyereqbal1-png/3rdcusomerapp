package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TRYatHOMETheme {
                MainAppContainer()
            }
        }
    }
}

@Composable
fun MainAppContainer() {
    var authSession by remember { mutableStateOf<AuthSession?>(Database.currentSession) }
    var currentView by remember { mutableStateOf("CUSTOMER") } // "CUSTOMER", "ADMIN", "DELIVERY", "SHOPKEEPER"

    // Listen to changes in auth state
    LaunchedEffect(authSession) {
        if (authSession == null) {
            currentView = "CUSTOMER"
        } else {
            currentView = when (authSession?.role) {
                "ADMIN" -> "ADMIN"
                "SHOPKEEPER" -> "SHOPKEEPER"
                "DELIVERY_BOY" -> "DELIVERY"
                else -> "CUSTOMER"
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val session = authSession
            if (session == null) {
                TryAtHomeLoginScreen(
                    onLoginSuccess = { s ->
                        authSession = s
                    }
                )
            } else {
                when (currentView) {
                    "CUSTOMER" -> CustomerPortal(
                        session = session,
                        onLogout = {
                            Database.logout()
                            authSession = null
                        },
                        onSwitchView = { currentView = it }
                    )
                    "ADMIN" -> AdminPortal(
                        session = session,
                        onLogout = {
                            Database.logout()
                            authSession = null
                        },
                        onSwitchView = { currentView = it }
                    )
                    "DELIVERY" -> DeliveryBoyPortal(
                        session = session,
                        onLogout = {
                            Database.logout()
                            authSession = null
                        }
                    )
                    "SHOPKEEPER" -> ShopkeeperPortal(
                        session = session,
                        onLogout = {
                            Database.logout()
                            authSession = null
                        },
                        onSwitchView = { currentView = it }
                    )
                }
            }
        }
    }
}

// ==================== AUTHENTICATION SCREEN ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TryAtHomeLoginScreen(onLoginSuccess: (AuthSession) -> Unit) {
    var mobile by remember { mutableStateOf("") }
    var showOtpStep by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var detectedRole by remember { mutableStateOf<String?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    
    // New registration states
    var isRegistering by remember { mutableStateOf(false) }
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFFFE4E6), Color.White)))
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Identity Header
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "Logo",
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "TRYatHOME",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Online Garment Store • Doorstep trials",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (showOtpStep) "Verify Verification Code" else if (isRegistering) "Register as Customer" else "Sign In / Register",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (showOtpStep) "Enter 6-digit OTP sent to +91 $mobile" else "Enter your Indian mobile number to proceed",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                if (!showOtpStep) {
                    if (isRegistering) {
                        OutlinedTextField(
                            value = regName,
                            onValueChange = { regName = it },
                            label = { Text("Your Full Name") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Person, null) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = regEmail,
                            onValueChange = { regEmail = it },
                            label = { Text("Email Address (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Email, null) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it.replace(Regex("\\D"), "").take(10) },
                        label = { Text("Mobile Number") },
                        placeholder = { Text("10-digit number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        leadingIcon = { Text("  +91 ", fontWeight = FontWeight.Bold, color = Color.Gray) }
                    )

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMsg ?: "", color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (mobile.length != 10) {
                                errorMsg = "Please enter a valid 10-digit mobile number."
                                return@Button
                            }
                            errorMsg = null
                            val check = Database.checkMobileRole(mobile)
                            detectedRole = check.first
                            if (detectedRole == null && !isRegistering) {
                                // Direct to registration
                                isRegistering = true
                            } else {
                                showOtpStep = true
                                otpCode = "123456" // Prefilled mock OTP
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isRegistering) "Register & Send OTP" else "Request Demo OTP", fontWeight = FontWeight.Bold)
                    }

                    if (isRegistering) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { isRegistering = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Already have an account? Sign In", fontSize = 12.sp)
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it.replace(Regex("\\D"), "").take(6) },
                        label = { Text("6-Digit Verification Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.Lock, null) }
                    )

                    if (errorMsg != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = errorMsg ?: "", color = Color.Red, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (otpCode.length != 6) {
                                errorMsg = "Please enter the complete 6-digit verification code."
                                return@Button
                            }
                            val role = detectedRole ?: "CUSTOMER"
                            val session = Database.login(mobile, role, name = regName, email = regEmail)
                            onLoginSuccess(session)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Verify & Secure Login", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showOtpStep = false },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Back to Mobile Number", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Demo Accounts helper section
        Text(
            text = "FAST TRACK DEMO ACCESS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DemoChip(role = "Admin", phone = "9999999999", modifier = Modifier.weight(1f)) {
                mobile = "9999999999"
                detectedRole = "ADMIN"
                showOtpStep = true
                otpCode = "123456"
            }
            DemoChip(role = "Shopkeeper", phone = "9810101010", modifier = Modifier.weight(1f)) {
                mobile = "9810101010"
                detectedRole = "SHOPKEEPER"
                showOtpStep = true
                otpCode = "123456"
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DemoChip(role = "Delivery", phone = "9876543201", modifier = Modifier.weight(1f)) {
                mobile = "9876543201"
                detectedRole = "DELIVERY_BOY"
                showOtpStep = true
                otpCode = "123456"
            }
            DemoChip(role = "Customer", phone = "9876543210", modifier = Modifier.weight(1f)) {
                mobile = "9876543210"
                detectedRole = "CUSTOMER"
                showOtpStep = true
                otpCode = "123456"
            }
        }
    }
}

@Composable
fun DemoChip(role: String, phone: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(0.5.dp, Color.LightGray)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = role, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(text = phone, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

// ==================== CUSTOMER PORTAL ====================

@Composable
fun CustomerPortal(
    session: AuthSession,
    onLogout: () -> Unit,
    onSwitchView: (String) -> Unit
) {
    var activeTab by remember { mutableStateOf("HOME") } // "HOME", "WISHLIST", "CART", "PROFILE"
    var activeCategorySlug by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var wishlist by remember { mutableStateOf(Database.getWishlist(session.mobile)) }
    var cart by remember { mutableStateOf(Database.getCart(session.mobile)) }

    // State for notification
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(notificationMessage) {
        if (notificationMessage != null) {
            delay(2500)
            notificationMessage = null
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.primary).padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("TRYatHOME", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                        Text("Doorstep Trial Garment Store", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (session.role == "ADMIN") {
                            IconButton(onClick = { onSwitchView("ADMIN") }) {
                                Icon(Icons.Default.Shield, "Admin Panel", tint = Color.White)
                            }
                        } else if (session.role == "SHOPKEEPER") {
                            IconButton(onClick = { onSwitchView("SHOPKEEPER") }) {
                                Icon(Icons.Default.Storefront, "Shopkeeper Panel", tint = Color.White)
                            }
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.LogOut, "Logout", tint = Color.White)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Search bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search elegant shirts, jeans, kurtis...", color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.15f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.White) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, null, tint = Color.White)
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "HOME",
                    onClick = { activeTab = "HOME" },
                    icon = { Icon(if (activeTab == "HOME") Icons.Filled.Home else Icons.Outlined.Home, "Home") },
                    label = { Text("Home", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "WISHLIST",
                    onClick = { activeTab = "WISHLIST" },
                    icon = { Icon(if (activeTab == "WISHLIST") Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, "Wishlist") },
                    label = { Text("Wishlist", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "CART",
                    onClick = { activeTab = "CART" },
                    icon = { 
                        BadgedBox(badge = {
                            if (cart.items.isNotEmpty()) {
                                Badge { Text(cart.items.sumOf { it.quantity }.toString()) }
                            }
                        }) {
                            Icon(if (activeTab == "CART") Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart, "Cart")
                        }
                    },
                    label = { Text("Cart", fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = activeTab == "PROFILE",
                    onClick = { activeTab = "PROFILE" },
                    icon = { Icon(if (activeTab == "PROFILE") Icons.Filled.Person else Icons.Outlined.Person, "Profile") },
                    label = { Text("Profile", fontSize = 10.sp) }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF8FAFC))
        ) {
            // Notification toast
            notificationMessage?.let { msg ->
                Card(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            when (activeTab) {
                "HOME" -> CustomerHomeTab(
                    activeCategorySlug = activeCategorySlug,
                    onSelectCategory = { activeCategorySlug = it },
                    searchQuery = searchQuery,
                    onSelectProduct = { selectedProduct = it },
                    wishlist = wishlist,
                    onToggleWishlist = { prod ->
                        Database.toggleWishlist(prod, session.mobile)
                        wishlist = Database.getWishlist(session.mobile)
                    },
                    session = session
                )
                "WISHLIST" -> CustomerWishlistTab(
                    wishlist = wishlist,
                    onRemove = { prod ->
                        Database.toggleWishlist(prod, session.mobile)
                        wishlist = Database.getWishlist(session.mobile)
                    },
                    onSelectProduct = { selectedProduct = it }
                )
                "CART" -> CustomerCartTab(
                    session = session,
                    cart = cart,
                    onUpdateQuantity = { itemId, q ->
                        Database.updateCartQuantity(itemId, q, session.mobile)
                        cart = Database.getCart(session.mobile)
                    },
                    onRemoveItem = { itemId ->
                        Database.removeFromCart(itemId, session.mobile)
                        cart = Database.getCart(session.mobile)
                    },
                    onOrderPlaced = { order ->
                        cart = Database.getCart(session.mobile)
                        notificationMessage = "Order Placed Successfully! (${order.order_id})"
                        activeTab = "PROFILE"
                    }
                )
                "PROFILE" -> CustomerProfileTab(
                    session = session,
                    onSwitchView = onSwitchView
                )
            }

            // Product Details Dialog
            selectedProduct?.let { prod ->
                ProductDetailsDialog(
                    product = prod,
                    onDismiss = { selectedProduct = null },
                    onAddToCart = { size, color ->
                        Database.addToCart(prod, size, color, 1, session.mobile)
                        cart = Database.getCart(session.mobile)
                        notificationMessage = "Added ${prod.name} (Size: $size, Color: $color) to Cart!"
                        selectedProduct = null
                    },
                    onBuyNow = { size, color ->
                        Database.addToCart(prod, size, color, 1, session.mobile)
                        cart = Database.getCart(session.mobile)
                        selectedProduct = null
                        activeTab = "CART"
                    }
                )
            }
        }
    }
}

@Composable
fun CustomerHomeTab(
    activeCategorySlug: String,
    onSelectCategory: (String) -> Unit,
    searchQuery: String,
    onSelectProduct: (Product) -> Unit,
    wishlist: List<Product>,
    onToggleWishlist: (Product) -> Unit,
    session: AuthSession
) {
    val scrollState = rememberScrollState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Active Try-at-Home orders header countdown
        val tryAtHomeActiveOrders = remember {
            derivedStateOf {
                Database.orders.filter { 
                    it.customer_id == session.mobile && 
                    it.order_type == "try_at_home" && 
                    it.order_status != "Delivered" && 
                    it.order_status != "Cancelled"
                }
            }
        }
        
        if (tryAtHomeActiveOrders.value.isNotEmpty()) {
            ActiveTryAtHomeTimerCard(order = tryAtHomeActiveOrders.value.first())
        }

        // Horizontal Category List
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CategoryCircle(
                    name = "All Garments",
                    imageUrl = "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=150",
                    isSelected = activeCategorySlug.isEmpty(),
                    onClick = { onSelectCategory("") }
                )
            }
            items(Database.categories) { cat ->
                CategoryCircle(
                    name = cat.name,
                    imageUrl = cat.image,
                    isSelected = activeCategorySlug == cat.slug,
                    onClick = { onSelectCategory(cat.slug) }
                )
            }
        }

        val filteredProducts = remember(activeCategorySlug, searchQuery, Database.products) {
            Database.products.filter { p ->
                val matchesCategory = if (activeCategorySlug.isEmpty()) true else {
                    val cat = Database.categories.find { it.slug == activeCategorySlug }
                    p.category_id == cat?.id
                }
                val matchesSearch = if (searchQuery.isEmpty()) true else {
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.brand.contains(searchQuery, ignoreCase = true) ||
                    p.description.contains(searchQuery, ignoreCase = true)
                }
                matchesCategory && matchesSearch && p.status == "Published"
            }
        }

        if (filteredProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("No published garments found", color = Color.Gray)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredProducts) { prod ->
                    ProductGridCard(
                        product = prod,
                        isWishlisted = wishlist.any { it.id == prod.id },
                        onToggleWishlist = { onToggleWishlist(prod) },
                        onClick = { onSelectProduct(prod) }
                    )
                }
            }
        }
    }
}

@Composable
fun ActiveTryAtHomeTimerCard(order: Order) {
    var ticksLeft by remember { mutableStateOf(1800) } // Mock 30 mins
    LaunchedEffect(Unit) {
        while (ticksLeft > 0) {
            delay(1000)
            ticksLeft--
        }
    }
    
    val minutes = ticksLeft / 60
    val seconds = ticksLeft % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        border = BorderStroke(1.dp, Color(0xFFFBBF24))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.AccessTime, "Timer", tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Try-at-Home Trial Active", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF92400E))
                    Text("Order ID: ${order.order_id}", fontSize = 11.sp, color = Color(0xFFB45309))
                }
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFFEF3C7))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$timeFormatted remaining",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    color = Color(0xFFB45309)
                )
            }
        }
    }
}

@Composable
fun CategoryCircle(name: String, imageUrl: String, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .width(72.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.White)
                .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ProductGridCard(
    product: Product,
    isWishlisted: Boolean,
    onToggleWishlist: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box {
            AsyncImage(
                model = if (product.images.isNotEmpty()) product.images[0].image_url else "",
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )
            
            // Discount tag
            if (product.discount_percentage > 0) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 0.dp, bottomEnd = 8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${product.discount_percentage}% OFF",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Wishlist icon
            IconButton(
                onClick = onToggleWishlist,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.8f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Wishlist",
                    tint = if (isWishlisted) Color.Red else Color.Gray,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Column(modifier = Modifier.padding(10.dp)) {
            Text(product.brand, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("₹${product.selling_price.toInt()}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(6.dp))
                Text("₹${product.mrp.toInt()}", fontSize = 11.sp, textDecoration = TextDecoration.LineThrough, color = Color.Gray)
            }
        }
    }
}

// ==================== PRODUCT DETAILS DIALOG ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsDialog(
    product: Product,
    onDismiss: () -> Unit,
    onAddToCart: (size: String, color: String) -> Unit,
    onBuyNow: (size: String, color: String) -> Unit
) {
    var selectedSize by remember { mutableStateOf(if (product.sizes.isNotEmpty()) product.sizes[0] else "M") }
    var selectedColor by remember { mutableStateOf(if (product.colors.isNotEmpty()) product.colors[0] else "Default") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = if (product.images.isNotEmpty()) product.images[0].image_url else "",
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(product.brand, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                Text(product.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹${product.selling_price.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("₹${product.mrp.toInt()}", fontSize = 13.sp, textDecoration = TextDecoration.LineThrough, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDCFCE7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("${product.discount_percentage}% OFF", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Text(product.description, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
                
                Spacer(modifier = Modifier.height(14.dp))
                
                // Size selectors
                Text("Select Size:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    product.sizes.forEach { size ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedSize == size) MaterialTheme.colorScheme.primary else Color(0xFFF1F5F9))
                                .border(1.dp, if (selectedSize == size) Color.Transparent else Color.LightGray, RoundedCornerShape(6.dp))
                                .clickable { selectedSize = size }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(size, color = if (selectedSize == size) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Color selectors
                Text("Select Color:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    product.colors.forEach { col ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedColor == col) MaterialTheme.colorScheme.secondary else Color(0xFFF1F5F9))
                                .border(1.dp, if (selectedColor == col) Color.Transparent else Color.LightGray, RoundedCornerShape(6.dp))
                                .clickable { selectedColor = col }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(col, color = if (selectedColor == col) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onAddToCart(selectedSize, selectedColor) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Add to Cart", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onBuyNow(selectedSize, selectedColor) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Buy Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==================== WISHLIST TAB ====================

@Composable
fun CustomerWishlistTab(
    wishlist: List<Product>,
    onRemove: (Product) -> Unit,
    onSelectProduct: (Product) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Your Wishlist (${wishlist.size})", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (wishlist.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your wishlist is empty.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(wishlist) { prod ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectProduct(prod) },
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = if (prod.images.isNotEmpty()) prod.images[0].image_url else "",
                                contentDescription = prod.name,
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.brand, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text(prod.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                Text("₹${prod.selling_price.toInt()}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { onRemove(prod) }) {
                                Icon(Icons.Default.Delete, "Remove", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== CART TAB & CHECKOUT ====================

@Composable
fun CustomerCartTab(
    session: AuthSession,
    cart: Cart,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onOrderPlaced: (Order) -> Unit
) {
    var showCheckoutDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Your Shopping Cart", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        if (cart.items.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Your cart is empty.", color = Color.Gray)
            }
        } else {
            Column(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(cart.items) { item ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = if (item.product.images.isNotEmpty()) item.product.images[0].image_url else "",
                                    contentDescription = item.product.name,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.product.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text("Size: ${item.size} • Color: ${item.color}", fontSize = 11.sp, color = Color.Gray)
                                    Text("₹${item.price.toInt()}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { onUpdateQuantity(item.id, item.quantity - 1) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
                                    }
                                    Text(item.quantity.toString(), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    IconButton(
                                        onClick = { onUpdateQuantity(item.id, item.quantity + 1) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { onRemoveItem(item.id) }) {
                                    Icon(Icons.Default.Delete, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }

                // Billing Summary Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", fontSize = 13.sp)
                            Text("₹${cart.subtotal.toInt()}", fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Fee", fontSize = 13.sp)
                            Text(if (cart.deliveryCharge > 0) "₹${cart.deliveryCharge.toInt()}" else "FREE", fontSize = 13.sp, color = Color(0xFF166534))
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("₹${cart.total.toInt()}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Button(
                            onClick = { showCheckoutDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Proceed to Checkout", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showCheckoutDialog) {
        CheckoutDialog(
            session = session,
            cart = cart,
            onDismiss = { showCheckoutDialog = false },
            onCheckoutSuccess = { order ->
                showCheckoutDialog = false
                onOrderPlaced(order)
            }
        )
    }
}

// ==================== CHECKOUT DIALOG ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutDialog(
    session: AuthSession,
    cart: Cart,
    onDismiss: () -> Unit,
    onCheckoutSuccess: (Order) -> Unit
) {
    var name by remember { mutableStateOf(session.name) }
    var mobile by remember { mutableStateOf(session.mobile) }
    var pincode by remember { mutableStateOf("") }
    var addressText by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Bengaluru") }
    var state by remember { mutableStateOf("Karnataka") }
    var deliveryMode by remember { mutableStateOf("standard") } // "standard", "try_at_home"
    var paymentMethod by remember { mutableStateOf("COD") }
    var hasError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Checkout Details", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Mode selection (Standard delivery vs TRYatHOME trial)
                Text("Select Order Type:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { deliveryMode = "standard" },
                        colors = CardDefaults.cardColors(
                            containerColor = if (deliveryMode == "standard") MaterialTheme.colorScheme.primaryContainer else Color(0xFFF1F5F9)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Standard", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Standard Delivery", fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { deliveryMode = "try_at_home" },
                        colors = CardDefaults.cardColors(
                            containerColor = if (deliveryMode == "try_at_home") MaterialTheme.colorScheme.secondaryContainer else Color(0xFFF1F5F9)
                        )
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("TRY at HOME", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("₹99 Fee • Trial Session", fontSize = 9.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Receiver Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Contact Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pincode,
                        onValueChange = { pincode = it.take(6) },
                        label = { Text("Pincode") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("City") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = addressText,
                    onValueChange = { addressText = it },
                    label = { Text("Detailed Delivery Address") },
                    modifier = Modifier.fillMaxWidth()
                )

                if (hasError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Please fill out all address details.", color = Color.Red, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Payment modes
                Text("Payment Method:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = paymentMethod == "COD",
                        onClick = { paymentMethod = "COD" },
                        label = { Text("Cash on Delivery (COD)") }
                    )
                    FilterChip(
                        selected = paymentMethod == "ONLINE_RAZORPAY",
                        onClick = { paymentMethod = "ONLINE_RAZORPAY" },
                        label = { Text("Prepaid (Razorpay)") }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                val totalPayable = if (deliveryMode == "try_at_home") cart.subtotal + 99.0 else cart.total

                Button(
                    onClick = {
                        if (name.isEmpty() || mobile.isEmpty() || pincode.isEmpty() || addressText.isEmpty()) {
                            hasError = true
                            return@Button
                        }
                        val cAddr = CustomerAddress(
                            customer_id = session.mobile,
                            name = name,
                            mobile = mobile,
                            pincode = pincode,
                            address = addressText,
                            city = city,
                            state = state,
                            is_default = true
                        )
                        val order = Database.placeOrder(
                            customerId = session.mobile,
                            address = cAddr,
                            orderType = deliveryMode,
                            paymentMethod = paymentMethod
                        )
                        onCheckoutSuccess(order)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Place Order • ₹${totalPayable.toInt()}", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== PROFILE TAB ====================

@Composable
fun CustomerProfileTab(
    session: AuthSession,
    onSwitchView: (String) -> Unit
) {
    val myOrders = remember { Database.orders.filter { it.customer_id == session.mobile }.sortedByDescending { it.created_at } }
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Customer Identity Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(session.name, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text("+91 ${session.mobile}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Role: ${session.role}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("Your Past Orders (${myOrders.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        if (myOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No orders placed yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(myOrders) { ord ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOrder = ord },
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(ord.order_id, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Mode: ${ord.order_type.uppercase()}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Text("Total: ₹${ord.total.toInt()}", fontSize = 12.sp, color = Color.DarkGray)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (ord.order_status) {
                                            "Delivered" -> Color(0xFFDCFCE7)
                                            "Pending" -> Color(0xFFFEF3C7)
                                            "Cancelled" -> Color(0xFFFEE2E2)
                                            else -> Color(0xFFE0E7FF)
                                        }
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    ord.order_status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (ord.order_status) {
                                        "Delivered" -> Color(0xFF166534)
                                        "Pending" -> Color(0xFFB45309)
                                        "Cancelled" -> Color(0xFF991B1B)
                                        else -> Color(0xFF3730A3)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedOrder?.let { ord ->
        OrderInvoiceDialog(order = ord, onDismiss = { selectedOrder = null })
    }
}

// ==================== ORDER INVOICE DIALOG ====================

@Composable
fun OrderInvoiceDialog(order: Order, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Order Receipt / Invoice", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }

                Spacer(modifier = Modifier.height(14.dp))
                
                Text("ORDER ID: ${order.order_id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Invoice No: ${order.invoice_number ?: "N/A"}", fontSize = 11.sp, color = Color.Gray)
                Text("Date: ${order.created_at.take(10)}", fontSize = 11.sp, color = Color.Gray)
                Text("Type: ${order.order_type.uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Address
                Text("Delivery Address:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(order.address.name, fontSize = 12.sp)
                Text(order.address.address, fontSize = 11.sp, color = Color.Gray)
                Text("${order.address.city}, ${order.address.pincode}", fontSize = 11.sp, color = Color.Gray)

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Items list
                Text("Items Summary:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                order.items.forEach { oi ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(oi.product_name, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("Size: ${oi.size} • Color: ${oi.color} • Qty: ${oi.quantity}", fontSize = 10.sp, color = Color.Gray)
                        }
                        Text("₹${(oi.price * oi.quantity).toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Calculations
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Subtotal", fontSize = 12.sp)
                    Text("₹${order.subtotal.toInt()}", fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Delivery Charge", fontSize = 12.sp)
                    Text(if (order.delivery_charge > 0) "₹${order.delivery_charge.toInt()}" else "FREE", fontSize = 12.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GST (5%)", fontSize = 12.sp)
                    Text("₹${order.tax_amount.toInt()}", fontSize = 12.sp)
                }
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Amount Paid", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("₹${order.total.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Payment Status", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(order.payment_status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                }
            }
        }
    }
}

// ==================== ADMIN PORTAL ====================

@Composable
fun AdminPortal(
    session: AuthSession,
    onLogout: () -> Unit,
    onSwitchView: (String) -> Unit
) {
    var adminTab by remember { mutableStateOf("STATS") } // "STATS", "ORDERS", "PRODUCTS"
    var showProductForm by remember { mutableStateOf<Product?>(null) }
    var isAddingProduct by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.tertiary).padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Shield, null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("ADMIN CONSOLE", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(session.name, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                    
                    Row {
                        IconButton(onClick = { onSwitchView("CUSTOMER") }) {
                            Icon(Icons.Default.ShoppingBag, "Marketplace", tint = Color.White)
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.LogOut, "Logout", tint = Color.White)
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding(), containerColor = Color.White) {
                NavigationBarItem(
                    selected = adminTab == "STATS",
                    onClick = { adminTab = "STATS" },
                    icon = { Icon(Icons.Default.Dashboard, "Dashboard") },
                    label = { Text("Stats") }
                )
                NavigationBarItem(
                    selected = adminTab == "ORDERS",
                    onClick = { adminTab = "ORDERS" },
                    icon = { Icon(Icons.Default.ListAlt, "Orders") },
                    label = { Text("Orders") }
                )
                NavigationBarItem(
                    selected = adminTab == "PRODUCTS",
                    onClick = { adminTab = "PRODUCTS" },
                    icon = { Icon(Icons.Default.ShoppingBag, "Garments") },
                    label = { Text("Garments") }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF1F5F9))
        ) {
            when (adminTab) {
                "STATS" -> AdminStatsTab()
                "ORDERS" -> AdminOrdersTab()
                "PRODUCTS" -> AdminProductsTab(
                    onEdit = { showProductForm = it },
                    onAddProduct = { isAddingProduct = true }
                )
            }

            // Product edit dialog
            showProductForm?.let { prod ->
                AdminProductFormDialog(
                    product = prod,
                    onDismiss = { showProductForm = null },
                    onSave = { updated ->
                        Database.saveProduct(updated)
                        showProductForm = null
                    }
                )
            }

            // Product add dialog
            if (isAddingProduct) {
                AdminProductFormDialog(
                    product = null,
                    onDismiss = { isAddingProduct = false },
                    onSave = { newProd ->
                        Database.saveProduct(newProd)
                        isAddingProduct = false
                    }
                )
            }
        }
    }
}

@Composable
fun AdminStatsTab() {
    val stats = remember { Database.getDashboardStats() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Store Performance Overview", fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(title = "Today Sales", value = "₹${stats.today_sales.toInt()}", modifier = Modifier.weight(1f))
            StatCard(title = "Total Orders", value = stats.total_orders.toString(), modifier = Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(title = "Total Products", value = stats.total_products.toString(), modifier = Modifier.weight(1f))
            StatCard(title = "Low Stock", value = stats.low_stock_products.toString(), modifier = Modifier.weight(1f))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard(title = "Out of Stock", value = stats.out_of_stock_products.toString(), modifier = Modifier.weight(1f))
            StatCard(title = "Total Customers", value = stats.total_customers.toString(), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersTab() {
    var activeFilter by remember { mutableStateOf("ALL") } // "ALL", "Pending", "Delivered", "try_at_home"
    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    val filteredOrders = remember(activeFilter, Database.orders.size) {
        Database.orders.filter { ord ->
            when (activeFilter) {
                "Pending" -> ord.order_status == "Pending"
                "Delivered" -> ord.order_status == "Delivered"
                "try_at_home" -> ord.order_type == "try_at_home"
                else -> true
            }
        }.sortedByDescending { it.created_at }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Horizontal filters
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(selected = activeFilter == "ALL", onClick = { activeFilter = "ALL" }, label = { Text("All") })
            FilterChip(selected = activeFilter == "Pending", onClick = { activeFilter = "Pending" }, label = { Text("Pending") })
            FilterChip(selected = activeFilter == "Delivered", onClick = { activeFilter = "Delivered" }, label = { Text("Delivered") })
            FilterChip(selected = activeFilter == "try_at_home", onClick = { activeFilter = "try_at_home" }, label = { Text("Try at Home") })
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No orders matching selection.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredOrders) { ord ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedOrder = ord },
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(ord.order_id, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Customer: ${ord.customer_name}", fontSize = 12.sp, color = Color.DarkGray)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (ord.order_type == "try_at_home") Color(0xFFF3E8FF) else Color(0xFFE2E8F0))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(ord.order_type.uppercase(), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = if (ord.order_type == "try_at_home") Color(0xFF7E22CE) else Color.DarkGray)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Amount: ₹${ord.total.toInt()}", fontSize = 11.sp)
                                    Text("Status: ${ord.order_status}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                
                                Text(
                                    text = if (ord.assigned_delivery_boy_name != null) "Assigned: ${ord.assigned_delivery_boy_name}" else "Unassigned",
                                    fontSize = 11.sp,
                                    color = if (ord.assigned_delivery_boy_name != null) Color(0xFF166534) else Color(0xFF991B1B),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedOrder?.let { ord ->
        AdminOrderActionDialog(
            order = ord,
            onDismiss = { selectedOrder = null },
            onUpdate = { selectedOrder = null }
        )
    }
}

@Composable
fun AdminProductsTab(
    onEdit: (Product) -> Unit,
    onAddProduct: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Garments Catalog", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Button(onClick = onAddProduct, shape = RoundedCornerShape(8.dp)) {
                Icon(Icons.Default.Add, null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Garment", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(Database.products) { prod ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = if (prod.images.isNotEmpty()) prod.images[0].image_url else "",
                            contentDescription = prod.name,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                            Text("SKU: ${prod.sku} • Stock: ${prod.stock}", fontSize = 11.sp, color = Color.Gray)
                            Text("₹${prod.selling_price.toInt()}", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        
                        IconButton(onClick = { onEdit(prod) }) {
                            Icon(Icons.Default.Edit, "Edit", tint = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

// ==================== ADMIN ORDER ACTIONS DIALOG ====================

@Composable
fun AdminOrderActionDialog(
    order: Order,
    onDismiss: () -> Unit,
    onUpdate: () -> Unit
) {
    var statusSelection by remember { mutableStateOf(order.order_status) }
    var assignedBoyId by remember { mutableStateOf(order.assigned_delivery_boy_id ?: "") }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Manage Order", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }

                Spacer(modifier = Modifier.height(14.dp))
                
                Text("Order ID: ${order.order_id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("Customer: ${order.customer_name} (${order.mobile})", fontSize = 12.sp)
                Text("Type: ${order.order_type.uppercase()}", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Status Update
                Text("Update Order Status:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                
                val statuses = listOf("Pending", "Confirmed", "Processing", "Packed", "Shipped", "Out for Delivery", "Delivered", "Cancelled")
                Column {
                    statuses.chunked(2).forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { st ->
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { statusSelection = st }
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (statusSelection == st) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF1F5F9)
                                    )
                                ) {
                                    Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                                        Text(st, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp))

                // Assign Delivery Boy
                Text("Assign Delivery Associate:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                
                Database.deliveryBoys.forEach { boy ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { assignedBoyId = boy.id }
                            .background(if (assignedBoyId == boy.id) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = assignedBoyId == boy.id, onClick = { assignedBoyId = boy.id })
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(boy.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("Area: ${boy.assigned_area ?: "N/A"}", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        Database.updateOrderStatus(order.id, statusSelection, "Admin Action")
                        if (assignedBoyId.isNotEmpty()) {
                            Database.assignDeliveryBoy(order.id, assignedBoyId)
                        }
                        onUpdate()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Apply & Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== ADMIN PRODUCT FORM DIALOG ====================

@Composable
fun AdminProductFormDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var name by remember { mutableStateOf(product?.name ?: "") }
    var brand by remember { mutableStateOf(product?.brand ?: "") }
    var sku by remember { mutableStateOf(product?.sku ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var mrp by remember { mutableStateOf(product?.mrp?.toString() ?: "") }
    var sellingPrice by remember { mutableStateOf(product?.selling_price?.toString() ?: "") }
    var stock by remember { mutableStateOf(product?.stock?.toString() ?: "") }
    var categoryId by remember { mutableStateOf(product?.category_id ?: "cat-1") }
    var gender by remember { mutableStateOf(product?.gender ?: "Men") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (product == null) "Add New Garment" else "Edit Garment", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Garment Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand Name") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU Code") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Product Description") }, modifier = Modifier.fillMaxWidth())
                
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = mrp, onValueChange = { mrp = it }, label = { Text("MRP (₹)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    OutlinedTextField(value = sellingPrice, onValueChange = { sellingPrice = it }, label = { Text("Selling Price (₹)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Inventory Stock") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

                Spacer(modifier = Modifier.height(12.dp))
                Text("Garment Target Gender:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("Men", "Women", "Kids", "Unisex").forEach { g ->
                        FilterChip(selected = gender == g, onClick = { gender = g }, label = { Text(g) })
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val finalMrp = mrp.toDoubleOrNull() ?: 999.0
                        val finalSelling = sellingPrice.toDoubleOrNull() ?: 499.0
                        val finalStock = stock.toIntOrNull() ?: 10
                        val disc = (((finalMrp - finalSelling) / finalMrp) * 100).toInt().coerceAtLeast(0)

                        val categoryName = Database.categories.find { it.id == categoryId }?.name ?: "Ethnic Wear"

                        val p = Product(
                            id = product?.id ?: "prod-${UUID.randomUUID()}",
                            sku = sku,
                            name = name,
                            slug = name.lowercase().replace(" ", "-"),
                            category_id = categoryId,
                            category_name = categoryName,
                            gender = gender,
                            description = description,
                            brand = brand,
                            mrp = finalMrp,
                            selling_price = finalSelling,
                            discount_percentage = disc,
                            stock = finalStock,
                            status = "Published",
                            sizes = listOf("M", "L", "XL"),
                            colors = listOf("Navy", "Off-White"),
                            images = product?.images ?: listOf(
                                ProductImage("img-${UUID.randomUUID()}", "https://images.unsplash.com/photo-1489987707025-afc232f7ea0f?w=400", 0, true)
                            ),
                            created_at = product?.created_at ?: SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
                        )
                        onSave(p)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Apply & Save Product", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ==================== DELIVERY BOY PORTAL ====================

@Composable
fun DeliveryBoyPortal(
    session: AuthSession,
    onLogout: () -> Unit
) {
    val assignedOrders = remember(Database.orders.size) {
        Database.orders.filter { it.assigned_delivery_boy_id == session.userId || it.assigned_delivery_boy_mobile == session.mobile }
    }
    
    var selectedOrderForDeliver by remember { mutableStateOf<Order?>(null) }
    var mockOtpCode by remember { mutableStateOf("") }
    var hasOtpError by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.secondary).padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Truck, null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("DELIVERY PORTAL", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(session.name, fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                    
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.LogOut, "Logout", tint = Color.White)
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text("Your Assigned Deliveries (${assignedOrders.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (assignedOrders.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No deliveries assigned.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(assignedOrders) { ord ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(ord.order_id, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("Type: ${ord.order_type.uppercase()}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(ord.order_status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 10.dp))

                                Text("Customer: ${ord.customer_name}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text("Mobile: +91 ${ord.mobile}", fontSize = 11.sp, color = Color.Gray)
                                Text("Address: ${ord.address.address}, ${ord.address.city}", fontSize = 11.sp, color = Color.Gray)

                                Divider(modifier = Modifier.padding(vertical = 10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            Database.updateOrderStatus(ord.id, "Out for Delivery", "Delivery Boy Started", session.name)
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6B7280))
                                    ) {
                                        Text("Out for Delivery", fontSize = 10.sp)
                                    }
                                    
                                    Button(
                                        onClick = {
                                            selectedOrderForDeliver = ord
                                            mockOtpCode = ""
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534))
                                    ) {
                                        Text("Mark Delivered", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Mock OTP Verification for Delivery
        selectedOrderForDeliver?.let { ord ->
            Dialog(onDismissRequest = { selectedOrderForDeliver = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Verify Customer OTP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Customer must provide 6-digit OTP to unlock delivery.", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("DEMO HINT: Customer OTP is \"123456\"", fontSize = 12.sp, color = Color(0xFF166534), fontWeight = FontWeight.Bold)
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = mockOtpCode,
                            onValueChange = { mockOtpCode = it.take(6) },
                            label = { Text("Customer OTP") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        if (hasOtpError) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Invalid OTP. Hint: Use 123456", color = Color.Red, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { selectedOrderForDeliver = null }, modifier = Modifier.weight(1f)) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    if (mockOtpCode == "123456") {
                                        Database.updateOrderStatus(ord.id, "Delivered", "Delivered with Customer OTP Verification", session.name)
                                        selectedOrderForDeliver = null
                                        hasOtpError = false
                                    } else {
                                        hasOtpError = true
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Confirm Code")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==================== SHOPKEEPER PORTAL ====================

@Composable
fun ShopkeeperPortal(
    session: AuthSession,
    onLogout: () -> Unit,
    onSwitchView: (String) -> Unit
) {
    var selectedShopkeeperTab by remember { mutableStateOf("STOCK") } // "STOCK", "HISTORY"
    val shopkeeper = remember { Database.shopkeepers.find { it.mobile == session.mobile } ?: Database.shopkeepers[0] }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF78350F)).padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Storefront, null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(shopkeeper.store_name ?: "SHOPKEEPER PORTAL", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Manager: ${shopkeeper.name}", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                    
                    Row {
                        IconButton(onClick = { onSwitchView("CUSTOMER") }) {
                            Icon(Icons.Default.ShoppingBag, "View Store", tint = Color.White)
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.LogOut, "Logout", tint = Color.White)
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.navigationBarsPadding(), containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedShopkeeperTab == "STOCK",
                    onClick = { selectedShopkeeperTab = "STOCK" },
                    icon = { Icon(Icons.Default.Inventory, "My Stock") },
                    label = { Text("My Stock") }
                )
                NavigationBarItem(
                    selected = selectedShopkeeperTab == "HISTORY",
                    onClick = { selectedShopkeeperTab = "HISTORY" },
                    icon = { Icon(Icons.Default.History, "Transactions") },
                    label = { Text("Transactions") }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Quick shop stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
            ) {
                Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Current Inventory Stock", fontSize = 11.sp, color = Color(0xFF92400E))
                        Text("${shopkeeper.current_stock} pcs", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF78350F))
                    }
                    Column {
                        Text("Your Portal Status", fontSize = 11.sp, color = Color(0xFF92400E))
                        Text(shopkeeper.status, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedShopkeeperTab == "STOCK") {
                Text("Stock In-Inventory Products", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(Database.products) { prod ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = if (prod.images.isNotEmpty()) prod.images[0].image_url else "",
                                    contentDescription = prod.name,
                                    modifier = Modifier.size(50.dp).clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(prod.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    Text("SKU: ${prod.sku}", fontSize = 11.sp, color = Color.Gray)
                                    Text("Stock: ${prod.stock} pcs", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                
                                Button(
                                    onClick = {
                                        prod.stock += 10
                                        shopkeeper.current_stock += 10
                                        Database.stockTransactions.add(
                                            StockTransaction(
                                                transaction_id = "STX-${1000 + Database.stockTransactions.size}",
                                                product_id = prod.id,
                                                product_name = prod.name,
                                                sku = prod.sku,
                                                shopkeeper_id = shopkeeper.id,
                                                shopkeeper_name = shopkeeper.name,
                                                transaction_type = "IN",
                                                quantity = 10,
                                                previous_stock = prod.stock - 10,
                                                new_stock = prod.stock,
                                                performed_by = "SHOPKEEPER",
                                                timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())
                                            )
                                        )
                                    },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("+10 Stock", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                Text("Recent Inventory Transactions", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                val txs = remember { Database.stockTransactions.sortedByDescending { it.timestamp } }

                if (txs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No stock transactions logged yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(txs) { tx ->
                            Card(colors = CardDefaults.cardColors(containerColor = Color.White)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(tx.transaction_id, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(tx.transaction_type, fontWeight = FontWeight.ExtraBold, color = if (tx.transaction_type == "IN") Color(0xFF166534) else Color(0xFF991B1B))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(tx.product_name, fontSize = 12.sp)
                                    Text("Qty added: ${tx.quantity} • Stock: ${tx.previous_stock} -> ${tx.new_stock}", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
