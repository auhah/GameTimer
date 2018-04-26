package cn.auhah.gametimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.view.Gravity
import android.view.WindowManager
import cn.auhah.gametimer.permissions.SettingsCompat
import org.jetbrains.anko.notificationManager
import org.jetbrains.anko.windowManager

class BackService : Service() {
  override fun onBind(intent: Intent): IBinder? {
    return null
  }

  private var timerView: TimerView? = null

  companion object {
    fun start(context: Context) {
      handleStart(context, 0)
    }

    fun enableMove(context: Context) {
      handleStart(context, 1)
    }

    fun disableMove(context: Context) {
      handleStart(context, 2)
    }

    fun exit(context: Context) {
      handleStart(context, 3)
    }

    fun refreshSetting(context: Context) {
      handleStart(context, 4)
    }

    private fun handleStart(
      context: Context,
      action: Int
    ) {
      val intent = Intent(context, BackService::class.java).putExtra("a", action)
      with(context) {
        if (action == 0 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
          startForegroundService(intent)
        else
          startService(intent)
      }
    }
  }

  override fun onStartCommand(
    intent: Intent?,
    flags: Int,
    startId: Int
  ): Int {
    val canDrawOverlays = SettingsCompat.canDrawOverlays(this)
    if (canDrawOverlays) {
      val intExtra = intent?.getIntExtra("a", 0)
      when (intExtra) {
        0 -> {
          timerView?.let {
            it.clearTimer()
            windowManager.removeView(it)
          }
          windowManager.addView(TimerView(this).apply {
            timerView = this
          }, WindowManager.LayoutParams().apply {
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
              WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            }
            // 系统提示window
            format = PixelFormat.TRANSLUCENT// 支持透明
            //params.format = PixelFormat.RGBA_8888;
            this.flags = this.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE// 焦点
            windowAnimations = 0
            gravity = Gravity.TOP or Gravity.START
            timerView!!.applyLocation(this)
          })

          val i = Intent(this, MainActivity::class.java)

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ccc", "游戏小工具",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            channel.description = "王者荣耀兵线计时器"

            notificationManager.createNotificationChannel(channel)

            val builder = Notification.Builder(this, "ccc")
                .setContentIntent(
                    PendingIntent.getActivity(this, 0, i, 0)
                )
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
                    PendingIntent.getActivity(this, 0, i, 0)
                )
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
        1 -> {
          timerView?.isEnableMove = true
        }
        2 -> {
          timerView?.isEnableMove = false
        }
        3 -> {
          timerView?.let {
            it.clearTimer()
            windowManager.removeView(it)
          }
          stopForeground(true)
          stopSelf()
        }
        4 -> {
          timerView?.initSetting()
        }
      }
    }

    return super.onStartCommand(intent, flags, startId)
  }

  override fun onDestroy() {
    timerView?.let {
      if (it.parent != null) windowManager.removeView(it)
    }
    super.onDestroy()
  }
}
