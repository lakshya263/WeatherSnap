package com.example.weathersnap.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.weathersnap.ui.camera.CameraScreen
import com.example.weathersnap.ui.createreport.CreateReportScreen
import com.example.weathersnap.ui.savedreports.SavedReportsScreen
import com.example.weathersnap.ui.weather.WeatherScreen

private const val TRANSITION_DURATION = 300

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Routes.WEATHER
    ) {

        // ── Weather Screen ────────────────────────────────────────────────
        composable(
            route = Routes.WEATHER,
            enterTransition = {
                slideIntoContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            }
        ) {
            WeatherScreen(navController = navController)
        }

        // ── Create Report Screen ──────────────────────────────────────────
        composable(
            route = Routes.CREATE_REPORT,
            enterTransition = {
                slideIntoContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            }
        ) {
            CreateReportScreen(navController = navController)
        }

        // ── Camera Screen ─────────────────────────────────────────────────
        composable(
            route = Routes.CAMERA,
            enterTransition = {
                slideIntoContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            }
        ) {
            CameraScreen(navController = navController)
        }

        // ── Saved Reports Screen ──────────────────────────────────────────
        composable(
            route = Routes.SAVED_REPORTS,
            enterTransition = {
                slideIntoContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.Start,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards      = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(TRANSITION_DURATION)
                )
            }
        ) {
            SavedReportsScreen(navController = navController)
        }
    }
}