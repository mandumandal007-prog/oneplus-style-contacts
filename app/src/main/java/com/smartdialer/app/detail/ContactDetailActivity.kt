package com.smartdialer.app.detail

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.smartdialer.app.R
import com.smartdialer.app.data.ContactDetails
import com.smartdialer.app.data.ContactsRepository
import com.smartdialer.app.data.PhoneEntry
import com.smartdialer.app.util.AvatarColor
import com.smartdialer.app.util.CallLauncher

class ContactDetailActivity : AppCompatActivity() {

    private var details: ContactDetails? = null
    private var fallbackNumber: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_detail)

        val contactId = intent.getLongExtra(EXTRA_CONTACT_ID, -1L)
        val name = intent.getStringExtra(EXTRA_CONTACT_NAME) ?: ""
        fallbackNumber = intent.getStringExtra(EXTRA_CONTACT_NUMBER) ?: ""

        val avatar = findViewById<TextView>(R.id.detail_avatar)
        val nameView = findViewById<TextView>(R.id.detail_name)
        val subtitle = findViewById<TextView>(R.id.detail_subtitle)

        val displayName = if (name.isBlank()) fallbackNumber else name
        nameView.text = displayName
        avatar.text = displayName.take(1).uppercase()
        avatar.backgroundTintList = ColorStateList.valueOf(AvatarColor.colorFor(displayName))
        avatar.setTextColor(Color.WHITE)

        details = if (contactId > 0) {
            try {
                ContactsRepository.loadDetails(this, contactId)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }

        val phones = details?.phones ?: emptyList()
        val emails = details?.emails ?: emptyList()

        val phoneCount = if (phones.isEmpty()) 1 else phones.size
        subtitle.text = if (phoneCount == 1) "1 phone number" else phoneCount.toString() + " phone numbers"

        findViewById<View>(R.id.btn_back).setOnClickListener { finish() }

        val primaryNumber = if (phones.isNotEmpty()) phones[0].number else fallbackNumber

        findViewById<View>(R.id.action_call).setOnClickListener {
            if (primaryNumber.isNotEmpty()) CallLauncher.dial(this, primaryNumber)
        }

        findViewById<View>(R.id.action_message).setOnClickListener {
            if (primaryNumber.isNotEmpty()) CallLauncher.sms(this, primaryNumber)
        }

        findViewById<View>(R.id.action_share).setOnClickListener {
            val share = Intent(Intent.ACTION_SEND)
            share.type = "text/plain"
            share.putExtra(Intent.EXTRA_TEXT, displayName + " - " + primaryNumber)
            startActivity(Intent.createChooser(share, "Share contact"))
        }

        val phoneContainer = findViewById<LinearLayout>(R.id.phone_container)
        phoneContainer.removeAllViews()
        if (phones.isEmpty() && fallbackNumber.isNotEmpty()) {
            phoneContainer.addView(buildRow(fallbackNumber, "Phone", true))
        }
        for (entry in phones) {
            phoneContainer.addView(buildRow(entry.number, entry.label, true))
        }

        val emailLabel = findViewById<TextView>(R.id.email_label)
        val emailContainer = findViewById<LinearLayout>(R.id.email_container)
        emailContainer.removeAllViews()
        if (emails.isNotEmpty()) {
            emailLabel.visibility = View.VISIBLE
            for (address in emails) {
                emailContainer.addView(buildRow(address, "Email", false))
            }
        } else {
            emailLabel.visibility = View.GONE
        }
    }

    private fun buildRow(value: String, label: String, isPhone: Boolean): View {
        val row = LinearLayout(this)
        row.orientation = LinearLayout.VERTICAL
        row.setPadding(0, 18, 0, 18)
        row.isClickable = true
        row.background = ContextCompat.getDrawable(this, R.drawable.bg_input)

        val main = TextView(this)
        main.text = value
        main.textSize = 16f
        main.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        row.addView(main)

        val sub = TextView(this)
        sub.text = label
        sub.textSize = 12f
        sub.setPadding(0, 4, 0, 0)
        sub.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        row.addView(sub)

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.bottomMargin = 10
        row.layoutParams = params
        row.gravity = Gravity.START

        row.setOnClickListener {
            if (isPhone) {
                CallLauncher.dial(this, value)
            } else {
                CallLauncher.email(this, value)
            }
        }
        return row
    }

    companion object {
        const val EXTRA_CONTACT_ID = "contact_id"
        const val EXTRA_CONTACT_NAME = "contact_name"
        const val EXTRA_CONTACT_NUMBER = "contact_number"
    }
}
