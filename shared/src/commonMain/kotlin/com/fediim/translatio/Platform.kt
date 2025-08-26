package com.fediim.translatio

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform