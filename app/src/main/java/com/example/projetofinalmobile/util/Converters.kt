package com.example.projetofinalmobile.util

import androidx.room.TypeConverter
import com.example.projetofinalmobile.model.ItemType

class Converters {
    //converte um objeto ItemType para uma String
    @TypeConverter
    fun fromItemType(value: ItemType): String {
        return value.name
    }
    // converte uma String de volta para um objeto ItemType
    @TypeConverter
    fun toItemType(value: String): ItemType {
        return ItemType.valueOf(value)
    }
}