package com.ttt.cinevibe.presentation.navigation

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ttt.cinevibe.ui.theme.Black
import com.ttt.cinevibe.ui.theme.LightGray
import com.ttt.cinevibe.ui.theme.NetflixRed
import com.ttt.cinevibe.ui.theme.White

@Composable
fun BottomNavBar(
    items: List<BottomNavItem>,
    selectedItemIndex: Int,
    onItemClick: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Black,
        contentColor = White
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = index == selectedItemIndex
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(index) },
                icon = {
                    Icon(
                        painter = painterResource(id = if (isSelected) item.selectedIcon else item.unselectedIcon),
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) NetflixRed else LightGray
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                        color = if (isSelected) NetflixRed else LightGray
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = NetflixRed,
                    unselectedIconColor = LightGray,
                    selectedTextColor = NetflixRed,
                    unselectedTextColor = LightGray,
                    indicatorColor = Black
                )
            )
        }
    }
}