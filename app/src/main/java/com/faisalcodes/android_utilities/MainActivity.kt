package com.faisalcodes.android_utilities

import android.graphics.Color
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.text.method.LinkMovementMethod
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.text.parseAsHtml
import com.faisalcodes.android_utilities.databinding.ActivityMainBinding
import com.faisalcodes.android_utilities.utils.bridge.AppBridge
import com.faisalcodes.android_utilities.utils.html.HtmlParser
import com.faisalcodes.android_utilities.utils.html.tagHandlers.AnchorLinkTagHandler

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        anchorLinkDemo(binding.anchorLink)

        binding.openMap.setOnClickListener {
            AppBridge(this).Opener().showLocationOnMap(27.173891, 78.042068)
        }
    }

    private fun anchorLinkDemo(textView: AppCompatTextView) {
        val htmlText = """
            <a href="https://en.wikipedia.org/">Wikipedia</a>
            <br>
            <a href="https://www.github.com">GitHub</a>
            <br>
            <a href="https://www.stackoverflow.com">StackOverflow</a>
        """.trimIndent()

        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.text = htmlText.parseAsHtml(
            tagHandler = HtmlParser(
                AnchorLinkTagHandler(
                    underline = true,
                    color = Color.GREEN
                ) { link ->
                    Snackbar.make(binding.root, link, Snackbar.LENGTH_SHORT).show()
                }
            )
        )
    }
}