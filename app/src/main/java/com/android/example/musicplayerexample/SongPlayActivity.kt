package com.android.example.musicplayerexample

import android.content.*
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class SongPlayActivity : AppCompatActivity() {

    private var songList: ArrayList<String> = ArrayList()
    private var idList: ArrayList<String> = ArrayList()
    private var position = 0

    private var isPlaying = false
    private lateinit var previousButton: ImageButton
    private lateinit var rewindButton: ImageButton
    private lateinit var playOrPauseButton: ImageButton
    private lateinit var fastButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var songTV: TextView

    private lateinit var serviceIntent: Intent
    private lateinit var broadcastReceiver: IconChangeBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_song_play)

        previousButton = findViewById(R.id.previousSong)
        rewindButton = findViewById(R.id.rewind)
        playOrPauseButton = findViewById(R.id.playOrPauseSong)
        fastButton = findViewById(R.id.fastForward)
        nextButton = findViewById(R.id.nextSong)
        songTV = findViewById(R.id.songName)

        if (intent.hasExtra("songList") && intent.hasExtra("idList") && intent.hasExtra("position")) {
            songList = intent.getStringArrayListExtra("songList") as ArrayList<String>
            idList = intent.getStringArrayListExtra("idList") as ArrayList<String>
            position = intent.getIntExtra("position", 0)

            broadcastReceiver = IconChangeBroadcastReceiver()
            val intentFilter = IntentFilter()
            intentFilter.addAction("com.android.example.musicplayerexample.SongPlayActivity")
            registerReceiver(broadcastReceiver, intentFilter)

            playSong()

            previousButton.setOnClickListener {
                position--
                if (position < 0)
                    position = songList.size - 1
                changeSong()
            }

            rewindButton.setOnClickListener {
                changeTime(false)
            }

            playOrPauseButton.setOnClickListener {
                if (isPlaying) {
                    playOrPauseMedia(isPlaying)
                    playOrPauseButton.setImageResource(R.drawable.ic_round_play_arrow_24)
                } else {
                    playOrPauseMedia(isPlaying)
                    playOrPauseButton.setImageResource(R.drawable.ic_round_pause_24)
                }
                isPlaying = !isPlaying
            }

            fastButton.setOnClickListener {
                changeTime(true)
            }

            nextButton.setOnClickListener {
                position++
                if (position >= songList.size)
                    position = 0
                changeSong()
            }
        }

    }

    private fun playSong() {
        serviceIntent = Intent(this, MusicService::class.java)
        serviceIntent.putExtra(
            "songUri",
            ContentUris
                .withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    idList[position].toLong()
                )
                .toString()
        )
        stopService(serviceIntent)
        startService(serviceIntent)

        isPlaying = true
        songTV.text = songList[position].substring(0, songList[position].lastIndexOf("."))
    }

    private fun changeSong() {

        isPlaying = true
        playOrPauseButton.setImageResource(R.drawable.ic_round_pause_24)
        val intent = Intent()
        intent.action = "com.android.example.musicplayerexample.MusicService"
        intent.putExtra("action", 0)
        intent.putExtra(
            "songUri",
            ContentUris
                .withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    idList[position].toLong()
                )
                .toString()
        )
        sendBroadcast(intent)

        songTV.text = songList[position].substring(0, songList[position].lastIndexOf("."))
    }

    private fun changeTime(isForward: Boolean) {
        val intent = Intent()
        intent.action = "com.android.example.musicplayerexample.MusicService"
        intent.putExtra("action", 1)
        intent.putExtra("isForward", isForward)
        sendBroadcast(intent)
    }

    private fun playOrPauseMedia(playing: Boolean) {
        val intent = Intent()
        intent.action = "com.android.example.musicplayerexample.MusicService"
        intent.putExtra("action", 2)
        intent.putExtra("isPlaying", playing)
        sendBroadcast(intent)
    }

    inner class IconChangeBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            isPlaying = false
            playOrPauseButton.setImageResource(R.drawable.ic_round_play_arrow_24)
            onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }
}