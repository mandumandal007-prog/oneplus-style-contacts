package com.smartdialer.app.data

import android.content.Context
import android.provider.ContactsContract

object ContactsRepository {

    fun load(context: Context): List<ContactItem> {
        val result = ArrayList<ContactItem>()
        val seen = HashSet<String>()
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI,
            ContactsContract.CommonDataKinds.Phone.STARRED
        )
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            cursor?.use { c ->
                val idIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
                val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
                val starIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)
                while (c.moveToNext()) {
                    val number = c.getString(numberIndex) ?: continue
                    val clean = number.replace(" ", "")
                    if (clean.isEmpty() || seen.contains(clean)) continue
                    seen.add(clean)
                    val name = c.getString(nameIndex) ?: ""
                    result.add(
                        ContactItem(
                            id = c.getLong(idIndex),
                            name = name,
                            number = number,
                            photoUri = c.getString(photoIndex),
                            starred = c.getInt(starIndex) == 1
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // permission missing or provider unavailable
        }
        return result
    }

    fun loadDetails(context: Context, contactId: Long): ContactDetails {
        val phones = ArrayList<PhoneEntry>()
        val emails = ArrayList<String>()
        var name = ""
        var starred = false

        try {
            val phoneCursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.STARRED
                ),
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                arrayOf(contactId.toString()),
                null
            )
            phoneCursor?.use { c ->
                val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val typeIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
                val starIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.STARRED)
                while (c.moveToNext()) {
                    if (name.isEmpty()) {
                        name = c.getString(nameIndex) ?: ""
                        starred = c.getInt(starIndex) == 1
                    }
                    val number = c.getString(numberIndex) ?: continue
                    val type = c.getInt(typeIndex)
                    phones.add(PhoneEntry(number, typeLabel(type)))
                }
            }
        } catch (e: Exception) {
            // ignore
        }

        try {
            val emailCursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                arrayOf(contactId.toString()),
                null
            )
            emailCursor?.use { c ->
                val addressIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)
                while (c.moveToNext()) {
                    val address = c.getString(addressIndex) ?: continue
                    if (address.isNotEmpty()) emails.add(address)
                }
            }
        } catch (e: Exception) {
            // ignore
        }

        return ContactDetails(name, starred, phones, emails)
    }

    private fun typeLabel(type: Int): String {
        return when (type) {
            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "Mobile"
            ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "Home"
            ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "Work"
            ContactsContract.CommonDataKinds.Phone.TYPE_MAIN -> "Main"
            ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK -> "Work fax"
            ContactsContract.CommonDataKinds.Phone.TYPE_OTHER -> "Other"
            else -> "Phone"
        }
    }
}
