package com.spiderybook.util

import java.util.regex.Pattern

object PackedDecoder {
    fun decode(packedHtml: String): String? {
        // Relaxed regex to handle whitespace variations: function ( p,a,c...
        val regex = "eval\\s*\\(\\s*function\\s*\\(p,a,c,k,e,d\\).*?\\{.*?\\}\\s*\\('(.*?)'\\s*,\\s*(\\d+)\\s*,\\s*(\\d+)\\s*,\\s*'(.*?)'\\.split\\('\\|'\\)".toRegex(RegexOption.DOT_MATCHES_ALL)
        val match = regex.find(packedHtml) ?: return null

        val p = match.groupValues[1]
        val a = match.groupValues[2].toInt()
        val c = match.groupValues[3].toInt()
        val k = match.groupValues[4].split("|")

        return unpack(p, a, c, k)
    }

    private fun unpack(p: String, a: Int, c: Int, k: List<String>): String {
        var unpacked = p
        var count = c
        
        // Build a map of replacements to avoid repeated string manipulation if possible, 
        // but for simplicity and correctness with the algorithm, user order matters.
        // Actually, the standard algorithm iterates from count down to 0.
        
        while (count-- > 0) {
            val value = if (count < k.size && k[count].isNotEmpty()) k[count] else count.toString(a)
            val key = count.toString(a)
            
            // Only replace if valid value (and not just the index itself if consistent with some packers)
            // But standard Edwards: if k[c] exists, replace base(c) with k[c].
            if (count < k.size && k[count].isNotEmpty()) {
                 unpacked = unpacked.replace(Regex("\\b$key\\b"), value)
            }
        }
        return unpacked
    }
}
