package com.raitha.bharosa.util

import com.raitha.bharosa.model.*

object DecisionEngine {

    fun getSowingIndex(soil: SoilData, weather: WeatherData): Int {
        var index = 100

        // Moisture logic
        if (soil.moisture > 30) index -= (soil.moisture - 30) * 2
        if (soil.moisture < 15) index -= (15 - soil.moisture) * 3

        // Rain logic
        if (weather.rainProbability > 0.5) index -= 40

        // Temp logic (Optimal 20-30C)
        if (weather.temp < 18 || weather.temp > 35) index -= 30

        return index.coerceIn(0, 100)
    }

    fun getRecommendations(crop: CropType, soil: SoilData, weather: WeatherData): List<Recommendation> {
        val recs = mutableListOf<Recommendation>()

        // Sowing
        val sowingIndex = getSowingIndex(soil, weather)
        if (sowingIndex > 75) {
            recs.add(Recommendation(
                action = "Sow Today",
                reason = "Optimal moisture and weather conditions.",
                status = "Go",
                color = "green"
            ))
        } else if (weather.rainProbability > 0.4) {
            recs.add(Recommendation(
                action = "Delay Sowing",
                reason = "Rain expected within 24 hours.",
                status = "Wait",
                color = "red"
            ))
        }

        // Irrigation
        if (soil.moisture < 15) {
            recs.add(Recommendation(
                action = "Irrigate Now",
                reason = "Soil moisture is critically low.",
                status = "Action",
                color = "yellow"
            ))
        }

        // Fertilization
        if (soil.nitrogen < 20 || soil.phosphorus < 20 || soil.potassium < 20) {
            recs.add(Recommendation(
                action = "Apply Fertilizer",
                reason = "Nutrient imbalance detected (Low NPK).",
                status = "Action",
                color = "yellow"
            ))
        }

        return recs
    }
}
