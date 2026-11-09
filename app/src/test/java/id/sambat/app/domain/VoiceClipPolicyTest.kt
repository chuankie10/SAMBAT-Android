package id.sambat.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class VoiceClipPolicyTest {
    @Test
    fun storyAcceptsMaximumDuration() {
        assertEquals(
            VoiceClipValidation.VALID,
            VoiceClipPolicy.validate(VoiceClipType.STORY, 120)
        )
    }

    @Test
    fun storyRejectsDurationOverTwoMinutes() {
        assertEquals(
            VoiceClipValidation.DURATION_EXCEEDS_LIMIT,
            VoiceClipPolicy.validate(VoiceClipType.STORY, 121)
        )
    }

    @Test
    fun replyAcceptsFiveSecondsAndRejectsSix() {
        assertEquals(
            VoiceClipValidation.VALID,
            VoiceClipPolicy.validate(VoiceClipType.REPLY, 5)
        )
        assertEquals(
            VoiceClipValidation.DURATION_EXCEEDS_LIMIT,
            VoiceClipPolicy.validate(VoiceClipType.REPLY, 6)
        )
    }

    @Test
    fun allClipsMustHavePositiveDuration() {
        assertEquals(
            VoiceClipValidation.DURATION_MUST_BE_POSITIVE,
            VoiceClipPolicy.validate(VoiceClipType.STORY, 0)
        )
        assertEquals(
            VoiceClipValidation.DURATION_MUST_BE_POSITIVE,
            VoiceClipPolicy.validate(VoiceClipType.REPLY, -1)
        )
    }

    @Test
    fun storyExpiresExactlyAfterTwentyFourHours() {
        val createdAt = Instant.parse("2026-11-09T06:00:00Z")
        assertEquals(
            Instant.parse("2026-11-10T06:00:00Z"),
            VoiceClipPolicy.expiresAt(createdAt)
        )
    }
}
