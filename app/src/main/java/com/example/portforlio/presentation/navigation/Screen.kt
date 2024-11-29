package com.example.portforlio.presentation.navigation

sealed class Screen(val route: String) {
    object WelcomeScreen: Screen("driver_screen")
}