package com.trucdecomptable.cuissonvapeur.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Pure parser tests — no network, no Android framework (org.json on the JVM). */
class ReleaseParserTest {

    private fun releaseJson(vararg releases: String): String = "[${releases.joinToString(",")}]"

    private fun release(
        tag: String,
        apk: String? = "app-${tag}.apk", // tag already includes the "v": v1.3 -> app-v1.3.apk
        draft: Boolean = false,
        body: String = "",
    ): String {
        val assets = if (apk != null) {
            """[{"name": "$apk", "browser_download_url": "https://github.com/x/y/releases/download/$tag/$apk"}]"""
        } else {
            "[]"
        }
        return """{"tag_name": "$tag", "draft": $draft, "body": "$body", "assets": $assets}"""
    }

    @Test
    fun `higher version with apk asset wins`() {
        val json = releaseJson(
            release("v1.0", body = "old"),
            release("v1.2"),
            release("v1.1"),
        )
        val info = ReleaseParser.parseReleases(json, "1.0")
        assertEquals("1.2", info?.versionName)
    }

    @Test
    fun `same or lower version than installed is ignored`() {
        val json = releaseJson(release("v1.0"), release("v0.9"))
        assertNull(ReleaseParser.parseReleases(json, "1.0"))
    }

    @Test
    fun `draft releases are ignored`() {
        val json = releaseJson(release("v2.0", draft = true))
        assertNull(ReleaseParser.parseReleases(json, "1.0"))
    }

    @Test
    fun `release without apk asset is ignored`() {
        val json = releaseJson(release("v2.0", apk = null))
        assertNull(ReleaseParser.parseReleases(json, "1.0"))
    }

    @Test
    fun `non version tag like debug-latest is ignored`() {
        val json = releaseJson(
            """{"tag_name": "debug-latest", "draft": false, "body": "", "assets": [{"name": "app-debug.apk", "browser_download_url": "https://github.com/x/y/releases/download/debug-latest/app-debug.apk"}]}""",
        )
        assertNull(ReleaseParser.parseReleases(json, "1.0"))
    }

    @Test
    fun `rolling latest tag with versioned asset still yields a version`() {
        val json = releaseJson(
            """{"tag_name": "latest", "draft": false, "body": "", "assets": [{"name": "app-v1.1.apk", "browser_download_url": "https://github.com/x/y/releases/download/latest/app-v1.1.apk"}]}""",
        )
        val info = ReleaseParser.parseReleases(json, "1.0")
        assertEquals("1.1", info?.versionName)
        assertEquals(
            "https://github.com/x/y/releases/download/latest/app-v1.1.apk",
            info?.downloadUrl,
        )
    }

    @Test
    fun `malformed json returns null instead of crashing`() {
        assertNull(ReleaseParser.parseReleases("not json at all", "1.0"))
        assertNull(ReleaseParser.parseReleases("", "1.0"))
    }

    @Test
    fun `version comparison is numeric not lexical`() {
        assertEquals(1, ReleaseParser.compareVersions("1.10", "1.9"))
        assertEquals(-1, ReleaseParser.compareVersions("1.9", "1.10"))
        assertEquals(0, ReleaseParser.compareVersions("1.1", "1.1.0"))
        assertEquals(1, ReleaseParser.compareVersions("1.1.1", "1.1"))
    }

    @Test
    fun `url of the apk asset is returned`() {
        val json = releaseJson(release("v1.3"))
        val info = ReleaseParser.parseReleases(json, "1.0")
        assertEquals(
            "https://github.com/x/y/releases/download/v1.3/app-v1.3.apk",
            info?.downloadUrl,
        )
    }
}
