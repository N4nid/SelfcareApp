package dev.nanid.selfcare.ui.dashboard

import android.content.Context
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.MarkerImage
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import dev.nanid.selfcare.R
import dev.nanid.selfcare.databinding.CustomMarkerLayoutBinding
import dev.nanid.selfcare.databinding.FragmentDashboardBinding
import dev.nanid.selfcare.databinding.MarkerLayoutBinding
import java.text.DecimalFormat


class DashboardFragment : Fragment() {

  private var _binding: FragmentDashboardBinding? = null
  val happyAmount = 15F
  val neutralAmount = 10f
  val sadAmount = 5f
  // This property is only valid between onCreateView and
  // onDestroyView.
  private val binding
    get() = _binding!!

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?
  ): View {
    val dashboardViewModel = ViewModelProvider(this).get(DashboardViewModel::class.java)

    _binding = FragmentDashboardBinding.inflate(inflater, container, false)
    val root: View = binding.root

    return root
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val root = view
    setLineChartData()
  }

  fun setLineChartData() {

    val linevalues = ArrayList<Entry>()
    linevalues.add(Entry(20f,happyAmount))
    linevalues.add(Entry(30f,sadAmount))
    linevalues.add(Entry(40f,neutralAmount))
    linevalues.add(Entry(50f,happyAmount))
    linevalues.add(Entry(60f,sadAmount))
    linevalues.add(Entry(70f,sadAmount))
    linevalues.add(Entry(80f,neutralAmount))
    linevalues.add(Entry(90f,sadAmount))
    linevalues.add(Entry(100f,happyAmount))
    linevalues.add(Entry(110f,neutralAmount))
    linevalues.add(Entry(120f,neutralAmount))

    val linedataset = LineDataSet(linevalues, "First")
    //We add features to our chart
    linedataset.color = resources.getColor(R.color.purple_200)

    linedataset.circleRadius = 5f
    linedataset.setDrawFilled(false)
    linedataset.valueTextSize = 15f
    linedataset.valueTextColor = resources.getColor(R.color.white)

    linedataset.setMode(LineDataSet.Mode.CUBIC_BEZIER);

    /*
                  /   |
  ______________//_   |
 / ___         ___ \  |
/ /   \       /   \ \ |
\ \___/  \_/  \___/ / |
 \_________________/  |
 -- No entrys yet --  |

:C not working since there is no mutliline support :C
    */

    //We connect our data to the UI Screen
    val data = LineData(linedataset)
    val lc = binding.linechart
    lc.data = data
    lc.setDrawMarkers(true)
    lc.setNoDataText("no entrys yet")

    //Styling
    lc.setBackgroundColor(resources.getColor(R.color.inside_widget_bg))
    //lc.setBackgroundColor(resources.getColor(R.color.white))
    lc.animateXY(600, 300, Easing.EaseInCubic)

    if(lc.data != null)lc.data.setValueFormatter(MyValueFormatter())
    lc.isHighlightPerTapEnabled = true
    lc.xAxis.isEnabled = true
    lc.axisLeft.isEnabled = false
    lc.axisRight.isEnabled = false

    //val markerPop = context?.let { CustomMarkerView(it,R.layout.marker_layout) }
    //val markerBind = markerPop?.let { CustomMarkerLayoutBinding.bind(it) }
    //lc.marker = markerPop


  }


  class CustomMarkerView(context: Context?, layoutResource: Int) :
    MarkerView(context, layoutResource) {
    private val tvContent: TextView

    init {

      // this markerview only displays a textview
      tvContent = findViewById<View>(R.id.tvContent) as TextView

    }

    // callbacks everytime the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry, highlight: Highlight) {
      tvContent.text = "" + e.getX() // set the entry-value as the display text
    }

    fun getXOffset(xpos: Float): Int {
      // this will center the marker-view horizontally
      return -(width / 2)
    }

    fun getYOffset(ypos: Float): Int {
      // this will cause the marker-view to be above the selected value
      return -height
    }
  }


  class MyValueFormatter : ValueFormatter() {

    val happyAmount = 15f
    val neutralAmount = 10f
    val sadAmount = 5f

    private val format = DecimalFormat("###,##0.000")
    // override this for e.g. LineChart or ScatterChart
    override fun getPointLabel(entry: Entry?): String {
      if (entry != null) {
        if(entry.y == happyAmount) return ":)"
        else if(entry.y == neutralAmount) return ":|"
        else return ":("
      }else return "tf"
    }
    // override this for custom formatting of XAxis or YAxis labels
    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
      if(value >= happyAmount) return ":)"
      else if(value == neutralAmount) return ":|"
      else return ":("
      //return format.format(value)
    }
    // ... override other methods for the other chart types
  }

  override fun onDestroyView() {
    super.onDestroyView()
    _binding = null
  }
}
