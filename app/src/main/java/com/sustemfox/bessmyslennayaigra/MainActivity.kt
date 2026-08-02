package com.sustemfox.bessmyslennayaigra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { MeaninglessGame() } }
}

private val phrases = listOf(
    "Нажми. Это важно для огурца.", "Кнопка слегка довольна.", "Ты приблизил конец, но неясно чего.",
    "Где-то вздохнул воображаемый бухгалтер.", "+1 к абсолютно ничему.", "Продолжай. На всякий случай.",
    "Голубь одобряет твою стратегию.", "Это было почти осмысленно. Не повторяй.", "Картошка наблюдает."
)
private val titles = listOf("Нажми меня", "Не трогай", "Почти готово", "Возможно, сюда", "Кнопка", "Срочно нажми")

@Composable private fun MeaninglessGame() {
    var score by rememberSaveable { mutableIntStateOf(0) }
    var phrase by rememberSaveable { mutableStateOf("Добро пожаловать в игру без цели.") }
    var title by rememberSaveable { mutableStateOf("Нажми меня") }
    var scale by remember { mutableFloatStateOf(1f) }
    val level = score / 25 + 1
    val color = listOf(Color(0xFFFF6B6B), Color(0xFF6C63FF), Color(0xFF00BFA6), Color(0xFFFFB703))[score % 4]
    MaterialTheme(colorScheme = darkColorScheme(background = Color(0xFF11111B), surface = Color(0xFF1E1E2E))) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(32.dp))
                Text("БЕССМЫСЛЕННАЯ ИГРА", color = Color(0xFFF5E0DC), fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text("Уровень $level • причина не найдена", color = Color(0xFFA6ADC8), modifier = Modifier.padding(top = 8.dp))
                Spacer(Modifier.weight(1f))
                AnimatedContent(score, label = "score") { Text("$it", fontSize = 82.sp, fontWeight = FontWeight.Black, color = Color(0xFFF5E0DC)) }
                Text("единиц бессмысленности", color = Color(0xFFA6ADC8))
                Spacer(Modifier.height(30.dp))
                Surface(color = color, shape = CircleShape, shadowElevation = 10.dp, modifier = Modifier.size(220.dp).scale(scale).clickable {
                    score++; phrase = phrases.random(); title = titles.random(); scale = if (Random.nextBoolean()) .92f else 1.06f
                }) { Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) { Text(title, textAlign = TextAlign.Center, color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp)) } }
                Spacer(Modifier.height(32.dp))
                Surface(color = Color(0xFF313244), shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp)) { Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text(phrase, color = Color(0xFFCDD6F4), textAlign = TextAlign.Center) } }
                Spacer(Modifier.weight(1f))
                Text("Рекорд: $score. Никому не рассказывай.", color = Color(0xFF6C7086), fontSize = 12.sp)
            }
        }
    }
}
