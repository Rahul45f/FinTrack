package com.fintrack.app

import android.content.Context
import android.provider.ContactsContract

data class PhoneContact(val name: String, val phone: String)

fun fetchPhoneContacts(ctx: Context): List<PhoneContact> {
    val list = mutableListOf<PhoneContact>()
    try {
        val cursor = ctx.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        ) ?: return list
        cursor.use {
            val ni = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val pi = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val name = it.getString(ni) ?: continue
                val phone = it.getString(pi) ?: ""
                list.add(PhoneContact(name.trim(), phone.trim()))
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list.distinctBy { it.name }.sortedBy { it.name }
}
