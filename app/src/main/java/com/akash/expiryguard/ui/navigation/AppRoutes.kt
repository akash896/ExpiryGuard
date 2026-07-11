package com.akash.expiryguard.ui.navigation

import android.net.Uri

object AppRoutes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val ADD_ITEM = "add_item"
    const val TEMPLATE_ID = "templateId"
    const val ADD_ITEM_WITH_TEMPLATE = "$ADD_ITEM?$TEMPLATE_ID={$TEMPLATE_ID}"
    const val ITEM_ID = "itemId"
    const val EDIT_ITEM = "edit_item/{$ITEM_ID}"
    const val DETAIL = "detail/{$ITEM_ID}"
    const val SETTINGS = "settings"
    const val EXPENSE_INSIGHTS = "expense_insights"

    fun editItem(itemId: String): String = "edit_item/${Uri.encode(itemId)}"

    fun detail(itemId: String): String = "detail/${Uri.encode(itemId)}"

    fun addItem(templateId: String? = null): String {
        return templateId?.let { "$ADD_ITEM?$TEMPLATE_ID=${Uri.encode(it)}" } ?: ADD_ITEM
    }
}
