package com.edric.week9_pokemon_card.data.remote

import com.edric.week9_pokemon_card.BuildConfig
import com.edric.week9_pokemon_card.data.repository.PokemonCardRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Tempat membuat objek-objek singleton (Retrofit, Repository) secara manual.
 * Sama seperti arsitektur MovieDatabase.
 */
object ServiceLocator {

    private const val BASE_URL = "https://www.pokemonpricetracker.com/"

    /**
     * Masukkan satu atau beberapa API key dari https://www.pokemonpricetracker.com/ di sini.
     * Jika key pertama habis kuota (100 request/hari), ApiKeyInterceptor otomatis lanjut ke key berikutnya.
     */
    val API_KEYS: List<String> = listOf(
        "pokeprice_free_b68fb52d0094cf2eb59116c4bdd95382faefacc45bc82698",//key 1
        "pokeprice_free_66ef3b3833ae5bf81a39ec927a15a498aa90d36ced3004bd",//key 2
        "pokeprice_free_9fe6f519f95a07d2ae7fb67ba6de890883757d4fd700b68a",//key 3
        "pokeprice_free_e287d6422c94f1f8eb3c0c70df8ce411f3392c077380a75c",//key 4
        "pokeprice_free_58e7e52240c729d90b0d6dcef36f830cd5e2db5b2e8cc7ff",//key 5
    )

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(API_KEYS))
            // BASIC saja agar Authorization header (API key) tidak tercetak di Logcat
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
                else HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    private val api: PokemonTrackerApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(PokemonTrackerApi::class.java)
    }

    val pokemonCardRepository: PokemonCardRepository by lazy {
        PokemonCardRepository(api = api, apiKeys = API_KEYS)
    }
}
