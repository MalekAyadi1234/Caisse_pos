package com.caisse.pos.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.caisse.pos.CaisseApp
import com.caisse.pos.R
import com.caisse.pos.databinding.ActivityMainBinding
import com.caisse.pos.ui.history.HistoryActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels {
        val app = application as CaisseApp
        MainViewModel.Factory(app.saleRepository, app.devicePrefix)
    }

    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartAdapter: CartAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        productAdapter = ProductAdapter { product -> viewModel.addProduct(product) }
        cartAdapter = CartAdapter(
            onIncrease = { viewModel.increase(it) },
            onDecrease = { viewModel.decrease(it) }
        )

        binding.productsRecycler.layoutManager = GridLayoutManager(this, 2)
        binding.productsRecycler.adapter = productAdapter

        binding.cartRecycler.layoutManager = LinearLayoutManager(this)
        binding.cartRecycler.adapter = cartAdapter

        binding.checkoutButton.setOnClickListener { viewModel.checkout() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.state.collect { state ->
                        productAdapter.submit(state.products)
                        cartAdapter.submit(state.cart.lines)
                        binding.totalLabel.text = getString(R.string.total, state.totalLabel)
                        binding.checkoutButton.isEnabled = state.checkoutEnabled
                        binding.emptyCartLabel.visibility =
                            if (state.cart.isEmpty) View.VISIBLE else View.GONE
                        binding.cartRecycler.visibility =
                            if (state.cart.isEmpty) View.GONE else View.VISIBLE
                        binding.deviceLabel.text =
                            getString(R.string.device_id, state.devicePrefix)
                        binding.networkBanner.visibility =
                            if (state.online) View.GONE else View.VISIBLE
                        binding.networkBanner.text = getString(R.string.offline)
                    }
                }
                launch {
                    viewModel.events.collect { event ->
                        when (event) {
                            is MainEvent.CheckoutSuccess -> {
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(
                                        R.string.ticket_created,
                                        event.ticketNumber,
                                        event.amountLabel
                                    ),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            is MainEvent.CheckoutError -> {
                                Toast.makeText(
                                    this@MainActivity,
                                    event.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
