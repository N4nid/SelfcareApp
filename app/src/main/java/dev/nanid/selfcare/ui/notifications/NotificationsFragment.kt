package dev.nanid.selfcare.ui.notifications


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import android.widget.ToggleButton

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.nanid.selfcare.MainActivity
import dev.nanid.selfcare.R

import dev.nanid.selfcare.databinding.FragmentNotificationsBinding
import java.lang.Exception


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


        val amount = root.findViewById<EditText>(R.id.editTextNumber2)
        val remindSwitcher = root.findViewById<Button>(R.id.button2)
        val intervalType = root.findViewById<ToggleButton>(R.id.switchTime) //checked(true) = day ; false = hours

        val sharedPreferences = requireActivity().getSharedPreferences("settings", Context.MODE_PRIVATE)
        var reminding = false

        try {
            reminding = sharedPreferences.getBoolean("reminding",false)
            var time = sharedPreferences.getInt("time",-1)
            amount.setText(time.toString())
            if (time == -1)amount.setText("")
            intervalType.isChecked = sharedPreferences.getBoolean("TypeIsDay",false)

            if(reminding){
                remindSwitcher.setText("Stop reminding")
            }else{
                remindSwitcher.setText("Start reminding")
            }

        }catch (e: Exception){
            remindSwitcher.setText("Stop reminding")
            Log.wtf("y00oo..",e)
        }



        remindSwitcher.setOnClickListener {
            //val editor = sharedPreferences.edit()
            if (reminding){
                reminding = false
                remindSwitcher.setText("Start reminding")
                val editor = sharedPreferences.edit()
                //editor.remove("nInput")
                editor.putBoolean("reminding",false)
                editor.apply()

                var intent = Intent("dev.nanid.notify")
                intent.putExtra("stop",true)

                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                (activity as MainActivity).sendBroadcast(intent)

                Toast.makeText(context,"stopped",Toast.LENGTH_SHORT).show()
            }else{
                val time:Int
                reminding = true
                try {
                    time = amount.text.toString().toInt()
                    if (time < 1) throw Exception("input to low")

                    remindSwitcher.setText("Stop reminding")
                    val editor = sharedPreferences.edit()
                    editor.putInt("time",time)
                    editor.putBoolean("TypeIsDay",intervalType.isChecked)
                    editor.putBoolean("reminding", true)
                    editor.apply()

                    var intent = Intent("dev.nanid.notify")
                    intent.putExtra("alarm",time)
                    intent.putExtra("type",intervalType.isChecked)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    (activity as MainActivity).sendBroadcast(intent)

                }catch (e:Exception){
                    Toast.makeText(context,"input time",Toast.LENGTH_SHORT).show()
                }
            }
        }


        return root
    }



    override fun onDestroyView() {

        super.onDestroyView()
        _binding = null
    }
}