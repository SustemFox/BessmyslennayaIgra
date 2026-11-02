package com.sustemfox.bessmyslennayaigra

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import kotlin.math.roundToInt

/**
 * Текст, нарисованный точками (5x7 dot-matrix).
 * Каждый "пиксель" буквы — отдельная точка, масштабируется под доступную ширину.
 */

private const val GW = 5
private const val GH = 7
private const val CHAR_GAP = 1
private const val LINE_GAP = 2

private val GLYPHS: Map<Char, String> = mapOf(
    // цифры
    '0' to "01110 10001 10011 10101 11001 10001 01110",
    '1' to "00100 01100 00100 00100 00100 00100 01110",
    '2' to "01110 10001 00001 00010 00100 01000 11111",
    '3' to "11111 00010 00100 00010 00001 10001 01110",
    '4' to "00010 00110 01010 10010 11111 00010 00010",
    '5' to "11111 10000 11110 00001 00001 10001 01110",
    '6' to "00110 01000 10000 11110 10001 10001 01110",
    '7' to "11111 00001 00010 00100 01000 01000 01000",
    '8' to "01110 10001 10001 01110 10001 10001 01110",
    '9' to "01110 10001 10001 01111 00001 00010 01100",
    // латиница
    'A' to "01110 10001 10001 11111 10001 10001 10001",
    'B' to "11110 10001 10001 11110 10001 10001 11110",
    'C' to "01110 10001 10000 10000 10000 10001 01110",
    'D' to "11110 10001 10001 10001 10001 10001 11110",
    'E' to "11111 10000 10000 11110 10000 10000 11111",
    'F' to "11111 10000 10000 11110 10000 10000 10000",
    'G' to "01110 10001 10000 10111 10001 10001 01111",
    'H' to "10001 10001 10001 11111 10001 10001 10001",
    'I' to "01110 00100 00100 00100 00100 00100 01110",
    'J' to "00111 00010 00010 00010 00010 10010 01100",
    'K' to "10001 10010 10100 11000 10100 10010 10001",
    'L' to "10000 10000 10000 10000 10000 10000 11111",
    'M' to "10001 11011 10101 10101 10001 10001 10001",
    'N' to "10001 11001 10101 10011 10001 10001 10001",
    'O' to "01110 10001 10001 10001 10001 10001 01110",
    'P' to "11110 10001 10001 11110 10000 10000 10000",
    'Q' to "01110 10001 10001 10001 10101 10010 01101",
    'R' to "11110 10001 10001 11110 10100 10010 10001",
    'S' to "01111 10000 10000 01110 00001 00001 11110",
    'T' to "11111 00100 00100 00100 00100 00100 00100",
    'U' to "10001 10001 10001 10001 10001 10001 01110",
    'V' to "10001 10001 10001 10001 10001 01010 00100",
    'W' to "10001 10001 10001 10101 10101 11011 10001",
    'X' to "10001 10001 01010 00100 01010 10001 10001",
    'Y' to "10001 10001 01010 00100 00100 00100 00100",
    'Z' to "11111 00001 00010 00100 01000 10000 11111",
    // кириллица
    'А' to "01110 10001 10001 11111 10001 10001 10001",
    'Б' to "11111 10000 11110 10001 10001 10001 11110",
    'В' to "11110 10001 10001 11110 10001 10001 11110",
    'Г' to "11111 10000 10000 10000 10000 10000 10000",
    'Д' to "00110 01010 01010 01010 01010 11111 10001",
    'Е' to "11111 10000 11110 10000 10000 10000 11111",
    'Ё' to "10101 00000 11111 10000 11110 10000 11111",
    'Ж' to "10101 10101 11111 10101 10101 10101 10101",
    'З' to "01110 10001 00001 00110 00001 10001 01110",
    'И' to "10001 10001 10011 10101 11001 10001 10001",
    'Й' to "01010 10001 10011 10101 11001 10001 10001",
    'К' to "10001 10010 10100 11000 10100 10010 10001",
    'Л' to "00111 01001 01001 01001 01001 01001 10001",
    'М' to "10001 11011 10101 10101 10001 10001 10001",
    'Н' to "10001 10001 10001 11111 10001 10001 10001",
    'О' to "01110 10001 10001 10001 10001 10001 01110",
    'П' to "11111 10001 10001 10001 10001 10001 10001",
    'Р' to "11110 10001 10001 11110 10000 10000 10000",
    'С' to "01110 10001 10000 10000 10000 10001 01110",
    'Т' to "11111 00100 00100 00100 00100 00100 00100",
    'У' to "10001 10001 01010 00100 00100 01000 10000",
    'Ф' to "00100 01110 10101 10101 01110 00100 00100",
    'Х' to "10001 10001 01010 00100 01010 10001 10001",
    'Ц' to "10010 10010 10010 10010 10010 11111 00001",
    'Ч' to "10001 10001 10001 01111 00001 00001 00001",
    'Ш' to "10101 10101 10101 10101 10101 10101 11111",
    'Щ' to "10101 10101 10101 10101 10101 11111 00001",
    'Ъ' to "10000 10000 11110 10001 10001 10001 11110",
    'Ы' to "10001 10001 10001 11101 10011 10001 11101",
    'Ь' to "10000 10000 11110 10001 10001 10001 11110",
    'Э' to "11110 00001 00001 01111 00001 00001 11110",
    'Ю' to "10011 10101 10101 11101 10101 10101 10011",
    'Я' to "01111 10001 10001 01111 00101 01001 10001",
    // знаки
    ' ' to "00000 00000 00000 00000 00000 00000 00000",
    '.' to "00000 00000 00000 00000 00000 01100 01100",
    ',' to "00000 00000 00000 00000 01100 00100 01000",
    '!' to "00100 00100 00100 00100 00100 00000 00100",
    '?' to "01110 10001 00001 00110 00100 00000 00100",
    '-' to "00000 00000 00000 11111 00000 00000 00000",
    '+' to "00000 00100 00100 11111 00100 00100 00000",
    ':' to "00000 01100 01100 00000 01100 01100 00000",
    '/' to "00001 00010 00010 00100 01000 01000 10000",
    '(' to "00010 00100 01000 01000 01000 00100 00010",
    ')' to "01000 00100 00010 00010 00010 00100 01000",
    '•' to "00000 00000 01100 01100 01100 00000 00000"
)

