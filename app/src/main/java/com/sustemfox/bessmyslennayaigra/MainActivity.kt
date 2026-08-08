package com.sustemfox.bessmyslennayaigra

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.sustemfox.bessmyslennayaigra.BuildConfig
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also { setContent { MeaninglessApp() } }
}

private val thresholdMessages = mapOf(
    50 to "Ты достиг 50. Зачем? Никто не знает.",
    100 to "Сто. Круглое число. Но смысла не прибавилось.",
    250 to "Четверть тысячи. Огурец впечатлён.",
    500 to "Полтысячи. Где-то плачет картошка.",
    1000 to "ТЫСЯЧА. Ты потратил время. Навсегда.",
    2500 to "Две с половиной тысячи. Это уже диагноз.",
    5000 to "Пять тысяч. Вселенная заметила. Ей всё равно.",
    10000 to "ДЕСЯТЬ ТЫСЯЧ. Ты легенда бессмыслицы."
)

// Фразы разбиты по условиям: скорость кликов (streak) и прогресс счёта.
// Каждый клик гарантированно получает фразу, без повторов подряд.
private val earlyPhrases = listOf(
    "Нажми. Это важно для огурца.",
    "Кнопка слегка довольна.",
    "+1 к абсолютно ничему.",
    "Голубь одобряет твою стратегию.",
    "Картошка наблюдает.",
    "Это было почти осмысленно. Не повторяй.",
    "Ты приблизил конец, но неясно чего.",
    "Где-то вздохнул воображаемый бухгалтер.",
    "Продолжай. На всякий случай.",
    "Первые 50 — самые бессмысленные."
)

private val midPhrases = listOf(
    "Кнопка устала. Но терпит.",
    "Ты нажал. Мир изменился. Немного.",
    "Это было лишнее. Но ты продолжай.",
    "Огурец начал записывать. Куда — неизвестно.",
    "Половина смысла уже потеряна. Осталось нечего.",
    "Кнопка начала тебя уважать. Почти.",
    "Странно, но голубь теперь твой фанат.",
    "50+ единиц. Абсурд крепчает."
)

private val veteranPhrases = listOf(
    "250+. Это уже диагноз. Мягкий.",
    "Кнопка сдалась. Жми дальше.",
    "Бухгалтер плачет где-то в углу.",
    "Ты побил рекорд бессмыслицы. Свой же.",
    "Картошка аплодирует. Молча.",
    "Вселенная заметила. Ей всё равно.",
    "Огурец впечатлён. Это редкость.",
    "Ветеран бессмыслицы. Медаль не предусмотрена."
)

private val epicPhrases = listOf(
    "1000+. Ты легенда бессмыслицы.",
    "Время — деньги. Ты тратишь рубли.",
    "Кнопка молится, чтобы ты остановился.",
    "Учёные в замешательстве. Снова.",
    "Это уже не игра. Это философия.",
    "Где-то далеко плачет картошка. От гордости.",
    "Ты доказал: цель не нужна.",
    "Эпос. Сага. Бессмыслица."
)

private val fastPhrases = listOf(
    "Быстро! Рука-молния!",
    "Ты что, робот?",
    "Кнопка еле успевает!",
    "Тап-тап-тап. Барабанная дробь.",
    "Скорость бессмыслицы зашкаливает.",
    "Пальцу нужен отдых. Но не сейчас."
)

private val ultraFastPhrases = listOf(
    "РОБОТ ОБНАРУЖЕН. Продолжай.",
    "Кнопка в ужасе!",
    "Это уже читерство бессмыслицы.",
    "Стоп! Так нечестно! Ну ладно, продолжай.",
    "Твоя рука — легенда.",
    "Огурец не успевает следить!"
)

private fun pickPhrase(score: Int, streak: Int, lastPhrase: String?): String {
    val pool = when {
        streak >= 15 -> ultraFastPhrases
        streak >= 8 -> fastPhrases
        score >= 1000 -> epicPhrases
        score >= 250 -> veteranPhrases
        score >= 50 -> midPhrases
        else -> earlyPhrases
    }
    val candidates = pool.filter { it != lastPhrase }
    return (candidates.ifEmpty { pool }).random()
}

private val titles = listOf("Нажми меня", "Не трогай", "Почти готово", "Возможно, сюда", "Кнопка", "Срочно нажми", "Осторожно!", "Тут что-то есть", "Не нажимай", "Последний шанс")

