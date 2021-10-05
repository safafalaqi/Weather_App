package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import retrofit2.Call
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    private val apiKey="cbe123b512a4e3abb6fc000832692af9"
    private var weather: Weather? =null
    private var zipCode=90001
    private var iscelsius=true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getApiData()
    }

    private fun getApiData() {
        CoroutineScope(Dispatchers.IO).launch {
            val data = async {
                fetchWeather()

            }.await()
            //after fetching data
            if (weather!=null)
            {
               updateWeather()
            }

        }
        }

    private suspend fun updateWeather() {
        withContext(Dispatchers.Main) {
            findViewById<LinearLayout>(R.id.llRefresh).setOnClickListener { getApiData() }
            //change to F
            findViewById<LinearLayout>(R.id.llTemp).setOnClickListener { cToF() }

            findViewById<LinearLayout>(R.id.llAddress).setOnClickListener {
                //hide the layout and show the zipcode layout
                findViewById<LinearLayout>(R.id.llweather).isVisible=false

                findViewById<LinearLayout>(R.id.llzipCode).isVisible=true

                var newZipCode=findViewById<EditText>(R.id.etZipCode)
                var btZip=findViewById<Button>(R.id.btZip)
                btZip.setOnClickListener {
                    try{
                    zipCode =Integer.parseInt(newZipCode.text.toString())
                    //get data for the new zipcode
                        getApiData()
                        if(weather!=null) {
                            findViewById<LinearLayout>(R.id.llweather).isVisible = true
                            findViewById<LinearLayout>(R.id.llzipCode).isVisible = false
                            newZipCode.text.clear()
                        }
                        else{
                            Toast.makeText(applicationContext, "Enter a Correct Zip Code", Toast.LENGTH_SHORT).show()
                            findViewById<LinearLayout>(R.id.llweather).isVisible = true
                            findViewById<LinearLayout>(R.id.llzipCode).isVisible = false
                            newZipCode.text.clear()
                        }
                    }catch (e:Exception){
                        Toast.makeText(applicationContext, "" + e.message, Toast.LENGTH_SHORT).show()
                        findViewById<LinearLayout>(R.id.llweather).isVisible=true
                        findViewById<LinearLayout>(R.id.llzipCode).isVisible=false
                        newZipCode.text.clear()
                    }
                }
            }

            findViewById<TextView>(R.id.tvCity).text =weather?.name +","+weather?.sys?.country.toString()
            val updateAt:Long=weather?.dt!!.toLong()
            findViewById<TextView>(R.id.tvUpdate).text= "Updated at: " + SimpleDateFormat(
                "dd/MM/yyyy hh:mm a",
                Locale.ENGLISH).format(Date(updateAt*1000))
            //change the image based on weather status
            imageStatus()

            findViewById<TextView>(R.id.tvStatus).text=weather?.weather?.get(0)?.description
            //update temperature
            var temp =weather?.main?.temp?.toInt()!! - 273.15 //convert from kelvin
            findViewById<TextView>(R.id.tvTemp).text= temp.toInt().toString() +"° C"
            temp =weather?.main?.temp_min?.toInt()!! - 273.15 //convert from kelvin
            findViewById<TextView>(R.id.tvLow).text="Low:" +temp.toInt().toString() +"°C"
            temp =weather?.main?.temp_max?.toInt()!! - 273.15 //convert from kelvin
            findViewById<TextView>(R.id.tvHigh).text= "High:" +temp.toInt().toString() +"°C"

            var time: Long = weather?.sys?.sunrise!!.toLong()
            findViewById<TextView>(R.id.tvSunrise).text= SimpleDateFormat("hh:mm ").format(Date(time*1000))
            time = weather?.sys?.sunset!!.toLong()
            findViewById<TextView>(R.id.tvSunset).text= SimpleDateFormat("hh:mm ").format(Date(time*1000))

            findViewById<TextView>(R.id.tvWind).text= weather?.wind?.speed.toString()
            findViewById<TextView>(R.id.tvPressure).text= weather?.main?.pressure.toString()
            findViewById<TextView>(R.id.tvHumidity).text= weather?.main?.humidity.toString()

        }
    }
    //to change the picture for weather status
    private fun imageStatus(){
       val status= weather?.weather?.get(0)?.main
        when(status){
            "Clear"->findViewById<ImageView>(R.id.imgStatus).setImageResource(R.drawable.clear_sky)
            "Clouds"->findViewById<ImageView>(R.id.imgStatus).setImageResource(R.drawable.scattered_clouds)
            "Mist"->findViewById<ImageView>(R.id.imgStatus).setImageResource(R.drawable.mist)
            "Snow"->findViewById<ImageView>(R.id.imgStatus).setImageResource(R.drawable.snow)
            "Rain"->findViewById<ImageView>(R.id.imgStatus).setImageResource(R.drawable.rain)
            "Thunderstorm"->findViewById<ImageView>(R.id.imgStatus).setImageResource(R.drawable.thunderstorm)
        }
    }
    //to change from C to F
    fun cToF()
    {
        if(iscelsius)
        {
            //update temperature
            var temp =weather?.main?.temp?.toInt()!! - 273.15 //convert from kelvin
            temp=(temp*9/5)+32 //convert to f
            findViewById<TextView>(R.id.tvTemp).text= temp.toInt().toString() +"° F"
            temp =weather?.main?.temp_min?.toInt()!! - 273.15 //convert from kelvin
            temp=(temp*9/5)+32 //convert to f
            findViewById<TextView>(R.id.tvLow).text="Low:" +temp.toInt().toString() +"°F"
            temp =weather?.main?.temp_max?.toInt()!! - 273.15 //convert from kelvin
            temp=(temp*9/5)+32 //convert to f
            findViewById<TextView>(R.id.tvHigh).text= "High:" +temp.toInt().toString() +"°F"
            iscelsius=false
        }else
        {
            getApiData()
            iscelsius=true
        }

    }

    private fun fetchWeather(): Boolean {

        val apiInterface = APIClient().getClient()?.create(APIInterface::class.java)
        val call: Call<Weather?>? = apiInterface!!.getCurrentWeatherData("/data/2.5/weather?zip=$zipCode&appid=$apiKey")
        //I am using synchronous as I am running this in another thread and no need for enqueue
        try{
        weather = call?.execute()?.body()
        }catch (e:Exception)
        {
            Toast.makeText(applicationContext, "" + e.message, Toast.LENGTH_SHORT).show()
            call?.cancel()
        }
        /*
        val apiInterface = APIClient().getClient()?.create(APIInterface::class.java)
        val call: Call<Weather?>? = apiInterface!!.getCurrentWeatherData("/data/2.5/weather?zip=$zipCode&appid=$apiKey")
        call?.enqueue(object : Callback<Weather?> {
            override fun onResponse(
                call: Call<Weather?>?,
                response: Response<Weather?>
            ) {
                weather= response.body()
            }

            override fun onFailure(call: Call<Weather?>, t: Throwable) {
                Toast.makeText(applicationContext, "" + t.message, Toast.LENGTH_SHORT).show()
                call.cancel()
            }
        })*/
        return true
    }
}
