package com.akash.expiryguard.data

enum class ItemCategory(val displayName: String) {
    FOOD("Food"),
    MEDICINE("Medicine"),
    DOCUMENT("Document"),
    WARRANTY("Warranty"),
    COSMETIC("Cosmetic"),
    SUBSCRIPTION("Subscription"),
    OTHER("Other");

    companion object {
        fun fromDisplayName(value: String): ItemCategory {
            return entries.firstOrNull { it.displayName.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
