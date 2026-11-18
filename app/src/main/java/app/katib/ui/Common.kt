package app.katib.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.katib.R
import java.text.DateFormat
import java.util.Date

/** Screen header: large display-serif title on the canvas, hairline beneath. */
@Composable
fun Header(
    title: String, onBack: (() -> Unit)? = null, onSettings: (() -> Unit)? = null, mark: Boolean = false,
    eyebrow: String? = null, actions: @Composable RowScope.() -> Unit = {},
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().padding(start = if (onBack != null) Space.xs else Space.gutter, end = Space.s, top = Space.m),
            verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "back") }
            if (mark) { EngravedLogo(Modifier.size(34.dp)); Spacer(Modifier.width(Space.s + 2.dp)) }
            Column(Modifier.weight(1f)) {
                eyebrow?.let { Text(it.uppercase(), style = labelStyle, maxLines = 1) }
                Text(title, style = if (onBack != null) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            actions()
            if (onSettings != null) IconButton(onClick = onSettings) { Icon(Icons.Outlined.Settings, "settings", tint = E.c.muted) }
        }
        Spacer(Modifier.height(Space.s))
    }
}

/** The vector mark, tinted with the current ink. */
@Composable
fun KatibMark(modifier: Modifier = Modifier, tint: Color = E.c.ink) {
    Icon(painterResource(R.drawable.ic_katib_mark), null, modifier, tint = tint)
}

/** The full engraved logo (alpha mask), tinted so it works on paper and night. */
@Composable
fun EngravedLogo(modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.logo_engraved), null, modifier, colorFilter = ColorFilter.tint(E.c.ink))
}

/** Tag: squared, hairline, mono caps. Replaces rounded pill chips. */
@Composable
fun Chip(text: String, color: Color = E.c.muted, dot: Color? = null) {
    Row(
        Modifier.clip(RoundedCornerShape(Radius.tag)).border(Stroke.hairline, E.c.line, RoundedCornerShape(Radius.tag))
            .padding(horizontal = Space.s, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (dot != null) { Box(Modifier.size(6.dp).clip(CircleShape).background(dot)); Spacer(Modifier.width(6.dp)) }
        Text(text.uppercase(), style = labelStyle.copy(color = color, fontSize = 10.sp), maxLines = 1)
    }
}

/** Ledger section label: "— MODELS" in tracked mono, with a rule. */
@Composable
fun SectionLabel(title: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(horizontal = Space.gutter), verticalAlignment = Alignment.CenterVertically) {
        Text(title.uppercase(), style = labelStyle)
        Spacer(Modifier.width(Space.s))
        HorizontalDivider(Modifier.weight(1f), thickness = Stroke.hairline, color = E.c.line)
    }
}

/** Raised panel with hairline border and the card radius. */
@Composable
fun Panel(modifier: Modifier = Modifier, border: Color = E.c.line, fill: Color = E.c.paper, pad: Dp = Space.m,
          content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.clip(RoundedCornerShape(Radius.card)).background(fill)
        .border(Stroke.hairline, border, RoundedCornerShape(Radius.card)).padding(pad), content = content)
}

private val btnShape = RoundedCornerShape(Radius.card)

/** Primary action: solid ink, squared. */
@Composable
fun KButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) =
    Button(onClick, modifier, enabled, shape = btnShape, contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp), content = content)

@Composable
fun KOutlinedButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
                    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(contentColor = E.c.ink),
                    content: @Composable RowScope.() -> Unit) =
    OutlinedButton(onClick, modifier, enabled, shape = btnShape, colors = colors,
        border = androidx.compose.foundation.BorderStroke(Stroke.hairline, if (enabled) colors.contentColor else E.c.line), content = content)

@Composable
fun kSwitchColors() = SwitchDefaults.colors(
    checkedThumbColor = E.c.paper, checkedTrackColor = E.c.ink, checkedBorderColor = E.c.ink,
    uncheckedThumbColor = E.c.muted, uncheckedTrackColor = E.c.paper, uncheckedBorderColor = E.c.line,
)

@Composable
fun kSliderColors() = SliderDefaults.colors(thumbColor = E.c.ink, activeTrackColor = E.c.ink, inactiveTrackColor = E.c.line,
    activeTickColor = E.c.paper, inactiveTickColor = E.c.muted)

val monoStyle @Composable get() = TextStyle(fontFamily = PlexMono, fontSize = 13.sp, color = E.c.muted)

fun fmtClock(sec: Float): String {
    val s = sec.toInt(); val h = s / 3600; val m = s % 3600 / 60; val ss = s % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, ss) else "%02d:%02d".format(m, ss)
}

fun fmtDate(ms: Long): String = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(ms))

@Composable
fun speakerColor(idx: Int): Color = if (idx < 0) E.c.muted else E.c.speakers[idx % E.c.speakers.size]
