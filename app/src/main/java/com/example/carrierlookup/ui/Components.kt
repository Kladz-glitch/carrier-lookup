package com.example.carrierlookup.ui

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.carrierlookup.ui.theme.AppColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** La manina che saluta: oscilla per un po' e poi si ferma, in ciclo. */
@Composable
fun WavingHand() {
    val transition = rememberInfiniteTransition(label = "wave")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2400
                0f at 0
                18f at 150
                -12f at 330
                18f at 510
                -10f at 690
                14f at 870
                0f at 1050
                0f at 2400
            }
        ),
        label = "waveAngle"
    )
    Text(
        "👋",
        fontSize = 26.sp,
        modifier = Modifier.graphicsLayer {
            rotationZ = angle
            transformOrigin = TransformOrigin(0.7f, 0.85f)
        }
    )
}

/** Scheda scura con bordo sottile. */
@Composable
fun AppCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AppColors.Card,
        border = BorderStroke(1.dp, AppColors.Border),
        content = content
    )
}

/** Pulsantino a pillola (Incolla, Contatti…). */
@Composable
fun ActionChip(label: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = AppColors.Card,
        border = BorderStroke(1.dp, AppColors.Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = AppColors.TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(label, color = AppColors.TextPrimary, style = MaterialTheme.typography.labelLarge)
        }
    }
}

private val avatarColors = listOf(
    Color(0xFFE5484D), Color(0xFFF76808), Color(0xFF2F6BFF), Color(0xFF12A594),
    Color(0xFF8E4EC6), Color(0xFFD6409F), Color(0xFF3E63DD)
)

/** Cerchio colorato con l'iniziale dell'operatore (niente loghi di marchi). */
@Composable
fun CarrierAvatar(name: String?, size: Int = 48) {
    val label = name?.trim().orEmpty()
    val color = if (label.isEmpty()) AppColors.CardHigh
    else avatarColors[(label.lowercase().hashCode() and 0x7fffffff) % avatarColors.size]
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.SimCard,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size((size * 0.55f).dp)
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = AppColors.TextPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = AppColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.weight(1f))
        Text(
            value,
            color = valueColor,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun HLine() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(AppColors.Border)
    )
}

// ---- Date e crediti ---------------------------------------------------------

private val zone: ZoneId get() = ZoneId.systemDefault()
private val timeFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
private val dayFmt: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

fun dateOf(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
fun timeText(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(timeFmt)
fun dayText(millis: Long): String = Instant.ofEpochMilli(millis).atZone(zone).format(dayFmt)

fun countToday(times: List<Long>): Int {
    val today = LocalDate.now()
    return times.count { dateOf(it) == today }
}

fun countThisMonth(times: List<Long>): Int {
    val now = LocalDate.now()
    return times.count { val d = dateOf(it); d.year == now.year && d.month == now.month }
}

/** "oggi 14:32", "ieri 09:10" oppure "12/10 18:00". */
fun lastSearchText(times: List<Long>, s: Strings): String {
    val last = times.maxOrNull() ?: return s.none
    val d = dateOf(last)
    val today = LocalDate.now()
    val day = when (d) {
        today -> s.today.lowercase()
        today.minusDays(1) -> s.yesterday
        else -> dayText(last)
    }
    return "$day ${timeText(last)}"
}

val ScreenPadding = PaddingValues(horizontal = 16.dp)

/** Opzione selezionabile a pillola (es. lingua). */
@Composable
fun OptionPill(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) AppColors.Accent else AppColors.CardHigh
    ) {
        Box(Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(
                label,
                color = if (selected) Color.White else AppColors.TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
