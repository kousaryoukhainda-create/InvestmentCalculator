package com.example.investmentcalculator

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.example.investmentcalculator.databinding.ActivityMainBinding
import java.text.NumberFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: InvestmentCalculatorViewModel
    private val indianFormat: NumberFormat = NumberFormat.getNumberInstance(Locale("en", "IN"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(InvestmentCalculatorViewModel::class.java)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        // Setup compound frequency spinner
        val frequencies = arrayOf("Annually", "Semi-Annually", "Quarterly", "Monthly", "Daily")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, frequencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCompoundFrequency.adapter = adapter

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
            clearFieldErrors()
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
        
        // Animate the result card appearance
        animateResultCard()

        binding.tvCalculationType.text = result.calculationType
        binding.tvPrincipal.text = formatCurrency(result.totalInvested)
        binding.tvRate.text = "${result.rate}%"
        binding.tvTime.text = "${result.time} ${if (result.time == 1.0) "year" else "years"}"
        binding.tvFutureValue.text = formatCurrency(result.futureValue)
        binding.tvTotalInterest.text = "+ ${formatCurrency(result.totalInterest)}"

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

        // Animate future value counter
        animateValueCounter()
    }

    private fun animateValueCounter() {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = 800
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.start()
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
    }
}
