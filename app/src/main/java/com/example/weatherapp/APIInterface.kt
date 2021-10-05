package com.example.weatherapp
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url





interface APIInterface {
   // @GET("/weather?zip={zip_code}&appid={api_key}")
    @GET
    fun getCurrentWeatherData(@Url url: String?): Call<Weather?>?

}
