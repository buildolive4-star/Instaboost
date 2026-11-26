package com.example.admin.ui

import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.admin.data.ImgBbUploader
import com.example.admin.theme.AdminBorder
import com.example.admin.theme.AdminDanger
import com.example.admin.theme.AdminGreen
import com.example.admin.theme.AdminPrimary
import com.example.admin.theme.AdminPrimaryLight
import com.example.admin.theme.AdminTextSecondary
import kotlinx.coroutines.launch

@Composable
fun AdminPhotoPreview(
    photoUrl: String,
    fallbackEmoji: String,
    modifier: Modifier = Modifier,
    size: Dp? = 56.dp,
    cornerRadius: Dp = 12.dp,
    contentScale: ContentScale = ContentScale.Crop
) {
    val cleanUrl = photoUrl.trim()
    val decodedBitmap = remember(cleanUrl) {
        if (cleanUrl.startsWith("data:image", ignoreCase = true) && cleanUrl.contains(",")) {
            runCatching {
                val base64Part = cleanUrl.substringAfter(",")
                val bytes = Base64.decode(base64Part, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }.getOrNull()
        } else {
            null
        }
    }

    val boxMod = if (size != null) {
        modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(AdminPrimaryLight)
    } else {
        modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(cornerRadius))
            .background(AdminPrimaryLight)
    }

    Box(
        modifier = boxMod,
        contentAlignment = Alignment.Center
    ) {
        when {
            decodedBitmap != null -> {
                Image(
                    bitmap = decodedBitmap,
                    contentDescription = "Uploaded photo",
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
            cleanUrl.startsWith("http://", ignoreCase = true) ||
                cleanUrl.startsWith("https://", ignoreCase = true) -> {
                AsyncImage(
                    model = cleanUrl,
                    contentDescription = "Remote photo",
                    contentScale = contentScale,
                    modifier = Modifier.fillMaxSize()
                )
            }
            fallbackEmoji.isNotBlank() -> {
                Text(text = fallbackEmoji, style = MaterialTheme.typography.headlineSmall)
            }
            else -> {
                Icon(Icons.Filled.Image, contentDescription = null, tint = AdminPrimary)
            }
        }
    }
}

@Composable
fun ImgBbPhotoPickerField(
    label: String,
    photoUrl: String,
    fallbackEmoji: String,
    onPhotoChange: (String) -> Unit,
    previewSize: Dp = 68.dp,
    previewContentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var uploading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                uploading = true
                statusMessage = "Uploading to ImgBB..."
                isError = false
                ImgBbUploader.uploadImageUri(context, uri)
                    .onSuccess { url ->
                        onPhotoChange(url)
                        statusMessage = "Uploaded to ImgBB!"
                        isError = false
                    }
                    .onFailure { err ->
                        statusMessage = err.localizedMessage ?: "ImgBB upload failed"
                        isError = true
                    }
                uploading = false
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, AdminBorder)
            ) {
                AdminPhotoPreview(
                    photoUrl = photoUrl,
                    fallbackEmoji = fallbackEmoji,
                    size = previewSize,
                    cornerRadius = 14.dp,
                    contentScale = previewContentScale
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            picker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !uploading,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(40.dp)
                    ) {
                        if (uploading) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = AdminPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (ImgBbUploader.isConfigured()) Icons.Filled.CloudUpload else Icons.Filled.AddPhotoAlternate,
                                contentDescription = null,
                                tint = AdminPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (uploading) "Uploading..." else "Upload via ImgBB",
                            style = MaterialTheme.typography.labelMedium,
                            color = AdminPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (photoUrl.isNotBlank()) {
                        IconButton(
                            onClick = {
                                onPhotoChange("")
                                statusMessage = null
                                isError = false
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove photo", tint = AdminDanger)
                        }
                    }
                }
                val helperText = statusMessage ?: if (ImgBbUploader.isConfigured()) {
                    "Pick 1:1 image to host on ImgBB, or paste URL"
                } else {
                    "Add IMGBB_API_KEY in Secrets to enable ImgBB upload, or paste URL"
                }
                Text(
                    text = helperText,
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        isError -> AdminDanger
                        statusMessage == "Uploaded to ImgBB!" -> AdminGreen
                        else -> AdminTextSecondary
                    }
                )
            }
        }
        OutlinedTextField(
            value = if (photoUrl.startsWith("data:image")) "" else photoUrl,
            onValueChange = {
                onPhotoChange(it)
                isError = false
            },
            label = { Text("ImgBB / Image URL (https://...)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
