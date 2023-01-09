package com.android.example.musicplayerexample

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.MediaStore
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class MainActivity : AppCompatActivity() {
    private val requestCode = 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            loadMusicList()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                requestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == this.requestCode && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            loadMusicList()
    }

    private fun loadMusicList() {
        val audioListView = findViewById<ListView>(R.id.listView)
        val audioList: ArrayList<String> = ArrayList()
        val idList: ArrayList<String> = ArrayList()

        val proj = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME
        )

        val audioCursor: Cursor? = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            proj,
            null,
            null,
            null
        )

        if (audioCursor != null) {
            if (audioCursor.moveToFirst()) {
                do {
                    idList.add(audioCursor.getString(0))
                    audioList.add(audioCursor.getString(1))
                } while (audioCursor.moveToNext())
            }
        }
        audioCursor?.close()

        val adapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.song_list_item, R.id.textView, audioList)
        audioListView.adapter = adapter

        audioListView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, p2, _ ->
                val intent = Intent(this@MainActivity, SongPlayActivity::class.java)
                intent.putExtra("idList", idList)
                intent.putExtra("songList", audioList)
                intent.putExtra("position", p2)
                startActivity(intent)
            }
    }
}