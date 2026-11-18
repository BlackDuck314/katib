package app.katib.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.katib.R
import app.katib.asr.Models

@Composable
fun RecordScreen(vm: AppVm, onSettings: () -> Unit, onOpen: (Long) -> Unit) {
    val rec by vm.rec.collectAsState()
    val consent by vm.consent.collectAsState()
    val title by vm.title.collectAsState()
    val personal by vm.personal.collectAsState()
    val withLoc by vm.withLocation.collectAsState()
    val ctx = LocalContext.current
    var showConsent by remember { mutableStateOf(false) }
    var permDenied by remember { mutableStateOf(false) }
    val tick by vm.modelTick.collectAsState()
    val hasAsr = remember(tick) { Models.ASR.any { Models.installed(ctx, it) } }
    val defaultTitle = stringResource(R.string.untitled_meeting) + " · " + fmtDate(System.currentTimeMillis())

    val perms = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        if (res[Manifest.permission.RECORD_AUDIO] == true) {
            permDenied = false
            val loc = res[Manifest.permission.ACCESS_FINE_LOCATION] == true || res[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
                androidx.core.content.ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
            vm.startRecording(defaultTitle, loc)
        }
        else permDenied = true
    }
    fun start() {
        val list = buildList {
            add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
            if (withLoc) { add(Manifest.permission.ACCESS_FINE_LOCATION); add(Manifest.permission.ACCESS_COARSE_LOCATION) }
        }
        perms.launch(list.toTypedArray())
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Header(stringResource(R.string.app_name), onSettings = onSettings)
        // Persistent privacy cues
        Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip(stringResource(R.string.on_device_only), dot = E.c.ok)
            Chip(stringResource(R.string.sync_off))
            Chip(stringResource(R.string.audio_kept_days, vm.prefs.retentionDays))
        }
        Spacer(Modifier.height(24.dp))

        if (!rec.recording) {
            // Mode: consent-gated meeting vs personal note (owner only)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                listOf(false to R.string.mode_meeting, true to R.string.mode_note).forEachIndexed { i, (v, r) ->
                    SegmentedButton(selected = personal == v, onClick = { vm.personal.value = v },
                        shape = SegmentedButtonDefaults.itemShape(i, 2),
                        colors = SegmentedButtonDefaults.colors(activeContainerColor = E.c.accent, activeContentColor = E.c.onAccent,
                            inactiveContainerColor = E.c.paper, activeBorderColor = E.c.accent, inactiveBorderColor = E.c.line)) {
                        Text(stringResource(r))
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = title, onValueChange = { vm.title.value = it },
                label = { Text(stringResource(R.string.meeting_title_hint)) }, singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(16.dp))
            if (personal) {
                Row(Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .border(1.dp, E.c.line, RoundedCornerShape(12.dp)).padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.EditNote, null, tint = E.c.accent)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.note_explainer), style = MaterialTheme.typography.bodyMedium, color = E.c.muted)
                }
            } else ConsentCard(consent) { showConsent = true }
            Row(Modifier.fillMaxWidth().clickable { vm.withLocation.value = !withLoc }.padding(horizontal = 20.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.LocationOn, null, tint = if (withLoc) E.c.accent else E.c.muted)
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.attach_location), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Switch(withLoc, { vm.withLocation.value = it }, colors = SwitchDefaults.colors(checkedTrackColor = E.c.accent))
            }
        }

        Spacer(Modifier.height(if (rec.recording) 24.dp else 40.dp))
        val stateText = when {
            rec.recording && rec.paused -> stringResource(R.string.paused)
            rec.recording -> stringResource(R.string.recording)
            else -> stringResource(R.string.ready_to_record)
        }
        Text(stateText.uppercase(), style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 2.sp),
            color = if (rec.recording && !rec.paused) E.c.rec else E.c.muted,
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(fmtClock(rec.elapsedMs / 1000f),
            style = TextStyle(fontFamily = PlexMono, fontSize = 56.sp, color = E.c.ink),
            modifier = Modifier.align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(12.dp))
        LevelMeter(rec.levels, active = rec.recording && !rec.paused,
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 28.dp))
        Spacer(Modifier.height(28.dp))

        val enabled = rec.recording || consent != null || personal
        val bg by animateColorAsState(if (!enabled) E.c.line else E.c.rec, label = "rec")
        Row(Modifier.align(Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(28.dp)) {
            if (rec.recording) {
                FilledTonalIconButton(onClick = { if (rec.paused) vm.resume() else vm.pause() }, modifier = Modifier.size(56.dp)) {
                    Icon(if (rec.paused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                        stringResource(if (rec.paused) R.string.resume else R.string.pause))
                }
            }
            val desc = stringResource(if (rec.recording) R.string.stop_recording else R.string.start_recording)
            Box(
                Modifier.size(104.dp).clip(CircleShape).border(4.dp, bg.copy(alpha = 0.35f), CircleShape).padding(8.dp)
                    .clip(CircleShape).background(bg)
                    .clickable(enabled = enabled) { if (rec.recording) vm.stopRecording() else start() }
                    .semantics { contentDescription = desc },
                contentAlignment = Alignment.Center,
            ) {
                if (rec.recording) Box(Modifier.size(30.dp).clip(RoundedCornerShape(6.dp)).background(E.c.paper))
                else Icon(Icons.Outlined.Mic, null, tint = E.c.paper, modifier = Modifier.size(38.dp))
            }
            if (rec.recording) Spacer(Modifier.size(56.dp))
        }
        Spacer(Modifier.height(20.dp))
        if (permDenied) Text(stringResource(R.string.mic_permission), color = E.c.rec,
            modifier = Modifier.padding(horizontal = 20.dp).align(Alignment.CenterHorizontally))
        if (!hasAsr && !rec.recording) {
            Column(Modifier.padding(20.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(E.c.surface).padding(16.dp)) {
                Text(stringResource(R.string.model_missing), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onSettings) { Text(stringResource(R.string.open_models)) }
            }
        }
    }

    if (showConsent) ConsentDialog(withLoc, onDismiss = { showConsent = false }) { p, m -> vm.consent.value = ConsentDraft(p, m); showConsent = false }
}

@Composable
private fun ConsentCard(consent: ConsentDraft?, onClick: () -> Unit) {
    val ok = consent != null
    Row(
        Modifier.padding(horizontal = 20.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp))
            .border(1.dp, if (ok) E.c.ok else E.c.line, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(if (ok) Icons.Outlined.VerifiedUser else Icons.Outlined.Groups, null, tint = if (ok) E.c.ok else E.c.ink)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(if (ok) stringResource(R.string.consent_confirmed, consent!!.method) else stringResource(R.string.consent_required),
                style = MaterialTheme.typography.titleSmall)
            if (ok) Text(consent!!.participants, style = MaterialTheme.typography.bodyMedium, color = E.c.muted, maxLines = 2)
        }
        if (!ok) Text(stringResource(R.string.confirm_consent), color = E.c.accent, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun ConsentDialog(withLocation: Boolean, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var participants by remember { mutableStateOf("") }
    val methods = listOf(stringResource(R.string.method_verbal), stringResource(R.string.method_written), stringResource(R.string.method_notice))
    var method by remember { mutableStateOf(methods[0]) }
    var checked by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss, containerColor = E.c.paper,
        title = { Text(stringResource(R.string.consent_title)) },
        text = {
            Column {
                Text(stringResource(R.string.consent_body), style = MaterialTheme.typography.bodyMedium, color = E.c.muted)
                if (withLocation) Text(stringResource(R.string.consent_location), style = MaterialTheme.typography.bodyMedium,
                    color = E.c.ink, modifier = Modifier.padding(top = 6.dp))
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(participants, { participants = it }, label = { Text(stringResource(R.string.participants_hint)) },
                    modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    methods.forEach { m -> FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) }) }
                }
                Row(Modifier.clickable { checked = !checked }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked, { checked = it })
                    Text(stringResource(R.string.consent_check), style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(participants.trim(), method) }, enabled = checked && participants.isNotBlank()) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun LevelMeter(levels: List<Float>, active: Boolean, modifier: Modifier) {
    val ink = if (active) E.c.ink else E.c.line
    val line = E.c.line
    Canvas(modifier) {
        val bars = 60
        val w = size.width / bars
        drawLine(line, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), 1f)
        val data = levels.takeLast(bars)
        data.forEachIndexed { i, db ->
            val norm = ((db + 60f) / 60f).coerceIn(0.03f, 1f)
            val h = norm * size.height
            drawRoundRect(ink, Offset(i * w + w * 0.2f, (size.height - h) / 2), Size(w * 0.6f, h), CornerRadius(w))
        }
    }
}
