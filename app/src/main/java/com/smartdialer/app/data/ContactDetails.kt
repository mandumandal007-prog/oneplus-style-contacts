package com.smartdialer.app.data

data class ContactDetails(
    val name: String,
    val starred: Boolean,
    val phones: List<PhoneEntry>,
    val emails: List<String>
)
