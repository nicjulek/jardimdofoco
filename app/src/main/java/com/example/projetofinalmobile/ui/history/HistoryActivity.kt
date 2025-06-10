package com.example.projetofinalmobile.ui.history

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetofinalmobile.databinding.ActivityHistoryBinding
import com.example.projetofinalmobile.viewmodel.HistoryViewModel

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    //instancia viewmodel
    private val historyViewModel: HistoryViewModel by viewModels()
    private lateinit var sprintHistoryAdapter: SprintHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //botao voltar
        setSupportActionBar(binding.toolbarHistory)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        //funcoes de config
        setupRecyclerView()
        observeViewModel()
    }

    //configura o recyclerview
    private fun setupRecyclerView() {
        sprintHistoryAdapter = SprintHistoryAdapter()
        binding.recyclerViewHistory.apply {
            adapter = sprintHistoryAdapter
            layoutManager = LinearLayoutManager(this@HistoryActivity)
        }
    }

    //observa mudanças no livedata do viewmodel
    private fun observeViewModel() {
        historyViewModel.allSprints.observe(this, Observer { sprints ->
            //se a lista de sprints for nula ou vazia, mostra uma mensagem
            if (sprints.isNullOrEmpty()) {
                binding.recyclerViewHistory.visibility = View.GONE
                binding.textViewNoHistory.visibility = View.VISIBLE
            } else {
                //mostra o RecyclerView e esconde a mensagem
                binding.recyclerViewHistory.visibility = View.VISIBLE
                binding.textViewNoHistory.visibility = View.GONE
                //lista atualizada para o adapter
                sprintHistoryAdapter.submitList(sprints)
            }
        })
    }

    //clique no voltar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed() //tchau activity
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}