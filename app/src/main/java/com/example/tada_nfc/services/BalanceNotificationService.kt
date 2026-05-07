package com.example.tada_nfc.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.tada_nfc.MainActivity
import com.example.tada_nfc.R
import com.example.tada_nfc.config.CardConfig

class BalanceNotificationService : Service() {

    companion object {
        const val CHANNEL_ID = "balance_notification_channel"
        const val NOTIFICATION_ID = 1001

        fun updateNotification(context: Context, balance: String, userType: String, cardNumber: String = "") {
            val intent = Intent(context, BalanceNotificationService::class.java).apply {
                putExtra("BALANCE", balance)
                putExtra("USER_TYPE", userType)
                putExtra("CARD_NUMBER", cardNumber)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val balance = intent?.getStringExtra("BALANCE") ?: "₩ 0"
        val userType = intent?.getStringExtra("USER_TYPE") ?: "UNKNOWN"
        val cardNumber = intent?.getStringExtra("CARD_NUMBER") ?: ""
        showNotification(balance, userType, cardNumber)
        return START_STICKY
    }

    private fun showNotification(balance: String, userType: String, cardNumber: String) {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val circleColorInt = composeColorToInt(CardConfig.notificationCircleColor)
        val largeIcon = createEnhancedCircleTIcon(circleColorInt)

        val remoteViews = RemoteViews(packageName, R.layout.notification_balance).apply {
            setTextViewText(R.id.notification_balance_value, balance)
            setTextViewText(R.id.notification_card_number, cardNumber)
            
            val labelColorInt = composeColorToInt(CardConfig.notificationLabelColor)
            val valueColorInt = composeColorToInt(CardConfig.notificationValueColor)
            
            setTextColor(R.id.notification_card_number, labelColorInt)
            setTextColor(R.id.notification_balance_value, valueColorInt)

            setTextViewTextSize(R.id.notification_card_number, TypedValue.COMPLEX_UNIT_SP, CardConfig.notificationLabelFontSize.value)
            setTextViewTextSize(R.id.notification_balance_value, TypedValue.COMPLEX_UNIT_SP, CardConfig.notificationBalanceFontSize.value)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_t)
            .setLargeIcon(largeIcon)
            .setCustomContentView(remoteViews)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun composeColorToInt(composeColor: androidx.compose.ui.graphics.Color): Int {
        return (composeColor.alpha * 255).toInt() shl 24 or
               ((composeColor.red * 255).toInt() shl 16) or
               ((composeColor.green * 255).toInt() shl 8) or
               (composeColor.blue * 255).toInt()
    }

    private fun createEnhancedCircleTIcon(color: Int): Bitmap {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        
        paint.color = color
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        
        paint.color = Color.WHITE
        paint.textSize = size * 0.9f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.strokeWidth = 2f
        paint.style = Paint.Style.FILL_AND_STROKE
        
        val textBounds = android.graphics.Rect()
        paint.getTextBounds("T", 0, 1, textBounds)
        val yPos = (canvas.height / 2f + textBounds.height() / 2f) - 2f
        canvas.drawText("T", canvas.width / 2f, yPos, paint)

        return bitmap
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Tada Balance Monitor",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }
}
