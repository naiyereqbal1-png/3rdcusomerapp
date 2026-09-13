package com.example

import java.util.UUID

data class CustomerAddress(
    val id: String = UUID.randomUUID().toString(),
    val customer_id: String,
    val name: String,
    val mobile: String,
    val pincode: String,
    val address: String,
    val locality: String? = null,
    val city: String,
    val state: String,
    val landmark: String? = null,
    val address_type: String = "HOME", // "HOME", "WORK", "OTHER"
    val is_default: Boolean = false
)

data class Customer(
    val id: String,
    val customer_id: String,
    val name: String,
    val mobile: String,
    val email: String? = null,
    val created_at: String,
    val status: String = "ACTIVE", // "ACTIVE", "BLOCKED"
    val total_orders: Int = 0,
    val total_spent: Double = 0.0,
    val last_order_at: String? = null,
    val addresses: List<CustomerAddress> = emptyList()
)

data class Category(
    val id: String,
    val name: String,
    val slug: String,
    val image: String,
    val status: String = "ACTIVE", // "ACTIVE", "INACTIVE"
    val sort_order: Int = 0,
    val description: String? = null
)

data class ProductImage(
    val id: String = UUID.randomUUID().toString(),
    val image_url: String,
    val sort_order: Int = 0,
    val is_primary: Boolean = false
)

data class ProductVariant(
    val id: String = UUID.randomUUID().toString(),
    val size: String,
    val color: String,
    val sku: String,
    var stock: Int,
    val price: Double,
    val mrp: Double
)

data class Product(
    val id: String,
    val sku: String,
    val name: String,
    val slug: String,
    val category_id: String,
    val category_name: String,
    val gender: String, // "Men", "Women", "Kids", "Unisex"
    val description: String,
    val brand: String,
    val mrp: Double,
    val selling_price: Double,
    val discount_percentage: Int,
    var stock: Int,
    var status: String, // "Draft", "Published", "Unpublished", "Out of Stock"
    val rating: Double = 4.5,
    val rating_count: Int = 12,
    val sizes: List<String> = emptyList(),
    val colors: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val images: List<ProductImage> = emptyList(),
    var variants: List<ProductVariant> = emptyList(),
    val created_at: String,
    val shopkeeper_id: String? = null,
    val shopkeeper_name: String? = null,
    var approval_status: String = "APPROVED", // "PENDING", "APPROVED", "REJECTED"
    var is_live: Boolean = true,
    var rejection_reason: String? = null
)

data class CartItem(
    val id: String = UUID.randomUUID().toString(),
    val product_id: String,
    val product: Product,
    val size: String,
    val color: String,
    var quantity: Int,
    val price: Double,
    val mrp: Double
)

data class Cart(
    val id: String = UUID.randomUUID().toString(),
    val customer_id: String,
    var items: List<CartItem> = emptyList()
) {
    val subtotal: Double get() = items.sumOf { it.price * it.quantity }
    val totalDiscount: Double get() = items.sumOf { (it.mrp - it.price) * it.quantity }
    val deliveryCharge: Double get() = if (subtotal > 499.0 || subtotal == 0.0) 0.0 else 49.0
    val total: Double get() = subtotal + deliveryCharge
}

data class WishlistItem(
    val id: String = UUID.randomUUID().toString(),
    val customer_id: String,
    val product_id: String,
    val product: Product,
    val added_at: String
)

data class OrderItem(
    val id: String = UUID.randomUUID().toString(),
    val order_id: String,
    val product_id: String,
    val product_name: String,
    val brand: String,
    val sku: String,
    val quantity: Int,
    val price: Double,
    val mrp: Double,
    val size: String,
    val color: String,
    val image_url: String,
    var item_status: String? = "Pending", // "Pending", "Confirmed", etc.
    var return_status: String? = null, // "Return Requested", etc.
    var request_type: String? = null, // "return" or "replace"
    var replacement_size: String? = null,
    var replacement_color: String? = null
)

data class OrderStatusHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val order_id: String,
    val status: String,
    val changed_by: String,
    val changed_at: String,
    val notes: String? = null
)

