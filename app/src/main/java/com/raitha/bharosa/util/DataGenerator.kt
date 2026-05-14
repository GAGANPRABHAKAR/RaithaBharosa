package com.raitha.bharosa.util

import com.raitha.bharosa.model.*
import java.util.*

object DataGenerator {

    fun generateSoilData(): SoilData {
        return SoilData(
            moisture = (Math.random() * 40).toInt(),
            nitrogen = (Math.random() * 100).toInt(),
            phosphorus = (Math.random() * 100).toInt(),
            potassium = (Math.random() * 100).toInt(),
            ph = 5.5 + Math.random() * 2
        )
    }

    fun generateWeatherData(): WeatherData {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val forecast = days.map { day ->
            ForecastDay(
                day = day,
                rainProbability = Math.random(),
                tempRange = Pair((22 + Math.random() * 5).toInt(), (28 + Math.random() * 7).toInt())
            )
        }

        return WeatherData(
            temp = 26 + Math.random() * 6,
            humidity = 50 + Math.random() * 30,
            rainProbability = Math.random(),
            forecast = forecast
        )
    }

    fun generateCommunityPosts(): List<CommunityPost> {
        return listOf(
            CommunityPost(
                id = "1",
                author = "Ramesh Gowda",
                location = "Mandya",
                crop = CropType.Rice,
                content = "Started transplanting BPT-5204 today. The soil moisture looks perfect after last night's rain. #RiceFarming #Mandya",
                timestamp = "2 hours ago",
                likes = 24,
                commentsCount = 5,
                imageUrl = "https://images.unsplash.com/photo-1530513511303-3d0d86a63584?q=80&w=400&auto=format&fit=crop"
            ),
            CommunityPost(
                id = "2",
                author = "Suresh Kumar",
                location = "Dharwad",
                crop = CropType.Maize,
                content = "The ICAR fertilizer schedule is really helping. Seeing much better growth in my maize fields this year than the last.",
                timestamp = "5 hours ago",
                likes = 18,
                commentsCount = 2
            ),
            CommunityPost(
                id = "3",
                author = "Laxmi Patil",
                location = "Belagavi",
                crop = CropType.Sugarcane,
                content = "Any advice on protecting sugarcane from early shoot borer? Seeing some initial signs in the north corner.",
                timestamp = "Yesterday",
                likes = 12,
                commentsCount = 8
            ),
            CommunityPost(
                id = "4",
                author = "Basavaraj K.",
                location = "Raichur",
                crop = CropType.Cotton,
                content = "Great yield prediction results this season! Looking forward to the harvest in a few months.",
                timestamp = "2 days ago",
                likes = 45,
                commentsCount = 12,
                imageUrl = "https://images.unsplash.com/photo-1594904351111-a072f80b1a71?q=80&w=400&auto=format&fit=crop"
            )
        )
    }

