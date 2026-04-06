package com.example.myhatd.ui.driver

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhatd.R
import androidx.compose.foundation.BorderStroke
import androidx.navigation.NavController
import com.example.myhatd.viewmodel.DriverMatchViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.myhatd.data.model.MatchNotificationDTO
import com.example.myhatd.ui.navigation.NavigationRoutes
import java.util.Locale
import kotlin.math.roundToLong
import kotlinx.coroutines.launch

@Composable
fun ChiTietChuyenDiScreen(
    navController: NavController,
    matchId: Long, // ID Match được truyền vào từ Navigation
    viewModel: DriverMatchViewModel
) {
    // 1. Lắng nghe dữ liệu Match và trạng thái Loading
    val matchResult by viewModel.matchResult.collectAsState()
    val isConfirming by viewModel.isConfirming.collectAsState()
    val scope = rememberCoroutineScope()

    // 🛑 THAY THẾ LOGIC LẤY DỮ LIỆU:
    // Chỉ hiển thị Match nếu ID của nó khớp với Match ID được truyền từ Navigation.
    val displayTrip: MatchNotificationDTO? = remember(matchResult, matchId) {
        if (matchResult?.matchId == matchId) {
            matchResult
        } else {
            null
        }
    }

    // SỬ DỤNG displayTrip TỪ ĐÂY VỀ SAU
    val trip = displayTrip // Dùng trip để giữ sự nhất quán với code cũ

    val tenUser = trip?.tenUser ?: "Đang tải..."
    val sdtUser = trip?.sdtUser ?: "N/A"
    val diemDon = trip?.tenDiemDiUser ?: "Đang tải..."
    val diemDen = trip?.tenDiemDenUser ?: "Đang tải..."
    val hinhThucThanhToan = trip?.hinhThucThanhToan ?: "Tiền mặt"

    val giaTienInt = trip?.giaTien?.roundToLong() ?: 0L
    val giaTienFormatted = String.format(Locale.getDefault(), "%,dđ", giaTienInt)

    val thoiGianDenUserRaw = trip?.thoiGianDriverDenUser ?: "N/A"
    // Cắt chuỗi thời gian (ví dụ: "14:58")
    val thoiGianDenUserFormatted = thoiGianDenUserRaw
        .substringAfter('T')
        .substringBeforeLast(':')
        .substringBefore('.')
        .ifEmpty { "Vừa nhận" }

    // 3. Xử lý khi dữ liệu bị mất (Trạng thái xung đột/Hủy)
    // Nếu trip là null VÀ không phải do đang confirm, tức là match đã bị hủy từ server
    if (trip == null && !isConfirming) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy Match đang chờ (ID: $matchId). Đã bị hủy hoặc không khớp.", color = Color.Red) // 💡 Thêm ID để debug
            // ... (Logic quay về Home không đổi) ...
            LaunchedEffect(Unit) {
                kotlinx.coroutines.delay(3000)
                navController.popBackStack(NavigationRoutes.HOME_DRIVER, false)
            }
            Button(onClick = { navController.popBackStack(NavigationRoutes.HOME_DRIVER, false) }) {
                Text("Về trang chủ ngay")
            }
        }
        return
    }

    // 4. Xử lý logic nút
    val onConfirmClicked: () -> Unit = {
        scope.launch {
            // Gọi API xác nhận chuyến đi.
            viewModel.confirmBooking(matchId) { success ->
                if (success) {
                    // ✅ Tối ưu hóa: Thay thế màn hình Chi Tiết bằng màn hình Theo Dõi.
                    navController.navigate(NavigationRoutes.DRIVER_TRACKING) {
                        // Loại bỏ màn hình Chi Tiết hiện tại khỏi stack
                        popUpTo(navController.currentDestination!!.route!!) { inclusive = true }
                    }
                } else {
                    // THẤT BẠI: (Có thể do user hủy hoặc lỗi server).
                    // ViewModel đã tự động reset matchResult, chỉ cần quay về home.
                    navController.popBackStack(NavigationRoutes.HOME_DRIVER, false)
                }
            }
        }
    }

    val onCancelClicked: () -> Unit = {
        // Driver từ chối chuyến đi này
        viewModel.forceUpdateMatchResult(null)

        // Quay về Home
        navController.popBackStack(NavigationRoutes.HOME_DRIVER, false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
            .padding(top = 28.dp)
    ) {
        // 🔹 Thanh tiêu đề và Nút Back
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = Color.Black
                )
            }
            Text(
                text = "Chi tiết chuyến đi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Thông tin user
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xFF0081F1))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Ảnh nền
                Image(
                    painter = painterResource(id = R.drawable.anhnensauuser),
                    contentDescription = "Background Driver",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Avatar và tên user
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar (Placeholder)
                    Box {
                        Image(
                            painter = painterResource(id = R.drawable.anhuser),
                            contentDescription = "Ảnh user",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)

                        )

                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    // Tên và SĐT user
                    Column(
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(tenUser, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(sdtUser, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Logo góc dưới phải
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo HATD",
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 6.dp, y = 6.dp)
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Phần thông tin chuyến đi
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF4ABDE0), RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            Box {
                Image(
                    painter = painterResource(id = R.drawable.anhnenchitietchuyendi),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                    alpha = 0.35f
                )

                Column(modifier = Modifier.padding(16.dp)) {
                    Text("HATD bike", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    // Thời gian đón
                    Text("Đến đón lúc: $thoiGianDenUserFormatted", color = Color.Gray, fontSize = 14.sp)
                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.diemdon),
                            contentDescription = null,
                            tint = Color(0xFF000000),
                            modifier = Modifier.size(25.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            // Điểm đón
                            Text(diemDon, fontWeight = FontWeight.Bold)
                        }
                    }

                    Image(
                        painter = painterResource(id = R.drawable.duonggachnoi),
                        contentDescription = "Đường nối giữa điểm đón và điểm đến",
                        modifier = Modifier
                            .padding(start = 2.dp, top = 2.dp, bottom = 2.dp)
                            .height(40.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.diemden),
                            contentDescription = null,
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(25.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Column {
                            // Điểm đến
                            Text(diemDen, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.dola),
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        // Hình thức thanh toán
                        Text(hinhThucThanhToan, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.weight(1f))
                        // Giá tiền
                        Text(giaTienFormatted, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("Ghi chú: (Nếu có)", fontWeight = FontWeight.Bold)
                }

                // Images góc dưới (Giữ nguyên)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 Hai nút hành động dưới cùng (Xác nhận/Hủy)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Nút 1: HỦY CHUYẾN (Từ chối)
            OutlinedButton(
                onClick = onCancelClicked, // <-- Dùng lambda đã định nghĩa
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isConfirming,
                border = BorderStroke(1.dp, Color.Red)
            ) {
                Text("Hủy chuyến", color = Color.Red, fontSize = 16.sp)
            }

            // Nút 2: XÁC NHẬN CHUYẾN (Chấp nhận)
            Button(
                onClick = onConfirmClicked, // <-- Dùng lambda đã định nghĩa
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A5EE1)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .padding(start = 8.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isConfirming // Vô hiệu hóa khi đang gọi API
            ) {
                Text(
                    if (isConfirming) "Đang xử lý..." else "Xác nhận chuyến",
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
        }
    }
}