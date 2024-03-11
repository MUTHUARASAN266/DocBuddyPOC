package com.example.docbuddypoc

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract.Directory
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.docbuddypoc.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    lateinit var binding: ActivityMainBinding
    private val RECORD_AUDIO_PERMISSION_CODE = 1
    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognizerIntent: Intent
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var outputFile: File
    private var isListening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                RECORD_AUDIO_PERMISSION_CODE
            )
        }

//        outputFile = File(Environment.getExternalStorageDirectory().absolutePath + "/recording.mp3")
        outputFile = File(getExternalFilesDir(Environment.DIRECTORY_MUSIC), "recording.mp3")
//        outputFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)

        mediaRecorder = MediaRecorder()

        mediaPlayer=MediaPlayer()

        textToSpeech = TextToSpeech(this, this) // initializeTextToSpeech

        binding.speechButton.setOnClickListener {
            speakOut(binding.editTextMessage.text.toString())
        }

        speechRecognizer =
            SpeechRecognizer.createSpeechRecognizer(this) // initialize SpeechRecognizer
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        setupRecycleview()
        binding.buttonSend.setOnClickListener {
            sendMessage()
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.e(TAG, "onReadyForSpeech: $params")
            }

            override fun onBeginningOfSpeech() {
                Log.e(TAG, "onBeginningOfSpeech: ")
            }

            override fun onRmsChanged(rmsdB: Float) {
                Log.e(TAG, "onRmsChanged: $rmsdB")
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                Log.e(TAG, "onBufferReceived: $buffer")
            }

            override fun onEndOfSpeech() {
                Log.e(TAG, "onEndOfSpeech: ")
                binding.startButton.text = "Start Listening"
                stopSpeechRecognition()
            }

            override fun onError(error: Int) {
                // Handle recognition errors
                Toast.makeText(applicationContext, "Error occurred", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "onError: $error")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    binding.textView.text = matches[0]
                    binding.editTextMessage.setText(matches[0])
                    Log.e(TAG, "onResults: ${matches[0]}")
                    sendMessage()
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                Log.e(TAG, "onPartialResults: $partialResults")
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                Log.e(TAG, "onEvent: eventType: $eventType , $params")
            }
        })


        binding.startButton.setOnClickListener {
            if (isListening) {
                stopSpeechRecognition()
//                stopVoiceNote()
            } else {
                startSpeechRecognition()
//                startVoiceNote()
            }
        }

        var click=0
        binding.voiceNoteBtn.setOnClickListener {
//            if (isListening) {
//                stopVoiceNote()
//            } else {
//                startVoiceNote()
//            }
            if (click==0){
                click++
                startVoiceNote()
            }else{
                stopVoiceNote()
                click=0
            }
        }

    }

    private fun stopVoiceNote() {
        Log.e(TAG, "stopVoiceNote: ")
        binding.voiceNoteBtn.text="VoiceNote"
        try {
            mediaRecorder.stop()
            mediaRecorder.release()
            //openFilePathMob(outputFile.absolutePath)
        } catch (e: Exception) {
            Log.e(TAG, "stopRecording: ${e.message}")
        }
    }

    @SuppressLint("IntentReset", "QueryPermissionsNeeded")
    private fun openFilePathMob(absolutePath: String) {
        Log.e(TAG, "openFilePathMob: ")
        val newPath = absolutePath.substringBeforeLast("/") // Removes the last segment after the last "/"
        // Replace "file_path_here" with the actual file path

        // Create an intent with action ACTION_VIEW
        val intent = Intent(Intent.ACTION_VIEW)

        // Set the data and type for the intent
        intent.data = Uri.parse(newPath)
        intent.type = "*/*" // You can set specific MIME type if known

        // Check if there's an activity to handle this intent
        if (intent.resolveActivity(packageManager) != null) {
            // Start the activity with the intent
            startActivity(intent)
        } else {
            // Handle the case where no activity can handle the intent
            // or inform the user that no app is available to open the file
        }
    }


    private fun startVoiceNote() {
        Log.e(TAG, "startVoiceNote: ")
        binding.voiceNoteBtn.text="Stop"
        try {
            mediaRecorder.apply {
                reset()
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            Log.e(TAG, "startRecording: ${e.message}")
        }
    }

    private fun sendVoiceNote() {
        Log.e(TAG, "sendVoiceNote: ")
    }

    private fun speakOut(speechText: String) {
        if (speechText.isNotEmpty()) {
            textToSpeech.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "")
        }
    }

    private fun setupRecycleview() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            messageAdapter = MessageAdapter(mutableListOf())
            adapter = messageAdapter
        }

    }

   /* private fun sendMessage() {
        val userMessageContent = binding.editTextMessage.text.toString().trim()
        if (userMessageContent.isNotEmpty()) {
            // Add user message to the adapter
            val botMessageContent = processMessage(userMessageContent)
            Log.e(TAG, "sendMessage: $botMessageContent")
            speakOut(botMessageContent)
            val message = Message(userMessageContent, botMessageContent)
            messageAdapter.addMessage(message)

            // Scroll to the last item in the RecyclerView
            binding.recyclerView.scrollToPosition(messageAdapter.itemCount - 1)

            // Clear the input field
            binding.editTextMessage.text?.clear()
        }
    }*/

    private fun sendMessage() {
        val userMessageContent = binding.editTextMessage.text.toString().trim()
        if (userMessageContent.isNotEmpty() || outputFile.exists()) {
            if (outputFile.exists()) {
                // Add user voice note to the adapter
                val message = Message("", "", userMessageContent, outputFile.absolutePath)
                messageAdapter.addMessage(message)
            } else {
                // Add user text message to the adapter
                val botMessageContent = processMessage(userMessageContent)
                speakOut(botMessageContent)
                val message = Message(userMessageContent, botMessageContent)
                messageAdapter.addMessage(message)
            }

            if (outputFile.exists()) {
                // File exists, do something
                Log.e(TAG, "File exists: ${outputFile.absolutePath}")
            } else {
                // File does not exist
                Log.e(TAG, "File does not exist muthuarasan")
            }

            // Scroll to the last item in the RecyclerView
            binding.recyclerView.scrollToPosition(messageAdapter.itemCount - 1)

            // Clear the input field and reset outputFile
            binding.editTextMessage.text?.clear()
            outputFile.delete()
        }
    }



    /*private fun sendMessage() {
        val userMessageContent = binding.editTextMessage.text.toString().trim()
        if (userMessageContent.isNotEmpty() || outputFile.exists()) {
            val outputFileCopy = getUniqueFileCopy(outputFile)
            if (outputFile.exists()) {
                // Add user voice note to the adapter
                val message = Message("", "", userMessageContent, outputFileCopy.absolutePath)
                messageAdapter.addMessage(message)
            } else {
                // Add user text message to the adapter
                val botMessageContent = processMessage(userMessageContent)
                speakOut(botMessageContent)
                val message = Message(userMessageContent, botMessageContent)
                messageAdapter.addMessage(message)
            }

            // Scroll to the last item in the RecyclerView
            binding.recyclerView.scrollToPosition(messageAdapter.itemCount - 1)

            // Clear the input field and reset outputFile
            binding.editTextMessage.text?.clear()
            outputFile.delete()
            outputFileCopy.delete()
        }
    }*/

    private fun getUniqueFileCopy(originalFile: File): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "recording_$timeStamp.mp3"
        Log.e(TAG, "getUniqueFileCopy: $fileName")
        return File(originalFile, fileName)
    }



    private fun processMessage(message: String): String {
        // Simple rule-based logic to generate a response
        return when (message.toLowerCase()) {
            "hello" -> "Hi there!"
            "hi" -> "hey how can i help you?"
            "how are you?" -> "I'm just a chatbot, so I don't have feelings, but thanks for asking!"
            else -> "I'm sorry, I didn't understand that."
        }
    }


    private fun startSpeechRecognition() {
        isListening = true
        binding.startButton.text = "Stop"
        binding.textView.text = "Listening..."
        speechRecognizer.startListening(speechRecognizerIntent)
    }

    private fun stopSpeechRecognition() {
        isListening = false
        binding.startButton.text = "Start Listening"
        binding.textView.text = " "
        speechRecognizer.stopListening()
    }

    override fun onDestroy() {
        speechRecognizer.destroy()

        if (textToSpeech.isSpeaking) {
            textToSpeech.stop()
        }
        textToSpeech.shutdown()
        super.onDestroy()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, do nothing as we already have it.
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language to default locale
            textToSpeech.language = Locale.getDefault()
        } else {
            Toast.makeText(this, "Text to speech initialization failed", Toast.LENGTH_SHORT).show()
        }
    }
}
