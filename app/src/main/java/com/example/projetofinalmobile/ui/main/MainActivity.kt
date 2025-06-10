package com.example.projetofinalmobile.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.projetofinalmobile.R
import com.example.projetofinalmobile.databinding.ActivityMainBinding
import com.example.projetofinalmobile.model.UserInventoryItem
import com.example.projetofinalmobile.ui.history.HistoryActivity
import com.example.projetofinalmobile.ui.store.StoreActivity
import com.example.projetofinalmobile.viewmodel.FocusState
import com.example.projetofinalmobile.viewmodel.MainViewModel
import com.example.projetofinalmobile.viewmodel.MusicState
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    // mapeia o ID de uma skin para a lista de imagens de seus estágios
    private val plantSkins = mapOf(
        "default_plant" to listOf(
            R.drawable.planta_estagio1,
            R.drawable.planta_estagio2,
            R.drawable.planta_estagio3
        ),
        "cacto" to listOf(R.drawable.cacto1, R.drawable.cacto2, R.drawable.cacto3),
        "girassol" to listOf(R.drawable.girassol1, R.drawable.girassol2, R.drawable.girassol3)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupBottomNavigation()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.buttonFocus30Min.setOnClickListener { startFocusSessionWithDuration(30L) }
        binding.buttonFocus1Hr.setOnClickListener { startFocusSessionWithDuration(60L) }
        binding.buttonFocus2Hr.setOnClickListener { startFocusSessionWithDuration(120L) }

        binding.buttonCancelFocus.setOnClickListener {
            viewModel.startOrCancelFocusSession()
            Toast.makeText(this, "Sessão de foco cancelada!", Toast.LENGTH_SHORT).show()
        }

        binding.buttonPlayMusic.setOnClickListener { viewModel.playMusic() }
        binding.buttonPauseMusic.setOnClickListener { viewModel.pauseMusic() }
        binding.buttonStopMusic.setOnClickListener { viewModel.stopMusic() }
    }

    private fun startFocusSessionWithDuration(durationMinutes: Long) {
        viewModel.setFocusDuration(durationMinutes)
        viewModel.startOrCancelFocusSession()
    }

    private fun observeViewModel() {
        // observa o estagio visual pra atualizar a imagem
        viewModel.visualPlantStage.observe(this) { stage ->
            val skinId = viewModel.plantState.value?.currentPlantSkin
            updatePlantImage(stage, skinId)
        }

        // observa o estado de focol pra atualizar a os botoes
        viewModel.focusState.observe(this) { state ->
            updateUIVisibility(state)
            if (state != FocusState.RUNNING) {
                binding.textViewTimer.text = "Tempo focado total: ${viewModel.plantState.value?.totalActiveMinutes ?: 0} minutos"
            }
        }

        // observa o tempo restante pra atualiza ro timer
        viewModel.focusTimeRemainingSeconds.observe(this) { secondsRemaining ->
            if (viewModel.focusState.value == FocusState.RUNNING) {
                binding.textViewTimer.text = "Foco: ${formatSecondsToMMSS(secondsRemaining)}"
            }
        }

        // observa o estado da planta para atulizar pontos e minutos
        viewModel.plantState.observe(this) { state ->
            state?.let {
                binding.textViewUserPoints.text = "Pontos: ${it.userPoints}"
                if (viewModel.focusState.value != FocusState.RUNNING) {
                    binding.textViewTimer.text = "Tempo focado total: ${it.totalActiveMinutes} minutos"
                }
            }
        }

        // exibe citacao
        viewModel.quote.observe(this) { quote ->
            quote?.let {
                binding.textViewQuote.text = "\"${it.content}\""
                binding.textViewAuthor.text = "- ${it.author}"
            }
        }

        //musicas
        viewModel.ownedMusic.observe(this) { musicList ->
            setupMusicSpinner(musicList)
        }
        //player da musica
        viewModel.musicState.observe(this) { state ->
            updateMusicControlsUI(state)
        }
    }

    //atualiza a imagem da planta
    private fun updatePlantImage(stage: Int, skinId: String?) {
        val resolvedSkinId = skinId ?: MainViewModel.DEFAULT_PLANT_SKIN
        val images = plantSkins[resolvedSkinId] ?: plantSkins[MainViewModel.DEFAULT_PLANT_SKIN]!!
        val imageRes = images[stage.coerceIn(0, images.size - 1)]
        Glide.with(this).load(imageRes).into(binding.imageViewPlant)
    }

    //controla a visibilidade dos botoes iniciar/cancelar foco
    private fun updateUIVisibility(currentFocusState: FocusState?) {
        val inFocusSession = currentFocusState == FocusState.RUNNING
        val canStartSession = currentFocusState == FocusState.IDLE || currentFocusState == FocusState.FINISHED || currentFocusState == null

        binding.layoutFocusDurationSelection.visibility = if (canStartSession) View.VISIBLE else View.GONE
        binding.buttonCancelFocus.visibility = if (inFocusSession) View.VISIBLE else View.GONE
    }

    // barra de navegacao inferior
    private fun setupBottomNavigation() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> true
                R.id.navigation_history -> {
                    startActivity(Intent(this, HistoryActivity::class.java)); true
                }
                R.id.navigation_store -> {
                    startActivity(Intent(this, StoreActivity::class.java)); true
                }
                else -> false
            }
        }
        binding.bottomNavigationView.setOnItemReselectedListener { /* Não faz nada */ }
    }
    override fun onResume() {
        super.onResume()
        viewModel.onAppForeground()
        viewModel.fetchQuoteIfNeeded()
        binding.bottomNavigationView.menu.findItem(R.id.navigation_home)?.isChecked = true
    }
    override fun onPause() {
        super.onPause()
        viewModel.onAppBackground()
    }

    //formata o horario
    private fun formatSecondsToMMSS(seconds: Long): String {
        val minutes = TimeUnit.SECONDS.toMinutes(seconds)
        val remainingSeconds = seconds - TimeUnit.MINUTES.toSeconds(minutes)
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    private fun setupMusicSpinner(musicList: List<UserInventoryItem>) {
        if (musicList.isEmpty()) {
            binding.layoutMusicControls.visibility = View.GONE
            return
        }
        binding.layoutMusicControls.visibility = View.VISIBLE
        val musicNames = musicList.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, musicNames).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerMusicSelection.adapter = adapter
        binding.spinnerMusicSelection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                viewModel.setSelectedMusic(musicList[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun updateMusicControlsUI(state: MusicState?) {
        when (state) {
            MusicState.PLAYING -> {
                binding.buttonPlayMusic.visibility = View.GONE
                binding.buttonPauseMusic.visibility = View.VISIBLE
                binding.buttonStopMusic.visibility = View.VISIBLE
            }
            MusicState.PAUSED -> {
                binding.buttonPlayMusic.visibility = View.VISIBLE
                binding.buttonPauseMusic.visibility = View.GONE
                binding.buttonStopMusic.visibility = View.VISIBLE
            }
            else -> {
                binding.buttonPlayMusic.visibility = View.VISIBLE
                binding.buttonPauseMusic.visibility = View.GONE
                binding.buttonStopMusic.visibility = View.GONE
            }
        }
    }
}