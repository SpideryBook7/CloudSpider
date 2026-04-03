package com.spiderybook.ui.settings

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.widget.EditText
import android.widget.LinearLayout
import com.spiderybook.databinding.FragmentSettingsBinding
import com.spiderybook.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : BaseFragment<FragmentSettingsBinding>(FragmentSettingsBinding::inflate) {

    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupThemeSelection()
        setupProviderSelection()
        setupCacheClearing()
        setupDownloadsClearing()
        setupBackupSection()
    }

    private val exportBackupLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            requireContext().contentResolver.openOutputStream(it)?.let { outputStream ->
                viewModel.exportDatabase(outputStream)
            }
        }
    }

    private val importBackupLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            requireContext().contentResolver.openInputStream(it)?.let { inputStream ->
                viewModel.importDatabase(inputStream)
            }
        }
    }

    private fun setupBackupSection() {
        // Observe Toast Messages
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            if (message.isNotEmpty()) {
                com.google.android.material.snackbar.Snackbar.make(binding.root, message, com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                viewModel.clearToastMessage()
            }
        }

        binding.btnBackupExport.setOnClickListener {
            val dateString = java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", java.util.Locale.getDefault()).format(java.util.Date())
            exportBackupLauncher.launch("SpideryBook_Backup_$dateString.json")
        }

        binding.btnBackupImport.setOnClickListener {
            importBackupLauncher.launch(arrayOf("application/json", "*/*"))
        }
    }

    private fun setupThemeSelection() {
        viewModel.currentTheme.observe(viewLifecycleOwner) { mode ->
            // Prevent recursive loop if user just clicked it
            binding.rgTheme.setOnCheckedChangeListener(null)
            when (mode) {
                AppCompatDelegate.MODE_NIGHT_NO -> binding.rbLight.isChecked = true
                AppCompatDelegate.MODE_NIGHT_YES -> binding.rbDark.isChecked = true
                else -> binding.rbSystem.isChecked = true
            }
            AppCompatDelegate.setDefaultNightMode(mode)
            
            // Re-attach listener
            binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
                val newMode = when (checkedId) {
                    binding.rbLight.id -> AppCompatDelegate.MODE_NIGHT_NO
                    binding.rbDark.id -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                viewModel.saveTheme(newMode)
                AppCompatDelegate.setDefaultNightMode(newMode)
            }
        }
    }

    private fun setupProviderSelection() {
        viewModel.availableProviders.observe(viewLifecycleOwner) { providers ->
            val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, providers)
            binding.spinnerProvider.adapter = adapter
        }

        viewModel.selectedProvider.observe(viewLifecycleOwner) { selected ->
            @Suppress("UNCHECKED_CAST")
            val adapter = binding.spinnerProvider.adapter as? android.widget.ArrayAdapter<String>
            if (adapter != null && selected != null) {
                val position = adapter.getPosition(selected)
                if (position >= 0 && binding.spinnerProvider.selectedItemPosition != position) {
                    binding.spinnerProvider.setSelection(position, false)
                }
            }
        }

        binding.spinnerProvider.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val providerName = parent?.getItemAtPosition(position) as? String
                providerName?.let { viewModel.saveProvider(it) }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }

    @OptIn(coil.annotation.ExperimentalCoilApi::class)
    private fun setupCacheClearing() {
        binding.cardClearCache.setOnClickListener {
            val imageLoader = coil.Coil.imageLoader(requireContext())
            imageLoader.diskCache?.clear()
            imageLoader.memoryCache?.clear()
            com.google.android.material.snackbar.Snackbar.make(binding.root, "Caché de imágenes limpiada correctamente", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun setupDownloadsClearing() {
        binding.cardClearDownloads.setOnClickListener {
            com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Eliminar Descargas")
                .setMessage("¿Estás seguro de que deseas eliminar todas las películas y capítulos descargados? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar") { _, _ ->
                    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                    val spideryDir = java.io.File(downloadsDir, "SpideryBook")
                    
                    if (spideryDir.exists() && spideryDir.isDirectory) {
                        try {
                            spideryDir.deleteRecursively()
                            com.google.android.material.snackbar.Snackbar.make(binding.root, "Descargas eliminadas correctamente", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            com.google.android.material.snackbar.Snackbar.make(binding.root, "Error al eliminar las descargas", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        com.google.android.material.snackbar.Snackbar.make(binding.root, "No hay descargas guardadas", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }
}
