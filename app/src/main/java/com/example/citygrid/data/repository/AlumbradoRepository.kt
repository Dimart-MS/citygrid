package com.example.citygrid.data.repository

import com.example.citygrid.data.SupabaseManager
import com.example.citygrid.model.db.DbLecturaLuminaria
import com.example.citygrid.model.db.DbLuminaria
import io.github.jan.supabase.postgrest.from

object AlumbradoRepository {
    suspend fun obtenerLuminarias(): List<DbLuminaria> {
        return try {
            SupabaseManager.client.from("luminarias").select().decodeList<DbLuminaria>()
        } catch (e: Exception) {
            android.util.Log.e("AlumbradoRepository", "Error al obtener luminarias", e)
            emptyList()
        }
    }

    suspend fun obtenerLecturasPorLuminaria(idLuminaria: Int): List<DbLecturaLuminaria> {
        return try {
            SupabaseManager.client
                .from("lecturasluminaria")
                .select {
                    filter { eq("idluminaria", idLuminaria) }
                }
                .decodeList<DbLecturaLuminaria>()
        } catch (e: Exception) {
            android.util.Log.e("AlumbradoRepository", "Error al obtener lecturas luminaria", e)
            emptyList()
        }
    }
}
