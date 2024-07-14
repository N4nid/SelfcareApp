package dev.nanid.selfcare.ui.home

import android.app.Activity
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
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import dev.nanid.selfcare.MainActivity
import dev.nanid.selfcare.MoodData
import dev.nanid.selfcare.R
import dev.nanid.selfcare.bgService
import dev.nanid.selfcare.databinding.FragmentHomeBinding
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {
  var broadcastReceiver: BroadcastReceiver? = null
  private val model: MoodData by viewModels()
  private var _binding: FragmentHomeBinding? = null
  var root: View? = null

  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding
    get() = _binding!!

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    val homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

    _binding = FragmentHomeBinding.inflate(inflater, container, false)
    root = binding.root
    val intentFilter = IntentFilter("dev.nanid.moodAction")
    // create and register receiver
    broadcastReceiver = updateReceiver()
    (activity as MainActivity).registerReceiver(
        broadcastReceiver,
        intentFilter,
        Service.RECEIVER_EXPORTED
    )

    return root!!
  }

  inner class updateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      Toast.makeText(context, "yayo", Toast.LENGTH_SHORT)

      if (intent.hasExtra("mood")) {
        val mood = intent.getStringExtra("mood")
        val sharedPreference = context.getSharedPreferences("notification", Context.MODE_PRIVATE)
        val editor = sharedPreference.edit()
        // editor.remove("nInput")
        editor.putString("nInput", mood)
        editor.apply()

        // log mood here?
        updateUi(mood.toString())
      }
    }
  }

  /*
  LOGGING CSV STRUCTUR

  |date    |mood|
  ---------------
  example:

  |02.12.24|3   |

   */

  fun logMood(path: String, mood: String) {
    val moodVal: Int
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm")
    val date = LocalDateTime.now().format(formatter)
    if (mood == ":)") {
      moodVal = 3
    } else if (mood == ":|") {
      moodVal = 2
    } else if (mood == ":(") {
      moodVal = 1
    } else {
      moodVal = -1
    }
    Toast.makeText(context, moodVal.toString() + "|" + date.toString(), Toast.LENGTH_SHORT).show()
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
    if (requestCode == 69 && resultCode == Activity.RESULT_OK) {
      // The result data contains a URI for the document or directory that
      // the user selected.
      resultData?.data?.also { uri ->
        // Perform operations on the document using its URI.
        val infosPref = requireActivity().getSharedPreferences("infos", Context.MODE_PRIVATE)
        infosPref.edit().putString("statsPath", uri.path.toString()).apply()
        val sharedPreference =
            requireActivity().getSharedPreferences("notification", Context.MODE_PRIVATE)
        val mood = sharedPreference.getString("nInput", "mood")
        logMood(uri.path.toString(), mood.toString())
      }
    }
  }

  public fun updateUi(mood: String) {
    val btn = root!!.findViewById<Button>(R.id.button)
    btn.setText("Mood " + mood)

    val tvBody = root!!.findViewById<TextView>(R.id.tvInfo)
    val tvTitle = root!!.findViewById<TextView>(R.id.tvTitle)
    val titles: Array<String> = resources.getStringArray(R.array.tvTitle)
    val infosPref = requireActivity().getSharedPreferences("infos", Context.MODE_PRIVATE)

    val defVal = "-1"
    var good = infosPref.getString("good", defVal) // could be done better but who
    var mid = infosPref.getString("mid", defVal)
    var bad = infosPref.getString("bad", defVal)
    var moodShelf = arrayOf(good, mid, bad)
    var bkup: Array<String> = resources.getStringArray(R.array.info)

    // ---- logic for the core feature duuhhh ----
    for (i in 0..moodShelf.size - 1) {
      if (moodShelf[i] == defVal) {
        moodShelf[i] = bkup[i]
      }
    }

    if (mood == ":)") {
      btn.setBackgroundColor(getResources().getColor(R.color.good))
      tvTitle.setText(titles[0])
      tvBody.setText(moodShelf[0])
    } else if (mood == ":|") {
      btn.setBackgroundColor(getResources().getColor(R.color.mid))
      tvTitle.setText(titles[1])
      tvBody.setText(moodShelf[1])
    } else if (mood == ":(") {
      btn.setBackgroundColor(getResources().getColor(R.color.bad))
      tvTitle.setText(titles[2])
      tvBody.setText(moodShelf[2])
    } else {
      btn.setText("Mood :3")
      btn.setBackgroundColor(getResources().getColor(R.color.teal_700))
      tvTitle.setText("Selfcare Info")
      tvBody.setText("wow solch leer ._.")
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val sharedPreference =
        requireActivity().getSharedPreferences("notification", Context.MODE_PRIVATE)

    var mood = arguments?.getString("mood")
    // Toast.makeText(context, "arg" + mood, Toast.LENGTH_SHORT).show()
    if (mood == null) {
      mood = sharedPreference.getString("nInput", "meh")
      // mood = "wow"
      // Toast.makeText(context, "shrd " + mood, Toast.LENGTH_SHORT).show()
    }

    updateUi(mood.toString())

    val root = view
    val logBtn = root.findViewById<Button>(R.id.save)
    val btn = root.findViewById<Button>(R.id.button)
    val tvBody = root.findViewById<TextView>(R.id.tvInfo)
    val infosPref = requireActivity().getSharedPreferences("infos", Context.MODE_PRIVATE)

    val defVal = "-1"
    var good = infosPref.getString("good", defVal) // could be done better but who
    var mid = infosPref.getString("mid", defVal)
    var bad = infosPref.getString("bad", defVal)
    var moodShelf = arrayOf(good, mid, bad)
    // var bkup: Array<String> = resources.getStringArray(R.array.info)

    val infoSaveBtn = root.findViewById<Button>(R.id.saveInfo)

    logBtn.setOnClickListener {
      val path: String = infosPref.getString("statsPath", "-1").toString()
      if (path == "-1") {
        // get path to save statistics to
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {}
        startActivityForResult(intent, 69)
      } else {
        val txtField = root.findViewById<EditText>(R.id.textfield)
        logMood(path, mood.toString())
      }
    }

    tvBody.setOnFocusChangeListener(
        OnFocusChangeListener { _, hasFocus ->
          if (hasFocus) {
            infoSaveBtn.isVisible = true
          }
        }
    )

    infoSaveBtn.setOnClickListener {
      val editor = infosPref.edit()
      // editor.remove("nInput")

      if (mood == ":)") {
        moodShelf[0] = tvBody.text.toString()
        editor.putString("good", moodShelf[0])
        editor.apply()
        Toast.makeText(context, "updated", Toast.LENGTH_SHORT).show()
      } else if (mood == ":|") {
        moodShelf[1] = tvBody.text.toString()
        editor.putString("mid", moodShelf[1])
        editor.apply()
        Toast.makeText(context, "updated", Toast.LENGTH_SHORT).show()
      } else if (mood == ":(") {
        moodShelf[2] = tvBody.text.toString()
        editor.putString("bad", moodShelf[2])
        editor.apply()
        Toast.makeText(context, "updated", Toast.LENGTH_SHORT).show()
      } else {
        val x = Toast.makeText(context, "no mood set", Toast.LENGTH_SHORT)
        x.show()
      }
      infoSaveBtn.isVisible = false
      tvBody.clearFocus()
    }

    btn.setOnClickListener {
      btn.text = "Mood :D"

      val service: Intent = Intent(context, bgService::class.java)
      service.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      requireActivity().startService(service)
      // main.finish()
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    requireActivity().unregisterReceiver(broadcastReceiver)
    _binding = null
  }
}
