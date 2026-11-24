package edu.virginia.cs4720.fastfit

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

private val FavNavy = Color(0xFF13294B)
private val FavOrange = Color(0xFFE57200)
private val FavGreen = Color(0xFF4CAF50)
private val FavBg = Color(0xFFF5F6F8)

data class FavoriteMeal(
    val id: String,
    val restaurantName: String,
    val mealName: String,
    val calories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int
)

fun MenuItem.toFavoriteMeal(restaurantName: String = "Default Restaurant"): FavoriteMeal =
    FavoriteMeal(
        id = "${id}_${System.nanoTime()}",
        restaurantName = restaurantName.replace("\n", " "),
        mealName = name,
        calories = calories,
        proteinGrams = proteinGrams,
        carbsGrams = carbsGrams,
        fatGrams = fatGrams
    )

val defaultFavoriteMeals = listOf(
    FavoriteMeal(
        id = "fav_default_1",
        restaurantName = "Default Restaurant",
        mealName = "Default Grilled Chicken Bowl",
        calories = 410,
        proteinGrams = 42,
        carbsGrams = 22,
        fatGrams = 14
    )
)

private const val FAVORITES_PREFS_NAME = "fastfit_user_favorites"

fun loadUserFavorites(context: Context, userId: String?): List<FavoriteMeal> {
    if (userId.isNullOrBlank()) return emptyList()
    val prefs = context.getSharedPreferences(FAVORITES_PREFS_NAME, Context.MODE_PRIVATE)
    val rawJson = prefs.getString("favorites_$userId", null) ?: return emptyList()

    return try {
        val jsonArray = JSONArray(rawJson)
        List(jsonArray.length()) { index ->
            val obj = jsonArray.getJSONObject(index)
            FavoriteMeal(
                id = obj.getString("id"),
                restaurantName = obj.getString("restaurantName"),
                mealName = obj.getString("mealName"),
                calories = obj.getInt("calories"),
                proteinGrams = obj.getInt("proteinGrams"),
                carbsGrams = obj.getInt("carbsGrams"),
                fatGrams = obj.getInt("fatGrams")
            )
        }
    } catch (e: Exception) {
        emptyList()
    }
}

fun saveUserFavorites(context: Context, userId: String?, meals: List<FavoriteMeal>) {
    if (userId.isNullOrBlank()) return
    val jsonArray = JSONArray()
    meals.forEach { meal ->
        val obj = JSONObject().apply {
            put("id", meal.id)
            put("restaurantName", meal.restaurantName)
            put("mealName", meal.mealName)
            put("calories", meal.calories)
            put("proteinGrams", meal.proteinGrams)
            put("carbsGrams", meal.carbsGrams)
            put("fatGrams", meal.fatGrams)
        }
        jsonArray.put(obj)
    }
    context.getSharedPreferences(FAVORITES_PREFS_NAME, Context.MODE_PRIVATE)
        .edit()
        .putString("favorites_$userId", jsonArray.toString())
        .apply()
}

@Composable
fun FavoritesScreen(
    favoriteMeals: List<FavoriteMeal> = defaultFavoriteMeals,
    onRemoveFavorite: (FavoriteMeal) -> Unit = {},
    onClearFavorites: () -> Unit = {},
    onAddMoreClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onFoodLogClick: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FavBg)
    ) {
        // Top Navy Header
        Surface(
            color = FavNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "FAVORITES",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        // Scrollable Favorites List
        if (favoriteMeals.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "No favorites yet. Tap ADD MORE or ADD TO LOG on a menu item!",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(favoriteMeals, key = { it.id }) { meal ->
                    FavoriteMealCard(
                        meal = meal,
                        onHeartClick = { onRemoveFavorite(meal) },
                        onMapClick = {
                            val query = Uri.encode("${meal.restaurantName} near me")
                            val mapIntent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("geo:0,0?q=$query")
                            )
                            runCatching { context.startActivity(mapIntent) }
                        }
                    )
                }
            }
        }

        // Action Bar: CLEAR FAVORITES | SHARE | ADD MORE
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Button(
                onClick = onClearFavorites,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FavNavy),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    text = "CLEAR\nFAVORITES",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp
                )
            }

            Button(
                onClick = {
                    val shareText = if (favoriteMeals.isEmpty()) {
                        "Check out FastFit!"
                    } else {
                        "My FastFit Favorites:\n" + favoriteMeals.joinToString("\n") {
                            "- ${it.mealName} (${it.restaurantName}): ${it.calories} cal, ${it.proteinGrams}g protein, ${it.carbsGrams}g carbs, ${it.fatGrams}g fat"
                        }
                    }
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    runCatching {
                        context.startActivity(
                            Intent.createChooser(shareIntent, "Share FastFit Favorites")
                        )
                    }
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FavNavy),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "SHARE",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp
                    )
                }
            }

            Button(
                onClick = onAddMoreClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FavOrange),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(
                    text = "ADD\nMORE",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp
                )
            }
        }

        // Bottom Navigation Bar: HOME | FOOD LOG
        Surface(
            color = FavNavy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onHomeClick)
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "HOME",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onFoodLogClick)
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Food Log",
                        tint = FavOrange,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "FOOD LOG",
                        color = FavOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteMealCard(
    meal: FavoriteMeal,
    onHeartClick: () -> Unit,
    onMapClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFCCCCCC)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEAF0E6))
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = meal.mealName,
                    tint = Color(0xFF3E7B48),
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = meal.mealName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "CALORIES: ${meal.calories}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FavGreen
                )
                Text(
                    text = "PROTEIN: ${meal.proteinGrams}g",
                    fontSize = 12.sp,
                    color = Color(0xFF222222)
                )
                Text(
                    text = "CARBS: ${meal.carbsGrams}g   FAT: ${meal.fatGrams}g",
                    fontSize = 12.sp,
                    color = Color(0xFF222222)
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onHeartClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Remove from favorites",
                        tint = FavGreen,
                        modifier = Modifier.size(26.dp)
                    )
                }

                IconButton(
                    onClick = onMapClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Find restaurant on map",
                        tint = FavGreen,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FavoritesScreenPreview() {
    FavoritesScreen()
}