package com.trucdecomptable.cuissonvapeur.update

import com.trucdecomptable.cuissonvapeur.BuildConfig
import java.net.HttpURLConnection
import java.net.URL

/**
 * Network half of the update check: hits the public GitHub Releases API
 * (no token — 60 req/h/IP is plenty for a personal app) and delegates the
 * parsing to [ReleaseParser].
 *
 * `check()` returns null both when the app is up to date AND on any network
 * error; use [lastError] to tell the two apart in the UI (a 403 or timeout
 * must never masquerade as "✅ à jour").
 */
class UpdateChecker(
    private val owner: String = "ClawFabriceH92",
    private val repo: String = "cuisson-vapeur-legumes",
    private val currentVersion: String = BuildConfig.VERSION_NAME,
) {

    @Volatile
    var lastError: String? = null
        private set

    /** @return the newest release newer than the installed version, or null. */
    fun check(): UpdateInfo? {
        lastError = null
        return try {
            val url = URL("https://api.github.com/repos/$owner/$repo/releases?per_page=5")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("Accept", "application/vnd.github+json")
            conn.setRequestProperty("User-Agent", "CuissonVapeur-Android/1.1")
            try {
                if (conn.responseCode != 200) {
                    lastError = "HTTP ${conn.responseCode}"
                    return null
                }
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                ReleaseParser.parseReleases(body, currentVersion)
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            lastError = e.message ?: e.javaClass.simpleName
            null
        }
    }
}
