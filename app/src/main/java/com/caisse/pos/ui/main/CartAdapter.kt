package com.caisse.pos.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.caisse.pos.R
import com.caisse.pos.data.model.CartLine
import com.caisse.pos.databinding.ItemCartLineBinding
import com.caisse.pos.util.Money

class CartAdapter(
    private val onIncrease: (String) -> Unit,
    private val onDecrease: (String) -> Unit
) : RecyclerView.Adapter<CartAdapter.VH>() {

    private var items: List<CartLine> = emptyList()

    fun submit(list: List<CartLine>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemCartLineBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class VH(private val binding: ItemCartLineBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(line: CartLine) {
            binding.lineName.text = line.product.name
            binding.lineQty.text = binding.root.context.getString(R.string.qty, line.quantity)
            binding.lineTotal.text = Money.formatCents(line.lineTotalCents)
            binding.increaseButton.setOnClickListener { onIncrease(line.product.id) }
            binding.decreaseButton.setOnClickListener { onDecrease(line.product.id) }
        }
    }
}
