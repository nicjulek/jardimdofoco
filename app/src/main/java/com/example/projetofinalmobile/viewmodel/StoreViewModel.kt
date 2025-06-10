package com.example.projetofinalmobile.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.projetofinalmobile.data.local.AppDatabase
import com.example.projetofinalmobile.model.ItemType
import com.example.projetofinalmobile.model.StoreItem
import com.example.projetofinalmobile.model.UserInventoryItem
import kotlinx.coroutines.launch

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val plantDao = AppDatabase.getDatabase(application).plantDao()
    private val userInventoryDao = AppDatabase.getDatabase(application).userInventoryDao()

    private val _plantStateSource = plantDao.getPlantState()
    private val _userPoints = MediatorLiveData<Long>()
    val userPoints: LiveData<Long> = _userPoints

    private val _userInventorySource = userInventoryDao.getAllItems()

    private val _displayStoreItems = MediatorLiveData<List<StoreItem>>()
    val displayStoreItems: LiveData<List<StoreItem>> = _displayStoreItems

    private val _purchaseStatus = MutableLiveData<String?>()
    val purchaseStatus: LiveData<String?> = _purchaseStatus

    private val initialShopListing = listOf(
        StoreItem(
            id = "default_plant_skin",
            name = "Skin Padrão",
            description = "Visual original da planta.",
            price = 0,
            type = ItemType.PLANT_SKIN,
            resourceId = "default_plant"
        ),
        StoreItem("musica1", "Música 1", "Música para relaxar.", 50, ItemType.MUSIC),
        StoreItem("musica2", "Música 2", "Batidas para concentrar.", 75, ItemType.MUSIC),
        StoreItem("cacto", "Skin: Cacto", "Um visual de cacto.", 170, ItemType.PLANT_SKIN, resourceId = "cacto"),
        StoreItem("girassol", "Skin: Girassol", "Um visual de girassol.", 200, ItemType.PLANT_SKIN, resourceId = "girassol")
    )

    init {
        _userPoints.addSource(_plantStateSource) { plantState ->
            _userPoints.value = plantState?.userPoints ?: 0L
        }

        // função que será chamada sempre que os pontos do usuário ou o inventário mudarem
        val updateDisplayItemsList = {
            val currentPointsValue = _userPoints.value ?: 0L
            val currentInventoryList = _userInventorySource.value ?: emptyList()

            _displayStoreItems.value = initialShopListing.map { shopItem ->
                val isOwned = currentInventoryList.any { invItem -> invItem.itemId == shopItem.id } || shopItem.id == "default_plant_skin"

                shopItem.copy(
                    owned = isOwned,
                    canAfford = currentPointsValue >= shopItem.price
                )
            }
        }

        _displayStoreItems.addSource(_userPoints) { updateDisplayItemsList() }
        _displayStoreItems.addSource(_userInventorySource) { updateDisplayItemsList() }

        //carrega o estado inicial
        updateDisplayItemsList()
    }

    //compra de item
    fun buyItem(item: StoreItem) {
        viewModelScope.launch {
            val currentPlantState = plantDao.getPlantStateSnapshot()
            if (currentPlantState == null) {
                _purchaseStatus.postValue("Erro: Estado da planta não encontrado.")
                return@launch
            }

            if (userInventoryDao.hasItem(item.id)) {
                _purchaseStatus.postValue("Você já possui este item!")
                return@launch
            }

            if (currentPlantState.userPoints >= item.price) {
                val newPoints = currentPlantState.userPoints - item.price
                plantDao.insertOrUpdate(currentPlantState.copy(userPoints = newPoints))

                val inventoryItem = UserInventoryItem(
                    itemId = item.id,
                    itemType = item.type,
                    name = item.name,
                    resourceId = item.resourceId
                )
                userInventoryDao.addItem(inventoryItem)
                _purchaseStatus.postValue("${item.name} comprado com sucesso!")
                Log.d("StoreViewModel", "Item ${item.name} comprado. Pontos restantes: $newPoints")
            } else {
                _purchaseStatus.postValue("Pontos insuficientes para comprar ${item.name}.")
            }
        }
    }

    //aplica a skin
    fun applyPlantSkin(item: StoreItem) {
        if (item.type == ItemType.PLANT_SKIN && item.resourceId != null) {
            viewModelScope.launch {
                val hasItem = userInventoryDao.hasItem(item.id) || item.id == "default_plant_skin"

                if (hasItem) {
                    val currentState = plantDao.getPlantStateSnapshot()
                    currentState?.let {
                        plantDao.insertOrUpdate(
                            it.copy(
                                currentPlantSkin = item.resourceId,
                                lastUpdateTime = System.currentTimeMillis()
                            )
                        )
                        Log.d("StoreViewModel", "Skin da planta alterada para ${item.resourceId}")
                        _purchaseStatus.postValue("Skin ${item.name} aplicada!")
                    } ?: run {
                        _purchaseStatus.postValue("Erro ao aplicar skin: estado da planta não encontrado.")
                        Log.e("StoreViewModel", "Falha ao aplicar skin, estado da planta é nulo.")
                    }
                } else {
                    _purchaseStatus.postValue("Você não possui a skin ${item.name}.")
                }
            }
        } else {
            _purchaseStatus.postValue("Este item não é uma skin de planta aplicável.")
        }
    }

    fun clearPurchaseStatus() {
        _purchaseStatus.value = null
    }

    //add pontos para teste
    fun addTestPoints() {
        viewModelScope.launch {
            val currentPlantState = plantDao.getPlantStateSnapshot()
            currentPlantState?.let {
                val newPoints = it.userPoints + 50
                plantDao.insertOrUpdate(it.copy(userPoints = newPoints))
                Log.d("StoreViewModel", "50 pontos de teste adicionados. Novo total: $newPoints")
            }
        }
    }
}