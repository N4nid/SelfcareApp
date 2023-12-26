package dev.nanid.selfcare.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dev.nanid.selfcare.MainActivity
import dev.nanid.selfcare.R
import dev.nanid.selfcare.bgService
import dev.nanid.selfcare.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var main = MainActivity()

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
        val root: View = binding.root


        var mood:String?
        try {
            val sharedPreference = requireActivity().getSharedPreferences("notification", Context.MODE_PRIVATE)
            mood = sharedPreference.getString("nInput","mood")
        }catch (e: Exception){
            Log.wtf("y00oo..",e)
            mood = ":c"
        }

        val btn = root.findViewById<Button>(R.id.button)
        btn.setText("Mood "+mood)

        val tvBody = root.findViewById<TextView>(R.id.tvBody)
        val tvTitle = root.findViewById<TextView>(R.id.tvTitle)
        val titles: Array<String> = resources.getStringArray(R.array.tvTitle)
        val info: Array<String> = resources.getStringArray(R.array.info)

        //---- logic for the core feature duuhhh ----
        if(mood == ":)"){
            btn.setBackgroundColor(getResources().getColor(R.color.good))
            tvTitle.setText(titles[0])
            tvBody.setText(info[0])
        }
        else if(mood == ":|") {
            btn.setBackgroundColor(getResources().getColor(R.color.mid))
            tvTitle.setText(titles[1])
            tvBody.setText(info[1])
        }
        else if(mood == ":(") {
            btn.setBackgroundColor(getResources().getColor(R.color.bad))
            tvTitle.setText(titles[2])
            tvBody.setText(info[2])
        }
        else{
            btn.setText("Mood :3")
            btn.setBackgroundColor(getResources().getColor(R.color.teal_700))
            tvTitle.setText("Selfcare Info")
            tvBody.setText("wow solch leer ._.")

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

        return root
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}