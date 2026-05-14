package com.raitha.bharosa.util

import com.raitha.bharosa.model.*
import java.util.*
import kotlin.math.max
import kotlin.math.roundToInt

object FertilizerCalculator {

    fun getSoilHealth(n: Int, p: Int, k: Int): SoilHealthReport {
        val avg = (n + p + k) / 3.0
        val deficiencies = mutableListOf<String>()
        
        if (n < 33) deficiencies.add("Nitrogen (N)")
        if (p < 33) deficiencies.add("Phosphorus (P)")
        if (k < 33) deficiencies.add("Potassium (K)")

        val status = when {
            avg < 33 -> "Low"
            avg > 66 -> "High"
            else -> "Medium"
        }

        val recommendation = if (deficiencies.isNotEmpty()) {
            "Nitrogen is low → Apply Urea (top dressing). Phosphorous/Potash deficiency → Use DAP/MOP."
        } else {
            "Soil health is optimal. Maintain organic matter with compost."
        }

        return SoilHealthReport(
            score = avg.roundToInt(),
            status = status,
            deficiencies = deficiencies,
            recommendation = recommendation
        )
    }

    fun getSeasonList(): List<SeasonalAction> {
        return listOf(
            SeasonalAction(
                season = "Kharif",
                bestCrops = listOf("Rice (BPT-5204)", "Maize (HQPM-1)", "Cotton (BT)", "Soybean (JS-335)"),
                period = "June – October (Monsoon)",
                activities = listOf("Sowing (June/July)", "Transplanting Rice", "Weeding", "Urea top-dressing")
            ),
            SeasonalAction(
                season = "Rabi",
                bestCrops = listOf("Wheat (PBW-343)", "Mustard (Pusa Bold)", "Gram (JG-11)", "Peas (Arkel)"),
                period = "November – February (Winter)",
                activities = listOf("Land preparation", "Sowing (Nov)", "Irrigation management", "Pest scouting")
            ),
            SeasonalAction(
                season = "Zaid",
                bestCrops = listOf("Watermelon", "Cucumber", "Pumpkin", "Moong Dal (Pusa-9531)", "Vegetables"),
                period = "March – May (Summer)",
                activities = listOf("Summer ploughing", "Sowing (Mar)", "Frequent irrigation", "Mulching")
            )
        )
    }

    fun getSeasonInfo(selectedSeason: String? = null): SeasonalAction {
        val seasons = getSeasonList()
        if (selectedSeason != null) {
            return seasons.find { it.season == selectedSeason } ?: seasons[0]
        }

        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        return when (month) {
            in 6..10 -> seasons[0]
            in 11..12, in 1..2 -> seasons[1]
            else -> seasons[2]
        }
    }

    fun getRecommendedNPK(crop: CropType): Triple<Int, Int, Int> {
        return when (crop) {
            CropType.Rice -> Triple(40, 20, 20)
            CropType.Sugarcane -> Triple(100, 40, 40)
            CropType.Maize -> Triple(50, 25, 25)
            CropType.Ragi -> Triple(30, 15, 15)
            CropType.Cotton -> Triple(40, 20, 20)
            CropType.Wheat -> Triple(50, 25, 25)
        }
    }

    fun calculateFertilizer(area: Double, targetN: Double, targetP: Double, targetK: Double): FertilizerPlan {
        val dapNeeded = (targetP * area) / 0.46
        val nFromDAP = dapNeeded * 0.18
        val remainingN = (targetN * area) - nFromDAP
        val ureaNeeded = max(0.0, remainingN / 0.46)
        val mopNeeded = (targetK * area) / 0.60

        return FertilizerPlan(
            urea = (ureaNeeded * 10).roundToInt() / 10.0,
            dap = (dapNeeded * 10).roundToInt() / 10.0,
            mop = (mopNeeded * 10).roundToInt() / 10.0
        )
    }
}
