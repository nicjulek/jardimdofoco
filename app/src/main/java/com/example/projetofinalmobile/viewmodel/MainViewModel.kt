package com.example.projetofinalmobile.viewmodel

import android.app.Application
import android.media.MediaPlayer
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.projetofinalmobile.R
import com.example.projetofinalmobile.data.local.AppDatabase
import com.example.projetofinalmobile.model.PlantState
import com.example.projetofinalmobile.model.QuoteResponse
import com.example.projetofinalmobile.model.StudySprint
import com.example.projetofinalmobile.model.UserInventoryItem
import com.example.projetofinalmobile.data.remote.RetrofitInstance
import com.example.projetofinalmobile.util.PreferencesManager
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

//estados da sessão de foco
enum class FocusState { IDLE, RUNNING, PAUSED, FINISHED }
enum class MusicState { STOPPED, PLAYING, PAUSED }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val plantDao = AppDatabase.getDatabase(application).plantDao()
    private val studySprintDao = AppDatabase.getDatabase(application).studySprintDao()
    private val userInventoryDao = AppDatabase.getDatabase(application).userInventoryDao()
    private val preferencesManager = PreferencesManager

    val plantState: LiveData<PlantState?> = plantDao.getPlantState()
    val ownedMusic: LiveData<List<UserInventoryItem>> = userInventoryDao.getItemsByType("MUSIC")

    private val _quote = MutableLiveData<QuoteResponse?>()
    val quote: LiveData<QuoteResponse?> = _quote
    private val _isLoadingQuote = MutableLiveData<Boolean>()
    val isLoadingQuote: LiveData<Boolean> = _isLoadingQuote
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _focusState = MutableLiveData<FocusState>(FocusState.IDLE)
    val focusState: LiveData<FocusState> = _focusState

    private val _focusTimeRemainingSeconds = MutableLiveData<Long>(0L)
    val focusTimeRemainingSeconds: LiveData<Long> = _focusTimeRemainingSeconds

    private val _visualPlantStage = MutableLiveData<Int>(0)
    val visualPlantStage: LiveData<Int> = _visualPlantStage

    private val _musicState = MutableLiveData(MusicState.STOPPED)
    val musicState: LiveData<MusicState> = _musicState
    private val _selectedMusicResId = MutableLiveData<Int?>(null)

    //variaveis de estado interno
    private var countDownTimer: CountDownTimer? = null
    private var currentFocusDurationMillis: Long = 0L
    private val _currentFocusDurationMinutes = MutableLiveData<Long>(30L)
    private var mediaPlayer: MediaPlayer? = null
    private var currentTrackResId: Int? = null

    // mapeia o ID do item de música para o arquivo de áudio real em raw
    private val musicResourceMap = mapOf(
        "musica1" to R.raw.musica,
        "musica2" to R.raw.musica2
    )

    companion object {
        const val MINUTES_PER_STAGE = 60
        const val MAX_STAGES = 2 //0, 1 e 2
        const val DEFAULT_PLANT_SKIN = "default_plant"
        private const val QUOTE_FETCH_COOLDOWN = 24 * 60 * 60 * 1000 //24h para buscar citacao
    }

    init {
        initializePlant()
        fetchQuoteIfNeeded()
    }

    //logica de negocios
    //verifica se a planta já existe no banco. se não, cria uma nova com valores iniciais
    private fun initializePlant() {
        viewModelScope.launch {
            val initialState = plantDao.getPlantStateSnapshot()
            if (initialState == null) {
                val newPlant = PlantState(currentStage = 0, lastUpdateTime = System.currentTimeMillis(), totalActiveMinutes = 0L, userPoints = 0L, currentPlantSkin = DEFAULT_PLANT_SKIN)
                plantDao.insertOrUpdate(newPlant)
                _visualPlantStage.postValue(newPlant.currentStage)
            } else {
                _visualPlantStage.postValue(initialState.currentStage)
            }
        }
    }

    //efine a duração da sessão de foco com base no botão que o usuário clicou
    fun setFocusDuration(minutes: Long) {
        _currentFocusDurationMinutes.value = minutes
        currentFocusDurationMillis = TimeUnit.MINUTES.toMillis(minutes)
    }

    //controla o início e o cancelamento de uma sessão de foco
    fun startOrCancelFocusSession() {
        when (_focusState.value) {
            //se a sessão está rodando, o usuário quer cancelar
            FocusState.RUNNING -> {
                _focusState.value = FocusState.IDLE
                countDownTimer?.cancel()
                _focusTimeRemainingSeconds.value = 0L
                _visualPlantStage.value = plantState.value?.currentStage ?: 0
            }
            //se está ocioso ou finalizado, o usuário quer iniciar uma nova sessão
            FocusState.IDLE, FocusState.FINISHED, null -> {
                _focusState.value = FocusState.RUNNING
                startTimer(currentFocusDurationMillis)
            }
            FocusState.PAUSED -> {
            }
        }
    }

    private fun startTimer(durationMillis: Long) {
        countDownTimer?.cancel()
        _visualPlantStage.value = 0

        val totalDurationSeconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis)

        countDownTimer = object : CountDownTimer(durationMillis, 1000) {
            //chamado a cada segundo
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                _focusTimeRemainingSeconds.value = secondsRemaining

                //calcula o progresso da sessão (0.0 a 1.0)
                val progress = if (totalDurationSeconds > 0) (totalDurationSeconds - secondsRemaining).toFloat() / totalDurationSeconds.toFloat() else 0f

                //se passou da metade do tempo, avança a imagem da planta para o estágio 1
                if (progress >= 0.5f && (_visualPlantStage.value ?: 0) < 1) {
                    _visualPlantStage.postValue(1)
                }
            }

            //chamado quando o timer termina
            override fun onFinish() {
                _visualPlantStage.postValue(2) //estagio final
                _focusState.value = FocusState.FINISHED
                _focusTimeRemainingSeconds.value = 0L

                viewModelScope.launch {
                    val duration = _currentFocusDurationMinutes.value ?: 0L
                    updatePlantStateAfterFocus(duration)
                    studySprintDao.insert(StudySprint(durationMinutes = duration))
                }
            }
        }.start()
    }

    //atualiza os dados permanentes da planta (pontos e minutos totais) após uma sessão de foco bem-sucedida
    private suspend fun updatePlantStateAfterFocus(focusedMinutes: Long) {
        val currentState = plantDao.getPlantStateSnapshot() ?: return
        val newTotalMinutes = currentState.totalActiveMinutes + focusedMinutes
        val newPoints = currentState.userPoints + (focusedMinutes)
        val newPersistentStage = (newTotalMinutes / MINUTES_PER_STAGE).toInt().coerceAtMost(MAX_STAGES)

        plantDao.insertOrUpdate(
            currentState.copy(totalActiveMinutes = newTotalMinutes, userPoints = newPoints, currentStage = newPersistentStage, lastUpdateTime = System.currentTimeMillis())
        )
    }

    //chamado quando o app vai para o background. Cancela a sessão de foco para evitar trapaças
    fun onAppBackground() {
        if (_focusState.value == FocusState.RUNNING || _focusState.value == FocusState.PAUSED) {
            countDownTimer?.cancel()
            _focusState.value = FocusState.IDLE
            _focusTimeRemainingSeconds.value = 0L
        }
        mediaPlayer?.pause()
    }

    //chamado quando o app volta para o primeiro plano
    fun onAppForeground() {
        if (_focusState.value == FocusState.PAUSED) {
            val remainingMillis = TimeUnit.SECONDS.toMillis(_focusTimeRemainingSeconds.value ?: 0L)
            if (remainingMillis > 0) {
                _focusState.value = FocusState.RUNNING
                startTimer(remainingMillis)
            } else {
                _focusState.value = FocusState.IDLE
            }
        }
        if (_musicState.value == MusicState.PLAYING) mediaPlayer?.start()
    }

    // busca uma nova citação da API se o cooldown já passou
    fun fetchQuoteIfNeeded() {
        viewModelScope.launch {
            val lastFetchTime = preferencesManager.getLastQuoteFetchTime(getApplication())
            if (System.currentTimeMillis() - lastFetchTime > QUOTE_FETCH_COOLDOWN || quote.value == null) fetchQuote()
        }
    }

    //executa a chamada de rede para a API de citações
    private fun fetchQuote() {
        _isLoadingQuote.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getRandomZenQuote()
                if (response.isSuccessful && response.body()?.isNotEmpty() == true) {
                    val zenItem = response.body()!!.first()
                    _quote.postValue(QuoteResponse(id = "1", content = zenItem.q, author = zenItem.a))
                    preferencesManager.setLastQuoteFetchTime(getApplication(), System.currentTimeMillis())
                    _error.postValue(null)
                } else {
                    _error.postValue("Erro ao buscar citação: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Falha na conexão: ${e.message}")
            } finally {
                _isLoadingQuote.postValue(false)
            }
        }
    }

    //define a música selecionada no Spinner
    fun setSelectedMusic(inventoryItem: UserInventoryItem) {
        val resId = musicResourceMap[inventoryItem.itemId]
        if (resId != _selectedMusicResId.value) {
            stopMusic()
            _selectedMusicResId.value = resId
        }
    }

    //toca a musica
    fun playMusic() {
        val trackResId = _selectedMusicResId.value ?: return
        if (mediaPlayer?.isPlaying == true && currentTrackResId == trackResId) return
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(getApplication(), trackResId).apply {
            isLooping = true
            start()
        }
        currentTrackResId = trackResId
        _musicState.value = MusicState.PLAYING
    }

    //pausa a musica
    fun pauseMusic() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            _musicState.value = MusicState.PAUSED
        }
    }

    //para a musica
    fun stopMusic() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentTrackResId = null
        _musicState.value = MusicState.STOPPED
    }

    //ViewModel está prestes a ser destruído
    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
        mediaPlayer?.release()
    }
}