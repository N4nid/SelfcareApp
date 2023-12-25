package dev.nanid.selfcare

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.nanid.selfcare.databinding.ActivityMainBinding

import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val channelId = "nanid.selfcare.notifications"
    private val description = "Test notification"
    lateinit var notificationManager: NotificationManagerCompat
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val btn = findViewById<Button>(R.id.button)
        val titles: Array<String> = resources.getStringArray(R.array.notification_titles)
        btn.setOnClickListener {
            var rnd = Random
            var num = rnd.nextInt(0,titles.size)
            postNotification(titles[num],"")
            btn.text = "wowi"

        }
    }

    //----------- own shit from here ----------

    fun empty(): PendingIntent? {

        return null
    }

    fun postNotification(notificationTitel: String,notificationContent: String){
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId , "Your channel name", NotificationManager.IMPORTANCE_DEFAULT)
            NotificationManagerCompat.from(this).createNotificationChannel(channel)
        }
        // todo -> action for buttons on notification

        var builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(notificationTitel)
            .addAction(com.google.android.material.R.drawable.navigation_empty_icon,":D",empty())
            .addAction(com.google.android.material.R.drawable.navigation_empty_icon, "._.",empty())
            .addAction(com.google.android.material.R.drawable.navigation_empty_icon, ":c",empty())
            .setContentText(notificationContent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            val btn = findViewById<Button>(R.id.button)
            btn.text = ":c"
            return
        }
        NotificationManagerCompat.from(this).notify(1234, builder.build())
    }

}

