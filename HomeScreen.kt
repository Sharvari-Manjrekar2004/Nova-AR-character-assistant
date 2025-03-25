import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.WindowInsetsAnimation
import android.view.WindowInsetsAnimation.*
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.airbnb.lottie.compose.*
import com.xperiencelabs.astronaut.R
import com.xperiencelabs.astronaut.screens.ARView
import com.xperiencelabs.astronaut.utils.SpeechToTextManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.android.gms.common.api.Response
import com.google.android.gms.location.LocationServices
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import android.util.Base64
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.IconButtonDefaults
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var animation by remember { mutableStateOf("idle") }
    var listenEnable by remember { mutableStateOf(false) }
    var speechResponse by remember { mutableStateOf("") }
    var userInput by remember { mutableStateOf("") } // Holds the input from the search bar
    val context = LocalContext.current

    // Initialize Text-to-Speech
    val textToSpeech = remember {
        android.speech.tts.TextToSpeech(context) { status ->
            if (status != android.speech.tts.TextToSpeech.SUCCESS) {
                Log.e("TTS", "Initialization failed")
            }
        }
    }
    // Handle Speech Recognition Results
    LaunchedEffect(listenEnable) {
        if (listenEnable) {
            SpeechToTextManager.startListening(
                onSpeechStarted = { animation = "listening" },
                onSpeechStopped = { animation = "idle" },
                onSpeechError = { error -> Log.e("SpeechToText", error) },
                onSpeechResult = { results ->
                    if (results.isNotEmpty()) {
                        val question = results.first()
                        // Process the question
                        processQuestion(question, textToSpeech, context) { response ->
                            speechResponse = response
                        }
                    }
                    listenEnable = false
                }
            )
        } else {
            SpeechToTextManager.stopListening()
        }
    }

    // Main UI Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        // ARView Composable for rendering character animations
        ARView(animation = animation)

        // Search Bar
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White) // Set background color here
                    .clip(RoundedCornerShape(8.dp)) // Optional: rounded corners for the search bar
            ) {
                TextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    placeholder = { Text("Ask a question...") },
                    singleLine = true,
                    colors = TextFieldDefaults.textFieldColors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                if (userInput.isNotEmpty()) {
                                    processQuestion(
                                        question = userInput,
                                        textToSpeech = textToSpeech,
                                        context = context
                                    ) { response ->
                                        speechResponse = response
                                    }
                                }
                            },
                            modifier = Modifier,
                            enabled = true,
                            colors = IconButtonDefaults.iconButtonColors(),
                            interactionSource = remember { MutableInteractionSource() } // Correct placement
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_send_24),
                                contentDescription = "Send"
                            )
                        }


                    }
                )
            }
        }

        // Display the assistant's response
        if (speechResponse.isNotEmpty()) {
            Text(
                text = speechResponse,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.White.copy(alpha = 0.3f))
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
        }

        // Mic Button for voice input
        Box(
            modifier = Modifier
                .size(180.dp)
                .clickable { listenEnable = !listenEnable }
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            if (listenEnable) {
                val lottieCompositionResult = rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.listening))
                val composition = lottieCompositionResult.value

                LottieAnimation(
                    composition = composition,
                    progress = animateLottieCompositionAsState(composition = composition, iterations = LottieConstants.IterateForever).progress,
                    modifier = Modifier.size(180.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_mic_24),
                        contentDescription = "microphone",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

fun processQuestion(
    question: String,
    textToSpeech: android.speech.tts.TextToSpeech,
    context: android.content.Context,
    onResponse: (String) -> Unit
)
{

    val response = ""
    onResponse(response)

    // Speak the response using Text-to-Speech
    textToSpeech.speak(response, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)

    when {
        // Handle train schedule request
        question.contains("train schedule from", true) && question.contains("to", true) -> {
            val regex = Regex("train schedule from (.+) to (.+)", RegexOption.IGNORE_CASE)
            val matchResult = regex.find(question)

            val departureLocation = matchResult?.groups?.get(1)?.value
            val destinationLocation = matchResult?.groups?.get(2)?.value

            if (departureLocation != null && destinationLocation != null) {
                fetchTrainSchedule(departureLocation, destinationLocation, textToSpeech, onResponse)
            } else {
                val response = "Please specify both departure and destination locations."
                speakAndRespond(response, textToSpeech, onResponse)
            }
        }
        question.contains("bus schedule from", true) && question.contains("to", true) -> {
            val regex = Regex("bus schedule from (.+) to (.+)", RegexOption.IGNORE_CASE)
            val matchResult = regex.find(question)

            val departureLocation = matchResult?.groups?.get(1)?.value
            val destinationLocation = matchResult?.groups?.get(2)?.value

            if (departureLocation != null && destinationLocation != null) {
                fetchBusSchedule(departureLocation, destinationLocation, textToSpeech, onResponse)
            } else {
                val response = "Please specify both departure and destination locations."
                speakAndRespond(response, textToSpeech, onResponse)
            }
        }
        // Handle booking an Uber with a specific destination
        question.contains("book an Uber to", true) -> {
            val regex = Regex("book an Uber to (.+)", RegexOption.IGNORE_CASE)
            val matchResult = regex.find(question)
            val destination = matchResult?.groups?.get(1)?.value

            if (destination != null) {
                geocodeLocation(destination, context) { latitude, longitude ->
                    if (latitude != null && longitude != null) {
                        openUberApp(context, latitude, longitude)
                        val response = "Booking an Uber to $destination."
                        speakAndRespond(response, textToSpeech, onResponse)
                    } else {
                        val response = "I couldn't find the location $destination."
                        speakAndRespond(response, textToSpeech, onResponse)
                    }
                }
            } else {
                val response = "Please specify a destination for the Uber ride."
                speakAndRespond(response, textToSpeech, onResponse)
            }
        }
        // Handle general Uber booking
        question.contains("book an Uber", true) -> {
            val latitude = 40.748817 // Example latitude
            val longitude = -73.985428 // Example longitude
            openUberApp(context, latitude, longitude)
            val response = "Booking an Uber to your destination."
            speakAndRespond(response, textToSpeech, onResponse)
        }
        // Handle nearby medical stores
        question.contains("medical stores near me", true) -> {
            getNearbyPlaces(
                context = context,
                type = "pharmacy",
                textToSpeech = textToSpeech,
                onResponse = onResponse
            )
        }
        // Handle nearby restaurants
        question.contains("restaurants near me", true) -> {
            getNearbyRestaurants(
                context = context,
                textToSpeech = textToSpeech,
                onResponse = onResponse
            )
        }
        question.contains("play music", true) || question.contains("play", true) -> {
            val regex = Regex("play (.+)", RegexOption.IGNORE_CASE)
            val matchResult = regex.find(question)
            val songOrArtist = matchResult?.groups?.get(1)?.value

            if (songOrArtist != null) {
                openSpotify(songOrArtist, context)
                val response = "Playing $songOrArtist on Spotify."
                speakAndRespond(response, textToSpeech, onResponse)
            } else {
                val response = "Please specify the song or artist."
                speakAndRespond(response, textToSpeech, onResponse)
            }
        }


        // Handle calls
        question.contains("call", true) -> handleCallCommand(question, context, textToSpeech, onResponse)
        // Handle sending a message
        question.contains("send a message to", true) -> {
            handleMessageCommand(question, context, textToSpeech, onResponse)
        }
        // Handle general questions and fallback to the Gemini API
        else -> {
            val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(System.currentTimeMillis())
            val currentDate = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(System.currentTimeMillis())
            when {
                question.contains("how are you", true) -> {
                    val response = "I'm good, thank you!"
                    speakAndRespond(response, textToSpeech, onResponse)
                }
                question.contains("what is your name", true) -> {
                    val response = "Hi!, I am Nova your AR Assistant."
                    speakAndRespond(response, textToSpeech, onResponse)
                }
                question.contains("what time is it", true) || question.contains("tell me the time", true) -> {
                    val response = "The current time is $currentTime."
                    speakAndRespond(response, textToSpeech, onResponse)
                }
                question.contains("what is today's date", true) || question.contains("tell me the date", true) -> {
                    val response = "Today's date is $currentDate."
                    speakAndRespond(response, textToSpeech, onResponse)
                }
                else -> {
                    // Fallback to Gemini API
                    askGeminiAPI(question, textToSpeech, onResponse)
                }
            }
        }
    }
}

fun openSpotify(query: String, context: android.content.Context) {
    val encodedQuery = Uri.encode(query)
    val spotifyUri = "spotify:search:$encodedQuery"

    try {
        // Fetch the track ID based on the search query
        val trackId = fetchSpotifyTrackId(query) // Call to fetch the track ID based on song name

        if (trackId != null) {
            // If trackId is found, use the spotify:track URI to directly play the song
            val trackUri = "spotify:track:$trackId"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(trackUri))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setPackage("com.spotify.music") // Ensure it opens in Spotify
            context.startActivity(intent)
        } else {
            // If no track ID found, open the search results
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUri))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setPackage("com.spotify.music") // Ensure it opens in Spotify
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        // If Spotify is not installed, open it in a web browser
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com/search/$encodedQuery"))
        context.startActivity(browserIntent)
    }
}


