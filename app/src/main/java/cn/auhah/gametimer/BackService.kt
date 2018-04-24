package cn.auhah.gametimer

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.view.Gravity
import android.view.WindowManager
import ezy.assist.compat.SettingsCompat
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.windowManager


class BackService : Service() {
  override fun onBind(intent: Intent): IBinder? {
    return null
  }

  private var timerView: TimerView? = null

  companion object {
    fun start(context: Context) {
      val intent = Intent(context, BackService::class.java)
      handleStart(context, intent)
    }

    fun enableMove(context: Context) {
      val intent = Intent(context, BackService::class.java)
      intent.putExtra("a", 1)
      handleStart(context, intent)
    }

    fun disableMove(context: Context) {
      val intent = Intent(context, BackService::class.java)
      intent.putExtra("a", 2)
      handleStart(context, intent)
    }

    fun exit(context: Context) {
      val intent = Intent(context, BackService::class.java)
      intent.putExtra("a", 3)
      handleStart(context, intent)
    }

    fun refreshSetting(context: Context) {
      val intent = Intent(context, BackService::class.java)
      intent.putExtra("a", 4)
      handleStart(context, intent)
    }

    private fun handleStart(context: Context, intent: Intent) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
      } else {
        context.startService(intent)
      }

    }
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    val canDrawOverlays = SettingsCompat.canDrawOverlays(this)
    if (canDrawOverlays) {
      val params = WindowManager.LayoutParams()
      val intExtra = intent?.getIntExtra("a", 0)
      when (intExtra) {
        0 -> {

          if (timerView != null) {
            timerView!!.clearTimer()
            windowManager.removeView(timerView)
          }
          timerView = TimerView(this)
          params.width = WindowManager.LayoutParams.WRAP_CONTENT
          params.height = WindowManager.LayoutParams.WRAP_CONTENT
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
          } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
          }

          // 系统提示window
          params.format = PixelFormat.TRANSLUCENT// 支持透明
          //params.format = PixelFormat.RGBA_8888;
          params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE// 焦点
          params.windowAnimations = 0
          params.gravity = Gravity.TOP or Gravity.START
          timerView!!.applyLocation(params)
          windowManager.addView(timerView, params)

        }
        1 -> {
          timerView?.isEnableMove = true
        }
        2 -> {
          timerView?.isEnableMove = false
        }
        3 -> {
          if (timerView != null) {
            timerView!!.clearTimer()
            windowManager.removeView(timerView)
          }
          stopForeground(true)
          stopSelf()
        }
        4 -> {
          timerView?.initSetting()
        }
      }

      val i = Intent(this, MainActivity::class.java)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel("ccc", "游戏小工具",
            NotificationManager.IMPORTANCE_DEFAULT)
        channel.description = "王者荣耀兵线计时器"

        notificationManager.createNotificationChannel(channel)

        val builder = Notification.Builder(this, "ccc")
            .setContentIntent(
                PendingIntent.getActivity(this, 0, i, 0))
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
            .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
            .setContentTitle("计时器开启") // 设置下拉列表里的标题
            .setContentText("如果要关闭，点击通知以后在打开的页面点退出") // 设置上下文内容
            .setAutoCancel(true)

        val notification = builder.build()
        startForeground(1, notification)

      } else {
        val builder = NotificationCompat.Builder(this)
            .setContentIntent(
                PendingIntent.getActivity(this, 0, i, 0))
            .setContentTitle("计时器开启") // 设置下拉列表里的标题
            .setContentText("如果要关闭，点击通知以后在打开的页面点退出") // 设置上下文内容
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.mipmap.ic_launcher))
            .setSmallIcon(R.mipmap.ic_launcher) // 设置状态栏内的小图标
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val notification = builder.build()

        startForeground(1, notification)
      }
    }

    return super.onStartCommand(intent, flags, startId)
  }

  override fun onDestroy() {
    if (timerView?.parent != null) windowManager.removeView(timerView)
    super.onDestroy()
  }
}
