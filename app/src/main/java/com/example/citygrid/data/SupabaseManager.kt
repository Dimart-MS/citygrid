package com.example.citygrid.data

import com.example.citygrid.utils.Constants
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseManager {
    val client = createSupabaseClient(
        supabaseUrl = Constants.SUPABASE_URL,
        supabaseKey = Constants.SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Realtime)
    }
}
