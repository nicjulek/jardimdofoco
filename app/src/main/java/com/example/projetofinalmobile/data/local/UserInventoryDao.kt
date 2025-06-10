package com.example.projetofinalmobile.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.projetofinalmobile.model.UserInventoryItem

@Dao
interface UserInventoryDao {
    // adiciona um item ao inventário e se já existir, substitui.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItem(item: UserInventoryItem)

    // Retorna todos os itens do inventário
    @Query("SELECT * FROM user_inventory")
    fun getAllItems(): LiveData<List<UserInventoryItem>>

    // busca um item pelo seu ID
    @Query("SELECT * FROM user_inventory WHERE itemId = :itemId")
    suspend fun getItemById(itemId: String): UserInventoryItem?

    //verifica se um item com um determinado ID existe no inventário
    @Query("SELECT EXISTS(SELECT 1 FROM user_inventory WHERE itemId = :itemId LIMIT 1)")
    suspend fun hasItem(itemId: String): Boolean

    // Retorna todos os itens de um tipo específico
    @Query("SELECT * FROM user_inventory WHERE itemType = :type")
    fun getItemsByType(type: String): LiveData<List<UserInventoryItem>>
}