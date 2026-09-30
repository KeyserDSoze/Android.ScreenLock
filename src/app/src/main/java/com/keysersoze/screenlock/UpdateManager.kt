package com.keysersoze.screenlock

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class UpdateInfo(
    val version: String,
    val apkUrl: String,
)

object UpdateManager {
    private const val RELEASE_API =
        "https://api.github.com/repos/KeyserDSoze/Android.ScreenLock/releases/latest"
    private const val PREFS = "screen_lock_updater"
    private const val KEY_DOWNLOAD_ID = "download_id"
    private const val KEY_DOWNLOAD_VERSION = "download_version"

    suspend fun checkLatest(): UpdateInfo? = withContext(Dispatchers.IO) {
        val connection = (URL(RELEASE_API).openConnection() as HttpURLConnection).apply {
            connectTimeout = 8_000
            readTimeout = 8_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "ScreenLock/${BuildConfig.VERSION_NAME}")
        }

        try {
            if (connection.responseCode !in 200..299) return@withContext null
            val payload = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(payload)
            val version = json.optString("tag_name").removePrefix("v")
            if (!isNewer(version, BuildConfig.VERSION_NAME)) return@withContext null

            val assets = json.optJSONArray("assets") ?: return@withContext null
            var apkUrl: String? = null
            for (index in 0 until assets.length()) {
                val asset = assets.optJSONObject(index) ?: continue
                val name = asset.optString("name")
                if (name.endsWith(".apk", ignoreCase = true)) {
                    apkUrl = asset.optString("browser_download_url")
                    break
                }
            }

            apkUrl?.takeIf { it.startsWith("https://") }?.let {
                UpdateInfo(version = version, apkUrl = it)
            }
        } finally {
            connection.disconnect()
        }
    }

    fun enqueue(context: Context, info: UpdateInfo): Long {
        val manager = context.getSystemService(DownloadManager::class.java)
        val request = DownloadManager.Request(Uri.parse(info.apkUrl))
            .setTitle("Screen Lock ${info.version}")
            .setDescription("Scaricamento aggiornamento")
            .setMimeType("application/vnd.android.package-archive")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(false)
            .setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED,
            )
            .setDestinationInExternalFilesDir(
                context,
                Environment.DIRECTORY_DOWNLOADS,
                "screen-lock-${info.version}.apk",
            )

        val id = manager.enqueue(request)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_DOWNLOAD_ID, id)
            .putString(KEY_DOWNLOAD_VERSION, info.version)
            .apply()
        return id
    }

    fun currentDownloadId(context: Context): Long =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getLong(KEY_DOWNLOAD_ID, -1L)

    fun clearDownload(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_DOWNLOAD_ID)
            .remove(KEY_DOWNLOAD_VERSION)
            .apply()
    }

    fun isComplete(context: Context, id: Long = currentDownloadId(context)): Boolean {
        if (id < 0L) return false
        val manager = context.getSystemService(DownloadManager::class.java)
        manager.query(DownloadManager.Query().setFilterById(id)).use { cursor ->
            if (!cursor.moveToFirst()) return false
            val index = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            return index >= 0 && cursor.getInt(index) == DownloadManager.STATUS_SUCCESSFUL
        }
    }

    fun canInstallPackages(context: Context): Boolean =
        Build.VERSION.SDK_INT < 26 || context.packageManager.canRequestPackageInstalls()

    fun openInstallPermission(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        context.startActivity(
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }

    fun installDownloaded(context: Context, id: Long = currentDownloadId(context)): Boolean {
        if (id < 0L || !isComplete(context, id)) return false
        val manager = context.getSystemService(DownloadManager::class.java)
        val uri = manager.getUriForDownloadedFile(id) ?: return false

        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, "application/vnd.android.package-archive")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    private fun isNewer(candidate: String, current: String): Boolean {
        val left = candidate.split('.').mapNotNull(String::toIntOrNull)
        val right = current.split('.').mapNotNull(String::toIntOrNull)
        if (left.size != 3 || right.size != 3) return false

        for (index in 0..2) {
            if (left[index] != right[index]) return left[index] > right[index]
        }
        return false
    }
}
