package com.akash.expiryguard.data.model

enum class ExpiryCategory(val displayName: String) {
    FOOD("Food"),
    MEDICINE("Medicine"),
    DOCUMENT("Document"),
    WARRANTY("Warranty"),
    COSMETIC("Cosmetic"),
    SUBSCRIPTION("Subscription"),
    EXPIRED("Expired"),
    OTHER("Other")
}
