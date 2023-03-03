package com.faisalcodes.android_utilities.utils.html

import android.text.Editable
import android.text.Html
import com.faisalcodes.android_utilities.utils.html.tagHandlers.TagHandlerImpl
import org.xml.sax.Attributes
import org.xml.sax.ContentHandler
import org.xml.sax.Locator
import org.xml.sax.XMLReader

/**
 * A custom HTML parser that uses [TagHandlerImpl] to handle tags.
 */
class HtmlParser(
    private val tagHandler: TagHandlerImpl
) : Html.TagHandler, ContentHandler {
    private val tagStatus = ArrayDeque<Boolean>()
    private var wrapped: ContentHandler? = null
    private var text: Editable? = null

    override fun handleTag(opening: Boolean, tag: String, output: Editable, xmlReader: XMLReader) {
        if (wrapped == null) {
            // record result object
            text = output
            // record current content handler
            wrapped = xmlReader.contentHandler
            // replace content handler with our own that forwards the calls to original handler when needed
            xmlReader.contentHandler = this
            // add false to the stack to make sure we always have a tag to pop
            tagStatus.addLast(false)
        }
    }

    override fun startElement(
        uri: String, localName: String, qName: String, attributes: Attributes
    ) {
        var isHandled = false

        if (text != null) {
            isHandled = tagHandler.handleOpening(localName, text!!, attributes)
        }

        tagStatus.addLast(isHandled)

        if (!isHandled) {
            wrapped?.startElement(uri, localName, qName, attributes)
        }
    }

    override fun endElement(uri: String, localName: String, qName: String) {
        if (!tagStatus.removeLast()) {
            wrapped?.endElement(uri, localName, qName)
        }

        if (text != null) {
            tagHandler.handleClosing(localName, text!!)
        }
    }

    override fun setDocumentLocator(locator: Locator) {
        wrapped?.setDocumentLocator(locator)
    }

    override fun startDocument() {
        wrapped?.startDocument()
    }

    override fun endDocument() {
        wrapped?.endDocument()
    }

    override fun startPrefixMapping(prefix: String, uri: String) {
        wrapped?.startPrefixMapping(prefix, uri)
    }

    override fun endPrefixMapping(prefix: String) {
        wrapped?.endPrefixMapping(prefix)
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        wrapped?.characters(ch, start, length)
    }

    override fun ignorableWhitespace(ch: CharArray, start: Int, length: Int) {
        wrapped?.ignorableWhitespace(ch, start, length)
    }

    override fun processingInstruction(target: String, data: String) {
        wrapped?.processingInstruction(target, data)
    }

    override fun skippedEntity(name: String) {
        wrapped?.skippedEntity(name)
    }
}