enum class RandomEvent { NONE, RUN_AWAY, COLOR_SHIFT, DOUBLE_POINTS, SHRINK, UPSIDE_DOWN }

private val background = Color(0xFF11111B)
private val surface = Color(0xFF1E1E2E)
private val ink = Color(0xFFF5E0DC)
private val muted = Color(0xFFA6ADC8)

@Composable private fun MeaninglessApp() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("game_state", Context.MODE_PRIVATE) }
    var page by rememberSaveable { mutableStateOf("menu") }
    var soundEnabled by remember { mutableStateOf(prefs.getBoolean("sound_enabled", true)) }
    var vibrationEnabled by remember { mutableStateOf(prefs.getBoolean("vibration_enabled", true)) }
    val activity = context as? Activity
    BackHandler {
        when (page) {
            "game", "settings", "stats" -> page = "menu"
            else -> activity?.finish()
        }
    }
    MaterialTheme(colorScheme = darkColorScheme(background = background, surface = surface)) {
        Surface(Modifier.fillMaxSize(), color = background) {
            AnimatedContent(targetState = page, transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(180)) }, label = "page") { current ->
                when (current) {
                    "game" -> GameScreen(prefs, soundEnabled, vibrationEnabled, onBack = { page = "menu" })
                    "settings" -> SettingsScreen(soundEnabled, vibrationEnabled, { soundEnabled = it; prefs.edit().putBoolean("sound_enabled", it).apply() }, { vibrationEnabled = it; prefs.edit().putBoolean("vibration_enabled", it).apply() }, { page = "menu" })
                    "stats" -> StatsScreen(prefs, onBack = { page = "menu" })
                    else -> MenuScreen(onPlay = { page = "game" }, onSettings = { page = "settings" }, onStats = { page = "stats" }, onExit = { activity?.finish() })
                }
            }
        }
    }
}

@Composable private fun MenuScreen(onPlay: () -> Unit, onSettings: () -> Unit, onStats: () -> Unit, onExit: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "logo").animateFloat(initialValue = 0.96f, targetValue = 1.04f, animationSpec = androidx.compose.animation.core.infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Reverse), label = "pulse")
    Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(Modifier.size(128.dp).scale(pulse), CircleShape, color = Color(0xFFF97316), shadowElevation = 12.dp) { Box(contentAlignment = Alignment.Center) { Text("⚡", fontSize = 68.sp, fontWeight = FontWeight.Black, color = Color(0xFFFEF3C7)) } }
        Spacer(Modifier.height(28.dp))
        Text("БЕССМЫСЛЕННАЯ\nИГРА", textAlign = TextAlign.Center, color = ink, fontSize = 31.sp, lineHeight = 35.sp, fontWeight = FontWeight.Black)
        Text("Никакой цели. Никаких причин остановиться.", color = muted, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp, bottom = 40.dp))
        MenuButton("Играть", Color(0xFF00A884), onPlay)
        Spacer(Modifier.height(12.dp))
        MenuButton("Статистика", Color(0xFF8B5CF6), onStats)
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

@Composable private fun SettingsScreen(sound: Boolean, vibration: Boolean, onSound: (Boolean) -> Unit, onVibration: (Boolean) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("НАСТРОЙКИ", color = ink, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(22.dp))
        SettingToggle("Звук клика", "Аркадный звук при нажатии", sound, onSound)
        Spacer(Modifier.height(12.dp))
        SettingToggle("Вибрация", "Тактильный отклик при нажатии", vibration, onVibration)
        Spacer(Modifier.weight(1f)); OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("Назад") }
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
    val clicksPerMinute = if (minutes > 0) (totalClicks.toFloat() / minutes).roundToInt() else totalClicks
    val bestScore = prefs.getInt("best_score", 0)
    val thresholdReached = prefs.getInt("threshold_reached", 0)
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