fun fetchSpotifyTrackId(songName: String): String? {
    val clientId = "7cb5b367b990498e8ad28aac7e22bb8a" // Your Spotify client ID
    val clientSecret = "7cd045cfa98d4846bc39f1b65ab5a7de" // Your Spotify client secret
    val authUrl = "https://accounts.spotify.com/api/token"
    val searchUrl = "https://api.spotify.com/v1/search?q=${Uri.encode(songName)}&type=track&limit=1"

    // Create basic authorization header
    val authHeader = "Basic " + Base64.encodeToString("$clientId:$clientSecret".toByteArray(), Base64.NO_WRAP)
    val requestBody = "grant_type=client_credentials".toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull())

    val client = OkHttpClient()
    val authRequest = Request.Builder()
        .url(authUrl)
        .post(requestBody)
        .addHeader("Authorization", authHeader)
        .addHeader("Content-Type", "application/x-www-form-urlencoded")
        .build()

    try {
        // Get access token
        val response = client.newCall(authRequest).execute()
        if (!response.isSuccessful) return null

        val json = JSONObject(response.body?.string() ?: "")
        val accessToken = json.optString("access_token")

        if (accessToken.isEmpty()) return null

        // Search for the track
        val searchRequest = Request.Builder()
            .url(searchUrl)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()

        val searchResponse = client.newCall(searchRequest).execute()
        if (!searchResponse.isSuccessful) return null

        val searchJson = JSONObject(searchResponse.body?.string() ?: "")
        return searchJson.getJSONObject("tracks")
            .getJSONArray("items")
            .optJSONObject(0)
            ?.optString("id") // Extract track ID
    } catch (e: IOException) {
        return null
    }
}

