package com.example.projetofinalmobile.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_inventory")
data class UserInventoryItem(
    @PrimaryKey val itemId: String,
    val itemType: ItemType, //tipo
    val name: String,
    val resourceId: String? = null // Para skins de planta/música
)