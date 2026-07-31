package com.medhaup.time.data

import com.medhaup.time.supabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

object TimeTrackerRepository {

    private const val TABLE = "time_entries"

    /** All of today's entries (local timezone), newest first. */
    suspend fun getTodayEntries(): List<TimeEntry> {
        val startOfDay = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toString()

        return supabase.from(TABLE).select {
            filter { gte("check_in", startOfDay) }
            order("check_in", Order.DESCENDING)
        }.decodeList()
    }

    /** The currently open entry (checked in, not yet out), if any — regardless of day. */
    suspend fun getOpenEntry(): TimeEntry? =
        supabase.from(TABLE).select {
            filter { exact("check_out", null) }
            limit(1)
        }.decodeList<TimeEntry>().firstOrNull()

    suspend fun checkIn(): TimeEntry =
        supabase.from(TABLE).insert(NewTimeEntry()) {
            select()
        }.decodeSingle()

    suspend fun checkOut(entryId: String): TimeEntry =
        supabase.from(TABLE).update({
            set("check_out", Instant.now().toString())
        }) {
            filter { eq("id", entryId) }
            select()
        }.decodeSingle()
}