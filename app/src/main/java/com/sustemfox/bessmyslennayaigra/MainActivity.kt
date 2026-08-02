package com.sustemfox.bessmyslennayaigra

import android.app.Activity
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var page by rememberSaveable { mutableStateOf("menu") }
    var soundEnabled by rememberSaveable { mutableStateOf(true) }
    val activity = LocalView.current.context as? Activity
    MaterialTheme(colorScheme = darkColorScheme(background = background, surface = surface)) {
        Surface(Modifier.fillMaxSize(), color = background) {
            AnimatedContent(targetState = page, transitionSpec = { fadeIn(tween(280)) togetherWith fadeOut(tween(180)) }, label = "page") { current ->
                when (current) {
                    "game" -> GameScreen(soundEnabled, onBack = { page = "menu" })
                    "settings" -> SettingsScreen(soundEnabled, { soundEnabled = it }, { page = "menu" })
                    else -> MenuScreen(onPlay = { page = "game" }, onSettings = { page = "settings" }, onExit = { activity?.finish() })
                }
            }
        }
    }
}

@Composable private fun MenuScreen(onPlay: () -> Unit, onSettings: () -> Unit, onExit: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "logo").animateFloat(initialValue = 0.96f, targetValue = 1.04f, animationSpec = androidx.compose.animation.core.infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), androidx.compose.animation.core.RepeatMode.Reverse), label = "pulse")
    Column(Modifier.fillMaxSize().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Surface(Modifier.size(128.dp).scale(pulse), CircleShape, color = Color(0xFF6C63FF), shadowElevation = 12.dp) { Box(contentAlignment = Alignment.Center) { Text("?", fontSize = 76.sp, fontWeight = FontWeight.Black, color = Color.White) } }
        Spacer(Modifier.height(28.dp))
        Text("БЕССМЫСЛЕННАЯ\nИГРА", textAlign = TextAlign.Center, color = ink, fontSize = 31.sp, lineHeight = 35.sp, fontWeight = FontWeight.Black)
        Text("Никакой цели. Никаких причин остановиться.", color = muted, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp, bottom = 40.dp))
        MenuButton("Играть", Color(0xFF6C63FF), onPlay)
        Spacer(Modifier.height(12.dp))
        MenuButton("Настройки", Color(0xFF313244), onSettings)
        Spacer(Modifier.height(12.dp))
        MenuButton("Выход", Color(0xFF45475A), onExit)
        Spacer(Modifier.height(28.dp)); Text("v1.0 • сделано без особой причины", color = Color(0xFF6C7086), fontSize = 12.sp)
    }
}

@Composable private fun MenuButton(text: String, color: Color, action: () -> Unit) {
    Button(onClick = action, modifier = Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(18.dp), colors = ButtonDefaults.buttonColors(containerColor = color)) { Text(text, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
}

@Composable private fun SettingsScreen(sound: Boolean, onSound: (Boolean) -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("НАСТРОЙКИ", color = ink, fontSize = 27.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(22.dp))
        Surface(shape = RoundedCornerShape(18.dp), color = surface) { Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Звук клика", color = ink, fontWeight = FontWeight.Bold); Text("Короткий сигнал при нажатии", color = muted, fontSize = 13.sp) }; Switch(checked = sound, onCheckedChange = onSound) } }
        Spacer(Modifier.weight(1f)); OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text("Назад") }
    }
}

@Composable private fun GameScreen(soundEnabled: Boolean, onBack: () -> Unit) {
    var score by rememberSaveable { mutableIntStateOf(0) }; var phrase by rememberSaveable { mutableStateOf("Добро пожаловать в игру без цели.") }; var title by rememberSaveable { mutableStateOf("Нажми меня") }; var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.88f else 1f, tween(130, easing = FastOutSlowInEasing), label = "buttonScale")
    val color = listOf(Color(0xFFFF6B6B), Color(0xFF6C63FF), Color(0xFF00BFA6), Color(0xFFFFB703))[score % 4]
    val view = LocalView.current
    Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = onBack) { Text("← Меню") }; Spacer(Modifier.weight(1f)); Text("Уровень ${score / 25 + 1}", color = muted) }
        Text("ПРИЧИНА НЕ НАЙДЕНА", color = ink, fontSize = 19.sp, fontWeight = FontWeight.Black)
        Spacer(Modifier.weight(1f))
        AnimatedContent(score, label = "score") { Text("$it", fontSize = 82.sp, fontWeight = FontWeight.Black, color = ink) }
        Text("единиц бессмысленности", color = muted)
        Spacer(Modifier.height(28.dp))
        Surface(color = color, shape = CircleShape, shadowElevation = 12.dp, modifier = Modifier.size(220.dp).scale(scale).clickable { pressed = true; view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP); if (soundEnabled) ToneGenerator(AudioManager.STREAM_MUSIC, 35).startTone(ToneGenerator.TONE_PROP_BEEP, 55); score++; phrase = phrases.random(); title = titles.random(); pressed = false }) { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(title, color = Color.White, textAlign = TextAlign.Center, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp)) } }
        Spacer(Modifier.height(30.dp))
        Surface(color = Color(0xFF313244), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp)) { Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text(phrase, color = Color(0xFFCDD6F4), textAlign = TextAlign.Center) } }
        Spacer(Modifier.weight(1f)); Text("Рекорд: $score. Никому не рассказывай.", color = Color(0xFF6C7086), fontSize = 12.sp)
    }
}
