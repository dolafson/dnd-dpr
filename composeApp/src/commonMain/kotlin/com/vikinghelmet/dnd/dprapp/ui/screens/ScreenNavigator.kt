package com.vikinghelmet.dnd.dprapp.ui.screens

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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vikinghelmet.dnd.dpr.util.DprFiles
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.ViewType
import com.vikinghelmet.dnd.dprapp.data.Loader
import com.vikinghelmet.dnd.dprapp.getDocumentsDirPath
import dpr.composeapp.generated.resources.Res

val dprFiles = DprFiles(getDocumentsDirPath())

fun saveSettings(viewModel: DprViewModel) {
    DprFiles(getDocumentsDirPath()).saveSettings(viewModel.uiState.value.getSettings())
}

@Composable
fun ScreenNavigator(viewModel: DprViewModel = viewModel { DprViewModel() },
                    navController: NavHostController = rememberNavController())
{
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
            viewModel.setCurrentCharacter (viewModel.getMainCharacter())
            viewModel.setMainMonster (Globals.getMonsterOrNull(settings.monsterName))
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
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(innerPadding)
        ) {
            composable(route = ViewType.main.name) {
                MainScreen(viewModel, navController)
            }
            composable(route = ViewType.character.name) {
                CharacterScreen(viewModel, navController)
            }
            composable(route = ViewType.monster.name) {
                MonsterScreen(viewModel, navController)
            }
            composable(route = ViewType.money.name) {
                MoneyScreen(navController)
            }
            composable(route = ViewType.plan.name) {
                PlanningScreen(viewModel, navController)
            }
        }
    }
}