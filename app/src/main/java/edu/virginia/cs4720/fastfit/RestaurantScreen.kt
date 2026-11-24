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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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

// UVA-inspired palette matching the mockup
private val FastFitNavy = Color(0xFF13294B)
private val FastFitOrange = Color(0xFFE57200)
private val FastFitGreen = Color(0xFF4CAF50)
private val LightPageBg = Color(0xFFF5F6F8)

data class RestaurantItem(
    val id: String,
    val name: String,
    val badgeText: String,
    val badgeColor: Color
)

// Default placeholder data — add the rest of your restaurants here once collected
val defaultRestaurants = listOf(
    RestaurantItem(
        id = "default_1",
        name = "DEFAULT\nRESTAURANT",
        badgeText = "FF",
        badgeColor = FastFitOrange
    )
)

@Composable
fun RestaurantScreen(
    restaurants: List<RestaurantItem> = defaultRestaurants,
    onBackClick: () -> Unit = {},
    onRestaurantClick: (RestaurantItem) -> Unit = {},
    onReviewClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onFoodLogClick: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredRestaurants = remember(searchQuery, restaurants) {
        if (searchQuery.isBlank()) {
            restaurants
        } else {
            restaurants.filter {
                it.name.replace("\n", " ").contains(searchQuery.trim(), ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            RestaurantTopBar(onBackClick = onBackClick)
        },
        bottomBar = {
            RestaurantBottomBar(
                onHomeClick = onHomeClick,
                onFoodLogClick = onFoodLogClick
            )
        },
        containerColor = LightPageBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search restaurants",
                            tint = Color.DarkGray
                        )
                    },
                    placeholder = {
                        Text(
                            text = "SEARCH FOR A RESTAURANT",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = FastFitNavy,
                        unfocusedBorderColor = Color(0xFF4A4A4A)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3-Column Restaurant Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 84.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredRestaurants, key = { it.id }) { restaurant ->
                        RestaurantCard(
                            restaurant = restaurant,
                            onClick = { onRestaurantClick(restaurant) }
                        )
                    }
                }
            }

            // Bottom-right Review Action Button matching mockup
            ReviewFloatingButton(
                onClick = onReviewClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 14.dp, bottom = 10.dp)
            )
        }
    }
}

@Composable
private fun RestaurantTopBar(onBackClick: () -> Unit) {
    Surface(
        color = FastFitNavy,
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
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "FAST FIT\nRESTAURANTS",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
private fun RestaurantCard(
    restaurant: RestaurantItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.2.dp, Color(0xFF333333)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.78f)
            .clickable(onClick = onClick)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 10.dp)
        ) {
            Text(
                text = restaurant.name.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1A1A1A),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp,
                maxLines = 2
            )

            // Placeholder emblem — swap with Image(painterResource(...)) when logos are added
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(restaurant.badgeColor)
            ) {
                Text(
                    text = restaurant.badgeText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ReviewFloatingButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Surface(
            color = FastFitOrange,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 4.dp,
            modifier = Modifier.padding(top = 18.dp)
        ) {
            Text(
                text = "REVIEW",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        Surface(
            color = FastFitGreen,
            shape = CircleShape,
            shadowElevation = 4.dp,
            modifier = Modifier
                .size(34.dp)
                .offset(y = 0.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add review",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun RestaurantBottomBar(
    onHomeClick: () -> Unit,
    onFoodLogClick: () -> Unit
) {
    Surface(
        color = FastFitNavy,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onHomeClick)
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = FastFitOrange,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "HOME",
                    color = FastFitOrange,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onFoodLogClick)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Food Log / Favorites",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "FOOD LOG",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RestaurantScreenPreview() {
    RestaurantScreen()
}