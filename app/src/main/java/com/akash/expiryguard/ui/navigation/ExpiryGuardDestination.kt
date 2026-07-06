package com.akash.expiryguard.ui.navigation

object ExpiryGuardDestination {
    const val HOME = "home"
    const val ADD_EDIT = "add_edit"
    const val EXPENSE_INSIGHTS = "expense_insights"
    const val SETTINGS = "settings"
    const val DETAIL_ROUTE = "detail/{itemId}"

    fun detail(itemId: String): String = "detail/$itemId"
}
