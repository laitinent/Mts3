package com.example.mts3

//import androidx.compose.material3.MaterialTheme

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mts3.ui.theme.Mts3Theme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import kotlin.random.Random

object RetrofitHelper {

    val baseUrl = "https://laitinent.github.io/"

    fun getInstance(): Retrofit {
        return Retrofit.Builder().baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            // we need to add converter factory to
            // convert JSON object to Java object
            .build()
    }
}

interface QuotesApi {
    @GET("/recipes.csv")
    suspend fun getQuotes(): Response<String>
}

class Recipe(
    var Carbohydrate: String,
    var Protein: String,
    var Description: String,
    var Factor: Int = 1
)

class MainActivity : ComponentActivity() {
    private fun getCsvAsList(csv: String): List<Recipe> {
        val listToReturn = mutableListOf<Recipe>()
        val lines = csv.split("\n")
        for (line in lines) {
            val values = line.split(" ")
            if (values.size < 3) continue
            val recipe = Recipe(values[0], values[1], values[2])
            listToReturn.add(recipe)
        }
        return listToReturn
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Mts3Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting()//lista)//result.body().toString())
                }
            }
        }
    }
}

@Composable
//fun Greeting(name: List<Recipe>, modifier: Modifier = Modifier) {
fun Greeting(viewModel: MyViewModel = viewModel()) {
    val data by viewModel.data.collectAsState()
    val lis = listOf("perunaa", "riisiä", "pastaa", "leipää")
    val prots = listOf("kanaa", "kalaa", "jauhelihaa", "kinkkua", "possua", "makkaraa", "nautaa")
    var teksti by remember { mutableStateOf("Ruokaa") }
    var prev_lisä by remember { mutableIntStateOf(-1) }  // prevent same as previous
    var prev_prot by remember { mutableIntStateOf(-1) }
    val myFontSize = 36.sp
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "$teksti",
            modifier = Modifier.padding(16.dp),
            fontSize = myFontSize
        )

        Button(onClick = {
            /*TODO*/
            var lisä: Int = -1
            var prot: Int = -1

            if (data.isEmpty()) {
                viewModel.fetchData()
            }
            Log.d("Recipe: ", data.size.toString());

            do {
                lisä = (lis.indices).random()
                prot = (prots.indices).random()
            } while (prev_lisä == lisä && prev_prot == prot)

            teksti = "${prots[prot]} ja ${lis[lisä]}"

            // jos useita samoja yhdistelmiä, arvotaan niistä joku
            val samatAineetLista = mutableListOf<Recipe>()
            data.forEach { recipe ->
                if (recipe.Carbohydrate == lis[lisä] && recipe.Protein == prots[prot]) {
                    samatAineetLista.add(recipe)  // kertätään listalle
                }
                val valittu = Random.nextInt(0, samatAineetLista.size) //arvonta
                if (samatAineetLista.size > 0) {
                    teksti = samatAineetLista[valittu].Description  //recipe.Description
                }

            }

            prev_lisä = lisä
            prev_prot = prot

        }) {
            Text(text = "Valitse", fontSize = myFontSize)
        }

    }
}

class MyViewModel : ViewModel() {
    private val _data = MutableStateFlow<List<Recipe>>(emptyList())
    val data: StateFlow<List<Recipe>> = _data

    fun fetchData() {
        val quotesApi = RetrofitHelper.getInstance().create(QuotesApi::class.java)
        viewModelScope.launch(Dispatchers.IO) {
            // Replace with your network call using Retrofit or another library
            val response = quotesApi.getQuotes()/* Your network call here */

            if (response.isSuccessful) {
                val responseData = response.body() //?: emptyList()
                val lista = getCsvAsList(responseData.toString())
                _data.value = lista //responseData
            } else {
                // Handle error
            }
        }
    }

    /**
     * Converts CSV to list of Recipe objects
     * @param csv CSV string
     * @return List of Recipe objects
     */
    private fun getCsvAsList(csv: String): List<Recipe> {
        val listToReturn = mutableListOf<Recipe>()
        val lines = csv.split("\n")
        for (line in lines) {
            val values = line.split(" ")
            if (values.size < 3) continue
            val recipe = Recipe(values[0], values[1], values[2])
            if (values.size > 3) recipe.Factor = values[3].toInt()
            listToReturn.add(recipe)
        }
        return listToReturn
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Mts3Theme {
        Greeting()//listOf<Recipe>())
    }
}