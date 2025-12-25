package com.example.nalasaka.ui.screen.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nalasaka.R
import com.example.nalasaka.data.pref.UserModel
import com.example.nalasaka.di.Injection
import com.example.nalasaka.ui.components.PrimaryButton
import com.example.nalasaka.ui.viewmodel.AuthViewModel
import com.example.nalasaka.ui.navigation.Screen
import com.example.nalasaka.ui.theme.WelcomeGradientEnd
import com.example.nalasaka.ui.theme.WelcomeGradientStart

@Composable
fun WelcomeScreen(
    navController: androidx.navigation.NavHostController,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current

    val gradientBrush = Brush.verticalGradient(
        colors = listOf(WelcomeGradientStart, WelcomeGradientEnd)
    )

    val userFlow = Injection.provideRepository(context).getUser()
    val userModel by userFlow.collectAsState(initial = UserModel("", "", "", false))

    LaunchedEffect(userModel.isLogin) {
        if (userModel.isLogin) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Welcome.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradientBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Image(
                painter = painterResource(id = R.drawable.iconnala),
                contentDescription = "Logo NalaSaka",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "NalaSaka",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Find your fresh food",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(80.dp))

            PrimaryButton(
                text = "LOGIN",
                onClick = { navController.navigate(Screen.Login.route) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "DAFTAR (REGISTER)",
                onClick = { navController.navigate(Screen.Register.route) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            )
        }
    }
}