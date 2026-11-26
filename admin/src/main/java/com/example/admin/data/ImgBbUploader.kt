package com.example.admin.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.example.admin.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.math.max
import kotlin.math.roundToInt

object ImgBbUploader {

    private const val PLACEHOLDER_KEY = "IMGBB_API_KEY_DEFAULT_VALUE"
    private const val MAX_DIMENSION = 900
    private const val JPEG_QUALITY = 85

    fun isConfigured(): Boolean {
        val key = BuildConfig.IMGBB_API_KEY.trim()
        return key.isNotEmpty() && key != PLACEHOLDER_KEY
    }

    suspend fun uploadImageUri(context: Context, uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.IMGBB_API_KEY.trim()
        if (apiKey.isEmpty() || apiKey == PLACEHOLDER_KEY) {
            return@withContext Result.failure(
                IllegalStateException(
                    "ImgBB API key is not configured yet. Add IMGBB_API_KEY in Secrets and rebuild."
                )
            )
        }

        val bitmap = decodeScaledBitmap(context, uri)
            ?: return@withContext Result.failure(IllegalArgumentException("Could not read selected image"))

        val base64Image = try {
            val out = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
        } finally {
            bitmap.recycle()
        }

        uploadBase64(apiKey, base64Image)
    }

    private fun decodeScaledBitmap(context: Context, uri: Uri): Bitmap? {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, boundsOpts)
        }
        val origW = boundsOpts.outWidth
        val origH = boundsOpts.outHeight
        if (origW <= 0 || origH <= 0) return null

        var sampleSize = 1
        while (origW / sampleSize > MAX_DIMENSION * 2 || origH / sampleSize > MAX_DIMENSION * 2) {
            sampleSize *= 2
        }

        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val decoded = context.contentResolver.openInputStream(uri)?.use { input ->
            BitmapFactory.decodeStream(input, null, decodeOpts)
        } ?: return null

        val maxSide = max(decoded.width, decoded.height)
        if (maxSide <= MAX_DIMENSION) return decoded

        val scale = MAX_DIMENSION.toFloat() / maxSide.toFloat()
        val targetW = max(1, (decoded.width * scale).roundToInt())
        val targetH = max(1, (decoded.height * scale).roundToInt())
        val scaled = Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
        if (scaled != decoded) decoded.recycle()
        return scaled
    }

    private fun uploadBase64(apiKey: String, base64Image: String): Result<String> {
        val endpoint = "https://api.imgbb.com/1/upload?key=${URLEncoder.encode(apiKey, "UTF-8")}"
        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 20_000
            readTimeout = 30_000
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        }

        return try {
            val formBody = "image=${URLEncoder.encode(base64Image, "UTF-8")}"
            connection.outputStream.use { os ->
                os.write(formBody.toByteArray(Charsets.UTF_8))
                os.flush()
            }

            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()

            if (statusCode !in 200..299) {
                val errMsg = runCatching {
                    JSONObject(body).optJSONObject("error")?.optString("message").orEmpty()
                }.getOrDefault("")
                Result.failure(
                    IllegalStateException(
                        errMsg.ifBlank { "ImgBB upload failed (HTTP $statusCode)" }
                    )
                )
            } else {
                val json = JSONObject(body)
                if (!json.optBoolean("success", false)) {
                    Result.failure(IllegalStateException("ImgBB rejected image upload"))
                } else {
                    val data = json.optJSONObject("data")
                    val url = data?.optString("display_url").orEmpty()
                        .ifBlank { data?.optString("url").orEmpty() }
                    if (url.isBlank()) {
                        Result.failure(IllegalStateException("ImgBB did not return an image URL"))
                    } else {
                        Result.success(url)
                    }
                }
            }
        } catch (e: Exception) {
            Result.failure(IllegalStateException(e.message ?: "Network error while uploading to ImgBB"))
        } finally {
            connection.disconnect()
        }
    }
}
