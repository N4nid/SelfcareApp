package dev.nanid.selfcare

import android.Manifest
import android.app.ActivityManager
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import dev.nanid.selfcare.databinding.ActivityMainBinding
import kotlin.random.Random


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    var broadcastReceiver: BroadcastReceiver? = null

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



        val sharedPreference =
            getSharedPreferences("setup", Context.MODE_PRIVATE)


        if (!sharedPreference.getBoolean("didNotiSettings",false)){
            val editor = sharedPreference.edit()
            editor.putBoolean("didNotiSettings",true)
            editor.apply()
            val intent: Intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS)
                .putExtra(Settings.EXTRA_APP_PACKAGE, "dev.nanid.selfcare")
                .putExtra(Settings.EXTRA_CHANNEL_ID, "nanid.selfcare.hideMe")
            startActivity(intent)
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, Array<String>(1){Manifest.permission.POST_NOTIFICATIONS}, 0)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + this.getPackageName())
                )
                startActivityForResult(intent, 101)
            }
        }

        if(! isMyServiceRunning(notiService::class.java)){

            val service = Intent(
                this,
                notiService::class.java
            )
            //Toast.makeText(this,"started",Toast.LENGTH_SHORT).show()
            service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startService(service)
        }


        val intentFilter = IntentFilter("dev.nanid.endAction")
        //create and register receiver
        broadcastReceiver = endReceiver()
        registerReceiver(broadcastReceiver, intentFilter)

    }

    //----------- own shit from here ----------


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 101) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "This feature is crucial to this app", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }




    fun isMyServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    inner class endReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            var intent = Intent("dev.nanid.moodAction")
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("received",true)
            sendBroadcast(intent)
            unregisterReceiver(broadcastReceiver)
            //Toast.makeText(context,"send",Toast.LENGTH_SHORT).show()
            finish()
            System.exit( 0 )
        }
    }

}


// ------- Notification service --------
class notiService : Service() {
    var broadcastReceiver: BroadcastReceiver? = null
    private val channelId = "nanid.selfcare.hideMe"
    var manager: AlarmManager? = null
    var pintent: PendingIntent? = null
    inner class notiReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if(intent.hasExtra("alarm") ){//&& intent.hasExtra("repeating")

                SetAlarm(intent.getIntExtra("alarm",1),intent.getBooleanExtra("type",false))
                Toast.makeText(context,"reminder set",Toast.LENGTH_SHORT).show()
                //Toast.makeText(context,"yayo ",Toast.LENGTH_SHORT).show()
            }else if(intent.hasExtra("stop")){
                stopAlarm()
            }else{
                val service: Intent = Intent(
                    context,
                    bgService::class.java
                )
                service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startService(service)

                if (intent.hasExtra("time")){
                    SetAlarm(intent.getIntExtra("time",1),intent.getBooleanExtra("type",false))
                }

            }

        }
    }

    fun stopAlarm(){
        try {
            manager!!.cancel(pintent!!)
            pintent!!.cancel()
        }catch (e:Exception){

        }
    }
    fun SetAlarm(time:Int,isDay:Boolean) {
        stopAlarm()

        val intent = Intent("dev.nanid.notify")
        intent.putExtra("time",time)
        intent.putExtra("type",isDay)
        pintent = PendingIntent.getBroadcast(this, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT)
        //cancel alarm??
        manager = getSystemService(ALARM_SERVICE) as AlarmManager

        var interval: Int
        if(isDay) interval = AlarmManager.INTERVAL_HOUR.toInt() * time  //(time * 1000*60).toInt() //
        else interval = AlarmManager.INTERVAL_DAY.toInt() * time  //(time * 1000).toInt()//

        manager!!.set(AlarmManager.RTC_WAKEUP,  System.currentTimeMillis() + interval ,pintent!!)
    }

    override fun onDestroy() {
        //Toast.makeText(this, "bgService stopped", Toast.LENGTH_LONG).show()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onCreate() {
        // create IntentFilter
        val intentFilter = IntentFilter("dev.nanid.notify")
        //create and register receiver
        broadcastReceiver = notiReceiver()
        registerReceiver(broadcastReceiver, intentFilter)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //Toast.makeText(this, "bgService started", Toast.LENGTH_LONG).show()
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId , "hide Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            NotificationManagerCompat.from(this).createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) //todo make notification icon
            .setContentTitle("")
            .setContentText("")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(false)

        val n: Notification = builder.build()

        ServiceCompat.startForeground(this,34,n,FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(34)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}

// ------- receive Notification action service --------

class bgService : Service() {
    var broadcastReceiver: BroadcastReceiver? = null
    private val channelId = "nanid.selfcare.notifications"

    inner class ActionReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            var endIntent = Intent("dev.nanid.endAction")
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            sendBroadcast(endIntent)

            if(intent.hasExtra("mood")){
                val mood = intent.getStringExtra("mood")
                val sharedPreference =
                    context.getSharedPreferences("notification", Context.MODE_PRIVATE)
                val editor = sharedPreference.edit()
                //editor.remove("nInput")
                editor.putString("nInput", mood)
                editor.apply()

                Toast.makeText(context,mood,Toast.LENGTH_SHORT).show()

                val notificationManager =
                    applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(1234)

                if(!isAppRunning(context,"dev.nanid.selfcare")){
                    startApp(context)
                }

            } else if(intent.hasExtra("received")){
                startApp(context)
            }



        }
    }

    private fun startApp(context: Context){
        val startIntent = Intent()
        startIntent.setClass(context,MainActivity::class.java)
        startIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(startIntent)

        //Toast.makeText(context,"yay",Toast.LENGTH_SHORT).show()
        stopSelf()
    }

    private fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        activityManager.runningAppProcesses?.apply {
            for (processInfo in this) {
                if (processInfo.processName == packageName) {
                    return true
                }
            }
        }
        return false
    }

    override fun onDestroy() {
        //Toast.makeText(this, "bgService stopped", Toast.LENGTH_LONG).show()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onCreate() {
        // create IntentFilter
        val intentFilter = IntentFilter("dev.nanid.moodAction")
        //create and register receiver
        broadcastReceiver = ActionReceiver()
        registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        //Toast.makeText(this, "bgService started", Toast.LENGTH_LONG).show()

        val titles: Array<String> = resources.getStringArray(R.array.notification_titles)
        val rnd = Random
        val num = rnd.nextInt(0,titles.size)

        ServiceCompat.startForeground(this,69,createNotification(titles[num],""),FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        return START_STICKY
    }

    private fun notificationAction(answer: String): PendingIntent? {
        val resultIntent = Intent("dev.nanid.moodAction")
        resultIntent.putExtra("mood",answer)
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val resultPendingIntent: PendingIntent? = PendingIntent.getBroadcast(this,System.currentTimeMillis().toInt(),resultIntent,PendingIntent.FLAG_IMMUTABLE);

        return resultPendingIntent
    }

    private fun createNotification(notificationTitel: String,notificationContent: String): Notification {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId , "Dont hide Notifications", NotificationManager.IMPORTANCE_DEFAULT)
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
            .setOngoing(true)


        val n: Notification = builder.build()
        return n
    }
    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}




