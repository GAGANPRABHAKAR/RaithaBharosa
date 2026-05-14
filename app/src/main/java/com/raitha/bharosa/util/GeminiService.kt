package com.raitha.bharosa.util

import com.raitha.bharosa.model.SoilData
import com.raitha.bharosa.model.WeatherData

object GeminiService {

    fun analyzeCropDisease(imageData: String, crop: String): String {
        return """**Simulated Analysis for $crop**: The plant appears to have symptoms of **Blast Disease**.

### Symptoms:
- Spindle-shaped spots on leaves with gray centers.
- Quick spread during high humidity.

### Treatment:
- **Organic**: Spray 5% Neem Seed Kernel Extract (NSKE).
- **Cultural**: Avoid excess Nitrogen fertilizer.
- **Biocontrol**: Apply Pseudomonas fluorescens 10g/liter of water."""
    }

    fun getVoiceAdvice(query: String, soilData: SoilData, weatherData: WeatherData): String {
        val q = query.lowercase()
        val sowingIndex = DecisionEngine.getSowingIndex(soilData, weatherData)
        
        return when {
            q.contains("sow") || q.contains("planting") -> {
                when {
                    sowingIndex > 75 -> "Conditions are excellent for sowing today. Soil moisture is at ${soilData.moisture}% and the weather is stable."
                    weatherData.rainProbability > 0.4 -> "I recommend waiting to sow. There is a ${(weatherData.rainProbability * 100).toInt()}% chance of rain which could wash away seeds."
                    soilData.moisture < 15 -> "The soil is too dry for sowing. Please irrigate first to reach optimal moisture levels."
                    else -> "Sowing conditions are fair (Index: $sowingIndex%). You can proceed, but monitor the weather closely."
                }
            }
            q.contains("water") || q.contains("irrigate") || q.contains("moisture") -> {
                if (soilData.moisture < 20) {
                    "Your soil moisture is low at ${soilData.moisture}%. You should irrigate your crops soon."
                } else if (weatherData.rainProbability > 0.6) {
                    "No need to water today. Heavy rain is expected, which will provide natural irrigation."
                } else {
                    "Soil moisture is healthy at ${soilData.moisture}%. No immediate irrigation is required."
                }
            }
            q.contains("fertilizer") || q.contains("nutrient") || q.contains("npk") -> {
                val lowNutrients = mutableListOf<String>()
                if (soilData.nitrogen < 30) lowNutrients.add("Nitrogen")
                if (soilData.phosphorus < 20) lowNutrients.add("Phosphorus")
                if (soilData.potassium < 25) lowNutrients.add("Potassium")
                
                if (lowNutrients.isNotEmpty()) {
                    "Your soil is low in ${lowNutrients.joinToString(" and ")}. I suggest applying a balanced fertilizer soon."
                } else {
                    "Your soil nutrient levels (NPK) are currently optimal for your crop."
                }
            }
            q.contains("weather") || q.contains("rain") || q.contains("temp") -> {
                "Today's temperature is ${weatherData.temp.toInt()}°C with ${(weatherData.rainProbability * 100).toInt()}% chance of rain. Humidity is ${weatherData.humidity.toInt()}%."
            }
            else -> "I can help with sowing advice, irrigation needs, or soil health. What would you like to know about your farm today?"
        }
    }
}
