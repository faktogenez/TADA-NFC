package com.example.tada_nfc

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class TadaWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, TadaWidgetProvider::class.java)
            val allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (appWidgetId in allWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val prefs = context.getSharedPreferences("tada_prefs", Context.MODE_PRIVATE)
            val balance = prefs.getString("last_balance", "₩ 0") ?: "₩ 0"
            val cardNumber = prefs.getString("last_card_number", "**** **** **** ****") ?: "**** **** **** ****"

            val views = RemoteViews(context.packageName, R.layout.tada_widget_layout)
            views.setTextViewText(R.id.widget_balance_value, balance)
            views.setTextViewText(R.id.widget_card_number, cardNumber)

            // Нажатие на виджет открывает приложение
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