fun openSpotifyTrack(trackId: String, context: Context) {
    val spotifyUri = "spotify:track:$trackId"  // Create URI to open track in Spotify

    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(spotifyUri))
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.setPackage("com.spotify.music")  // Ensure it opens in Spotify
        context.startActivity(intent)
    } catch (e: Exception) {
        // If Spotify is not installed, open in browser
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://open.spotify.com/track/$trackId"))
        context.startActivity(browserIntent)
    }
}

fun searchAndPlaySong(songName: String, context: android.content.Context) {
    val trackId = fetchSpotifyTrackId(songName) // Search for track and fetch ID

    if (trackId != null) {
        openSpotifyTrack(trackId, context)  // Open Spotify and play the track
    } else {
        Toast.makeText(context, "Song not found", Toast.LENGTH_SHORT).show()  // If song not found
    }
}

fun fetchTrainSchedule(
    origin: String,
    destination: String,
    textToSpeech: TextToSpeech,
    onResponse: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // API key from Google Cloud Console
            val apiKey = "AIzaSyAfZHEdNzlzUH0TTGDh9ICtBmKTajfrJQ0" // Replace with your API key

            // Build the URL with the transit and train mode
            val url = "https://maps.googleapis.com/maps/api/directions/json" +
                    "?origin=${Uri.encode(origin)}" +
                    "&destination=${Uri.encode(destination)}" +
                    "&mode=transit" +
                    "&transit_mode=train" +
                    "&key=$apiKey"
            // Create an OkHttpClient instance
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            // Execute the request
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                // Parse the JSON response
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    // Check for valid routes in the response
                    if (jsonResponse.has("routes") && jsonResponse.getJSONArray("routes").length() > 0) {
                        val route = jsonResponse.getJSONArray("routes").getJSONObject(0)
                        val legs = route.getJSONArray("legs")
                        val leg = legs.getJSONObject(0)
                        val steps = leg.getJSONArray("steps")
                        // Prepare the train schedule details
                        val trainList = mutableListOf<String>()
                        for (i in 0 until steps.length()) {
                            val step = steps.getJSONObject(i)
                            if (step.has("transit_details")) {
                                val transitDetails = step.getJSONObject("transit_details")
                                val line = transitDetails.getJSONObject("line")
                                val trainName = line.getString("short_name")
                                val departureTime = transitDetails.getJSONObject("departure_time").getString("text")
                                trainList.add("$trainName at $departureTime")
                            }
                        }
                        // Prepare the response text
                        val responseText = if (trainList.isNotEmpty()) {
                            "Here is the train schedule: ${trainList.joinToString(", ")}"
                        } else {
                            "No train schedules found for this route."
                        }
                        // Use text-to-speech to announce the schedule
                        withContext(Dispatchers.Main) {
                            textToSpeech.speak(responseText, TextToSpeech.QUEUE_FLUSH, null, null)
                            onResponse(responseText)
                        }
                    } else {
                        val noDataResponse = "No train schedule data available for this route."
                        withContext(Dispatchers.Main) {
                            textToSpeech.speak(noDataResponse, TextToSpeech.QUEUE_FLUSH, null, null)
                            onResponse(noDataResponse)
                        }
                    }
                } else {
                    val noDataResponse = "I couldn't fetch the train schedule. Please try again."
                    withContext(Dispatchers.Main) {
                        textToSpeech.speak(noDataResponse, TextToSpeech.QUEUE_FLUSH, null, null)
                        onResponse(noDataResponse)
                    }
                }
            } else {
                val errorResponse = "Failed to fetch train schedule. Please try again."
                withContext(Dispatchers.Main) {
                    textToSpeech.speak(errorResponse, TextToSpeech.QUEUE_FLUSH, null, null)
                    onResponse(errorResponse)
                }
            }
        } catch (e: Exception) {
            // Handle any exception that occurs during the API request or parsing
            val error = "An error occurred while fetching the train schedule."
            withContext(Dispatchers.Main) {
                textToSpeech.speak(error, TextToSpeech.QUEUE_FLUSH, null, null)
                onResponse(error)
            }
        }
    }
}

