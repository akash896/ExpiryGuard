package com.akash.expiryguard.data

import com.akash.expiryguard.data.firebase.FirebaseExpiryItemRepository

class AppContainer {
    val itemRepository: ExpiryItemRepository by lazy {
        FirebaseExpiryItemRepository()
    }
}
