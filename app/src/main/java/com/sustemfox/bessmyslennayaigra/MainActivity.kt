package com.sustemfox.bessmyslennayaigra

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sustemfox.bessmyslennayaigra.BuildConfig
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also { setContent { MeaninglessApp() } }
}

val thresholdMessages = mapOf(
    50 to "Ты достиг 50. Зачем? Никто не знает.",
    100 to "Сто. Круглое число. Но смысла не прибавилось.",
    250 to "Четверть тысячи. Огурец впечатлён.",
    500 to "Полтысячи. Где-то плачет картошка.",
    1000 to "ТЫСЯЧА. Ты потратил время. Навсегда.",
    2500 to "Две с половиной тысячи. Это уже диагноз.",
    5000 to "Пять тысяч. Вселенная заметила. Ей всё равно.",
    10000 to "ДЕСЯТЬ ТЫСЯЧ. Ты легенда бессмыслицы."
)

data class DiaryEntry(val score: Int, val phrase: String, val rank: String)

fun readDiary(prefs: SharedPreferences): List<DiaryEntry> {
    val raw = prefs.getString("diary", null) ?: return emptyList()
    return try {
        val arr = JSONArray(raw)
        (0 until arr.length()).mapNotNull { i ->
            val o = arr.getJSONObject(i)
            DiaryEntry(o.getInt("score"), o.getString("phrase"), o.getString("rank"))
        }
    } catch (_: Exception) { emptyList() }
}

fun writeDiary(prefs: SharedPreferences, entries: List<DiaryEntry>) {
    val arr = JSONArray()
    entries.forEach { e -> arr.put(JSONObject().put("score", e.score).put("phrase", e.phrase).put("rank", e.rank)) }
    prefs.edit().putString("diary", arr.toString()).apply()
}

val titles = listOf("Нажми меня", "Не трогай", "Почти готово", "Возможно, сюда", "Кнопка", "Срочно нажми", "Осторожно!", "Тут что-то есть", "Не нажимай", "Последний шанс")

enum class RandomEvent { NONE, RUN_AWAY, COLOR_SHIFT, DOUBLE_POINTS, SHRINK, UPSIDE_DOWN }

private val background = Color(0xFF11111B)
private val surface = Color(0xFF1E1E2E)
val ink = Color(0xFFF5E0DC)
val muted = Color(0xFFA6ADC8)

@Composable private fun MeaninglessApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("game_state", Context.MODE_PRIVATE) }
    var page by rememberSaveable { mutableStateOf("menu") }
    var soundEnabled by remember { mutableStateOf(prefs.getBoolean("sound_enabled", true)) }
    var vibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration_enabled", true)) }
    val activity = context as? Activity
    BackHandler {
        when (page) {
            "game", "settings", "stats", "diary" -> page = "menu"
            else -> activity?.finish()
        }
    }
    MaterialTheme(colorScheme = darkColorScheme(background = background, surface = surface)) {
        Surface(Modifier.fillMaxSize(), color = background) {
            AnimatedContent(targetState = page, transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(180)) }, label = "page") { current ->
                when (current) {
                    "game" -> GameScreen(prefs, soundEnabled, vibrationEnabled, onBack = { page = "menu" })
                    "settings" -> SettingsScreen(prefs, soundEnabled, vibrationEnabled, { soundEnabled = it; prefs.edit().putBoolean("sound_enabled", it).apply() }, { vibrationEnabled = it; prefs.edit().putBoolean("vibration_enabled", it).apply() }, { page = "menu" })
                    "stats" -> StatsScreen(prefs, onBack = { page = "menu" })
                    "diary" -> DiaryScreen(prefs, onBack = { page = "menu" })
                    else -> MenuScreen(onPlay = { page = "game" }, onSettings = { page = "settings" }, onStats = { page = "stats" }, onDiary = { page = "diary" }, onExit = { activity?.finish() })
                }
            }
        }
    }
}

@Composable private fun MenuScreen(onPlay: () -> Unit, onSettings: () -> Unit, onStats: () -> Unit, onDiary: () -> Unit, onExit: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "logo").animateFloat(initialValue = 0.96f, targetValue = 1.04f, animationSpec = androidx.compose.animation.core.infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Reverse), label = "pulse")
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(Modifier.size(128.dp).scale(pulse), CircleShape, color = Color(0xFFF97316), shadowElevation = 12.dp) { Box(contentAlignment = Alignment.Center) { Text("⚡", fontSize = 68.sp, fontWeight = FontWeight.Black, color = Color(0xFFFEF3C7)) } }
        Spacer(Modifier.height(28.dp))
        Text("БЕССМЫСЛЕННАЯ\nИГРА", textAlign = TextAlign.Center, color = ink, fontSize = 31.sp, lineHeight = 35.sp, fontWeight = FontWeight.Black)
        Text("Никакой цели. Никаких причин остановиться.", color = muted, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp, bottom = 40.dp))
        MenuButton("Играть", Color(0xFF00A884), onPlay)
        Spacer(Modifier.height(12.dp))
        MenuButton("Статистика", Color(0xFF8B5CF6), onStats)
        Spacer(Modifier.height(12.dp))
        MenuButton("Дневник", Color(0xFF0EA5E9), onDiary)
        Spacer(Modifier.height(12.dp))
        MenuButton("Настройки", Color(0xFF2563EB), onSettings)
        Spacer(Modifier.height(12.dp))
        MenuButton("Выход", Color(0xFFD14343), onExit)
        Spacer(Modifier.height(28.dp)); Text("v${BuildConfig.VERSION_NAME} • сделано без особой причины", color = Color(0xFF6C7086), fontSize = 12.sp)
    }
}

