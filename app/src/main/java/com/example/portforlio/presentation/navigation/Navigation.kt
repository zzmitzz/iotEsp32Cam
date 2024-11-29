package com.example.portforlio.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.portforlio.presentation.drivecontrol.HomeScreen


@Composable
fun Navigation(){
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.WelcomeScreen.route){

        composable(Screen.WelcomeScreen.route){
            HomeScreen(modifier = Modifier.fillMaxSize(), navController = navController)
        }
    }

}