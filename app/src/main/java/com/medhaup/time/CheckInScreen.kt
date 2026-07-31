package com.medhaup.time

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.medhaup.time.data.TimeEntry
import com.medhaup.time.data.TimeTrackerRepository
import com.medhaup.time.ui.theme.*
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())

private val dateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEEE, MMMM d").withZone(ZoneId.systemDefault())

private val SuccessGreenDark = Color(0xFF15803D)

private fun Duration.formatHms(): String =
    "%02d:%02d:%02d".format(toHours(), toMinutesPart(), toSecondsPart())

private fun Duration.formatShort(): String {
    val h = toHours()
    val m = toMinutesPart()
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}

@Composable
fun CheckInScreen(onSignedOut: () -> Unit) {
    val scope = rememberCoroutineScope()

    var todayEntries by remember { mutableStateOf<List<TimeEntry>>(emptyList()) }
    var openEntry by remember { mutableStateOf<TimeEntry?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isBusy by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var now by remember { mutableStateOf(Instant.now()) }

    val userEmail = supabase.auth.currentUserOrNull()?.email ?: ""
    val userName = userEmail.substringBefore("@")
        .replaceFirstChar { it.uppercase() }

    suspend fun refresh() {
        try {
            errorMessage = null
            openEntry = TimeTrackerRepository.getOpenEntry()
            todayEntries = TimeTrackerRepository.getTodayEntries()
        } catch (e: Exception) {
            errorMessage = e.message ?: "Couldn't load your entries"
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { refresh() }

    // Tick every second (drives the live timer and clock)
    LaunchedEffect(Unit) {
        while (true) {
            now = Instant.now()
            delay(1_000)
        }
    }

    val checkedIn = openEntry != null
    val totalToday = todayEntries.fold(Duration.ZERO) { acc, e -> acc + e.duration(now) }

    // Smooth color transition for the big button
    val circleTop by animateColorAsState(
        targetValue = if (checkedIn) SuccessGreen else BurntOrangeLight,
        animationSpec = tween(600), label = "circleTop"
    )
    val circleBottom by animateColorAsState(
        targetValue = if (checkedIn) SuccessGreenDark else BurntOrange,
        animationSpec = tween(600), label = "circleBottom"
    )

    // Soft pulsing ring while checked in
    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.28f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart),
        label = "pulseScale"
    )
    val pulseAlpha by pulse.animateFloat(
        initialValue = 0.30f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart),
        label = "pulseAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Header ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                .background(Brush.verticalGradient(listOf(NavyBlue, NavyBlueDark)))
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 28.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar with initial
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(PureWhite.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Hi, $userName 👋",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PureWhite
                    )
                    Text(
                        text = dateFormatter.format(now),
                        fontSize = 13.sp,
                        color = PureWhite.copy(alpha = 0.65f)
                    )
                }
                IconButton(onClick = {
                    scope.launch {
                        try {
                            supabase.auth.signOut()
                        } catch (_: Exception) { }
                        onSignedOut()
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Sign out",
                        tint = PureWhite.copy(alpha = 0.75f)
                    )
                }
            }
        }

        if (isLoading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NavyBlue)
            }
            return
        }

        Spacer(Modifier.height(36.dp))

        // ── Big circular check-in button ────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulsing ring (only while checked in)
            if (checkedIn) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer {
                            scaleX = pulseScale
                            scaleY = pulseScale
                            alpha = pulseAlpha
                        }
                        .clip(CircleShape)
                        .background(SuccessGreen)
                )
            }

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .shadow(16.dp, CircleShape, spotColor = circleBottom)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(circleTop, circleBottom)))
                    .clickable(enabled = !isBusy) {
                        scope.launch {
                            isBusy = true
                            errorMessage = null
                            try {
                                val open = openEntry
                                if (open == null) {
                                    openEntry = TimeTrackerRepository.checkIn()
                                } else {
                                    TimeTrackerRepository.checkOut(open.id)
                                    openEntry = null
                                }
                                todayEntries = TimeTrackerRepository.getTodayEntries()
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Something went wrong"
                            } finally {
                                isBusy = false
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isBusy) {
                    CircularProgressIndicator(color = PureWhite, strokeWidth = 3.dp)
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        if (checkedIn) {
                            Text(
                                text = openEntry!!.duration(now).formatHms(),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = PureWhite
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Tap to check out",
                                fontSize = 13.sp,
                                color = PureWhite.copy(alpha = 0.85f)
                            )
                        } else {
                            Text(
                                text = "CHECK IN",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = PureWhite
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = timeFormatter.format(now),
                                fontSize = 14.sp,
                                color = PureWhite.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = ErrorRed,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp)
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── Stat chips ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatChip(
                label = "Checked in at",
                value = openEntry?.let { timeFormatter.format(it.checkInInstant) } ?: "—",
                modifier = Modifier.weight(1f)
            )
            StatChip(
                label = "Total today",
                value = totalToday.formatShort(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(28.dp))

        // ── Today's log ─────────────────────────────────────────
        Text(
            text = "Today's activity",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(Modifier.height(12.dp))

        if (todayEntries.isEmpty()) {
            Text(
                text = "No entries yet — tap the button to start your day",
                fontSize = 13.sp,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(horizontal = 24.dp)
            ) {
                todayEntries.forEach { entry ->
                    EntryRow(entry = entry, now = now)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun EntryRow(entry: TimeEntry, now: Instant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status dot
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (entry.isOpen) SuccessGreen else NavyBlue.copy(alpha = 0.35f))
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = timeFormatter.format(entry.checkInInstant) + "  →  " +
                            (entry.checkOutInstant?.let { timeFormatter.format(it) } ?: "now"),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
                if (entry.isOpen) {
                    Text(
                        text = "In progress",
                        fontSize = 12.sp,
                        color = SuccessGreen
                    )
                }
            }
            // Duration pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (entry.isOpen) SuccessGreen.copy(alpha = 0.12f)
                        else NavyBlue.copy(alpha = 0.06f)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = entry.duration(now).formatShort(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (entry.isOpen) SuccessGreenDark else NavyBlue
                )
            }
        }
    }
}