private val cache = HashMap<Char, List<BooleanArray>?>()

private fun bitsOf(ch: Char): List<BooleanArray>? {
    if (cache.containsKey(ch)) return cache[ch]
    val rows = GLYPHS[ch]?.split(' ')?.takeIf { it.size == GH }
    val parsed = rows?.map { row -> BooleanArray(GW) { row[it] == '1' } }
    cache[ch] = parsed
    return parsed
}

@Composable
fun DotText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = ink,
    dotScale: Float = 0.45f
) {
    val lines = text.uppercase().split('\n')
    val longest = lines.maxOfOrNull { it.length } ?: 0
    val cols = longest * (GW + CHAR_GAP) - CHAR_GAP
    val rows = lines.size * GH + (lines.size - 1) * LINE_GAP
    if (cols <= 0 || rows <= 0) return
    val ratio = cols.toFloat() / rows.toFloat()

    Layout(
        modifier = modifier,
        content = {
            Canvas(Modifier.fillMaxSize()) {
                val pitch = minOf(size.width / cols, size.height / rows)
                val radius = pitch * dotScale
                val ox = (size.width - cols * pitch) / 2f
                val oy = (size.height - rows * pitch) / 2f
                lines.forEachIndexed { lineIndex, line ->
                    val top = lineIndex * (GH + LINE_GAP)
                    line.forEachIndexed charLoop@{ charIndex, ch ->
                        val glyph = bitsOf(ch) ?: return@charLoop
                        val left = charIndex * (GW + CHAR_GAP)
                        for (row in 0 until GH) {
                            for (col in 0 until GW) {
                                if (glyph[row][col]) {
                                    drawCircle(
                                        color = color,
                                        radius = radius,
                                        center = Offset(ox + (left + col + 0.5f) * pitch, oy + (top + row + 0.5f) * pitch)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        measurePolicy = { measurables, constraints ->
            val maxW = if (constraints.hasBoundedWidth) constraints.maxWidth else 1080
            val maxH = constraints.maxHeight
            var width = maxW
            var height = (width / ratio).roundToInt().coerceAtLeast(1)
            if (height > maxH) {
                height = maxH.coerceAtLeast(1)
                width = (height * ratio).roundToInt().coerceAtLeast(1)
            }
            val placeable = measurables.first().measure(Constraints.fixed(width, height))
            layout(width, height) { placeable.place(0, 0) }
        }
    )
}
