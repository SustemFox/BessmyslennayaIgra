package com.sustemfox.bessmyslennayaigra

import android.content.SharedPreferences
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable fun GameScreen(prefs: SharedPreferences, soundEnabled: Boolean, vibrationEnabled: Boolean, onBack: () -> Unit) {
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
    var currentRank by remember { mutableStateOf("") }
    var record by remember { mutableIntStateOf(prefs.getInt("best_score", 0)) }
    var diary by remember { mutableStateOf(readDiary(prefs)) }
    var easterEggTriggered by remember { mutableStateOf(prefs.getBoolean("easter_egg_found", false)) }
    var sessionStart by remember { mutableStateOf(System.currentTimeMillis()) }
    var eggsFound by remember { mutableStateOf(prefs.getStringSet("eggs_found", emptySet())?.toSet() ?: emptySet()) }
    var eggMessage by remember { mutableStateOf<String?>(null) }
    var clickTick by remember { mutableIntStateOf(0) }

    val flipDegrees by animateFloatAsState(if (currentEvent == RandomEvent.UPSIDE_DOWN) 180f else 0f, tween(700), label = "flip")
    val scale by animateFloatAsState(if (pressed) 0.88f else 1f, tween(130, easing = FastOutSlowInEasing), label = "buttonScale")
    val offset by animateOffsetAsState(targetValue = buttonOffset, animationSpec = tween(400), label = "offset")
    val view = LocalView.current
    val context = view.context

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                prefs.edit()
                    .putInt("score", score)
                    .putLong("time_played_ms", prefs.getLong("time_played_ms", 0) + (System.currentTimeMillis() - sessionStart))
                    .apply()
                sessionStart = System.currentTimeMillis()
            } else if (event == Lifecycle.Event.ON_RESUME) {
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

    LaunchedEffect(currentEvent) {
        if (currentEvent == RandomEvent.RUN_AWAY) {
            repeat(5) {
                buttonOffset = Offset(Random.nextInt(-150, 151).toFloat(), Random.nextInt(-130, 131).toFloat())
                delay(400)
            }
            buttonOffset = Offset.Zero
        }
    }

    LaunchedEffect(showThreshold) {
        if (showThreshold != null) {
            delay(3000)
            showThreshold = null
        }
    }

    LaunchedEffect(eggMessage) {
        if (eggMessage != null) {
            delay(3500)
            eggMessage = null
        }
    }

    LaunchedEffect(clickTick) {
        if (clickTick == 0) return@LaunchedEffect
        delay(60_000)
        if (EGG_IDLE_ID !in eggsFound) {
            eggMessage = EGG_IDLE_TEXT
            eggsFound = eggsFound + EGG_IDLE_ID
            prefs.edit().putStringSet("eggs_found", eggsFound).apply()
            val entry = DiaryEntry(score, EGG_IDLE_TEXT, "Пасхалка")
            diary = (diary + entry).takeLast(100)
            writeDiary(prefs, diary)
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

        if (eggMessage != null) {
            Surface(color = Color(0xFFEAB308), shape = RoundedCornerShape(12.dp), modifier = Modifier.padding(bottom = 16.dp)) {
                Text(eggMessage!!, color = Color(0xFF11111B), fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center)
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
        Surface(color = buttonColor, shape = CircleShape, shadowElevation = 12.dp, modifier = Modifier.size(220.dp).scale(scale * clickScale).offset(offset.x.dp, offset.y.dp).clip(CircleShape).clickable {
            pressed = true
            clickTick++
            if (vibrationEnabled) view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            if (soundEnabled) {
                if (soundReady && clickSound != 0) soundPool.play(clickSound, 0.9f, 0.9f, 1, 0, 1f) else pendingClick = true
            }

            var gained = 1
            var diaryFromEvent = false

            val now = System.currentTimeMillis()
            clickTimestamps = (clickTimestamps.filter { now - it < 2000 } + now).takeLast(20)
            val streak = clickTimestamps.size

            if (streak >= 10 && !easterEggTriggered) {
                easterEggTriggered = true
                prefs.edit().putBoolean("easter_egg_found", true).apply()
                eventMessage = "🥚 ПАСХАЛКА! Ты слишком быстр. +50 очков!"
                gained += 50
                buttonColor = Color(0xFFEAB308)
            }

            if (Random.nextInt(100) < 4 && currentEvent == RandomEvent.NONE) {
                when (Random.nextInt(5)) {
                    0 -> {
                        currentEvent = RandomEvent.RUN_AWAY
                        buttonOffset = Offset(Random.nextInt(-150, 151).toFloat(), Random.nextInt(-130, 131).toFloat())
                        eventMessage = "🏃 Кнопка убежала!"
                    }
                    1 -> {
                        currentEvent = RandomEvent.COLOR_SHIFT
                        buttonColor = listOf(Color(0xFFEC4899), Color(0xFF06B6D4), Color(0xFF84CC16), Color(0xFFF59E0B)).random()
                        eventMessage = "🎨 Кнопка сменила цвет!"
                    }
                    2 -> {
                        currentEvent = RandomEvent.DOUBLE_POINTS
                        gained += 2
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
                        currentRank = "Перевёрнутая"
                        eventMessage = "🙃 Мир перевернулся!"
                        diaryFromEvent = true
                    }
                }
            }

            val previousScore = score
            score += gained
            prefs.edit().putInt("total_clicks", prefs.getInt("total_clicks", 0) + 1).apply()

            if (score > record) {
                record = score
                prefs.edit().putInt("best_score", record).apply()
            }

            thresholdMessages.keys.sorted().firstOrNull { previousScore < it && score >= it }?.let { threshold ->
                showThreshold = thresholdMessages[threshold]
                prefs.edit().putInt("threshold_reached", maxOf(prefs.getInt("threshold_reached", 0), threshold)).apply()
            }

            if (diaryFromEvent) {
                val entry = DiaryEntry(score, phrase, currentRank)
                diary = (diary + entry).takeLast(100)
                writeDiary(prefs, diary)
            }

            crossedScoreEgg(previousScore, score, eggsFound)?.let { egg ->
                eggMessage = egg.text
                eggsFound = eggsFound + egg.id
                prefs.edit().putStringSet("eggs_found", eggsFound).apply()
                val entry = DiaryEntry(score, egg.text, "Пасхалка")
                diary = (diary + entry).takeLast(100)
                writeDiary(prefs, diary)
            }

            if (EGG_NIGHT_ID !in eggsFound && isNight(now)) {
                eggMessage = EGG_NIGHT_TEXT
                eggsFound = eggsFound + EGG_NIGHT_ID
                prefs.edit().putStringSet("eggs_found", eggsFound).apply()
                val entry = DiaryEntry(score, EGG_NIGHT_TEXT, "Пасхалка")
                diary = (diary + entry).takeLast(100)
                writeDiary(prefs, diary)
            }

            if (currentEvent == RandomEvent.NONE) {
                val showPhrase = streak >= 8 || Random.nextInt(100) < 25
                if (showPhrase) {
                    val (newPhrase, newRank) = rollPhrase(score, streak, lastPhrase)
                    phrase = newPhrase
                    lastPhrase = phrase
                    currentRank = newRank
                    title = titles.random()
                    val entry = DiaryEntry(score, phrase, currentRank)
                    diary = (diary + entry).takeLast(100)
                    writeDiary(prefs, diary)
                } else {
                    phrase = ""
                    currentRank = ""
                }
            }
        }) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(title, color = Color.White, textAlign = TextAlign.Center, fontSize = if (currentEvent == RandomEvent.SHRINK) 18.sp else 25.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(20.dp))
            }
        }
        Spacer(Modifier.height(30.dp))
        Surface(color = Color(0xFF313244), shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth().heightIn(min = 76.dp)) { Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text(if (phrase.isEmpty()) "…" else phrase, color = if (phrase.isEmpty()) Color(0xFF6C7086) else Color(0xFFCDD6F4), textAlign = TextAlign.Center) } }
        if (currentRank.isNotEmpty()) { Text("ранг: $currentRank", color = Color(0xFFF59E0B), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp)) }
        Spacer(Modifier.weight(1f))
        Text("Рекорд: $record. Никому не рассказывай.", color = Color(0xFF6C7086), fontSize = 12.sp)
    }
}
