package com.akash.expiryguard.data

import com.akash.expiryguard.data.firebase.FirebaseExpiryItemRepository
import com.akash.expiryguard.data.repository.ExpiryItemRepository

class AppContainer {
    val itemRepository: ExpiryItemRepository by lazy {
        FirebaseExpiryItemRepository()
    }
}
