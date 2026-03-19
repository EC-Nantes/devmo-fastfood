package com.example.devmoproj

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.devmoproj.ui.theme.DEVMOProjTheme

// --- MODÈLES ---

enum class Priority(val label: String, val color: Color, val bgColor: Color, val icon: ImageVector) {
    CRITIQUE("Critique", Color(0xFFE91E63), Color(0xFFFFF1F2), Icons.Default.Warning),
    URGENT("Urgent", Color(0xFFFF9800), Color(0xFFFFF8E1), Icons.Default.Warning),
    NORMAL("En cours", Color(0xFF4CAF50), Color(0xFFF1F8E9), Icons.Default.CheckCircle)
}

data class Ingredient(
    val name: String,
    val isMissing: MutableState<Boolean> = mutableStateOf(false)
)

data class OrderItem(
    val name: String,
    val ingredients: List<Ingredient>
)

data class Order(
    val id: String,
    val customerName: String,
    val items: List<OrderItem>,
    val price: Double,
    val priority: Priority,
    val time: String,
    val duration: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DEVMOProjTheme {
                FastFoodApp()
            }
        }
    }
}

@Composable
fun FastFoodApp() {
    val orders = remember {
        mutableStateListOf(
            Order(
                "006", "Manon",
                listOf(
                    OrderItem("Burger Spicy", listOf(
                        Ingredient("Pain brioché"), 
                        Ingredient("Steak haché 150g"), 
                        Ingredient("Jalapeños"), 
                        Ingredient("Sauce piquante")
                    )),
                    OrderItem("Nuggets x6", listOf(Ingredient("Nuggets"), Ingredient("Sauce BBQ"))),
                    OrderItem("Orangina", listOf(Ingredient("Canette 33cl")))
                ),
                18.61, Priority.CRITIQUE, "08:58", "15:36"
            ),
            Order(
                "005", "Lucas",
                listOf(
                    OrderItem("Chicken Burger", listOf(Ingredient("Pain"), Ingredient("Poulet pané"))),
                    OrderItem("Frites L", listOf(Ingredient("Pommes de terre")))
                ),
                14.18, Priority.CRITIQUE, "08:57", "16:04"
            ),
            Order(
                "003", "Léa",
                listOf(OrderItem("Wrap BBQ", listOf(Ingredient("Galette"), Ingredient("Poulet"), Ingredient("Sauce BBQ")))),
                13.53, Priority.URGENT, "09:01", "12:04"
            ),
            Order(
                "004", "Théo",
                listOf(OrderItem("Wrap BBQ", listOf(Ingredient("Galette"))), OrderItem("Coleslaw", listOf(Ingredient("Chou")))),
                10.22, Priority.NORMAL, "09:08", "5:04"
            )
        )
    }

    var selectedOrder by remember { mutableStateOf<Order?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFF0D1117)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            HeaderSection(orders.size, orders.count { it.priority == Priority.CRITIQUE }, orders.count { it.priority == Priority.URGENT })

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                val sortedOrders = orders.sortedBy { 
                    when(it.priority) {
                        Priority.CRITIQUE -> 0
                        Priority.URGENT -> 1
                        Priority.NORMAL -> 2
                    }
                }
                items(sortedOrders) { order ->
                    OrderCard(order, onClick = { selectedOrder = order }, onComplete = { orders.remove(order) })
                }
            }
        }

        selectedOrder?.let { order ->
            OrderDetailDialog(
                order = order,
                onDismiss = { selectedOrder = null },
                onComplete = { 
                    orders.remove(order)
                    selectedOrder = null
                }
            )
        }
    }
}

@Composable
fun HeaderSection(total: Int, critiques: Int, urgentes: Int) {
    Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(40.dp).background(Color(0xFFFFC107), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Notifications, null, tint = Color.Black)
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("Cuisine", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text("Commandes en attente", color = Color.Gray, fontSize = 12.sp)
        }
        Spacer(Modifier.weight(1f))
        StatusBadge("$critiques critiques", Color(0xFFE91E63))
        Spacer(Modifier.width(8.dp))
        StatusBadge("$total commandes", Color(0xFF37474F))
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp), border = BorderStroke(1.dp, color.copy(alpha = 0.5f))) {
        Text(text, color = color, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OrderCard(order: Order, onClick: () -> Unit, onComplete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() }.border(2.dp, order.priority.color.copy(0.5f), RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = order.priority.bgColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("N° ${order.id}", color = Color.Gray, fontSize = 12.sp)
                Text(order.priority.label, color = order.priority.color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
            }
            Text(order.customerName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            order.items.take(3).forEach { Text("• ${it.name}", fontSize = 13.sp, color = Color.DarkGray) }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text("${order.duration} min", color = order.priority.color, fontWeight = FontWeight.Bold)
                Text("${order.price} €", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(12.dp))
            Button(onClick = { onComplete() }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = order.priority.color)) {
                Text("Commande prête", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun OrderDetailDialog(order: Order, onDismiss: () -> Unit, onComplete: () -> Unit) {
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.85f).padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = order.priority.bgColor)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Commande N° ${order.id}", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text(order.customerName, fontSize = 18.sp, color = Color.Gray)
                    }
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null) }
                }
                
                Spacer(Modifier.height(24.dp))
                
                Row(Modifier.fillMaxWidth().background(Color.White.copy(0.5f), RoundedCornerShape(12.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Heure", fontSize = 12.sp, color = Color.Gray)
                        Text(order.time, fontWeight = FontWeight.Bold)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Temps", fontSize = 12.sp, color = Color.Gray)
                        Text(order.duration, fontWeight = FontWeight.Bold, color = order.priority.color)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total", fontSize = 12.sp, color = Color.Gray)
                        Text("${order.price} €", fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(Modifier.height(24.dp))
                
                LazyColumn(modifier = Modifier.weight(1f)) {
                    order.items.forEach { item ->
                        item {
                            Text(item.name, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                        }
                        items(item.ingredients) { ingredient ->
                            IngredientRow(ingredient)
                        }
                    }
                }
                
                Spacer(Modifier.height(16.dp))
                
                Button(onClick = onComplete, modifier = Modifier.fillMaxWidth().height(56.dp), colors = ButtonDefaults.buttonColors(containerColor = order.priority.color)) {
                    Text("Commande prête", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun IngredientRow(ingredient: Ingredient) {
    val isMissing = ingredient.isMissing.value // Accès explicite pour la réactivité
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(ingredient.name, color = if (isMissing) Color.Red else Color.Black)
        
        Surface(
            onClick = { ingredient.isMissing.value = !isMissing },
            color = if (isMissing) Color.Red.copy(alpha = 0.1f) else Color.Transparent,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Warning, null, modifier = Modifier.size(16.dp), tint = if (isMissing) Color.Red else Color.LightGray)
                Spacer(Modifier.width(4.dp))
                Text("Manquant", color = if (isMissing) Color.Red else Color.LightGray, fontSize = 12.sp)
            }
        }
    }
}
