package com.akash.expiryguard.data.repository

import com.akash.expiryguard.data.model.ExpiryItem
import kotlinx.coroutines.flow.Flow

interface ExpiryItemRepository {
    suspend fun signInAnonymouslyIfNeeded(): String
    fun observeItems(): Flow<List<ExpiryItem>>
    fun observeActiveItems(): Flow<List<ExpiryItem>>
    suspend fun getItem(itemId: String): ExpiryItem?
    suspend fun addItem(item: ExpiryItem): String
    suspend fun updateItem(item: ExpiryItem)
    suspend fun deleteItem(itemId: String)
    suspend fun archiveItem(itemId: String)
    suspend fun markItemConsumed(itemId: String, consumedAt: String)
    suspend fun markItemNotConsumed(itemId: String)
}
