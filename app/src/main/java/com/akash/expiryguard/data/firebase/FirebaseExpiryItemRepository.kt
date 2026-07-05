package com.akash.expiryguard.data.firebase

import com.akash.expiryguard.data.model.ExpiryItem
import com.akash.expiryguard.data.repository.ExpiryItemRepository
import com.akash.expiryguard.util.awaitResult
import com.akash.expiryguard.util.awaitVoid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseExpiryItemRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ExpiryItemRepository {

    override suspend fun signInAnonymouslyIfNeeded(): String {
        auth.currentUser?.uid?.let { return it }
        val result = auth.signInAnonymously().awaitResult()
        return result.user?.uid ?: error("Anonymous sign-in did not return a user.")
    }

    override fun observeItems(): Flow<List<ExpiryItem>> = callbackFlow {
        val registration = try {
            itemsCollection(signInAnonymouslyIfNeeded())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val items = snapshot
                        ?.documents
                        ?.map { it.toExpiryItem() }
                        ?.sortedBy { it.expiryDate }
                        .orEmpty()

                    trySend(items)
                }
        } catch (error: Exception) {
            close(error)
            return@callbackFlow
        }

        awaitClose { registration.remove() }
    }

    override fun observeActiveItems(): Flow<List<ExpiryItem>> = callbackFlow {
        val registration = try {
            itemsCollection(signInAnonymouslyIfNeeded())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val items = snapshot
                        ?.documents
                        ?.map { it.toExpiryItem() }
                        ?.filterNot { it.archived }
                        ?.sortedBy { it.expiryDate }
                        .orEmpty()

                    trySend(items)
                }
        } catch (error: Exception) {
            close(error)
            return@callbackFlow
        }

        awaitClose { registration.remove() }
    }

    override suspend fun getItem(itemId: String): ExpiryItem? {
        if (itemId.isBlank()) return null
        val snapshot = itemsCollection(signInAnonymouslyIfNeeded())
            .document(itemId)
            .get()
            .awaitResult()

        return if (snapshot.exists()) snapshot.toExpiryItem() else null
    }

    override suspend fun addItem(item: ExpiryItem): String {
        val userId = signInAnonymouslyIfNeeded()
        val collection = itemsCollection(userId)
        val now = System.currentTimeMillis()
        val itemId = item.id.ifBlank { collection.document().id }
        val itemToSave = item.copy(
            id = itemId,
            createdAt = item.createdAt.takeIf { it > 0L } ?: now,
            updatedAt = now
        )

        collection.document(itemId).set(itemToSave.toFirestoreMap()).awaitVoid()
        return itemId
    }

    override suspend fun updateItem(item: ExpiryItem) {
        require(item.id.isNotBlank()) { "Item id is required for update." }
        val now = System.currentTimeMillis()
        val itemToSave = item.copy(
            createdAt = item.createdAt.takeIf { it > 0L } ?: now,
            updatedAt = now
        )
        itemsCollection(signInAnonymouslyIfNeeded())
            .document(item.id)
            .set(itemToSave.toFirestoreMap())
            .awaitVoid()
    }

    override suspend fun deleteItem(itemId: String) {
        itemsCollection(signInAnonymouslyIfNeeded()).document(itemId).delete().awaitVoid()
    }

    override suspend fun archiveItem(itemId: String) {
        itemsCollection(signInAnonymouslyIfNeeded())
            .document(itemId)
            .update(
                mapOf(
                    "archived" to true,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .awaitVoid()
    }

    override suspend fun markItemConsumed(itemId: String, consumedAt: String) {
        itemsCollection(signInAnonymouslyIfNeeded())
            .document(itemId)
            .update(
                mapOf(
                    "consumed" to true,
                    "consumedAt" to consumedAt,
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .awaitVoid()
    }

    override suspend fun markItemNotConsumed(itemId: String) {
        itemsCollection(signInAnonymouslyIfNeeded())
            .document(itemId)
            .update(
                mapOf(
                    "consumed" to false,
                    "consumedAt" to "",
                    "updatedAt" to System.currentTimeMillis()
                )
            )
            .awaitVoid()
    }

    private fun itemsCollection(userId: String): CollectionReference {
        return firestore.collection("users")
            .document(userId)
            .collection("items")
    }

    private fun DocumentSnapshot.toExpiryItem(): ExpiryItem {
        return ExpiryItem(
            id = getString("id").orEmpty().ifBlank { id },
            name = getString("name").orEmpty(),
            category = getString("category").orEmpty().ifBlank { "Other" },
            expiryDate = getString("expiryDate").orEmpty(),
            purchaseDate = getString("purchaseDate").orEmpty(),
            quantity = getString("quantity").orEmpty(),
            price = getDouble("price")?.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0,
            currency = getString("currency").orEmpty().ifBlank { "INR" },
            notes = getString("notes").orEmpty(),
            reminderDaysBefore = getLong("reminderDaysBefore")?.toInt() ?: 1,
            createdAt = getLong("createdAt") ?: 0L,
            updatedAt = getLong("updatedAt") ?: 0L,
            archived = getBoolean("archived") ?: false,
            consumed = getBoolean("consumed") ?: false,
            consumedAt = getString("consumedAt").orEmpty()
        )
    }

    private fun ExpiryItem.toFirestoreMap(): Map<String, Any> {
        return mapOf<String, Any>(
            "id" to id,
            "name" to name,
            "category" to category.ifBlank { "Other" },
            "expiryDate" to expiryDate,
            "purchaseDate" to purchaseDate,
            "quantity" to quantity,
            "price" to (price.takeIf { it.isFinite() && it >= 0.0 } ?: 0.0),
            "currency" to currency.ifBlank { "INR" },
            "notes" to notes,
            "reminderDaysBefore" to reminderDaysBefore,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt,
            "archived" to archived,
            "consumed" to consumed,
            "consumedAt" to consumedAt
        )
    }
}
