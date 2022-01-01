package com.udacity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var previousProgress: Float = 0f
    private var progress: Float = 0f

    private lateinit var downloadManager: DownloadManager
    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private lateinit var downloadButton: LoadingButton
    private lateinit var downloadOptions: RadioGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        downloadButton = findViewById(R.id.downloadButton)
        downloadOptions = findViewById(R.id.downloadOptions)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        downloadButton.setOnClickListener {
            val url = getSelectedDownloadUrl()

            if (url != null) {
                downloadButton.isEnabled = false
                downloadButton.reset()
                download(url)
                // Set progress to 10% to indicate beginning of download
                downloadButton.setProgress(10f, 500)
                observeProgress()

            } else {
                showToast(R.string.warning_no_file_selected)
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            downloadButton.isEnabled = true
            if (progress < 100) {
                downloadButton.setProgress(100f)
            }
        }
    }

    private fun download(url: String) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.
    }

    private fun observeProgress() {
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                val query = DownloadManager.Query()
                query.setFilterById(downloadID)
                val cursor: Cursor = downloadManager.query(query)

                if (cursor.moveToFirst()) {
                    progress = getProgress(cursor)
                    if (progress in 0.0..100.0 && progress > previousProgress + PROGRESS_THRESHOLD) {
                        runOnUiThread {
                            downloadButton.setProgress(progress)
                        }
                    }
                    if (progress >= 100) {
                        timer.cancel()
                    }
                    previousProgress = progress
                } else {
                    timer.cancel()
                }

                cursor.close()
            }
        }, 0, 100)
    }

    @SuppressLint("Range")
    private fun getProgress(cursor: Cursor): Float {
        val bytesDownloaded: Int =
            cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
        val bytesTotal: Int =
            cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

        return bytesDownloaded * 1f / bytesTotal * 100
    }

    private fun showToast(resId: Int) {
        Toast.makeText(applicationContext, resId, Toast.LENGTH_SHORT).show()
    }

    private fun getSelectedDownloadUrl(): String? {
        val selectedRadio = findViewById<RadioButton>(downloadOptions.checkedRadioButtonId)
        val selectedRadioIdx = downloadOptions.indexOfChild(selectedRadio)

        return when(selectedRadioIdx) {
            0 -> "https://github.com/bumptech/glide"
            1 -> "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
            2 -> "https://github.com/square/retrofit"
            else -> null
        }
    }

    companion object {
        private const val CHANNEL_ID = "channelId"
        private const val PROGRESS_THRESHOLD = 1f
    }

}
