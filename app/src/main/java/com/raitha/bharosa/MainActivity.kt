package com.raitha.bharosa

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*
import androidx.compose.material3.CenterAlignedTopAppBar

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.raitha.bharosa.model.*
import com.raitha.bharosa.util.*
import java.util.*
import kotlin.math.roundToInt

// Theme Colors
val HarvestGold = Color(0xFFDAA520)
val EarthBrown = Color(0xFF5A4033)
val SeedGreen = Color(0xFF4F7942)
val SkyBlue = Color(0xFF87CEEB)
val ClayRed = Color(0xFFB22222)
val WarmWhite = Color(0xFFFDF5E6)

class MainActivity : ComponentActivity() {
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize TTS
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
            }
        }
        
        // Request permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 100)
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
        }
        
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = SeedGreen,
                    secondary = HarvestGold,
                    tertiary = SkyBlue,
                    surface = WarmWhite,
                    background = WarmWhite,
                    onPrimary = Color.White,
                    onSecondary = Color.White,
                    onSurface = EarthBrown,
                    onBackground = EarthBrown
                )
            ) {
                RaithaBharosaApp()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer?.destroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RaithaBharosaApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("raitha_prefs", Context.MODE_PRIVATE) }
    
    // Load saved profile
    var profile by remember {
        mutableStateOf<FarmerProfile?>(null).apply {
            val savedName = prefs.getString("profile_name", null)
            val savedCrop = prefs.getString("profile_crop", null)
            val savedLandSize = prefs.getFloat("profile_land_size", 0f)
            val savedLanguage = prefs.getString("profile_language", "en")
            
            if (savedName != null && savedCrop != null) {
                value = FarmerProfile(
                    name = savedName,
                    crop = CropType.valueOf(savedCrop),
                    landSize = savedLandSize.toDouble(),
                    language = Language.valueOf(savedLanguage ?: "en")
                )
            }
        }
    }
    
    var currentScreen by remember { mutableStateOf("dashboard") }
    var language by remember { mutableStateOf(profile?.language ?: Language.en) }
    var showVoiceAssistant by remember { mutableStateOf(false) }
    
    val t = remember(language) { Translations.get(language) }
    
    // Update language when profile changes
    LaunchedEffect(profile) {
        profile?.let { language = it.language }
    }
    
    fun saveProfile(newProfile: FarmerProfile) {
        profile = newProfile
        prefs.edit().apply {
            putString("profile_name", newProfile.name)
            putString("profile_crop", newProfile.crop.name)
            putFloat("profile_land_size", newProfile.landSize.toFloat())
            putString("profile_language", newProfile.language.name)
            apply()
        }
    }
    
    if (profile == null) {
        OnboardingScreen(
            onComplete = { saveProfile(it) },
            language = language,
            onLanguageChange = { language = it },
            translations = t
        )
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column {
                            Text(
                                t["appTitle"] ?: "Raitha-Bharosa Hub",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = EarthBrown
                            )
                            LocationText(language)
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { currentScreen = "profile" },
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = EarthBrown)
                        }
                        Button(
                            onClick = { 
                                language = if (language == Language.en) Language.kn else Language.en
                                profile?.let { 
                                    val updated = it.copy(language = language)
                                    saveProfile(updated)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HarvestGold),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                if (language == Language.en) "KN" else "EN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = WarmWhite)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showVoiceAssistant = true },
                    containerColor = SeedGreen,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Assistant")
                }
            },
            bottomBar = {
                NavigationBar(containerColor = EarthBrown) {
                    val tabs = listOf(
                        Triple("dashboard", Icons.Default.Home, t["home"] ?: "Home"),
                        Triple("calendar", Icons.Default.DateRange, t["calendar"] ?: "Calendar"),
                        Triple("ai", Icons.Default.Psychology, t["advisor"] ?: "Advisor"),
                        Triple("community", Icons.Default.People, t["feed"] ?: "Feed"),
                        Triple("profile", Icons.Default.Person, t["profile"] ?: "Profile")
                    )
                    tabs.forEach { (route, icon, label) ->
                        NavigationBarItem(
                            selected = currentScreen == route,
                            onClick = { currentScreen = route },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Color.White.copy(alpha = 0.4f),
                                unselectedTextColor = Color.White.copy(alpha = 0.4f),
                                indicatorColor = SeedGreen.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(WarmWhite)
            ) {
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                    },
                    label = "screen_transition"
                ) { screen ->
                    when (screen) {
                        "dashboard" -> DashboardScreen(profile!!, language, t, onNavigate = { currentScreen = it })
                        "input" -> InputCenterScreen(profile!!, language, t, onProfileUpdate = { saveProfile(it) }, onBack = { currentScreen = "profile" })
                        "calendar" -> FarmingCalendarScreen(language, t)
                        "history" -> HistoryScreen(language, t)
                        "ai" -> AdvisorScreen(profile!!, language, t)
                        "calc" -> CalculatorsScreen(profile!!, language, t)
                        "community" -> CommunityScreen(language, t)
                        "market" -> MarketPredictionsScreen(profile!!, language, t, onBack = { currentScreen = "dashboard" })
                        "schemes" -> GovtSchemesScreen(language, t, onBack = { currentScreen = "dashboard" })
                        "store" -> FarmStoreScreen(language, t, onBack = { currentScreen = "dashboard" })
                        "reports" -> FarmReportsScreen(profile!!, language, t, onBack = { currentScreen = "dashboard" })
                        "profile" -> FarmerProfileScreen(profile!!, language, t, onEditProfile = { currentScreen = "input" }, onLogout = { profile = null })
                        else -> DashboardScreen(profile!!, language, t, onNavigate = { currentScreen = it })
                    }
                }

                if (showVoiceAssistant) {
                    VoiceAssistantDialog(
                        onDismiss = { showVoiceAssistant = false },
                        language = language,
                        translations = t,
                        profile = profile!!
                    )
                }
            }
        }
    }
}

