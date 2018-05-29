package ru.xmn.russiancraftbeer.screens.map.ui.pubviewmodel

import android.content.Intent
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_pub_contact.view.*
import ru.xmn.common.extensions.inflate
import ru.xmn.common.ui.adapter.AutoUpdatableAdapter
import ru.xmn.russiancraftbeer.R
import ru.xmn.russiancraftbeer.screens.map.bl.data.MapPoint
import kotlin.properties.Delegates

class PubContactsAdapter : RecyclerView.Adapter<PubContactsAdapter.PubContactsViewHolder>(), AutoUpdatableAdapter {
    companion object {
        fun from(adress: List<String>?, map: List<MapPoint>, phones: List<String>?, site: List<String>?): PubContactsAdapter {
            val items = ArrayList<ContactItem>()
            if (adress != null) {
                items += adress.zip(map).map {
                    ContactItem(R.drawable.ic_location_on_black_24dp, it.first, View.OnClickListener { view ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${it.second.lat},${it.second.long}"))
                        ContextCompat.startActivity(view.context, intent, null)
                    })
                }
            }
            if (phones != null) {
                items += phones.map {
                    ContactItem(R.drawable.ic_local_phone_black_24dp, it, View.OnClickListener { view ->
                        val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$it"))
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        ContextCompat.startActivity(view.context, i, null)
                    })
                }
            }
            if (site != null) {
                items += site.map {
                    ContactItem(R.drawable.ic_language_black_24dp, it, View.OnClickListener { view ->
                        val i = Intent(Intent.ACTION_VIEW)
                        i.data = Uri.parse(it)
                        ContextCompat.startActivity(view.context, i, null)
                    })
                }
            }

            return PubContactsAdapter().also { it.items = items }
        }
    }

    var items: List<ContactItem> by Delegates.observable(emptyList(),
            { _, oldValue, newValue ->
                autoNotify(oldValue, newValue) { a, b -> a.uniqueTag == b.uniqueTag }
            })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PubContactsViewHolder = PubContactsViewHolder(parent.inflate(R.layout.item_pub_contact))
    override fun onBindViewHolder(holder: PubContactsViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.count()

    class PubContactsViewHolder(private val v: View) : RecyclerView.ViewHolder(v) {
        fun bind(item: ContactItem) {

            v.apply {
                contactIcon.setImageResource(item.iconRes)
                contactTitle.text = item.title
                setOnClickListener(item.click)
            }
        }

    }
}

class ContactItem(val iconRes: Int, val title: CharSequence, val click: View.OnClickListener) {
    val uniqueTag
        get() = title
}