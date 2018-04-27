package cn.auhah.gametimer

import android.graphics.drawable.Icon
import android.os.Build.VERSION_CODES
import android.service.quicksettings.Tile.STATE_ACTIVE
import android.service.quicksettings.Tile.STATE_INACTIVE
import android.service.quicksettings.Tile.STATE_UNAVAILABLE
import android.service.quicksettings.TileService
import android.support.annotation.RequiresApi
import cn.auhah.gametimer.permissions.SettingsCompat
import org.jetbrains.anko.toast

@RequiresApi(VERSION_CODES.N)
class TimerTileService : TileService() {

  companion object {
    const val TEXT_OPEN = "Timer Opened"
    const val TEXT_CLOSED = "Timer Closed"
  }

  override fun onClick() {
    super.onClick()
    with(qsTile) {
      when (state) {
        STATE_UNAVAILABLE -> {
          icon = Icon.createWithResource(this@TimerTileService, R.drawable.ic_timer_off)
          label = TEXT_CLOSED
          state = STATE_INACTIVE
          BackService.exit(this@TimerTileService)
        }
        STATE_INACTIVE -> {
          if (SettingsCompat.canDrawOverlays(this@TimerTileService)) {
            label = TEXT_OPEN
            icon = Icon.createWithResource(this@TimerTileService, R.drawable.ic_timer)
            state = STATE_ACTIVE
            BackService.start(this@TimerTileService)
          } else {
            toast("没有悬浮窗权限，请打开GameTimer点击检测权限获取权限")
          }
        }
        STATE_ACTIVE -> {
          icon = Icon.createWithResource(this@TimerTileService, R.drawable.ic_timer_off)
          label = TEXT_CLOSED
          state = STATE_INACTIVE
          BackService.exit(this@TimerTileService)
        }
      }
      updateTile()
    }
  }

  override fun onTileAdded() {
    super.onTileAdded()
  }

  override fun onTileRemoved() {
    super.onTileRemoved()
  }

  override fun onStartListening() {
    super.onStartListening()
    with(qsTile) {
      if (BackService.isShown) {
        label = TEXT_OPEN
        icon = Icon.createWithResource(this@TimerTileService, R.drawable.ic_timer)
        state = STATE_ACTIVE
      } else {
        icon = Icon.createWithResource(this@TimerTileService, R.drawable.ic_timer_off)
        label = TEXT_CLOSED
        state = STATE_INACTIVE
      }
      updateTile()
    }
  }

  override fun onStopListening() {
    super.onStopListening()
  }

}