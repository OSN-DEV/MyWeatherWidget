package jp.gr.java_conf.osndev.myweatherwidget

import android.app.PendingIntent
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.res.XmlResourceParser
import android.net.Uri
import android.util.Log
import android.util.Xml
import android.view.View
import android.widget.RemoteViews
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import java.text.SimpleDateFormat
import java.util.*

/**
 * Implementation of App Widget functionality.
 */
class MyWeatherWidget : AppWidgetProvider() {
    companion object {
        private const val RssUrl = "https://rss-weather.yahoo.co.jp/rss/days/1400.xml"
        private const val ActionClick = "APPWIDGET_CLICK"
        private const val ExtraUrl = "ExtraUrl"
        private val weatherImages: Map<String, Int> = mapOf<String, Int>(
            "aa" to 1
        )
        private var scheduler: JobScheduler? = null
        private var lock = false
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d("###", "onUpdate")
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        setup(context)
    }

    override fun onEnabled(context: Context) {
        setup(context)
    }

    override fun onDisabled(context: Context) {
    }

    fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        Log.d("###", "updateAppWidget")
        if (lock) {
            return
        }
        lock = true
        RssUrl.httpGet().response { _, response, result ->
            when (result) {
                is Result.Success -> {
                    var model = WeatherDataModel()
                    model.parse(String(response.data))
                    showData(context, appWidgetManager, appWidgetId, model)
                }
                is Result.Failure -> {
                    println("通信に失敗しました。")
                    println(Result.toString())
                }
            }
        }
        lock = false
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("###", "onReceive: " + intent?.action ?: "")
        super.onReceive(context, intent)
        setup(context)
        if (intent?.action == ActionClick && intent?.getStringExtra(ExtraUrl) != null) {
            val uri = Uri.parse(intent.getStringExtra(ExtraUrl))
            val i = Intent(Intent.ACTION_VIEW, uri)
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(i)
        } else if (intent?.action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            val ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)
            if (null != ids) {
                onUpdate(context, AppWidgetManager.getInstance(context.applicationContext), ids)
            }
        }
    }

    private fun showData(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, model: WeatherDataModel) {
        val views = RemoteViews(context.packageName, R.layout.my_weather_widget)

        val date = Date()
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        views.setTextViewText(R.id.time, format.format(date))

        weatherImages[model.weather]?.also {
            views.setImageViewResource(R.id.weather_image, it)
            views.setViewVisibility(R.id.weather_image, View.VISIBLE)
            views.setViewVisibility(R.id.weather_text, View.INVISIBLE)
        } ?: run {
            views.setViewVisibility(R.id.weather_image, View.INVISIBLE)
            views.setViewVisibility(R.id.weather_text, View.VISIBLE)
            views.setTextViewText(R.id.weather_text, model.weather)
        }
        views.setTextViewText(R.id.temperature, model.temperature)

        val i = Intent(context, MyWeatherWidget::class.java)
        i.setAction(ActionClick)
        i.putExtra(ExtraUrl, model.link)
        val intent = PendingIntent.getBroadcast(context, appWidgetId, i, 0)
        views.setOnClickPendingIntent(R.id.container, intent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
//        Log.d("###", model.weather)
//        Log.d("###", model.temperature)
    }

    private fun setup(context: Context?) {
        if (null == scheduler && null != context) {
            scheduler = context.applicationContext.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            scheduler?.schedule(WeatherJobService.WeatherJobServiceFactory(context.applicationContext, 0, 0))
        }
    }
}




