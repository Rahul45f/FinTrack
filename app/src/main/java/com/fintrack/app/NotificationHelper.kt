package com.fintrack.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val ctx: Context) {

    companion object {
        const val CHANNEL_ID = "fintrack_due"
        const val CHANNEL_NAME = "Payment Reminders"
    }

    fun createChannel() {
        val ch = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply { description = "Reminders for due and overdue payments" }
        (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(ch)
    }

    fun checkAndNotify(people: List<Person>) {
        createChannel()
        val today = java.time.LocalDate.now().toString()
        var id = 2000
        people.forEach { p ->
            p.transactions.forEach { tx ->
                if (!tx.settled && !tx.dueDate.isNullOrBlank() && tx.dueDate <= today) {
                    val title = if (tx.type == "lent")
                        "💰 ${p.name} owes you ${fmtNum(tx.amount)}"
                    else
                        "💸 You owe ${p.name} ${fmtNum(tx.amount)}"
                    showNotif(id++, title, "${tx.description} • Due ${tx.dueDate}")
                }
            }
        }
    }

    private fun fmtNum(n: Double): String {
        return "₹" + n.toLong().toString()
    }

    private fun showNotif(id: Int, title: String, body: String) {
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(ctx, id, intent, PendingIntent.FLAG_IMMUTABLE)
        val n = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(ctx).notify(id, n)
        } catch (e: SecurityException) {
            // permission not granted yet
        }
    }
}
