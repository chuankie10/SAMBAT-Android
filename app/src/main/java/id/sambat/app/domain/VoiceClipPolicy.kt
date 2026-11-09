package id.sambat.app.domain

import java.time.Duration
import java.time.Instant

/**
 * Product rules for voice content. Kept independent from Android UI and storage
 * so the same rules can be tested and reused by future repository/backend layers.
 */
enum class VoiceClipType {
    STORY,
    REPLY
}

enum class VoiceClipValidation {
    VALID,
    DURATION_MUST_BE_POSITIVE,
    DURATION_EXCEEDS_LIMIT
}

object VoiceClipPolicy {
    const val MAX_STORY_DURATION_SECONDS = 120
    const val MAX_REPLY_DURATION_SECONDS = 5
    const val STORY_RETENTION_HOURS = 24L

    fun validate(type: VoiceClipType, durationSeconds: Int): VoiceClipValidation {
        if (durationSeconds <= 0) {
            return VoiceClipValidation.DURATION_MUST_BE_POSITIVE
        }

        val maximum = when (type) {
            VoiceClipType.STORY -> MAX_STORY_DURATION_SECONDS
            VoiceClipType.REPLY -> MAX_REPLY_DURATION_SECONDS
        }

        return if (durationSeconds <= maximum) {
            VoiceClipValidation.VALID
        } else {
            VoiceClipValidation.DURATION_EXCEEDS_LIMIT
        }
    }

    fun expiresAt(createdAt: Instant): Instant =
        createdAt.plus(Duration.ofHours(STORY_RETENTION_HOURS))

    fun isExpired(createdAtMillis: Long, nowMillis: Long): Boolean {
        if (nowMillis < createdAtMillis) return false
        val retentionMillis = Duration.ofHours(STORY_RETENTION_HOURS).toMillis()
        return nowMillis - createdAtMillis >= retentionMillis
    }
}
