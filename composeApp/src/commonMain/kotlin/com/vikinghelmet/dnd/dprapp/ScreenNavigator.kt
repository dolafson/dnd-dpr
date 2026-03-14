package com.vikinghelmet.dnd.dprapp

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController


@Composable
fun ScreenNavigator(viewModel: DprViewModel = viewModel { DprViewModel() },
                    navController: NavHostController = rememberNavController())
{
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = ViewType.valueOf(
        backStackEntry?.destination?.route ?: ViewType.main.name
    )
    Scaffold(){
        innerPadding ->
            val uiState by viewModel.uiState.collectAsState()

        NavHost(
            navController = navController,
            startDestination = ViewType.main.name,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
        ) {
            composable(route = ViewType.main.name) {
                MainView(
                    onCharacterButtonClicked = {
                        navController.navigate(ViewType.character.name)
                    },
                    onMonsterButtonClicked = {
                        navController.navigate(ViewType.monster.name)
                    },
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            }
            composable(route = ViewType.character.name) {
                CharacterScreen(
                    {
                        navController.navigate(ViewType.main.name)
                    },
                    { dialogSelectedValue ->
                        println("OK button clicked - character")
                        navController.navigate(ViewType.main.name)
                    }
                )
            }
            composable(route = ViewType.monster.name) {
                MonsterScreen(
                    {
                        navController.navigate(ViewType.main.name)
                    },
                    { dialogSelectedValue ->
                        println("OK button clicked - monster")
                        navController.navigate(ViewType.main.name)
                    })
            }
        }
    }
}