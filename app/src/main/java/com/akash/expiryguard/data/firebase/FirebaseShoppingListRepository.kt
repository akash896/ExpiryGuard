package com.akash.expiryguard.data.firebase

import com.akash.expiryguard.data.model.ExpiryCategory
import com.akash.expiryguard.data.model.ShoppingItem
import com.akash.expiryguard.data.repository.ShoppingListRepository
import com.akash.expiryguard.util.awaitResult
import com.akash.expiryguard.util.awaitVoid
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseShoppingListRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ShoppingListRepository {

    override suspend fun signInAnonymouslyIfNeeded(): String {
        return auth.currentUser
            ?.takeUnless { it.isAnonymous }
            ?.uid
            ?: error("A signed-in account is required.")
    }

    override fun observeShoppingItems(): Flow<List<ShoppingItem>> = callbackFlow {
        val registration = try {
            shoppingCollection(signInAnonymouslyIfNeeded())
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    val items = snapshot?.documents
                        ?.map { it.toShoppingItem() }
                        ?.sortedWith(compareBy<ShoppingItem> { it.checked }.thenByDescending { it.createdAt })
                        .orEmpty()
                    trySend(items)
                }
        } catch (error: Exception) {
            close(error)
            return@callbackFlow
        }

        awaitClose { registration.remove() }
    }

    override suspend fun addShoppingItem(item: ShoppingItem): String {
        val collection = shoppingCollection(signInAnonymouslyIfNeeded())
        val now = System.currentTimeMillis()
        val itemId = item.id.ifBlank { collection.document().id }
        val itemToSave = item.copy(
            id = itemId,
            estimatedPrice = item.estimatedPrice.safePrice(),
            currency = item.currency.ifBlank { "INR" },
            createdAt = item.createdAt.takeIf { it > 0L } ?: now,
            updatedAt = now
        )
        collection.document(itemId).set(itemToSave.toFirestoreMap()).awaitVoid()
        return itemId
    }

    override suspend fun updateShoppingItem(item: ShoppingItem) {
        require(item.id.isNotBlank()) { "Shopping item id is required for update." }
        val now = System.currentTimeMillis()
        val itemToSave = item.copy(
            estimatedPrice = item.estimatedPrice.safePrice(),
            currency = item.currency.ifBlank { "INR" },
            createdAt = item.createdAt.takeIf { it > 0L } ?: now,
            updatedAt = now
        )
        shoppingCollection(signInAnonymouslyIfNeeded())
            .document(item.id)
            .set(itemToSave.toFirestoreMap())
            .awaitVoid()
    }

    override suspend fun deleteShoppingItem(itemId: String) {
        if (itemId.isBlank()) return
        shoppingCollection(signInAnonymouslyIfNeeded()).document(itemId).delete().awaitVoid()
    }

    override suspend fun setShoppingItemChecked(itemId: String, checked: Boolean) {
        if (itemId.isBlank()) return
        shoppingCollection(signInAnonymouslyIfNeeded())
            .document(itemId)
            .update(mapOf("checked" to checked, "updatedAt" to System.currentTimeMillis()))
            .awaitVoid()
    }

    private fun shoppingCollection(userId: String): CollectionReference {
        return firestore.collection("users")
            .document(userId)
            .collection("shoppingList")
    }

    private fun DocumentSnapshot.toShoppingItem(): ShoppingItem {
        return ShoppingItem(
            id = getString("id").orEmpty().ifBlank { id },
            name = getString("name").orEmpty(),
            category = getString("category").orEmpty().ifBlank { ExpiryCategory.OTHER.displayName },
            quantity = getString("quantity").orEmpty(),
            estimatedPrice = getDouble("estimatedPrice")?.safePrice() ?: 0.0,
            currency = getString("currency").orEmpty().ifBlank { "INR" },
            sourceItemId = getString("sourceItemId").orEmpty(),
            checked = getBoolean("checked") ?: false,
            createdAt = getLong("createdAt") ?: 0L,
            updatedAt = getLong("updatedAt") ?: 0L
        )
    }

    private fun ShoppingItem.toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "name" to name,
            "category" to category.ifBlank { ExpiryCategory.OTHER.displayName },
            "quantity" to quantity,
            "estimatedPrice" to estimatedPrice.safePrice(),
            "currency" to currency.ifBlank { "INR" },
            "sourceItemId" to sourceItemId,
            "checked" to checked,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }

    private fun Double.safePrice(): Double {
        return takeIf { it.isFinite() && it >= 0.0 } ?: 0.0
    }
}
