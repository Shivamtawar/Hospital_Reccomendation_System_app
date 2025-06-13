package com.example.hospotalrecommedationsystem




import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.hospotalrecommedationsystem.data.model.NavItems
import com.example.hospotalrecommedationsystem.presentation.screens.HomeScreen
import com.example.hospotalrecommedationsystem.presentation.screens.ProfileScreen
import com.example.hospotalrecommedationsystem.presentation.screens.QuickCareScreen


@Composable
fun BottomNav(
    navController: NavController
) {

    val NavItemslist = listOf(
        NavItems("Home", Icons.Default.Home),
        NavItems("Specialties", Icons.Default.FlashOn),
        NavItems("Profile", Icons.Default.Person)




    )

    var selectedIndex = remember {
        mutableIntStateOf(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavItemslist.forEachIndexed { index, navItems ->
                    NavigationBarItem(
                        selected = selectedIndex.intValue == index ,
                        onClick = {
                            selectedIndex.intValue = index
                        },
                        icon = { Icon(navItems.icon, "") },
                        label = {
                            Text(text = navItems.label)
                        }
                    )
                }


            }
        }
    )
    { innerPadding ->
        contentScreen(modifier = Modifier.padding(innerPadding), selectedIndex.intValue, navController = navController )

    }

}

@Composable
fun contentScreen(modifier: Modifier = Modifier, selectedIndex: Int, navController: NavController) {


    when(selectedIndex){
        0 ->  HomeScreen(
            onNavigateToResults = { latitude, longitude, disease ->
                navController.navigate("results/$latitude/$longitude/$disease")
            }
        )

        1 -> QuickCareScreen(
            onNavigateToResults = { latitude, longitude, disease ->
                navController.navigate("results/$latitude/$longitude/$disease")
            }
        )
        2->ProfileScreen(navController)

//        3 -> ProfileScreen(
//            navController = navController
//        )


    }

}