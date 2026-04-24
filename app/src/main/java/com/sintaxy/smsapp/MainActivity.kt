package com.sintaxy.smsapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.sintaxy.smsapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) sendSms() else showToast("Permissão de SMS negada")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSend.setOnClickListener {
            val number = binding.etNumber.text.toString().trim()
            val message = binding.etMessage.text.toString().trim()

            when {
                number.isEmpty() -> showToast("Informe o número de destino")
                message.isEmpty() -> showToast("Escreva uma mensagem")
                else -> checkPermissionAndSend()
            }
        }

        binding.btnClear.setOnClickListener {
            binding.etNumber.text?.clear()
            binding.etMessage.text?.clear()
        }
    }

    private fun checkPermissionAndSend() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED -> sendSms()
            else -> requestPermissionLauncher.launch(Manifest.permission.SEND_SMS)
        }
    }

    private fun sendSms() {
        val number = binding.etNumber.text.toString().trim()
        val message = binding.etMessage.text.toString().trim()

        try {
            val smsManager: SmsManager = if (android.os.Build.VERSION.SDK_INT >= 31) {
                getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            // Divide a mensagem automaticamente se passar de 160 caracteres
            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(number, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(number, null, message, null, null)
            }

            showToast("SMS enviado para $number")
            binding.etMessage.text?.clear()

        } catch (e: Exception) {
            showToast("Erro ao enviar: ${e.localizedMessage}")
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
