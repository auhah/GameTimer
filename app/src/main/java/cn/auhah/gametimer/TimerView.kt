package cn.auhah.gametimer

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.os.CountDownTimer
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.WindowManager
import android.widget.TextView
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.sdk25.coroutines.onClick
import org.jetbrains.anko.textColor
import org.jetbrains.anko.vibrator
import org.jetbrains.anko.windowManager

class TimerView : TextView {
  private var nextIsShortTimer: Boolean = false
  var isEnableMove: Boolean = false
    set(value) {
      field = value
      backgroundColor = if (value) {
        Color.LTGRAY
      } else {
        Color.TRANSPARENT
      }
    }

  companion object {
    const val NORMAL_DURATION = 33250L                 // 正常兵线
    const val SHORT_DURATION = NORMAL_DURATION - 2250L // 大龙之后的兵线-2.25s
    const val COUNT_DOWN_INTERVAL = 1000L
    const val SHORT_TIMER_REPEAT_TIME = 3
    const val CLICK_DURATION = 500L
  }

  private val normalTimer = object : CountDownTimer(NORMAL_DURATION, COUNT_DOWN_INTERVAL) {
    override fun onTick(millisUntilFinished: Long) {
      log("normalTimerTick")
      showText(millisUntilFinished)
    }

    override fun onFinish() {
      log("normalTimerFinish")
      if (nextIsShortTimer)
        startShortTimer()
      else
        start()
    }
  }
  private var shortTimer = object : CountDownTimer(SHORT_DURATION, COUNT_DOWN_INTERVAL) {
    var shortTimerRepeatTime = 0
    override fun onTick(millisUntilFinished: Long) {
      log("shortTimerTick")
      showText(millisUntilFinished)
    }

    override fun onFinish() {
      log("shortTimerFinish")
      shortTimerRepeatTime++
      nextIsShortTimer = shortTimerRepeatTime < SHORT_TIMER_REPEAT_TIME
      if (nextIsShortTimer) {
        start()
      } else {
        startNormalTimer()
      }
    }
  }

  constructor(context: Context) : super(context) {
    init()
  }

  constructor(
    context: Context,
    attrs: AttributeSet?
  ) : super(context, attrs) {
    init()
  }

  private var lastClickTime = 0L

  private fun init() {
    initSetting()
    startNormalTimer()
    onClick {
      val clickTime = System.currentTimeMillis()
      if (clickTime - lastClickTime >= CLICK_DURATION) {
        lastClickTime = clickTime
        /*
          1. 正常模式点击一下变蓝，下次是短计时器
          2. 等待短计时/短计时的时候点击回到正常模式
         */
        if (!nextIsShortTimer && !isShortTimerRunning) {
          textColor = colorShortTimer
          if (!text.endsWith("$SHORT_TIMER_REPEAT_TIME")) text = "$text 0/$SHORT_TIMER_REPEAT_TIME"
        } else {
          startNormalTimer()
        }
        nextIsShortTimer = !nextIsShortTimer
      }
    }
  }

  private var colorShortTimer = Color.GREEN
    set(value) {
      field = value
      if (isShortTimerRunning) {
        textColor = value
      }
    }
  private var colorNormalTimer = Color.RED
    set(value) {
      field = value
      if (!isShortTimerRunning) {
        textColor = value
      }
    }

  private var isShortTimerRunning = false
    set(value) {
      field = value
      textColor = if (value)
        colorShortTimer
      else {
        colorNormalTimer
      }
    }

  private fun startShortTimer() {
    normalTimer.cancel()
    cancelShortTimer()
    shortTimer.start()
    isShortTimerRunning = true
  }

  private fun startNormalTimer() {
    cancelShortTimer()
    normalTimer.cancel()
    normalTimer.start()
    textColor = colorNormalTimer
    isShortTimerRunning = false
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
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
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
      vibrateEnable = getBoolean(context.getString(R.string.key_vibrate_enable), true)
      vibrateTime = getString(
          context.getString(R.string.key_vibrate_time),
          context.getString(R.string.value_vibrate_time)
      ).toInt()
      vibrateDuration = getString(
          context.getString(R.string.key_vibrate_duration),
          context.getString(R.string.value_vibrate_duration)
      ).toLong()
      colorShortTimer = getInt(
          context.getString(R.string.key_color_short_timer),
          context.resources.getColor(R.color.default_short_timer)
      )
      colorNormalTimer = getInt(
          context.getString(R.string.key_color_normal_timer),
          context.resources.getColor(R.color.default_normal_timer)
      )
    }
  }

  fun applyLocation(lp: WindowManager.LayoutParams) {
    PreferenceManager.getDefaultSharedPreferences(context)
        .let {
          lp.x = it.getInt("x", 0)
          lp.y = it.getInt("y", 0)
        }
  }

  private fun showText(millisUntilFinished: Long) {
    val t = Math.round(millisUntilFinished / 1000.00)
        .toInt()
    text = when {
      isShortTimerRunning -> "${t}秒 ${shortTimer.shortTimerRepeatTime + 1}/$SHORT_TIMER_REPEAT_TIME"
      nextIsShortTimer -> "${t}秒 0/$SHORT_TIMER_REPEAT_TIME"
      else -> "${t}秒"
    }
    if (vibrateEnable and (t == vibrateTime)) {
      context.vibrator.vibrate(vibrateDuration)
    }
  }

  fun clearTimer() {
    normalTimer.cancel()
    cancelShortTimer()
  }

  private fun cancelShortTimer() {
    shortTimer.cancel()
    shortTimer.shortTimerRepeatTime = 0
  }

  private fun log(msg: String) {
    if (BuildConfig.DEBUG) {
      Log.wtf("TimerView", msg)
    }
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