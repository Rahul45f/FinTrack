package com.fintrack.app

import android.content.Context
import com.google.gson.Gson
import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val amount: Double,
    val description: String,
    val date: String,
    val settled: Boolean = false,
    val dueDate: String? = null
)

data class Person(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phone: String = "",
    val transactions: List<Transaction> = emptyList()
)

data class Investment(
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val name: String,
    val investedAmount: Double,
    val currentValue: Double,
    val date: String,
    val notes: String = ""
)

data class UserData(
    val accountBalance: Double = 0.0,
    val people: List<Person> = emptyList(),
    val investments: List<Investment> = emptyList()
)

data class UserProfile(val name: String, val pin: String)

data class FTCategory(val id: String, val label: String, val icon: String, val colorHex: Long)

val CATS = listOf(
    FTCategory("stocks",       "Stocks",         "📈", 0xFF0A84FF),
    FTCategory("crypto",       "Crypto",         "₿",  0xFFFF9F0A),
    FTCategory("mutual_funds", "Mutual Funds",   "🏦", 0xFF30D158),
    FTCategory("fd",           "Fixed Deposits", "🔒", 0xFFBF5AF2),
    FTCategory("real_estate",  "Real Estate",    "🏘", 0xFFFF6B35),
    FTCategory("gold",         "Gold",           "🥇", 0xFFFFD60A),
)

class AppStorage(ctx: Context) {
    private val p = ctx.getSharedPreferences("ft", Context.MODE_PRIVATE)
    private val g = Gson()

    var profile: UserProfile?
        get() = p.getString("profile", null)
            ?.let { runCatching { g.fromJson(it, UserProfile::class.java) }.getOrNull() }
        set(v) = if (v == null) p.edit().remove("profile").apply()
        else p.edit().putString("profile", g.toJson(v)).apply()

    var dark: Boolean
        get() = p.getBoolean("dark", true)
        set(v) { p.edit().putBoolean("dark", v).apply() }

    fun load(name: String): UserData =
        p.getString("d_$name", null)
            ?.let { runCatching { g.fromJson(it, UserData::class.java) }.getOrNull() }
            ?: UserData()

    fun save(name: String, data: UserData) =
        p.edit().putString("d_$name", g.toJson(data)).apply()
}
