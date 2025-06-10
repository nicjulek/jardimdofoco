package com.example.projetofinalmobile.data.remote

import com.example.projetofinalmobile.model.ZenApiResponseItem
import retrofit2.Response
import retrofit2.http.GET

interface ApiService {
    //busca uma citacao aleatoria da API
    @GET("api/random")
    suspend fun getRandomZenQuote(): Response<List<ZenApiResponseItem>>
}