package com.example.projetofinalmobile.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projetofinalmobile.model.PlantState

@Dao
interface PlantDao {
    //insere ou atualiza um 'PlantState'
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(plantState: PlantState)

    //seleciona e retorna o estado da planta como LiveData.
    @Query("SELECT * FROM plant_state WHERE id = 1")
    fun getPlantState(): LiveData<PlantState?>

    @Query("SELECT * FROM plant_state WHERE id = 1")
    suspend fun getPlantStateSnapshot(): PlantState?
}