package com.example.projetofinalmobile.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.projetofinalmobile.R
import com.example.projetofinalmobile.databinding.ListItemSprintBinding
import com.example.projetofinalmobile.model.StudySprint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SprintHistoryAdapter : ListAdapter<StudySprint, SprintHistoryAdapter.SprintViewHolder>(
    SprintDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SprintViewHolder {
        val binding = ListItemSprintBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SprintViewHolder(binding)
    }

    //exibe os dados na posicao especificada
    override fun onBindViewHolder(holder: SprintViewHolder, position: Int) {
        val sprint = getItem(position)
        holder.bind(sprint)
    }

    inner class SprintViewHolder(private val binding: ListItemSprintBinding) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        //associa os dados de uma sprint as views
        fun bind(sprint: StudySprint) {
            binding.textViewSprintTimestamp.text = dateFormat.format(Date(sprint.timestamp))
            binding.textViewSprintDuration.text = itemView.context.getString(R.string.sprint_duration_format, sprint.durationMinutes)
        }
    }

    class SprintDiffCallback : DiffUtil.ItemCallback<StudySprint>() {
        //verifica se os itens sao os mesmos
        override fun areItemsTheSame(oldItem: StudySprint, newItem: StudySprint): Boolean {
            return oldItem.id == newItem.id
        }

        //verifica se o conteudo dos itens é o mesmo
        override fun areContentsTheSame(oldItem: StudySprint, newItem: StudySprint): Boolean {
            return oldItem == newItem
        }
    }
}