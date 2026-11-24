package edu.virginia.cs4720.fastfit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val MenuNavy = Color(0xFF13294B)
private val MenuGreen = Color(0xFF3E7B48)
private val MenuBg = Color(0xFFF5F6F8)

enum class MenuFilter(val label: String) {
    MAX_CALORIES("MAX CALORIES"),
    HIGH_PROTEIN("HIGH PROTEIN"),
    LOW_CARB("LOW CARB")
}

data class MenuItem(
    val id: String,
    val restaurantId: String,
    val name: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int,
    val goalCalories: Int = 500
)

// Default placeholder item under the default restaurant — add more items later as you collect data
val defaultMenuItems = listOf(
    MenuItem(
        id = "default_item_1",
        restaurantId = "default_1",
        name = "DEFAULT GRILLED CHICKEN BOWL",
        calories = 410,
        proteinGrams = 42,
        carbsGrams = 22,
        fatGrams = 14,
        goalCalories = 500
    ),
    MenuItem(
        id = "default_item_2",
        restaurantId = "default_1",
        name = "LOW CARB TURKEY AVOCADO PLATE",
        calories = 340,
        proteinGrams = 34,
        carbsGrams = 7,
        fatGrams = 19,
        goalCalories = 500
    ),
    MenuItem(
        id = "default_item_3",
        restaurantId = "default_1",
        name = "MAX CALORIE CRISPY FEAST MEAL",
        calories = 780,
        proteinGrams = 28,
        carbsGrams = 68,
        fatGrams = 42,
        goalCalories = 500
    )
)

@Composable
fun MenuScreen(
    restaurantName: String = "Default Restaurant",
    restaurantId: String = "default_1",
    menuItems: List<MenuItem> = defaultMenuItems,
    onBackClick: () -> Unit = {},
    onAddToLogClick: (MenuItem) -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf(MenuFilter.HIGH_PROTEIN) }

    val displayedItems = remember(menuItems, restaurantId, selectedFilter) {
        val forRestaurant = menuItems.filter { it.restaurantId == restaurantId }
        val baseList = if (forRestaurant.isNotEmpty()) forRestaurant else menuItems

        when (selectedFilter) {
            MenuFilter.MAX_CALORIES -> baseList.sortedByDescending { it.calories }
            MenuFilter.HIGH_PROTEIN -> baseList.sortedByDescending { it.proteinGrams }
            MenuFilter.LOW_CARB -> baseList.sortedBy { it.carbsGrams }
        }
    }

    ScaffoldLayout(
        restaurantName = restaurantName,
        selectedFilter = selectedFilter,
        onSelectFilter = { selectedFilter = it },
        items = displayedItems,
        onBackClick = onBackClick,
        onAddToLogClick = onAddToLogClick
    )
}

@Composable
private fun ScaffoldLayout(
    restaurantName: String,
    selectedFilter: MenuFilter,
    onSelectFilter: (MenuFilter) -> Unit,
    items: List<MenuItem>,
    onBackClick: () -> Unit,
    onAddToLogClick: (MenuItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MenuBg)
    ) {
        // Top Navy Bar
        Surface(
            color = MenuNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to restaurants",
                        tint = Color.White
                    )
                }

                Text(
                    text = "${restaurantName.replace("\n", " ")} - Fast Fit",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 44.dp)
                )
            }
        }

        // Filter Pills Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            MenuFilter.entries.forEach { filter ->
                val isSelected = filter == selectedFilter
                Surface(
                    color = if (isSelected) MenuGreen else Color.White,
                    contentColor = if (isSelected) Color.White else Color(0xFF1A1A1A),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(
                        width = 1.4.dp,
                        color = if (isSelected) MenuGreen else Color(0xFF222222)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectFilter(filter) }
                ) {
                    Text(
                        text = filter.label,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Menu Items List
        LazyColumn(
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(items, key = { it.id }) { item ->
                MenuItemCard(
                    item = item,
                    onAddToLogClick = { onAddToLogClick(item) }
                )
            }
        }
    }
}

@Composable
private fun MenuItemCard(
    item: MenuItem,
    onAddToLogClick: () -> Unit
) {
    val progressFraction = (item.calories.toFloat() / item.goalCalories.coerceAtLeast(1).toFloat())
        .coerceIn(0f, 1f)

    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFCCCCCC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Placeholder meal thumbnail — swap with Image(...) when item photos are ready
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEAF0E6))
                ) {
                    Icon(
                        imageVector = Icons.Default.Restaurant,
                        contentDescription = item.name,
                        tint = MenuGreen,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A1A1A),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "CALORIES: ${item.calories}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MenuGreen
                    )
                    Text(
                        text = "PROTEIN: ${item.proteinGrams}g",
                        fontSize = 12.sp,
                        color = Color(0xFF222222)
                    )
                    Text(
                        text = "CARBS: ${item.carbsGrams}g",
                        fontSize = 12.sp,
                        color = Color(0xFF222222)
                    )
                    Text(
                        text = "FAT: ${item.fatGrams}g",
                        fontSize = 12.sp,
                        color = Color(0xFF222222)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onAddToLogClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MenuNavy),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "ADD TO LOG",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calorie value & goal bar matching the mockup
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${item.calories}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF222222)
                )
                Text(
                    text = "${item.goalCalories} cal",
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }

            Spacer(modifier = Modifier.height(3.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFD8DCE2))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MenuGreen)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuScreenPreview() {
    MenuScreen()
}