package com.example

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object Database {
    val settings = StoreSettings()
    
    val categories = mutableListOf<Category>()
    val products = mutableListOf<Product>()
    val customers = mutableListOf<Customer>()
    val orders = mutableListOf<Order>()
    val deliveryBoys = mutableListOf<DeliveryBoy>()
    val shopkeepers = mutableListOf<Shopkeeper>()
    val stockTransactions = mutableListOf<StockTransaction>()
    
    private val carts = mutableMapOf<String, Cart>() // customerId -> Cart
    private val wishlists = mutableMapOf<String, MutableList<String>>() // customerId -> productIds
    
    var currentSession: AuthSession? = null

    init {
        seedCategories()
        seedProducts()
        seedCustomers()
        seedDeliveryBoys()
        seedShopkeepers()
        seedOrders()
    }

    private fun getCurrentTimeString(offsetDays: Long = 0): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val time = System.currentTimeMillis() - (offsetDays * 24 * 60 * 60 * 1000)
        return sdf.format(Date(time))
    }

    private fun seedCategories() {
        categories.addAll(
            listOf(
                Category("cat-1", "Ethnic Wear", "ethnic-wear", "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=400", "ACTIVE", 1, "Gorgeous traditional Indian sarees, kurtas and sherwanis"),
                Category("cat-2", "Casual Wear", "casual-wear", "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400", "ACTIVE", 2, "Laid back jeans, tees, shirts, and casual outfits"),
                Category("cat-3", "Activewear", "activewear", "https://images.unsplash.com/photo-1517838277536-f5f99be501cd?w=400", "ACTIVE", 3, "High comfort breathable athletic and sports apparel"),
                Category("cat-4", "Kids Wardrobe", "kids-wardrobe", "https://images.unsplash.com/photo-1519457431-44ccd64a579b?w=400", "ACTIVE", 4, "Cute garments, shirts and frocks for infants & children"),
                Category("cat-5", "Footwear", "footwear", "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=400", "ACTIVE", 5, "Elegant shoes, sandals, slides, and ethnic footwear")
            )
        )
    }

    private fun seedProducts() {
        val p1 = Product(
            id = "prod-1",
            sku = "ST1-JNS-0001",
            name = "Men's Urban Slim Fit Washed Blue Denim",
            slug = "mens-urban-slim-fit-denim",
            category_id = "cat-2",
            category_name = "Casual Wear",
            gender = "Men",
            description = "Premium hand-washed stretchable blue denim. Slim tailoring through the hip and leg for a modern silhouette.",
            brand = "TRYatHOME Originals",
            mrp = 2499.0,
            selling_price = 999.0,
            discount_percentage = 60,
            stock = 45,
            status = "Published",
            sizes = listOf("30", "32", "34", "36"),
            colors = listOf("Washed Blue", "Dark Indigo"),
            images = listOf(
                ProductImage("img-1-1", "https://images.unsplash.com/photo-1542272604-787c3835535d?w=800", 0, true)
            ),
            created_at = getCurrentTimeString(10)
        )

        val p2 = Product(
            id = "prod-2",
            sku = "ST1-KRT-0002",
            name = "Lucknowi Handcrafted Chikankari Pure Georgette Kurti",
            slug = "lucknowi-handcrafted-chikankari-kurti",
            category_id = "cat-1",
            category_name = "Ethnic Wear",
            gender = "Women",
            description = "Elegantly hand-embroidered Lucknowi Chikankari georgette kurti. Features fine shadow work thread embroideries.",
            brand = "TRYatHOME Heritage Loom",
            mrp = 2799.0,
            selling_price = 1199.0,
            discount_percentage = 57,
            stock = 30,
            status = "Published",
            sizes = listOf("S", "M", "L", "XL"),
            colors = listOf("Teal Blue", "Peach Sunset", "Ivory White"),
            images = listOf(
                ProductImage("img-2-1", "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=800", 0, true)
            ),
            created_at = getCurrentTimeString(12)
        )

        val p3 = Product(
            id = "prod-3",
            sku = "ST1-TSH-0003",
            name = "Men's Cotton Heavyweight Oversized Graphic Tee",
            slug = "mens-heavyweight-oversized-graphic-tee",
            category_id = "cat-2",
            category_name = "Casual Wear",
            gender = "Men",
            description = "Oversized fit streetwear tee made from 240 GSM heavy combed cotton. Bold retro graphic on chest.",
            brand = "TRYatHOME Originals",
            mrp = 1199.0,
            selling_price = 499.0,
            discount_percentage = 58,
            stock = 60,
            status = "Published",
            sizes = listOf("M", "L", "XL"),
            colors = listOf("Charcoal", "Emerald Green", "Off-White"),
            images = listOf(
                ProductImage("img-3-1", "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=800", 0, true)
            ),
            created_at = getCurrentTimeString(5)
        )

        val p4 = Product(
            id = "prod-4",
            sku = "ST1-DRS-0004",
            name = "Women's Floral Tiered Ruffle Georgette Dress",
            slug = "womens-floral-tiered-ruffle-dress",
            category_id = "cat-2",
            category_name = "Casual Wear",
            gender = "Women",
            description = "Beautiful flowing georgette summer maxi dress featuring ditsy flower pattern and layered tier ruffles.",
            brand = "Boutique Threads",
            mrp = 2999.0,
            selling_price = 1299.0,
            discount_percentage = 56,
            stock = 25,
            status = "Published",
            sizes = listOf("XS", "S", "M", "L"),
            colors = listOf("Blush Floral", "Sky Blue Floral"),
            images = listOf(
                ProductImage("img-4-1", "https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=800", 0, true)
            ),
            created_at = getCurrentTimeString(4)
        )

        val p5 = Product(
            id = "prod-5",
            sku = "ST1-KDS-0005",
            name = "Kids Comfort-Fit Cotton Dungaree Set",
            slug = "kids-comfort-fit-cotton-dungaree",
            category_id = "cat-4",
            category_name = "Kids Wardrobe",
            gender = "Kids",
            description = "Adorable 100% organic cotton dungaree set for toddlers. Extremely soft, stretchable, and features easy-snap buttons.",
            brand = "TinyTot Garments",
            mrp = 1499.0,
            selling_price = 699.0,
            discount_percentage = 53,
            stock = 20,
            status = "Published",
            sizes = listOf("2Y", "3Y", "4Y", "5Y"),
            colors = listOf("Mustard & White", "Olive & Stripes"),
            images = listOf(
                ProductImage("img-5-1", "https://images.unsplash.com/photo-1519457431-44ccd64a579b?w=800", 0, true)
            ),
            created_at = getCurrentTimeString(8)
        )

        val p6 = Product(
            id = "prod-6",
            sku = "ST1-SHO-0006",
            name = "Handcrafted Kolhapuri Leather Sandals",
            slug = "handcrafted-kolhapuri-leather-sandals",
            category_id = "cat-5",
            category_name = "Footwear",
            gender = "Unisex",
            description = "Genuinely hand-stitched traditional Kolhapuri leather flat slippers. Features braided cord embellishments.",
            brand = "TRYatHOME Heritage Loom",
            mrp = 1999.0,
            selling_price = 799.0,
            discount_percentage = 60,
            stock = 15,
            status = "Published",
            sizes = listOf("7", "8", "9", "10"),
            colors = listOf("Tan Tan", "Coal Black"),
            images = listOf(
                ProductImage("img-6-1", "https://images.unsplash.com/photo-1549298916-b41d501d3772?w=800", 0, true)
            ),
            created_at = getCurrentTimeString(15)
        )

        products.addAll(listOf(p1, p2, p3, p4, p5, p6))
        
        // Also map variants for each product
        products.forEach { prod ->
            val vList = mutableListOf<ProductVariant>()
            prod.sizes.forEach { sz ->
                prod.colors.forEach { col ->
                    vList.add(
                        ProductVariant(
                            size = sz,
                            color = col,
                            sku = "${prod.sku}-$sz-${col.take(2).uppercase()}",
                            stock = 10,
                            price = prod.selling_price,
                            mrp = prod.mrp
                        )
                    )
                }
            }
            prod.variants = vList
        }
    }

    private fun seedCustomers() {
        customers.addAll(
            listOf(
                Customer(
                    id = "cust-1",
                    customer_id = "STYLE1-CUST-000001",
                    name = "Aarav Sharma",
                    mobile = "9876543210",
                    email = "aarav.sharma@example.com",
                    created_at = getCurrentTimeString(30),
                    status = "ACTIVE",
                    total_orders = 1,
                    total_spent = 1997.0,
                    last_order_at = getCurrentTimeString(5),
                    addresses = listOf(
                        CustomerAddress(
                            id = "addr-1",
                            customer_id = "STYLE1-CUST-000001",
                            name = "Aarav Sharma",
                            mobile = "9876543210",
                            pincode = "560001",
                            address = "Flat 402, Sunshine Heights, MG Road",
                            locality = "Near Trinity Metro",
                            city = "Bengaluru",
                            state = "Karnataka",
                            landmark = "Opposite Taj",
                            address_type = "HOME",
                            is_default = true
                        )
                    )
                ),
                Customer(
                    id = "cust-2",
                    customer_id = "STYLE1-CUST-000002",
                    name = "Priya Patel",
                    mobile = "9898989898",
                    email = "priya.patel@example.com",
                    created_at = getCurrentTimeString(14),
                    status = "ACTIVE",
                    addresses = listOf(
                        CustomerAddress(
                            id = "addr-3",
                            customer_id = "STYLE1-CUST-000002",
                            name = "Priya Patel",
                            mobile = "9898989898",
                            pincode = "380009",
                            address = "A-12 Nilgiri Apartments, Navrangpura",
                            city = "Ahmedabad",
                            state = "Gujarat",
                            address_type = "HOME",
                            is_default = true
                        )
                    )
                )
            )
        )
    }

    private fun seedDeliveryBoys() {
        deliveryBoys.addAll(
            listOf(
                DeliveryBoy("dboy-1", "STYLE1-DBOY-000001", "Ramesh Kumar", "9876543201", "ramesh@tryathome.in", "Motorcycle", "KA-01-AB-1234", "Bengaluru", "ACTIVE", "Indiranagar", getCurrentTimeString(30), 42),
                DeliveryBoy("dboy-2", "STYLE1-DBOY-000002", "Sunil Verma", "9876543202", "sunil@tryathome.in", "Scooter", "KA-05-XY-5678", "Bengaluru", "ACTIVE", "Koramangala", getCurrentTimeString(25), 29)
            )
        )
    }

    private fun seedShopkeepers() {
        shopkeepers.addAll(
            listOf(
                Shopkeeper("shop-1", "STYLE1-SHOP-000001", "Rajesh Mehra", "Rajesh Ethnic Trends", "9810101010", "rajesh@tryathome.in", "Jaipur", "ACTIVE", getCurrentTimeString(25), ShopkeeperPermissions(), 3, 2, 1, 85, 8)
            )
        )
    }

    private fun seedOrders() {
        orders.add(
            Order(
                id = "ord-1",
                order_id = "STYLE1-ORD-000001",
                invoice_number = "INV-10025",
                customer_id = "STYLE1-CUST-000001",
                customer_name = "Aarav Sharma",
                mobile = "9876543210",
                email = "aarav.sharma@example.com",
                address = customers[0].addresses[0],
                items = listOf(
                    OrderItem(
                        id = "oi-1",
                        order_id = "STYLE1-ORD-000001",
                        product_id = "prod-1",
                        product_name = "Men's Urban Slim Fit Washed Blue Denim",
                        brand = "TRYatHOME Originals",
                        sku = "ST1-JNS-0001",
                        quantity = 1,
                        price = 999.0,
                        mrp = 2499.0,
                        size = "32",
                        color = "Washed Blue",
                        image_url = "https://images.unsplash.com/photo-1542272604-787c3835535d?w=400",
                        item_status = "Delivered"
                    )
                ),
                subtotal = 999.0,
                discount = 1500.0,
                delivery_charge = 49.0,
                tax_amount = 50.0,
                total = 1048.0,
                payment_method = "COD",
                payment_status = "PAID",
                order_status = "Delivered",
                created_at = getCurrentTimeString(5),
                updated_at = getCurrentTimeString(2),
                status_history = listOf(
                    OrderStatusHistoryItem("sh-1", "STYLE1-ORD-000001", "Pending", "Customer", getCurrentTimeString(5)),
                    OrderStatusHistoryItem("sh-2", "STYLE1-ORD-000001", "Delivered", "Delivery Associate", getCurrentTimeString(2), "Delivered to recipient.")
                )
            )
        )
    }

    // Role authentication check
    fun checkMobileRole(phone: String): Pair<String?, Any?> {
        val clean = phone.replace(Regex("\\D"), "")
        if (clean == "9999999999") {
            return Pair("ADMIN", mapOf("id" to "adm-1", "name" to "TRYatHOME Admin", "mobile" to "9999999999"))
        }
        val sb = shopkeepers.find { it.mobile == clean }
        if (sb != null) return Pair("SHOPKEEPER", sb)
        
        val dboy = deliveryBoys.find { it.mobile == clean }
        if (dboy != null) return Pair("DELIVERY_BOY", dboy)
        
        val cust = customers.find { it.mobile == clean }
        if (cust != null) return Pair("CUSTOMER", cust)
        
        return Pair(null, null)
    }

    fun login(phone: String, role: String, name: String = "", email: String = ""): AuthSession {
        val clean = phone.replace(Regex("\\D"), "")
        val sessionName = when (role) {
            "ADMIN" -> "TRYatHOME Admin"
            "SHOPKEEPER" -> shopkeepers.find { it.mobile == clean }?.name ?: "Shopkeeper"
            "DELIVERY_BOY" -> deliveryBoys.find { it.mobile == clean }?.name ?: "Delivery Associate"
            else -> {
                val existing = customers.find { it.mobile == clean }
                if (existing != null) existing.name else {
                    val newCust = Customer(
                        id = "cust-${UUID.randomUUID()}",
                        customer_id = "STYLE1-CUST-00000${customers.size + 1}",
                        name = name.ifEmpty { "Customer" },
                        mobile = clean,
                        email = email.ifEmpty { null },
                        created_at = getCurrentTimeString(),
                        addresses = emptyList()
                    )
                    customers.add(newCust)
                    newCust.name
                }
            }
        }

        currentSession = AuthSession(
            userId = clean,
            role = role,
            mobile = clean,
            name = sessionName,
            token = UUID.randomUUID().toString(),
            authenticated_at = getCurrentTimeString(),
            expires_at = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)
        )
        return currentSession!!
    }

    fun logout() {
        currentSession = null
    }

    // Cart operations
    fun getCart(customerId: String): Cart {
        return carts.getOrPut(customerId) { Cart(customer_id = customerId) }
    }

    fun addToCart(product: Product, size: String, color: String, qty: Int, customerId: String) {
        val cart = getCart(customerId)
        val existing = cart.items.find { it.product_id == product.id && it.size == size && it.color == color }
        if (existing != null) {
            existing.quantity += qty
        } else {
            val newItem = CartItem(
                product_id = product.id,
                product = product,
                size = size,
                color = color,
                quantity = qty,
                price = product.selling_price,
                mrp = product.mrp
            )
            cart.items = cart.items + newItem
        }
    }

    fun updateCartQuantity(cartItemId: String, qty: Int, customerId: String) {
        val cart = getCart(customerId)
        cart.items.find { it.id == cartItemId }?.let {
            if (qty <= 0) {
                cart.items = cart.items.filter { it.id != cartItemId }
            } else {
                it.quantity = qty
            }
        }
    }

    fun removeFromCart(cartItemId: String, customerId: String) {
        val cart = getCart(customerId)
        cart.items = cart.items.filter { it.id != cartItemId }
    }

    fun clearCart(customerId: String) {
        carts[customerId] = Cart(customer_id = customerId)
    }

    // Wishlist operations
    fun getWishlist(customerId: String): List<Product> {
        val list = wishlists.getOrPut(customerId) { mutableListOf() }
        return products.filter { list.contains(it.id) }
    }

    fun toggleWishlist(product: Product, customerId: String) {
        val list = wishlists.getOrPut(customerId) { mutableListOf() }
        if (list.contains(product.id)) {
            list.remove(product.id)
        } else {
            list.add(product.id)
        }
    }

    fun removeFromWishlist(productId: String, customerId: String) {
        wishlists[customerId]?.remove(productId)
    }

    // Checkout operations
    fun placeOrder(customerId: String, address: CustomerAddress, orderType: String = "standard", paymentMethod: String = "COD"): Order {
        val cart = getCart(customerId)
        val itemsList = cart.items.map {
            OrderItem(
                order_id = "",
                product_id = it.product_id,
                product_name = it.product.name,
                brand = it.product.brand,
                sku = it.product.sku,
                quantity = it.quantity,
                price = it.price,
                mrp = it.mrp,
                size = it.size,
                color = it.color,
                image_url = if (it.product.images.isNotEmpty()) it.product.images[0].image_url else ""
            )
        }

        val oId = "STYLE1-ORD-00000${orders.size + 1}"
        val order = Order(
            id = "ord-${UUID.randomUUID()}",
            order_id = oId,
            invoice_number = "INV-${10000 + orders.size + 1}",
            customer_id = customerId,
            customer_name = address.name,
            mobile = address.mobile,
            address = address,
            items = itemsList,
            subtotal = cart.subtotal,
            discount = cart.totalDiscount,
            delivery_charge = if (orderType == "try_at_home") settings.try_at_home_charge else cart.deliveryCharge,
            tax_amount = cart.subtotal * 0.05,
            total = if (orderType == "try_at_home") cart.subtotal + settings.try_at_home_charge else cart.total,
            payment_method = paymentMethod,
            payment_status = if (paymentMethod == "COD") "PENDING" else "PAID",
            order_status = "Pending",
            order_type = orderType,
            created_at = getCurrentTimeString(),
            updated_at = getCurrentTimeString(),
            try_at_home_status = if (orderType == "try_at_home") "ACTIVE" else null,
            try_at_home_expires_at = if (orderType == "try_at_home") getCurrentTimeString(-1) /* placeholder */ else null,
            try_at_home_fee = if (orderType == "try_at_home") settings.try_at_home_charge else 0.0
        )

        // Bind correct Order ID to items
        val finalItems = order.items.map { it.copy(order_id = order.id) }
        val finalOrder = order.copy(
            items = finalItems,
            status_history = listOf(
                OrderStatusHistoryItem(
                    order_id = order.id,
                    status = "Pending",
                    changed_by = "Customer (Checkout)",
                    changed_at = getCurrentTimeString()
                )
            )
        )

        orders.add(finalOrder)
        clearCart(customerId)
        
        // Decrement product stocks
        finalItems.forEach { oi ->
            products.find { it.id == oi.product_id }?.let { prod ->
                prod.stock = (prod.stock - oi.quantity).coerceAtLeast(0)
                prod.variants.find { it.size == oi.size && it.color == oi.color }?.let { vr ->
                    vr.stock = (vr.stock - oi.quantity).coerceAtLeast(0)
                }
            }
        }

        return finalOrder
    }

    fun updateOrderStatus(orderId: String, newStatus: String, notes: String? = null, changedBy: String = "Admin") {
        orders.find { it.id == orderId || it.order_id == orderId }?.let { ord ->
            ord.order_status = newStatus
            if (newStatus == "Delivered") {
                ord.payment_status = "PAID"
            }
            ord.status_history = ord.status_history + OrderStatusHistoryItem(
                order_id = ord.id,
                status = newStatus,
                changed_by = changedBy,
                changed_at = getCurrentTimeString(),
                notes = notes
            )
            ord.items.forEach { it.item_status = newStatus }
        }
    }

    fun assignDeliveryBoy(orderId: String, boyId: String) {
        val boy = deliveryBoys.find { it.id == boyId || it.delivery_boy_id == boyId }
        orders.find { it.id == orderId || it.order_id == orderId }?.let { ord ->
            ord.assigned_delivery_boy_id = boy?.id
            ord.assigned_delivery_boy_name = boy?.name
            ord.assigned_delivery_boy_mobile = boy?.mobile
        }
    }

    fun getDashboardStats(): DashboardStats {
        val delivered = orders.filter { it.order_status == "Delivered" }
        return DashboardStats(
            today_orders = orders.size,
            today_sales = delivered.sumOf { it.total },
            total_orders = orders.size,
            total_customers = customers.size,
            total_products = products.size,
            out_of_stock_products = products.count { it.stock == 0 },
            low_stock_products = products.count { it.stock in 1..5 },
            pending_orders = orders.count { it.order_status == "Pending" },
            delivered_orders = delivered.size,
            total_sales_amount = delivered.sumOf { it.total }
        )
    }

    fun saveProduct(product: Product) {
        val index = products.indexOfFirst { it.id == product.id }
        if (index >= 0) {
            products[index] = product
        } else {
            products.add(product)
        }
    }
}
