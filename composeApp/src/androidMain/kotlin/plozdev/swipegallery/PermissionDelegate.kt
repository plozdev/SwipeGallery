package plozdev.swipegallery

import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object PermissionDelegate {
    private var launcher: ActivityResultLauncher<Array<String>>? = null
    private var continuation: CancellableContinuation<Boolean>? = null


    fun registerLauncher(launcher: ActivityResultLauncher<Array<String>>) {
        this.launcher = launcher
    }

    fun unregisterLauncher() {
        this.launcher = null
        this.continuation = null
    }

    // Hàm suspend nhận lệnh yêu cầu quyền và treo coroutine cho tới khi có kết quả
    suspend fun requestPermissions(permissions: Array<String>): Boolean = suspendCancellableCoroutine { cont ->
        val l = launcher
        if (l != null) {
            continuation = cont
            l.launch(permissions)
        } else {
            // Trả về false ngay lập tức nếu launcher chưa được đăng ký
            cont.resume(false)
        }
    }

    // Callback nhận kết quả từ Activity và giải phóng coroutine đang treo
    fun onPermissionResult(results: Map<String, Boolean>) {
        val isGranted = results.values.any { it } // Trả về true nếu được cấp ít nhất một quyền (ví dụ quyền giới hạn)
        continuation?.resume(isGranted)
        continuation = null
    }
}