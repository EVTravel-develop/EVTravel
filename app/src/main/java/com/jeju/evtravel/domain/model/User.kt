// com/jeju/evtravel/domain/model/User.kt
package com.jeju.evtravel.domain.model

data class User(
    val uid: String = "",
    val displayName : String = "",
    val imageUrl: String? = null,
    val email: String? = null,
    val createdAt: Long = 0,
)
