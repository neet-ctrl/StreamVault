package com.streamvault.app.presentation.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
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
    BottomNavItem(Screen.Home, "Home", Icons.Outlined.Home, Icons.Filled.Home),
    BottomNavItem(Screen.Search, "Search", Icons.Outlined.Search, Icons.Filled.Search),
    BottomNavItem(Screen.Library, "Library", Icons.Outlined.BookmarkBorder, Icons.Filled.Bookmark),
    BottomNavItem(Screen.Downloads, "Downloads", Icons.Outlined.Download, Icons.Filled.Download),
    BottomNavItem(Screen.Settings, "Settings", Icons.Outlined.Settings, Icons.Filled.Settings)
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    var showSplash by remember { mutableStateOf(true) }
    var pendingStream by remember { mutableStateOf<Triple<Stream, Int, String>?>(null) }

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
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                PremiumBottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
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
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300, easing = EaseOutCubic)
                ) + fadeIn(tween(300))
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(300, easing = EaseInCubic)
                ) + fadeOut(tween(200))
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(300, easing = EaseOutCubic)
                ) + fadeIn(tween(300))
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300, easing = EaseInCubic)
                ) + fadeOut(tween(200))
            }
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
                        pendingStream = Triple(stream, 0, "Now Playing")
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
            ) {
                val triple = pendingStream ?: return@composable
                PlayerScreen(
                    stream = triple.first,
                    movieId = triple.second,
                    title = triple.third,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Downloads.route) { DownloadsScreen() }

            composable(Screen.Library.route) {
                LibraryScreen(
                    onMovieClick = { id, isTv ->
                        navController.navigate(Screen.Details.createRoute(id, isTv))
                    }
                )
            }

            composable(Screen.Settings.route) { SettingsScreen() }
        }
    }
}

@Composable
private fun PremiumBottomBar(navController: NavController, currentRoute: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(colors = listOf(Color.Transparent, BlackSurface))
            )
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(BlackCard)
                .border(0.5.dp, BlackBorder, RoundedCornerShape(24.dp))
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute == item.screen.route
                    BottomNavIcon(
                        item = item,
                        selected = selected,
                        onClick = {
                            if (!selected) {
                                navController.navigate(item.screen.route) {
                                    popUpTo(Screen.Home.route) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavIcon(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_scale"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (selected)
                    Brush.linearGradient(listOf(AccentRed.copy(alpha = 0.2f), AccentRed.copy(alpha = 0.1f)))
                else
                    Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
            )
            .then(
                if (selected)
                    Modifier.border(0.5.dp, AccentRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                else
                    Modifier
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                if (selected) item.selectedIcon else item.icon,
                contentDescription = item.label,
                tint = if (selected) AccentRed else TextTertiary,
                modifier = Modifier.size(22.dp)
            )
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(AccentRed)
                )
            }
        }
    }
}

private val EaseOutCubic = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val EaseInCubic = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)