fun fetchBusSchedule(
    origin: String,
    destination: String,
    textToSpeech: android.speech.tts.TextToSpeech,
    onResponse: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val apiKey = "AIzaSyAfZHEdNzlzUH0TTGDh9ICtBmKTajfrJQ0" // Replace with your API key
            val url = "https://maps.googleapis.com/maps/api/directions/json?origin=${Uri.encode(origin)}&destination=${Uri.encode(destination)}&mode=transit&transit_mode=bus&key=$apiKey"
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val jsonResponse = JSONObject(responseBody)
                    // Parse bus schedule data
                    val routes = jsonResponse.getJSONArray("routes")
                    val busSchedules = mutableListOf<String>()
                    for (i in 0 until routes.length()) {
                        val route = routes.getJSONObject(i)
                        val legs = route.getJSONArray("legs")
                        for (j in 0 until legs.length()) {
                            val leg = legs.getJSONObject(j)
                            val steps = leg.getJSONArray("steps")
                            for (k in 0 until steps.length()) {
                                val step = steps.getJSONObject(k)
                                if (step.has("transit_details")) {
                                    val transitDetails = step.getJSONObject("transit_details")
                                    val line = transitDetails.getJSONObject("line")
                                    val busName = line.getString("name")
                                    val departureTime = transitDetails.getJSONObject("departure_time").getString("text")
                                    busSchedules.add("Bus $busName departs at $departureTime")
                                }
                            }
                        }
                    }
                    val responseText = if (busSchedules.isNotEmpty()) {
                        "Here is the bus schedule: ${busSchedules.joinToString(", ")}"
                    } else {
                        "No bus schedules available for this route."
                    }
                    withContext(Dispatchers.Main) {
                        textToSpeech.speak(responseText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                        onResponse(responseText)
                    }
                } else {
                    val noDataResponse = "I couldn't fetch the bus schedule. Please try again."
                    withContext(Dispatchers.Main) {
                        textToSpeech.speak(noDataResponse, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                        onResponse(noDataResponse)
                    }
                }
            } else {
                val errorResponse = "Failed to fetch bus schedule. Please try again."
                withContext(Dispatchers.Main) {
                    textToSpeech.speak(errorResponse, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                    onResponse(errorResponse)
                }
            }
        } catch (e: Exception) {
            val error = "An error occurred while fetching the bus schedule."
            withContext(Dispatchers.Main) {
                textToSpeech.speak(error, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                onResponse(error)
            }
        }
    }
}

