package com.example.myhatd.ui.customer

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myhatd.R
import com.example.myhatd.viewmodel.ChuyenDiViewModel
import com.example.myhatd.viewmodel.UserViewModel

import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.maps.model.LatLng
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager

@SuppressLint("UnrememberedGetBackStackEntry", "MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaoYeuCauChuyenDiScreen(
    navController: NavController,
    chuyenDiViewModel: ChuyenDiViewModel,
    userViewModel: UserViewModel
) {
    val context = LocalContext.current

    // --- STATE VỊ TRÍ CỤC BỘ ---
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var isLocationLoading by remember { mutableStateOf(true) }

    // --- Lấy State ---
    val state by chuyenDiViewModel.state.collectAsState()
    val user by userViewModel.userData

    var diemDi by remember { mutableStateOf("") }
    var diemDen by remember { mutableStateOf("") }

    val nearbyPlaces = listOf<Pair<String, String>>()

    // --- HÀM LẤY VỊ TRÍ TRỰC TIẾP ---
    fun fetchLocation(ctx: Context) {
        isLocationLoading = true // Bắt đầu tải

        // 1. Kiểm tra quyền (chỉ kiểm tra, giả định đã được cấp)
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("LOCATION_DEBUG", "Permission not granted for direct fetch.")
            isLocationLoading = false
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(ctx)
        val cancellationTokenSource = CancellationTokenSource()

        // 2. Gọi API getCurrentLocation
        fusedLocationClient.getCurrentLocation(
            LocationRequest.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { loc ->
            loc?.let {
                currentLocation = LatLng(it.latitude, it.longitude)
                Log.d("LOCATION_DEBUG", "Direct Location received: $currentLocation")
            }
            isLocationLoading = false
        }.addOnFailureListener { e ->
            Log.e("LOCATION_DEBUG", "Direct Location Error:", e)
            isLocationLoading = false
            currentLocation = null
        }
    }

    // 🚀 KÍCH HOẠT LẤY VỊ TRÍ NGAY KHI MÀN HÌNH MỞ
    LaunchedEffect(Unit) {
        fetchLocation(context)
    }

    // --- XỬ LÝ TRẠNG THÁI (UDF) ---
    LaunchedEffect(state.successMessage, state.errorMessage) {
        if (state.successMessage != null || state.errorMessage != null) {
            chuyenDiViewModel.resetState()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Ảnh nền
        Image(
            painter = painterResource(id = R.drawable.nentaoyeucauchuyendi),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.9f),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Nút quay lại và Tiêu đề
            Box(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Image(
                    painter = painterResource(id = R.drawable.backicon),
                    contentDescription = "Quay lại",
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(40.dp)
                        .offset(y = 15.dp)
                        .clickable { navController.navigate("home") },
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Đi cùng HATD",
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    modifier = Modifier.align(Alignment.Center).offset(y = 15.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Điểm đi
            OutlinedTextField(
                value = diemDi,
                onValueChange = { diemDi = it },
                label = { Text("Điểm đi") },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3085E0),
                    unfocusedBorderColor = Color(0xFF3085E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Điểm đến
            OutlinedTextField(
                value = diemDen,
                onValueChange = { diemDen = it },
                label = { Text("Điểm đến") },
                trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF3085E0),
                    unfocusedBorderColor = Color(0xFF3085E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(30.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(86.dp)
            )

            // Xác nhận gửi chuyến đi
            Button(
                onClick = {
                    user?.let {
                        chuyenDiViewModel.sendChuyenDi(
                            phoneNumber = it.phoneNumber ?: "",
                            role = it.role ?: "USER",
                            diemDi = diemDi,
                            diemDen = diemDen,
                            viDo = currentLocation?.latitude,
                            kinhDo = currentLocation?.longitude
                        )
                    }
                },
                // Nút chỉ được bật khi KHÔNG loading, KHÔNG đang tải vị trí VÀ vị trí đã có
                enabled = !state.isLoading && !isLocationLoading && currentLocation != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Thay đổi text để thông báo nếu đang chờ vị trí
                Text(when {
                    isLocationLoading -> "Đang lấy vị trí..."
                    currentLocation != null -> "Xác nhận"
                    else -> "Không tìm thấy vị trí"
                })
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Hiển thị trạng thái
            Box(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    state.isLoading || isLocationLoading -> CircularProgressIndicator()
                    state.successMessage != null -> Text(state.successMessage!!, color = Color.Green)
                    state.errorMessage != null -> Text(state.errorMessage!!, color = Color.Red)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Điểm đến gần đây",
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 20.dp, top = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Danh sách địa điểm gần đây
            if (nearbyPlaces.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có dữ liệu", color = Color.Gray, fontSize = 15.sp)
                }
            } else {
                LazyColumn {
                    items(nearbyPlaces.size) { index ->
                        val place = nearbyPlaces[index]
                        NearbyPlaceCard(name = place.first, address = place.second)
                    }
                }
            }
        }
    }
}

@Composable
fun NearbyPlaceCard(name: String, address: String) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                Icons.Default.Place,
                contentDescription = null,
                tint = Color(0xFF1976D2),
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(name, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(address, fontSize = 13.sp, color = Color.DarkGray)
            }
        }
    }
}