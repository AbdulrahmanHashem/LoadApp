package com.example.android.loadapp

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.android.loadapp.databinding.ActivityMainBinding

//import kotlinx.android.synthetic.main.activity_main.*
//import kotlinx.android.synthetic.main.content_main.*

private val notificationID = 0

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private lateinit var notificationManager: NotificationManager
    private lateinit var pendingIntent: PendingIntent
    private lateinit var action: NotificationCompat.Action

    private lateinit var binding: ActivityMainBinding

    companion object {
        private var URL: String? =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "channelId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createNotificationChannel()

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        binding.layout.radioGroup.setOnCheckedChangeListener{ oldViewID, newViewID ->
            URL = when (newViewID) {
                binding.layout.glideRB.id -> "https://github.com/bumptech/glide/archive/refs/heads/master.zip"
                binding.layout.projectRB.id -> "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/refs/heads/master.zip"
                binding.layout.retrofitRB.id -> "https://github.com/square/retrofit/archive/refs/heads/master.zip"
                else -> null
            }
        }

        binding.layout.customButton.setOnClickListener {
            download()
        }
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            val mainView = context as MainActivity
            mainView.binding.layout.customButton.buttonState = ButtonState.Completed

            try {
                sendNotification()
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    private fun download() {
        URL?.let {
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

            val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadID =
                downloadManager.enqueue(request)// enqueue puts the download request in the queue.
            binding.layout.customButton.buttonState = ButtonState.Loading
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                getString(R.string.notification_channel_id),
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT)
                .apply { setShowBadge(false) }

            channel.description = getString(R.string.notification_description)

            try {
                notificationManager.createNotificationChannel(channel)
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    private fun sendNotification() {
        val notificationIntent = Intent(applicationContext, DetailActivity::class.java)
        pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationID,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(applicationContext, getString(R.string.notification_channel_id))
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(notificationID, builder.build())
        }
    }
}
