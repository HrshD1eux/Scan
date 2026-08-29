package com.HrshD1eux.Scan

import android.content.Intent
import android.service.quicksettings.TileService

class ScanTileService : TileService() {
    override fun onClick() {
        super.onClick()
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivityAndCollapse(intent)
    }
}
