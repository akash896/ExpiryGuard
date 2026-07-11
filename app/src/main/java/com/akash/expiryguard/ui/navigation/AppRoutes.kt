package com.akash.expiryguard.ui.navigation

import android.net.Uri

object AppRoutes {
    const val HOME = "home"
    const val ADD_ITEM = "add_item"
    const val ITEM_ID = "itemId"
    const val EDIT_ITEM = "edit_item/{$ITEM_ID}"
    const val DETAIL = "detail/{$ITEM_ID}"
    const val SETTINGS = "settings"
    const val EXPENSE_INSIGHTS = "expense_insights"

    fun editItem(itemId: String): String = "edit_item/${Uri.encode(itemId)}"

    fun detail(itemId: String): String = "detail/${Uri.encode(itemId)}"
}
