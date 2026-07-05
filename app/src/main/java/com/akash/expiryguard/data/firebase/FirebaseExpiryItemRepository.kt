package com.akash.expiryguard.data.firebase

import com.akash.expiryguard.data.model.ExpiryItem
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.util.awaitResult
import com.akash.expiryguard.util.awaitVoid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseExpiryItemRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ExpiryItemRepository {

    override suspend fun ensureSignedIn(): String {
        auth.currentUser?.uid?.let { return it }
        val result = auth.signInAnonymously().awaitResult()
        return result.user?.uid ?: error("Anonymous sign-in did not return a user.")
    }

    override fun observeActiveItems(): Flow<List<ExpiryItem>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = itemsCollection(userId)
            .whereEqualTo("archived", false)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val items = snapshot
                    ?.documents
                    ?.mapNotNull { document ->
                        document.toObject(ExpiryItem::class.java)?.copy(id = document.id)
                    }
                    ?.sortedBy { it.expiryDate }
                    .orEmpty()

                trySend(items)
            }

        awaitClose { registration.remove() }
    }

    override suspend fun getActiveItems(): List<ExpiryItem> {
        val userId = ensureSignedIn()
        val snapshot = itemsCollection(userId)
            .whereEqualTo("archived", false)
            .get()
            .awaitResult()

        return snapshot.documents
            .mapNotNull { document ->
                document.toObject(ExpiryItem::class.java)?.copy(id = document.id)
            }
            .sortedBy { it.expiryDate }
    }

    override suspend fun saveItem(item: ExpiryItem): String {
        val userId = ensureSignedIn()
        val collection = itemsCollection(userId)
        val now = System.currentTimeMillis()
        val itemId = item.id.ifBlank { collection.document().id }
        val itemToSave = item.copy(
            id = itemId,
            createdAt = item.createdAt.takeIf { it > 0L } ?: now,
            updatedAt = now
        )

        collection.document(itemId).set(itemToSave).awaitVoid()
        return itemId
    }

    override suspend fun archiveItem(itemId: String) {
        val userId = ensureSignedIn()
        itemsCollection(userId)
            .document(itemId)
            .update(
                mapOf(
                    "archived" to true,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .awaitVoid()
    }

    override suspend fun deleteItem(itemId: String) {
        val userId = ensureSignedIn()
        itemsCollection(userId).document(itemId).delete().awaitVoid()
    }

    private fun itemsCollection(userId: String): CollectionReference {
        return firestore.collection("users")
            .document(userId)
            .collection("items")
    }
}
