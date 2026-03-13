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
    val calculationType: String = "Compound Interest",
    val currencySymbol: String = "₹"
)

data class GrowthData(
    val year: Int,
    val principal: Double,
    val interest: Double,
    val totalValue: Double
)

class InvestmentCalculatorViewModel : ViewModel() {

    val principal = MutableLiveData<String>("")
    val annualRate = MutableLiveData<String>("")
    val timeYears = MutableLiveData<String>("")
    val compoundFrequency = MutableLiveData<String>("Monthly")
    val selectedCurrency = MutableLiveData<String>("INR")
    val result = MutableLiveData<CalculationResult?>(null)
    val growthData = MutableLiveData<List<GrowthData>>(emptyList())

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

            // Safe to use non-null values now
            val principalValue = p!!
            val rateValue = r!!
            val timeValue = t!!

            val n = when (compoundFrequency.value) {
                "Annually" -> 1
                "Semi-Annually" -> 2
                "Quarterly" -> 4
                "Monthly" -> 12
                "Daily" -> 365
                else -> 12
            }

            val rateDecimal = rateValue / 100
            val futureValue = principalValue * (1 + rateDecimal / n).pow(n * timeValue)
            val totalInterest = futureValue - principalValue

            val currencySymbol = getCurrencySymbol(selectedCurrency.value ?: "INR")

            result.value = CalculationResult(
                principal = principalValue,
                rate = rateValue,
                time = timeValue,
                compoundFrequency = n,
                futureValue = futureValue,
                totalInterest = totalInterest,
                totalInvested = principalValue,
                calculationType = "Compound Interest",
                currencySymbol = currencySymbol
            )

            // Generate year-by-year growth data for chart
            generateGrowthData(principalValue, rateValue, timeValue, n, isSIP = false)
        } catch (e: Exception) {
            errorMessage.value = "error_calculation"
        }
    }

    private fun generateGrowthData(principal: Double, rate: Double, time: Double, n: Int, isSIP: Boolean, monthlyInvestment: Double = 0.0) {
        val growthList = mutableListOf<GrowthData>()
        val rateDecimal = rate / 100
        val years = time.toInt()

        for (year in 1..years) {
            val yearData = if (isSIP) {
                // SIP calculation for each year
                val totalMonths = year * 12
                val totalPrincipal = monthlyInvestment * totalMonths
                val monthlyRate = rateDecimal / 12
                val futureValue = if (monthlyRate > 0) {
                    monthlyInvestment * (((1 + monthlyRate).pow(totalMonths) - 1) / monthlyRate) * (1 + monthlyRate)
                } else {
                    totalPrincipal
                }
                val interest = futureValue - totalPrincipal
                GrowthData(year, totalPrincipal, interest, futureValue)
            } else {
                // Compound interest calculation for each year
                val yearValue = principal * (1 + rateDecimal / n).pow(n * year)
                val yearInterest = yearValue - principal
                GrowthData(year, principal, yearInterest, yearValue)
            }
            growthList.add(yearData)
        }

        growthData.value = growthList
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

            // Safe to use non-null values now
            val principalValue = p!!
            val rateValue = r!!
            val timeValue = t!!

            val rateDecimal = rateValue / 100
            val totalInterest = principalValue * rateDecimal * timeValue
            val futureValue = principalValue + totalInterest

            val currencySymbol = getCurrencySymbol(selectedCurrency.value ?: "INR")

            result.value = CalculationResult(
                principal = principalValue,
                rate = rateValue,
                time = timeValue,
                compoundFrequency = 1,
                futureValue = futureValue,
                totalInterest = totalInterest,
                totalInvested = principalValue,
                calculationType = "Simple Interest",
                currencySymbol = currencySymbol
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

            // Safe to use non-null values now
            val rateValue = r!!
            val timeValue = t!!

            val monthlyRate = rateValue / 100 / 12
            val totalMonths = (timeValue * 12).toInt()
            val totalPrincipal = monthlyInvestment * totalMonths

            val futureValue = if (monthlyRate > 0) {
                monthlyInvestment * (((1 + monthlyRate).pow(totalMonths) - 1) / monthlyRate) * (1 + monthlyRate)
            } else {
                totalPrincipal
            }

            val totalInterest = futureValue - totalPrincipal

            val currencySymbol = getCurrencySymbol(selectedCurrency.value ?: "INR")

            result.value = CalculationResult(
                principal = totalPrincipal,
                rate = rateValue,
                time = timeValue,
                compoundFrequency = 12,
                futureValue = futureValue,
                totalInterest = totalInterest,
                totalInvested = totalPrincipal,
                calculationType = "SIP (Systematic Investment Plan)",
                currencySymbol = currencySymbol
            )

            // Generate year-by-year growth data for chart
            generateGrowthData(0.0, rateValue, timeValue, 12, isSIP = true, monthlyInvestment = monthlyInvestment)
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
        growthData.value = emptyList()
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

    fun getCurrencySymbol(currencyCode: String): String {
        return when (currencyCode) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "INR" -> "₹"
            "JPY" -> "¥"
            "CNY" -> "¥"
            "AUD" -> "A$"
            "CAD" -> "C$"
            "CHF" -> "Fr"
            "KRW" -> "₩"
            "BRL" -> "R$"
            "RUB" -> "₽"
            "ZAR" -> "R"
            "MXN" -> "$"
            "SGD" -> "S$"
            "HKD" -> "HK$"
            "NOK" -> "kr"
            "SEK" -> "kr"
            "DKK" -> "kr"
            "NZD" -> "NZ$"
            "PLN" -> "zł"
            "THB" -> "฿"
            "MYR" -> "RM"
            "IDR" -> "Rp"
            "PHP" -> "₱"
            "CZK" -> "Kč"
            "HUF" -> "Ft"
            "ILS" -> "₪"
            "CLP" -> "$"
            "AED" -> "د.إ"
            "SAR" -> "﷼"
            "ARS" -> "$"
            "EGP" -> "E£"
            "PKR" -> "₨"
            "VND" -> "₫"
            "BDT" -> "৳"
            "NGN" -> "₦"
            "UAH" -> "₴"
            "KES" -> "KSh"
            "QAR" -> "﷼"
            "KWD" -> "د.ك"
            "OMR" -> "﷼"
            "BHD" -> ".د.ب"
            "JOD" -> "د.ا"
            "LKR" -> "₨"
            "MAD" -> "د.م."
            "RON" -> "lei"
            "BGN" -> "лв"
            "HRK" -> "kn"
            "ISK" -> "kr"
            "TRY" -> "₺"
            else -> "$"
        }
    }
}
