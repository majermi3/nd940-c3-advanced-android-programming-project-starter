package com.udacity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import com.udacity.util.sendNotification


class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0
    private var previousProgress: Float = 0f
    private var progress: Float = 0f

    private lateinit var downloadManager: DownloadManager

    private lateinit var downloadButton: LoadingButton
    private lateinit var downloadOptions: RadioGroup
    private lateinit var glideRadioButton: RadioButton
    private lateinit var udacityRadioButton: RadioButton
    private lateinit var retrofitRadioButton: RadioButton
    private lateinit var otherRadioButton: RadioButton
    private lateinit var mainLayout: MotionLayout
    private lateinit var urlEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        downloadButton = findViewById(R.id.downloadButton)
        downloadOptions = findViewById(R.id.downloadOptions)
        glideRadioButton = findViewById(R.id.radio_glide)
        udacityRadioButton = findViewById(R.id.radio_loadapp)
        retrofitRadioButton = findViewById(R.id.radio_retrofit)
        otherRadioButton = findViewById(R.id.radio_other)
        mainLayout = findViewById(R.id.main_layout)
        urlEditText = findViewById(R.id.url_edit_text)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        createChannel(
            getString(R.string.notification_channel_id),
            getString(R.string.notification_channel_name)
        )

        glideRadioButton.setOnClickListener {
            mainLayout.transitionToStart()
        }
        udacityRadioButton.setOnClickListener {
            mainLayout.transitionToStart()
        }
        retrofitRadioButton.setOnClickListener {
            mainLayout.transitionToStart()
        }
        otherRadioButton.setOnClickListener {
            mainLayout.transitionToEnd()
        }

        downloadButton.setOnClickListener {
            val idx = getSelectedRadioIndex()
            val url = getDownloadUrl(idx)
            val fileName = getFileName(idx)

            if (url == null) {
                showToast(R.string.warning_no_file_selected)
            } else if (!isUrlValid(url)) {
                showToast(R.string.warning_invalid_url)
            } else {
                downloadButton.isEnabled = false
                download(url, fileName)

                // Set progress to 10% to indicate beginning of download
                downloadButton.setProgress(INITIAL_PROGRESS, 500)
                observeProgress()
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
            3 -> urlEditText.text.toString()
            else -> null
        }
    }

    private fun getFileName(idx: Int): String? {
        return when(idx) {
            0 -> getString(R.string.file_name_glide)
            1 -> getString(R.string.file_name_udacity)
            2 -> getString(R.string.file_name_retrofit)
            3 -> getString(R.string.file_name_other)
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

    private fun isUrlValid(url: String): Boolean {
        return Patterns.WEB_URL.matcher(url).matches()
    }

    companion object {
        private const val PROGRESS_THRESHOLD = 1f
        private const val INITIAL_PROGRESS = 10f
    }
}
