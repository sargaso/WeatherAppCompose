package com.example.weatherappcompose

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherappcompose.data.WeatherModel
import com.example.weatherappcompose.screens.MainCard
import com.example.weatherappcompose.screens.TabLayout
import com.example.weatherappcompose.ui.theme.WeatherAppComposeTheme
import org.json.JSONObject

const val API_KEY = "602e5ab516424c45ab6143539220708"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WeatherAppComposeTheme {
                requestWeatherData("Moscow",this)
                Image(
                    painter = painterResource(id = R.drawable.img), contentDescription = "img1",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillHeight
                )
                Column {
                    MainCard()
                    TabLayout()


                }


            }
        }
    }
}

private fun requestWeatherData(city: String, context: Context) {
    val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
            API_KEY +
            "&q=" +
            city +
            "&days=" +
            "3" +
            "&aqi=no&alerts=no"
    val queue = Volley.newRequestQueue(context)
    val request = StringRequest(
        Request.Method.GET,
        url,
        { result ->
            parseWeatherData(result)
        },
        { error ->
            Log.d("MyLog", "Error: $error")
        }
    )
    queue.add(request)
}

private fun parseWeatherData(result: String) {
    val mainObject = JSONObject(result)
    val list = parseDays(mainObject)
    parseCurrentData(mainObject, list[0])
}


private fun parseDays(mainJsonObject: JSONObject): List<WeatherModel> {
    val list = ArrayList<WeatherModel>()
    val daysArray = mainJsonObject.getJSONObject("forecast")
        .getJSONArray("forecastday")
    val name = mainJsonObject.getJSONObject("location").getString("name")
    for (i in 0 until daysArray.length()) {
        val day = daysArray[i] as JSONObject
        val item = WeatherModel(
            name,
            day.getString("date"),
            day.getJSONObject("day").getJSONObject("condition")
                .getString("text"),
            "",
            day.getJSONObject("day").getString("maxtemp_c"),
            day.getJSONObject("day").getString("mintemp_c"),
            day.getJSONObject("day").getJSONObject("condition")
                .getString("icon"),
            day.getJSONArray("hour").toString()
        )
        list.add(item)
    }
    return list
}

private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {
    val item = WeatherModel(
        mainObject.getJSONObject("location").getString("name"),
        mainObject.getJSONObject("current").getString("last_updated"),
        mainObject.getJSONObject("current")
            .getJSONObject("condition").getString("text"),
        mainObject.getJSONObject("current").getString("temp_c"),
        weatherItem.maxTemp,
        weatherItem.minTemp,
        mainObject.getJSONObject("current")
            .getJSONObject("condition").getString("icon"),
        weatherItem.hours
    )

    Log.d("MyLog", "City: ${item.maxTemp}")
    Log.d("MyLog", "Time: ${item.minTemp}")
    Log.d("MyLog", "Time: ${item.hours}")

}