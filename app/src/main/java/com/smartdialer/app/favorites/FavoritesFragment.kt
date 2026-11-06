package com.smartdialer.app.favorites

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.smartdialer.app.R
import com.smartdialer.app.data.ContactItem
import com.smartdialer.app.data.ContactsRepository
import com.smartdialer.app.data.SpamRepository
import com.smartdialer.app.detail.ContactDetailActivity
import com.smartdialer.app.ui.ContactAdapter
import com.smartdialer.app.util.CallLauncher

class FavoritesFragment : Fragment() {

    private lateinit var adapter: ContactAdapter
    private lateinit var emptyView: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = view.findViewById<RecyclerView>(R.id.favorites_list)
        emptyView = view.findViewById(R.id.favorites_empty)
        adapter = ContactAdapter(
            emptyList(),
            { item -> openDetail(item) },
            { item -> showOptions(item.number, item.name) }
        )
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val contacts = try {
            ContactsRepository.load(requireContext())
        } catch (e: Exception) {
            emptyList<ContactItem>()
        }
        val favorites = contacts.filter { it.starred }
        adapter.update(favorites)
        emptyView.visibility = if (favorites.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun openDetail(item: ContactItem) {
        val intent = Intent(requireContext(), ContactDetailActivity::class.java)
        intent.putExtra(ContactDetailActivity.EXTRA_CONTACT_ID, item.id)
        intent.putExtra(ContactDetailActivity.EXTRA_CONTACT_NAME, item.name)
        intent.putExtra(ContactDetailActivity.EXTRA_CONTACT_NUMBER, item.number)
        startActivity(intent)
    }

    private fun showOptions(number: String, name: String?) {
        val currentContext = context ?: return
        val options = arrayOf("Call", "WhatsApp", "SMS", "Block number")
        AlertDialog.Builder(currentContext)
            .setTitle(if (name.isNullOrBlank()) number else name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val activity = activity as? AppCompatActivity ?: return@setItems
                        CallLauncher.dial(activity, number)
                    }
                    1 -> CallLauncher.whatsapp(currentContext, number)
                    2 -> CallLauncher.sms(currentContext, number)
                    3 -> SpamRepository.block(currentContext, number)
                }
            }
            .show()
    }
}
