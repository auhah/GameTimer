package cn.auhah.gametimer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

object AppUtils {
  private const val APP_PACKAGE_NAME = "com.tencent.tmgp.sgame"//包名

  fun launchAPP(context: Context) {
    // 判断是否安装过App，否则去市场下载
    if (isAppInstalled(context, APP_PACKAGE_NAME)) {
      context.startActivity(context.packageManager.getLaunchIntentForPackage(APP_PACKAGE_NAME))
    } else {
      goToMarket(context, APP_PACKAGE_NAME)
    }
  }

  private fun isAppInstalled(
    context: Context,
    packageName: String
  ): Boolean {
    return try {
      context.packageManager.getPackageInfo(packageName, 0)
      true
    } catch (e: PackageManager.NameNotFoundException) {
      false
    }

  }

  private fun goToMarket(
    context: Context,
    packageName: String
  ) {
    val uri = Uri.parse("market://details?id=$packageName")
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    try {
      context.startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
    }

  }
}
