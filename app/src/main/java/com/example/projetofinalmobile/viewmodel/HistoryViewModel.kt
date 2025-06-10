package com.example.projetofinalmobile.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.example.projetofinalmobile.data.local.AppDatabase
import com.example.projetofinalmobile.model.StudySprint

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    // expõe a lista de sprints como LiveData. a activity irá observar este LiveData para receber atualizações automaticamente.
    private val studySprintDao = AppDatabase.getDatabase(application).studySprintDao()
    val allSprints: LiveData<List<StudySprint>> = studySprintDao.getAllSprints()
}