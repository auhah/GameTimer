package cn.auhah.gametimer

import android.app.Application
import com.oasisfeng.condom.CondomContext
import com.oasisfeng.condom.CondomOptions
import com.oasisfeng.condom.kit.NullDeviceIdKit
import com.tencent.bugly.Bugly

class App : Application() {
  override fun onCreate() {
    super.onCreate()
    Bugly.init(
        CondomContext.wrap(applicationContext, null, CondomOptions().apply {
          addKit(NullDeviceIdKit())
        }),
        "ebecb3e3a9",
        BuildConfig.DEBUG
    )
  }
}