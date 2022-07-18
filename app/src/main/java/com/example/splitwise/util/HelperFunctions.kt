package com.example.splitwise.util

fun nameCheck(value: String): Boolean {
    return value.matches("^[a-zA-Z0-9-&\"'.\n /,]+$".toRegex())
}