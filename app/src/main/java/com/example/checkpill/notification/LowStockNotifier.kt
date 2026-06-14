package com.example.checkpill.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.checkpill.R
import com.example.checkpill.data.PillInventoryStore
import kotlin.math.abs

class LowStockNotifier(private val context: Context) {
    private val notificationManager = NotificationManagerCompat.from(context)

    fun notifyIfLowStock(pillName: String, store: PillInventoryStore) {
        val currentCount = store.getTotalCountForPill(pillName)
        if (currentCount > LOW_STOCK_THRESHOLD) {
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createChannel()
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("재고 부족 알림")
            .setContentText("$pillName 재고가 ${currentCount}정 남았습니다.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(abs(pillName.hashCode()), notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            CHANNEL_ID,
            "재고 부족 알림",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "알약 재고가 부족할 때 알려줍니다."
        }

        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val LOW_STOCK_THRESHOLD = 5
        private const val CHANNEL_ID = "low_stock"
    }
}
