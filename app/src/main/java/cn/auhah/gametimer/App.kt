package cn.auhah.gametimer

import android.app.Application
import com.tencent.bugly.crashreport.CrashReport

class App : Application() {
  override fun onCreate() {
    super.onCreate()
    CrashReport.initCrashReport(applicationContext, "ebecb3e3a9", true)
  }
}