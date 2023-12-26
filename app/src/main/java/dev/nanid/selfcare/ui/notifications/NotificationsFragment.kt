package dev.nanid.selfcare.ui.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat.registerReceiver
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.nanid.selfcare.MainActivity
import dev.nanid.selfcare.R
import dev.nanid.selfcare.databinding.FragmentNotificationsBinding


class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
                ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root


        val btn = root.findViewById<Button>(R.id.test)
        btn.setOnClickListener {
            (activity as MainActivity).SetAlarm()
        }

        return root
    }



    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null
    }
}