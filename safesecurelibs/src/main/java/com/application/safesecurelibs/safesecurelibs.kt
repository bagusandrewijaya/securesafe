package com.application.safesecurelibs

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import java.io.File
import java.io.BufferedReader
import java.io.InputStreamReader
import android.util.Log

class SafeSecureLibs(private val context: Context) {
    companion object {
        private const val TAG = "SafeSecureLibs"
    }

    fun getSecurityStatus(): Map<String, Boolean> {
        return try {
            mapOf(
                "isDevModeEnabled" to isDevModeEnabled(),
                "isRooted" to isDeviceRooted(),
                "hasDangerousApps" to checkForDangerousApps(),
                "hasHiddenProperties" to checkHiddenProperties()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting security status", e)
            mapOf(
                "isDevModeEnabled" to false,
                "isRooted" to false,
                "hasDangerousApps" to false,
                "hasHiddenProperties" to false
            )
        }
    }

    fun isDevModeEnabled(): Boolean {
        return try {
            Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0
            ) != 0
        } catch (e: Exception) {
            Log.e(TAG, "Error checking dev mode", e)
            false
        }
    }

    fun isDeviceRooted(): Boolean {
        return try {
            checkRootMethod1() ||
                    checkRootMethod2() ||
                    checkRootMethod3() ||
                    checkForMagiskFiles() ||
                    checkForBusyboxBinary()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking root status", e)
            false
        }
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

        return try {
            rootPaths.any { File(it).exists() }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception checking root paths", e)
            false
        }
    }

    private fun checkRootMethod2(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("su"))
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error executing su command", e)
            false
        } finally {
            try {
                process?.destroy()
            } catch (e: Exception) {
                Log.e(TAG, "Error destroying process", e)
            }
        }
    }

    private fun checkRootMethod3(): Boolean {
        return try {
            Build.TAGS?.contains("test-keys") ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error checking build tags", e)
            false
        }
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

        return try {
            magiskPaths.any { File(it).exists() }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception checking Magisk files", e)
            false
        }
    }

    private fun checkForBusyboxBinary(): Boolean {
        val busyboxPaths = arrayOf(
            "/system/xbin/busybox",
            "/system/bin/busybox",
            "/data/local/busybox",
            "/data/local/xbin/busybox",
            "/system/sd/xbin/busybox"
        )

        return try {
            busyboxPaths.any { File(it).exists() }
        } catch (e: SecurityException) {
            Log.e(TAG, "Security exception checking Busybox binary", e)
            false
        }
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

        return try {
            val packageManager = context.packageManager
            dangerousApps.any { appName ->
                try {
                    packageManager.getPackageInfo(appName, PackageManager.GET_ACTIVITIES)
                    true
                } catch (e: PackageManager.NameNotFoundException) {
                    false
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking dangerous apps", e)
            false
        }
    }

    private fun checkHiddenProperties(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("getprop")
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                val output = reader.readText()
                val props = arrayOf(
                    "ro.debuggable",
                    "ro.secure",
                    "ro.build.type",
                    "ro.build.tags",
                    "ro.build.selinux"
                )

                props.any { prop ->
                    output.contains("$prop=[1]") ||
                            output.contains("$prop=[eng]") ||
                            output.contains("$prop=[userdebug]")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking hidden properties", e)
            false
        }
    }
}