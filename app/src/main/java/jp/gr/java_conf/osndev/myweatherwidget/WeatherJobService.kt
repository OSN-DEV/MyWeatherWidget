package jp.gr.java_conf.osndev.myweatherwidget

import android.app.Service
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.util.TimeUtils
import java.util.concurrent.TimeUnit

class WeatherJobService : JobService() {
    companion object {
        fun WeatherJobServiceFactory(context: Context, jobId: Int, latency: Long = 0): JobInfo {
            val componentName = ComponentName(context, WeatherJobService::class.java)
            return JobInfo.Builder(jobId, componentName).apply {
                setPersisted(true)
//                setMinimumLatency(TimeUnit.MINUTES.toMillis(1))
                setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                setPeriodic(TimeUnit.MINUTES.toMillis(60))
            }.build()
        }
    }
    private val tag = WeatherJobService::class.simpleName ?: ""

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(tag, "onStopJob")
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(tag, "onStartJob")
        Thread(Runnable {
//            val widget = ComponentName(application, MyWeatherWidget::class.java)
//            val manager = AppWidgetManager.getInstance(application)
//            val ids = manager.getAppWidgetIds(widget)
//            manager.updateAppWidget(application, manager, ids)

            val intent = Intent(this, MyWeatherWidget::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(ComponentName(application, MyWeatherWidget::class.java))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            application.sendBroadcast(intent)
            Log.d(tag, "send boardcast")
        }).start()
        return true
    }
}