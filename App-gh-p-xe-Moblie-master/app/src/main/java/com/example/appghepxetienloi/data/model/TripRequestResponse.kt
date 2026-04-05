package com.example.myhatd.data.model

import com.google.gson.annotations.SerializedName

/**
 * Data Class ánh xạ phản hồi JSON từ Server sau khi gửi yêu cầu chuyến đi thành công.
 * Đây là DTO được sử dụng trong Retrofit API Interface.
 */
data class TripRequestResponse(

    // ID của yêu cầu chuyến đi (requestId)
    @SerializedName("requestId")
    val requestId: Long,

    // Trạng thái của yêu cầu (status) - ví dụ: "PENDING"
    @SerializedName("status")
    val status: String,

    // Thông báo cho người dùng
    @SerializedName("message")
    val message: String,

    // Thời gian yêu cầu được tạo
    // Server Spring Boot sử dụng LocalDateTime, Gson/Retrofit sẽ nhận nó dưới dạng chuỗi
    @SerializedName("createdAt")
    val createdAt: String
)