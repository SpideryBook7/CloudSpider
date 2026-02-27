package com.spiderybook.utils.dlna

data class DLNADevice(
    val name: String,
    val location: String, // The full URL to the device's description XML
    val avTransportControlUrl: String? = null // Extracted from XML for sending Play commands
)
