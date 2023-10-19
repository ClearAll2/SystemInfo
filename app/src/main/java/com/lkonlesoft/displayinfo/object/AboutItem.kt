package com.lkonlesoft.displayinfo.`object`

import androidx.annotation.StringRes
import com.lkonlesoft.displayinfo.R

sealed class AboutItem(var tittle: String, @StringRes var text: Int, @StringRes var url: Int){
    object AppVer: AboutItem("App version",
        R.string.app_ver,
        R.string.rate_link
    )
    object Privacy: AboutItem("Privacy Policy",
        R.string.privacy_text,
        R.string.privacy_link
    )

    object More: AboutItem("More apps",
        R.string.more_app_text,
        R.string.more_app_link
    )
    object Contact: AboutItem("© 2023 LKONLE",
        R.string.contact_text,
        R.string.contact_link
    )
}
