package com.example.myhatd.ui.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myhatd.R
import com.example.myhatd.MyApplication
import com.example.myhatd.ui.navigation.NavigationRoutes
import com.example.myhatd.viewmodel.FindingRideViewModel
import com.example.myhatd.viewmodel.FindingRideViewModelFactory
import java.util.Locale
import kotlin.math.roundToLong

// ✅ IMPORTS MAPLIBRE VÀ VIEWMODEL
import com.example.myhatd.ui.common.MapLibreComposable
import com.example.myhatd.ui.utils.addOrUpdateMarker
import com.example.myhatd.viewmodel.MapViewModel
import org.maplibre.android.annotations.Marker
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.geometry.LatLng


/**
 * Màn hình xác nhận đặt xe (Hiển thị giá, thời gian đến).
 */
@Composable
fun XacNhanDatXeScreen(
    navController: NavController,
    viewModel: FindingRideViewModel,
    // ✅ THÊM MapViewModel
    mapViewModel: MapViewModel = viewModel()
) {

    // ✅ TRẠNG THÁI MAPLIBRE
    var mapLibreMap by remember { mutableStateOf<MapLibreMap?>(null) }
    var currentMarker by remember { mutableStateOf<Marker?>(null) }

    // 2. LẮNG NGHE DỮ LIỆU
    val matchResult by viewModel.matchResult.collectAsState()
    val isConfirming by viewModel.isConfirming.collectAsState()
    // ✅ Lắng nghe cờ báo hủy mới
    val isMatchCancelled by viewModel.isMatchCancelled.collectAsState()

    // --- LẮNG NGHE DỮ LIỆU MAP ---
    val mapUiState by mapViewModel.uiState.collectAsState()
    val userLocation = mapUiState.lastKnownLocation // MapLibre LatLng (Vị trí hiện tại của User)

    // --- XỬ LÝ DỮ LIỆU THẬT ---
    val matchId = matchResult?.matchId
    val giaTienInt = matchResult?.giaTien?.roundToLong() ?: 0L
    val giaTienFormatted = String.format(Locale.getDefault(), "%,dđ", giaTienInt)
    val thoiGianDuKienRaw = matchResult?.thoiGianDriverDenUser

    // Cắt chuỗi thời gian
    val thoiGianDuKienText = thoiGianDuKienRaw
        ?.substringAfter('T')
        ?.substringBefore('.')
        ?.substringBeforeLast(':') ?: "Đang tính..."

    // 3. ✅ XỬ LÝ KHI DỮ LIỆU BỊ MẤT/HỦY BỎ
    // Dùng LaunchedEffect để đảm bảo việc điều hướng xảy ra an toàn
    LaunchedEffect(matchResult, isMatchCancelled) {
        // Điều kiện quay về: matchResult bị null VÀ (isMatchCancelled là true HOẶC chưa có matchResult bao giờ)
        if (matchResult == null && isMatchCancelled) {
            // Chỉ quay về khi Match bị hủy/timeout sau khi đã được hiển thị
            navController.popBackStack(NavigationRoutes.TIM_DIA_CHI, false)
        }
    }

    // ✅ LOGIC HIỂN THỊ MARKER CHO MAPLIBRE
    // Đây chỉ là marker cho vị trí hiện tại của user, không phải lộ trình
    // ... trong file XacNhanDiemDonScreen.kt

// ✅ LOGIC HIỂN THỊ MARKER CHO MAPLIBRE
    LaunchedEffect(mapLibreMap, userLocation) {
        if (mapLibreMap != null && userLocation != null) {
            // 1. CẬP NHẬT MARKER
            addOrUpdateMarker(
                map = mapLibreMap,
                currentMarker = currentMarker,
                onMarkerUpdate = { marker -> currentMarker = marker },
                latLng = userLocation,
                name = "Điểm đón hiện tại"
            )

            // 2. ✅ LOGIC MỚI: DI CHUYỂN CAMERA ĐẾN VỊ TRÍ NGƯỜI DÙNG
            if (mapLibreMap?.cameraPosition?.target != userLocation) {
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLocation, 16.0) // Zoom 16.0 là tốt cho chi tiết đường phố
                mapLibreMap?.animateCamera(cameraUpdate, 1000) // Di chuyển mượt mà trong 1 giây
            }
        }
    }


    // Hiển thị loading nếu chưa có dữ liệu và chưa bị hủy
    if (matchResult == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Nếu chưa bị hủy, có thể đang trong quá trình load
            if (!isMatchCancelled) {
                CircularProgressIndicator()
                Text("Đang tải chi tiết chuyến đi...", modifier = Modifier.offset(y = 40.dp))
            } else {
                // Đã bị hủy -> Thông báo và hiển thị nút quay về (Trường hợp popBackStack bị lỗi)
                Text("Chuyến đi đã bị hủy hoặc hết hạn.", modifier = Modifier.offset(y = (-20).dp))
                Button(onClick = {
                    navController.popBackStack(NavigationRoutes.TIM_DIA_CHI, false)
                }) {
                    Text("Quay về tìm kiếm")
                }
            }
        }
        // return ngay để tránh vẽ UI phức tạp bên dưới khi không có dữ liệu
        return
    }

    // 4. UI CHÍNH
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        // ✨ MAPLIBRE COMPOSABLE (Bản đồ nền)
        MapLibreComposable(
            modifier = Modifier
                .fillMaxSize(), // ✅ FILL MAX SIZE (Không padding, không bo góc)
            userLocation = userLocation,
            onMapReady = { mapLibreMap = it } // Nhận tham chiếu MapLibreMap
        )

        // Nút Back
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .zIndex(2f)
                .padding(16.dp)
                // Thêm background cho nút back để dễ nhìn trên map
                .background(Color.White.copy(alpha = 0.8f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Quay lại",
                tint = Color.Black
            )
        }

        // --- Bottom Sheet và Background ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                // ... (Graphics layers) ...
                .graphicsLayer {
                    shadowElevation = 16f
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    clip = true
                }
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .zIndex(1f)
        ) {

            // Background Image (Giữ nguyên, nhưng đặt dưới ZIndex của nội dung để tránh mất màu)
            Image(
                painter = painterResource(id = R.drawable.xacnhan),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer { alpha = 0.1f } // Giảm alpha đi nhiều để không bị chói
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Tiêu đề
                Text(text = "Phương tiện", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                // -----------------------------------------------------------------
                // CARD CHỌN XE MÁY (HATD bike)
                // -----------------------------------------------------------------
                Card(
                    // Thêm modifier cần thiết: .fillMaxWidth().clip(...).border(...)
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).border(1.dp, Color(0xFF5C9BE3), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    // Cần có Box để chứa Image và Row cho layout
                    Box(modifier = Modifier.fillMaxWidth().height(80.dp).padding(8.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.xemay),
                            contentDescription = "HATD Bike",
                            modifier = Modifier.size(64.dp).align(Alignment.CenterStart).padding(end = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxSize().padding(start = 72.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "HATD bike", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "Dự kiến lúc: ${thoiGianDuKienText}", fontSize = 12.sp, fontWeight = FontWeight.Normal, color = Color(0xFF666666))
                            }
                            Text(text = giaTienFormatted, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5C9BE3))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // -----------------------------------------------------------------
                // PHƯƠNG THỨC THANH TOÁN
                // -----------------------------------------------------------------
                Text(text = "Phương thức thanh toán", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFFF3F7FF)).padding(horizontal = 16.dp, vertical = 12.dp).border(1.dp, Color(0xFF5C9BE3), RoundedCornerShape(10.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.dola),
                        contentDescription = "Tiền mặt",
                        tint = Color(0xFF5C9BE3),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = matchResult?.hinhThucThanhToan ?: "Tiền mặt", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // -----------------------------------------------------------------
                // NÚT ĐẶT XE (XÁC NHẬN)
                // -----------------------------------------------------------------
                Button(
                    onClick = {
                        if (matchId != null && !isConfirming) {
                            viewModel.confirmBooking(matchId) { success ->
                                if (success) {
                                    // Xác nhận thành công -> Chuyển sang màn hình thông tin chuyến đi
                                    navController.navigate(NavigationRoutes.THEO_DOI_LO_TRINH) {
                                        // ✅ Pop up màn hình này để người dùng không quay lại màn hình xác nhận được nữa
                                        popUpTo(NavigationRoutes.XAC_NHAN_DAT_XE) { inclusive = true }
                                    }
                                } else {
                                    // Xử lý lỗi: Logic trong ViewModel đã set isMatchCancelled = true và matchResult = null
                                    // LaunchedEffect ở trên sẽ tự động điều hướng về TIM_DIA_CHI
                                    // TODO: Hiển thị Toast thông báo lỗi xác nhận
                                }
                            }
                        }
                    },
                    enabled = matchId != null && !isConfirming,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C9BE3)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isConfirming) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    } else {
                        Text(text = "Đặt xe", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }
}