package com.trucdecomptable.cuissonvapeur.update

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Update state for the Réglages screen: check now (button + on-open), expose
 * the result, and start the APK download through DownloadManager. The
 * install itself is triggered by [UpdateDownloadReceiver] when the download
 * completes (the receiver persists the download id in SharedPreferences).
 */
@HiltViewModel
class UpdateViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val checker = UpdateChecker()

    private val _state = MutableStateFlow<UpdateUiState>(UpdateUiState.Idle)
    val state: StateFlow<UpdateUiState> = _state.asStateFlow()

    init {
        checkForUpdate()
    }

    fun checkForUpdate() {
        _state.value = UpdateUiState.Checking
        viewModelScope.launch {
            val info = withContext(Dispatchers.IO) { checker.check() }
            val error = checker.lastError
            _state.value = when {
                info != null -> UpdateUiState.Available(info)
                error != null -> UpdateUiState.Error(error)
                else -> UpdateUiState.UpToDate
            }
        }
    }

    /** Starts the APK download. Call only after [canRequestPackageInstalls]. */
    fun download(info: UpdateInfo) {
        val request = DownloadManager.Request(Uri.parse(info.downloadUrl)).apply {
            setTitle("Cuisson vapeur — mise à jour v${info.versionName}")
            setDescription(info.downloadUrl)
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "app-update.apk")
        }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val id = dm.enqueue(request)
        context.getSharedPreferences(UPDATE_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_DOWNLOAD_ID, id)
            .apply()
        _state.value = UpdateUiState.Downloading
    }

    companion object {
        const val UPDATE_PREFS = "update_prefs"
        const val KEY_DOWNLOAD_ID = "download_id"
    }
}
