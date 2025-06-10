package com.example.projetofinalmobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "plant_state")
data class PlantState(
    @PrimaryKey val id: Int = 1,
    var currentStage: Int = 0, // 0,1 e 2
    var lastUpdateTime: Long = System.currentTimeMillis(), //tempo da ultima att
    var userPoints: Long = 0L, //pontos
    var totalActiveMinutes: Long = 0L, //total de minutos focados
    var currentPlantSkin: String = "default_plant" //skin
)