package com.ajou.xive.home

import android.graphics.Bitmap
import android.net.http.SslError
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import com.ajou.xive.R
import com.ajou.xive.databinding.ActivityWebviewBinding

class WebviewActivity : AppCompatActivity() {
    private var _binding : ActivityWebviewBinding? = null
    private val binding get() = _binding!!

    override fun onBackPressed() {
        if (binding.webview.canGoBack()) {
            binding.webview.goBack()
        }else{
            super.onBackPressed()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityWebviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val url = intent.getStringExtra("url")
        WebView.setWebContentsDebuggingEnabled(true)

        binding.webview.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            javaScriptCanOpenWindowsAutomatically = false
            allowFileAccess = true

            setRenderPriority(WebSettings.RenderPriority.HIGH)
            cacheMode = WebSettings.LOAD_DEFAULT

            allowContentAccess = true
            loadsImagesAutomatically = true
            loadWithOverviewMode = true

            useWideViewPort = true

        }
        WebView.setWebContentsDebuggingEnabled(true)
        binding.webview.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        binding.webview.setNetworkAvailable(true)
//        binding.webview.webViewClient = WebViewClient()
        binding.webview.webViewClient = object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d("nfc data url check",url.toString())
            }
        }
        binding.webview.webChromeClient = WebChromeClient()

        binding.webview.loadUrl(url!!)
//        Log.d("nfc data url",url.toString())

//        var prevScrollY = 0
//        binding.webview.viewTreeObserver.addOnScrollChangedListener {
//            val scrollY = binding.webview.scrollY
//            Log.d("prevScrollY","$prevScrollY $scrollY ${binding.header.visibility}")
//            if (prevScrollY < scrollY && binding.header.visibility == View.VISIBLE) {
//                binding.header.visibility = View.GONE
//            } else if (prevScrollY - scrollY > 20 && binding.header.visibility == View.GONE) {
//                binding.header.visibility = View.VISIBLE
//            } else if (scrollY == 0) {
//                binding.header.visibility = View.VISIBLE
//            }
//            prevScrollY = scrollY
//        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }
}