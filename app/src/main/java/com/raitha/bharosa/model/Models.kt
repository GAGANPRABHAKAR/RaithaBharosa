package com.raitha.bharosa.model

import java.util.Date

enum class CropType {
    Rice, Sugarcane, Maize, Ragi, Cotton, Wheat
}

enum class Language {
    en, kn
}

data class FarmerProfile(
    val name: String,
    val crop: CropType,
    val landSize: Double,
    val language: Language,
    val location: String = "Karnataka, India"
)

data class SoilData(
    val moisture: Int,
    val nitrogen: Int,
    val phosphorus: Int,
    val potassium: Int,
    val ph: Double
)

data class WeatherData(
    val temp: Double,
    val humidity: Double,
    val rainProbability: Double,
    val forecast: List<ForecastDay>
)

data class ForecastDay(
    val day: String,
    val rainProbability: Double,
    val tempRange: Pair<Int, Int>
)

data class Recommendation(
    val action: String,
    val reason: String,
    val status: String,
    val color: String
)

data class ActivityItem(
    val id: String,
    val date: String,
    val type: String,
    val result: String
)

data class CommunityPost(
    val id: String,
    val author: String,
    val location: String,
    val crop: CropType,
    val content: String,
    val timestamp: String,
    val likes: Int,
    val commentsCount: Int,
    val imageUrl: String? = null
)

data class FertilizerPlan(
    val urea: Double,
    val dap: Double,
    val mop: Double
)

data class SoilHealthReport(
    val score: Int,
    val status: String,
    val deficiencies: List<String>,
    val recommendation: String
)

data class SeasonalAction(
    val season: String,
    val bestCrops: List<String>,
    val period: String,
    val activities: List<String>
)

data class DiagnosisResult(
    val problem: String,
    val solution: String
)

data class MarketPrice(
    val crop: CropType,
    val currentPrice: Int,
    val priceHistory: List<Int>,
    val predictedPrice: Int,
    val trend: String,
    val optimalHarvestDays: Int
)

data class FarmTask(
    val id: String,
    val title: String,
    val day: String,
    val isDone: Boolean = false,
    val priority: String = "medium",
    val type: String = "General" // Irrigation, Fertilizer, Harvest, Weather, etc.
)

data class ResourceUsage(
    val waterLiters: Int,
    val fertilizerKg: Int,
    val pesticideMl: Int
)

data class YieldRecord(
    val season: String,
    val actual: Double,
    val predicted: Double
)

// ── Government Schemes ─────────────────────────────────────
data class GovernmentScheme(
    val id: String,
    val name: String,
    val shortName: String,
    val description: String,
    val eligibility: String,
    val benefits: List<String>,
    val applySteps: List<String>,
    val category: String  // "Subsidy" | "Insurance" | "Support"
)

// ── Farmer Store ───────────────────────────────────────────
data class StoreProduct(
    val id: String,
    val name: String,
    val price: Int,
    val unit: String,
    val description: String,
    val usage: String,
    val category: String  // "Seeds" | "Fertilizers" | "Tools"
)

data class CartItem(
    val product: StoreProduct,
    val quantity: Int
)
