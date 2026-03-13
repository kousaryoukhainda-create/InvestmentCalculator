package com.example.investmentcalculator

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.example.investmentcalculator.databinding.ActivityMainBinding
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: InvestmentCalculatorViewModel
    private val indianFormat: NumberFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))
    private var countUpAnimator: ValueAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(InvestmentCalculatorViewModel::class.java)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup currency spinner
        val currencyCodes = resources.getStringArray(R.array.currency_codes)
        val currencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyCodes)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = currencyAdapter

        // Setup compound frequency spinner
        val frequencies = arrayOf("Annually", "Semi-Annually", "Quarterly", "Monthly", "Daily")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, frequencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCompoundFrequency.adapter = adapter

        // Setup preset rate chips
        setupPresetChips()

        // Calculate Compound Interest Button
        binding.btnCalculateCompound.setOnClickListener {
            viewModel.principal.value = binding.etPrincipal.text.toString()
            viewModel.annualRate.value = binding.etRate.text.toString()
            viewModel.timeYears.value = binding.etTime.text.toString()
            viewModel.compoundFrequency.value = binding.spinnerCompoundFrequency.selectedItem.toString()
            viewModel.calculateCompoundInterest()
        }

        // Calculate Simple Interest Button
        binding.btnCalculateSimple.setOnClickListener {
            viewModel.principal.value = binding.etPrincipal.text.toString()
            viewModel.annualRate.value = binding.etRate.text.toString()
            viewModel.timeYears.value = binding.etTime.text.toString()
            viewModel.calculateSimpleInterest()
        }

        // Calculate SIP Button - now uses principal field as monthly investment
        binding.btnCalculateSIP.setOnClickListener {
            viewModel.principal.value = binding.etPrincipal.text.toString()
            viewModel.annualRate.value = binding.etRate.text.toString()
            viewModel.timeYears.value = binding.etTime.text.toString()
            viewModel.calculateSIP()
        }

        // Clear Button
        binding.btnClear.setOnClickListener {
            viewModel.clear()
            binding.resultCard.visibility = View.GONE
            binding.chartCard.visibility = View.GONE
            clearFieldErrors()
            // Clear preset chips
            listOf(R.id.chip6pct, R.id.chip8pct, R.id.chip10pct, R.id.chip12pct, R.id.chip15pct).forEach { id ->
                binding.root.findViewById<Chip>(id).isChecked = false
            }
            // Clear input fields
            binding.etPrincipal.text?.clear()
            binding.etRate.text?.clear()
            binding.etTime.text?.clear()
            binding.etMonthlySIP.text?.clear()
            // Reset compound frequency spinner
            binding.spinnerCompoundFrequency.setSelection(3) // Monthly
        }

        // Currency spinner listener
        binding.spinnerCurrency.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCurrency = currencyCodes[position]
                viewModel.selectedCurrency.value = selectedCurrency
                // Update hints with currency symbol
                updateCurrencyHints(selectedCurrency)
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    private fun observeViewModel() {
        viewModel.principal.observe(this) { }
        viewModel.annualRate.observe(this) { }
        viewModel.timeYears.observe(this) { }
        viewModel.compoundFrequency.observe(this) { }

        viewModel.result.observe(this) { result ->
            if (result != null) {
                displayResult(result)
            }
        }

        viewModel.growthData.observe(this) { data ->
            if (data.isNotEmpty()) {
                displayChart(data)
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                val message = getString(getErrorResource(error))
                Snackbar.make(binding.coordinatorLayout, message, Snackbar.LENGTH_SHORT).show()
            }
        }

        // Field-specific error observers
        viewModel.principalError.observe(this) { error ->
            setErrorOnField(binding.tilPrincipal, error)
        }
        viewModel.rateError.observe(this) { error ->
            setErrorOnField(binding.tilRate, error)
        }
        viewModel.timeError.observe(this) { error ->
            setErrorOnField(binding.tilTime, error)
        }
        viewModel.sipError.observe(this) { error ->
            setErrorOnField(binding.tilMonthlySIP, error)
        }
    }

    private fun displayResult(result: CalculationResult) {
        binding.resultCard.visibility = View.VISIBLE
        binding.chartCard.visibility = View.VISIBLE

        // Animate the result card appearance
        animateResultCard()

        binding.tvCalculationType.text = result.calculationType
        binding.tvPrincipal.text = formatCurrency(result.totalInvested, result.currencySymbol)
        binding.tvRate.text = "${result.rate}%"
        binding.tvTime.text = "${result.time} ${if (result.time == 1.0) "year" else "years"}"

        // Count-up animation for future value and interest
        animateCountUp(binding.tvFutureValue, 0.0, result.futureValue, prefix = result.currencySymbol)
        animateCountUp(binding.tvTotalInterest, 0.0, result.totalInterest, prefix = "+ ${result.currencySymbol}")

        // Scroll to results with animation
        binding.scrollView.post {
            binding.scrollView.smoothScrollTo(0, binding.resultCard.top)
        }
    }

    private fun animateResultCard() {
        // Scale animation
        binding.resultCard.scaleX = 0.8f
        binding.resultCard.scaleY = 0.8f
        binding.resultCard.alpha = 0f

        val scaleX = ObjectAnimator.ofFloat(binding.resultCard, "scaleX", 0.8f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.resultCard, "scaleY", 0.8f, 1f)
        val alpha = ObjectAnimator.ofFloat(binding.resultCard, "alpha", 0f, 1f)

        scaleX.duration = 400
        scaleY.duration = 400
        alpha.duration = 300

        scaleX.interpolator = OvershootInterpolator(0.8f)
        scaleY.interpolator = OvershootInterpolator(0.8f)
        alpha.interpolator = AccelerateDecelerateInterpolator()

        scaleX.start()
        scaleY.start()
        alpha.start()
    }

    private fun animateCountUp(targetView: android.widget.TextView, startValue: Double, endValue: Double, prefix: String = "") {
        countUpAnimator?.cancel()

        // Set initial value immediately
        targetView.text = prefix + formatNumber(startValue)

        countUpAnimator = ValueAnimator.ofFloat(startValue.toFloat(), endValue.toFloat()).apply {
            duration = 1200
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animation ->
                val animatedValue = (animation.animatedValue as Float).toDouble()
                targetView.text = prefix + formatNumber(animatedValue)
            }
            addListener(object : android.animation.AnimatorListenerAdapter {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    // Ensure final value is set correctly
                    targetView.text = prefix + formatNumber(endValue)
                }
            })
            start()
        }
    }

    private fun updateCurrencyHints(currencyCode: String) {
        val symbol = viewModel.getCurrencySymbol(currencyCode)
        binding.tilPrincipal.hint = "${getString(R.string.hint_principal)} ($symbol)"
        binding.tilMonthlySIP.hint = "${getString(R.string.hint_monthly_sip)} ($symbol)"
    }

    private fun formatCurrency(amount: Double, currencySymbol: String = "₹"): String {
        val formatted = indianFormat.format(amount)
        return "$currencySymbol$formatted"
    }

    private fun formatNumber(amount: Double): String {
        return indianFormat.format(amount)
    }

    private fun setupPresetChips() {
        val presetRates = mapOf(
            R.id.chip6pct to 6.0,
            R.id.chip8pct to 8.0,
            R.id.chip10pct to 10.0,
            R.id.chip12pct to 12.0,
            R.id.chip15pct to 15.0
        )

        presetRates.forEach { (chipId, rate) ->
            val chip = binding.root.findViewById<Chip>(chipId)
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    binding.etRate.setText(rate.toString())
                    // Clear other chips
                    presetRates.keys.filter { it != chipId }.forEach { otherId ->
                        binding.root.findViewById<Chip>(otherId).isChecked = false
                    }
                }
            }
        }
    }

    private fun displayChart(growthData: List<GrowthData>) {
        val chart = binding.growthChart
        chart.visibility = View.VISIBLE

        // Create entries for principal and interest
        val principalEntries = growthData.mapIndexed { index, data ->
            Entry(index.toFloat(), data.principal.toFloat())
        }
        val totalValueEntries = growthData.mapIndexed { index, data ->
            Entry(index.toFloat(), data.totalValue.toFloat())
        }

        // Principal dataset
        val principalDataSet = LineDataSet(principalEntries, "Invested").apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.chart_line_principal)
            valueTextColor = ContextCompat.getColor(this@MainActivity, R.color.text_primary)
            lineWidth = 2f
            setDrawCircles(true)
            setDrawCircleHole(true)
            circleRadius = 4f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(this@MainActivity, R.color.chart_fill_principal)
            fillAlpha = 100
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
        }

        // Total value dataset
        val totalValueDataSet = LineDataSet(totalValueEntries, "Total Value").apply {
            color = ContextCompat.getColor(this@MainActivity, R.color.chart_line_interest)
            valueTextColor = ContextCompat.getColor(this@MainActivity, R.color.text_primary)
            lineWidth = 2f
            setDrawCircles(true)
            setDrawCircleHole(true)
            circleRadius = 4f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(this@MainActivity, R.color.chart_fill_interest)
            fillAlpha = 100
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
        }

        // Apply chart data
        chart.data = LineData(principalDataSet, totalValueDataSet)

        // Customize chart appearance
        chart.apply {
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(false)
            setPinchZoom(false)

            // X-axis
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = ContextCompat.getColor(this@MainActivity, R.color.text_secondary)
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(this@MainActivity, R.color.divider)
                granularity = 1f
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return "Y${value.toInt() + 1}"
                    }
                }
            }

            // Left axis
            axisLeft.apply {
                textColor = ContextCompat.getColor(this@MainActivity, R.color.text_secondary)
                setDrawGridLines(true)
                gridColor = ContextCompat.getColor(this@MainActivity, R.color.divider)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val currencySymbol = viewModel.getCurrencySymbol(viewModel.selectedCurrency.value ?: "INR")
                        val amount = (value / 100000).toInt()
                        return "$currencySymbol${amount}L"
                    }
                }
            }

            // Right axis - disabled
            axisRight.isEnabled = false

            // Legend
            legend.apply {
                textColor = ContextCompat.getColor(this@MainActivity, R.color.text_primary)
                orientation = Legend.LegendOrientation.HORIZONTAL
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            }

            // Description
            description.isEnabled = false

            // Animate
            animateX(800)
            animateY(800)

            invalidate() // Refresh
        }
    }

    private fun formatCurrency(amount: Double): String {
        val formatted = indianFormat.format(amount)
        return "₹$formatted"
    }

    private fun setErrorOnField(layout: com.google.android.material.textfield.TextInputLayout, errorRes: String?) {
        if (errorRes != null) {
            layout.error = getString(getErrorResource(errorRes))
            layout.boxStrokeColor = ContextCompat.getColor(this, R.color.error)
        } else {
            layout.error = null
            layout.boxStrokeColor = ContextCompat.getColor(this, R.color.primary)
        }
    }

    private fun clearFieldErrors() {
        binding.tilPrincipal.error = null
        binding.tilRate.error = null
        binding.tilTime.error = null
        binding.tilMonthlySIP.error = null
        binding.tilPrincipal.boxStrokeColor = ContextCompat.getColor(this, R.color.primary)
        binding.tilRate.boxStrokeColor = ContextCompat.getColor(this, R.color.primary)
        binding.tilTime.boxStrokeColor = ContextCompat.getColor(this, R.color.primary)
        binding.tilMonthlySIP.boxStrokeColor = ContextCompat.getColor(this, R.color.primary)
    }

    private fun getErrorResource(error: String): Int {
        return when (error) {
            "error_invalid_principal" -> R.string.error_invalid_principal
            "error_empty_rate" -> R.string.error_empty_rate
            "error_negative_rate" -> R.string.error_negative_rate
            "error_invalid_time" -> R.string.error_invalid_time
            "error_invalid_sip" -> R.string.error_invalid_sip
            "error_calculation" -> R.string.error_calculation
            else -> R.string.error_calculation
        }
    }

    override fun onPause() {
        super.onPause()
        // Save input values to ViewModel
        viewModel.principal.value = binding.etPrincipal.text.toString()
        viewModel.annualRate.value = binding.etRate.text.toString()
        viewModel.timeYears.value = binding.etTime.text.toString()
        viewModel.compoundFrequency.value = binding.spinnerCompoundFrequency.selectedItem.toString()
    }

    override fun onResume() {
        super.onResume()
        // Restore input values from ViewModel
        binding.etPrincipal.setText(viewModel.principal.value)
        binding.etRate.setText(viewModel.annualRate.value)
        binding.etTime.setText(viewModel.timeYears.value)

        val freqIndex = when(viewModel.compoundFrequency.value) {
            "Annually" -> 0
            "Semi-Annually" -> 1
            "Quarterly" -> 2
            "Monthly" -> 3
            "Daily" -> 4
            else -> 3
        }
        binding.spinnerCompoundFrequency.setSelection(freqIndex)

        // Restore currency selection
        val currencyCodes = resources.getStringArray(R.array.currency_codes)
        val currencyIndex = currencyCodes.indexOf(viewModel.selectedCurrency.value)
        if (currencyIndex >= 0) {
            binding.spinnerCurrency.setSelection(currencyIndex)
            updateCurrencyHints(viewModel.selectedCurrency.value ?: "INR")
        }
    }
}
