package cn.auhah.gametimer

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ezy.assist.compat.SettingsCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.toast


class MainActivity : AppCompatActivity() {


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    if (SettingsCompat.canDrawOverlays(this)) {
      BackService.start(this)
    } else {
      toast("没有悬浮窗权限，无法启动计时器\n请点击${check.text}按钮获取权限")
    }

    start.onClick {
      AppUtils.launchAPP(this@MainActivity)
    }
    exit.onClick {
      BackService.exit(this@MainActivity)
      finish()
    }
    setting.onClick {
      val intent = Intent(this@MainActivity, SettingsActivity::class.java)
      startActivity(intent)
    }
    check.onClick {
      askForPermission()
    }
  }

  private var isRequestingPermission = false

  private fun askForPermission() {
    if (!SettingsCompat.canDrawOverlays(this)) {
      toast("当前无权限，请授权！")
      isRequestingPermission = true
      SettingsCompat.manageDrawOverlays(this)
    } else {
      toast("当前有权限")
      BackService.start(this)
    }
  }

  override fun onRestart() {
    super.onRestart()
    if (isRequestingPermission) {
      isRequestingPermission = false
      if (SettingsCompat.canDrawOverlays(this)) {
        toast("拿到权限，尝试启动计时器")
        BackService.start(this)
      } else {
        toast("没有权限，请尝试重新授权")
      }
    }
  }
}
