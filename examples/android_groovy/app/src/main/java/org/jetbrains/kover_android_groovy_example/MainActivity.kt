package org.jetbrains.kover_android_groovy_example

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import org.jetbrains.kover_android_groovy_example.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = MainActivityBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val viewModel = ViewModelProvider(this, ViewModelFactory)[MainViewModel::class.java]

        binding.mainEditTextName.addTextChangedListener {
            viewModel.onUserNameChanged(it?.toString())
        }

        viewModel.messageLiveData.observe(this) {
            binding.mainTextViewMessage.text = it
        }
    }
}