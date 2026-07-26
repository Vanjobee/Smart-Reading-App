package com.sgbread.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sgbread.app.audio.AudioManager
import com.sgbread.app.data.Modules
import com.sgbread.app.progress.ProgressViewModel
import com.sgbread.app.screens.home.HomeScreen
import com.sgbread.app.screens.home.ModuleScreen
import com.sgbread.app.screens.home.ProfileScreen
import com.sgbread.app.screens.module1.LetterBasketScreen
import com.sgbread.app.screens.module1.MatchCaseScreen
import com.sgbread.app.screens.module1.TraceLetterScreen
import com.sgbread.app.screens.module2.ListenMatchScreen
import com.sgbread.app.screens.module2.LetterHuntScreen
import com.sgbread.app.screens.module2.TapLetterScreen
import com.sgbread.app.screens.module3.BlendingMatchScreen
import com.sgbread.app.screens.module3.BlendReadScreen
import com.sgbread.app.screens.module3.MissingLetterScreen
import com.sgbread.app.screens.module4.DigraphBuildScreen
import com.sgbread.app.screens.module4.DigraphHuntScreen
import com.sgbread.app.screens.module4.PictureWordMatchScreen

object Routes {
    const val HOME = "home"
    const val MODULE = "module/{moduleId}"
    const val PROFILE = "profile"
    fun module(moduleId: String) = "module/$moduleId"
}

@Composable
fun SgbNavGraph(
    navController: NavHostController = rememberNavController(),
    progressViewModel: ProgressViewModel,
    audio: AudioManager
) {
    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                audio = audio,
                onModuleSelected = { moduleId -> navController.navigate(Routes.module(moduleId)) },
                onProfileSelected = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                progressViewModel = progressViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MODULE) { backStackEntry ->
            val moduleId = backStackEntry.arguments?.getString("moduleId")
            val module = Modules.all.firstOrNull { it.id == moduleId } ?: Modules.module1
            ModuleScreen(
                module = module,
                audio = audio,
                onActivitySelected = { route -> navController.navigate(route) },
                onProfileSelected = { navController.navigate(Routes.PROFILE) },
                onBack = { navController.popBackStack() }
            )
        }

        // Module 1: Letter Recognition
        composable("trace_letter") {
            TraceLetterScreen(audio, onComplete = { progressViewModel.completeActivity("m1a1"); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }
        composable("letter_basket") {
            LetterBasketScreen(audio, onComplete = { progressViewModel.completeActivity("m1a2"); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }
        composable("match_case") {
            MatchCaseScreen(audio, onComplete = { progressViewModel.completeActivity("m1a3"); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }

        // Module 2: Phonics
        composable("listen_match") {
            ListenMatchScreen(audio, onComplete = { progressViewModel.completeActivity("m2a1"); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }
        composable("tap_letter") {
            TapLetterScreen(audio, onComplete = { progressViewModel.completeActivity("m2a2"); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }
        composable("letter_hunt") {
            LetterHuntScreen(audio, onComplete = { progressViewModel.completeActivity("m2a3"); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }

        // Module 3: Blending
        composable("missing_letter") {
            MissingLetterScreen(audio, onComplete = { progressViewModel.completeActivity("m3a2"); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }
        composable("blend_read") {
            BlendReadScreen(audio, onComplete = { progressViewModel.completeActivity("m3a3"); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }
        composable("blending_match") {
            BlendingMatchScreen(audio, onComplete = { progressViewModel.completeActivity("m3a4"); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }

        // Module 4: Digraphs
        composable("digraph_build") {
            DigraphBuildScreen(audio, onComplete = { progressViewModel.completeActivity("m4a1"); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }
        composable("picture_word_match") {
            PictureWordMatchScreen(audio, onComplete = { progressViewModel.completeActivity("m4a2"); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }
        composable("digraph_hunt") {
            DigraphHuntScreen(audio, onComplete = { progressViewModel.completeActivity("m4a3"); navController.popBackStack() }, onBack = { navController.popBackStack() })
        }
    }
}
