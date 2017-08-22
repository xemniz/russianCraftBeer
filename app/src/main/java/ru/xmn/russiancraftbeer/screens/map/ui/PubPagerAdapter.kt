package ru.xmn.russiancraftbeer.screens.map.ui

import android.arch.lifecycle.Observer
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.pub_sheet.view.*
import org.jetbrains.anko.toast
import ru.xmn.common.extensions.loadUrl
import ru.xmn.russiancraftbeer.R
import ru.xmn.russiancraftbeer.services.beer.PubDto
import ru.xmn.russiancraftbeer.services.beer.PubMapDto
import kotlin.properties.Delegates

class PubPagerAdapter(private val activity: MapsActivity, val pubViewModel: PubViewModel) : PagerAdapter() {
    val TAG = R.string.PubPagerAdapterTag

    var items by Delegates.observable<List<PubMapDto>>(emptyList(), onChange = { _, _, value -> notifyDataSetChanged() })

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return items.count()
    }

    override fun getPageTitle(position: Int): CharSequence {
        return items[position].nid
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val pubMapDto = items[position]
        val inflater = LayoutInflater.from(container.context);
        val layout = inflater.inflate(R.layout.pub_sheet, container, false)
        layout.setTag(TAG, pubMapDto.nid)
        bind(layout, pubMapDto)
        container.addView(layout)
        return layout;
    }

    override fun destroyItem(collection: ViewGroup, position: Int, view: Any) {
        collection.removeView(view as View)
    }

//    override fun getItemPosition(`object`: Any?): Int {
//        return PagerAdapter.POSITION_NONE
//    }

    private fun bind(layout: View, pubMapDto: PubMapDto) {
        layout.apply {
            pubTitle.text = pubMapDto.title
            pubType.text = pubMapDto.type
            pubLogo.loadUrl(pubMapDto.field_logo?:"")

            pubViewModel.mapState.observe(activity, Observer {
                when {
                    it is PubState.Loading -> {
                    progressBarTopLayout.visibility = View.VISIBLE
                    bindPub(layout, PubDto.empty())
                    }
                    it is PubState.Success -> {
                    progressBarTopLayout.visibility = View.GONE
                    bindPub(layout, it.pub)
                    }
                    it is PubState.Error -> {
                        activity.toast(it.errorMessage)
                    }
                }
            })
            pubViewModel.clickPub(pubMapDto.nid)
        }
    }

    fun bindPub(layout: View, pub: PubDto) {
        layout.apply {
            pubDescription.text = pub.body
        }
    }
}