@Composable private fun MenuButton(text: String, color: Color, action: () -> Unit) {
    Button(onClick = action, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.White)) { Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
}

@Composable private fun SettingsScreen(prefs: SharedPreferences, sound: Boolean, vibration: Boolean, onSound: (Boolean) -> Unit, onVibration: (Boolean) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val bestScore = prefs.getInt("best_score", 0)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)) {
        Text("НАСТРОЙКИ", color = ink, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(22.dp))
        SettingToggle("Звук клика", "Аркадный звук при нажатии", sound, onSound)
        Spacer(Modifier.height(12.dp))
        SettingToggle("Вибрация", "Тактильный отклик при нажатии", vibration, onVibration)
        Spacer(Modifier.height(22.dp))
        Surface(shape = RoundedCornerShape(18.dp), color = surface) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text("Рекорд", color = muted, fontSize = 14.sp)
                Text("$bestScore", color = ink, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Text("единиц бессмысленности. Хотя он бессмысленный, как и всё здесь.", color = Color(0xFF6C7086), fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(22.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = {
                val shareText = "Я набрал $bestScore очков в Бессмысленной Игре! Никакой цели, никаких причин остановиться. 🎮⚡"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, "Поделиться результатом"))
            }) { Text("📤 Поделиться") }
            TextButton(onClick = {
                val statsText = "📊 Моя статистика в Бессмысленной Игре:\n• Всего кликов: ${prefs.getInt("total_clicks", 0)}\n• Лучший счёт: $bestScore\n• Время в игре: ${prefs.getLong("time_played_ms", 0) / 60000} мин"
                clipboardManager.setText(AnnotatedString(statsText))
            }) { Text("📋 Копировать") }
        }
        Spacer(Modifier.height(22.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("Назад") }
    }
}

@Composable private fun SettingToggle(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = surface) { Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, color = ink, fontWeight = FontWeight.Bold); Text(subtitle, color = muted, fontSize = 13.sp) }; Switch(checked = checked, onCheckedChange = onChange) } }
}

@Composable private fun StatsScreen(prefs: SharedPreferences, onBack: () -> Unit) {
    val totalClicks = prefs.getInt("total_clicks", 0)
    val timePlayed = prefs.getLong("time_played_ms", 0)
    val minutes = (timePlayed / 60000).toInt()
    val seconds = ((timePlayed % 60000) / 1000).toInt()
    val clicksPerMinute = if (timePlayed > 0) (totalClicks * 60000f / timePlayed).roundToInt() else 0
    val bestScore = prefs.getInt("best_score", 0)
    val thresholdReached = prefs.getInt("threshold_reached", 0)
    val eggsFound = prefs.getStringSet("eggs_found", emptySet())?.size ?: 0
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("СТАТИСТИКА", color = ink, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(22.dp))
        StatCard("Всего кликов", "$totalClicks", "единиц бессмысленности потрачено")
        Spacer(Modifier.height(12.dp))
        StatCard("Время в игре", "$minutes мин $seconds сек", "которое не вернуть")
        Spacer(Modifier.height(12.dp))
        StatCard("Кликов в минуту", "$clicksPerMinute", "скорость бессмыслицы")
        Spacer(Modifier.height(12.dp))
        StatCard("Лучший счёт", "$bestScore", "рекорд, который никто не просил")
        Spacer(Modifier.height(12.dp))
        StatCard("Достигнут порог", "$thresholdReached", "уровень абсурда")
        Spacer(Modifier.height(12.dp))
        StatCard("Найдено пасхалок", "$eggsFound", "тоже без смысла")
        Spacer(Modifier.weight(1f))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("Назад") }
    }
}

@Composable private fun StatCard(title: String, value: String, subtitle: String) {
    Surface(shape = RoundedCornerShape(18.dp), color = surface) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text(title, color = muted, fontSize = 14.sp)
            Text(value, color = ink, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Text(subtitle, color = Color(0xFF6C7086), fontSize = 12.sp)
        }
    }
}

@Composable private fun DiaryScreen(prefs: SharedPreferences, onBack: () -> Unit) {
    var entries by remember { mutableStateOf(readDiary(prefs)) }
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("ДНЕВНИК БЕССМЫСЛИЦЫ", color = ink, fontSize = 25.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.weight(1f))
            if (entries.isNotEmpty()) {
                TextButton(onClick = {
                    entries = emptyList()
                    writeDiary(prefs, emptyList())
                }) { Text("Очистить", color = Color(0xFFD14343)) }
            }
        }
        Spacer(Modifier.height(18.dp))
        if (entries.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Пока пусто.\nКликай — фразы будут копиться здесь.", color = muted, textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(entries.reversed()) { entry ->
                    Surface(shape = RoundedCornerShape(18.dp), color = surface) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(entry.phrase, color = ink)
                                Text("ранг: ${entry.rank}", color = Color(0xFFF59E0B), fontSize = 12.sp)
                            }
                            Text("${entry.score}", color = muted, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("Назад") }
    }
}
