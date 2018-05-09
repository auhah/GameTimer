package cn.auhah.gametimer

import android.app.Application
import com.tencent.bugly.Bugly

class App : Application() {
  override fun onCreate() {
    super.onCreate()
    Bugly.init(
//        CondomContext.wrap(applicationContext, null, CondomOptions().apply {
//          addKit(NullDeviceIdKit())
//        }),
        applicationContext,
        "ebecb3e3a9",
        BuildConfig.DEBUG
    )
  }
}