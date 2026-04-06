package com.example.myhatd.ui.customer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhatd.R

@Composable
fun ThongBaoScreen(
    navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        //  Nút quay lại
        Image(
            painter = painterResource(id = R.drawable.backicon),
            contentDescription = "Quay lại",
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 40.dp, start = 12.dp)
                .size(40.dp) // kích thước nút back
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.8f)) // nền mờ nhẹ
                .clickable {
                    navController.navigateUp(/*-------------*/)  },
            contentScale = ContentScale.Fit
        )

        // 🔹 Tiêu đề
        Text(
            text = "Thông Báo",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.Black,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
        )

        // 🔹 Gạch dưới tiêu đề
        Divider(
            color = Color(0xFF4ABDE0),
            thickness = 3.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
                .width(150.dp)
        )

        // 🔹 Các thông báo
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 120.dp, start = 20.dp, end = 20.dp)
        ) {
            NotificationItem(
                title = "10 phút nữa đến giờ khởi hành",
                content = "Chuyến xe: KTX khu A đến Đại học GTVT\nTài xế: Nguyễn Văn A"
            )

            NotificationItem(
                title = "5 phút nữa tài xế đến điểm đón",
                content = "Điểm đón: Cổng KTX khu A"
            )

            NotificationItem(
                title = "Bạn đã đến nơi thành công",
                content = "Cảm ơn bạn đã đi cùng HATD"
            )
        }
    }
}

@Composable
fun NotificationItem(
    title: String,
    content: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        Text(
            text = title,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content,
            color = Color.Black,
            fontSize = 15.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Divider(
            color = Color(0xFF4ABDE0),
            thickness = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50.dp))
        )
    }
}