fun getNearbyRestaurants(
    context: Context,
    textToSpeech: android.speech.tts.TextToSpeech,
    onResponse: (String) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    // Check for location permissions
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        val permissionRequest = "Please enable location permissions."
        textToSpeech.speak(permissionRequest, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
        onResponse(permissionRequest)
        return
    }

    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude
            val apiKey = "AIzaSyDnFqQ0lquYBKjzOBkFbeBa9OO6vuYeNn0" // Replace with your API key
            val radius = 5000 // Search within a 5 km radius
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location=$latitude,$longitude&radius=$radius&type=restaurant&key=$apiKey"
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val client = OkHttpClient()
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            val jsonResponse = JSONObject(responseBody)
                            val results = jsonResponse.getJSONArray("results")
                            if (results.length() > 0) {
                                val restaurants = mutableListOf<String>()
                                for (i in 0 until results.length()) {
                                    val place = results.getJSONObject(i)
                                    val name = place.getString("name")
                                    restaurants.add(name)
                                }
                                val responseText = "Here are some restaurants near you: " +
                                        restaurants.joinToString(", ")
                                withContext(Dispatchers.Main) {
                                    textToSpeech.speak(responseText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                                    onResponse(responseText)
                                }
                            } else {
                                val noResults = "I couldn't find any restaurants near your location."
                                withContext(Dispatchers.Main) {
                                    textToSpeech.speak(noResults, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                                    onResponse(noResults)
                                }
                            }
                        }
                    } else {
                        val error = "Failed to fetch restaurants. Please try again."
                        withContext(Dispatchers.Main) {
                            textToSpeech.speak(error, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                            onResponse(error)
                        }
                    }
                } catch (e: Exception) {
                    val error = "An error occurred while fetching nearby restaurants."
                    withContext(Dispatchers.Main) {
                        textToSpeech.speak(error, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                        onResponse(error)
                    }
                }
            }
        } else {
            val error = "Unable to get your location. Please try again."
            textToSpeech.speak(error, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
            onResponse(error)
        }
    }
}

