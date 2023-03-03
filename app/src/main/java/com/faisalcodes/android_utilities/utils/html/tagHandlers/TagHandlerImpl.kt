package com.faisalcodes.android_utilities.utils.html.tagHandlers

import android.text.Editable
import org.xml.sax.Attributes

/**
 * Provides a way to handle tags in HTML text.
 */
interface TagHandlerImpl {
    fun handleOpening(tag: String, output: Editable, attributes: Attributes): Boolean

    fun handleClosing(tag: String, output: Editable): Boolean
}