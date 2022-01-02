package com.udacity

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.app.NotificationManager
import android.database.Cursor
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.util.cancelNotification
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {

    private lateinit var fileNameTextView: TextView
    private lateinit var statusTextView: TextView
    private lateinit var okButton: Button

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        val downloadId = intent.getLongExtra(Constants.EXTRA_DOWNLOAD_ID, 0)

        cancelNotification(downloadId)

        fileNameTextView = findViewById(R.id.file_name)
        statusTextView = findViewById(R.id.status)
        okButton = findViewById(R.id.ok_button)

        okButton.setOnClickListener {
            finish()
        }

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        val query = DownloadManager.Query()
        query.setFilterById(downloadId)
        val cursor: Cursor = downloadManager.query(query)

        if (cursor.moveToFirst()) {
            val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            val fileName = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))

            fileNameTextView.text = fileName
            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> {
                    statusTextView.text = getString(R.string.status_success)
                    statusTextView.setTextColor(getColor(R.color.success))
                }
                else -> {
                    statusTextView.text = getString(R.string.status_fail)
                    statusTextView.setTextColor(getColor(R.color.error))
                }
            }
        }
    }

    private fun cancelNotification(id: Long?) {
        val notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager
        notificationManager.cancelNotification(id)
    }
}
