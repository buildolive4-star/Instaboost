package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DefaultCatalog
import com.example.data.SpinSegment
import com.example.data.UserProfile
import com.example.ui.components.GoldFlameCoin
import com.example.ui.components.HeaderIconButton
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.GoldCoinLight
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RaspberryLight
import com.example.ui.theme.RaspberryPrimary
import com.example.ui.theme.SoftCardBg
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AiStudioState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

@Composable
fun DailyRewardsScreen(
    profile: UserProfile,
    busy: Boolean,
    onBack: () -> Unit,
    onClaim: () -> Unit
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val claimedToday = profile.lastDailyClaimDay == today
    val nextDay = ((profile.dailyStreak % 7L) + 1L).toInt()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeaderIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                onClick = onBack,
                testTag = "daily_back_btn"
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Daily Rewards",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = GoldCoinLight,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Current Streak: ${profile.dailyStreak} / 7 Days",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF171717)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (claimedToday) "Today's reward is claimed! Come back tomorrow."
                        else "Tap Claim Now to collect Day $nextDay bonus coins.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B5313)
                    )
                }
                GoldFlameCoin(size = 46.dp)
            }
        }

        // 7-day grid
        val rows = DefaultCatalog.dailyRewards.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            rows.forEach { rowDays ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowDays.forEach { dayReward ->
                        val isCompleted = dayReward.day <= profile.dailyStreak.toInt()
                        val isCurrent = !claimedToday && dayReward.day == nextDay
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = when {
                                isCurrent -> RaspberryLight
                                isCompleted -> Color(0xFFEAF9EF)
                                else -> SoftCardBg
                            },
                            border = BorderStroke(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = when {
                                    isCurrent -> RaspberryPrimary
                                    isCompleted -> SuccessGreen
                                    else -> MaterialTheme.colorScheme.outline
                                }
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Day ${dayReward.day}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) RaspberryPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                if (isCompleted) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Claimed",
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    GoldFlameCoin(size = 32.dp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "+${dayReward.coins} Coins",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "+${dayReward.points} pts" +
                                        (dayReward.bonusLabel?.let { " • $it" } ?: ""),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    if (rowDays.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Button(
            onClick = onClaim,
            enabled = !claimedToday && !busy,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RaspberryPrimary,
                contentColor = PureWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("claim_daily_reward_btn")
        ) {
            if (busy) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = PureWhite, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.CardGiftcard, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (claimedToday) "Claimed for Today" else "Claim Day $nextDay Reward",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun SpinWheelScreen(
    profile: UserProfile,
    onBack: () -> Unit,
    onSpinComplete: (SpinSegment) -> Unit
) {
    val today = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val spinsUsed = if (profile.lastSpinDay == today) profile.spinsToday.toInt() else 0
    val spinsLeft = (3 - spinsUsed).coerceAtLeast(0)

    val segments = DefaultCatalog.spinSegments
    val sweepAngle = 360f / segments.size
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var spinning by remember { mutableStateOf(false) }
    var wonSegment by remember { mutableStateOf<SpinSegment?>(null) }

    fun pickWeightedSegmentIndex(): Int {
        val totalWeight = segments.sumOf { it.weight }
        var roll = Random.nextInt(totalWeight)
        segments.forEachIndexed { index, seg ->
            roll -= seg.weight
            if (roll < 0) return index
        }
        return 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HeaderIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                onClick = onBack,
                testTag = "spin_back_btn"
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Spin & Win",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$spinsLeft of 3 daily spins remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }

        // Pointer triangle + animated wheel
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .size(264.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(rotation.value)
                ) {
                    val radius = size.minDimension / 2f
                    val topLeft = Offset((size.width - radius * 2f) / 2f, (size.height - radius * 2f) / 2f)
                    val arcSize = Size(radius * 2f, radius * 2f)

                    segments.forEachIndexed { idx, seg ->
                        drawArc(
                            color = seg.color,
                            startAngle = -90f + idx * sweepAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = topLeft,
                            size = arcSize
                        )
                    }
                    drawCircle(
                        color = PureWhite,
                        radius = radius * 0.24f,
                        center = center
                    )
                    drawCircle(
                        color = RaspberryPrimary,
                        radius = radius * 0.16f,
                        center = center
                    )
                }
            }

            // Top pointer indicator
            Canvas(modifier = Modifier.size(28.dp)) {
                val path = Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(path, color = RaspberryPrimary)
            }
        }

        // Prize legend chips
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = SoftCardBg,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Wheel Prizes & Odds",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                segments.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        pair.forEach { seg ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(seg.color)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${seg.label} (${seg.weight}%)",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                if (spinning || spinsLeft <= 0) return@Button
                spinning = true
                val chosenIndex = pickWeightedSegmentIndex()
                val chosen = segments[chosenIndex]
                val targetAngle = (360f - (chosenIndex * sweepAngle + sweepAngle / 2f))
                val totalRotation = rotation.value - (rotation.value % 360f) + 360f * 5f + targetAngle
                scope.launch {
                    rotation.animateTo(
                        targetValue = totalRotation,
                        animationSpec = tween(
                            durationMillis = 3200,
                            easing = CubicBezierEasing(0.12f, 0.8f, 0.2f, 1f)
                        )
                    )
                    spinning = false
                    wonSegment = chosen
                    onSpinComplete(chosen)
                }
            },
            enabled = !spinning && spinsLeft > 0,
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = RaspberryPrimary,
                contentColor = PureWhite
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("spin_wheel_btn")
        ) {
            Text(
                text = when {
                    spinning -> "Spinning..."
                    spinsLeft <= 0 -> "Daily Limit Reached (0/3)"
                    else -> "Spin Now ($spinsLeft left)"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }

    wonSegment?.let { prize ->
        AlertDialog(
            onDismissRequest = { wonSegment = null },
            title = {
                Text(
                    text = if (prize.coins > 0 || prize.points > 0) "You Won ${prize.label}!" else "Almost There!",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    if (prize.coins > 0 || prize.points > 0)
                        "Added +${prize.coins} coins and +${prize.points} points to your wallet."
                    else "Better luck on your next spin!"
                )
            },
            confirmButton = {
                Button(
                    onClick = { wonSegment = null },
                    colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary)
                ) {
                    Text("Awesome!", color = PureWhite)
                }
            }
        )
    }
}

@Composable
fun ReferEarnScreen(
    profile: UserProfile,
    busy: Boolean,
    onBack: () -> Unit,
    onRedeemCode: (String, () -> Unit) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    var friendCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            HeaderIconButton(
                icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Back",
                onClick = onBack,
                testTag = "refer_back_btn"
            )
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "Refer & Earn",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = RaspberryLight,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GoldFlameCoin(size = 52.dp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Share & Earn +25 Coins",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = RaspberryPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Share your unique invite code with fellow creators. Friends get +25 bonus coins & +5 points when they apply your code.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = Color(0xFF5A2135)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PureWhite,
                    border = BorderStroke(1.5.dp, RaspberryPrimary.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = profile.referralCode.ifBlank { "CBCREATOR" },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = RaspberryPrimary,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(profile.referralCode))
                            onShowMessage("Referral code copied!")
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("copy_referral_btn")
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Code")
                    }
                    Button(
                        onClick = {
                            val shareText = "Join me on Fame Maker for trending hashtags, captions & AI bio tools! Use my code ${profile.referralCode} for bonus coins."
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Invite Friends"))
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary),
                        modifier = Modifier.testTag("share_referral_btn")
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = PureWhite)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share", color = PureWhite)
                    }
                }
            }
        }

        // Redeem friend's code
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Have a Friend's Code?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (profile.referredBy.isNotBlank()) {
                    Text(
                        text = "Already redeemed code: ${profile.referredBy}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SuccessGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    OutlinedTextField(
                        value = friendCode,
                        onValueChange = { friendCode = it.uppercase(Locale.US) },
                        label = { Text("Enter Referral Code") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("redeem_code_input")
                    )
                    Button(
                        onClick = { onRedeemCode(friendCode) { friendCode = "" } },
                        enabled = !busy && friendCode.trim().length >= 4,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RaspberryPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("redeem_code_btn")
                    ) {
                        Text("Redeem +25 Coins", color = PureWhite, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AiStudioScreen(
    aiState: AiStudioState,
    onBack: () -> Unit,
    onGenerate: (toolType: String, topic: String, tone: String, count: Int) -> Unit,
    onSaveFavorite: (title: String, category: String, content: String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val toolTypes = listOf("HASHTAGS" to "Hashtags", "CAPTIONS" to "Captions", "BIOS" to "Creator Bios", "IDEAS" to "Reel Hooks")
    val tones = listOf("Trendy", "Aesthetic", "Witty", "Professional", "Bold")

    var selectedTool by remember { mutableStateOf("HASHTAGS") }
    var selectedTone by remember { mutableStateOf("Trendy") }
    var topic by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 104.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HeaderIconButton(
                    icon = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Back",
                    onClick = onBack,
                    testTag = "ai_studio_back_btn"
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "AI Creator Studio",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Generate tailored hashtags, captions & bios",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                toolTypes.forEach { (key, label) ->
                    FilterChip(
                        selected = selectedTool == key,
                        onClick = { selectedTool = key },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RaspberryPrimary,
                            selectedLabelColor = PureWhite
                        )
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = topic,
                onValueChange = { topic = it },
                label = { Text("Topic or Niche (e.g. Street Food, Travel Vlog, Fitness)") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RaspberryPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_topic_input")
            )
        }

        item {
            Text(
                text = "Tone / Vibe",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tones.forEach { tone ->
                    FilterChip(
                        selected = selectedTone == tone,
                        onClick = { selectedTone = tone },
                        label = { Text(tone) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RaspberryLight,
                            selectedLabelColor = RaspberryPrimary
                        )
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    val count = if (selectedTool == "HASHTAGS") 20 else 8
                    onGenerate(selectedTool, topic, selectedTone, count)
                },
                enabled = !aiState.loading && topic.trim().isNotBlank(),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RaspberryPrimary,
                    contentColor = PureWhite
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("ai_generate_btn")
            ) {
                if (aiState.loading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = PureWhite, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Generating...", fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Now", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (aiState.error != null) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = aiState.error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        if (aiState.generatedLines.isNotEmpty()) {
            item {
                val combined = if (selectedTool == "HASHTAGS") {
                    aiState.generatedLines.joinToString(" ")
                } else {
                    aiState.generatedLines.joinToString("\n\n")
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SoftCardBg,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = aiState.generatedTitle.ifBlank { "Generated Results" },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(combined))
                                    onShowMessage("Copied all generated items!")
                                }
                            ) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy All", tint = RaspberryPrimary)
                            }
                            IconButton(
                                onClick = {
                                    onSaveFavorite(
                                        aiState.generatedTitle.ifBlank { "AI $selectedTool" },
                                        topic.ifBlank { "AI Studio" },
                                        combined
                                    )
                                }
                            ) {
                                Icon(Icons.Filled.FavoriteBorder, contentDescription = "Save Favorite", tint = RaspberryPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (selectedTool == "HASHTAGS") {
                            Text(
                                text = combined,
                                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 25.sp)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                aiState.generatedLines.forEach { line ->
                                    Surface(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(line))
                                            onShowMessage("Copied line!")
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = line,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(
                                                Icons.Filled.ContentCopy,
                                                contentDescription = "Copy",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
