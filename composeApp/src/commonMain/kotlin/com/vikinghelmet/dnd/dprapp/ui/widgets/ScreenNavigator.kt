package com.vikinghelmet.dnd.dprapp.ui.widgets

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vikinghelmet.dnd.dpr.DprFiles
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.ViewType
import com.vikinghelmet.dnd.dprapp.data.Loader
import com.vikinghelmet.dnd.dprapp.getDocumentsDirPath
import com.vikinghelmet.dnd.dprapp.ui.screens.*
import dpr.composeapp.generated.resources.Res

val dprFiles = DprFiles(getDocumentsDirPath())

fun saveSettings(viewModel: DprViewModel) {
    DprFiles(getDocumentsDirPath()).saveSettings(viewModel.uiState.value.getSettings())
}

@Composable
fun ScreenNavigator(viewModel: DprViewModel = viewModel { DprViewModel() },
                    navController: NavHostController = rememberNavController())
{
    // Get current back stack entry
    // val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    // val currentScreen = ViewType.valueOf(backStackEntry?.destination?.route ?: ViewType.main.name)

    LaunchedEffect(Unit) {
        println("ScreenNavigator: launchedEffect")

        for (filename in mutableListOf("files/spells.json", "files/extra.spells.json")) {
            Globals.addSpells(Res.readBytes(filename).decodeToString())
        }
        Globals.addMonsters(Res.readBytes("files/monsters.json").decodeToString())

        dprFiles.init()
        try {
            val settings = dprFiles.getSettings()
            viewModel.setProximity (settings.proximity)
            viewModel.setMainCharacter (Loader.getEditableCharacter(settings.characterName))
            viewModel.setMainMonster (Loader.getMonster(settings.monsterName))
        }
        catch (e: Exception) {
            println("unable to load settings: $e")
        }
    }

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
                MainScreen(
                    viewModel = viewModel,
                    onCharacterButtonClicked = {
                        navController.navigate(ViewType.character.name)
                    },
                    onMonsterButtonClicked = {
                        navController.navigate(ViewType.monster.name)
                    },
                    onAttackButtonClicked = { dialogSelectedValue ->
                        println("OK button clicked - attack")

                        viewModel.setProximity(dialogSelectedValue)
                        saveSettings(viewModel)

                        // no navigation needed here, stay on main screen
                    },
                    onMoneyButtonClicked = {
                        navController.navigate(ViewType.money.name)
                    },
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                )
            }
            composable(route = ViewType.character.name) {
                CharacterScreen(
                    viewModel = viewModel,
                    {
                        navController.navigate(ViewType.main.name)
                    },
                    { selectedEditableCharacter ->
                        println("PLAN button clicked")
                        navController.navigate(ViewType.plan.name)
                    },
                    { selectedEditableCharacter ->
                        println("OK button clicked - character")

                        viewModel.setMainCharacter(selectedEditableCharacter)
                        saveSettings(viewModel)

                        navController.navigate(ViewType.main.name)
                    }
                )
            }
            composable(route = ViewType.monster.name) {
                MonsterScreen(
                    viewModel = viewModel,
                    {
                        navController.navigate(ViewType.main.name)
                    },
                    { dialogSelectedValue ->
                        println("OK button clicked - monster")

                        viewModel.setMainMonster(viewModel.getCurrentMonster())
                        saveSettings(viewModel)

                        navController.navigate(ViewType.main.name)
                    })
            }
            composable(route = ViewType.money.name) {
                MoneyScreen({  navController.navigate(ViewType.main.name) })
            }
            composable(route = ViewType.plan.name) {
                PlanningScreen(
                    viewModel,
                    // TODO: diff behavior for dismiss/confirm ?
                    { navController.navigate(ViewType.character.name) },
                    { navController.navigate(ViewType.character.name) }
                )
            }
        }
    }
}