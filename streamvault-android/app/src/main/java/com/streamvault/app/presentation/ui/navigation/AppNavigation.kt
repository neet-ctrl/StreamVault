package com.streamvault.app.presentation.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.*
import androidx.navigation.compose.*
import com.streamvault.app.domain.model.Stream
import com.streamvault.app.presentation.ui.screens.details.MovieDetailsScreen
import com.streamvault.app.presentation.ui.screens.downloads.DownloadsScreen
import com.streamvault.app.presentation.ui.screens.home.HomeScreen
import com.streamvault.app.presentation.ui.screens.library.LibraryScreen
import com.streamvault.app.presentation.ui.screens.player.PlayerScreen
import com.streamvault.app.presentation.ui.screens.search.SearchScreen
import com.streamvault.app.presentation.ui.screens.settings.SettingsScreen
import com.streamvault.app.presentation.ui.screens.splash.SplashScreen
import com.streamvault.app.presentation.ui.screens.streams.StreamSourcesScreen
import com.streamvault.app.presentation.ui.theme.*

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Home : Screen("home")
    object Search : Screen("search")
    object Details : Screen("details/{movieId}/{isTv}") {
        fun createRoute(movieId: Int, isTv: Boolean) = "details/$movieId/$isTv"
    }
    object StreamSources : Screen("streams/{imdbId}") {
        fun createRoute(imdbId: String) = "streams/$imdbId"
    }
    object Player : Screen("player/{movieId}/{title}") {
        fun createRoute(movieId: Int, title: String) = "player/$movieId/${title.take(50)}"
    }
    object Downloads : Screen("downloads")
    object Library : Screen("library")
    object Settings : Screen("settings")
}

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector = icon
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Home", Icons.Default.Home),
    BottomNavItem(Screen.Search, "Search", Icons.Default.Search),
    BottomNavItem(Screen.Library, "Library", Icons.Default.BookmarkBorder, Icons.Default.Bookmark),
    BottomNavItem(Screen.Downloads, "Downloads", Icons.Default.Download),
    BottomNavItem(Screen.Settings, "Settings", Icons.Default.Settings)
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }
    var pendingStream by remember { mutableStateOf<Pair<Stream, Pair<Int, String>>?>(null) }

    if (showSplash) {
        SplashScreen(onSplashComplete = { showSplash = false })
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.screen.route == currentRoute }

    Scaffold(
        containerColor = AmoledBlack,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = BlackSurface,
                    contentColor = AccentRed,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    navController.navigate(item.screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = AccentRed,
                                selectedTextColor = AccentRed,
                                unselectedIconColor = TextTertiary,
                                unselectedTextColor = TextTertiary,
                                indicatorColor = AccentRed.copy(alpha = 0.15f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(AmoledBlack),
            enterTransition = { slideInHorizontally { it } + fadeIn() },
            exitTransition = { slideOutHorizontally { -it } + fadeOut() },
            popEnterTransition = { slideInHorizontally { -it } + fadeIn() },
            popExitTransition = { slideOutHorizontally { it } + fadeOut() }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onMovieClick = { id, isTv ->
                        navController.navigate(Screen.Details.createRoute(id, isTv))
                    },
                    onPlayClick = { movie ->
                        movie.imdbId?.let { imdbId ->
                            navController.navigate(Screen.StreamSources.createRoute(imdbId))
                        }
                    }
                )
            }

            composable(Screen.Search.route) {
                SearchScreen(
                    onMovieClick = { id, isTv ->
                        navController.navigate(Screen.Details.createRoute(id, isTv))
                    }
                )
            }

            composable(
                Screen.Details.route,
                arguments = listOf(
                    navArgument("movieId") { type = NavType.IntType },
                    navArgument("isTv") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable
                val isTv = backStackEntry.arguments?.getBoolean("isTv") ?: false
                MovieDetailsScreen(
                    movieId = movieId,
                    isTv = isTv,
                    onBack = { navController.popBackStack() },
                    onStreamClick = { imdbId ->
                        navController.navigate(Screen.StreamSources.createRoute(imdbId))
                    },
                    onSimilarClick = { id, tv ->
                        navController.navigate(Screen.Details.createRoute(id, tv))
                    }
                )
            }

            composable(
                Screen.StreamSources.route,
                arguments = listOf(navArgument("imdbId") { type = NavType.StringType })
            ) { backStackEntry ->
                val imdbId = backStackEntry.arguments?.getString("imdbId") ?: return@composable
                StreamSourcesScreen(
                    imdbId = imdbId,
                    onBack = { navController.popBackStack() },
                    onStreamSelected = { stream ->
                        pendingStream = stream to (0 to "Now Playing")
                        navController.navigate(Screen.Player.createRoute(0, "Now Playing"))
                    }
                )
            }

            composable(
                Screen.Player.route,
                arguments = listOf(
                    navArgument("movieId") { type = NavType.IntType },
                    navArgument("title") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val movieId = backStackEntry.arguments?.getInt("movieId") ?: 0
                val title = backStackEntry.arguments?.getString("title") ?: "Now Playing"
                val (stream, meta) = pendingStream ?: return@composable
                PlayerScreen(
                    stream = stream,
                    movieId = meta.first,
                    title = meta.second,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Downloads.route) {
                DownloadsScreen()
            }

            composable(Screen.Library.route) {
                LibraryScreen(
                    onMovieClick = { id, isTv ->
                        navController.navigate(Screen.Details.createRoute(id, isTv))
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
