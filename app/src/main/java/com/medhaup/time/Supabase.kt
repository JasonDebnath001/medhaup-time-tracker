package com.medhaup.time

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

val supabase = createSupabaseClient(
    supabaseUrl = "https://ymbbwwtcbnyvgonaznnq.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InltYmJ3d3RjYm55dmdvbmF6bm5xIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODU0NzQ5MjMsImV4cCI6MjEwMTA1MDkyM30.jFpIJw4Ml853k8UvVm3MobJGgWwsF1ZSiG0MpZgal5c"                           // ← paste your anon key
) {
    install(Auth)
    install(Postgrest)
}