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

class WeatherJobService : JobService() {
    companion object {
        fun WeatherJobServiceFactory(context: Context, jobId: Int, latency: Long = 0): JobInfo {
            val componentName = ComponentName(context, WeatherJobService::class.java)
            return JobInfo.Builder(jobId, componentName).apply {
                setPersisted(true)
                setMinimumLatency(latency * (1000 * 60))
                setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                setPeriodic(60 * 60 * 1000)
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
            val intent = Intent(this, WeatherJobService::class.java)
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(ComponentName(application, WeatherJobService::class.java))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            sendBroadcast(intent)
        }).start()
        return false
    }
}