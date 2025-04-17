package com.example.refleq

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.refleq.ui.theme.RefleQTheme

class MainActivity : ComponentActivity() {

    private var mUploadMessage: android.webkit.ValueCallback<Array<Uri>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RefleQTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WebViewScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }

    // Override onActivityResult to get the result from file chooser
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {
            if (data != null && data.data != null) {
                val result = data.data!!
                mUploadMessage?.onReceiveValue(arrayOf(result))
            } else {
                mUploadMessage?.onReceiveValue(null)
            }
            mUploadMessage = null
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @Composable
    fun WebViewScreen(modifier: Modifier = Modifier) {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    webChromeClient = object : WebChromeClient() {
                        // Handle file chooser for uploading images
                        override fun onShowFileChooser(
                            webView: WebView?,
                            filePathCallback: android.webkit.ValueCallback<Array<Uri>>?,
                            fileChooserParams: FileChooserParams?
                        ): Boolean {
                            mUploadMessage = filePathCallback
                            val intent = fileChooserParams?.createIntent()

                            // Safely unwrap the intent
                            intent?.let {
                                try {
                                    startActivityForResult(it, FILE_CHOOSER_REQUEST_CODE)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error opening file chooser", Toast.LENGTH_SHORT).show()
                                }
                            } ?: run {
                                Toast.makeText(context, "File chooser intent is null", Toast.LENGTH_SHORT).show()
                            }
                            return true
                        }
                    }

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                    // ✅ Added for image & file access support
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true

                    // Force mobile view
                    settings.userAgentString =
                        "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Mobile Safari/537.36"

                    CookieManager.getInstance().setAcceptCookie(true)
                    CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                    loadUrl("http://refleqtions.ct.ws")
                }
            }
        )
    }
}

// Constant for file chooser request code
private const val FILE_CHOOSER_REQUEST_CODE = 1
