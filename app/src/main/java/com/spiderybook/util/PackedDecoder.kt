package com.spiderybook.util

import java.util.regex.Pattern

object PackedDecoder {
    fun decode(packedHtml: String): String? {
        // Primary: anchor to "return p}(" which appears in most modern packers
        // Greedy .* is needed because the obfuscated p string can contain commas and quotes
        var match = Regex("return\\s+p\\}\\s*\\('(.*)',\\s*(\\d+),\\s*(\\d+),\\s*'(.*?)'\\.split\\('\\|'\\)", RegexOption.DOT_MATCHES_ALL).find(packedHtml)

        // Fallback: older-style packer pattern
        if (match == null) {
            match = Regex("eval\\s*\\(\\s*function\\s*\\(p,a,c,k,e,d\\)[^)]*\\)\\s*\\('(.*?)',\\s*(\\d+),\\s*(\\d+),\\s*'(.*?)'\\.split\\('\\|'\\)", RegexOption.DOT_MATCHES_ALL).find(packedHtml)
        }

        if (match == null) return null

        val p = match.groupValues[1]
        val a = match.groupValues[2].toInt()
        val c = match.groupValues[3].toInt()
        val k = match.groupValues[4].split("|")

        return unpack(p, a, c, k)
    }

    fun unpack(p: String, a: Int, c: Int, k: List<String>): String {
        var unpacked = p
        var count = c
        while (count-- > 0) {
            val value = if (count < k.size && k[count].isNotEmpty()) k[count] else count.toString(a)
            val key = count.toString(a)
            if (count < k.size && k[count].isNotEmpty()) {
                unpacked = unpacked.replace(Regex("\\b$key\\b"), value)
            }
        }
        return unpacked
    }
}
