package com.example.weatherapp

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import retrofit2.Call
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*



class MainActivity : AppCompatActivity() {
    private val apiKey="cbe123b512a4e3abb6fc000832692af9" // the API key
    private var weather: Weather? =null //for weather data
    private var zipCode=90001 // a default zipcode
    private var iscelsius=true //for changing the unit
    private var layoutVisibility=true //for the visibility purpose

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //to hide the app name bar
        val actionBar: ActionBar? = supportActionBar
            if (actionBar != null) {
                actionBar.hide()
            }

          hideLayouts(true)
         setZipCode()
    }
    //to get the data from the API
    private fun getApiData() {
        CoroutineScope(Dispatchers.IO).launch {
            val data = async {
                fetchWeather()
            }.await()
            //after fetching data
            if (weather!=null && data!=false)
            {
               updateWeather()
            }
            else {
                withContext(Dispatchers.Main) {
                Toast.makeText(applicationContext, "Enter a Correct Zip Code", Toast.LENGTH_SHORT)
                    .show()
            }
         }
        }
    }
    //to change the background based on time if day or night
    fun changeBackground(){
        val str= SimpleDateFormat( "a",Locale.ENGLISH).format(Date((weather?.dt!!).toLong()*1000))
        if(str=="PM")
        findViewById<ConstraintLayout>(R.id.llMain).setBackgroundResource(R.drawable.night_bg)
        else
        findViewById<ConstraintLayout>(R.id.llMain).setBackgroundResource(R.drawable.day_bg)
    }
    //to let user enter the zipcode
    fun setZipCode(){
            hideLayouts(true)
             var newZipCode=findViewById<EditText>(R.id.etZipCode)
            var btZip=findViewById<Button>(R.id.btZip)

            btZip.setOnClickListener {
                try{
                    zipCode =Integer.parseInt(newZipCode.text.toString())
                    getApiData()
                    newZipCode.text.clear()
                    newZipCode.hideKeyboard()
                }catch (e:Exception){
                    Toast.makeText(applicationContext, "" + e.message, Toast.LENGTH_SHORT).show()
                    hideLayouts(false)
                    newZipCode.text.clear()
                    newZipCode.hideKeyboard()
                }
            }
        }
    //to hide the keyboard
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    //to control the visibility of layouts
    fun hideLayouts(v:Boolean){
        layoutVisibility=v
        if(layoutVisibility) {
            //hide the layout and show the zipcode layout
            findViewById<LinearLayout>(R.id.llweather).isVisible = false
            findViewById<LinearLayout>(R.id.llzipCode).isVisible = true
            layoutVisibility = false
        }
        else
        {
            findViewById<LinearLayout>(R.id.llweather).isVisible = true
            findViewById<LinearLayout>(R.id.llzipCode).isVisible = false
        }
    }
    //update the UI elements when weather data are available
    private suspend fun updateWeather() {
        withContext(Dispatchers.Main) {
            hideLayouts(false)
            changeBackground()
            findViewById<LinearLayout>(R.id.llAddress).setOnClickListener { setZipCode() }
            findViewById<LinearLayout>(R.id.llRefresh).setOnClickListener { getApiData() }
            //change to F
            findViewById<LinearLayout>(R.id.llTemp).setOnClickListener { cToF()}

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
            findViewById<TextView>(R.id.tvSunrise).text= SimpleDateFormat("hh:mm a").format(Date(time*1000))
            time = weather?.sys?.sunset!!.toLong()
            findViewById<TextView>(R.id.tvSunset).text= SimpleDateFormat("hh:mm a").format(Date(time*1000))

            findViewById<TextView>(R.id.tvWind).text= weather?.wind?.speed.toString()
            findViewById<TextView>(R.id.tvPressure).text= weather?.main?.pressure.toString()
            findViewById<TextView>(R.id.tvHumidity).text= weather?.main?.humidity.toString()

        }
    }
    //to change the picture for weather status
    private fun imageStatus(){
       val status= weather?.weather?.get(0)?.main
        //get the icon id and change the image of the weather status
        val imageView = findViewById<ImageView>(R.id.imgStatus)
        Glide.with(this).load("https://openweathermap.org/img/wn/${weather?.weather?.get(0)?.icon}@2x.png").into(imageView)
    }
    //to change from C to F
    fun cToF() {
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
