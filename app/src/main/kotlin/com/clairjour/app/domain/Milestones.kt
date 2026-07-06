package com.clairjour.app.domain

import androidx.annotation.StringRes
import com.clairjour.app.R

data class Milestone(
    val days: Int,
    @StringRes val labelRes: Int
)

object Milestones {
    val all: List<Milestone> = listOf(
        Milestone(1, R.string.milestone_1d),
        Milestone(3, R.string.milestone_3d),
        Milestone(7, R.string.milestone_7d),
        Milestone(14, R.string.milestone_14d),
        Milestone(30, R.string.milestone_30d),
        Milestone(60, R.string.milestone_60d),
        Milestone(90, R.string.milestone_90d),
        Milestone(180, R.string.milestone_180d),
        Milestone(365, R.string.milestone_365d),
        Milestone(730, R.string.milestone_730d),
        Milestone(1095, R.string.milestone_1095d),
        Milestone(1825, R.string.milestone_1825d),
        Milestone(3650, R.string.milestone_3650d)
    )

    fun reached(days: Int): List<Milestone> = all.filter { it.days <= days }

    fun next(days: Int): Milestone? = all.firstOrNull { it.days > days }

    fun progressToNext(days: Int): Float {
        val next = next(days) ?: return 1f
        val previous = all.lastOrNull { it.days <= days }?.days ?: 0
        val span = (next.days - previous).coerceAtLeast(1)
        val done = (days - previous).coerceAtLeast(0)
        return (done.toFloat() / span.toFloat()).coerceIn(0f, 1f)
    }
}
