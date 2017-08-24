package ru.xmn.russiancraftbeer.screens.map.ui

import android.arch.lifecycle.Observer
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.item_pub_contact.view.*
import kotlinx.android.synthetic.main.pub_sheet.view.*
import org.jetbrains.anko.toast
import ru.xmn.common.extensions.inflate
import ru.xmn.common.extensions.loadUrl
import ru.xmn.common.ui.adapter.AutoUpdatableAdapter
import ru.xmn.russiancraftbeer.R
import ru.xmn.russiancraftbeer.services.beer.MapPoint
import ru.xmn.russiancraftbeer.services.beer.PubDto
import ru.xmn.russiancraftbeer.services.beer.PubMapDto
import kotlin.properties.Delegates

class PubPagerAdapter(private val activity: MapsActivity, val pubViewModelFactory: (pub: PubMapDto) -> PubViewModel) : PagerAdapter() {
    val TAG = R.string.PubPagerAdapterTag

    var items by Delegates.observable<List<PubMapDto>>(emptyList(), onChange = { _, _, value -> notifyDataSetChanged() })
    var offset = 0f

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return items.count()
    }

    override fun getPageTitle(position: Int): CharSequence {
        return items[position].uniqueTag
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val pubMapDto = items[position]
        val inflater = LayoutInflater.from(container.context);
        val layout = inflater.inflate(R.layout.pub_sheet, container, false)
        layout.setTag(TAG, pubMapDto.uniqueTag)
        bind(layout, pubMapDto)
        container.addView(layout)
        return layout;
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

    private fun bind(layout: View, pubMapDto: PubMapDto) {
        layout.apply {
            ViewCompat.setNestedScrollingEnabled(nestedScrollView, true)
            pubTitle.text = pubMapDto.title
            pubType.text = pubMapDto.type
            pubLogo.loadUrl(pubMapDto.field_logo ?: "")

            val pubViewModel = pubViewModelFactory(pubMapDto)
            pubViewModel.mapState.observe(activity, Observer {
                when {
                    it is PubState.Loading -> {
                        progressBarTopLayout.visibility = View.VISIBLE
                        bindPub(layout, PubDto.empty())
                    }
                    it is PubState.Success -> {
                        progressBarTopLayout.visibility = View.INVISIBLE
                        bindPub(layout, it.pub)
                    }
                    it is PubState.Error -> {
                        activity.toast(it.errorMessage)
                    }
                }
            })
        }
    }

    fun bindPub(layout: View, pub: PubDto) {
        (layout as ViewGroup?)?.let {
            TransitionManager.beginDelayedTransition(layout)
        }
        layout.apply {
            pubDescription.text = pub.body
            pubContacts.layoutManager = LinearLayoutManager(activity)
            pubContacts.adapter = PubContactsAdapter.from(pub.address, pub.map!!, pub.phones, pub.site)
        }
        performOffset(activity, layout.pubCard, offset)
    }
}

class PubContactsAdapter() : RecyclerView.Adapter<PubContactsAdapter.PubContactsViewHolder>(), AutoUpdatableAdapter {
    companion object {
        fun from(adress: List<String>?, map: List<MapPoint>, phones: List<String>?, site: List<String>?): PubContactsAdapter {
            val items = ArrayList<ContactItem>()
            items += adress!!.zip(map).map { ContactItem(R.drawable.ic_close_black_24dp, it.first, View.OnClickListener { }) }
            items += phones!!.map { ContactItem(R.drawable.ic_close_black_24dp, it, View.OnClickListener { }) }
            items += site!!.map { ContactItem(R.drawable.ic_close_black_24dp, it, View.OnClickListener { }) }

            return PubContactsAdapter().also { it.items = items }
        }
    }

    var items: List<ContactItem> by Delegates.observable(emptyList(),
            { property, oldValue, newValue ->
                autoNotify(oldValue, newValue) { a, b -> a.uniqueTag == b.uniqueTag }
            })

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PubContactsViewHolder = PubContactsViewHolder(parent.inflate(R.layout.item_pub_contact))
    override fun onBindViewHolder(holder: PubContactsViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.count()

    class PubContactsViewHolder(val v: View) : RecyclerView.ViewHolder(v) {
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
