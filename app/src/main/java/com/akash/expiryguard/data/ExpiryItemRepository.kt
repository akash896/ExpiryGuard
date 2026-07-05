package com.akash.expiryguard.data

import kotlinx.coroutines.flow.Flow

interface ExpiryItemRepository {
    suspend fun ensureSignedIn(): String
    fun observeActiveItems(): Flow<List<ExpiryItem>>
    suspend fun getActiveItems(): List<ExpiryItem>
    suspend fun saveItem(item: ExpiryItem): String
    suspend fun archiveItem(itemId: String)
    suspend fun deleteItem(itemId: String)
}
