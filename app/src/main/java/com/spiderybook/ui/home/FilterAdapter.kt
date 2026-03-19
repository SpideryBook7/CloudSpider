package com.spiderybook.ui.home

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.spiderybook.databinding.ItemFilterBinding

class FilterAdapter(
    private val onClick: (String, android.view.View) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private val items = mutableListOf<String>()
    private var selectedPosition = 0
    private var isDropdownOpen = false

    fun setDropdownOpen(isOpen: Boolean) {
        if (isDropdownOpen != isOpen) {
            isDropdownOpen = isOpen
            notifyItemChanged(items.indexOf("All"))
        }
    }

    fun submitList(newItems: List<String>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    fun setSelection(item: String) {
        val index = items.indexOf(item)
        if (selectedPosition != index) {
            val previous = selectedPosition
            selectedPosition = index
            if (previous != -1) notifyItemChanged(previous)
            if (selectedPosition != -1) notifyItemChanged(selectedPosition)
        }
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
                binding.tvFilter.setBackgroundResource(com.spiderybook.R.drawable.bg_pill_accent)
                binding.root.alpha = 1.0f
                binding.tvFilter.textSize = 14f
            } else {
                binding.tvFilter.setTextColor(Color.GRAY)
                binding.tvFilter.setBackgroundResource(com.spiderybook.R.drawable.bg_pill_dark)
                binding.root.alpha = 0.7f
                binding.tvFilter.textSize = 14f
            }
            
            if (item == "All") {
                val chevronRes = if (isDropdownOpen) com.spiderybook.R.drawable.ic_chevron_up_thin else com.spiderybook.R.drawable.ic_chevron_down_thin
                binding.tvFilter.setCompoundDrawablesWithIntrinsicBounds(0, 0, chevronRes, 0)
                binding.tvFilter.compoundDrawablePadding = 12
                binding.tvFilter.compoundDrawables[2]?.setTint(if (isSelected) Color.WHITE else Color.GRAY)
            } else {
                binding.tvFilter.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
            
            binding.root.setOnClickListener {
                onClick(item, binding.root)
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
