package com.example.projetofinalmobile.ui.store

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.projetofinalmobile.databinding.ActivityStoreBinding
import com.example.projetofinalmobile.viewmodel.StoreViewModel

class StoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoreBinding
    private val storeViewModel: StoreViewModel by viewModels()
    private lateinit var storeItemAdapter: StoreItemAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //botao voltar
        setSupportActionBar(binding.toolbarStore)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setupRecyclerView()
        observeViewModel()
        setupClickListeners()
    }

    //botao de teste pra adicionar 50 pontos
    private fun setupClickListeners() {
        binding.buttonAddTestPoints.setOnClickListener {
            storeViewModel.addTestPoints()
            Toast.makeText(this, "+50 pontos adicionados!", Toast.LENGTH_SHORT).show()
        }
    }

    //configuracao do recycler
    private fun setupRecyclerView() {
        storeItemAdapter = StoreItemAdapter(
            onBuyClicked = { item ->
                storeViewModel.buyItem(item)
            },
            onApplySkinClicked = { item ->
                storeViewModel.applyPlantSkin(item)
            }
        )
        binding.recyclerViewStoreItems.apply {
            adapter = storeItemAdapter
            layoutManager = LinearLayoutManager(this@StoreActivity)
        }
    }

    //observa o livedata pra mudar a UI
    private fun observeViewModel() {
        //pontos do usuario
        storeViewModel.userPoints.observe(this, Observer { points ->
            binding.textViewStoreUserPoints.text = "Pontos: $points"
        })

        //lista de itens e manda pro adapter
        storeViewModel.displayStoreItems.observe(this, Observer { items ->
            if (items.isNullOrEmpty()) {
                binding.recyclerViewStoreItems.visibility = View.GONE
                binding.textViewNoStoreItems.visibility = View.VISIBLE
            } else {
                binding.recyclerViewStoreItems.visibility = View.VISIBLE
                binding.textViewNoStoreItems.visibility = View.GONE
                storeItemAdapter.submitList(items)
            }
        })

        //status da compra (sucesso ou falha)
        storeViewModel.purchaseStatus.observe(this, Observer { status ->
            status?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
                storeViewModel.clearPurchaseStatus()
            }
        })
    }

    //clicou no voltar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
    }
}