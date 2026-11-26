package com.example.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val AI_TAG = "AiContentRepository"

class AiContentRepository {

    private val structuredSchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Short catchy title for the generated creator pack"),
            "items" to Schema.array(
                items = Schema.string(),
                description = "List of generated hashtags, captions, bio lines, or reel hooks"
            )
        )
    )

    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = structuredSchema
            }
        )
    }

    suspend fun generateCreatorPack(
        toolType: String,
        topic: String,
        tone: String,
        count: Int
    ): Result<Pair<String, List<String>>> {
        val prompt = buildString {
            append("You are an expert social media growth strategist. ")
            when (toolType) {
                "HASHTAGS" -> append("Generate exactly $count high-reach, non-spammy, current trending hashtags (each starting with # and no spaces) for topic '$topic' with a '$tone' vibe.")
                "CAPTIONS" -> append("Generate exactly $count scroll-stopping social media captions for topic '$topic' in a '$tone' tone. Keep them punchy and ready to paste.")
                "BIOS" -> append("Generate exactly $count clean, memorable Instagram/YouTube creator bio ideas for niche '$topic' in a '$tone' style.")
                else -> append("Generate exactly $count viral short-form Reel/Shorts content hooks and ideas for topic '$topic' in a '$tone' style.")
            }
        }
        return try {
            val response = withTimeout(60_000L) {
                structuredModel.generateContent(prompt)
            }
            val jsonText = response.text
                ?: return Result.failure(IllegalStateException("AI returned an empty response."))
            val json = JSONObject(jsonText)
            val title = json.optString("title", "$count $topic")
            val array = json.getJSONArray("items")
            val lines = (0 until array.length()).mapNotNull { idx ->
                array.optString(idx).trim().takeIf { it.isNotBlank() }
            }
            if (lines.isEmpty()) {
                Result.failure(IllegalStateException("No items were generated. Please try again."))
            } else {
                Result.success(title to lines)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(AI_TAG, "AI generation timed out", e)
            Result.failure(IllegalStateException("Generation timed out after 60 seconds. Please try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(AI_TAG, "Firebase AI generation failed", e)
            Result.failure(e)
        }
    }
}
