package com.ajou.xive.home.view

import android.app.PendingIntent
import android.content.Intent
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
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.request.RequestOptions
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class WebviewActivity : AppCompatActivity() {
    private var _binding : ActivityWebviewBinding? = null
    private val binding get() = _binding!!
    private val dataStore = UserDataStore()

    var nfcAdapter: NfcAdapter? = null
    val multiOptions = RequestOptions().transform(
        FitCenter(),
        BlurTransformation(10, 1)
    )

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

        CoroutineScope(Dispatchers.IO).launch {
            val accessToken = dataStore.getAccessToken().toString()
            val refreshToken = dataStore.getRefreshToken().toString()
            withContext(Dispatchers.Main) {
                binding.webview.evaluateJavascript("registerTicket($accessToken, $refreshToken)",null)
                binding.webview.webViewClient = WebViewClient()
                binding.webview.webChromeClient = WebChromeClient()

                binding.webview.loadUrl(url!!)
            }
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

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
        WebView.setWebContentsDebuggingEnabled(true)
        binding.webview.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        binding.webview.setNetworkAvailable(true)

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

//    private fun getDecryptionTicket(url: String) {
//        val splitUrl = "?nfc="
//        var originUrl = url.replace(splitUrl, "")
//        originUrl = originUrl.replace("https://", "")
//        CoroutineScope(Dispatchers.IO).launch {
//            val jsonObject = JsonObject().apply {
//                addProperty("url", originUrl)
//            }
//            val requestBody = RequestBody.create(
//                "application/json".toMediaTypeOrNull(),
//                jsonObject.toString()
//            )
//            val nfcDeferred = async { nfcService.postNFCTicket(requestBody) }
//            val nfcResponse = nfcDeferred.await()
//            if (nfcResponse.isSuccessful) {
//                val body = nfcResponse.body()
//                val data = NFCData(body!!.eventId.toInt(), body.nfcId.toInt(), body.seatNumber, url)
//                val postTicketDeferred =
//                    async { ticketService.postTicket(accessToken, refreshToken, data) }
//                val postTicketResponse = postTicketDeferred.await()
//                if (postTicketResponse.isSuccessful) {
//                    val body = postTicketResponse.body()!!
//                    if (body.isNew){
//                        val list = mutableListOf<Ticket>()
//                        viewModel.ticketList.value?.let { list.addAll(it) }
//                        list.add(body)
//                        withContext(Dispatchers.Main) {
//                            viewModel.setType("insert")
//                            viewModel.setTicketList(list)
//                        }
//                    }else{
//                        withContext(Dispatchers.Main){
//                            Toast.makeText(this@HomeActivity, "이미 등록된 티켓입니다", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                } else {
//                    Log.d(
//                        "postTicketResponse faill",
//                        postTicketResponse.errorBody()?.string().toString()
//                    )
//                }
//            } else {
//                Log.d("nfcResponse fail", nfcResponse.errorBody()?.string().toString())
//            }
//        }
//    }

//    override fun getSelectedTicketId(id: Int, position: Int) {
//        CoroutineScope(Dispatchers.IO).launch {
//            val deleteDeferred =
//                async { ticketService.deleteTicket(accessToken, refreshToken, id.toString()) }
//            val deleteResponse = deleteDeferred.await()
//            if (deleteResponse.isSuccessful) {
//                viewModel.setType(position.toString())
//                val list = viewModel.ticketList.value
//                list!!.removeAt(position)
//                viewModel.setTicketList(list)
//            } else {
//                Log.d("deleteResponse fail", deleteResponse.errorBody()?.string().toString())
//            }
//        }
//    }

//    override fun getSelectedTicketUrl(url: String) {
//        val intent = Intent(this, WebviewActivity::class.java)
//        intent.putExtra("url", url)
//        startActivity(intent)
//    }
}