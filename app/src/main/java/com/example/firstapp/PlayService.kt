package com.example.firstapp

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import java.util.*

class PlayService : Service() {

    var player: MediaPlayer? = null
    var notification: NotificationCompat.Builder? = null

    override fun onCreate() {
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.action == "stop") {
            player?.stop()
            //выключаем нотификацию
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(333)
            //полностью останавливаем сервис
            stopSelf()
            return START_NOT_STICKY
        }

        player?.stop()
        val url = intent!!.extras.getString("mp3")
        player = MediaPlayer()
        player?.setDataSource(this, Uri.parse(url))
        player?.setOnPreparedListener { p ->
            p.start()
            //запускаем таймер для отображения в нотификации
            val timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    if (!p.isPlaying) {
                        timer.cancel()
                        return
                    }
                    notification?.setContentText("${p.currentPosition/1000} sek / ${p.duration/1000}")
                    //выводим в нотификацию (обновляем ее)
                    (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(333, notification?.build())
                }
            }, 1000, 1000)
        }
        player?.prepareAsync() //только асинхронно!!! т.к. мы в главном потоке

        val notificationIntent = Intent(this, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val iStop = Intent(this, PlayService::class.java).setAction("stop")
        val piStop = PendingIntent.getService(this, 0, iStop, PendingIntent.FLAG_CANCEL_CURRENT)

        notification = NotificationCompat.Builder(this, "1")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("MP3")
            .setContentText("")
            .setContentIntent(PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
            .addAction(R.mipmap.ic_launcher, "Stop", piStop)
            .setAutoCancel(true)
            .setOngoing(false)

        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).notify(333, notification?.build())

        return START_NOT_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onDestroy() {
        player?.stop()

        super.onDestroy()
    }
}
