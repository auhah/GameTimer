/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package cn.auhah.gametimer.permissions.rom

import android.annotation.TargetApi
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.util.Log
import cn.auhah.gametimer.permissions.SettingsCompat

object MeizuUtils {
  private val TAG = "MeizuUtils"

  /**
   * 检测 meizu 悬浮窗权限
   */
  fun checkFloatWindowPermission(context: Context): Boolean {
    val version = Build.VERSION.SDK_INT
    return if (version >= 19) {
      checkOp(context, 24) //OP_SYSTEM_ALERT_WINDOW = 24;
    } else true
  }

  /**
   * 去魅族权限申请页面
   */
  fun applyPermission(context: Context) {
    try {
      val intent = Intent("com.meizu.safe.security.SHOW_APPSEC")
      //            intent.setClassName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity");//remove this line code for fix flyme6.3
      intent.putExtra("packageName", context.packageName)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
      context.startActivity(intent)
    } catch (e: Exception) {
      try {
        Log.e(TAG, "获取悬浮窗权限, 打开AppSecActivity失败, " + Log.getStackTraceString(e))
        // 最新的魅族flyme 6.2.5 用上述方法获取权限失败, 不过又可以用下述方法获取权限了
        SettingsCompat.commonROMPermissionApplyInternal(context)
      } catch (eFinal: Exception) {
        Log.e(TAG, "获取悬浮窗权限失败, 通用获取方法失败, " + Log.getStackTraceString(eFinal))
      }

    }

  }

  @TargetApi(Build.VERSION_CODES.KITKAT)
  private fun checkOp(
    context: Context,
    op: Int
  ): Boolean {
    val version = Build.VERSION.SDK_INT
    if (version >= 19) {
      val manager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
      try {
        val clazz = AppOpsManager::class.java
        val method = clazz.getDeclaredMethod(
            "checkOp", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
            String::class.java
        )
        return AppOpsManager.MODE_ALLOWED == method.invoke(
            manager, op, Binder.getCallingUid(), context.packageName
        ) as Int
      } catch (e: Exception) {
        Log.e(TAG, Log.getStackTraceString(e))
      }

    } else {
      Log.e(TAG, "Below API 19 cannot invoke!")
    }
    return false
  }
}
