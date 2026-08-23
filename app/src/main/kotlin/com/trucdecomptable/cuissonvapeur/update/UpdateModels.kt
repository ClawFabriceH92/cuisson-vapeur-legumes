package com.trucdecomptable.cuissonvapeur.update

import org.json.JSONArray
import org.json.JSONObject

/** A newer release found on GitHub, ready to download. */
data class UpdateInfo(
    val versionName: String,
    val downloadUrl: String,
    val releaseNotes: String,
)

/** UI state of the update check (Réglages section). */
sealed interface UpdateUiState {
    data object Idle : UpdateUiState
    data object Checking : UpdateUiState
    data object UpToDate : UpdateUiState
    data class Available(val info: UpdateInfo) : UpdateUiState
    data object Downloading : UpdateUiState
    data class Error(val message: String) : UpdateUiState
}

/**
 * Pure parser for the GitHub Releases API response — no network, no Android
 * framework, unit-testable on the JVM (see ReleaseParserTest).
 *
 * Rules (from the auto-update skill):
 *  - drafts are ignored;
 *  - a release only counts if it ships an `.apk` asset;
 *  - the version is parsed from the tag (`v1.1`, `1.1.0` → `1.1`);
 *    non-version tags such as `debug-latest` are ignored;
 *  - the highest version strictly greater than [currentVersion] wins;
 *  - any JSON error returns null (robust to a malformed response).
 */
object ReleaseParser {

    private val VERSION_REGEX = Regex("""(\d+(?:\.\d+){1,2})""")

    fun parseReleases(json: String, currentVersion: String): UpdateInfo? {
        return try {
            val releases = JSONArray(json)
            var best: UpdateInfo? = null
            for (i in 0 until releases.length()) {
                val release = releases.getJSONObject(i)
                if (release.optBoolean("draft", false)) continue
                val version = parseVersion(release) ?: continue
                if (compareVersions(version, currentVersion) <= 0) continue
                val apkUrl = findApkUrl(release) ?: continue
                if (best == null || compareVersions(version, best.versionName) > 0) {
                    best = UpdateInfo(
                        versionName = version,
                        downloadUrl = apkUrl,
                        releaseNotes = release.optString("body", ""),
                    )
                }
            }
            best
        } catch (e: Exception) {
            null
        }
    }

    /** Version is parsed from the tag (`v1.1`) or, failing that, from an
     *  asset name (`app-v1.1.apk`) — so a rolling `latest` tag still yields
     *  a usable version. `debug-latest` + `app-debug.apk` yields null. */
    private fun parseVersion(release: JSONObject): String? {
        VERSION_REGEX.find(release.optString("tag_name", ""))?.let { return it.groupValues[1] }
        val assets = release.optJSONArray("assets") ?: return null
        for (i in 0 until assets.length()) {
            VERSION_REGEX.find(assets.getJSONObject(i).optString("name", ""))
                ?.let { return it.groupValues[1] }
        }
        return null
    }

    private fun findApkUrl(release: JSONObject): String? {
        val assets = release.optJSONArray("assets") ?: return null
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            if (asset.optString("name", "").endsWith(".apk")) {
                asset.optString("browser_download_url").takeIf { it.isNotBlank() }?.let { return it }
            }
        }
        return null
    }

    /** Segment-by-segment numeric comparison: 1.10 > 1.9 (never lexical). */
    fun compareVersions(a: String, b: String): Int {
        val sa = a.split(".").map { it.toIntOrNull() ?: 0 }
        val sb = b.split(".").map { it.toIntOrNull() ?: 0 }
        val n = maxOf(sa.size, sb.size)
        for (i in 0 until n) {
            val x = sa.getOrElse(i) { 0 }
            val y = sb.getOrElse(i) { 0 }
            if (x != y) return x - y
        }
        return 0
    }
}
