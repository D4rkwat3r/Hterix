package com.darkwater.hterix2.objects

data class Community(
    val name: String,
    val ndcId: String,
    val iconUrl: String,
    var onlineMembersCount: Int? = null
)
