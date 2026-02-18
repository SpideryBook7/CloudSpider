package com.spiderybook.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spiderybook.databinding.ItemFilterBinding

class FilterAdapter(
    private val onClick: (String) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private val items = mutableListOf<String>()
    private var selectedPosition = 0

    fun submitList(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun selectItem(position: Int) {
        val previous = selectedPosition
        selectedPosition = position
        notifyItemChanged(previous)
        notifyItemChanged(selectedPosition)
    }

    inner class FilterViewHolder(private val binding: ItemFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: String, position: Int) {
            binding.tvFilter.text = item
            
            val isSelected = position == selectedPosition
            if (isSelected) {
                binding.tvFilter.setTextColor(Color.WHITE)
                binding.tvFilter.setTypeface(null, android.graphics.Typeface.BOLD)
                // Use a visible background for selected state if needed, or just text color
                // For now, assuming dark theme, White vs Gray
                binding.root.alpha = 1.0f
                binding.tvFilter.textSize = 18f
            } else {
                binding.tvFilter.setTextColor(Color.GRAY)
                binding.tvFilter.setTypeface(null, android.graphics.Typeface.NORMAL)
                binding.root.alpha = 0.7f
                binding.tvFilter.textSize = 14f
            }
            
            binding.root.setOnClickListener {
                onClick(item)
                selectItem(bindingAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        return FilterViewHolder(
            ItemFilterBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}
