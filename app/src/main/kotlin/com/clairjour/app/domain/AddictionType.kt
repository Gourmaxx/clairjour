package com.clairjour.app.domain

import androidx.annotation.StringRes
import com.clairjour.app.R

enum class AddictionType(@StringRes val labelRes: Int) {
    ALCOHOL(R.string.addiction_type_alcohol),
    TOBACCO(R.string.addiction_type_tobacco),
    DRUG(R.string.addiction_type_drug),
    SUGAR(R.string.addiction_type_sugar),
    SCREEN(R.string.addiction_type_screen),
    GAMBLING(R.string.addiction_type_gambling),
    PORN(R.string.addiction_type_porn),
    OTHER(R.string.addiction_type_other);

    companion object {
        fun fromName(name: String?): AddictionType =
            entries.firstOrNull { it.name == name } ?: OTHER
    }
}
