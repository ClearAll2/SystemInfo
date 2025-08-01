package com.lkonlesoft.displayinfo.`object`

import androidx.annotation.StringRes
import com.lkonlesoft.displayinfo.R

sealed class AboutItem(@param:StringRes val title: Int, @param:StringRes val text: Int, @param:StringRes val url: Int){
    object AppVer: AboutItem(R.string.app_version,
        R.string.app_ver,
        R.string.rate_link
    )

    object Rate: AboutItem(
        R.string.rate_app,
        R.string.rate_text,
        R.string.rate_link
    )

    object Privacy: AboutItem(R.string.privacy_policy,
        R.string.privacy_text,
        R.string.privacy_link
    )

    object Terms: AboutItem(
        R.string.terms,
        R.string.terms_text,
        R.string.term_link
    )

    object More: AboutItem(R.string.more_app,
        R.string.more_app_text,
        R.string.more_app_link
    )
    object Contact: AboutItem(R.string.copy_right,
        R.string.contact_text,
        R.string.contact_link
    )
}
