package com.vikinghelmet.dnd.dprapp.screens

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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vikinghelmet.dnd.dpr.DprFiles
import com.vikinghelmet.dnd.dpr.util.Globals
import com.vikinghelmet.dnd.dpr.util.Settings
import com.vikinghelmet.dnd.dprapp.DprViewModel
import com.vikinghelmet.dnd.dprapp.ViewType
import com.vikinghelmet.dnd.dprapp.getDocumentsDirPath
import dpr.composeapp.generated.resources.Res

val dprFiles = DprFiles(getDocumentsDirPath())
val settings = Settings()

fun saveSettings(characterId: String, monsterName: String, proximity: Int) {
    settings.characterId = characterId
    settings.monsterName = monsterName
    settings.proximity = proximity
    DprFiles(getDocumentsDirPath()).saveSettings(settings)
}

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

    LaunchedEffect(Unit) {
        println("ScreenNavigator: launchedEffect")

        for (filename in mutableListOf("files/spells.json", "files/extra.spells.json")) {
            Globals.addSpells(Res.readBytes(filename).decodeToString())
        }
        Globals.addMonsters(Res.readBytes("files/monsters.json").decodeToString())

        dprFiles.init()
        try {
            settings.copy (other = dprFiles.getSettings())

            viewModel.setCharacterId(settings.characterId ?: "")
            viewModel.setMonsterName(settings.monsterName ?: "")
            viewModel.setProximity(settings.proximity ?: 0)
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
                    dprUiState = uiState,
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
                    dprUiState = uiState,
                    {
                        navController.navigate(ViewType.main.name)
                    },
                    { dialogSelectedValue ->
                        println("OK button clicked - character")
                        viewModel.setCharacterId(dialogSelectedValue)
                        navController.navigate(ViewType.main.name)
                    }
                )
            }
            composable(route = ViewType.monster.name) {
                MonsterScreen(
                    dprUiState = uiState,
                    {
                        navController.navigate(ViewType.main.name)
                    },
                    { dialogSelectedValue ->
                        println("OK button clicked - monster")
                        viewModel.setMonsterName(dialogSelectedValue)
                        navController.navigate(ViewType.main.name)
                    })
            }
        }
    }
}