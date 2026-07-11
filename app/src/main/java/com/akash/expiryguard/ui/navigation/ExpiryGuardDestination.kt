package com.akash.expiryguard.ui.navigation

object ExpiryGuardDestination {
    const val HOME = "home"
    const val ADD_EDIT = "add_edit?itemId={itemId}"
    const val EXPENSE_INSIGHTS = "expense_insights"
    const val SETTINGS = "settings"
    const val DETAIL_ROUTE = "detail/{itemId}"

    fun addEdit(itemId: String? = null): String {
        return if (itemId.isNullOrBlank()) "add_edit" else "add_edit?itemId=$itemId"
    }

    fun detail(itemId: String): String = "detail/$itemId"
}
