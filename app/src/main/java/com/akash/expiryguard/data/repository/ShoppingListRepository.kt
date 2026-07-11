package com.akash.expiryguard.data.repository

import com.akash.expiryguard.data.model.ShoppingItem
import kotlinx.coroutines.flow.Flow

interface ShoppingListRepository {
    suspend fun signInAnonymouslyIfNeeded(): String
    fun observeShoppingItems(): Flow<List<ShoppingItem>>
    suspend fun addShoppingItem(item: ShoppingItem): String
    suspend fun updateShoppingItem(item: ShoppingItem)
    suspend fun deleteShoppingItem(itemId: String)
    suspend fun setShoppingItemChecked(itemId: String, checked: Boolean)
}
