package com.example.weatherappcompose.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import com.android.volley.Request
import com.android.volley.Request.Method.GET
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherappcompose.MainViewModel
import com.example.weatherappcompose.R
import com.example.weatherappcompose.data.WeatherModel
import com.example.weatherappcompose.ui.theme.BlueLight
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import org.json.JSONObject
import androidx.lifecycle.viewmodel.compose.*

const val API_KEY = "602e5ab516424c45ab6143539220708"

@Preview(showBackground = true)
@Composable
fun MainCard(viewModel: MainViewModel = viewModel() ) {
//
    Column(
        modifier = Modifier
            .padding(5.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = BlueLight,
            elevation = 0.dp,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                            viewModel.liveDataCurrent.value?.let {
                                Text(
                                    modifier = Modifier.padding(top = 8.dp),
                                    text = it.time,
                                    style = TextStyle(fontSize = 15.sp),
                                    color = Color.White

                                )
                            }

                    AsyncImage(
                        model = "icon",
                        contentDescription = "weatherIcon",
                        modifier = Modifier
                            .size(35.dp)
                            .padding(top = 3.dp, end = 8.dp)
                    )
                }
                    Text(
                        text = "city",
                        style = TextStyle(fontSize = 25.sp),
                        color = Color.White
                    )
                    Text(
                        text = "currentTemp",
                        style = TextStyle(fontSize = 64.sp),
                        color = Color.White
                    )
                    Text(
                        text = "condition",
                        style = TextStyle(fontSize = 16.sp),
                        color = Color.White
                    )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick =
                    {


                    }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "searchIcon",
                            tint = Color.White
                        )


                    }
                    Text(
                        text = "maxTemp/minTemp",
                        style = TextStyle(fontSize = 16.sp),
                        color = Color.White
                    )

                    IconButton(onClick = {requestWeatherData(viewModel.liveDataCurrent.value.c)}

                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sync),
                            contentDescription = "syncIcon",
                            tint = Color.White
                        )

                    }
                }
            }
        }

    }

}

@OptIn(ExperimentalPagerApi::class)
@Preview(showBackground = true)
@Composable
fun TabLayout() {
    val tabList = listOf("HOURS", "DAYS")
    val pagerState = rememberPagerState()
    val tabIndex = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()


    Column(
        modifier = Modifier
            .padding(start = 5.dp, end = 5.dp)
            .clip(RoundedCornerShape(5.dp))
    ) {
        TabRow(
            selectedTabIndex = 0,
            indicator = { pos ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, pos)
                )
            },
            backgroundColor = BlueLight,
            contentColor = Color.White

        ) {
            tabList.forEachIndexed { index, text ->
                Tab(selected = false,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                    text = {
                        Text(text = text)
                    }
                )
            }
        }
        HorizontalPager(
            count = tabList.size,
            state = pagerState,
            modifier = Modifier.weight(1.0f)
        ) { index ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(
                    listOf(
                        WeatherModel("London",
                            "10:00",
                            "25ºC",
                            "Sunny",
                            "//cdn.weatherapi.com/weather/64x64/day/176.png",
                            "",
                            "",
                            ""
                    ))){ _, item ->
                    ListItem(item)
                }
            }
        }
    }

}



private fun requestWeatherData(city: String){
    val url = "https://api.weatherapi.com/v1/forecast.json?key=" +
            API_KEY +
            "&q=" +
            city +
            "&days=" +
            "3" +
            "&aqi=no&alerts=no"
    val queue = Volley.newRequestQueue(this)
    val request = StringRequest(
        GET,
        url,
        {
                result -> parseWeatherData(result)
        },
        {
                error -> Log.d("MyLog", "Error: $error")
        }
    )
    queue.add(request)
}

private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel){
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
private fun parseWeatherData(result: String) {
    val mainObject = JSONObject(result)
    val list = parseDays(mainObject)
    parseCurrentData(mainObject, list[0])
}

private fun parseDays(mainObject: JSONObject): List<WeatherModel>{
    val list = ArrayList<WeatherModel>()
    val daysArray = mainObject.getJSONObject("forecast")
        .getJSONArray("forecastday")
    val name =  mainObject.getJSONObject("location").getString("name")
    for (i in 0 until daysArray.length()){
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


private fun updateCurrentCard(){

}