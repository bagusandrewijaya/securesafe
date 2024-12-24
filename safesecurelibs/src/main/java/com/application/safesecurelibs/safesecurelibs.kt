// SafeSecureLibs.kt
package com.application.safesecurelibs

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader
//add notes
class SafeSecureLibs(private val context: Context) {

    fun getSecurityStatus(): Map<String, Boolean> {
        return mapOf(
            "isDevModeEnabled" to isDevModeEnabled(),
            "isRooted" to isDeviceRooted(),
            "hasDangerousApps" to checkForDangerousApps(),
            "hasHiddenProperties" to checkHiddenProperties()
        )
    }

    fun isDevModeEnabled(): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
            0
        ) != 0
    }

    fun isDeviceRooted(): Boolean {
        return checkRootMethod1() ||
                checkRootMethod2() ||
                checkRootMethod3() ||
                checkForMagiskFiles() ||
                checkForBusyboxBinary()
    }

    private fun checkRootMethod1(): Boolean {
        val rootPaths = arrayOf(
            "/system/app/Superuser.apk",
            "/system/xbin/su",
            "/system/bin/su",
            "/sbin/su",
            "/system/sd/xbin/su",
            "/system/bin/failsafe/su",
            "/data/local/xbin/su",
            "/data/local/bin/su",
            "/system/bin/.ext/.su",
            "/system/xbin/.ext/.su",
            "/data/adb/magisk",
            "/system/etc/init.d/99SuperSUDaemon",
            "/dev/magisk/mirror"
        )

        for (path in rootPaths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun checkRootMethod2(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("su"))
            true
        } catch (e: Exception) {
            false
        } finally {
            process?.destroy()
        }
    }

    private fun checkRootMethod3(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkForMagiskFiles(): Boolean {
        val magiskPaths = arrayOf(
            "/data/adb/magisk",
            "/data/adb/modules",
            "/data/local/tmp/magisk.db",
            "/cache/magisk.log",
            "/data/user/0/com.topjohnwu.magisk",
            "/data/user_de/0/com.topjohnwu.magisk",
            "/data/data/com.topjohnwu.magisk"
        )

        for (path in magiskPaths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun checkForBusyboxBinary(): Boolean {
        val busyboxPaths = arrayOf(
            "/system/xbin/busybox",
            "/system/bin/busybox",
            "/data/local/busybox",
            "/data/local/xbin/busybox",
            "/system/sd/xbin/busybox"
        )

        for (path in busyboxPaths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun checkForDangerousApps(): Boolean {
        val dangerousApps = arrayOf(
            "com.topjohnwu.magisk",
            "com.thirdparty.superuser",
            "eu.chainfire.supersu",
            "com.noshufou.android.su",
            "com.koushikdutta.superuser",
            "com.zachspong.temprootremovejb",
            "com.ramdroid.appquarantine",
            "com.touchtype.swiftkey.beta",
            "com.devadvance.rootcloak",
            "com.saurik.substrate",
            "com.amphoras.hidemyroot",
            "com.formyhm.hideroot",
            "com.manning.xposed.installer"
        )

        val packageManager = context.packageManager
        for (appName in dangerousApps) {
            try {
                packageManager.getPackageInfo(appName, PackageManager.GET_ACTIVITIES)
                return true
            } catch (e: PackageManager.NameNotFoundException) {
                continue
            }
        }
        return false
    }

    private fun checkHiddenProperties(): Boolean {
        val props = arrayOf(
            "ro.debuggable",
            "ro.secure",
            "ro.build.type",
            "ro.build.tags",
            "ro.build.selinux"
        )

        try {
            val process = Runtime.getRuntime().exec("getprop")
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readText()

            for (prop in props) {
                if (output.contains("$prop=[1]") ||
                    output.contains("$prop=[eng]") ||
                    output.contains("$prop=[userdebug]")) {
                    return true
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}