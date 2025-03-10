package com.example.checkpill.network

import com.example.checkpill.model.PillResponse
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class PillResponseDeserializer : JsonDeserializer<PillResponse> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): PillResponse {
        // 응답 문자열 로깅
        val jsonString = json.toString()
        android.util.Log.d("API_RESPONSE", jsonString)

        return try {
            // 정상적인 경우
            context.deserialize(json, PillResponse::class.java)
        } catch (e: Exception) {
            // 오류 발생 시 기본 객체 반환
            android.util.Log.e("API_RESPONSE", "파싱 오류: ${e.message}")
            PillResponse(
                PillResponse.Header("ERROR", e.message ?: "Unknown error"),
                PillResponse.Body(emptyList(), 0, 0, 0)
            )
        }
    }
}