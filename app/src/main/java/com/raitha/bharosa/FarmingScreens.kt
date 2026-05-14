package com.raitha.bharosa

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raitha.bharosa.model.*
import com.raitha.bharosa.util.*
import java.util.*

// ════════════════════════════════════════════════════════════
//  CALENDAR / TASK MANAGEMENT SCREEN
// ════════════════════════════════════════════════════════════

@Composable
fun FarmingCalendarScreen(
    language: Language,
    t: Map<String, String>
) {
    var tasks by remember { mutableStateOf(DataGenerator.generateDefaultTasks()) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    val weather = remember { DataGenerator.generateWeatherData() }
    
    // Add specific task types for reminders
    val reminders = remember {
        listOf(
            FarmTask("r1", t["irrigationReminder"] ?: "Irrigation Reminder", "Today", false, "high", "Irrigation"),
            FarmTask("r2", t["fertilizerReminder"] ?: "Fertilizer Reminder", "Tomorrow", false, "medium", "Fertilizer"),
            FarmTask("r3", t["harvestPlanning"] ?: "Harvest Planning", "Next Week", false, "low", "Harvest")
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmWhite)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                t["calendar"] ?: "Calendar",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = EarthBrown
            )
            Text(
                t["dailySchedule"] ?: "Daily Farming Schedule",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = EarthBrown.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Weather Reminder Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SeedGreen.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(SeedGreen, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            t["weatherAlert"] ?: "Weather Alert",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = SeedGreen
                        )
                        Text(
                            if (weather.rainProbability > 0.6) "High chance of rain today. Adjust irrigation." 
                            else "Clear skies. Good day for fertilizer application.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = EarthBrown
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Date Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val calendar = Calendar.getInstance()
                for (i in 0..4) {
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    val dayName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)
                    val isToday = i == 0
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isToday) SeedGreen else Color.White)
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            dayName ?: "",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isToday) Color.White else EarthBrown.copy(alpha = 0.4f)
                        )
                        Text(
                            day.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isToday) Color.White else EarthBrown
                        )
                    }
                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Reminders Section
            Text(
                t["reminders"] ?: "Reminders",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = EarthBrown
            )
            Spacer(modifier = Modifier.height(12.dp))
            reminders.forEach { reminder ->
                TaskItemCard(reminder, t)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // All Tasks Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    t["tasks"] ?: "All Tasks",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = EarthBrown
                )
                Text(
                    "${tasks.count { it.isDone }}/${tasks.size} ${t["completedTasks"] ?: "Done"}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SeedGreen
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            tasks.forEach { task ->
                TaskItemCard(
                    task = task,
                    t = t,
                    onToggle = { 
                        tasks = tasks.map { if (it.id == task.id) it.copy(isDone = !it.isDone) else it }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(80.dp))
        }

        // Floating Add Button
        LargeFloatingActionButton(
            onClick = { showAddTaskDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = SeedGreen,
            contentColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task", modifier = Modifier.size(32.dp))
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            t = t,
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { name, day ->
                tasks = listOf(FarmTask(UUID.randomUUID().toString(), name, day)) + tasks
                showAddTaskDialog = false
            }
        )
    }
}

@Composable
fun TaskItemCard(
    task: FarmTask,
    t: Map<String, String>,
    onToggle: (() -> Unit)? = null
) {
    val icon = when (task.type) {
        "Irrigation" -> Icons.Default.WaterDrop
        "Fertilizer" -> Icons.Default.Science
        "Harvest" -> Icons.Default.Agriculture
        else -> Icons.AutoMirrored.Filled.ListAlt
    }
    
    val iconColor = when (task.type) {
        "Irrigation" -> SkyBlue
        "Fertilizer" -> HarvestGold
        "Harvest" -> SeedGreen
        else -> EarthBrown.copy(alpha = 0.6f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(enabled = onToggle != null) { onToggle?.invoke() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    task.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isDone) EarthBrown.copy(alpha = 0.3f) else EarthBrown
                )
                Text(
                    "${task.day} • ${task.priority.uppercase()}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EarthBrown.copy(alpha = 0.4f)
                )
            }
            
            if (onToggle != null) {
                Checkbox(
                    checked = task.isDone,
                    onCheckedChange = { onToggle.invoke() },
                    colors = CheckboxDefaults.colors(checkedColor = SeedGreen)
                )
            } else {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = EarthBrown.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun AddTaskDialog(
    t: Map<String, String>,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var day by remember { mutableStateOf("Today") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(t["addTask"] ?: "Add New Task", fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(t["taskName"] ?: "What needs to be done?") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Text(t["selectDay"] ?: "Select Day", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Today", "Tomorrow", "Next Week").forEach { d ->
                        FilterChip(
                            selected = day == d,
                            onClick = { day = d },
                            label = { Text(d) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onConfirm(name, day) },
                colors = ButtonDefaults.buttonColors(containerColor = SeedGreen)
            ) {
                Text(t["create"] ?: "Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(t["cancel"] ?: "Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

// ════════════════════════════════════════════════════════════
//  FARMER PROFILE SCREEN
// ════════════════════════════════════════════════════════════

@Composable
fun FarmerProfileScreen(
    profile: FarmerProfile,
    language: Language,
    t: Map<String, String>,
    onEditProfile: () -> Unit,
    onLogout: () -> Unit
) {
    val stats = remember {
        listOf(
            Triple(t["totalYield"] ?: "Total Yield", "145 q", SeedGreen),
            Triple(t["tasksCompleted"] ?: "Tasks Done", "28", HarvestGold),
            Triple(t["avgYield"] ?: "Avg Yield", "24 q/ac", SkyBlue)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmWhite)
            .verticalScroll(rememberScrollState())
    ) {
        // Header / Profile Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(EarthBrown)
                .padding(bottom = 32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                
                // Profile Avatar
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        modifier = Modifier.size(100.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f),
                        border = BorderStroke(3.dp, Color.White)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(20.dp),
                            tint = Color.White
                        )
                    }
                    Surface(
                        modifier = Modifier.size(32.dp).clickable { onEditProfile() },
                        shape = CircleShape,
                        color = HarvestGold,
                        border = BorderStroke(2.dp, EarthBrown)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.padding(6.dp), tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    profile.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    profile.location,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.6f)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    stats.forEach { (label, value, color) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = color)
                            Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Details Section
        Column(
            modifier = Modifier
                .padding(16.dp)
                .offset(y = (-20).dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(t["cropDetails"] ?: "Crop & Land Details", fontWeight = FontWeight.Black, color = EarthBrown)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ProfileDetailRow(Icons.Default.Agriculture, t["cropType"] ?: "Main Crop", profile.crop.name)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = WarmWhite)
                    ProfileDetailRow(Icons.Default.SquareFoot, t["landSize"] ?: "Land Size", "${profile.landSize} ${t["acres"] ?: "Acres"}")
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = WarmWhite)
                    ProfileDetailRow(Icons.Default.LocationOn, t["village"] ?: "Village", profile.location.split(",")[0])
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = WarmWhite)
                    ProfileDetailRow(Icons.Default.Language, t["prefLanguage"] ?: "Language", if (profile.language == Language.en) "English" else "Kannada")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Section
            Text(
                t["settings"] ?: "Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = EarthBrown,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )
            
            SettingsItem(Icons.Default.Notifications, "Notifications", "Alerts & reminders settings")
            SettingsItem(Icons.Default.Security, "Security", "Privacy and data protection")
            SettingsItem(Icons.AutoMirrored.Filled.Help, "Help & Support", "FAQs and contact support")
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ClayRed.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = ClayRed)
                Spacer(modifier = Modifier.width(8.dp))
                Text(t["logout"] ?: "Logout", color = ClayRed, fontWeight = FontWeight.Black)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ProfileDetailRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = SeedGreen, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = EarthBrown.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Black, color = EarthBrown)
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { },
        shape = RoundedCornerShape(16.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = EarthBrown.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Black, color = EarthBrown)
                Text(subtitle, fontSize = 11.sp, color = EarthBrown.copy(alpha = 0.4f))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = EarthBrown.copy(alpha = 0.2f))
        }
    }
}