@Composable private fun GameScreen(prefs: SharedPreferences, soundEnabled: Boolean, vibrationEnabled: Boolean, onBack: () -> Unit) {
    var score by rememberSaveable { mutableIntStateOf(prefs.getInt("score", 0)) }
    var phrase by rememberSaveable { mutableStateOf("Добро пожаловать в игру без цели.") }
    var title by rememberSaveable { mutableStateOf("Нажми меня") }
    var pressed by remember { mutableStateOf(false) }
    var buttonOffset by remember { mutableStateOf(Offset.Zero) }
    var buttonColor by remember { mutableStateOf(Color(0xFFFF6B6B)) }
    var currentEvent by remember { mutableStateOf(RandomEvent.NONE) }
    var eventMessage by remember { mutableStateOf("") }
    var showThreshold by remember { mutableStateOf<String?>(null) }
    var clickTimestamps by remember { mutableStateOf<List<Long>>(emptyList()) }
    var lastPhrase by remember { mutableStateOf<String?>(null) }
    var easterEggTriggered by remember { mutableStateOf(false) }
    var sessionStart by remember { mutableStateOf(System.currentTimeMillis()) }

    // «Мир перевернулся» — реальный переворот всего интерфейса на 180°
    val flipDegrees by animateFloatAsState(if (currentEvent == RandomEvent.UPSIDE_DOWN) 180f else 0f, tween(700), label = "flip")
    val scale by animateFloatAsState(if (pressed) 0.88f else 1f, tween(130, easing = FastOutSlowInEasing), label = "buttonScale")
    val offset by animateOffsetAsState(targetValue = buttonOffset, animationSpec = tween(400), label = "offset")
    val view = LocalView.current
    val context = view.context
    val clipboardManager = LocalClipboardManager.current

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                prefs.edit()
                    .putInt("score", score)
                    .putLong("time_played_ms", prefs.getLong("time_played_ms", 0) + (System.currentTimeMillis() - sessionStart))
                    .apply()
                sessionStart = System.currentTimeMillis()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            prefs.edit()
                .putInt("score", score)
                .putLong("time_played_ms", prefs.getLong("time_played_ms", 0) + (System.currentTimeMillis() - sessionStart))
                .apply()
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val soundPool = remember { SoundPool.Builder().setMaxStreams(2).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build() }
    var clickSound by remember { mutableIntStateOf(0) }
    var soundReady by remember { mutableStateOf(false) }
    var pendingClick by remember { mutableStateOf(false) }
    DisposableEffect(soundPool) {
        clickSound = soundPool.load(context, R.raw.click, 1)
        soundPool.setOnLoadCompleteListener { _, _, status ->
            if (status == 0) {
                soundReady = true
                if (pendingClick && soundEnabled) soundPool.play(clickSound, 0.9f, 0.9f, 1, 0, 1f)
                pendingClick = false
            }
        }
        onDispose { soundPool.release() }
    }

    LaunchedEffect(pressed) { if (pressed) { delay(140); pressed = false } }

    LaunchedEffect(currentEvent) {
        if (currentEvent != RandomEvent.NONE) {
            delay(2500)
            currentEvent = RandomEvent.NONE
            buttonOffset = Offset.Zero
            buttonColor = listOf(Color(0xFFFF6B6B), Color(0xFF6C63FF), Color(0xFF00BFA6), Color(0xFFFFB703))[score % 4]
            eventMessage = ""
        }
    }

    LaunchedEffect(showThreshold) {
        if (showThreshold != null) {
            delay(3000)
            showThreshold = null
        }
    }

    Column(Modifier.fillMaxSize().padding(24.dp).graphicsLayer { rotationZ = flipDegrees }, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = {
                prefs.edit().putInt("score", score).putLong("time_played_ms", prefs.getLong("time_played_ms", 0) + (System.currentTimeMillis() - sessionStart)).apply()
                sessionStart = System.currentTimeMillis()
                onBack()
            }) { Text("← Меню") }
            Spacer(Modifier.weight(1f))
            Text("Уровень ${score / 25 + 1}", color = muted)
        }
        Text("ПРИЧИНА НЕ НАЙДЕНА", color = ink, fontSize = 19.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.weight(1f))

        if (showThreshold != null) {
            Surface(color = Color(0xFF8B5CF6), shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                Text(showThreshold!!, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center)
            }
        }

        if (eventMessage.isNotEmpty()) {
            Surface(color = Color(0xFFF97316), shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                Text(eventMessage, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center)
            }
        }

        Text("$score", fontSize = 82.sp, fontWeight = FontWeight.Black, color = ink)
        Text("единиц бессмысленности", color = muted)
        Spacer(Modifier.height(28.dp))

        val clickScale = if (currentEvent == RandomEvent.SHRINK) 0.7f else 1f
        Surface(color = buttonColor, shape = CircleShape, shadowElevation = 12.dp, modifier = Modifier.size(220.dp).scale(scale * clickScale).offset(offset.x.dp, offset.y.dp).clickable {
            pressed = true
            if (vibrationEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            if (soundEnabled) {
                if (soundReady && clickSound != 0) soundPool.play(clickSound, 0.9f, 0.9f, 1, 0, 1f) else pendingClick = true
            }

            val now = System.currentTimeMillis()
            clickTimestamps = (clickTimestamps.filter { now - it < 2000 } + now).takeLast(20)
            val streak = clickTimestamps.size
            if (streak >= 10 && !easterEggTriggered) {
                easterEggTriggered = true
                eventMessage = "🥚 ПАСХАЛКА! Ты слишком быстр. +50 очков!"
                score += 50
                buttonColor = Color(0xFFEAB308)
            }

            score++
            prefs.edit().putInt("total_clicks", prefs.getInt("total_clicks", 0) + 1).apply()
            if (score > prefs.getInt("best_score", 0)) prefs.edit().putInt("best_score", score).apply()

            // Фраза всегда обновляется по условиям: скорость > прогресс, без повторов подряд
            phrase = pickPhrase(score, streak, lastPhrase)
            lastPhrase = phrase
            title = titles.random()

            val threshold = thresholdMessages.keys.firstOrNull { it == score }
            if (threshold != null) {
                showThreshold = thresholdMessages[threshold]
                prefs.edit().putInt("threshold_reached", maxOf(prefs.getInt("threshold_reached", 0), threshold)).apply()
            }

            if (Random.nextInt(100) < 8 && currentEvent == RandomEvent.NONE) {
                when (Random.nextInt(5)) {
                    0 -> {
                        currentEvent = RandomEvent.RUN_AWAY
                        buttonOffset = Offset(Random.nextInt(-80, 81).toFloat(), Random.nextInt(-80, 81).toFloat())
                        eventMessage = "🏃 Кнопка убежала!"
                    }
                    1 -> {
                        currentEvent = RandomEvent.COLOR_SHIFT
                        buttonColor = listOf(Color(0xFFEC4899), Color(0xFF06B6D4), Color(0xFF84CC16), Color(0xFFF59E0B)).random()
                        eventMessage = "🎨 Кнопка сменила цвет!"
                    }
                    2 -> {
                        currentEvent = RandomEvent.DOUBLE_POINTS
                        score += 2
                        eventMessage = "✨ Двойные очки! +2"
                    }
                    3 -> {
                        currentEvent = RandomEvent.SHRINK
                        eventMessage = "🔍 Кнопка уменьшилась!"
                    }
                    4 -> {
                        currentEvent = RandomEvent.UPSIDE_DOWN
                        phrase = "🙃 Держись. Всё наоборот."
                        lastPhrase = phrase
                        eventMessage = "🙃 Мир перевернулся!"
                    }
                }
            }
        }) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(title, color = Color.White, textAlign = TextAlign.Center, fontSize = if (currentEvent == RandomEvent.SHRINK) 18.sp else 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp))
            }
        }
        Spacer(Modifier.height(30.dp))
        Surface(color = Color(0xFF313244), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp)) { Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text(phrase, color = Color(0xFFCDD6F4), textAlign = TextAlign.Center) } }
        Spacer(Modifier.weight(1f))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            TextButton(onClick = {
                val shareText = "Я набрал $score очков в Бессмысленной Игре! Никакой цели, никаких причин остановиться. 🎮⚡"
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                context.startActivity(Intent.createChooser(intent, "Поделиться результатом"))
            }) { Text("📤 Поделиться") }
            TextButton(onClick = {
                val statsText = "📊 Моя статистика в Бессмысленной Игре:\n• Всего кликов: ${prefs.getInt("total_clicks", 0)}\n• Лучший счёт: ${prefs.getInt("best_score", 0)}\n• Время в игре: ${prefs.getLong("time_played_ms", 0) / 60000} мин"
                clipboardManager.setText(AnnotatedString(statsText))
                eventMessage = "📋 Скопировано в буфер!"
            }) { Text("📋 Копировать") }
        }
    }
}
