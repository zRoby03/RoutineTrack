package com.example.routinetrack.data.mapper

import com.example.routinetrack.data.local.entity.UserEntity
import com.example.routinetrack.domain.model.User

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        remoteId = remoteId,
        email = email,
        displayName = displayName,
        token = token,
        isLoggedIn = isLoggedIn
    )
}

fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        remoteId = remoteId,
        email = email,
        displayName = displayName,
        token = token,
        isLoggedIn = isLoggedIn
    )
}
