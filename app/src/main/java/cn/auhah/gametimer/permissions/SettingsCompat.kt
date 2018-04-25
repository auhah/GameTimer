/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package cn.auhah.gametimer.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import cn.auhah.gametimer.permissions.rom.HuaweiUtils
import cn.auhah.gametimer.permissions.rom.MeizuUtils
import cn.auhah.gametimer.permissions.rom.MiuiUtils
import cn.auhah.gametimer.permissions.rom.OppoUtils
import cn.auhah.gametimer.permissions.rom.QikuUtils
import cn.auhah.gametimer.permissions.rom.RomUtils

/**
 * Description:
 *
 * @author zhaozp
 * @since 2016-10-17
 */

object SettingsCompat {
  private const val TAG = "SettingsCompat"

  fun canDrawOverlays(context: Context): Boolean {
    //6.0 版本之后由于 google 增加了对悬浮窗权限的管理，所以方式就统一了
    if (Build.VERSION.SDK_INT < 23) {
      when {
        RomUtils.checkIsMiuiRom() -> return miuiPermissionCheck(context)
        RomUtils.checkIsMeizuRom() -> return meizuPermissionCheck(context)
        RomUtils.checkIsHuaweiRom() -> return huaweiPermissionCheck(context)
        RomUtils.checkIs360Rom() -> return qikuPermissionCheck(context)
        RomUtils.checkIsOppoRom() -> return oppoROMPermissionCheck(context)
        else -> {
        }
      }
    }
    return commonROMPermissionCheck(context)
  }

  private fun huaweiPermissionCheck(context: Context): Boolean {
    return HuaweiUtils.checkFloatWindowPermission(context)
  }

  private fun miuiPermissionCheck(context: Context): Boolean {
    return MiuiUtils.checkFloatWindowPermission(context)
  }

  private fun meizuPermissionCheck(context: Context): Boolean {
    return MeizuUtils.checkFloatWindowPermission(context)
  }

  private fun qikuPermissionCheck(context: Context): Boolean {
    return QikuUtils.checkFloatWindowPermission(context)
  }

  private fun oppoROMPermissionCheck(context: Context): Boolean {
    return OppoUtils.checkFloatWindowPermission(context)
  }

  private fun commonROMPermissionCheck(context: Context): Boolean {
    //最新发现魅族6.0的系统这种方式不好用，天杀的，只有你是奇葩，没办法，单独适配一下
    if (RomUtils.checkIsMeizuRom()) {
      return meizuPermissionCheck(context)
    } else {
      var result: Boolean? = true
      if (Build.VERSION.SDK_INT >= 23) {
        try {
          val clazz = Settings::class.java
          val canDrawOverlays = clazz.getDeclaredMethod("canDrawOverlays", Context::class.java)
          result = canDrawOverlays.invoke(null, context) as Boolean
        } catch (e: Exception) {
          Log.e(TAG, Log.getStackTraceString(e))
        }

      }
      return result!!
    }
  }

  fun manageDrawOverlays(context: Context) {
    if (Build.VERSION.SDK_INT < 23) {
      when {
        RomUtils.checkIsMiuiRom() -> miuiROMPermissionApply(context)
        RomUtils.checkIsMeizuRom() -> meizuROMPermissionApply(context)
        RomUtils.checkIsHuaweiRom() -> huaweiROMPermissionApply(context)
        RomUtils.checkIs360Rom() -> rom360PermissionApply(context)
        RomUtils.checkIsOppoRom() -> oppoROMPermissionApply(context)
      }
    } else {
      commonROMPermissionApply(context)
    }
  }

  private fun rom360PermissionApply(context: Context) {
    QikuUtils.applyPermission(context)
  }

  private fun huaweiROMPermissionApply(context: Context) {
    HuaweiUtils.applyPermission(context)
  }

  private fun meizuROMPermissionApply(context: Context) {
    MeizuUtils.applyPermission(context)
  }

  private fun miuiROMPermissionApply(context: Context) {
    MiuiUtils.applyMiuiPermission(context)
  }

  private fun oppoROMPermissionApply(context: Context) {
    OppoUtils.applyOppoPermission(context)
  }

  /**
   * 通用 rom 权限申请
   */
  private fun commonROMPermissionApply(context: Context) {
    //这里也一样，魅族系统需要单独适配
    if (Build.VERSION.SDK_INT >= 23) {
      if (RomUtils.checkIsMeizuRom()) {
        meizuROMPermissionApply(context)
      } else {
        try {
          commonROMPermissionApplyInternal(context)
        } catch (e: Exception) {
          Log.e(TAG, Log.getStackTraceString(e))
        }

      }
    }
  }

  @Throws(NoSuchFieldException::class, IllegalAccessException::class)
  public fun commonROMPermissionApplyInternal(context: Context) {
    val clazz = Settings::class.java
    val field = clazz.getDeclaredField("ACTION_MANAGE_OVERLAY_PERMISSION")

    val intent = Intent(field.get(null).toString())
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.data = Uri.parse("package:" + context.packageName)
    context.startActivity(intent)
  }
}
