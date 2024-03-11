package com.example.docbuddypoc

data class Message(
    val content: String,
    val botReply: String,
    val userMessage: String = "",
    val voiceNotePath: String = ""
)