fun getNearbyPlaces(
    context: Context,
    type: String,
    textToSpeech: android.speech.tts.TextToSpeech,
    onResponse: (String) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    // Check for location permissions
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        val permissionRequest = "Please enable location permissions."
        textToSpeech.speak(permissionRequest, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
        onResponse(permissionRequest)
        return
    }
    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
        if (location != null) {
            val latitude = location.latitude
            val longitude = location.longitude
            val apiKey = "AIzaSyDnFqQ0lquYBKjzOBkFbeBa9OO6vuYeNn0" // Replace with your API key
            val radius = 5000 // Search within a 5 km radius
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                    "location=$latitude,$longitude&radius=$radius&type=$type&key=$apiKey"
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val client = OkHttpClient()
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            val jsonResponse = JSONObject(responseBody)
                            val results = jsonResponse.getJSONArray("results")
                            if (results.length() > 0) {
                                val places = mutableListOf<String>()
                                for (i in 0 until results.length()) {
                                    val place = results.getJSONObject(i)
                                    val name = place.getString("name")
                                    places.add(name)
                                }
                                val responseText = "Here are some $type places near you: " +
                                        places.joinToString(", ")
                                withContext(Dispatchers.Main) {
                                    textToSpeech.speak(responseText, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                                    onResponse(responseText)
                                }
                            } else {
                                val noResults = "I couldn't find any $type places near your location."
                                withContext(Dispatchers.Main) {
                                    textToSpeech.speak(noResults, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                                    onResponse(noResults)
                                }
                            }
                        }
                    } else {
                        val error = "Failed to fetch places. Please try again."
                        withContext(Dispatchers.Main) {
                            textToSpeech.speak(error, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                            onResponse(error)
                        }
                    }
                } catch (e: Exception) {
                    val error = "An error occurred while fetching nearby places."
                    withContext(Dispatchers.Main) {
                        textToSpeech.speak(error, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                        onResponse(error)
                    }
                }
            }
        } else {
            val error = "Unable to get your location. Please try again."
            textToSpeech.speak(error, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
            onResponse(error)
        }
    }
}

fun geocodeLocation(
    locationName: String,
    context: Context,
    callback: (latitude: Double?, longitude: Double?) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val apiKey = "AIzaSyBchckkCwymTEjbrPiGmCYBkeDqsK3wKSU"
            val url = "https://maps.googleapis.com/maps/api/geocode/json?address=${Uri.encode(locationName)}&key=$apiKey"
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        val jsonResponse = JSONObject(responseBody)
                        val results = jsonResponse.getJSONArray("results")

                        if (results.length() > 0) {
                            val location = results.getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                            val latitude = location.getDouble("lat")
                            val longitude = location.getDouble("lng")

                            withContext(Dispatchers.Main) {
                                callback(latitude, longitude)
                            }
                            return@use
                        }
                    }
                }
                withContext(Dispatchers.Main) {
                    callback(null, null)
                }
            }
        } catch (e: Exception) {
            Log.e("GeocodeError", e.message ?: "Error occurred")
            withContext(Dispatchers.Main) {
                callback(null, null)
            }
        }
    }
}

fun openUberApp(context: Context, destinationLatitude: Double, destinationLongitude: Double) {
    val uberUri = "uber://?action=setPickup&pickup=my_location&dropoff[latitude]=$destinationLatitude&dropoff[longitude]=$destinationLongitude"

    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uberUri))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        val browserUri = "https://m.uber.com/ul/?action=setPickup&pickup=my_location&dropoff[latitude]=$destinationLatitude&dropoff[longitude]=$destinationLongitude"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(browserUri))
        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(browserIntent)
        } catch (browserException: Exception) {
            Toast.makeText(context, "Unable to open Uber app or browser.", Toast.LENGTH_SHORT).show()
        }
    }
}

