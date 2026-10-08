package com.example.spotrapp.navigation

object Routes {

    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val SEARCH = "search"
    const val VOICE_SEARCH = "voiceSearch"

    // Camera scan screen
    const val PUTAWAY = "putaway"

    // Add item screen — photoPath is an optional query-style arg so
    // Routes.ADD_ITEM (no photo) and Routes.addItem(path) (from Camera)
    // both resolve to the same route.
    const val ADD_ITEM = "add_item?photoPath={photoPath}"

    fun addItem(photoPath: String? = null): String =
        if (photoPath != null) "add_item?photoPath=$photoPath" else "add_item"

    const val ITEM_DETAILS = "item_details/{itemId}"

    fun itemDetails(itemId: Int) =
        "item_details/$itemId"

    const val EDIT_ITEM = "edit_item/{itemId}"

    fun editItem(itemId: Int) =
        "edit_item/$itemId"

    // put the tutorial order in a list
    private val TUTORIAL_STEP_ORDER = listOf(DASHBOARD, PUTAWAY, ADD_ITEM)

    // goes on to the next step based on currentstep and index
    fun nextTutorialStep(currentStep: String): String? {
        val index = TUTORIAL_STEP_ORDER.indexOf(currentStep)
        return TUTORIAL_STEP_ORDER.getOrNull(index + 1)
    }
}