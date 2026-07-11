package com.akash.expiryguard.data

import android.content.Context
import com.akash.expiryguard.data.firebase.FirebaseExpiryItemRepository
import com.akash.expiryguard.data.firebase.FirebaseShoppingListRepository
import com.akash.expiryguard.data.local.AppPreferences
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.data.repository.ShoppingListRepository

class AppContainer(context: Context) {
    val itemRepository: ExpiryItemRepository by lazy {
        FirebaseExpiryItemRepository()
    }

    val shoppingListRepository: ShoppingListRepository by lazy {
        FirebaseShoppingListRepository()
    }

    val appPreferences: AppPreferences by lazy {
        AppPreferences(context.applicationContext)
    }
}
