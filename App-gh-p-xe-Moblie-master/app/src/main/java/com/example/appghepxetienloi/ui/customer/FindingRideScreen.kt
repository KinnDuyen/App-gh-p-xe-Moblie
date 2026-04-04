package com.example.myhatd.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // <-- Cần import này
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel // <-- Cần import này để dùng viewModel(factory = ...)
import androidx.navigation.NavController
import com.example.myhatd.MyApplication // <-- Import lớp Application
import com.example.myhatd.data.model.MatchNotificationDTO
import com.example.myhatd.viewmodel.FindingRideViewModel
import com.example.myhatd.viewmodel.FindingRideViewModelFactory // <-- Import Factory

@Composable
fun FindingRideScreen(
    userPhone: String, // SĐT của user
    viewModel: FindingRideViewModel,
    navController: NavController
) {
    // 1. Khởi tạo ViewModel bằng Factory (KHÔNG DÙNG HILT)
    val context = LocalContext.current

    // Lấy instance MatchRepository từ Application class
    // PHẢI CÓ FILE MyApplication.kt VÀ CẬP NHẬT AndroidManifest.xml
//    val application = context.applicationContext as MyApplication
//    val matchRepository = application.matchRepository
//
//    // Tạo Factory và khởi tạo ViewModel
//    val factory = FindingRideViewModelFactory(matchRepository)
//    // viewModel() bây giờ biết cách tạo FindingRideViewModel
//    val viewModel: FindingRideViewModel = viewModel(factory = factory)

    // 2. Kích hoạt tìm kiếm (chỉ chạy 1 lần khi vào màn hình)
    LaunchedEffect(key1 = userPhone) {
        viewModel.startFindingRide(userPhone)
    }

    // 3. Lắng nghe kết quả từ ViewModel
    val matchResult by viewModel.matchResult.collectAsState()
    val isConnected by viewModel.isSocketConnected.collectAsState()

    // 4. Quyết định hiển thị UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (matchResult == null) {
            // Đang tìm kiếm
            SearchingUI(
                isConnected = isConnected,
                onCancelClick = {
                    // 🛑 DÒNG BỊ LỖI PHẢI SỬA:
                    // viewModel.cancelFindingRide()

                    // ✅ GỌI HÀM HỦY TÌM KIẾM MỚI
                    viewModel.cancelFindingProcess()

                    // Sau khi hủy, bạn có thể điều hướng về màn hình khác nếu cần:
                    navController.popBackStack()
                }
            )
        } else {
            // Đã tìm thấy
            MatchFoundUI_Simple(notification = matchResult!!)
        }
    }
}


// --- GIAO DIỆN "ĐANG TÌM..." (Đã thêm nút Hủy) ---

@Composable
fun SearchingUI(isConnected: Boolean, onCancelClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        if (isConnected) {
            Text(
                text = "✅ Đang lắng nghe Socket...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = "Đang kết nối lại...",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(24.dp))
        CircularProgressIndicator(modifier = Modifier.size(64.dp))

        Spacer(Modifier.height(32.dp))

        Button(onClick = onCancelClick) {
            Text("Hủy tìm kiếm")
        }
    }
}

// --- GIAO DIỆN "ĐÃ TÌM THẤY" ---

@Composable
fun MatchFoundUI_Simple(notification: MatchNotificationDTO) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Text(
                text = "✅ ĐÃ NHẬN ĐƯỢC DỮ LIỆU:",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                color = androidx.compose.ui.graphics.Color.Blue
            )
            Spacer(Modifier.height(16.dp))
        }

        // --- In tất cả các trường ---

        item { Text("matchId: ${notification.matchId}", fontWeight = FontWeight.Bold) }
        item { Text("message: ${notification.message}") }

        item { Spacer(Modifier.height(12.dp)) }
        item { Text("--- TÀI XẾ ---", fontWeight = FontWeight.Bold) }
        item { Text("tenDriver: ${notification.tenDriver}") }
        item { Text("sdtDriver: ${notification.sdtDriver}") }
        item { Text("bienSoXe: ${notification.bienSoXe}") }
        item { Text("hangXe: ${notification.hangXe}") }

        item { Spacer(Modifier.height(12.dp)) }
        item { Text("--- USER ---", fontWeight = FontWeight.Bold) }
        item { Text("tenUser: ${notification.tenUser}") }
        item { Text("sdtUser: ${notification.sdtUser}") }

        item { Spacer(Modifier.height(12.dp)) }
        item { Text("--- CHUYẾN ĐI ---", fontWeight = FontWeight.Bold) }
        item { Text("tenDiemDiUser: ${notification.tenDiemDiUser}") }
        item { Text("tenDiemDenUser: ${notification.tenDiemDenUser}") }
        item { Text("giaTien: ${notification.giaTien}") }
        item { Text("thoiGianDriverDenUser (RAW): ${notification.thoiGianDriverDenUser}") }
        item { Text("hinhThucThanhToan: ${notification.hinhThucThanhToan}") }
    }
}