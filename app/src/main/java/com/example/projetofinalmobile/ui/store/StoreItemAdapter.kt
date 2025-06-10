package com.example.projetofinalmobile.ui.store

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projetofinalmobile.R
import com.example.projetofinalmobile.databinding.ListItemStoreBinding
import com.example.projetofinalmobile.model.ItemType
import com.example.projetofinalmobile.model.StoreItem

class StoreItemAdapter(
    private val onBuyClicked: (StoreItem) -> Unit,
    private val onApplySkinClicked: ((StoreItem) -> Unit)?
) : ListAdapter<StoreItem, StoreItemAdapter.StoreItemViewHolder>(StoreItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreItemViewHolder {
        val binding = ListItemStoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoreItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoreItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, onBuyClicked, onApplySkinClicked)
    }

    class StoreItemViewHolder(private val binding: ListItemStoreBinding) : RecyclerView.ViewHolder(binding.root) {
        //popula as views de um item da lista com os dados do StoreItem
        fun bind(
            item: StoreItem,
            onBuyClicked: (StoreItem) -> Unit,
            onApplySkinClicked: ((StoreItem) -> Unit)?
        ) {
            binding.textViewItemName.text = item.name
            binding.textViewItemDescription.text = item.description
            binding.textViewItemPrice.text = "Preço: ${item.price}P"

            //define o icone do item pelo seu tipo
            val iconRes = when (item.type) {
                ItemType.MUSIC -> R.drawable.ic_music_note
                ItemType.PLANT_SKIN -> R.drawable.ic_plant_skin
            }
            binding.imageViewItemIcon.setImageResource(iconRes)

            //skin padrao = nao pode ser comprada, apenas aplicada
            if (item.id == "default_skin") {
                binding.textViewItemPrice.visibility = View.GONE
                binding.buttonBuyItem.visibility = View.VISIBLE
                binding.textViewItemOwned.visibility = View.GONE
                binding.buttonBuyItem.text = "Aplicar"
                binding.buttonBuyItem.isEnabled = true
                binding.buttonBuyItem.setOnClickListener { onApplySkinClicked?.invoke(item) }
            }
            //se o item foi comprado
            else if (item.owned) {
                binding.buttonBuyItem.visibility = View.GONE
                binding.textViewItemOwned.visibility = View.VISIBLE

                //skin de planta mostra o aplicar
                if (item.type == ItemType.PLANT_SKIN && onApplySkinClicked != null) {
                    binding.buttonBuyItem.visibility = View.VISIBLE
                    binding.textViewItemOwned.visibility = View.GONE
                    binding.buttonBuyItem.text = "Aplicar"
                    binding.buttonBuyItem.isEnabled = true
                    binding.buttonBuyItem.setOnClickListener { onApplySkinClicked(item) }
                }
                //se o item nao foi comprado
            } else {
                binding.buttonBuyItem.visibility = View.VISIBLE
                binding.textViewItemOwned.visibility = View.GONE
                binding.textViewItemPrice.visibility = View.VISIBLE
                binding.buttonBuyItem.text = "Comprar"
                binding.buttonBuyItem.isEnabled = item.canAfford
                binding.buttonBuyItem.setOnClickListener { onBuyClicked(item) }
            }
        }
    }

    class StoreItemDiffCallback : DiffUtil.ItemCallback<StoreItem>() {
        override fun areItemsTheSame(oldItem: StoreItem, newItem: StoreItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: StoreItem, newItem: StoreItem): Boolean = oldItem == newItem
    }
}