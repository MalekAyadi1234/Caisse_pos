package com.caisse.pos.ui.history

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.caisse.pos.data.model.PrintStatus
import com.caisse.pos.databinding.ItemSaleBinding

class SaleAdapter : RecyclerView.Adapter<SaleAdapter.VH>() {

    private var items: List<SaleRow> = emptyList()

    fun submit(list: List<SaleRow>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemSaleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class VH(private val binding: ItemSaleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(row: SaleRow) {
            binding.saleTicket.text = row.ticketNumber
            binding.saleAmount.text = row.amountLabel
            binding.saleStatus.text = row.statusLabel
            binding.saleSync.text = row.syncLabel
            binding.saleDate.text = row.dateLabel

            binding.saleStatus.setTextColor(
                when (row.status) {
                    PrintStatus.PENDING -> Color.parseColor("#CC6600")
                    PrintStatus.PRINTED -> Color.parseColor("#006600")
                    PrintStatus.FAILED -> Color.parseColor("#CC0000")
                }
            )
        }
    }
}
