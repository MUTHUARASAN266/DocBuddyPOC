package com.example.docbuddypoc

import android.media.MediaPlayer
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.docbuddypoc.databinding.ItemMessageBinding
import java.io.File
import java.io.FileInputStream
import java.io.IOException

class MessageAdapter(val messages: MutableList<Message>) :
    RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    companion object {
        const val TAG = "MessageAdapter"
    }

    inner class ViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.audioMessage.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val message = messages[position]
                    if (message.voiceNotePath.isNotEmpty()) {
                        playVoiceNote(message.voiceNotePath)
                    }
                }
            }
        }

        fun bind(message: Message) {
            binding.userMessage.text = message.content
            binding.botMessage.text = message.botReply
            if (message.voiceNotePath.isNotEmpty()) {
                binding.userMessage.text = "Voice Note"
                binding.botMessage.text = ""
                binding.audioMessage.visibility = View.VISIBLE
            } else {
                binding.audioMessage.visibility = View.GONE
            }
        }

        private fun playVoiceNote(voiceNotePath: String) {
            val mediaPlayer = MediaPlayer()
            try {
                val file = File(voiceNotePath)

                if (!file.exists() || !file.isFile) {
                    Log.e(TAG, "File does not exist or is not a file at path: $voiceNotePath")
                }

                val fileDescriptor = FileInputStream(file).fd

                mediaPlayer.apply {
                    setDataSource(fileDescriptor)
                    prepare()
                    start()
                    setOnCompletionListener {
                        release()
                    }
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error playing voice note: ${e.message}")
            }
        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageAdapter.ViewHolder {
        val layoutInflater =
            ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(layoutInflater)
    }

    override fun onBindViewHolder(holder: MessageAdapter.ViewHolder, position: Int) {
        val data = messages[position]
        holder.bind(data)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    fun addMessage(message: Message) {
        messages.add(message)
        notifyDataSetChanged()
    }
}
