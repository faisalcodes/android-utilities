package com.faisalcodes.android_utilities.utils.html.tagHandlers

import android.text.Editable
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import org.xml.sax.Attributes


/**
 * Provides a way to handle anchor links in HTML text.
 * @param callback: The callback to be invoked when the link is clicked. The link is passed as a parameter.
 * @param underline: Whether to underline the link or not.
 * @param color: The color of the link. If null, the default color will be used.
 */
class AnchorLinkTagHandler(
    private val underline: Boolean = false,
    private val color: Int? = null,
    private val callback: (String) -> Unit,
) : TagHandlerImpl {
    private var tagStartIndex = -1
    private var anchorLink: String? = null

    override fun handleOpening(tag: String, output: Editable, attributes: Attributes): Boolean {
        if (tag != "a") return false

        tagStartIndex = output.length
        anchorLink = attributes.getValue("href")

        return true
    }

    override fun handleClosing(tag: String, output: Editable): Boolean {
        if (tag != "a") return false

        applyLinkSpans(output, tagStartIndex, output.length, anchorLink!!)
        tagStartIndex = -1
        anchorLink = null

        return true
    }

    private fun applyLinkSpans(output: Editable, start: Int, end: Int, link: String) {
        output.setSpan(
            object : ClickableSpan() {
                override fun onClick(widget: View) {
                    callback(link)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = underline
                    color?.let { ds.color = it }
                }

            }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}