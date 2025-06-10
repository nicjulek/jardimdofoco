package com.example.projetofinalmobile.model

//categoriza os tipos de itens na loja
enum class ItemType {
    MUSIC,
    PLANT_SKIN
}

data class StoreItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val type: ItemType, //tipo
    val resourceId: String? = null,
    val owned: Boolean = false, //ja tem?
    val canAfford: Boolean = true //tem pontos pra comprar?
)