@Composable
fun LocationText(language: Language) {
    val context = LocalContext.current
    var locationText by remember { mutableStateOf("Karnataka, India") }
    
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                location?.let {
                    locationText = "${String.format("%.2f", it.latitude)}, ${String.format("%.2f", it.longitude)}"
                }
            } catch (e: Exception) {
                locationText = if (language == Language.en) "Karnataka, India" else "ಕರ್ನಾಟಕ, ಭಾರತ"
            }
        }
    }
    
    Text(
        locationText,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = EarthBrown.copy(alpha = 0.4f)
    )
}


@Composable
fun OnboardingScreen(
    onComplete: (FarmerProfile) -> Unit,
    language: Language,
    onLanguageChange: (Language) -> Unit,
    translations: Map<String, String>
) {
    var name by remember { mutableStateOf("") }
    var selectedCrop by remember { mutableStateOf<CropType?>(null) }
    var landSize by remember { mutableStateOf("1") }
    var selectedLanguage by remember { mutableStateOf(language) }
    val t = Translations.get(selectedLanguage)
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Icon(
            Icons.Default.Spa,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = SeedGreen
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            t["appTitle"] ?: "Raitha-Bharosa Hub",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = EarthBrown
        )
        
        Text(
            t["smartAssistant"] ?: "Smart Farming Assistant",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = EarthBrown.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            t["welcome"] ?: "Welcome, Farmer!",
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = EarthBrown
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            t["setupProfile"] ?: "Setup Your Profile",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = EarthBrown.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Name Input
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(t["name"] ?: "Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SeedGreen,
                unfocusedBorderColor = EarthBrown.copy(alpha = 0.2f)
            )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Language Selection
        Text(
            t["selectLanguage"] ?: "Select Language",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = EarthBrown.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Language.values().forEach { lang ->
                val isSelected = selectedLanguage == lang
                Button(
                    onClick = { 
                        selectedLanguage = lang
                        onLanguageChange(lang)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) HarvestGold else EarthBrown.copy(alpha = 0.1f),
                        contentColor = if (isSelected) Color.White else EarthBrown
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        if (lang == Language.en) "English" else "Kannada",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Crop Selection
        Text(
            t["selectCrop"] ?: "Select Your Crop",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = EarthBrown.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

            val crops = CropType.values().toList()

            for (i in crops.indices step 2) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    val crop1 = crops[i]
                    val isSelected1 = selectedCrop == crop1

                    Button(
                        onClick = { selectedCrop = crop1 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected1) SeedGreen else EarthBrown.copy(alpha = 0.1f),
                            contentColor = if (isSelected1) Color.White else EarthBrown
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(crop1.name, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }

                    if (i + 1 < crops.size) {
                        val crop2 = crops[i + 1]
                        val isSelected2 = selectedCrop == crop2

                        Button(
                            onClick = { selectedCrop = crop2 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected2) SeedGreen else EarthBrown.copy(alpha = 0.1f),
                                contentColor = if (isSelected2) Color.White else EarthBrown
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(crop2.name, fontSize = 12.sp, fontWeight = FontWeight.Black)
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Land Size
        OutlinedTextField(
            value = landSize,
            onValueChange = { landSize = it },
            label = { Text(t["landArea"] ?: "Land Area (Acres)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SeedGreen,
                unfocusedBorderColor = EarthBrown.copy(alpha = 0.2f)
            )
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = {
                if (name.isNotBlank() && selectedCrop != null) {
                    onComplete(
                        FarmerProfile(
                            name = name,
                            crop = selectedCrop!!,
                            landSize = landSize.toDoubleOrNull() ?: 1.0,
                            language = selectedLanguage
                        )
                    )
                }
            },
            enabled = name.isNotBlank() && selectedCrop != null,
            colors = ButtonDefaults.buttonColors(containerColor = SeedGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                t["getStarted"] ?: "Get Started",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun DashboardScreen(
    profile: FarmerProfile,
    language: Language,
    t: Map<String, String>,
    onNavigate: (String) -> Unit = {}
) {
    var soil by remember { mutableStateOf(DataGenerator.generateSoilData()) }
    var weather by remember { mutableStateOf(DataGenerator.generateWeatherData()) }
    
    // Update weather periodically
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(10000)
            weather = weather.copy(
                temp = weather.temp + (Math.random() - 0.5) * 0.5,
                humidity = (weather.humidity + (Math.random() - 0.5) * 2).coerceIn(0.0, 100.0)
            )
        }
    }
    
    val sowingIndex = remember(soil, weather) { DecisionEngine.getSowingIndex(soil, weather) }
    val recommendations = remember(profile, soil, weather) { DecisionEngine.getRecommendations(profile.crop, soil, weather) }

    var tasks by remember { mutableStateOf(DataGenerator.generateDefaultTasks()) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskName by remember { mutableStateOf("") }
    var newTaskDay by remember { mutableStateOf("Today") }
    val resourceUsage = remember { DataGenerator.getResourceUsage() }
    val yieldHistory = remember(profile.crop) { DataGenerator.generateYieldHistory(profile.crop) }
    val marketPreview = remember { DataGenerator.generateMarketPrices() }

    if (showAddTaskDialog) {
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text(t["addTask"] ?: "Add Task", fontWeight = FontWeight.Black, color = EarthBrown) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newTaskName,
                        onValueChange = { newTaskName = it },
                        label = { Text(t["taskName"] ?: "Task Name") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SeedGreen,
                            unfocusedBorderColor = EarthBrown.copy(alpha = 0.2f)
                        )
                    )
                    Text(
                        t["selectDay"] ?: "Select Day",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EarthBrown.copy(alpha = 0.6f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Today", "Tomorrow", "Wed", "Thu", "Fri").forEach { day ->
                            val isDaySelected = newTaskDay == day
                            Button(
                                onClick = { newTaskDay = day },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDaySelected) SeedGreen else EarthBrown.copy(alpha = 0.1f),
                                    contentColor = if (isDaySelected) Color.White else EarthBrown
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(day, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTaskName.isNotBlank()) {
                            tasks = tasks + FarmTask(
                                id = System.currentTimeMillis().toString(),
                                title = newTaskName,
                                day = newTaskDay
                            )
                            newTaskName = ""
                            newTaskDay = "Today"
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SeedGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(t["create"] ?: "Create", fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showAddTaskDialog = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(t["cancel"] ?: "Cancel", fontWeight = FontWeight.Black)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Sowing Index Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SeedGreen)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    t["sowingIndex"] ?: "Sowing Index",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.7f)
                )
                
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        "$sowingIndex%",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            when {
                                sowingIndex > 70 -> t["optimalSowing"] ?: "Optimal for sowing"
                                sowingIndex > 40 -> t["fair"] ?: "Fair"
                                else -> t["tooWet"] ?: "Too wet to sow"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Smart Alerts
        Text(
            t["smartAlerts"] ?: "Smart Alerts",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = EarthBrown.copy(alpha = 0.4f),
            modifier = Modifier.padding(start = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        recommendations.forEach { rec ->
            val bgColor = when (rec.color) {
                "green" -> SeedGreen
                "red" -> ClayRed
                else -> HarvestGold
            }
            val icon = when (rec.status) {
                "Go" -> Icons.Default.CheckCircle
                "Wait" -> Icons.Default.Cancel
                else -> Icons.Default.Warning
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(rec.action, fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        Text(rec.reason, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick Stats Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                icon = Icons.Default.WaterDrop,
                label = t["soilMoisture"] ?: "Soil Moisture",
                value = "${soil.moisture}%",
                iconColor = SkyBlue,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Default.Thermostat,
                label = t["temperature"] ?: "Temperature",
                value = "${weather.temp.roundToInt()}°C",
                iconColor = ClayRed,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Weather Forecast
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    t["rainForecast"] ?: "Rain Forecast",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = EarthBrown.copy(alpha = 0.4f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    weather.forecast.take(5).forEach { day ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                day.day,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = EarthBrown.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (day.rainProbability > 0.5) SkyBlue.copy(alpha = 0.1f) else EarthBrown.copy(alpha = 0.05f),
                                        RoundedCornerShape(12.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = if (day.rainProbability > 0.5) SkyBlue else EarthBrown.copy(alpha = 0.2f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "${(day.rainProbability * 100).roundToInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = EarthBrown
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // ── Task Management Section ──────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.List, contentDescription = null, tint = SeedGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            t["taskManagement"] ?: "Task Management",
                            fontWeight = FontWeight.Bold,
                            color = EarthBrown
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(SeedGreen, RoundedCornerShape(8.dp))
                            .clickable { onNavigate("calendar") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.ArrowForward,
                            contentDescription = "View Calendar",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val pendingCount = tasks.count { !it.isDone }
                val doneCount = tasks.count { it.isDone }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(HarvestGold.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("$pendingCount", fontSize = 20.sp, fontWeight = FontWeight.Black, color = HarvestGold)
                            Text(
                                t["pendingTasks"] ?: "Pending",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EarthBrown.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(SeedGreen.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text("$doneCount", fontSize = 20.sp, fontWeight = FontWeight.Black, color = SeedGreen)
                            Text(
                                t["completedTasks"] ?: "Completed",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EarthBrown.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                tasks.take(3).forEach { task ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .background(
                                    if (task.isDone) SeedGreen else Color.Transparent,
                                    CircleShape
                                )
                                .border(
                                    2.dp,
                                    if (task.isDone) SeedGreen else EarthBrown.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (task.isDone) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            task.title,
                            fontSize = 13.sp,
                            fontWeight = if (task.isDone) FontWeight.Normal else FontWeight.Bold,
                            color = if (task.isDone) EarthBrown.copy(alpha = 0.3f) else EarthBrown,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // ── Resource Usage Section ───────────────────────────────
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WaterDrop, contentDescription = null, tint = SkyBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        t["resourceUsage"] ?: "Resource Usage",
                        fontWeight = FontWeight.Bold,
                        color = EarthBrown
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResourceChip(
                        label = t["waterUsed"] ?: "Water",
                        value = "${String.format("%,d", resourceUsage.waterLiters)} L",
                        subtitle = t["thisWeek"] ?: "This Week",
                        color = SkyBlue,
                        modifier = Modifier.weight(1f)
                    )
                    ResourceChip(
                        label = t["fertilizerUsed"] ?: "Fertilizer",
                        value = "${resourceUsage.fertilizerKg} kg",
                        subtitle = t["thisMonth"] ?: "This Month",
                        color = SeedGreen,
                        modifier = Modifier.weight(1f)
                    )
                    ResourceChip(
                        label = t["pesticideUsed"] ?: "Pesticide",
                        value = "${resourceUsage.pesticideMl} ml",
                        subtitle = t["thisMonth"] ?: "This Month",
                        color = ClayRed,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // ── Yield Forecast Section ───────────────────────────────
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = SeedGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        t["yieldForecast"] ?: "Yield Forecast",
                        fontWeight = FontWeight.Bold,
                        color = EarthBrown
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                yieldHistory.forEachIndexed { index, record ->
                    val isCurrentSeason = record.actual == 0.0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                record.season,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EarthBrown
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    t["predicted"] ?: "Predicted",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = EarthBrown.copy(alpha = 0.4f)
                                )
                                Text(
                                    "${String.format("%.1f", record.predicted)} q/ac",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = HarvestGold
                                )
                            }
                        }
                    }
                    if (index < yieldHistory.size - 1) {
                        HorizontalDivider(color = EarthBrown.copy(alpha = 0.05f))
                    }
                }
            }
        }

        // ── Quick Access: Schemes / Store / Reports ──────────────
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            if (language == Language.en) "More Features" else "ಇನ್ನಷ್ಟು ವೈಶಿಷ್ಟ್ಯಗಳು",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = EarthBrown.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickFeatureCard(t["govtSchemes"] ?: "Schemes", Icons.Default.AccountBalance, SkyBlue, modifier = Modifier.weight(1f), onClick = { onNavigate("schemes") })
            QuickFeatureCard(t["store"] ?: "Store", Icons.Default.ShoppingCart, SeedGreen, modifier = Modifier.weight(1f), onClick = { onNavigate("store") })
            QuickFeatureCard(t["calculators"] ?: "Calcs", Icons.Default.Calculate, HarvestGold, modifier = Modifier.weight(1f), onClick = { onNavigate("calc") })
            QuickFeatureCard(t["reports"] ?: "Reports", Icons.Default.Article, ClayRed, modifier = Modifier.weight(1f), onClick = { onNavigate("reports") })
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun QuickFeatureCard(label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = EarthBrown,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun StatCard(icon: ImageVector, label: String, value: String, iconColor: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = EarthBrown.copy(alpha = 0.5f)
            )
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = EarthBrown
            )
        }
    }
}


@Composable
fun InputCenterScreen(
    profile: FarmerProfile,
    language: Language,
    t: Map<String, String>,
    onProfileUpdate: (FarmerProfile) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var selectedCrop by remember { mutableStateOf(profile.crop) }
    var landSize by remember { mutableStateOf(profile.landSize.toString()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = EarthBrown)
            }
            Text(
                t["editProfile"] ?: "Edit Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = EarthBrown
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text(t["name"] ?: "Name") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = landSize,
            onValueChange = { landSize = it },
            label = { Text(t["landArea"] ?: "Land Area (Acres)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                onProfileUpdate(profile.copy(
                    name = name,
                    landSize = landSize.toDoubleOrNull() ?: profile.landSize
                ))
                onBack()
            },
            colors = ButtonDefaults.buttonColors(containerColor = SeedGreen),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(t["save"] ?: "Save Profile", fontWeight = FontWeight.Black, modifier = Modifier.padding(8.dp))
        }
    }
}

@Composable
fun SliderInput(label: String, value: Int, range: IntRange, color: Color, onValueChange: (Int) -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EarthBrown.copy(alpha = 0.6f))
            Text("$value%", fontSize = 14.sp, fontWeight = FontWeight.Black, color = EarthBrown)
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = range.first.toFloat()..range.last.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
fun AdvisorScreen(profile: FarmerProfile, language: Language, t: Map<String, String>) {
    var leafColor by remember { mutableStateOf("green") }
    var hasSpots by remember { mutableStateOf(false) }
    var hasCurling by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var diagnosisResult by remember { mutableStateOf<DiagnosisResult?>(null) }
    
    fun runDiagnosis() {
        isAnalyzing = true
        diagnosisResult = null
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val result = when {
                hasCurling -> DiagnosisResult("Pest Attack (Aphids/Thrips)", "Spray Neem oil (5ml/L).")
                leafColor == "yellow" && !hasSpots -> DiagnosisResult("Nitrogen Deficiency", "Apply Urea (15-20kg/acre).")
                leafColor == "brown" && hasSpots -> DiagnosisResult("Fungal Infection", "Apply Fungicide (Mancozeb 2g/L).")
                else -> DiagnosisResult("Healthy Plant", "Maintain current care schedule.")
            }
            diagnosisResult = result
            isAnalyzing = false
        }, 1500)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(t["diagnosis"] ?: "Diagnosis", fontSize = 24.sp, fontWeight = FontWeight.Black, color = EarthBrown)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(t["leafColor"] ?: "Leaf Color", fontSize = 12.sp, fontWeight = FontWeight.Black, color = EarthBrown.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("green", "yellow", "brown").forEach { color ->
                        val isSelected = leafColor == color
                        FilterChip(
                            selected = isSelected,
                            onClick = { leafColor = color },
                            label = { Text(color.replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(t["spots"] ?: "Spots?", fontSize = 12.sp, fontWeight = FontWeight.Black, color = EarthBrown.copy(alpha = 0.4f))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(true, false).forEach { value ->
                        FilterChip(
                            selected = hasSpots == value,
                            onClick = { hasSpots = value },
                            label = { Text(if (value) "Yes" else "No") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = { runDiagnosis() },
                    enabled = !isAnalyzing,
                    colors = ButtonDefaults.buttonColors(containerColor = SeedGreen),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isAnalyzing) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                    else Text(t["diagnoseNow"] ?: "Diagnose Now", fontWeight = FontWeight.Black)
                }
            }
        }
        
        AnimatedVisibility(visible = diagnosisResult != null) {
            diagnosisResult?.let { result ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SeedGreen.copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(result.problem, fontSize = 18.sp, fontWeight = FontWeight.Black, color = ClayRed)
                        Text(result.solution, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = EarthBrown)
                    }
                }
            }
        }
    }
}


@Composable
fun HistoryScreen(language: Language, t: Map<String, String>) {
    val historyItems = remember { DataGenerator.generateHistoryItems() }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(t["history"] ?: "History", fontSize = 24.sp, fontWeight = FontWeight.Black, color = EarthBrown)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        historyItems.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(item.date, fontSize = 10.sp, fontWeight = FontWeight.Black, color = EarthBrown.copy(alpha = 0.3f))
                        Text(item.type, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EarthBrown)
                    }
                    Box(modifier = Modifier.background(SeedGreen.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text(item.result, fontSize = 10.sp, fontWeight = FontWeight.Black, color = SeedGreen)
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorsScreen(profile: FarmerProfile, language: Language, t: Map<String, String>) {
    var landArea by remember { mutableStateOf(profile.landSize.toFloat()) }
    var seedRate by remember { mutableStateOf(10f) }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())
    ) {
        Text(t["calculators"] ?: "Calculators", fontSize = 24.sp, fontWeight = FontWeight.Black, color = EarthBrown)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        CalculationCard(t["seedCalc"] ?: "Seed Calculator", Icons.Default.Spa, SeedGreen) {
            SliderInput(t["landArea"] ?: "Land Area", landArea.toInt(), 1..100, SeedGreen) { landArea = it.toFloat() }
            SliderInput(t["seedRate"] ?: "Seed Rate", seedRate.toInt(), 1..1000, SeedGreen) { seedRate = it.toFloat() }
            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = EarthBrown.copy(alpha = 0.05f))
            Text("${t["totalSeeds"]}: ${(landArea * seedRate).roundToInt()} kg", fontWeight = FontWeight.Black, fontSize = 18.sp, color = SeedGreen)
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        CalculationCard(t["waterCalc"] ?: "Water Calculator", Icons.Default.WaterDrop, SkyBlue) {
            Text("${t["waterTotal"]}: ${String.format("%,d", (landArea * 25 * 4046.86).roundToInt())} L/day", fontWeight = FontWeight.Black, fontSize = 16.sp, color = SkyBlue)
        }
    }
}


@Composable
fun CommunityScreen(language: Language, t: Map<String, String>) {
    val posts = remember { DataGenerator.generateCommunityPosts() }
    
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(t["community"] ?: "Community", fontSize = 24.sp, fontWeight = FontWeight.Black, color = EarthBrown)
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(posts) { post ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(40.dp).background(EarthBrown.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(post.author[0].toString(), fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(post.author, fontWeight = FontWeight.Black, fontSize = 14.sp)
                                Text("${post.location} • ${post.timestamp}", fontSize = 10.sp, color = EarthBrown.copy(alpha = 0.4f))
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(post.content, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CalculationCard(title: String, icon: ImageVector, color: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color)
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, color = EarthBrown)
            }
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun ResourceChip(label: String, value: String, subtitle: String, color: Color, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(color.copy(alpha = 0.08f), RoundedCornerShape(12.dp)).padding(10.dp)) {
        Column {
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.Black, color = color)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = EarthBrown)
            Text(subtitle, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = EarthBrown.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun MarketPredictionsScreen(profile: FarmerProfile, language: Language, t: Map<String, String>, onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
        Text(t["marketPredictions"] ?: "Market Predictions", fontSize = 24.sp, fontWeight = FontWeight.Black)
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = HarvestGold)) {
            Text("Price Trends for ${profile.crop}", modifier = Modifier.padding(20.dp), color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun VoiceAssistantDialog(onDismiss: () -> Unit, language: Language, translations: Map<String, String>, profile: FarmerProfile) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(translations["voiceAssistant"] ?: "Voice Assistant") },
        text = { Text(translations["askVoice"] ?: "How can I help you today?") },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
