package com.example.investmentcalculator

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.pow

data class CalculationResult(
    val principal: Double = 0.0,
    val rate: Double = 0.0,
    val time: Double = 0.0,
    val compoundFrequency: Int = 12,
    val futureValue: Double = 0.0,
    val totalInterest: Double = 0.0,
    val calculationType: String = "Compound Interest"
)

class InvestmentCalculatorViewModel : ViewModel() {
    
    val principal = MutableLiveData<String>("")
    val annualRate = MutableLiveData<String>("")
    val timeYears = MutableLiveData<String>("")
    val compoundFrequency = MutableLiveData<String>("Monthly")
    val result = MutableLiveData<CalculationResult?>(null)
    val errorMessage = MutableLiveData<String?>(null)

    fun calculateCompoundInterest() {
        try {
            errorMessage.value = null
            
            val p = principal.value?.toDoubleOrNull() ?: return showError("Enter principal amount")
            val r = annualRate.value?.toDoubleOrNull() ?: return showError("Enter annual rate")
            val t = timeYears.value?.toDoubleOrNull() ?: return showError("Enter time period")
            
            if (p <= 0) return showError("Principal must be greater than 0")
            if (r < 0) return showError("Rate cannot be negative")
            if (t <= 0) return showError("Time must be greater than 0")
            
            val n = when (compoundFrequency.value) {
                "Annually" -> 1
                "Semi-Annually" -> 2
                "Quarterly" -> 4
                "Monthly" -> 12
                "Daily" -> 365
                else -> 12
            }
            
            val rateDecimal = r / 100
            val futureValue = p * (1 + rateDecimal / n).pow(n * t)
            val totalInterest = futureValue - p
            
            result.value = CalculationResult(
                principal = p,
                rate = r,
                time = t,
                compoundFrequency = n,
                futureValue = futureValue,
                totalInterest = totalInterest,
                calculationType = "Compound Interest"
            )
        } catch (e: Exception) {
            showError("Calculation error: ${e.message}")
        }
    }

    fun calculateSimpleInterest() {
        try {
            errorMessage.value = null
            
            val p = principal.value?.toDoubleOrNull() ?: return showError("Enter principal amount")
            val r = annualRate.value?.toDoubleOrNull() ?: return showError("Enter annual rate")
            val t = timeYears.value?.toDoubleOrNull() ?: return showError("Enter time period")
            
            if (p <= 0) return showError("Principal must be greater than 0")
            if (r < 0) return showError("Rate cannot be negative")
            if (t <= 0) return showError("Time must be greater than 0")
            
            val rateDecimal = r / 100
            val totalInterest = p * rateDecimal * t
            val futureValue = p + totalInterest
            
            result.value = CalculationResult(
                principal = p,
                rate = r,
                time = t,
                compoundFrequency = 1,
                futureValue = futureValue,
                totalInterest = totalInterest,
                calculationType = "Simple Interest"
            )
        } catch (e: Exception) {
            showError("Calculation error: ${e.message}")
        }
    }

    fun calculateSIP(monthlyInvestment: Double) {
        try {
            errorMessage.value = null
            
            val r = annualRate.value?.toDoubleOrNull() ?: return showError("Enter annual rate")
            val t = timeYears.value?.toDoubleOrNull() ?: return showError("Enter time period")
            
            if (monthlyInvestment <= 0) return showError("Monthly investment must be greater than 0")
            if (r < 0) return showError("Rate cannot be negative")
            if (t <= 0) return showError("Time must be greater than 0")
            
            val monthlyRate = r / 100 / 12
            val totalMonths = (t * 12).toInt()
            val totalPrincipal = monthlyInvestment * totalMonths
            
            val futureValue = if (monthlyRate > 0) {
                monthlyInvestment * (((1 + monthlyRate).pow(totalMonths) - 1) / monthlyRate) * (1 + monthlyRate)
            } else {
                totalPrincipal
            }
            
            val totalInterest = futureValue - totalPrincipal
            
            result.value = CalculationResult(
                principal = totalPrincipal,
                rate = r,
                time = t,
                compoundFrequency = 12,
                futureValue = futureValue,
                totalInterest = totalInterest,
                calculationType = "SIP (Systematic Investment Plan)"
            )
        } catch (e: Exception) {
            showError("Calculation error: ${e.message}")
        }
    }

    fun clear() {
        principal.value = ""
        annualRate.value = ""
        timeYears.value = ""
        compoundFrequency.value = "Monthly"
        result.value = null
        errorMessage.value = null
    }

    private fun showError(message: String) {
        errorMessage.value = message
        result.value = null
    }
}
