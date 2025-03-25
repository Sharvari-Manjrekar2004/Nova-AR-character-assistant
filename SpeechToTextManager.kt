package com.xperiencelabs.astronaut.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

object SpeechToTextManager {
    private lateinit var speechRecognizer: SpeechRecognizer

    fun initialise(context: Context) {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
    }

    fun startListening(
        onSpeechStarted: () -> Unit,
        onSpeechStopped: () -> Unit,
        onSpeechError: (String) -> Unit,
        onSpeechResult: (List<String>) -> Unit
    ) {
        val recognitionListener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {
                onSpeechStarted()
            }
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {
                onSpeechStopped()
            }
            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error."
                    SpeechRecognizer.ERROR_CLIENT -> "Client-side error."
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions."
                    SpeechRecognizer.ERROR_NETWORK -> "Network error."
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout."
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found."
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer is busy."
                    SpeechRecognizer.ERROR_SERVER -> "Server error."
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout."
                    else -> "Unknown error."
                }
                onSpeechError(errorMessage)
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (matches != null) {
                    onSpeechResult(matches)
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US") // Change language as needed
        }

        speechRecognizer.setRecognitionListener(recognitionListener)
        speechRecognizer.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
    }

    fun destroy() {
        speechRecognizer.destroy()
    }
}