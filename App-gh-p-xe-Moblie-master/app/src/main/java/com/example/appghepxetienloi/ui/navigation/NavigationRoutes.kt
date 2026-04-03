package com.example.myhatd.ui.navigation

import java.net.URLEncoder

/**
 * Định nghĩa các routes cho navigation
 */
object NavigationRoutes {
//    const val AUTH_FLOW = "auth_flow"

    const val GIOI_THIEU = "gioithieu"
    const val HOME = "home"
    const val MAP = "map"
    const val PHONE_AUTH = "phone_auth"
    const val VERIFY_OTP = "verify_otp"
    const val INTRO = "intro"

    const val DANG_KY_DRIVER = "dang_ky_driver"

    const val THONG_BAO_USER = "thong_bao_user"

    const val HOME_DRIVER = "home_driver"

    const val TIM_DIA_CHI = "tim_dia_chi"

    const val TIM_DIA_CHI_DRIVER = "tim_dia_chi_driver"


    // ✅ SỬA ĐỔI: Thêm {phoneNumber} và {role} vào định nghĩa route.

    const val XAC_NHAN_DIEM_DON = "xac_nhan_diem_don"

    const val CHO_SOCKET_BASE = "cho_socket"
    const val CHO_SOCKET = "$CHO_SOCKET_BASE/{userPhone}"

    const val XAC_NHAN_DAT_XE = "xac_nhan_dat_xe"

    // ✅ THÊM ROUTE THÔNG TIN CHUYẾN ĐI
    const val THEO_DOI_LO_TRINH = "theo_doi_lo_trinh"

    const val CHI_TIET_CHUYEN_DI_USER = "chi_tiet_chuyen_di_user"

    const val DRIVER_RIDE_DETAIL_BASE = "driver_ride_detail"

    const val DRIVER_TRACKING = "driver_tracking_ride"

    const val DU_DOAN_DRIVER = "du_doan_driver"
    const val DRIVER_RIDE_DETAIL = "$DRIVER_RIDE_DETAIL_BASE/{matchId}"

    const val CHI_TIET_CHUYEN_DI_DRIVER_BASE = "chi_tiet_chuyen_di_driver"
    const val CHI_TIET_CHUYEN_DI_DRIVER = "$CHI_TIET_CHUYEN_DI_DRIVER_BASE/{matchId}" // Định nghĩa route với tham số

    fun createDriverRideDetailRoute(matchId: Long): String {
        return "$DRIVER_RIDE_DETAIL_BASE/$matchId"
    }

    const val REVIEW_SCREEN_BASE = "review_screen"
    const val REVIEW_SCREEN = "$REVIEW_SCREEN_BASE/{matchId}"

    /**
     * Tạo route cho màn hình hẹn giờ của Tài xế.
     * Đã cập nhật để bao gồm phoneNumber và role.
     */

    fun createChoSocketRoute(phoneNumber: String): String {
        return "$CHO_SOCKET_BASE/$phoneNumber"
    }

    fun createReviewScreenRoute(matchId: Long): String {
        return "$REVIEW_SCREEN_BASE/$matchId"
    }
    fun createDuDoanDriverRoute(): String {
        return DU_DOAN_DRIVER
    }
}