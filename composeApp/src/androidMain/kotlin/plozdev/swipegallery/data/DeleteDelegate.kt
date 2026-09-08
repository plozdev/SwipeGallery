package plozdev.swipegallery.data

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object DeleteDelegate {
    private var launcher: ActivityResultLauncher<IntentSenderRequest>? = null
    private var continuation: CancellableContinuation<Boolean>? = null

    fun registerLauncher(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        this.launcher = launcher
    }

    fun unregisterLauncher() {
        this.launcher = null
        this.continuation = null
    }

    suspend fun requestDelete(intentSenderRequest: IntentSenderRequest): Boolean =
        suspendCancellableCoroutine { cont ->
            val l = launcher
            if (l != null) {
                continuation = cont
                l.launch(intentSenderRequest)
            } else {
                cont.resume(false)
            }
        }

    fun onDeleteResult(isSuccess: Boolean) {
        continuation?.resume(isSuccess)
        continuation = null
    }
}
