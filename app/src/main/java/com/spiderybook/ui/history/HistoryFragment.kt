package com.spiderybook.ui.history

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.spiderybook.databinding.FragmentHistoryBinding
import com.spiderybook.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HistoryFragment : BaseFragment<FragmentHistoryBinding>(FragmentHistoryBinding::inflate) {

    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var adapter: HistoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            items = emptyList(),
            onClick = { item ->
                val bundle = Bundle().apply {
                    putString("url", item.url)
                    putString("apiName", item.apiName)
                }
                findNavController().navigate(com.spiderybook.R.id.nav_result, bundle)
            },
            onSelectionModeChanged = { isSelecting ->
                binding.fabDeleteSelection.isVisible = isSelecting
                if (isSelecting) {
                    binding.fabDeleteSelection.text = "Eliminar Selección"
                }
            }
        )
        binding.rvHistory.adapter = adapter
    }

    private fun setupListeners() {
        binding.fabDeleteSelection.setOnClickListener {
            val urlsToDelete = adapter.selectedUrls.toList()
            if (urlsToDelete.isNotEmpty()) {
                viewModel.deleteHistoryItems(urlsToDelete)
                android.widget.Toast.makeText(requireContext(), "${urlsToDelete.size} eliminados", android.widget.Toast.LENGTH_SHORT).show()
            }
            adapter.clearSelection()
            binding.fabDeleteSelection.isVisible = false
        }
    }

    private fun setupObservers() {
        viewModel.history.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvEmpty.isVisible = list.isEmpty()
        }
    }
}
