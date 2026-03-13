package com.example.investmentcalculator

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.example.investmentcalculator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: InvestmentCalculatorViewModel

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
            viewModel.calculateCompoundInterest()
        }

        // Calculate Simple Interest Button
        binding.btnCalculateSimple.setOnClickListener {
            viewModel.calculateSimpleInterest()
        }

        // Calculate SIP Button
        binding.btnCalculateSIP.setOnClickListener {
            val monthlyInput = binding.etMonthlySIP.text.toString().toDoubleOrNull() ?: 0.0
            viewModel.calculateSIP(monthlyInput)
        }

        // Clear Button
        binding.btnClear.setOnClickListener {
            viewModel.clear()
            binding.etMonthlySIP.text?.clear()
            binding.resultCard.visibility = View.GONE
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
                Snackbar.make(binding.root, error, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun displayResult(result: CalculationResult) {
        binding.resultCard.visibility = View.VISIBLE
        
        binding.tvCalculationType.text = result.calculationType
        binding.tvPrincipal.text = String.format("Principal: ₹%.2f", result.principal)
        binding.tvRate.text = String.format("Rate: %.2f%%", result.rate)
        binding.tvTime.text = String.format("Time: %.2f years", result.time)
        binding.tvFutureValue.text = String.format("₹%.2f", result.futureValue)
        binding.tvTotalInterest.text = String.format("₹%.2f", result.totalInterest)
        
        // Scroll to results
        binding.scrollView.post {
            binding.scrollView.smoothScrollTo(0, binding.resultCard.top)
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
            "Semi-Annually" -> 1
            "Quarterly" -> 2
            "Monthly" -> 3
            "Daily" -> 4
            else -> 3
        }
        binding.spinnerCompoundFrequency.setSelection(freqIndex)
    }
}
