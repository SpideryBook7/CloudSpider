package com.spiderybook.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.spiderybook.databinding.ItemHomeChildBinding
import com.spiderybook.databinding.ItemHomeParentBinding
import com.spiderybook.domain.model.HomePageList
import com.spiderybook.domain.model.SearchResponse

class ChildItemAdapter(
    private var items: List<SearchResponse>,
    private val onClick: (SearchResponse) -> Unit
) : RecyclerView.Adapter<ChildItemAdapter.ChildViewHolder>() {

    fun updateList(newItems: List<SearchResponse>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ChildViewHolder(private val binding: ItemHomeChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: SearchResponse) {
            binding.tvTitle.text = item.name
            binding.imgPoster.load(item.posterUrl) {
                crossfade(true)
            }
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildViewHolder {
        return ChildViewHolder(
            ItemHomeChildBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChildViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

class ParentItemAdapter(
    private val onClick: (SearchResponse) -> Unit
) : RecyclerView.Adapter<ParentItemAdapter.ParentViewHolder>() {

    private val items = mutableListOf<HomePageList>()

    fun submitList(newItems: List<HomePageList>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ParentViewHolder(private val binding: ItemHomeParentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: HomePageList) {
            binding.tvHeader.text = item.name
            
            // Toggle visibility based on isExpanded
            binding.rvChild.visibility = if (item.isExpanded) android.view.View.VISIBLE else android.view.View.GONE
            
            // Header Click Listener
            binding.root.setOnClickListener {
                item.isExpanded = !item.isExpanded
                notifyItemChanged(bindingAdapterPosition)
            }
            
            val childAdapter = ChildItemAdapter(item.list, onClick)
            binding.rvChild.apply {
                adapter = childAdapter
                layoutManager = if (item.isHorizontal) {
                    LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                } else {
                    androidx.recyclerview.widget.GridLayoutManager(context, 3) // Grid for vertical lists
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        return ParentViewHolder(
            ItemHomeParentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
