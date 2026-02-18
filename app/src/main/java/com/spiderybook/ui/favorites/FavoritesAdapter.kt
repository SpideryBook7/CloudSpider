package com.spiderybook.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.spiderybook.data.local.entity.FavoriteEntity
import com.spiderybook.databinding.ItemHomeChildBinding

class FavoritesAdapter(
    private var items: List<FavoriteEntity>,
    private val onClick: (FavoriteEntity) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    fun submitList(newItems: List<FavoriteEntity>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemHomeChildBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: FavoriteEntity) {
            binding.tvTitle.text = item.name // or "valname" if property name was typo in entity
            binding.imgPoster.load(item.posterUrl) {
                crossfade(true)
            }
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHomeChildBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
