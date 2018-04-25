/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package cn.auhah.gametimer.permissions.rom

import android.os.Build
import android.text.TextUtils
import android.util.Log

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Description:
 *
 * @author zhaozp
 * @since 2016-05-23
 */
object RomUtils {
  private const val TAG = "RomUtils"

  /**
   * 获取 emui 版本号
   * @return
   */
  val emuiVersion: Double
    get() {
      try {
        val emuiVersion = getSystemProperty("ro.build.version.emui")
        val version = emuiVersion!!.substring(emuiVersion.indexOf("_") + 1)
        return java.lang.Double.parseDouble(version)
      } catch (e: Exception) {
        e.printStackTrace()
      }

      return 4.0
    }

  /**
   * 获取小米 rom 版本号，获取失败返回 -1
   *
   * @return miui rom version code, if fail , return -1
   */
  val miuiVersion: Int
    get() {
      val version = getSystemProperty("ro.miui.ui.version.name")
      if (version != null) {
        try {
          return Integer.parseInt(version.substring(1))
        } catch (e: Exception) {
          Log.e(TAG, "get miui version code error, version : $version")
        }

      }
      return -1
    }

  fun getSystemProperty(propName: String): String? {
    val line: String
    var input: BufferedReader? = null
    try {
      val p = Runtime.getRuntime()
          .exec("getprop $propName")
      input = BufferedReader(InputStreamReader(p.inputStream), 1024)
      line = input.readLine()
      input.close()
    } catch (ex: IOException) {
      Log.e(TAG, "Unable to read sysprop $propName", ex)
      return null
    } finally {
      if (input != null) {
        try {
          input.close()
        } catch (e: IOException) {
          Log.e(TAG, "Exception while closing InputStream", e)
        }

      }
    }
    return line
  }

  fun checkIsHuaweiRom(): Boolean {
    return Build.MANUFACTURER.contains("HUAWEI")
  }

  /**
   * check if is miui ROM
   */
  fun checkIsMiuiRom(): Boolean {
    return !TextUtils.isEmpty(getSystemProperty("ro.miui.ui.version.name"))
  }

  fun checkIsMeizuRom(): Boolean {
    //return Build.MANUFACTURER.contains("Meizu");
    val meizuFlymeOSFlag = getSystemProperty("ro.build.display.id")
    return if (TextUtils.isEmpty(meizuFlymeOSFlag)) {
      false
    } else meizuFlymeOSFlag!!.contains("flyme") || meizuFlymeOSFlag.toLowerCase().contains(
        "flyme"
    )
  }

  fun checkIs360Rom(): Boolean {
    //fix issue https://github.com/zhaozepeng/FloatWindowPermission/issues/9
    return Build.MANUFACTURER.contains("QiKU") || Build.MANUFACTURER.contains("360")
  }

  fun checkIsOppoRom(): Boolean {
    //https://github.com/zhaozepeng/FloatWindowPermission/pull/26
    return Build.MANUFACTURER.contains("OPPO") || Build.MANUFACTURER.contains("oppo")
  }
}
