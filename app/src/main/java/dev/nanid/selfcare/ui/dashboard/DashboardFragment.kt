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
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import dev.nanid.selfcare.R
import dev.nanid.selfcare.databinding.FragmentDashboardBinding
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


    //We connect our data to the UI Screen
    val data = LineData(linedataset)
    val lc = binding.linechart
    lc.data = data

    //Styling
    lc.setBackgroundColor(resources.getColor(R.color.inside_widget_bg))
    //lc.setBackgroundColor(resources.getColor(R.color.white))
    lc.animateXY(600, 300, Easing.EaseInCubic)

    lc.data.setValueFormatter(MyValueFormatter())
    lc.isHighlightPerTapEnabled = true
    lc.xAxis.isEnabled = true
    lc.axisLeft.isEnabled = false
    lc.axisRight.isEnabled = false
    lc.marker = CustomMarkerView(context,R.layout.marker_layout)

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

  interface IMarker {
    /**
     * @return The desired (general) offset you wish the IMarker to have on the x- and y-axis.
     * By returning x: -(width / 2) you will center the IMarker horizontally.
     * By returning y: -(height / 2) you will center the IMarker vertically.
     */
    val offset: MPPointF?

    /**
     * @return The offset for drawing at the specific `point`. This allows conditional adjusting of the Marker position.
     * If you have no adjustments to make, return getOffset().
     *
     * @param posX This is the X position at which the marker wants to be drawn.
     * You can adjust the offset conditionally based on this argument.
     * @param posY This is the X position at which the marker wants to be drawn.
     * You can adjust the offset conditionally based on this argument.
     */
    fun getOffsetForDrawingAtPos(posX: Float, posY: Float): MPPointF?

    /**
     * This method enables a specified custom IMarker to update it's content every time the IMarker is redrawn.
     *
     * @param e         The Entry the IMarker belongs to. This can also be any subclass of Entry, like BarEntry or
     * CandleEntry, simply cast it at runtime.
     * @param highlight The highlight object contains information about the highlighted value such as it's dataset-index, the
     * selected range or stack-index (only stacked bar entries).
     */
    fun refreshContent(e: Entry?, highlight: Highlight?)

    /**
     * Draws the IMarker on the given position on the screen with the given Canvas object.
     *
     * @param canvas
     * @param posX
     * @param posY
     */
    fun draw(canvas: Canvas?, posX: Float, posY: Float)
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
