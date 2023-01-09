package com.android.example.musicplayerexample

import android.app.*
import android.content.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat


class MusicService : Service() {
    private var mPlayer: MediaPlayer? = null
    private lateinit var mBroadcastReceiver: MusicBroadcastReceiver
    private val channelId = "music notification"
    private val changeTimeInMillis = 5000

    private fun changeSong(uri: Uri) {
        mPlayer?.release()
        mPlayer = MediaPlayer.create(this, uri)
        mPlayer?.start()
        mPlayer?.setOnCompletionListener {
            sendBroadcastToChangeIcon()
        }
    }

    private fun changePlayPosition(isForward: Boolean) {
        mPlayer?.let {
            if (isForward) {
                val newPos = it.currentPosition + changeTimeInMillis
                if (newPos < it.duration)
                    it.seekTo(newPos)
            } else {
                val newPos = it.currentPosition - changeTimeInMillis
                if (newPos >= 0)
                    it.seekTo(newPos)
                else
                    it.seekTo(0)
            }
        }
    }

    private fun playOrPause(isPlaying: Boolean) {
        if (isPlaying)
            mPlayer?.pause()
        else
            mPlayer?.start()
    }

    override fun onCreate() {
        super.onCreate()
        mBroadcastReceiver = MusicBroadcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.android.example.musicplayerexample.MusicService")
        registerReceiver(mBroadcastReceiver, intentFilter)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.hasExtra("songUri")) {
            mPlayer = MediaPlayer.create(this, Uri.parse(intent.getStringExtra("songUri")))
            mPlayer?.start()
            mPlayer?.setOnCompletionListener {
                sendBroadcastToChangeIcon()
            }
            showNotification()
        } else {
            sendBroadcastToChangeIcon()
            stopSelf();
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun sendBroadcastToChangeIcon() {
        val intent = Intent()
        intent.action = "com.android.example.musicplayerexample.SongPlayActivity"
        sendBroadcast(intent)
    }

    private fun showNotification() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Music Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            if (!manager.notificationChannels.contains(serviceChannel))
                manager.createNotificationChannel(serviceChannel)
        }

        val stopSelf = Intent(this, MusicService::class.java)
        val pendingIntent =
            PendingIntent.getService(this, 0, stopSelf, PendingIntent.FLAG_CANCEL_CURRENT);

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("Song notification")
            .setSmallIcon(R.mipmap.ic_launcher)
            .addAction(R.drawable.ic_round_clear_24, "Close", pendingIntent)
            .build()

        startForeground(105, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        mPlayer?.release()
        unregisterReceiver(mBroadcastReceiver)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    inner class MusicBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            when (intent.getIntExtra("action", 0)) {

                0 -> changeSong(Uri.parse(intent.getStringExtra("songUri")))
                1 -> changePlayPosition(intent.getBooleanExtra("isForward", true))
                2 -> playOrPause(intent.getBooleanExtra("isPlaying", true))
            }
        }
    }
}