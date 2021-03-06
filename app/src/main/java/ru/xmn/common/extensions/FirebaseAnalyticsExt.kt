package ru.xmn.common.extensions

import com.google.firebase.analytics.FirebaseAnalytics
import android.R.attr.name
import android.os.Bundle


/**
 * Created by xmn on 29.08.2017.
 */
fun FirebaseAnalytics.log(eventType: String, vararg params: Pair<String, String>) {
    val bundle = params.fold(Bundle()) { acc, pair -> acc.putString(pair.first, pair.second); acc}
    this.logEvent(eventType, bundle)
}