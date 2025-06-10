package com.example.projetofinalmobile.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.projetofinalmobile.model.StudySprint

@Dao
interface StudySprintDao {
    //insere um novo 'sprint' (sessão de foco) no banco
    @Insert
    suspend fun insert(sprint: StudySprint)

    //retorna todas as sessões de foco
    @Query("SELECT * FROM study_sprints ORDER BY timestamp DESC")
    fun getAllSprints(): LiveData<List<StudySprint>>

    //retorna um snapshot de todas as sessões de foco
    @Query("SELECT * FROM study_sprints ORDER BY timestamp DESC")
    suspend fun getAllSprintsSnapshot(): List<StudySprint>
}