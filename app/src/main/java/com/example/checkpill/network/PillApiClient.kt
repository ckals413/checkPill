package com.example.checkpill.network

import android.util.Log
import com.example.checkpill.model.PillResponse
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.pytorch.BuildConfig
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.concurrent.TimeUnit



// 공공데이터 포털 의약품 정보 API 서비스 인터페이스
interface PillApiService {
    @GET("getDrbEasyDrugList")
    suspend fun searchPills(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("itemName") itemName: String,
        @Query("pageNo") pageNo: Int = 1,
        @Query("numOfRows") numOfRows: Int = 10,
        @Query("type") type: String = "json"
    ): Response<PillResponse>
}

 // 알약 API 클라이언트
class PillApiClient {
    private val apiService: PillApiService

    // 공공데이터 의약품 API 키
    private val apikey = com.example.checkpill.BuildConfig.API_KEY

    // 공공데이터 의약품 API 기본 URL
    private val baseUrl = "https://apis.data.go.kr/1471000/DrbEasyDrugInfoService/"

    init {
        // HTTP 요청/응답 로깅을 위한 인터셉터
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC // BODY 대신 BASIC 사용
        }

        // OkHttp 클라이언트 설정
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()

        // Retrofit 설정
        val gson = GsonBuilder()
            .setLenient()
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        // API 서비스 생성
        apiService = retrofit.create(PillApiService::class.java)
    }

   // 알약 이름으로 검색
    suspend fun searchPills(query: String): Response<PillResponse> {
        return apiService.searchPills(apikey, query)
    }
}