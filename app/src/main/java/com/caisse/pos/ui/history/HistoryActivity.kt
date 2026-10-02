package com.caisse.pos.ui.history

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.caisse.pos.CaisseApp
import com.caisse.pos.R
import com.caisse.pos.databinding.ActivityHistoryBinding
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding

    private val viewModel: HistoryViewModel by viewModels {
        val app = application as CaisseApp
        HistoryViewModel.Factory(app.saleRepository)
    }

    private lateinit var adapter: SaleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        title = getString(R.string.history)

        val app = application as CaisseApp
        binding.historyDeviceLabel.text = getString(R.string.device_id, app.devicePrefix)

        adapter = SaleAdapter()
        binding.historyRecycler.layoutManager = LinearLayoutManager(this)
        binding.historyRecycler.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.rows.collect { rows ->
                    adapter.submit(rows)
                    binding.emptyHistoryLabel.visibility =
                        if (rows.isEmpty()) View.VISIBLE else View.GONE
                    binding.historyRecycler.visibility =
                        if (rows.isEmpty()) View.GONE else View.VISIBLE
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
