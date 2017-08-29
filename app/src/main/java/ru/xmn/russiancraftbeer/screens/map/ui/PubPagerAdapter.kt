package ru.xmn.russiancraftbeer.screens.map.ui

import android.arch.lifecycle.Observer
import android.support.transition.Fade
import android.support.transition.TransitionManager
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.pub_sheet.view.*
import ru.xmn.common.extensions.gone
import ru.xmn.common.extensions.invisible
import ru.xmn.common.extensions.loadUrl
import ru.xmn.common.extensions.visible
import ru.xmn.common.transformations.BlurTransformation
import ru.xmn.common.transformations.RoundedCornersTransformation
import ru.xmn.russiancraftbeer.R
import ru.xmn.russiancraftbeer.services.beer.PubDto
import ru.xmn.russiancraftbeer.services.beer.PubMapDto
import kotlin.properties.Delegates


class PubPagerAdapter(private val activity: MapsActivity, val pubViewModelFactory: (PubMapDto) -> PubViewModel, val itemClick: (position: Int) -> Unit) : PagerAdapter() {
    val TAG = R.string.PubPagerAdapterTag

    var items by Delegates.observable<List<PubMapDto>>(emptyList(), onChange = { _, _, value -> notifyDataSetChanged() })
    var offset = 0f
    var observers: MutableMap<String, Observer<PubState>> = HashMap()

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
        bind(layout, pubMapDto, position)
        container.addView(layout)
        return layout;
    }

    override fun destroyItem(viewGroup: ViewGroup, position: Int, view: Any) {
        val pubViewModel = pubViewModelFactory(items[position])
        val tag = items[position].uniqueTag
        pubViewModel.mapState.removeObserver(observers.get(tag))
        observers.remove(tag)
        viewGroup.removeView(view as View)
    }

    private fun bind(layout: View, pubMapDto: PubMapDto, position: Int) {
        layout.apply {
            ViewCompat.setNestedScrollingEnabled(nestedScrollView, true)
            pubTitle.text = pubMapDto.title
            pubType.text = pubMapDto.type
            pubLogo.loadUrl(pubMapDto.field_logo ?: "", pubLogoProgressBar,
                    transformations = listOf(RoundedCornersTransformation(pubLogoBack.context, 5, 0, RoundedCornersTransformation.CornerType.ALL)))
            pubLogoBack.loadUrl(
                    pubMapDto.field_logo ?: "",
                    transformations = listOf(BlurTransformation(pubLogoBack.context))
            )

            val pubViewModel = pubViewModelFactory(pubMapDto)
            val observer = Observer<PubState> {
                when {
                    it is PubState.Loading -> {
                        progressBarTopLayout.visible()
                        pub_error_button.gone()
                        pub_error_text.gone()
                        bindPub(layout, PubDto.empty(), position, View.OnClickListener{pubViewModel.refresh()})
                    }
                    it is PubState.Success -> {
                        progressBarTopLayout.invisible()
                        pub_error_button.gone()
                        pub_error_text.gone()
                        bindPub(layout, it.pub, position, View.OnClickListener{pubViewModel.refresh()})
                    }
                    it is PubState.Error -> {
                        progressBarTopLayout.invisible()
                        pub_error_button.visible()
                        pub_error_text.visible()
                        bindPub(layout, PubDto.empty(), position, View.OnClickListener{pubViewModel.refresh()})
                    }
                }
            }
            observers.put(items[position].uniqueTag, observer)
            pubViewModel.mapState.observe(activity, observer)
        }
    }

    fun bindPub(layout: View, pub: PubDto, position: Int, errorClick: View.OnClickListener) {
        (layout as ViewGroup?)?.let {
            val transition = Fade()
                    .addTarget(layout.pubContacts)
                    .addTarget(layout.pubDescription)
                    .addTarget(layout.progressBarTopLayout)
            TransitionManager.beginDelayedTransition(layout, transition)
        }
        layout.apply {
            pub_error_button.setOnClickListener(errorClick)
            pubContacts.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            pubDescription.setLayerType(View.LAYER_TYPE_HARDWARE, null)
            progressBarTopLayout.setLayerType(View.LAYER_TYPE_HARDWARE, null)

            pubLogo.setOnClickListener { itemClick(position) }
            topPanelTextBack.setOnClickListener { itemClick(position) }
            pubDescription.text = pub.body
            pubContacts.layoutManager = LinearLayoutManager(activity)
            pubContacts.adapter = PubContactsAdapter.from(pub.address, pub.map!!, pub.phones, pub.site)
        }
        performOffset(activity, layout.pubCard, offset)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }
}