    fun generateHistoryItems(): List<ActivityItem> {
        return listOf(
            ActivityItem("1", "28 Apr", "Sowing", "Successful"),
            ActivityItem("2", "30 Apr", "Irrigation", "2h Done"),
            ActivityItem("3", "01 May", "Fertilizer", "Added N")
        )
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

    fun generateMarketPrices(): List<MarketPrice> {
        return listOf(
            MarketPrice(CropType.Rice, 2150, listOf(1950, 1980, 2010, 2050, 2090, 2120, 2150), 2300, "rising", 12),
            MarketPrice(CropType.Wheat, 2350, listOf(2500, 2480, 2460, 2430, 2400, 2370, 2350), 2200, "falling", 5),
            MarketPrice(CropType.Maize, 1850, listOf(1820, 1830, 1840, 1850, 1855, 1850, 1850), 1900, "stable", 8),
            MarketPrice(CropType.Sugarcane, 3500, listOf(3200, 3260, 3310, 3360, 3410, 3460, 3500), 3700, "rising", 20),
            MarketPrice(CropType.Cotton, 6200, listOf(6500, 6450, 6400, 6350, 6300, 6250, 6200), 6000, "falling", 3),
            MarketPrice(CropType.Ragi, 3700, listOf(3600, 3620, 3640, 3660, 3680, 3690, 3700), 3800, "rising", 15)
        )
    }

    fun generateDefaultTasks(): List<FarmTask> {
        return listOf(
            FarmTask("1", "Apply DAP fertilizer", "Today", false, "high"),
            FarmTask("2", "Irrigation - North field", "Tomorrow", false, "medium"),
            FarmTask("3", "Pest spray inspection", "Wed", false, "medium"),
            FarmTask("4", "Soil moisture check", "Thu", true, "low"),
            FarmTask("5", "Weed removal", "Fri", false, "low")
        )
    }

    fun getResourceUsage(): ResourceUsage {
        return ResourceUsage(
            waterLiters = 12500,
            fertilizerKg = 45,
            pesticideMl = 250
        )
    }

    fun generateYieldHistory(crop: CropType): List<YieldRecord> {
        val base = when (crop) {
            CropType.Rice -> 28.0
            CropType.Wheat -> 32.0
            CropType.Maize -> 35.0
            CropType.Sugarcane -> 400.0
            CropType.Cotton -> 12.0
            CropType.Ragi -> 20.0
        }
        return listOf(
            YieldRecord("Kharif 2024", base * 0.90, base * 0.88),
            YieldRecord("Rabi 2025", base * 0.95, base * 0.92),
            YieldRecord("Kharif 2025", 0.0, base * 1.05)
        )
    }

    // ── Government Schemes ──────────────────────────────────
    fun getGovernmentSchemes(): List<GovernmentScheme> = listOf(
        GovernmentScheme(
            id = "pmkisan",
            name = "PM-KISAN",
            shortName = "PM-KISAN",
            description = "Pradhan Mantri Kisan Samman Nidhi provides income support of ₹6,000 per year " +
                    "to all farmer families across India, payable in three equal installments of ₹2,000.",
            eligibility = "All landholding farmer families with cultivable land, subject to certain exclusions " +
                    "(institutional landholders, constitutional post holders, serving/retired officers, etc.).",
            benefits = listOf(
                "₹6,000 per year direct bank transfer",
                "3 equal installments of ₹2,000 every 4 months",
                "No middlemen — direct to bank account",
                "Covers all small and marginal farmers"
            ),
            applySteps = listOf(
                "Visit nearest Common Service Centre (CSC)",
                "Carry Aadhaar card, land ownership documents & bank passbook",
                "Fill PM-KISAN registration form",
                "Submit documents for verification",
                "Track status at pmkisan.gov.in"
            ),
            category = "Support"
        ),
        GovernmentScheme(
            id = "soilhealth",
            name = "Soil Health Card Scheme",
            shortName = "Soil Health Card",
            description = "The Soil Health Card (SHC) Scheme provides every farmer a soil health card " +
                    "once every 2 years which carries crop-wise recommendations of nutrients and fertilizers " +
                    "required for the individual farm.",
            eligibility = "All farmers who own or operate agricultural land in India are eligible to " +
                    "receive a Soil Health Card free of cost.",
            benefits = listOf(
                "Free soil testing every 2 years",
                "Crop-specific fertilizer recommendations",
                "Reduces input cost by 10–20%",
                "Improves yield through balanced nutrition",
                "Prevents over/under fertilization"
            ),
            applySteps = listOf(
                "Contact nearest Krishi Vigyan Kendra (KVK) or Agriculture office",
                "Request soil testing — officer collects sample from your farm",
                "Wait 15–20 days for laboratory analysis",
                "Receive Soil Health Card with recommendations",
                "Apply fertilizers as per card guidance"
            ),
            category = "Support"
        ),
        GovernmentScheme(
            id = "pmfby",
            name = "Pradhan Mantri Fasal Bima Yojana",
            shortName = "PMFBY",
            description = "PMFBY provides financial support to farmers suffering crop loss/damage due to " +
                    "unforeseen events — natural calamities, pests and diseases. It stabilizes farmer income " +
                    "and ensures continuity of farming.",
            eligibility = "All farmers growing notified crops in notified areas during Kharif and Rabi seasons. " +
                    "Loanee farmers enrolled automatically; non-loanee farmers can opt-in voluntarily.",
            benefits = listOf(
                "Covers natural calamity, pest, disease losses",
                "Farmer premium: 2% (Kharif), 1.5% (Rabi), 5% (Horticulture)",
                "Full sum insured without upper limit",
                "Post-harvest losses also covered for 14 days",
                "Quick claim settlement within 60 days"
            ),
            applySteps = listOf(
                "Visit nearest bank or insurance company before cut-off date",
                "Carry Aadhaar, land records, bank passbook & sowing certificate",
                "Fill PMFBY application form",
                "Pay farmer's share of premium",
                "Receive policy document and track at pmfby.gov.in"
            ),
            category = "Insurance"
        ),
        GovernmentScheme(
            id = "pmkcc",
            name = "Kisan Credit Card",
            shortName = "KCC",
            description = "The Kisan Credit Card scheme provides farmers with affordable credit for their " +
                    "agricultural needs including purchase of seeds, fertilizers, pesticides and other " +
                    "short-term credit requirements.",
            eligibility = "All farmers — individual/joint cultivators, tenant farmers, sharecroppers, " +
                    "self-help groups of farmers. No upper income limit.",
            benefits = listOf(
                "Credit limit up to ₹3 lakh at 4% interest (after subsidy)",
                "Flexible repayment linked to harvest/income",
                "Cover for agriculture, allied activities and post-harvest",
                "ATM card for easy cash withdrawal",
                "Personal accident insurance cover of ₹50,000"
            ),
            applySteps = listOf(
                "Visit any bank branch (nationalized/cooperative/RRB)",
                "Carry land records, Aadhaar, PAN and photograph",
                "Fill KCC application form",
                "Bank assesses limit based on land size and crop",
                "Card issued within 2 weeks of approval"
            ),
            category = "Subsidy"
        ),
        GovernmentScheme(
            id = "pkvy",
            name = "Paramparagat Krishi Vikas Yojana",
            shortName = "PKVY",
            description = "PKVY promotes organic farming in India to improve soil health and reduce " +
                    "dependence on chemical inputs. Farmers get financial support to adopt and certify " +
                    "organic farming practices.",
            eligibility = "Farmers willing to form groups of 50 with minimum 50 acres contiguous land " +
                    "and adopt organic farming practices for 3 years.",
            benefits = listOf(
                "₹50,000 per hectare over 3 years",
                "Support for organic inputs: compost, bio-pesticides, seeds",
                "Certification cost covered by government",
                "Training on organic farming techniques",
                "Market linkage support for organic produce"
            ),
            applySteps = listOf(
                "Form a farmer group of at least 50 members",
                "Identify 50+ acres of contiguous cultivable land",
                "Contact District Agriculture Officer for registration",
                "Submit group application with land documents",
                "Attend training programs organized by Agriculture Department"
            ),
            category = "Subsidy"
        ),
        GovernmentScheme(
            id = "nfsm",
            name = "National Food Security Mission",
            shortName = "NFSM",
            description = "NFSM aims to increase production of rice, wheat, pulses, coarse cereals and " +
                    "commercial crops through area expansion and productivity enhancement in a sustainable manner.",
            eligibility = "Farmers growing rice, wheat, pulses, coarse cereals or commercial crops in " +
                    "identified districts across all states.",
            benefits = listOf(
                "Subsidized high-yielding variety seeds",
                "50% subsidy on micro-nutrients and soil amendments",
                "Support for improved farm equipment",
                "Training and demonstration plots",
                "Cluster demonstrations on farmer fields"
            ),
            applySteps = listOf(
                "Contact local Agriculture Extension Officer",
                "Register as beneficiary at agriculture department office",
                "Carry land records, Aadhaar and bank details",
                "Apply for specific component (seeds/equipment/training)",
                "Subsidy credited directly or via material supply"
            ),
            category = "Subsidy"
        )
    )

    // ── Store Products ──────────────────────────────────────
    fun getStoreProducts(): List<StoreProduct> = listOf(
        // Seeds
        StoreProduct("s1", "BPT-5204 Rice Seeds", 180, "kg",
            "High-yielding Sona Masuri variety. Short duration (125 days). Suitable for Kharif season.",
            "Soak seeds 24 hrs before sowing. Transplant 25-day-old seedlings. Row spacing 20x15 cm.",
            "Seeds"),
        StoreProduct("s2", "Hybrid Maize Seeds (Dekalb)", 320, "250g",
            "Pioneer high-yield maize hybrid. Resistant to leaf blight and turcicum blight.",
            "Sow at 2–3 cm depth. Plant spacing 60x25 cm. Requires 3 irrigations during critical stages.",
            "Seeds"),
        StoreProduct("s3", "Cotton Hybrid Seeds (NHH-44)", 750, "pack",
            "High-yielding cotton hybrid with Bt gene. Bollworm resistant. Suitable for rainfed conditions.",
            "Plant at 90x45 cm spacing. Requires 4 irrigations. No need for pesticide sprays for bollworm.",
            "Seeds"),
        StoreProduct("s4", "Ragi (Finger Millet) Seeds", 95, "kg",
            "Improved variety GPU-28. High in calcium and iron. Drought tolerant.",
            "Broadcast or line sow at 8–10 kg/acre. Requires minimal inputs. Harvest at 90 days.",
            "Seeds"),
        StoreProduct("s5", "Wheat Seeds (HD-2967)", 42, "kg",
            "Semi-dwarf variety with high yield potential. Resistant to rust and karnal bunt.",
            "Sow at 100–125 kg/ha with 22 cm row spacing. Best sown in Nov–Dec (Rabi season).",
            "Seeds"),
        StoreProduct("s6", "Sugarcane Setts (Co-86032)", 28, "kg",
            "High sucrose variety with good ratooning ability. Suitable for Karnataka and Tamil Nadu.",
            "Use 3-budded setts. Plant at 90 cm row spacing in furrows 20–25 cm deep.",
            "Seeds"),
        // Fertilizers
        StoreProduct("f1", "Urea (46% N)", 350, "50 kg bag",
            "Most common nitrogen fertilizer. Contains 46% nitrogen. Fast-acting, excellent for top dressing.",
            "Apply 50–60 kg/acre for most crops. Split into 2 doses — basal and 30 days after sowing.",
            "Fertilizers"),
        StoreProduct("f2", "DAP (Di-Ammonium Phosphate)", 1450, "50 kg bag",
            "18-46-0 fertilizer. Primary source of phosphorus. Ideal for basal application.",
            "Apply 50–60 kg/acre as basal dose at time of sowing/planting. Do not mix with lime.",
            "Fertilizers"),
        StoreProduct("f3", "MOP (Muriate of Potash)", 950, "50 kg bag",
            "0-0-60 fertilizer. Pure potash source. Improves crop quality, disease resistance.",
            "Apply 20–40 kg/acre. Use as basal or split application. Avoid in saline soils.",
            "Fertilizers"),
        StoreProduct("f4", "Neem Coated Urea", 380, "50 kg bag",
            "Urea coated with neem oil. Slow-release nitrogen. Reduces nitrogen losses by 15–20%.",
            "Same rate as regular Urea. Superior to plain Urea — reduces leaching and volatilization.",
            "Fertilizers"),
        StoreProduct("f5", "Vermicompost", 12, "kg",
            "Organic fertilizer from earthworm castings. Rich in NPK and micronutrients. pH neutral.",
            "Apply 1–2 tonnes/acre as basal application. Mix into soil before planting. Safe for all crops.",
            "Fertilizers"),
        StoreProduct("f6", "Micronutrient Mix (Zinc + Boron)", 280, "kg",
            "Combined zinc sulphate and borax mixture. Corrects common micronutrient deficiencies.",
            "Soil application: 10–25 kg/ha. Foliar spray: dissolve 5g in 1 litre water. Apply at flowering.",
            "Fertilizers"),
        // Tools
        StoreProduct("t1", "Hand Sprayer Pump (16L)", 850, "unit",
            "Manual knapsack sprayer. 16-litre capacity. Includes 4 nozzle types. Adjustable pressure.",
            "Fill with water + pesticide mix. Pump handle 5–8 times. Walk at steady pace for even coverage.",
            "Tools"),
        StoreProduct("t2", "Soil Moisture Meter", 450, "unit",
            "Digital probe meter. Measures soil moisture 0–100 scale. No batteries needed. Instant reading.",
            "Insert probe 8–10 cm into soil. Read display in 60 seconds. Clean probe after each use.",
            "Tools"),
        StoreProduct("t3", "Wheel Hoe / Rotary Weeder", 1200, "unit",
            "Manual rotary weeder for inter-row cultivation. Reduces weeding time by 60% vs hand weeding.",
            "Use in moist soil. Push through crop rows to uproot weeds. Best used 15–25 days after sowing.",
            "Tools"),
        StoreProduct("t4", "Digital pH Meter", 680, "unit",
            "Pocket-size soil pH tester. Range 0–14 pH. Accurate to ±0.1. Includes calibration solution.",
            "Calibrate before use. Mix soil with distilled water (1:2 ratio). Insert probe and read value.",
            "Tools"),
        StoreProduct("t5", "Seed Drill (Manual)", 2800, "unit",
            "Manual seed drill for row planting. Adjustable seed rate. Suitable for small-medium farms.",
            "Set seed rate dial per crop. Walk at steady pace. Ensures uniform spacing and depth of 3–5 cm.",
            "Tools"),
        StoreProduct("t6", "Irrigation Drip Kit (1 Acre)", 4500, "set",
            "Complete drip irrigation kit for 1 acre. Includes main line, lateral, drippers and fittings.",
            "Install as per layout guide. Connect to water source. Saves 40–60% water vs flood irrigation.",
            "Tools")
    )
}
