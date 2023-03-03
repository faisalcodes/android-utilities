package com.faisalcodes.android_utilities.utils.bridge

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

class AppBridge(private val context: Context) {
    fun resolvePackage(destination: Int): String {
        return when (destination) {
            AppBridgeDestination.WHATSAPP -> "com.whatsapp"
            AppBridgeDestination.FACEBOOK -> "com.facebook.katana"
            AppBridgeDestination.TWITTER -> "com.twitter.android"
            AppBridgeDestination.INSTAGRAM -> "com.instagram.android"
            AppBridgeDestination.GITHUB -> "com.github.android"
            else -> ""
        }
    }

    fun checkPackage(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: Exception) {
            false
        }
    }

    inner class Opener {
        private fun tryOpen(intent: Intent) {
            try {
                context.startActivity(intent)
            } catch (ignored: ActivityNotFoundException) {
                Toast.makeText(context, "Failed to open.", Toast.LENGTH_SHORT).show()
            }
        }

        fun openFacebookPage(profileId: String, fallbackUsername: String) {
            val intent = Intent(Intent.ACTION_VIEW)

            val packageName = resolvePackage(AppBridgeDestination.FACEBOOK)
            val uriStr = if (checkPackage(packageName)) {
                intent.setPackage(packageName)
                "fb://facewebmodal/f?href=https://www.facebook.com/$profileId"
            } else {
                "https://www.facebook.com/$fallbackUsername"
            }

            intent.data = Uri.parse(uriStr)

            tryOpen(intent)
        }

        fun openTwitterProfile(username: String) {
            val intent = Intent(Intent.ACTION_VIEW)

            val packageName = resolvePackage(AppBridgeDestination.TWITTER)
            val uriStr = if (checkPackage(packageName)) {
                intent.setPackage(packageName)
                "twitter://user?screen_name=$username"
            } else {
                "https://twitter.com/$username"
            }

            intent.data = Uri.parse(uriStr)

            tryOpen(intent)
        }

        fun openInstagramProfile(username: String) {
            val intent = Intent(Intent.ACTION_VIEW)

            val packageName = resolvePackage(AppBridgeDestination.INSTAGRAM)
            val uriStr = if (checkPackage(packageName)) {
                intent.setPackage(packageName)
                "http://instagram.com/_u/$username"
            } else {
                "http://instagram.com/$username"
            }

            intent.data = Uri.parse(uriStr)

            tryOpen(intent)
        }

        fun openGithubProfile(username: String) {
            val intent = Intent(Intent.ACTION_VIEW)

            val packageName = resolvePackage(AppBridgeDestination.GITHUB)
            val uriStr = if (checkPackage(packageName)) {
                intent.setPackage(packageName)
                "github://user?username=$username"
            } else {
                "https://www.github.com/$username"
            }

            intent.data = Uri.parse(uriStr)

            tryOpen(intent)
        }

        fun openAppPlayStorePage() {
            openPlayStorePage(context.packageName)
        }

        fun openPlayStorePage(packageName: String) {
            val intent = Intent(Intent.ACTION_VIEW)

            try {
                intent.data = Uri.parse("market://details?id=$packageName")
                context.startActivity(intent)
            } catch (ignored: ActivityNotFoundException) {
                openWebPage("https://play.google.com/store/apps/details?id=$packageName")
            }
        }

        fun openWebPage(url: String) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            tryOpen(intent)
        }

        fun showLocationOnMap(latitude: Double, longitude: Double, zoom: Int = -1, label: String? = null) {
            val geoLocation = if (label != null) "geo:0,0?q=$latitude,$longitude($label)"
            else "geo:$latitude,$longitude"

            if (label == null && zoom > 0) geoLocation.plus("?z=$zoom")
            else if (label != null && zoom > 0) geoLocation.plus("&z=$zoom")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(geoLocation)
            }

            tryOpen(intent)
        }

        fun showAddressOnMap(address: String) {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("geo:0,0?q=$address")
            }

            tryOpen(intent)
        }

        fun dialPhoneNumber(phoneNumber: String) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phoneNumber")
            }

            tryOpen(intent)
        }

        fun openSettingsSection(section: String) {
            tryOpen(Intent(section))
        }

        fun openAppSettings() {
            openSettingsSection(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        }

        fun openSettings() {
            openSettingsSection(Settings.ACTION_SETTINGS)
        }

        fun openWifiSettings() {
            openSettingsSection(Settings.ACTION_WIFI_SETTINGS)
        }

        fun openBluetoothSettings() {
            openSettingsSection(Settings.ACTION_BLUETOOTH_SETTINGS)
        }

        fun openLocationSettings() {
            openSettingsSection(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        }

        fun openDisplaySettings() {
            openSettingsSection(Settings.ACTION_DISPLAY_SETTINGS)
        }

        fun openInputMethodSettings() {
            openSettingsSection(Settings.ACTION_INPUT_METHOD_SETTINGS)
        }
    }

    inner class Sharer(private val chooserTitle: String = "Share via...") {
        private val intent = Intent(Intent.ACTION_SEND)
        var text: String? = null
        var image: Uri? = null
        var mimeType: String? = null
        var destination: Int = AppBridgeDestination.SYSTEM_SHARE

        fun share() {
            prepare()
            tryShare()
        }

        fun prepare(): Intent {
            if (text != null) {
                intent.putExtra(Intent.EXTRA_TEXT, text)
            }

            if (image != null) {
                intent.putExtra(Intent.EXTRA_STREAM, image)
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            intent.setTypeAndNormalize(mimeType ?: "text/plain")

            if (destination != AppBridgeDestination.SYSTEM_SHARE) {
                intent.setPackage(resolvePackage(destination))
            }

            return Intent.createChooser(intent, chooserTitle)
        }

        private fun tryShare() {
            try {
                context.startActivity(Intent.createChooser(intent, chooserTitle))
            } catch (ignored: ActivityNotFoundException) {
                Toast.makeText(context, "Failed to share.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    inner class Email {
        private val intent = Intent(Intent.ACTION_SEND_MULTIPLE)
        var subject: String? = null
        var body: String? = null
        var recipients = arrayOf<String>()
        var cc = arrayOf<String>()
        var bcc = arrayOf<String>()
        var attachments = arrayOf<Uri>()

        fun send() {
            prepare()
            tryOpen()
        }

        fun prepare(): Intent {
            intent.type = "*/*"
            intent.putExtra(Intent.EXTRA_SUBJECT, subject)
            intent.putExtra(Intent.EXTRA_TEXT, body)
            intent.putExtra(Intent.EXTRA_EMAIL, recipients)
            intent.putExtra(Intent.EXTRA_CC, cc)
            intent.putExtra(Intent.EXTRA_BCC, bcc)
            intent.putExtra(Intent.EXTRA_STREAM, attachments)

            return Intent.createChooser(intent, "Send Email")
        }

        fun tryOpen() {
            try {
                context.startActivity(intent)
            } catch (ignored: ActivityNotFoundException) {
                Toast.makeText(context, "Failed to open.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}