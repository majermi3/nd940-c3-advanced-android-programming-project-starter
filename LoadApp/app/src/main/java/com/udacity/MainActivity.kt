package com.udacity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import com.udacity.util.sendNotification


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
        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )

        downloadButton.setOnClickListener {
            val idx = getSelectedRadioIndex()
            val url = getDownloadUrl(idx)
            val fileName = getFileName(idx)

            if (url != null) {
                downloadButton.isEnabled = false
                download(url, fileName)

                // Set progress to 10% to indicate beginning of download
                downloadButton.setProgress(INITIAL_PROGRESS, 500)
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
            sendNotification(id)
        }
    }

    private fun download(url: String, fileName: String?) {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
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
                    if (progress in INITIAL_PROGRESS..100.0f && progress > previousProgress + PROGRESS_THRESHOLD) {
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

    private fun getDownloadUrl(idx: Int): String? {
        return when(idx) {
            0 -> "https://github.com/bumptech/glide"
            1 -> "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter"
            2 -> "https://github.com/square/retrofit"
            else -> null
        }
    }

    private fun getFileName(idx: Int): String? {
        return when(idx) {
            0 -> getString(R.string.file_name_glide)
            1 -> getString(R.string.file_name_udacity)
            2 -> getString(R.string.file_name_retrofit)
            else -> null
        }
    }

    private fun getSelectedRadioIndex(): Int {
        val selectedRadio = findViewById<RadioButton>(downloadOptions.checkedRadioButtonId)
        return downloadOptions.indexOfChild(selectedRadio)
    }

    private fun sendNotification(id: Long?) {
        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager

        notificationManager.sendNotification(
            id,
            getString(R.string.notification_message),
            applicationContext
        )
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
            )
                .apply {
                    setShowBadge(false)
                }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(true)
            notificationChannel.description = "File download"

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "channelId"
        private const val PROGRESS_THRESHOLD = 1f
        private const val INITIAL_PROGRESS = 10f
    }

}
