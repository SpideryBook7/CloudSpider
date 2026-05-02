package com.spiderybook.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.spiderybook.R
import coil.load
import com.spiderybook.databinding.ItemHomeChildBinding
import com.spiderybook.databinding.ItemHomeContinueBinding
import com.spiderybook.databinding.ItemHomeParentBinding
import com.spiderybook.databinding.ItemHomeTrendingBinding
import com.spiderybook.domain.model.HomePageList
import com.spiderybook.domain.model.SearchResponse

class TrendingAdapter(
    private var items: List<SearchResponse>,
    private val onClick: (SearchResponse) -> Unit
) : RecyclerView.Adapter<TrendingAdapter.TrendingViewHolder>() {

    inner class TrendingViewHolder(private val binding: ItemHomeTrendingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: SearchResponse, position: Int) {
            binding.tvRank.text = String.format("%02d", position + 1)
            binding.tvTitle.text = item.name
            
            val hash = Math.abs(item.name.hashCode())
            val rating = 7.0 + (hash % 26) / 10.0
            val views = 1.0 + (hash % 40) / 10.0
            
            val categories = when (hash % 5) {
                0 -> "Action • Fantasy • Shounen"
                1 -> "Romance • School • Comedy"
                2 -> "Isekai • Magic • Adventure"
                3 -> "Sci-Fi • Thriller • Psychological"
                else -> "Drama • Supernatural • Mystery"
            }
            
            binding.tvSubtitle.text = item.subtitle ?: categories
            binding.tvMetadata.text = "⭐ ${String.format("%.1f", rating)}   ${String.format("%.1f", views)}M Views"
            
            binding.imgPoster.load(item.posterUrl) {
                if (com.spiderybook.BuildConfig.FLAVOR != "legacy") {
                    crossfade(true)
                }
            }
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendingViewHolder {
        return TrendingViewHolder(
            ItemHomeTrendingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TrendingViewHolder, position: Int) {
        holder.bind(items[position], position)
    }

    override fun getItemCount(): Int = items.size
}

class ContinueWatchingAdapter(
    private var items: List<SearchResponse>,
    private val onClick: (SearchResponse) -> Unit
) : RecyclerView.Adapter<ContinueWatchingAdapter.ContinueViewHolder>() {

    inner class ContinueViewHolder(private val binding: ItemHomeContinueBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(item: SearchResponse) {
            binding.tvTitle.text = item.name
            binding.tvSubtitle.text = item.subtitle ?: "Episodio"
            binding.progressBar.progress = ((item.progress ?: 0f) * 100).toInt()
            
            binding.imgPoster.load(item.posterUrl) {
                if (com.spiderybook.BuildConfig.FLAVOR != "legacy") {
                    crossfade(true)
                }
            }
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContinueViewHolder {
        return ContinueViewHolder(
            ItemHomeContinueBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ContinueViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}

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
                if (com.spiderybook.BuildConfig.FLAVOR != "legacy") {
                    crossfade(true)
                    allowHardware(true)
                }
            }
            
            // Handle new metadata text below title
            val yearStr = item.year?.toString() ?: "2024"
            val typeStr = item.type?.name ?: "Anime"
            binding.tvMetadata.text = "$yearStr • $typeStr"
            
            // Handle Badge Pill (Top Right)
            if (!item.quality.isNullOrEmpty()) {
                binding.tvBadge.text = item.quality
                binding.tvBadge.visibility = View.VISIBLE
                binding.tvBadge.setBackgroundResource(R.drawable.bg_badge_hd)
            } else {
                // Placeholder logic to show NEW or TV badges to match mockup style randomly if empty
                val isNew = Math.random() > 0.7
                if (isNew) {
                    binding.tvBadge.text = "NEW"
                    binding.tvBadge.setBackgroundResource(R.drawable.bg_badge_new)
                    binding.tvBadge.visibility = View.VISIBLE
                } else {
                    binding.tvBadge.visibility = View.GONE
                }
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
            val isTrendingRow = item.name == "Últimos Episodios" || item.name == "Últimos Animes" || item.name == "Trending Now"
            
            // Toggle visibility based on isExpanded, EXCEPT for Trending which just shrinks
            if (isTrendingRow) {
                binding.rvChild.visibility = android.view.View.VISIBLE
            } else {
                binding.rvChild.visibility = if (item.isExpanded) android.view.View.VISIBLE else android.view.View.GONE
            }
            
            // Update "See All" text to indicate state
            binding.tvSeeAll.text = if (item.isExpanded) "Hide" else "See All"
            
            // Header Click Listener
            binding.root.setOnClickListener {
                item.isExpanded = !item.isExpanded
                notifyItemChanged(bindingAdapterPosition)
            }
            
            if (item.name == "Continuar Viendo") {
                val continueAdapter = ContinueWatchingAdapter(item.list, onClick)
                binding.rvChild.apply {
                    adapter = continueAdapter
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                }
            } else if (isTrendingRow) {
                val displayList = if (item.isExpanded) item.list else item.list.take(3)
                val trendingAdapter = TrendingAdapter(displayList, onClick)
                binding.rvChild.apply {
                    adapter = trendingAdapter
                    layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                }
            } else {
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