data class Order(
    val id: String,
    val order_id: String,
    val invoice_number: String? = null,
    val customer_id: String,
    val customer_name: String,
    val mobile: String,
    val email: String? = null,
    val address: CustomerAddress,
    val items: List<OrderItem>,
    val subtotal: Double,
    val discount: Double,
    val delivery_charge: Double,
    val tax_amount: Double,
    val total: Double,
    val payment_method: String, // "COD", "ONLINE_RAZORPAY"
    var payment_status: String, // "PENDING", "PAID", "FAILED"
    var order_status: String, // "Pending", "Confirmed", "Processing", "Packed", "Shipped", "Out for Delivery", "Delivered", "Cancelled"
    val order_type: String = "standard", // "standard", "try_at_home"
    val created_at: String,
    val updated_at: String,
    var status_history: List<OrderStatusHistoryItem> = emptyList(),
    var assigned_delivery_boy_id: String? = null,
    var assigned_delivery_boy_name: String? = null,
    var assigned_delivery_boy_mobile: String? = null,
    var try_at_home_status: String? = null, // "ACTIVE", "EXPIRED", "CLOSED", "COMPLETED"
    var try_at_home_expires_at: String? = null,
    var try_at_home_fee: Double = 99.0,
    var try_at_home_duration_minutes: Int = 30,
    var try_at_home_decision_notes: String? = null
)

data class DeliveryBoy(
    val id: String,
    val delivery_boy_id: String,
    val name: String,
    val mobile: String,
    val email: String? = null,
    val vehicle_type: String? = null,
    val vehicle_number: String? = null,
    val city: String? = null,
    val status: String = "ACTIVE",
    val assigned_area: String? = null,
    val created_at: String,
    var total_delivered: Int = 0,
    val rating: Double = 4.8
)

data class ShopkeeperPermissions(
    val can_view_dashboard: Boolean = true,
    val can_add_product: Boolean = true,
    val can_edit_product: Boolean = true,
    val can_upload_images: Boolean = true,
    val can_view_catalog: Boolean = true,
    val can_stock_in: Boolean = true,
    val can_stock_out: Boolean = true,
    val can_view_inventory: Boolean = true,
    val can_view_orders: Boolean = true,
    val can_view_stock_history: Boolean = true,
    val can_edit_price: Boolean = true,
    val can_edit_category: Boolean = false,
    val can_edit_images: Boolean = true
)

data class Shopkeeper(
    val id: String,
    val shopkeeper_id: String,
    val name: String,
    val store_name: String? = null,
    val mobile: String,
    val email: String? = null,
    val city: String? = null,
    val status: String = "ACTIVE",
    val created_at: String,
    val permissions: ShopkeeperPermissions = ShopkeeperPermissions(),
    var total_products: Int = 0,
    var live_products: Int = 0,
    var pending_products: Int = 0,
    var current_stock: Int = 0,
    var total_orders: Int = 0
)

data class StockTransaction(
    val id: String = UUID.randomUUID().toString(),
    val transaction_id: String,
    val product_id: String,
    val product_name: String,
    val sku: String,
    val shopkeeper_id: String,
    val shopkeeper_name: String,
    val transaction_type: String, // "IN", "OUT", "ORDER_STOCK_OUT", "RETURN_STOCK_IN"
    val quantity: Int,
    val previous_stock: Int,
    val new_stock: Int,
    val reason: String? = null,
    val performed_by: String, // "ADMIN", "SHOPKEEPER"
    val timestamp: String
)

data class AuthSession(
    val userId: String,
    val role: String, // "CUSTOMER", "DELIVERY_BOY", "ADMIN", "SHOPKEEPER"
    val mobile: String,
    val name: String,
    val email: String? = null,
    val token: String,
    val authenticated_at: String,
    val expires_at: Long
)

data class StoreSettings(
    val store_name: String = "TRYatHOME",
    val store_tagline: String = "India’s Modern Garment & Fashion Destination • Try at Home",
    val contact_email: String = "care@tryathome.in",
    val contact_phone: String = "+91 98765 43210",
    val delivery_charge: Double = 49.0,
    val free_delivery_threshold: Double = 499.0,
    val cod_enabled: Boolean = true,
    val online_payment_enabled: Boolean = true,
    val min_order_value: Double = 199.0,
    val gst_percentage: Double = 5.0,
    val currency: String = "INR",
    val currency_symbol: String = "₹",
    val try_at_home_duration_minutes: Int = 30,
    val try_at_home_auto_close_on_expiry: Boolean = true,
    val try_at_home_charge: Double = 99.0
)

data class DashboardStats(
    val today_orders: Int = 0,
    val today_sales: Double = 0.0,
    val total_orders: Int = 0,
    val total_customers: Int = 0,
    val total_products: Int = 0,
    val out_of_stock_products: Int = 0,
    val low_stock_products: Int = 0,
    val pending_orders: Int = 0,
    val delivered_orders: Int = 0,
    val total_sales_amount: Double = 0.0
)
