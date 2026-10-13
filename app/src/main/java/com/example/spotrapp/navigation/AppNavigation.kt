package com.example.spotrapp.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.spotrapp.data.repository.Resource
import com.example.spotrapp.data.local.HistoryAction
import com.example.spotrapp.ui.dashboard.DashboardScreen
import com.example.spotrapp.ui.history.HistoryScreen
import com.example.spotrapp.ui.onboarding.OnboardingScreen
import com.example.spotrapp.ui.putaway.AddItemScreen
import com.example.spotrapp.ui.putaway.CameraScanScreen
import com.example.spotrapp.ui.retrieval.EditItemScreen
import com.example.spotrapp.ui.retrieval.ItemDetailsScreen
import com.example.spotrapp.ui.search.SearchScreen
import com.example.spotrapp.viewmodel.ItemViewModel
import androidx.core.content.edit

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
                    onboardingPreferences.edit {
                        putBoolean("onboarding_completed", true)
                    }

                    activeTutorialStep = null
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                },
                onStartTutorial = {
                    onboardingPreferences.edit {
                        putBoolean("onboarding_completed", true)
                    }

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
                    activeTutorialStep = Routes.ADD_ITEM
                    navController.navigate(Routes.addItem())
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
                onVoiceSearchClick = { },
                onItemClick = { item -> navController.navigate(Routes.itemDetails(item.id)) },
                incomingVoiceQuery = voiceQuery.value,
                onVoiceQueryConsumed = {
                    backStackEntry.savedStateHandle["voiceQuery"] = null
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
                onItemClick = { itemId, action ->
                    navController.navigate(Routes.itemDetails(itemId) + "?historyAction=${action.name}")
                }
            )
        }

        composable(Routes.PUTAWAY) {
            CameraScanScreen(
                onBackClick = { navController.popBackStack() },
                onPhotoCaptured = { photoPath ->
                    navController.currentBackStackEntry
                        ?.savedStateHandle
                        ?.set("newItemPhotoPath", photoPath)
                    navController.navigate(Routes.addItem())
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
            val photoPath = remember(backStackEntry) {
                navController.previousBackStackEntry
                    ?.savedStateHandle
                    ?.remove<String>("newItemPhotoPath")
            }

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
            route = Routes.ITEM_DETAILS + "?historyAction={historyAction}",
            arguments = listOf(
                navArgument("itemId") { type = NavType.IntType },
                navArgument("historyAction") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments?.getInt("itemId") ?: -1
            val itemViewModel: ItemViewModel = hiltViewModel()
            val itemState by itemViewModel.itemState.collectAsStateWithLifecycle()
            val item = (itemState as? Resource.Success)?.data?.find { it.id == itemId }
            var lastKnownItem by remember(backStackEntry) {
                mutableStateOf<com.example.spotrapp.data.local.ItemEntity?>(null)
            }

            androidx.compose.runtime.LaunchedEffect(item) {
                if (item != null) {
                    lastKnownItem = item
                }
            }

            val itemToDisplay = item ?: lastKnownItem

            when {
                itemToDisplay != null -> ItemDetailsScreen(
                    item = itemToDisplay,
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
                itemState is Resource.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                else -> {
                    com.example.spotrapp.ui.common.StatusPopup(
                        message = "This item is no longer in your active inventory.",
                        icon = androidx.compose.material.icons.Icons.Filled.Warning,
                        onDismiss = { navController.popBackStack() }
                    )
                }
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
