package com.example.myhatd.ui.customer

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myhatd.R
import com.example.myhatd.viewmodel.FindingRideViewModel
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.example.myhatd.ui.navigation.NavigationRoutes
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myhatd.viewmodel.MapViewModel

// ✅ IMPORTS MAPLIBRE
import com.example.myhatd.ui.common.MapLibreComposable
import com.example.myhatd.ui.utils.addOrUpdateMarker
import org.maplibre.android.annotations.Marker
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.camera.CameraUpdateFactory


// --- ENTRY POINT MỚI ---
@Composable
fun RideTrackingScreen(
    navController: NavController,
    viewModel: FindingRideViewModel, // Nhận ViewModel chung
    // ✅ THÊM MapViewModel
    mapViewModel: MapViewModel = viewModel()
) {
    // 1. Lấy dữ liệu động từ ViewModel
    val rideInfo by viewModel.currentRide.collectAsState()
    val rideStatus by viewModel.currentRideStatus.collectAsState()

    // --- TRẠNG THÁI MAPLIBRE VÀ VỊ TRÍ USER ---
    val context = LocalContext.current
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var userMarker by remember { mutableStateOf<Marker?>(null) }
    // XÓA: driverMarker
    val mapUiState by mapViewModel.uiState.collectAsState()
    val userLocation = mapUiState.lastKnownLocation // Vị trí User

    var showRejectedDialog by remember { mutableStateOf(false) }
    var showPickedUpDialog by remember { mutableStateOf(false) }

    // 2. Xử lý dữ liệu
    val driverName = rideInfo?.tenDriver ?: "Đang tải..."
    val driverPhone = rideInfo?.sdtDriver
    val carType = rideInfo?.hangXe ?: "Xe máy"
    val licensePlate = rideInfo?.bienSoXe ?: "N/A"
    val destinationName = rideInfo?.tenDiemDenUser ?: "Điểm đến"
    val pickupDropoffInfo =
        "${rideInfo?.tenDiemDiUser ?: "Điểm đón"} - ${destinationName}"
    val rating = "5.0⭐"


    val titleText = when (rideStatus) {
        "PICKED_UP" -> "Chuyến đi đã bắt đầu"
        "DRIVER_ACCEPTED" -> "Tài xế đang đến"
        "DRIVER_REJECTED" -> "Chuyến đi đã bị hủy"
        else -> "Đang chờ tài xế xác nhận"
    }

    // 3. Xử lý trạng thái Loading/Null
    if (rideInfo == null && rideStatus != "DRIVER_REJECTED") {
        return Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Đang tải thông tin chuyến đi...", fontSize = 20.sp, color = Color.Gray)
        }
    }

    // 4. LOGIC MAPLIBRE & MARKER USER
    LaunchedEffect(mapLibreMap, userLocation) {
        if (mapLibreMap != null) {
            // Cập nhật Marker User
            if (userLocation != null) {
                addOrUpdateMarker(
                    map = mapLibreMap,
                    currentMarker = userMarker,
                    onMarkerUpdate = { marker -> userMarker = marker },
                    latLng = userLocation,
                    name = "Vị trí của bạn"
                )
                // Focus vào vị trí User
                mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 14.0))
            }
        }
    }


    // 5. XỬ LÝ TRẠNG THÁI CHUYẾN ĐI (Không đổi)
    LaunchedEffect(rideStatus) {
        if (rideStatus == "COMPLETED" && rideInfo != null) {
            val matchId = rideInfo!!.matchId
            navController.navigate("review_screen/$matchId") {
                popUpTo(NavigationRoutes.THEO_DOI_LO_TRINH) { inclusive = true }
            }
            viewModel.resetRideStatus()
        }

        if (rideStatus == "PICKED_UP") {
            showPickedUpDialog = true
            kotlinx.coroutines.delay(5000L)
            showPickedUpDialog = false
        }

        if (rideStatus == "DRIVER_REJECTED") {
            showRejectedDialog = true
            kotlinx.coroutines.delay(5000L)
            showRejectedDialog = false
            viewModel.resetRideStatus()
            navController.popBackStack(NavigationRoutes.TIM_DIA_CHI, false)
        }
    }

    // 6. UI Chính
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // 🔹 KHU VỰC NỀN TRÊN (MAPLIBRE)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.7f) // Cố định chiều cao cho Map
                .align(Alignment.TopCenter)
        ) {
            // ✨ MAPLIBRE COMPOSABLE (Thay thế background ảnh)
            MapLibreComposable(
                modifier = Modifier.fillMaxSize(),
                userLocation = userLocation,
                onMapReady = { mapLibreMap = it }
            )


            // 🔹 Nút CHỈ ĐƯỜNG (GOOGLE MAPS)
            Button(
                onClick = {
                    val uri = Uri.parse("google.navigation:q=${destinationName}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                    mapIntent.setPackage("com.google.android.apps.maps")

                    try {
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${destinationName}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                    }
                },

                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 16.dp, top = 40.dp)
                    .clip(RoundedCornerShape(8.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.map),
                        contentDescription = "Google Maps Direction",
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Chỉ đường",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        } // END Top Background Box

        // 🔹 Nút quay lại (Overlay trên nền)
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .padding(start = 16.dp, top = 40.dp)
                .align(Alignment.TopStart)
                .background(Color.White, CircleShape)
                .size(40.dp)
                .zIndex(1f)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.Black
            )
        }


        // 🔹 Khung thông tin tài xế (Bottom Sheet)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            Text(
                text = titleText,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Text(
                text = if (rideStatus == "PICKED_UP") "Đang đi đến ${destinationName}" else pickupDropoffInfo,
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 🔸 Card thông tin tài xế
            DynamicDriverInfoCard(
                driverName = driverName,
                carType = carType,
                licensePlate = licensePlate,
                rating = rating,
                onCardClick = {
                    navController.navigate("chi_tiet_chuyen_di_user")
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 🔸 Hàng nút chat và gọi
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(x = 20.dp, y = 0.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔸 Nút Chat
                OutlinedButton(
                    onClick = {
                        if (driverPhone != null) {
                            val uri = Uri.parse("smsto:$driverPhone")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        }
                    },
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, Color.Black),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(44.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.chat),
                        contentDescription = "Chat",
                        tint = Color.Black,
                        modifier = Modifier.size(45.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Chat với tài xế", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 🔸 Nút gọi
                IconButton(
                    onClick = {
                        if (driverPhone != null) {
                            val uri = Uri.parse("tel:$driverPhone")
                            val intent = Intent(Intent.ACTION_DIAL, uri)
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .size(55.dp)
                        .padding(start = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.call),
                        contentDescription = "Call",
                        tint = Color.Black,
                        modifier = Modifier.size(45.dp)
                    )
                }
            }
        } // END Bottom Info Column

        if (showRejectedDialog) {
            AlertDialog(
                onDismissRequest = { /* Không cho người dùng tắt thủ công, trừ khi nhấn nút */ },
                title = { Text("🚨 Chuyến đi bị hủy") },
                text = { Text("Tài xế đã từ chối/bỏ qua chuyến đi. Hệ thống sẽ tự động chuyển bạn về màn hình tìm kiếm trong 5 giây.") },
                confirmButton = {
                    Button(onClick = {
                        showRejectedDialog = false
                        viewModel.resetRideStatus()
                        navController.popBackStack(NavigationRoutes.TIM_DIA_CHI, false)
                    }) {
                        Text("Đã hiểu & Trở về")
                    }
                }
            )
        }

        if (showPickedUpDialog) {
            AlertDialog(
                onDismissRequest = { showPickedUpDialog = false },
                title = { Text("🎉 Chuyến đi bắt đầu!") },
                text = { Text("Tài xế đã đón bạn thành công. Chúc bạn có một chuyến đi vui vẻ.") },
                confirmButton = {
                    Button(onClick = { showPickedUpDialog = false }) {
                        Text("Tuyệt vời")
                    }
                }
            )
        }
    } // END Main Box
}


// --- DynamicDriverInfoCard giữ nguyên ---
@Composable
fun DynamicDriverInfoCard(
    driverName: String,
    carType: String,
    licensePlate: String,
    rating: String,
    onCardClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(onClick = onCardClick)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFF0081F1))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Ảnh nền trong card tài xế
            Image(
                painter = painterResource(id = R.drawable.nenthongtindriver),
                contentDescription = "Background Driver",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Avatar + xe + tên
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        // Avatar
                        Image(
                            painter = painterResource(id = R.drawable.avtdriver),
                            contentDescription = "Ảnh tài xế",
                            modifier = Modifier
                                .size(80.dp)
                                .background(Color.White, CircleShape)
                                .border(2.dp, Color(0xFF0081F1), CircleShape)
                                .clip(CircleShape)
                        )

                        // Ảnh xe máy
                        Image(
                            painter = painterResource(id = R.drawable.xemay),
                            contentDescription = "Xe máy",
                            modifier = Modifier
                                .size(50.dp)
                                .align(Alignment.BottomEnd)
                                .offset(x = 6.dp, y = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Tên tài xế và sao
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = driverName,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color(0xFF333333)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = rating,
                            color = Color.Black,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Biển số xe + loại xe
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF0081F1)),
                    color = Color(0xFFE3F2FD),
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = licensePlate,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0081F1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = carType,
                    fontSize = 12.sp,
                    color = Color(0xFF007ACC)
                )
            }

            // Logo góc phải dưới
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo HATD",
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 10.dp, y = 30.dp)
                    .padding(bottom = 8.dp, end = 16.dp)
            )
        }
    }
}