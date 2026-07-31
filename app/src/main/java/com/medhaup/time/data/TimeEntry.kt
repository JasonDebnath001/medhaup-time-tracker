package com.medhaup.time.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime

@Serializable
data class TimeEntry(
    val id: String,
    @SerialName("user_id") val userId: String,
    @SerialName("check_in") val checkIn: String,
    @SerialName("check_out") val checkOut: String? = null
) {
    val checkInInstant: Instant get() = OffsetDateTime.parse(checkIn).toInstant()
    val checkOutInstant: Instant? get() = checkOut?.let { OffsetDateTime.parse(it).toInstant() }
    val isOpen: Boolean get() = checkOut == null

    /** Duration of this entry — live (until now) if still open. */
    fun duration(now: Instant = Instant.now()): Duration =
        Duration.between(checkInInstant, checkOutInstant ?: now)
}

/** Insert payload — user_id and check_in are filled in by Postgres defaults. */
@Serializable
data class NewTimeEntry(
    @SerialName("check_in") val checkIn: String = Instant.now().toString()
)