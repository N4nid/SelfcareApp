package dev.nanid.selfcare

import android.Manifest
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.Toast
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

    val notificationInput = "nInput"
    private lateinit var binding: ActivityMainBinding
    private val channelId = "nanid.selfcare.notifications"


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


        val serviceUp = isMyServiceRunning(bgService::class.java)
        if(!serviceUp){
            val service: Intent = Intent(
                this,
                bgService::class.java
            )
            service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startService(service)
        }

        val sharedPreference =  getSharedPreferences("notification",Context.MODE_PRIVATE)
        val mood = sharedPreference.getString(notificationInput,"mood")

        val btn = findViewById<Button>(R.id.button)
        btn.setText(mood)
        val titles: Array<String> = resources.getStringArray(R.array.notification_titles)
        btn.setOnClickListener {
            val rnd = Random
            val num = rnd.nextInt(0,titles.size)
            postNotification(titles[num],"")

            val serviceUp = isMyServiceRunning(bgService::class.java)
            btn.text = ":"+serviceUp
            if(!serviceUp){
                val service: Intent = Intent(
                    this,
                    bgService::class.java
                )
                service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startService(service)
            }

        }
    }

    //----------- own shit from here ----------

    private fun notificationAction(answer: String): PendingIntent? {
        val resultIntent = Intent("dev.nanid.moodAction")
        //val intent = Intent(this, dev.nanid.selfcare.Service.ActionReceiver::class.java)
        resultIntent.putExtra("mood",answer)
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val resultPendingIntent: PendingIntent? = PendingIntent.getBroadcast(this,System.currentTimeMillis().toInt(),resultIntent,PendingIntent.FLAG_IMMUTABLE);

        return resultPendingIntent
    }

    private fun postNotification(notificationTitel: String,notificationContent: String){
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId , "coolChannel", NotificationManager.IMPORTANCE_DEFAULT)
            NotificationManagerCompat.from(this).createNotificationChannel(channel)
        }

        var moods = arrayOf(":)",":|",":(")
        var pIntents: Array<PendingIntent?> = emptyArray()

        for (i in 0..2){
            pIntents += notificationAction(moods[i])
        }



        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) //todo make notification icon
            .setContentTitle(notificationTitel)
            .setContentText(notificationContent)
            .addAction(R.drawable.ic_dashboard_black_24dp,moods[0],pIntents[0])
            .addAction(R.drawable.ic_dashboard_black_24dp, moods[1],pIntents[1])
            .addAction(R.drawable.ic_dashboard_black_24dp, moods[2],pIntents[2])
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, Array<String>(1){Manifest.permission.POST_NOTIFICATIONS}, 0)
            val btn = findViewById<Button>(R.id.button)
            btn.text = "Please allow notifications :c"
            return
        }
        NotificationManagerCompat.from(this).notify(1234, builder.build())
    }


    private fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

}


class bgService : Service() {
    var broadcastReceiver: BroadcastReceiver? = null


    inner class ActionReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            val mood = intent.getStringExtra("mood")
            val sharedPreference = context.getSharedPreferences("notification",Context.MODE_PRIVATE)
            val editor = sharedPreference.edit()
            editor.remove("nInput")
            editor.putString("nInput",mood)
            editor.apply()

            val i: Intent = Intent(context, MainActivity::class.java)
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(i)

            val notificationManager =
                applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(1234)

            Toast.makeText(context,mood,Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        Toast.makeText(this, "bgService stopped", Toast.LENGTH_LONG).show()
    }

    override fun onCreate() {
        // create IntentFilter
        val intentFilter = IntentFilter("dev.nanid.moodAction")
        //Toast.makeText(this,"yeee",Toast.LENGTH_SHORT).show()
        //create and register receiver
        broadcastReceiver = ActionReceiver()
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Toast.makeText(this, "bgService started", Toast.LENGTH_LONG).show()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}




