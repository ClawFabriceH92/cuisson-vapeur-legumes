package com.trucdecomptable.cuissonvapeur.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File

/**
 * Waits for the in-app APK download to complete, then hands the file to the
 * system installer. Registered in the manifest with exported=false; system
 * broadcasts (ACTION_DOWNLOAD_COMPLETE) still reach non-exported receivers.
 * Ignores any download id that is not the one the app started.
 */
class UpdateDownloadReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != DownloadManager.ACTION_DOWNLOAD_COMPLETE) return
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        val prefs = context.getSharedPreferences(UpdateViewModel.UPDATE_PREFS, Context.MODE_PRIVATE)
        val expected = prefs.getLong(UpdateViewModel.KEY_DOWNLOAD_ID, -1L)
        if (id != expected) return
        prefs.edit().remove(UpdateViewModel.KEY_DOWNLOAD_ID).apply()
        installApk(context)
    }

    private fun installApk(context: Context) {
        val file = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
            "app-update.apk",
        )
        if (!file.exists()) return
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val install = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(install)
    }
}
