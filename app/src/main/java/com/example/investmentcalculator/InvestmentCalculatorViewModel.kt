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
    val totalInvested: Double = 0.0,
    val calculationType: String = "Compound Interest"
)

class InvestmentCalculatorViewModel : ViewModel() {

    val principal = MutableLiveData<String>("")
    val annualRate = MutableLiveData<String>("")
    val timeYears = MutableLiveData<String>("")
    val compoundFrequency = MutableLiveData<String>("Monthly")
    val result = MutableLiveData<CalculationResult?>(null)
    
    // Field-specific error messages
    val principalError = MutableLiveData<String?>(null)
    val rateError = MutableLiveData<String?>(null)
    val timeError = MutableLiveData<String?>(null)
    val sipError = MutableLiveData<String?>(null)
    val errorMessage = MutableLiveData<String?>(null)

    fun calculateCompoundInterest() {
        // Clear previous errors
        clearErrors()
        
        try {
            val p = principal.value?.toDoubleOrNull()
            val r = annualRate.value?.toDoubleOrNull()
            val t = timeYears.value?.toDoubleOrNull()

            // Validate inputs with field-specific errors
            var hasError = false
            
            if (p == null || p <= 0) {
                principalError.value = "error_invalid_principal"
                hasError = true
            }
            if (r == null) {
                rateError.value = "error_empty_rate"
                hasError = true
            } else if (r < 0) {
                rateError.value = "error_negative_rate"
                hasError = true
            }
            if (t == null || t <= 0) {
                timeError.value = "error_invalid_time"
                hasError = true
            }

            if (hasError) return

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
                totalInvested = p,
                calculationType = "Compound Interest"
            )
        } catch (e: Exception) {
            errorMessage.value = "error_calculation"
        }
    }

    fun calculateSimpleInterest() {
        // Clear previous errors
        clearErrors()
        
        try {
            val p = principal.value?.toDoubleOrNull()
            val r = annualRate.value?.toDoubleOrNull()
            val t = timeYears.value?.toDoubleOrNull()

            // Validate inputs with field-specific errors
            var hasError = false
            
            if (p == null || p <= 0) {
                principalError.value = "error_invalid_principal"
                hasError = true
            }
            if (r == null) {
                rateError.value = "error_empty_rate"
                hasError = true
            } else if (r < 0) {
                rateError.value = "error_negative_rate"
                hasError = true
            }
            if (t == null || t <= 0) {
                timeError.value = "error_invalid_time"
                hasError = true
            }

            if (hasError) return

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
                totalInvested = p,
                calculationType = "Simple Interest"
            )
        } catch (e: Exception) {
            errorMessage.value = "error_calculation"
        }
    }

    fun calculateSIP() {
        // Clear previous errors
        clearErrors()
        
        try {
            val monthlyInvestment = principal.value?.toDoubleOrNull() ?: 0.0
            val r = annualRate.value?.toDoubleOrNull()
            val t = timeYears.value?.toDoubleOrNull()

            // Validate inputs with field-specific errors
            var hasError = false
            
            if (monthlyInvestment <= 0) {
                principalError.value = "error_invalid_sip"
                hasError = true
            }
            if (r == null) {
                rateError.value = "error_empty_rate"
                hasError = true
            } else if (r < 0) {
                rateError.value = "error_negative_rate"
                hasError = true
            }
            if (t == null || t <= 0) {
                timeError.value = "error_invalid_time"
                hasError = true
            }

            if (hasError) return

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
                totalInvested = totalPrincipal,
                calculationType = "SIP (Systematic Investment Plan)"
            )
        } catch (e: Exception) {
            errorMessage.value = "error_calculation"
        }
    }

    fun clear() {
        principal.value = ""
        annualRate.value = ""
        timeYears.value = ""
        compoundFrequency.value = "Monthly"
        result.value = null
        clearErrors()
    }

    private fun clearErrors() {
        principalError.value = null
        rateError.value = null
        timeError.value = null
        sipError.value = null
        errorMessage.value = null
    }

    private fun showError(message: String) {
        errorMessage.value = message
        result.value = null
    }
}
