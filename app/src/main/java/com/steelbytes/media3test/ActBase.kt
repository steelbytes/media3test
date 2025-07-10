package com.steelbytes.media3test

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder

open class ActBase : AppCompatActivity() {

    private var progDlg: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        theme.applyStyle(R.style.OptOutEdgeToEdgeEnforcement, /* force */ false)
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        findViewById<Toolbar>(R.id.toolbar)?.let { toolbar ->
            setSupportActionBar(toolbar)
        }
    }

    fun killBusy() {
        progDlg?.cancel()
        progDlg = null
    }

    fun showBusy(s:String) {
        killBusy()
        progDlg = MaterialAlertDialogBuilder(this)
            .setCancelable(false)
            .setMessage(s)
            .show()
    }
}