package cn.auhah.gametimer

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.sdk25.coroutines.onLongClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.vibrator
import org.jetbrains.anko.windowManager


class TimerView : TextView {
  var time: Long = 0
  var isShort: Boolean = true
  var isEnableMove: Boolean = false

  val timer = object : CountDownTimer(33250, 1000) {
    override fun onTick(millisUntilFinished: Long) {
      isShort = false
      time = millisUntilFinished
      showText()
    }

    override fun onFinish() {
      start()
    }
  }
  private var shortTimer: CountDownTimer? = null

  constructor(context: Context) : super(context) {
    init()
  }

  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    init()
  }

  private fun init() {
    initSetting()
    textColor = Color.RED
    backgroundColor = Color.TRANSPARENT
    timer.start()
    onClick {
      if (isShort) {
        shortTimer?.cancel()
        isShort = false
        timer.start()
      } else {
        timer.cancel()
        if (time < 6750) {
          time += 33250
        }
        time -= 6750
        shortTimer?.cancel()
        shortTimer = object : CountDownTimer(time, 1000) {
          override fun onTick(millisUntilFinished: Long) {
            time = millisUntilFinished
            showText()
          }

          override fun onFinish() {
            timer.start()
          }
        }.start()
        isShort = true
      }
    }

    onLongClick {
    }
  }

  private var mStartX: Float = 0.0f

  private var mStartY: Float = 0.0f

  private var mRawX: Float = 0.0f

  private var mRawY: Float = 0.0f

  override fun onTouchEvent(event: MotionEvent): Boolean {

    if (!isEnableMove) return super.onTouchEvent(event)
    // 当前值以屏幕左上角为原点
    mRawX = event.rawX
    mRawY = event.rawY

    val action = event.action

    when (action) {
      MotionEvent.ACTION_DOWN -> {
        // 以当前父视图左上角为原点
        mStartX = event.x
        mStartY = event.y
      }

      MotionEvent.ACTION_MOVE -> updateWindowPosition()

      MotionEvent.ACTION_UP -> updateWindowPosition()
    }

    // 消耗触摸事件
    return true
  }

  private fun updateWindowPosition() {
    if (layoutParams is WindowManager.LayoutParams)
      with(layoutParams as WindowManager.LayoutParams) {
        // 更新坐标
        x = (mRawX - mStartX).toInt()
        y = (mRawY - mStartY).toInt()
        // 使参数生效
        context.windowManager.updateViewLayout(this@TimerView, this)
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putInt("x", x)
            .putInt("y", y)
            .apply()
      }
  }

  private var vibrateEnable: Boolean = true
  private var vibrateTime: Int = 5
  private var vibrateDuration: Long = 300
  fun initSetting() {
    with(PreferenceManager.getDefaultSharedPreferences(context)) {
      vibrateEnable = getBoolean("vibrate_enable", true)
      vibrateTime = getString("vibrate_time", "5").toInt()
      vibrateDuration = getString("vibrate_duration", "300").toLong()
    }
  }

  fun applyLocation(lp: WindowManager.LayoutParams) {
    val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
    lp.x = sharedPreferences.getInt("x", 0)
    lp.y = sharedPreferences.getInt("y", 0)
  }


  private fun showText() {
    val t = Math.round(time / 1000.00).toInt()
    text = "$t 秒"
    if (vibrateEnable and (t == vibrateTime)) {
      context.vibrator.vibrate(vibrateDuration)
    }
  }

  fun clearTimer() {
    timer.cancel()
    shortTimer?.cancel()
  }

  override fun onConfigurationChanged(newConfig: Configuration?) {
    super.onConfigurationChanged(newConfig)
//    if (newConfig?.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//      context.toast("横屏")
//    } else {
//      context.toast("竖屏")
//    }
  }
}