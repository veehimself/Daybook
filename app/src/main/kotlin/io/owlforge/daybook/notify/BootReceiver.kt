package io.owlforge.daybook.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.owlforge.daybook.DaybookApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** Alarms don't survive reboots / app updates, so rebuild them all. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as DaybookApplication
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try { app.rescheduleAll() } finally { pending.finish() }
        }
    }
}
