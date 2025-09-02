// com/jeju/evtravel/domain/model/User.kt
package com.jeju.evtravel.domain.model

import androidx.annotation.Keep
import com.google.firebase.firestore.IgnoreExtraProperties

@Keep
@IgnoreExtraProperties
data class User(
    var uid: String = "",
    var displayName : String = "",
    var imageUrl: String? = null,
    var email: String? = null,
    var createdAt: Long = 0,
) {
    constructor(): this("", "", null, null, 0)
}
