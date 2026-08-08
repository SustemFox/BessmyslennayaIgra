package com.sustemfox.bessmyslennayaigra

import android.app.Activity
import android.content.Context
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
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sustemfox.bessmyslennayaigra.BuildConfig
import kotlinx.coroutines.delay
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also { setContent { MeaninglessApp() } }
}

private val phrases = listOf("Нажми. Это важно для огурца.", "Кнопка слегка довольна.", "Ты приблизил конец, но неясно чего.", "Где-то вздохнул воображаемый бухгалтер.", "+1 к абсолютно ничему.", "Продолжай. На всякий случай.", "Голубь одобряет твою стратегию.", "Это было почти осмысленно. Не повторяй.", "Картошка наблюдает.")
private val titles = listOf("Нажми меня", "Не трогай", "Почти готово", "Возможно, сюда", "Кнопка", "Срочно нажми")
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
            "game", "settings" -> page = "menu"
            else -> activity?.finish()
        }
    }
    MaterialTheme(colorScheme = darkColorScheme(background = background, surface = surface)) {
        Surface(Modifier.fillMaxSize(), color = background) {
            AnimatedContent(targetState = page, transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(180)) }, label = "page") { current ->
                when (current) {
                    "game" -> GameScreen(prefs.getInt("score", 0), prefs.getInt("record", 0), soundEnabled, vibrationEnabled, onSave = { s, r -> prefs.edit().putInt("score", s).putInt("record", r).apply() }, onBack = { page = "menu" })
                    "settings" -> SettingsScreen(soundEnabled, vibrationEnabled, { soundEnabled = it; prefs.edit().putBoolean("sound_enabled", it).apply() }, { vibrationEnabled = it; prefs.edit().putBoolean("vibration_enabled", it).apply() }, { page = "menu" })
                    else -> MenuScreen(onPlay = { page = "game" }, onSettings = { page = "settings" }, onExit = { activity?.finish() })
                }
            }
        }
    }
}

@Composable private fun MenuScreen(onPlay: () -> Unit, onSettings: () -> Unit, onExit: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "logo").animateFloat(initialValue = 0.96f, targetValue = 1.04f, animationSpec = androidx.compose.animation.core.infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Reverse), label = "pulse")
    Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(Modifier.size(128.dp).scale(pulse), CircleShape, color = Color(0xFFF97316), shadowElevation = 12.dp) { Box(contentAlignment = Alignment.Center) { Text("⚡", fontSize = 68.sp, fontWeight = FontWeight.Black, color = Color(0xFFFEF3C7)) } }
        Spacer(Modifier.height(28.dp))
        Text("БЕССМЫСЛЕННАЯ\nИГРА", textAlign = TextAlign.Center, color = ink, fontSize = 31.sp, lineHeight = 35.sp, fontWeight = FontWeight.Black)
        Text("Никакой цели. Никаких причин остановиться.", color = muted, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp, bottom = 40.dp))
        MenuButton("Играть", Color(0xFF00A884), onPlay)
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

@Composable private fun GameScreen(savedScore: Int, savedRecord: Int, soundEnabled: Boolean, vibrationEnabled: Boolean, onSave: (Int, Int) -> Unit, onBack: () -> Unit) {
    var score by rememberSaveable { mutableIntStateOf(savedScore) }
    var record by rememberSaveable { mutableIntStateOf(maxOf(savedRecord, savedScore)) }
    var phrase by rememberSaveable { mutableStateOf("Добро пожаловать в игру без цели.") }
    var title by rememberSaveable { mutableStateOf("Нажми меня") }
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.88f else 1f, tween(130, easing = FastOutSlowInEasing), label = "buttonScale")
    val color = listOf(Color(0xFFFF6B6B), Color(0xFF6C63FF), Color(0xFF00BFA6), Color(0xFFFFB703))[score % 4]
    val view = LocalView.current
    val context = view.context

    // Счёт сохраняем не на каждый клик, а при паузе/выходе — бережём флеш-память
    val saveAll = { onSave(score, record) }
    val currentSave by rememberUpdatedState(saveAll)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) currentSave()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            currentSave()
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

    // Возврат кнопки после нажатия — с задержкой, чтобы анимация успевала проиграться
    LaunchedEffect(pressed) { if (pressed) { delay(140); pressed = false } }

    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = { currentSave(); onBack() }) { Text("← Меню") }; Spacer(Modifier.weight(1f)); Text("Уровень ${score / 25 + 1}", color = muted) }
        Text("ПРИЧИНА НЕ НАЙДЕНА", color = ink, fontSize = 19.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.weight(1f))
        Text("$score", fontSize = 82.sp, fontWeight = FontWeight.Black, color = ink)
        Text("единиц бессмысленности", color = muted)
        Spacer(Modifier.height(28.dp))
        Surface(color = color, shape = CircleShape, shadowElevation = 12.dp, modifier = Modifier.size(220.dp).scale(scale).clickable {
            pressed = true
            if (vibrationEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            if (soundEnabled) {
                if (soundReady && clickSound != 0) soundPool.play(clickSound, 0.9f, 0.9f, 1, 0, 1f) else pendingClick = true
            }
            score++
            if (score > record) record = score
            phrase = phrases.random()
            title = titles.random()
        }) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(title, color = Color.White, textAlign = TextAlign.Center, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp)) } }
        Spacer(Modifier.height(30.dp))
        Surface(color = Color(0xFF313244), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp)) { Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text(phrase, color = Color(0xFFCDD6F4), textAlign = TextAlign.Center) } }
        Spacer(Modifier.weight(1f)); Text("Рекорд: $record. Никому не рассказывай.", color = Color(0xFF6C7086), fontSize = 12.sp)
    }
}
