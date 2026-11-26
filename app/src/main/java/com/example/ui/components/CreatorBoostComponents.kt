package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.asImageBitmap
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ServiceCategoryUi
import com.example.ui.theme.BlueStar
import com.example.ui.theme.GoldCoin
import com.example.ui.theme.GoldCoinDeep
import com.example.ui.theme.GoldCoinLight
import com.example.ui.theme.LevelGreen
import com.example.ui.theme.PureWhite
import com.example.ui.theme.RaspberryLight
import com.example.ui.theme.RaspberryPrimary
import com.example.ui.theme.SoftCardBg
import com.example.ui.theme.SubtleBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WarmCoinTileBg

@Composable
fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    testTag: String = "header_icon_btn"
) {
    Box(modifier = modifier) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .size(48.dp)
                .testTag(testTag)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(RaspberryPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (badgeCount > 9) "9+" else badgeCount.toString(),
                    color = PureWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GoldFlameCoin(
    size: Dp = 32.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFFFFE57F), GoldCoin, GoldCoinDeep)
                )
            )
            .border(width = (size * 0.08f).coerceAtLeast(1.5.dp), color = Color(0xFFFFF1A8), shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.LocalFireDepartment,
            contentDescription = null,
            tint = Color(0xFFFFF8D6),
            modifier = Modifier.size(size * 0.56f)
        )
    }
}

@Composable
fun CoinStackTile(
    stackCount: Int,
    tileSize: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(tileSize)
            .clip(RoundedCornerShape(18.dp))
            .background(WarmCoinTileBg),
        contentAlignment = Alignment.Center
    ) {
        when (stackCount.coerceIn(1, 4)) {
            1 -> GoldFlameCoin(size = tileSize * 0.62f)
            2 -> {
                GoldFlameCoin(size = tileSize * 0.48f, modifier = Modifier.offset(x = (-7).dp, y = (-5).dp))
                GoldFlameCoin(size = tileSize * 0.48f, modifier = Modifier.offset(x = 7.dp, y = 6.dp))
            }
            3 -> {
                GoldFlameCoin(size = tileSize * 0.42f, modifier = Modifier.offset(x = 0.dp, y = (-9).dp))
                GoldFlameCoin(size = tileSize * 0.42f, modifier = Modifier.offset(x = (-10).dp, y = 8.dp))
                GoldFlameCoin(size = tileSize * 0.42f, modifier = Modifier.offset(x = 10.dp, y = 8.dp))
            }
            else -> {
                GoldFlameCoin(size = tileSize * 0.38f, modifier = Modifier.offset(x = (-9).dp, y = (-9).dp))
                GoldFlameCoin(size = tileSize * 0.38f, modifier = Modifier.offset(x = 9.dp, y = (-9).dp))
                GoldFlameCoin(size = tileSize * 0.38f, modifier = Modifier.offset(x = (-9).dp, y = 9.dp))
                GoldFlameCoin(size = tileSize * 0.38f, modifier = Modifier.offset(x = 9.dp, y = 9.dp))
            }
        }
    }
}

@Composable
fun BlueStarCoin(
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(colors = listOf(Color(0xFF6FA3FF), BlueStar))
            )
            .border(width = (size * 0.1f).coerceAtLeast(1.5.dp), color = GoldCoin, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = GoldCoin,
            modifier = Modifier.size(size * 0.62f)
        )
    }
}

@Composable
fun LevelGreenBadge(
    size: Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                Brush.linearGradient(colors = listOf(Color(0xFF34D399), LevelGreen))
            )
            .border(width = 3.dp, color = GoldCoin, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Lv",
            color = Color(0xFFFFF3B0),
            fontWeight = FontWeight.ExtraBold,
            fontSize = (size.value * 0.36f).sp
        )
    }
}

@Composable
fun LevelCrownPill(
    level: Long,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        shape = RoundedCornerShape(24.dp),
        color = RaspberryLight,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, RaspberryPrimary.copy(alpha = 0.45f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.WorkspacePremium,
                    contentDescription = null,
                    tint = RaspberryPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Level $level",
                style = MaterialTheme.typography.labelLarge,
                color = RaspberryPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BonusCoinChip(text: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = GoldCoinLight
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            GoldFlameCoin(size = 16.dp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF7A5300),
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun PointsStarChip(text: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = SoftCardBg,
        border = BorderStroke(1.dp, SubtleBorder)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            BlueStarCoin(size = 16.dp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun DynamicPhotoImage(
    photoUrl: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    fallbackRes: Int? = null,
    fallbackContent: @Composable () -> Unit = {}
) {
    val trimmed = photoUrl.trim()
    if (trimmed.startsWith("data:image", ignoreCase = true) && trimmed.contains("base64,")) {
        val bitmap = remember(trimmed) {
            try {
                val raw = trimmed.substringAfter("base64,")
                val bytes = Base64.decode(raw, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
            return
        }
    } else if (trimmed.startsWith("http://", ignoreCase = true) || trimmed.startsWith("https://", ignoreCase = true)) {
        AsyncImage(
            model = trimmed,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        return
    }

    if (fallbackRes != null) {
        Image(
            painter = painterResource(id = fallbackRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        fallbackContent()
    }
}

@Composable
fun CategoryTileCard(
    category: ServiceCategoryUi,
    packCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(0.76f)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .testTag("category_card_${category.id}")
    ) {
        DynamicPhotoImage(
            photoUrl = category.photoUrl,
            contentDescription = category.name,
            fallbackRes = category.imageRes,
            modifier = Modifier.fillMaxSize(),
            fallbackContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(category.gradientStart, category.gradientEnd)
                            )
                        )
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.14f),
                            radius = size.width * 0.55f,
                            center = Offset(size.width * 0.85f, size.height * 0.2f)
                        )
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.12f),
                            radius = size.width * 0.5f,
                            center = Offset(size.width * 0.15f, size.height * 0.9f)
                        )
                    }
                    Text(
                        text = category.emoji,
                        fontSize = 36.sp,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(y = 10.dp)
                    )
                }
            }
        )

        // Top dark scrim for crisp readable label just like screenshot
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.62f), Color.Transparent)
                    )
                )
        )

        Text(
            text = category.name,
            color = PureWhite,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(horizontal = 6.dp, vertical = 10.dp)
        )

        if (packCount > 0) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.Black.copy(alpha = 0.45f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "$packCount ${if (packCount == 1) "pack" else "packs"}",
                    color = PureWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun FloatingMascotCompanion(
    visible: Boolean,
    message: String,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    var showBubble by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "mascot_float")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mascot_bob"
    )

    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        AnimatedVisibility(
            visible = showBubble,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 6.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.padding(bottom = 6.dp, end = 4.dp)
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Surface(
            onClick = { showBubble = !showBubble },
            shape = CircleShape,
            color = PureWhite,
            shadowElevation = 6.dp,
            border = BorderStroke(2.dp, RaspberryLight),
            modifier = Modifier
                .offset(y = floatY.dp)
                .size(56.dp)
                .testTag("mascot_companion_button")
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_mascot_flame_1795714394908),
                contentDescription = "Fame Maker Mascot",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .clip(CircleShape)
            )
        }
    }
}
