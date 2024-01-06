package dev.nanid.selfcare.ui.home

import android.app.Activity
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import dev.nanid.selfcare.MainActivity
import dev.nanid.selfcare.R
import dev.nanid.selfcare.bgService
import dev.nanid.selfcare.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
    var broadcastReceiver: BroadcastReceiver? = null
    private var _binding: FragmentHomeBinding? = null
    var root: View? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        root = binding.root
        val intentFilter = IntentFilter("dev.nanid.moodAction")
        //create and register receiver
        broadcastReceiver = updateReceiver()
        (activity as MainActivity).registerReceiver(broadcastReceiver, intentFilter, Service.RECEIVER_EXPORTED)

        return root!!
    }

    inner class updateReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Toast.makeText(context,"yayo",Toast.LENGTH_SHORT)

            if(intent.hasExtra("mood")){
                val mood = intent.getStringExtra("mood")
                val sharedPreference =
                    context.getSharedPreferences("notification", Context.MODE_PRIVATE)
                val editor = sharedPreference.edit()
                //editor.remove("nInput")
                editor.putString("nInput", mood)
                editor.apply()

                updateUi(mood.toString())
            }

        }
    }

    fun updateUi(mood:String){
        val btn = root!!.findViewById<Button>(R.id.button)
        btn.setText("Mood "+mood)


        val tvBody = root!!.findViewById<TextView>(R.id.tvInfo)
        val tvTitle = root!!.findViewById<TextView>(R.id.tvTitle)
        val titles: Array<String> = resources.getStringArray(R.array.tvTitle)
        val infosPref = requireActivity().getSharedPreferences("infos", Context.MODE_PRIVATE)

        val defVal = "-1"
        var good = infosPref.getString("good",defVal) // could be done better but who
        var mid = infosPref.getString("mid",defVal)
        var bad = infosPref.getString("bad",defVal)
        var moodShelf = arrayOf(good,mid,bad)
        var bkup: Array<String> = resources.getStringArray(R.array.info)

        //---- logic for the core feature duuhhh ----
        for (i in 0..moodShelf.size-1){
            if(moodShelf[i] == defVal){
                moodShelf[i] = bkup[i]
            }
        }

        if(mood == ":)"){
            btn.setBackgroundColor(getResources().getColor(R.color.good))
            tvTitle.setText(titles[0])
            tvBody.setText(moodShelf[0])
        }
        else if(mood == ":|") {
            btn.setBackgroundColor(getResources().getColor(R.color.mid))
            tvTitle.setText(titles[1])
            tvBody.setText(moodShelf[1])
        }
        else if(mood == ":(") {
            btn.setBackgroundColor(getResources().getColor(R.color.bad))
            tvTitle.setText(titles[2])
            tvBody.setText(moodShelf[2])
        }
        else{
            btn.setText("Mood :3")
            btn.setBackgroundColor(getResources().getColor(R.color.teal_700))
            tvTitle.setText("Selfcare Info")
            tvBody.setText("wow solch leer ._.")

        }

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sharedPreference = requireActivity().getSharedPreferences("notification", Context.MODE_PRIVATE)
        val mood = sharedPreference.getString("nInput","mood")
        val root = view
        updateUi(mood.toString())
        val nameObserver = Observer<String> { newMood ->
            // Update the UI
            updateUi(newMood)
        }


        val btn = root.findViewById<Button>(R.id.button)
        val tvBody = root.findViewById<TextView>(R.id.tvInfo)
        val infosPref = requireActivity().getSharedPreferences("infos", Context.MODE_PRIVATE)

        val defVal = "-1"
        var good = infosPref.getString("good",defVal) // could be done better but who
        var mid = infosPref.getString("mid",defVal)
        var bad = infosPref.getString("bad",defVal)
        var moodShelf = arrayOf(good,mid,bad)
        //var bkup: Array<String> = resources.getStringArray(R.array.info)

        val infoSaveBtn = root.findViewById<Button>(R.id.saveInfo)



        tvBody.setOnFocusChangeListener(OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                infoSaveBtn.isVisible = true
            }
        })


        infoSaveBtn.setOnClickListener {
            val editor = infosPref.edit()
            //editor.remove("nInput")

            if(mood == ":)"){
                moodShelf[0] = tvBody.text.toString()
                editor.putString("good",moodShelf[0])
                editor.apply()
                Toast.makeText(context,"updated", Toast.LENGTH_SHORT).show()

            }
            else if(mood == ":|") {
                moodShelf[1] = tvBody.text.toString()
                editor.putString("mid",moodShelf[1])
                editor.apply()
                Toast.makeText(context,"updated", Toast.LENGTH_SHORT).show()

            }
            else if(mood == ":(") {
                moodShelf[2] = tvBody.text.toString()
                editor.putString("bad", moodShelf[2])
                editor.apply()
                Toast.makeText(context,"updated", Toast.LENGTH_SHORT).show()

            }else{
                val x = Toast.makeText(context,"no mood set",Toast.LENGTH_SHORT)
                x.show()
            }
            infoSaveBtn.isVisible = false
            tvBody.clearFocus()
        }



        btn.setOnClickListener {
            btn.text = "Mood :D"

            val service: Intent = Intent(
                context,
                bgService::class.java
            )
            service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            requireActivity().startService(service)
            //main.finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(broadcastReceiver)
        _binding = null
    }
}