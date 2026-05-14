package com.raitha.bharosa

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.raitha.bharosa.model.*
import com.raitha.bharosa.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

// ════════════════════════════════════════════════════════════
//  GOVERNMENT SCHEME PORTAL
// ════════════════════════════════════════════════════════════

@Composable
fun GovtSchemesScreen(
    language: Language,
    t: Map<String, String>,
    onBack: () -> Unit
) {
    val schemes = remember { DataGenerator.getGovernmentSchemes() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Subsidy", "Insurance", "Support")
    val categoryLabels = mapOf(
        "All" to (t["allCategories"] ?: "All"),
        "Subsidy" to (t["subsidy"] ?: "Subsidy"),
        "Insurance" to (t["insurance"] ?: "Insurance"),
        "Support" to (t["support"] ?: "Support")
    )

    val filtered = remember(searchQuery, selectedCategory, schemes) {
        schemes.filter { scheme ->
            val matchesSearch = searchQuery.isBlank() ||
                    scheme.name.contains(searchQuery, ignoreCase = true) ||
                    scheme.shortName.contains(searchQuery, ignoreCase = true) ||
                    scheme.description.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || scheme.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(EarthBrown)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = androidx.compose.ui.graphics.Color.White)
                }
                Text(
                    t["govtSchemes"] ?: "Govt Schemes",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(t["searchSchemes"] ?: "Search schemes...", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f), fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HarvestGold) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f))
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = HarvestGold,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f),
                    focusedTextColor = androidx.compose.ui.graphics.Color.White,
                    unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
                    cursorColor = HarvestGold
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Category chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    val (catColor, catBg) = when (cat) {
                        "Subsidy" -> Pair(SeedGreen, SeedGreen.copy(alpha = 0.15f))
                        "Insurance" -> Pair(ClayRed, ClayRed.copy(alpha = 0.15f))
                        "Support" -> Pair(SkyBlue, SkyBlue.copy(alpha = 0.15f))
                        else -> Pair(HarvestGold, HarvestGold.copy(alpha = 0.15f))
                    }
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) catColor else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f),
                                RoundedCornerShape(20.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) catColor else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            categoryLabels[cat] ?: cat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Scheme cards
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "${filtered.size} ${if (language == Language.en) "schemes found" else "ಯೋಜನೆಗಳು ಕಂಡುಬಂದಿವೆ"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EarthBrown.copy(alpha = 0.4f)
                )
            }
            items(filtered) { scheme ->
                SchemeCard(scheme, t)
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun SchemeCard(scheme: GovernmentScheme, t: Map<String, String>) {
    var expanded by remember { mutableStateOf(false) }

    val catColor = when (scheme.category) {
        "Subsidy" -> SeedGreen
        "Insurance" -> ClayRed
        else -> SkyBlue
    }
    val catIcon: ImageVector = when (scheme.category) {
        "Subsidy" -> Icons.Default.AttachMoney
        "Insurance" -> Icons.Default.Security
        else -> Icons.Default.SupportAgent
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(catColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(catIcon, contentDescription = null, tint = catColor, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            scheme.shortName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = EarthBrown
                        )
                        Box(
                            modifier = Modifier
                                .background(catColor.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(scheme.category, fontSize = 9.sp, fontWeight = FontWeight.Black, color = catColor)
                        }
                    }
                }
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = EarthBrown.copy(alpha = 0.4f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                scheme.description,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = EarthBrown.copy(alpha = 0.7f),
                lineHeight = 18.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = EarthBrown.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Eligibility
                    SchemeSectionHeader(Icons.Default.Person, t["eligibility"] ?: "Eligibility", catColor)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        scheme.eligibility,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = EarthBrown.copy(alpha = 0.7f),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Benefits
                    SchemeSectionHeader(Icons.Default.Star, t["benefits"] ?: "Benefits", catColor)
                    Spacer(modifier = Modifier.height(6.dp))
                    scheme.benefits.forEach { benefit ->
                        Row(
                            modifier = Modifier.padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 5.dp)
                                    .size(6.dp)
                                    .background(catColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(benefit, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = EarthBrown.copy(alpha = 0.7f), lineHeight = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // How to Apply
                    SchemeSectionHeader(Icons.Default.HowToReg, t["howToApply"] ?: "How to Apply", catColor)
                    Spacer(modifier = Modifier.height(6.dp))
                    scheme.applySteps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(catColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${index + 1}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = androidx.compose.ui.graphics.Color.White
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(step, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = EarthBrown.copy(alpha = 0.7f), lineHeight = 18.sp, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { expanded = !expanded },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, catColor.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = catColor)
            ) {
                Text(
                    if (expanded) (t["collapse"] ?: "Collapse") else (t["learnMore"] ?: "Learn More"),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun SchemeSectionHeader(icon: ImageVector, label: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Black, color = EarthBrown)
    }
}


// ════════════════════════════════════════════════════════════
//  FARMERS BUYING STORE
// ════════════════════════════════════════════════════════════

@Composable
fun FarmStoreScreen(
    language: Language,
    t: Map<String, String>,
    onBack: () -> Unit
) {
    val products = remember { DataGenerator.getStoreProducts() }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var cart by remember { mutableStateOf<List<CartItem>>(emptyList()) }
    var showCart by remember { mutableStateOf(false) }
    var showOrderSuccess by remember { mutableStateOf(false) }

    val categories = listOf("All", "Seeds", "Fertilizers", "Tools")
    val categoryLabels = mapOf(
        "All" to (t["allProducts"] ?: "All"),
        "Seeds" to (t["seeds"] ?: "Seeds"),
        "Fertilizers" to (t["fertilizers"] ?: "Fertilizers"),
        "Tools" to (t["tools"] ?: "Tools")
    )

    val filtered = remember(searchQuery, selectedCategory) {
        products.filter { p ->
            val matchSearch = searchQuery.isBlank() || p.name.contains(searchQuery, ignoreCase = true) ||
                    p.description.contains(searchQuery, ignoreCase = true)
            val matchCat = selectedCategory == "All" || p.category == selectedCategory
            matchSearch && matchCat
        }
    }

    val cartCount = cart.sumOf { it.quantity }
    val cartTotal = cart.sumOf { it.product.price * it.quantity }

    fun addToCart(product: StoreProduct) {
        val existing = cart.find { it.product.id == product.id }
        cart = if (existing != null) {
            cart.map { if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it }
        } else {
            cart + CartItem(product, 1)
        }
    }

    fun removeFromCart(productId: String) {
        cart = cart.filter { it.product.id != productId }
    }

    fun updateQty(productId: String, newQty: Int) {
        if (newQty <= 0) {
            removeFromCart(productId)
        } else {
            cart = cart.map { if (it.product.id == productId) it.copy(quantity = newQty) else it }
        }
    }

    // Order Success Dialog
    if (showOrderSuccess) {
        AlertDialog(
            onDismissRequest = { showOrderSuccess = false; showCart = false; cart = emptyList() },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SeedGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(t["orderPlaced"] ?: "Order Placed!", fontWeight = FontWeight.Black, color = EarthBrown)
                }
            },
            text = {
                Text(
                    t["orderMsg"] ?: "Your order has been recorded. Contact your local dealer to complete purchase.",
                    fontSize = 13.sp,
                    color = EarthBrown.copy(alpha = 0.7f),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showOrderSuccess = false; showCart = false; cart = emptyList() },
                    colors = ButtonDefaults.buttonColors(containerColor = SeedGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(t["continueShopping"] ?: "Continue Shopping", fontWeight = FontWeight.Black)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showCart) {
        // ── Cart Screen ──────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmWhite)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EarthBrown)
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { showCart = false }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = androidx.compose.ui.graphics.Color.White)
                    }
                    Text(
                        t["cart"] ?: "Cart",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                }
            }

            if (cart.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ShoppingCartCheckout,
                            contentDescription = null,
                            tint = EarthBrown.copy(alpha = 0.2f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(t["cartEmpty"] ?: "Your cart is empty", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = EarthBrown.copy(alpha = 0.4f))
                    }
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        item {
                            Text(
                                t["checkout"] ?: "Checkout Summary",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = EarthBrown.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        items(cart) { item ->
                            CartItemCard(item, t,
                                onRemove = { removeFromCart(item.product.id) },
                                onQtyChange = { updateQty(item.product.id, it) }
                            )
                        }
                    }
                    // Order summary
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = EarthBrown)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(t["totalItems"] ?: "Total Items", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f))
                                Text("$cartCount items", fontSize = 12.sp, fontWeight = FontWeight.Black, color = androidx.compose.ui.graphics.Color.White)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(t["totalCost"] ?: "Total Cost", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f))
                                Text("₹$cartTotal", fontSize = 20.sp, fontWeight = FontWeight.Black, color = HarvestGold)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showOrderSuccess = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SeedGreen),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(t["placeOrder"] ?: "Place Order", fontSize = 16.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    } else {
        // ── Product Listing Screen ───────────────────────────
        Column(modifier = Modifier.fillMaxSize().background(WarmWhite)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EarthBrown)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = androidx.compose.ui.graphics.Color.White)
                        }
                        Text(
                            t["store"] ?: "Farm Store",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = androidx.compose.ui.graphics.Color.White
                        )
                    }
                    // Cart button with badge
                    Box {
                        IconButton(onClick = { showCart = true }) {
                            Icon(Icons.Default.ShoppingCart, contentDescription = "Cart", tint = HarvestGold, modifier = Modifier.size(28.dp))
                        }
                        if (cartCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(18.dp)
                                    .background(ClayRed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("$cartCount", fontSize = 9.sp, fontWeight = FontWeight.Black, color = androidx.compose.ui.graphics.Color.White)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Search
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(t["searchProducts"] ?: "Search products...", color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f), fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = HarvestGold) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HarvestGold,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.3f),
                        focusedTextColor = androidx.compose.ui.graphics.Color.White,
                        unfocusedTextColor = androidx.compose.ui.graphics.Color.White,
                        cursorColor = HarvestGold
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
                // Category chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        val catColor = when (cat) {
                            "Seeds" -> SeedGreen
                            "Fertilizers" -> HarvestGold
                            "Tools" -> SkyBlue
                            else -> androidx.compose.ui.graphics.Color.White
                        }
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) catColor else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(20.dp)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) catColor else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f),
                                    RoundedCornerShape(20.dp)
                                )
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Text(
                                categoryLabels[cat] ?: cat,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) androidx.compose.ui.graphics.Color.White else androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "${filtered.size} ${if (language == Language.en) "products" else "ಉತ್ಪನ್ನಗಳು"}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EarthBrown.copy(alpha = 0.4f)
                    )
                }
                items(filtered) { product ->
                    val cartQty = cart.find { it.product.id == product.id }?.quantity ?: 0
                    ProductCard(product, cartQty, t,
                        onAddToCart = { addToCart(product) },
                        onQtyChange = { updateQty(product.id, it) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: StoreProduct,
    cartQty: Int,
    t: Map<String, String>,
    onAddToCart: () -> Unit,
    onQtyChange: (Int) -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
    val catColor = when (product.category) {
        "Seeds" -> SeedGreen
        "Fertilizers" -> HarvestGold
        else -> SkyBlue
    }
    val catIcon: ImageVector = when (product.category) {
        "Seeds" -> Icons.Default.Spa
        "Fertilizers" -> Icons.Default.WaterDrop
        else -> Icons.Default.Build
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(catColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(catIcon, contentDescription = null, tint = catColor, modifier = Modifier.size(26.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(product.name, fontSize = 14.sp, fontWeight = FontWeight.Black, color = EarthBrown)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("₹${product.price}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = catColor)
                            Text(" / ${product.unit}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EarthBrown.copy(alpha = 0.4f))
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .background(catColor.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(product.category, fontSize = 9.sp, fontWeight = FontWeight.Black, color = catColor)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                product.description,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = EarthBrown.copy(alpha = 0.65f),
                lineHeight = 18.sp,
                maxLines = if (showDetails) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            AnimatedVisibility(visible = showDetails) {
                Column {
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = EarthBrown.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = catColor, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(t["usageInfo"] ?: "Usage", fontSize = 10.sp, fontWeight = FontWeight.Black, color = EarthBrown.copy(alpha = 0.4f))
                            Text(product.usage, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = EarthBrown.copy(alpha = 0.7f), lineHeight = 18.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showDetails = !showDetails }) {
                    Text(
                        if (showDetails) "Hide details" else "Details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = catColor.copy(alpha = 0.7f)
                    )
                }
                if (cartQty == 0) {
                    Button(
                        onClick = onAddToCart,
                        colors = ButtonDefaults.buttonColors(containerColor = catColor),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(t["addToCart"] ?: "Add to Cart", fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(catColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 4.dp)
                    ) {
                        IconButton(
                            onClick = { onQtyChange(cartQty - 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Minus", tint = catColor)
                        }
                        Text(
                            "$cartQty",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = EarthBrown,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(
                            onClick = { onQtyChange(cartQty + 1) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Plus", tint = catColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemCard(item: CartItem, t: Map<String, String>, onRemove: () -> Unit, onQtyChange: (Int) -> Unit) {
    val catColor = when (item.product.category) {
        "Seeds" -> SeedGreen
        "Fertilizers" -> HarvestGold
        else -> SkyBlue
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, fontSize = 13.sp, fontWeight = FontWeight.Black, color = EarthBrown)
                Text("₹${item.product.price} / ${item.product.unit}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = catColor)
                Text(
                    "Subtotal: ₹${item.product.price * item.quantity}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = EarthBrown
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(catColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = { onQtyChange(item.quantity - 1) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = "Minus", tint = catColor, modifier = Modifier.size(16.dp))
                    }
                    Text("${item.quantity}", fontSize = 14.sp, fontWeight = FontWeight.Black, color = EarthBrown, modifier = Modifier.padding(horizontal = 6.dp))
                    IconButton(onClick = { onQtyChange(item.quantity + 1) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Add, contentDescription = "Plus", tint = catColor, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(onClick = onRemove, contentPadding = PaddingValues(0.dp)) {
                    Text(t["remove"] ?: "Remove", fontSize = 10.sp, fontWeight = FontWeight.Black, color = ClayRed)
                }
            }
        }
    }
}


// ════════════════════════════════════════════════════════════
//  REPORT GENERATION MODULE
// ════════════════════════════════════════════════════════════

@Composable
fun FarmReportsScreen(
    profile: FarmerProfile,
    language: Language,
    t: Map<String, String>,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("raitha_prefs", Context.MODE_PRIVATE) }

    val soilN = prefs.getInt("soil_n", 45)
    val soilP = prefs.getInt("soil_p", 20)
    val soilK = prefs.getInt("soil_k", 30)
    val soilHealth = remember(soilN, soilP, soilK) { FertilizerCalculator.getSoilHealth(soilN, soilP, soilK) }
    val activities = remember { DataGenerator.generateHistoryItems() }
    val yieldHistory = remember(profile.crop) { DataGenerator.generateYieldHistory(profile.crop) }
    val marketPrices = remember { DataGenerator.generateMarketPrices() }
    val cropMarket = remember(profile.crop) { marketPrices.find { it.crop == profile.crop } }
    val dateStr = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date())
    }

    var isGenerating by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var lastSavedFile by remember { mutableStateOf<File?>(null) }

    fun generateAndSavePdf() {
        scope.launch {
            isGenerating = true
            statusMessage = ""
            try {
                val file = withContext(Dispatchers.IO) {
                    val document = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
                    val page = document.startPage(pageInfo)
                    val canvas: Canvas = page.canvas

                    val titlePaint = Paint().apply {
                        color = Color.parseColor("#5A4033")
                        textSize = 24f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    val headerPaint = Paint().apply {
                        color = Color.parseColor("#4F7942")
                        textSize = 14f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    val bodyPaint = Paint().apply {
                        color = Color.parseColor("#5A4033")
                        textSize = 11f
                        isAntiAlias = true
                    }
                    val labelPaint = Paint().apply {
                        color = Color.parseColor("#99785C")
                        textSize = 10f
                        isAntiAlias = true
                    }
                    val linePaint = Paint().apply {
                        color = Color.parseColor("#E8DDD0")
                        strokeWidth = 1f
                        isAntiAlias = true
                    }
                    val accentPaint = Paint().apply {
                        color = Color.parseColor("#DAA520")
                        textSize = 11f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }

                    var y = 60f
                    val leftMargin = 50f
                    val rightEdge = 545f

                    // Header background
                    val bgPaint = Paint().apply { color = Color.parseColor("#5A4033") }
                    canvas.drawRect(0f, 0f, 595f, 80f, bgPaint)
                    val whitePaint = Paint().apply {
                        color = Color.WHITE
                        textSize = 20f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    canvas.drawText("RAITHA-BHAROSA HUB", leftMargin, 35f, whitePaint)
                    val subPaint = Paint().apply {
                        color = Color.parseColor("#DAA520")
                        textSize = 11f
                        isAntiAlias = true
                    }
                    canvas.drawText("Farm Activity Report  •  $dateStr", leftMargin, 60f, subPaint)

                    y = 110f

                    fun drawSection(title: String, yPos: Float): Float {
                        canvas.drawText(title.uppercase(), leftMargin, yPos, headerPaint)
                        canvas.drawLine(leftMargin, yPos + 4f, rightEdge, yPos + 4f, linePaint)
                        return yPos + 20f
                    }

                    fun drawRow(label: String, value: String, yPos: Float): Float {
                        canvas.drawText(label, leftMargin, yPos, labelPaint)
                        canvas.drawText(value, 230f, yPos, bodyPaint)
                        return yPos + 18f
                    }

                    // Farmer Details
                    y = drawSection("Farmer Details", y)
                    y = drawRow("Name", profile.name, y)
                    y = drawRow("Crop", profile.crop.name, y)
                    y = drawRow("Land Size", "${profile.landSize} Acres", y)
                    y = drawRow("Language", if (profile.language == Language.en) "English" else "Kannada", y)
                    y = drawRow("Report Date", dateStr, y)
                    y += 14f

                    // Soil Health
                    y = drawSection("Soil Health Status", y)
                    y = drawRow("Nitrogen (N)", "$soilN%", y)
                    y = drawRow("Phosphorus (P)", "$soilP%", y)
                    y = drawRow("Potassium (K)", "$soilK%", y)
                    y = drawRow("Health Score", "${soilHealth.score}%", y)
                    y = drawRow("Fertility Status", "${soilHealth.status} Fertility", y)
                    if (soilHealth.deficiencies.isNotEmpty()) {
                        y = drawRow("Deficiencies", soilHealth.deficiencies.joinToString(", "), y)
                    }
                    y += 14f

                    // Activities
                    y = drawSection("Farm Activities", y)
                    activities.forEach { act ->
                        y = drawRow(act.date, "${act.type} — ${act.result}", y)
                    }
                    y += 14f

                    // Yield Estimation
                    y = drawSection("Yield Estimation", y)
                    yieldHistory.forEach { rec ->
                        val display = if (rec.actual > 0.0)
                            "Predicted: ${String.format("%.1f", rec.predicted)} q/ac  |  Actual: ${String.format("%.1f", rec.actual)} q/ac"
                        else
                            "Predicted: ${String.format("%.1f", rec.predicted)} q/ac  (Current Season)"
                        y = drawRow(rec.season, display, y)
                    }
                    y += 14f

                    // Market Snapshot
                    y = drawSection("Market Price Snapshot", y)
                    cropMarket?.let { mp ->
                        y = drawRow(profile.crop.name, "₹${mp.currentPrice}/q", y)
                        y = drawRow("Trend", mp.trend.replaceFirstChar { it.uppercase() }, y)
                        y = drawRow("Predicted Price", "₹${mp.predictedPrice}/q", y)
                        y = drawRow("Optimal Harvest", "In ${mp.optimalHarvestDays} days", y)
                    }
                    y += 20f

                    // Footer
                    val footerPaint = Paint().apply {
                        color = Color.parseColor("#99785C")
                        textSize = 9f
                        isAntiAlias = true
                    }
                    canvas.drawLine(leftMargin, 810f, rightEdge, 810f, linePaint)
                    canvas.drawText("Generated by Raitha-Bharosa Hub  •  Smart Farming Assistant  •  $dateStr", leftMargin, 828f, footerPaint)

                    document.finishPage(page)

                    // Save file to app-specific external storage to avoid permission issues on modern Android
                    val fileName = "RaithaBharosa_Report_${SimpleDateFormat("yyyyMMdd_HHmm", Locale.ENGLISH).format(Date())}.pdf"
                    val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    val outputFile = File(downloadsDir, fileName)
                    val fos = FileOutputStream(outputFile)
                    document.writeTo(fos)
                    fos.flush()
                    fos.close()
                    document.close()
                    outputFile
                }

                lastSavedFile = file
                statusMessage = "✓ ${t["reportSaved"] ?: "Report saved successfully"}!"
                isGenerating = false
            } catch (e: Exception) {
                statusMessage = "Error: ${e.message}"
                isGenerating = false
            }
        }
    }

    fun shareReport(context: Context) {
        val file = lastSavedFile
        if (file == null || !file.exists()) {
            statusMessage = if (language == Language.en)
                "Generate the report first, then share."
            else
                "ಮೊದಲು ವರದಿಯನ್ನು ತಯಾರಿಸಿ, ನಂತರ ಶೇರ್ ಮಾಡಿ."
            return
        }
        try {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Farm Report"))
        } catch (e: Exception) {
            statusMessage = "Error sharing: ${e.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(EarthBrown)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = androidx.compose.ui.graphics.Color.White)
                }
                Text(
                    t["reports"] ?: "Farm Reports",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = androidx.compose.ui.graphics.Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Report Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Article, contentDescription = null, tint = HarvestGold, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t["reportPreview"] ?: "Report Preview", fontWeight = FontWeight.Black, color = EarthBrown, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = EarthBrown.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Farmer Details Section ──
                    ReportSectionHeader(t["farmerDetails"] ?: "Farmer Details", SeedGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportRow("Name", profile.name)
                    ReportRow(t["selectCrop"] ?: "Crop", profile.crop.name)
                    ReportRow(t["landArea"] ?: "Land Area", "${profile.landSize} Acres")
                    ReportRow("Report Date", dateStr)

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Soil Health Section ──
                    ReportSectionHeader(t["soilStatus"] ?: "Soil Health Status", SeedGreen)
                    Spacer(modifier = Modifier.height(8.dp))
                    ReportRow("N / P / K", "$soilN% / $soilP% / $soilK%")
                    ReportRow(t["soilScore"] ?: "Health Score", "${soilHealth.score}%")
                    ReportRow(t["shcStatus"] ?: "Fertility", "${soilHealth.status} Fertility")
                    if (soilHealth.deficiencies.isNotEmpty()) {
                        ReportRow(t["deficiency"] ?: "Deficiencies", soilHealth.deficiencies.joinToString(", "))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Activities Section ──
                    ReportSectionHeader(t["activitySummary"] ?: "Activity Summary", HarvestGold)
                    Spacer(modifier = Modifier.height(8.dp))
                    activities.forEach { act ->
                        ReportRow(act.date, "${act.type} — ${act.result}")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Yield Section ──
                    ReportSectionHeader(t["yieldEstimation"] ?: "Yield Estimation", SkyBlue)
                    Spacer(modifier = Modifier.height(8.dp))
                    yieldHistory.forEach { rec ->
                        val label = if (rec.actual > 0.0)
                            "${String.format("%.1f", rec.predicted)} q/ac (pred)  /  ${String.format("%.1f", rec.actual)} q/ac (actual)"
                        else
                            "${String.format("%.1f", rec.predicted)} q/ac (forecast)"
                        ReportRow(rec.season, label)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ── Market Snapshot ──
                    cropMarket?.let { mp ->
                        ReportSectionHeader("Market Snapshot", ClayRed)
                        Spacer(modifier = Modifier.height(8.dp))
                        ReportRow(profile.crop.name, "₹${mp.currentPrice}/q  (${mp.trend})")
                        ReportRow("Predicted Price", "₹${mp.predictedPrice}/q")
                        ReportRow("Optimal Harvest", "In ${mp.optimalHarvestDays} days")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status message
            if (statusMessage.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (statusMessage.startsWith("✓")) SeedGreen.copy(alpha = 0.1f)
                        else ClayRed.copy(alpha = 0.1f)
                    )
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (statusMessage.startsWith("✓")) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (statusMessage.startsWith("✓")) SeedGreen else ClayRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            statusMessage,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (statusMessage.startsWith("✓")) SeedGreen else ClayRed,
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Action Buttons
            Button(
                onClick = { generateAndSavePdf() },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = EarthBrown),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = androidx.compose.ui.graphics.Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(t["generating"] ?: "Generating...", fontSize = 15.sp, fontWeight = FontWeight.Black)
                } else {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(t["generateReport"] ?: "Generate Report PDF", fontSize = 15.sp, fontWeight = FontWeight.Black)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = { shareReport(context) },
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(2.dp, EarthBrown.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, tint = EarthBrown)
                Spacer(modifier = Modifier.width(8.dp))
                Text(t["shareReport"] ?: "Share Report", fontSize = 15.sp, fontWeight = FontWeight.Black, color = EarthBrown)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReportSectionHeader(title: String, color: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.width(4.dp).height(16.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontSize = 13.sp, fontWeight = FontWeight.Black, color = EarthBrown)
    }
}

@Composable
private fun ReportRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EarthBrown.copy(alpha = 0.5f), modifier = Modifier.weight(0.42f))
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Black, color = EarthBrown, modifier = Modifier.weight(0.58f))
    }
}
