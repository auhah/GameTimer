/*
 * Copyright (C) 2016 Facishare Technology Co., Ltd. All Rights Reserved.
 */
package cn.auhah.gametimer.permissions.rom

import android.annotation.TargetApi
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.provider.Settings
import android.util.Log

object MiuiUtils {
  private const val TAG = "MiuiUtils"

  /**
   * 获取小米 rom 版本号，获取失败返回 -1
   *
   * @return miui rom version code, if fail , return -1
   */
  private val miuiVersion: Int
    get() {
      val version = RomUtils.getSystemProperty("ro.miui.ui.version.name")
      if (version != null) {
        try {
          return Integer.parseInt(version.substring(1))
        } catch (e: Exception) {
          Log.e(TAG, "get miui version code error, version : $version")
          Log.e(TAG, Log.getStackTraceString(e))
        }

      }
      return -1
    }

  /**
   * 检测 miui 悬浮窗权限
   */
  fun checkFloatWindowPermission(context: Context): Boolean {
    val version = Build.VERSION.SDK_INT

    return if (version >= 19) {
      checkOp(context, 24) //OP_SYSTEM_ALERT_WINDOW = 24;
    } else {
      //            if ((context.getApplicationInfo().flags & 1 << 27) == 1) {
      //                return true;
      //            } else {
      //                return false;
      //            }
      true
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

  /**
   * 小米 ROM 权限申请
   */
  fun applyMiuiPermission(context: Context) {
    val versionCode = miuiVersion
    when (versionCode) {
      5 -> goToMiuiPermissionActivityV5(context)
      6 -> goToMiuiPermissionActivityV6(context)
      7 -> goToMiuiPermissionActivityV7(context)
      8 -> goToMiuiPermissionActivityV8(context)
      else -> Log.e(TAG, "this is a special MIUI rom version, its version code $versionCode")
    }
  }

  private fun isIntentAvailable(
    intent: Intent?,
    context: Context
  ): Boolean {
    return if (intent == null) {
      false
    } else context.packageManager.queryIntentActivities(
        intent, PackageManager.MATCH_DEFAULT_ONLY
    ).size > 0
  }

  /**
   * 小米 V5 版本 ROM权限申请
   */
  private fun goToMiuiPermissionActivityV5(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    val packageName = context.packageName
    val uri = Uri.fromParts("package", packageName, null)
    intent.data = uri
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    if (isIntentAvailable(intent, context)) {
      context.startActivity(intent)
    } else {
      Log.e(TAG, "intent is not available!")
    }

    //设置页面在应用详情页面
    //        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
    //        PackageInfo pInfo = null;
    //        try {
    //            pInfo = context.getPackageManager().getPackageInfo
    //                    (HostInterfaceManager.getHostInterface().getApp().getPackageName(), 0);
    //        } catch (PackageManager.NameNotFoundException e) {
    //            AVLogUtils.e(TAG, e.getMessage());
    //        }
    //        intent.setClassName("com.android.settings", "com.miui.securitycenter.permission.AppPermissionsEditor");
    //        intent.putExtra("extra_package_uid", pInfo.applicationInfo.uid);
    //        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //        if (isIntentAvailable(intent, context)) {
    //            context.startActivity(intent);
    //        } else {
    //            AVLogUtils.e(TAG, "Intent is not available!");
    //        }
  }

  /**
   * 小米 V6 版本 ROM权限申请
   */
  private fun goToMiuiPermissionActivityV6(context: Context) {
    val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
    intent.setClassName(
        "com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
    )
    intent.putExtra("extra_pkgname", context.packageName)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

    if (isIntentAvailable(intent, context)) {
      context.startActivity(intent)
    } else {
      Log.e(TAG, "Intent is not available!")
    }
  }

  /**
   * 小米 V7 版本 ROM权限申请
   */
  private fun goToMiuiPermissionActivityV7(context: Context) {
    val intent = Intent("miui.intent.action.APP_PERM_EDITOR")
    intent.setClassName(
        "com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity"
    )
    intent.putExtra("extra_pkgname", context.packageName)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

    if (isIntentAvailable(intent, context)) {
      context.startActivity(intent)
    } else {
      Log.e(TAG, "Intent is not available!")
    }
  }

  /**
   * 小米 V8 版本 ROM权限申请
   */
  private fun goToMiuiPermissionActivityV8(context: Context) {
    var intent = Intent("miui.intent.action.APP_PERM_EDITOR")
    intent.setClassName(
        "com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity"
    )
    //        intent.setPackage("com.miui.securitycenter");
    intent.putExtra("extra_pkgname", context.packageName)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

    if (isIntentAvailable(intent, context)) {
      context.startActivity(intent)
    } else {
      intent = Intent("miui.intent.action.APP_PERM_EDITOR")
      intent.`package` = "com.miui.securitycenter"
      intent.putExtra("extra_pkgname", context.packageName)
      intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

      if (isIntentAvailable(intent, context)) {
        context.startActivity(intent)
      } else {
        Log.e(TAG, "Intent is not available!")
      }
    }
  }
}
