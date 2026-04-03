package com.example.myhatd.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhatd.data.model.YeuCauChuyenDi // Import DTO chính xác
import com.example.myhatd.data.model.TripRequestResponse // Import Response DTO
import com.example.myhatd.data.network.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// State cho màn hình gửi thông tin chuyến đi
data class ChuyenDiState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
)

class ChuyenDiViewModel : ViewModel() {

    private val _state = MutableStateFlow(ChuyenDiState())
    val state: StateFlow<ChuyenDiState> get() = _state

    /**
     * Gửi yêu cầu chuyến đi (có thể bao gồm hẹn giờ từ DRIVER) lên Server Spring Boot.
     * Hàm này sử dụng DTO YeuCauChuyenDi đầy đủ.
     */
    fun sendChuyenDi(
        phoneNumber: String,
        role: String,
        tenDiemDi: String,
        tenDiemDen: String,
        viDoDiemDi: Double,
        kinhDoDiemDi: Double,
        viDoDiemDen: Double,
        kinhDoDiemDen: Double,
        scheduleTime: String? = null // Long Timestamp (null nếu là chuyến đi ngay lập tức)
    ) {
        // Kiểm tra cơ bản các tọa độ bắt buộc (có thể kiểm tra chặt chẽ hơn)
        if (viDoDiemDi == 0.0 || viDoDiemDen == 0.0) {
            _state.value = ChuyenDiState(
                isLoading = false,
                errorMessage = "Thiếu thông tin tọa độ Điểm đi/Điểm đến."
            )
            return
        }

        _state.value = ChuyenDiState(isLoading = true)

        viewModelScope.launch {
            try {
                // 1. TẠO DTO HOÀN CHỈNH
                val request = YeuCauChuyenDi(
                    tenDiemDi = tenDiemDi,
                    tenDiemDen = tenDiemDen,
                    viDoDiemDi = viDoDiemDi,
                    kinhDoDiemDi = kinhDoDiemDi,
                    viDoDiemDen = viDoDiemDen,
                    kinhDoDiemDen = kinhDoDiemDen,
                    soDienThoai = phoneNumber,
                    vaiTro = role,
                    thoiGianDriver = scheduleTime // ✅ Ánh xạ: Gửi Long Timestamp
                )

                // 2. GỌI API
                val response = RetrofitClient.apiService.taoYeuCauChuyenDi(request)

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    val message = responseBody?.message ?: "Gửi yêu cầu thành công"

                    _state.value = ChuyenDiState(
                        isLoading = false,
                        successMessage = message
                    )
                } else {
                    // Xử lý lỗi HTTP (4xx/5xx)
                    val errorBody = response.errorBody()?.string() ?: "Lỗi server không rõ."
                    _state.value = ChuyenDiState(
                        isLoading = false,
                        errorMessage = "Lỗi server ${response.code()}: ${errorBody}"
                    )
                }
            } catch (e: HttpException) {
                _state.value = ChuyenDiState(
                    isLoading = false,
                    errorMessage = "Lỗi HTTP ${e.code()}: Không thể kết nối hoặc lỗi cấu hình API."
                )
            } catch (e: IOException) {
                _state.value = ChuyenDiState(
                    isLoading = false,
                    errorMessage = "Lỗi mạng: Vui lòng kiểm tra kết nối Internet."
                )
            } catch (e: Exception) {
                // Lỗi Parsing hoặc lỗi không xác định
                _state.value = ChuyenDiState(
                    isLoading = false,
                    errorMessage = "Lỗi không xác định: ${e.localizedMessage}"
                )
            }
        }
    }

    /**
     * Đặt lại trạng thái để màn hình có thể xử lý hành động tiếp theo.
     */
    fun resetState() {
        _state.value = ChuyenDiState()
    }
}