fun handleCallCommand(
    command: String,
    context: android.content.Context,
    textToSpeech: android.speech.tts.TextToSpeech,
    onResponse: (String) -> Unit
) {
    val regex = Regex("call (.+)", RegexOption.IGNORE_CASE)
    val matchResult = regex.find(command)
    val contactName = matchResult?.groups?.get(1)?.value
    if (contactName != null) {
        val resolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$contactName%"),
            null
        )
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            if (nameIndex >= 0 && numberIndex >= 0 && it.moveToFirst()) {
                val phoneNumber = it.getString(numberIndex)
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        context as Activity,
                        arrayOf(Manifest.permission.CALL_PHONE),
                        1
                    )
                } else {
                    try {
                        context.startActivity(intent)
                        val response = "Calling $contactName."
                        speakAndRespond(response, textToSpeech, onResponse)
                    } catch (e: ActivityNotFoundException) {
                        val response = "Unable to make the call. Please try again."
                        speakAndRespond(response, textToSpeech, onResponse)
                    }
                }
            } else {
                val response = "I couldn't find $contactName in your contacts."
                speakAndRespond(response, textToSpeech, onResponse)
            }
        }
    } else {
        val response = "Please specify the contact name to call."
        speakAndRespond(response, textToSpeech, onResponse)
    }
}fun handleMessageCommand(
    command: String,
    context: android.content.Context,
    textToSpeech: android.speech.tts.TextToSpeech,
    onResponse: (String) -> Unit
) {
    val regex = Regex("send a message to (.+) saying (.+)", RegexOption.IGNORE_CASE)
    val matchResult = regex.find(command)
    val contactName = matchResult?.groups?.get(1)?.value
    val message = matchResult?.groups?.get(2)?.value

    if (contactName != null && message != null) {
        val resolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$contactName%"),
            null
        )
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            if (nameIndex >= 0 && numberIndex >= 0 && it.moveToFirst()) {
                val phoneNumber = it.getString(numberIndex)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("sms:$phoneNumber")
                    putExtra("sms_body", message)
                }
                try {
                    context.startActivity(intent)
                    val response = "Sending a message to $contactName."
                    speakAndRespond(response, textToSpeech, onResponse)
                } catch (e: ActivityNotFoundException) {
                    val response = "Unable to send the message. Please try again."
                    speakAndRespond(response, textToSpeech, onResponse)
                }
            } else {
                val response = "I couldn't find $contactName in your contacts."
                speakAndRespond(response, textToSpeech, onResponse)
            }
        }
    } else {
        val response = "Please specify both the contact name and the message to send."
        speakAndRespond(response, textToSpeech, onResponse)
    }
}

fun askGeminiAPI(question: String, textToSpeech: android.speech.tts.TextToSpeech, onResponse: (String) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val model = GenerativeModel(
                "gemini-2.0-flash-exp",
                "AIzaSyC930rmU5zGtYo_Qahl-bU2P5FALz9iDgw",
                generationConfig = generationConfig {
                    temperature = 1f
                    topK = 40
                    topP = 0.95f
                    maxOutputTokens = 8192
                    responseMimeType = "text/plain"
                },
            )
            val chat = model.startChat()
            val response = chat.sendMessage(question)
            var result = response.text?.replace("*", "") ?: ""
            result = result.replace("**", "")
            withContext(Dispatchers.Main) {
                textToSpeech.speak(result, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                if (result.isNotEmpty()) {
                    onResponse(result)
                }
            }
        } catch (e: Exception) {
            val error = "Exception: ${e.message}"
            withContext(Dispatchers.Main) {
                textToSpeech.speak(error, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
                onResponse(error)
            }
        }
    }
}

fun speakAndRespond(response: String, textToSpeech: android.speech.tts.TextToSpeech, onResponse: (String) -> Unit) {
    textToSpeech.speak(response, android.speech.tts.TextToSpeech.QUEUE_FLUSH, null, null)
    onResponse(response)
}
