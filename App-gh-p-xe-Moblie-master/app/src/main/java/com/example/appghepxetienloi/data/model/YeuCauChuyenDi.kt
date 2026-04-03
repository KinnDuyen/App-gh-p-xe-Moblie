package com.example.myhatd.data.model

data class YeuCauChuyenDi(
    // Thông tin địa điểm
    val tenDiemDi: String,
    val tenDiemDen: String,
    val viDoDiemDi: Double,
    val kinhDoDiemDi: Double,
    val viDoDiemDen: Double,
    val kinhDoDiemDen: Double,

    // Thông tin người dùng
    val soDienThoai: String,
    val vaiTro: String, // ✅ ĐÃ XÓA GIÁ TRỊ MẶC ĐỊNH ("User")

    val thoiGianDriver: String? = null
)