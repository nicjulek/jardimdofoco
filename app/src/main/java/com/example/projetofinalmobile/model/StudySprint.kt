package com.example.projetofinalmobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sprints")
data class StudySprint(
    @PrimaryKey(autoGenerate = true) //chave primaria gerada automaticamente
    val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(), //qunado a sessão foi completada
    val durationMinutes: Long //duracao em minutos
)