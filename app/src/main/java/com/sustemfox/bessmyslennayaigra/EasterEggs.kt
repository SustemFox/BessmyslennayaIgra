package com.sustemfox.bessmyslennayaigra

// Пасхалки. Правило: отсылка узнаётся, но награды нет — смысла не прибавилось.
// Каждая срабатывает один раз, факт хранится в SharedPreferences ("eggs_found").

// Пасхалки по счёту: id, порог, текст
class ScoreEgg(val id: String, val value: Int, val text: String)

val scoreEggs = listOf(
    ScoreEgg("777", 777, "🎰 Три семёрки. Джекпот. Приз: ничего. Забирай."),
    ScoreEgg("1337", 1337, "🕹️ ЧИТЕР ОБНАРУЖЕН. Античит не пришёл. Он тоже бессмысленный."),
    ScoreEgg("9001", 9001, "💥 Больше девяти тысяч. Кнопка не стала сильнее."),
    ScoreEgg("9999", 9999, "🧱 Почти предел. Предела нет. Обманул.")
)

const val EGG_IDLE_ID = "idle"
const val EGG_IDLE_TEXT = "⌛ Ты ушёл. Бессмыслица подождала. Ей некуда спешить."
const val EGG_NIGHT_ID = "night"
const val EGG_NIGHT_TEXT = "🌙 Ночь. Ты кликаешь. Бессмыслица не спит. И ты не спи."

// Ночь — с 00:00 до 04:59
fun isNight(now: Long): Boolean {
    val hour = java.util.Calendar.getInstance().apply { timeInMillis = now }.get(java.util.Calendar.HOUR_OF_DAY)
    return hour in 0..4
}

// Первая несобранная пасхалка, пересёкшаяся этим кликом
fun crossedScoreEgg(previousScore: Int, score: Int, found: Set<String>): ScoreEgg? =
    scoreEggs.firstOrNull { it.id !in found && previousScore < it.value && score >= it.value }
