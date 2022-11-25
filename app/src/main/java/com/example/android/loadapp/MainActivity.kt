package com.example.android.loadapp

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.app.NotificationCompat
import androidx.core.os.bundleOf
import androidx.core.view.setMargins
import com.example.android.loadapp.databinding.ActivityMainBinding

private val notificationID = 0

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private lateinit var downloadManager: DownloadManager

    private lateinit var binding: ActivityMainBinding

    private val URL: String
        get() {
            if (binding.layout.radioGroup.checkedRadioButtonId != -1){
                return findViewById<RadioButton>(
                    binding.layout.radioGroup.checkedRadioButtonId
                )
                    .contentDescription.toString()
            } else {
                return ""
            }
        }

    private val viewItems = mapOf(
        "Glide - Image Loading Library by BumpTech" to "https://github.com/bumptech/glide/archive/refs/heads/master.zip",
        "LoadApp - Current Repository by Udacity" to "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/refs/heads/master.zip",
        "Retrofit - Type-Safe HTTP Client for Android and Java by Square. Inc" to "https://github.com/square/retrofit/archive/refs/heads/master.zip")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        for (item in viewItems) {
            val radioButton = RadioButton(applicationContext)
            radioButton.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(50)
            }

            radioButton.text = item.key
            radioButton.contentDescription = item.value

            binding.layout.radioGroup.addView(radioButton)
        }

        binding.layout.customButton.setOnClickListener {
            if (URL == ""){
                Toast.makeText(applicationContext, "Please Select a File To Download!", Toast.LENGTH_SHORT).show()
            } else {
                download()
            }
        }
        binding.layout.customButton.valueAnimator.doOnEnd {
            if (binding.layout.customButton.buttonState == ButtonState.Loading){
                val query = DownloadManager.Query()
                query.setFilterById(downloadID)
                val cursor: Cursor = downloadManager.query(query)
                if (cursor.moveToFirst()) {
                    val State =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))

                    if (State == DownloadManager.STATUS_SUCCESSFUL) {
                        sendDownloadUpdate("Success")
                    } else {
                        sendDownloadUpdate("Failed")
                    }
                }
            }
        }
    }

    private fun sendDownloadUpdate(Status: String){
        downloadManager.remove(downloadID)
        binding.layout.customButton.buttonState = ButtonState.Completed
        val bundle = bundleOf(
            "File Name" to findViewById<RadioButton>(binding.layout.radioGroup.checkedRadioButtonId).text,
            "Status" to Status
        )
        sendNotification(bundle)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (binding.layout.customButton.buttonState == ButtonState.Loading) {
                sendDownloadUpdate("Success")
            }
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(URL))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        setRequiresCharging(false)
                    }
                }

        downloadID =
            downloadManager.enqueue(request)// enqueue puts the download request in the queue.

        if (downloadID != 0L){
            binding.layout.customButton.buttonState = ButtonState.Loading
        } else {
            Toast.makeText(applicationContext, "Download Failed To Start", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.notification_channel_id),
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { setShowBadge(false) }

            channel.description = getString(R.string.notification_description)

            try {
                notificationManager.createNotificationChannel(channel)
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    private fun sendNotification(bundle: Bundle = Bundle()) {
        createNotificationNavigationIntent(bundle)

        action = NotificationCompat.Action(R.drawable.ic_assistant_black_24dp,
            "Download Details", pendingIntent)

        val builder = NotificationCompat.Builder(
            applicationContext,
            getString(R.string.notification_channel_id)
        )
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setContentIntent(pendingIntent)
            .addAction(action)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(notificationID, builder.build())
    }

    private fun createNotificationNavigationIntent(bundle: Bundle = Bundle()) {
        val notificationIntent = Intent(this, DetailActivity::class.java)
            .putExtra("Download Info", bundle)
        pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationID,
            notificationIntent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    }
}

