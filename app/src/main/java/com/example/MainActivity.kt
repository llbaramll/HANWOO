package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.net.http.SslError
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.MyApplicationTheme
import java.net.URISyntaxException

class MainActivity : ComponentActivity() {

    // Value callbacks for Handling file-choosers (input type="file" inside web forms)
    private var fileUploadCallback: ValueCallback<Array<Uri>>? = null

    private val fileUploadLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (fileUploadCallback != null) {
            val results = if (result.resultCode == RESULT_OK) {
                val dataString = result.data?.dataString
                val clipData = result.data?.clipData
                when {
                    dataString != null -> arrayOf(Uri.parse(dataString))
                    clipData != null -> {
                        val list = mutableListOf<Uri>()
                        for (i in 0 until clipData.itemCount) {
                            list.add(clipData.getItemAt(i).uri)
                        }
                        list.toTypedArray()
                    }
                    else -> null
                }
            } else {
                null
            }
            fileUploadCallback?.onReceiveValue(results)
            fileUploadCallback = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Enable Edge-to-Edge to support modern, elegant view setups
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                // Outer scaffold takes the full screen
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // innerPadding automatically excludes the top (status bar) and bottom (navigation bar/gesture bar) areas.
                    // Placing our WebView inside this container guarantees it fits perfectly without covering the system bars!
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        WebViewContainer(
                            startUrl = "https://prod.limc.co.kr/m/login.jsp",
                            fileUploadLauncher = fileUploadLauncher,
                            onSetUploadCallback = { callback -> fileUploadCallback = callback }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WebViewContainer(
    startUrl: String,
    fileUploadLauncher: ActivityResultLauncher<Intent>,
    onSetUploadCallback: (ValueCallback<Array<Uri>>?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    var isLoading by remember { mutableStateOf(true) }
    var isError by remember { mutableStateOf(false) }
    var errorDescription by remember { mutableStateOf("") }

    // Intercept hardware Back Button inside WebView to support intuitive history backnavigation
    BackHandler(enabled = webViewRef?.canGoBack() == true) {
        webViewRef?.goBack()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // High-quality loading progress indicator at the top
            if (isLoading) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(3.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            if (isError) {
                // Friendly modern retry layout for offline/error state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "연결을 완료하지 못했습니다.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (errorDescription.isNotEmpty()) errorDescription else "인터넷 연결 상태를 확인하고 다시 시도해 주세요.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                isError = false
                                isLoading = true
                                progress = 0f
                                webViewRef?.reload()
                            }
                        ) {
                            Text(text = "다시 시도")
                        }
                    }
                }
            } else {
                // AndroidView host for standard WebView
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            webViewRef = this
                            layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.MATCH_PARENT
                            )

                            // Apply Security & Full Feature Settings
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                javaScriptCanOpenWindowsAutomatically = true
                                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                                
                                // Proper caching behaviors
                                cacheMode = WebSettings.LOAD_DEFAULT
                                allowFileAccess = true
                            }

                            // Cookie Management (Critical for session persistence, authentication, parameter passing, cookies)
                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            cookieManager.setAcceptThirdPartyCookies(this, true)

                            // Helper for custom links/loading within the local webview client
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    return handleUrlRedirect(ctx, url)
                                }

                                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isLoading = true
                                    isError = false
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    isLoading = false
                                    // Flush cookies to ensure immediate disk/memory sync persistence
                                    CookieManager.getInstance().flush()
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    request: WebResourceRequest?,
                                    error: WebResourceError?
                                ) {
                                    super.onReceivedError(view, request, error)
                                    // Only show error page if the failed loading is for the main frame
                                    if (request?.isForMainFrame == true) {
                                        isError = true
                                        errorDescription = error?.description?.toString() ?: ""
                                        isLoading = false
                                    }
                                }

                                override fun onReceivedSslError(
                                    view: WebView?,
                                    handler: SslErrorHandler?,
                                    error: SslError?
                                ) {
                                    // Standard SSL behavior
                                    super.onReceivedSslError(view, handler, error)
                                }
                            }

                            // Handling progress and File selection launches
                            webChromeClient = object : WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    super.onProgressChanged(view, newProgress)
                                    progress = newProgress / 100f
                                }

                                override fun onShowFileChooser(
                                    webView: WebView?,
                                    filePathCallback: ValueCallback<Array<Uri>>?,
                                    fileChooserParams: FileChooserParams?
                                ): Boolean {
                                    onSetUploadCallback(filePathCallback)
                                    val intent = fileChooserParams?.createIntent()
                                    return try {
                                        if (intent != null) {
                                            fileUploadLauncher.launch(intent)
                                            true
                                        } else {
                                            onSetUploadCallback(null)
                                            false
                                        }
                                    } catch (e: Exception) {
                                        onSetUploadCallback(null)
                                        false
                                    }
                                }
                            }

                            loadUrl(startUrl)
                        }
                    },
                    update = { view ->
                        webViewRef = view
                    }
                )
            }
        }
    }
}

/**
 * Handles custom Korean application scheme links (PASS, Kakao, Naver, Toss, etc.)
 * as well as direct app intents, markets, maps, emails, phone clicks, standard intents.
 */
private fun handleUrlRedirect(context: Context, url: String): Boolean {
    // 1. Let the WebView handle standard HTTP/HTTPS links matching our portal
    if (url.startsWith("http://") || url.startsWith("https://")) {
        return false // Let WebView load it
    }

    // 2. Parse special Intent URI schemes (commonly used for login/payment gateways in South Korea)
    if (url.startsWith("intent:")) {
        try {
            val intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            if (intent != null) {
                val package_name = intent.`package`
                if (package_name != null && context.packageManager.getLaunchIntentForPackage(package_name) == null) {
                    // Try to redirect to Market if package is specified but app is not installed
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$package_name")))
                        return true
                    } catch (e: Exception) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$package_name")))
                        return true
                    }
                }
                
                try {
                    context.startActivity(intent)
                    return true
                } catch (e: Exception) {
                    // Fallback to market query inside intent uri fallback link
                    val fallbackUrl = intent.getStringExtra("browser_fallback_url")
                    if (fallbackUrl != null) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl)))
                        return true
                    }
                }
            }
        } catch (e: URISyntaxException) {
            // Bad intent uri format
        }
        return true
    }

    // 3. General custom handling for other standard systems (tel:, mailto:, sms:, market:)
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
        return true
    } catch (e: Exception) {
        // App is incapable of handling the scheme
        return false
    }
}
