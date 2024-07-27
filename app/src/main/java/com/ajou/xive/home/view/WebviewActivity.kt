package com.ajou.xive.home.view

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import com.ajou.xive.UserDataStore
import com.ajou.xive.databinding.ActivityWebviewBinding
import com.ajou.xive.home.TicketViewModel
import com.ajou.xive.network.RetrofitInstance
import com.ajou.xive.network.api.EventService
import com.ajou.xive.network.api.StampService
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.*
import org.json.JSONObject
import retrofit2.create
import java.util.*

class WebviewActivity : AppCompatActivity() {
    private var _binding : ActivityWebviewBinding? = null
    private val binding get() = _binding!!
    private val dataStore = UserDataStore()
    private val stampService = RetrofitInstance.getInstance().create(StampService::class.java)
    private val eventService = RetrofitInstance.getInstance().create(EventService::class.java)

    var nfcAdapter: NfcAdapter? = null

//    val multiOptions = RequestOptions().transform(
//        FitCenter(),
//        BlurTransformation(10, 1)
//    )

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

//        val url = intent.getStringExtra("url")
        val url = "https://xive.co.kr/xive-test"
        WebView.setWebContentsDebuggingEnabled(true)

        CoroutineScope(Dispatchers.IO).launch {
            val accessToken = dataStore.getAccessToken().toString()
            val refreshToken = dataStore.getRefreshToken().toString()
            Log.d("token check",accessToken.toString())
            val stampImgDeferred = async { stampService.getInitStamp(accessToken, refreshToken, "6") }
            val stampImgResponse = stampImgDeferred.await()
            var stampImgJsonData : String = ""
            var eventStampJsonData : String = ""
            val eventStampsDeferred = async { stampService.getEventStamps(accessToken, refreshToken,"6") }
            val eventStampResponse = eventStampsDeferred.await()

            if (stampImgResponse.isSuccessful) {
                stampImgJsonData = stampImgResponse.body()!!.string().replace("'", "\\'")
                Log.d("stampImgJsonData",stampImgJsonData)
            }
            if (eventStampResponse.isSuccessful) {
                eventStampJsonData = eventStampResponse.body()!!.string().replace("'", "\\'")
                Log.d("eventStampJsonData",eventStampJsonData)
            }

            withContext(Dispatchers.Main) {
                binding.webview.clearCache(true)
                binding.webview.clearHistory()
                binding.webview.clearFormData()

                binding.webview.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    javaScriptCanOpenWindowsAutomatically = false

                    databaseEnabled = true

                    cacheMode = WebSettings.LOAD_NO_CACHE

                    allowContentAccess = true
                    loadsImagesAutomatically = true
                    loadWithOverviewMode = true

                    useWideViewPort = true
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    binding.webview.settings.databasePath = "/data/data/" + binding.webview.context.packageName + "/databases/";
                }

                binding.webview.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        binding.webview.evaluateJavascript("javascript:testFunc('$accessToken','$refreshToken', ')",null)
//                        binding.webview.evaluateJavascript("javascript:stampInit('$stampImgJsonData')",null)
//                        binding.webview.evaluateJavascript("javascript:setStamp('$eventStampJsonData')",null)
                    }
                }
                binding.webview.webChromeClient = WebChromeClient()

                binding.webview.loadUrl(url!!)
            }
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)


        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            val messages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

            for (i in messages!!.indices) getNdefMsg(messages[i] as NdefMessage)
        }
    }

    override fun onPause() {
        super.onPause()
        if (nfcAdapter != null) nfcAdapter?.disableForegroundDispatch(this)
    }

    private fun getNdefMsg(mMessage: NdefMessage) {
        val recs = mMessage.records
        for (i in recs.indices) {
            val record = recs[i]
            if (Arrays.equals(record.type, NdefRecord.RTD_TEXT)) {
                val text = byteArrayToStringWithNDEF(record.payload)
//                getDecryptionTicket(url)
            } else if (Arrays.equals(record.type, NdefRecord.RTD_URI)) {
                val url = record.toUri().toString()
//                getDecryptionTicket(url)
            }
        }
    }

    fun byteArrayToStringWithNDEF(byteArray: ByteArray): String {
        if (byteArray.isEmpty()) {
            return ""
        }

        // 첫 번째 바이트는 상태 바이트
        val statusByte = byteArray[0].toInt()

        // 상태 바이트의 하위 5비트는 언어 코드의 길이를 나타냄
        val languageCodeLength = statusByte and 0x3F

        // 실제 텍스트 데이터는 언어 코드 다음에 위치
        return String(
            byteArray,
            languageCodeLength + 1,
            byteArray.size - languageCodeLength - 1,
            Charsets.UTF_8
        )
    }
}