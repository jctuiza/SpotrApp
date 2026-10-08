package com.example.spotrapp.ui.common

import com.example.spotrapp.data.local.ItemEntity
import com.example.spotrapp.data.local.ZoneEntity

data class ZoneChoices(
    val suggested: List<String>,
    val other: List<String>
)

/**
reference para sa gagawa ng zone function:
suggested zone =
- recently used zone o newest zone
- current zone (para lang to sa edit item screen)
other zone = remaining zones
 */
fun splitZones(
    zones: List<ZoneEntity>,
    items: List<ItemEntity>,
    currentZone: String? = null,
    maxSuggested: Int = 2
): ZoneChoices {

    val known = zones.map { it.name }

    val recentlyUsed = items
        .sortedByDescending { it.id }
        .mapNotNull { item ->
            zones.find { zone ->
                zone.id == item.zoneId
            }?.name
        }
        .distinct()
        .filter { it in known }

    val suggested = buildList {
        currentZone
            ?.takeIf { it in known }
            ?.let { add(it) }
        recentlyUsed.forEach {
            if (it !in this && size < maxSuggested) {
                add(it)
            }
        }
        known.forEach {
            if (it !in this && size < maxSuggested) {
                add(it)
            }
        }
    }

    return ZoneChoices(
        suggested = suggested,
        other = known.filter { it !in suggested }
    )
}