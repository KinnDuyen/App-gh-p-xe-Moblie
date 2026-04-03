package com.example.myhatd.ui.navigation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myhatd.data.network.RetrofitClient
import com.example.myhatd.data.storage.TokenManager
import com.example.myhatd.repository.AuthRepository
import com.example.myhatd.ui.otp.*
import com.example.myhatd.ui.home.HomeUserScreen
import com.example.myhatd.ui.customer.LocationSearchScreen
import com.example.myhatd.ui.customer.TaoChuyenDiScreen // Giả định file của bạn nằm đây
import com.example.myhatd.viewmodel.AuthViewModel
import com.example.myhatd.viewmodel.UserViewModel
import com.example.myhatd.viewmodel.MainViewModel
import com.example.myhatd.viewmodel.MapViewModel
import com.example.myhatd.viewmodel.LocationSearchViewModel
import com.example.myhatd.viewmodel.ChuyenDiViewModel
import com.google.firebase.auth.FirebaseAuth

// Hàm hỗ trợ lấy Activity từ Context
fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context.findActivity() ?: return

    val apiService = RetrofitClient.apiService
    val authRepository = remember { AuthRepository(apiService = apiService) }
    val tokenManager = remember { TokenManager(context = context) }

    // --- KHỞI TẠO VIEWMODELS CHUNG ---
    val mainViewModel: MainViewModel = viewModel(
        factory = MainViewModel.Factory(tokenManager = tokenManager)
    )
    val isLoggedIn by mainViewModel.isUserLoggedIn

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.Factory(
            activity = activity,
            authRepository = authRepository,
            tokenManager = tokenManager
        )
    )

    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModel.Factory(authRepository)
    )

    val mapViewModel: MapViewModel = viewModel()
    val authState by authViewModel.state.collectAsStateWithLifecycle()
    val currentPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber ?: ""

    // Tải dữ liệu người dùng khi login thành công
    LaunchedEffect(currentPhoneNumber) {
        if (currentPhoneNumber.isNotEmpty()) {
            userViewModel.loadUser(currentPhoneNumber)
        }
    }

    // Luồng bắt đầu
    val startDestination = if (isLoggedIn) NavigationRoutes.HOME else NavigationRoutes.PHONE_AUTH

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 1. Màn hình Nhập Số Điện Thoại
        composable(NavigationRoutes.PHONE_AUTH) {
            PhoneAuthScreen(navController = navController, viewModel = authViewModel)
        }

        // 2. Màn hình Xác Thực OTP
        composable(NavigationRoutes.VERIFY_OTP) {
            VerifyOtpScreen(navController = navController, viewModel = authViewModel)
        }

        // 3. Màn hình Cập Nhật Thông Tin (Nếu là User mới)
        composable(NavigationRoutes.THONG_TIN_USER) {
            xacnhanotpScreen(navController = navController, viewModel = authViewModel)
        }

        // 4. Màn hình Trang Chủ
        composable(NavigationRoutes.HOME) {
            HomeUserScreen(
                navController = navController,
                mapViewModel = mapViewModel
            )
        }

        // 5. Màn hình Tìm Địa Chỉ (Điểm đến)
        composable(NavigationRoutes.TIM_DIA_CHI) {
            val locationSearchViewModel: LocationSearchViewModel = viewModel()
            LocationSearchScreen(
                navController = navController,
                viewModel = locationSearchViewModel,
                mapViewModel = mapViewModel,
                phoneNumber = currentPhoneNumber,
                role = "User"
            )
        }

        // 6. MÀN HÌNH TẠO CHUYẾN ĐI (Trang bạn mới tạo)
        composable(NavigationRoutes.TAO_CHUYEN_DI) {
            val chuyenDiViewModel: ChuyenDiViewModel = viewModel()
            TaoChuyenDiScreen(
                navController = navController,
                viewModel = chuyenDiViewModel,
                mapViewModel = mapViewModel,
                phoneNumber = currentPhoneNumber
            )
        }
    }

    // --- LOGIC ĐIỀU HƯỚNG TỰ ĐỘNG SAU KHI AUTH ---
    LaunchedEffect(authState.isOtpSent, authState.isAuthenticated, authState.isInfoSaved) {
        when {
            authState.isInfoSaved -> {
                navController.navigate(NavigationRoutes.HOME) {
                    popUpTo(NavigationRoutes.PHONE_AUTH) { inclusive = true }
                }
            }
            authState.isAuthenticated -> {
                navController.navigate(NavigationRoutes.THONG_TIN_USER) {
                    popUpTo(NavigationRoutes.PHONE_AUTH) { inclusive = true }
                }
            }
            authState.isOtpSent -> {
                if (navController.currentDestination?.route != NavigationRoutes.VERIFY_OTP) {
                    navController.navigate(NavigationRoutes.VERIFY_OTP)
                }
            }
        }
    }
}