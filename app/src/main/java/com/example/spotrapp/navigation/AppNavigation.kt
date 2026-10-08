package com.example.spotrapp.navigation

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.spotrapp.data.repository.Resource
import com.example.spotrapp.ui.dashboard.DashboardScreen
import com.example.spotrapp.ui.history.HistoryScreen
import com.example.spotrapp.ui.onboarding.OnboardingScreen
import com.example.spotrapp.ui.putaway.AddItemScreen
import com.example.spotrapp.ui.putaway.CameraScanScreen
import com.example.spotrapp.ui.retrieval.EditItemScreen
import com.example.spotrapp.ui.retrieval.ItemDetailsScreen
import com.example.spotrapp.ui.search.SearchScreen
import com.example.spotrapp.ui.search.VoiceSearchScreen
import com.example.spotrapp.viewmodel.ItemViewModel

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val onboardingPreferences =
        remember {
            context.getSharedPreferences(
                "spotr_preferences",
                Context.MODE_PRIVATE
            )
        }

    var activeTutorialStep by remember { mutableStateOf<String?>(null) }

    val onboardingCompleted =
        onboardingPreferences.getBoolean(
            "onboarding_completed",
            false
        )

    NavHost(
        navController = navController,
        startDestination =
            if (onboardingCompleted) {
                Routes.DASHBOARD
            } else {
                Routes.ONBOARDING
            },
        modifier = modifier
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onBoardingFinished = {
                    onboardingPreferences.edit()
                        .putBoolean("onboarding_completed", true)
                        .apply()

                    activeTutorialStep = null
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onStartTutorial = {
                    onboardingPreferences.edit()
                        .putBoolean("onboarding_completed", true)
                        .apply()

                    activeTutorialStep = Routes.DASHBOARD
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onItemClick = { item ->
                    navController.navigate(Routes.itemDetails(item.id))
                },
                showTutorial = activeTutorialStep == Routes.DASHBOARD,
                onShowTutorial = {
                    activeTutorialStep = Routes.DASHBOARD
                },
                onDismissTutorial = {
                    activeTutorialStep = Routes.PUTAWAY
                    navController.navigate(Routes.PUTAWAY)
                },
                onSearchClick = { navController.navigate(Routes.SEARCH) },
                onHistoryClick = { navController.navigate(Routes.HISTORY) },
                onScanClick = { navController.navigate(Routes.PUTAWAY) }
            )
        }

        composable(Routes.SEARCH) { backStackEntry ->
            val voiceQuery = backStackEntry.savedStateHandle
                .getStateFlow<String?>("voiceQuery", null)
                .collectAsStateWithLifecycle()

            SearchScreen(
                onHomeClick = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onScanClick = { navController.navigate(Routes.PUTAWAY) },
                onHistoryClick = { navController.navigate(Routes.HISTORY) },
                onVoiceSearchClick = { navController.navigate(Routes.VOICE_SEARCH) },
                onItemClick = { item -> navController.navigate(Routes.itemDetails(item.id)) },
                incomingVoiceQuery = voiceQuery.value,
                onVoiceQueryConsumed = {
                    backStackEntry.savedStateHandle["voiceQuery"] = null
                }
            )
        }

        composable(Routes.VOICE_SEARCH) {
            VoiceSearchScreen(
                onBackClick = { navController.popBackStack() },
                onResult = { recognizedText ->
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "voiceQuery",
                        recognizedText
                    )
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onHomeClick = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onScanClick = { navController.navigate(Routes.PUTAWAY) },
                onItemClick = { itemId -> navController.navigate(Routes.itemDetails(itemId)) }
            )
        }

        composable(Routes.PUTAWAY) {
            CameraScanScreen(
                onBackClick = { navController.popBackStack() },
                onPhotoCaptured = { photoPath ->
                    navController.navigate(Routes.addItem(Uri.encode(photoPath)))
                },
                showTutorial = activeTutorialStep == Routes.PUTAWAY,
                onDismissTutorial = {
                    activeTutorialStep = Routes.ADD_ITEM
                    navController.navigate(Routes.addItem())
                }
            )
        }

        composable(
            route = Routes.ADD_ITEM,
            arguments = listOf(
                navArgument("photoPath") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val encodedPhotoPath = backStackEntry.arguments?.getString("photoPath")
            val photoPath = encodedPhotoPath?.let { Uri.decode(it) }

            AddItemScreen(
                onBack = { navController.popBackStack() },
                onSaved = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                showTutorial = activeTutorialStep == Routes.ADD_ITEM,
                onTutorialComplete = {
                    activeTutorialStep = null
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                photoPath = photoPath
            )
        }

        composable(
            route = Routes.ITEM_DETAILS,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: -1
            val itemViewModel: ItemViewModel = hiltViewModel()
            val itemState by itemViewModel.itemState.collectAsStateWithLifecycle()
            val item = (itemState as? Resource.Success)?.data?.find { it.id == itemId }

            if (item != null) {
                ItemDetailsScreen(
                    item = item,
                    onBack = { navController.popBackStack() },
                    onEditClick = { selectedItem ->
                        navController.navigate(Routes.editItem(selectedItem.id))
                    },
                    onRetrieved = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.DASHBOARD) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onDeleted = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.DASHBOARD) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(
            route = Routes.EDIT_ITEM,
            arguments = listOf(navArgument("itemId") { type = NavType.IntType })
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: -1
            val itemViewModel: ItemViewModel = hiltViewModel()
            val itemState by itemViewModel.itemState.collectAsStateWithLifecycle()
            val item = (itemState as? Resource.Success)?.data?.find { it.id == itemId }

            if (item != null) {
                EditItemScreen(
                    item = item,
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.DASHBOARD) { inclusive = false }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
