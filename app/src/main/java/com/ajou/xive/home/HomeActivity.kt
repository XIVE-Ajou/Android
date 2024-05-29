package com.ajou.xive.home

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.os.*
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.ajou.xive.*
import com.ajou.xive.databinding.ActivityHomeBinding
import com.ajou.xive.home.model.Ticket
import com.ajou.xive.network.NFCRetrofitInstance
import com.ajou.xive.network.RetrofitInstance
import com.ajou.xive.network.api.NFCService
import com.ajou.xive.network.api.TicketService
import com.ajou.xive.network.model.NFCData
import com.ajou.xive.setting.SettingActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.JsonObject
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import java.util.*

class HomeActivity : AppCompatActivity(), DataSelection {
    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!
    private val dataStore = UserDataStore()
    private val viewModel: TicketViewModel by viewModels()
    private val ticketService = RetrofitInstance.getInstance().create(TicketService::class.java)
    private val nfcService = NFCRetrofitInstance.getInstance().create(NFCService::class.java)
    private lateinit var accessToken: String
    private lateinit var refreshToken: String
    var state = 0

    var nfcAdapter: NfcAdapter? = null
    val multiOptions = RequestOptions().transform(
        FitCenter(),
        BlurTransformation(10, 1)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val handler = Handler(Looper.myLooper()!!)
        val anim: Animation = AnimationUtils.loadAnimation(this, R.anim.nfc_btn_effect)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        val thread = object : Thread() {
            override fun run() {
                super.run()
                binding.nfcBtn.startAnimation(anim)
                handler.postDelayed(this, 1500) // 100 쉬고 동작 -> 100 사이에 화면 처리

            }
        }


        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC 사용이 불가능한 기종입니다", Toast.LENGTH_SHORT)
        }

        binding.nfcBtn.setOnClickListener {
            val dialog = NfcTaggingBottomSheetFragment()
            dialog.show(supportFragmentManager, dialog.tag)
        }

        binding.setting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
        val adapter = TicketViewPagerAdapter(this, emptyList(), this)

        viewModel.hasTicket.observe(this, androidx.lifecycle.Observer {
            if (viewModel.hasTicket.value == true) {
                binding.bgImg.visibility = View.VISIBLE
                binding.ticketVP.visibility = View.VISIBLE
                binding.indicator.visibility = View.VISIBLE
                state = 1
                animatedBtn(handler, thread, anim) // 애니메이션 멈추기
            }else{
                binding.nullLogo.visibility = View.VISIBLE
                binding.nullText1.visibility = View.VISIBLE
                binding.nullText3.visibility = View.VISIBLE
                state = 0
                animatedBtn(handler, thread, anim)
            }
        })

        viewModel.ticketList.observe(this, androidx.lifecycle.Observer {
            when (viewModel.type.value) {
                "insert" -> {
                    adapter.addToList(viewModel.ticketList.value!!)
                    binding.indicator.attachToPager(binding.ticketVP)
                    if (binding.indicator.visibility == View.VISIBLE) binding.indicator.invalidate()
                    if (viewModel.ticketList.value!!.size != 0) {
                        binding.indicator.visibility = View.VISIBLE
                        binding.ticketVP.visibility = View.VISIBLE
                        binding.nullLogo.visibility = View.GONE
                        binding.nullText1.visibility = View.GONE
                        binding.nullText3.visibility = View.GONE
                        Glide.with(this@HomeActivity)
                            .load(R.drawable.ticket_bg)
                            .apply(multiOptions)
                            .into(binding.bgImg)
                        binding.bgImg.visibility = View.VISIBLE
                    }
                }
                "update" -> {
                    adapter.updateList(viewModel.ticketList.value!!)
                }
                else -> {
                    adapter.removeAtList(viewModel.ticketList.value!!)
                    if (viewModel.ticketList.value!!.size == 0) {
                        binding.bgImg.setImageDrawable(null)
                        binding.indicator.visibility = View.INVISIBLE
                        binding.nullLogo.visibility = View.VISIBLE
                        binding.nullText1.visibility = View.VISIBLE
                        binding.nullText3.visibility = View.VISIBLE
                    }
                }
            }
        })

        viewModel.isError.observe(this, androidx.lifecycle.Observer {
            if (viewModel.isError.value == true){
                val intent = Intent(this, NetworkErrorActivity::class.java)
                startActivity(intent)
                viewModel.isError.value = false
            }
        })

        CoroutineScope(Dispatchers.IO).launch {
            accessToken = dataStore.getAccessToken().toString()
            refreshToken = dataStore.getRefreshToken().toString()
        }
        val text = "Add+\nSmart ticket"
        val spannable = SpannableStringBuilder(text)

        spannable.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(this, R.color.primary)),
            0,
            4,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // TextView에 Spannable 문자열 설정
        binding.nullText1.text = spannable

        binding.ticketVP.adapter = adapter
        binding.ticketVP.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        binding.indicator.attachToPager(binding.ticketVP)

        binding.calendar.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }
        binding.ticketVP.offscreenPageLimit = 4
        // item_view 간의 양 옆 여백을 상쇄할 값

        val offsetBetweenPages =
            resources.getDimensionPixelOffset(R.dimen.offsetBetweenPages).toFloat()

        binding.ticketVP.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.right = offsetBetweenPages.toInt()
                outRect.left = offsetBetweenPages.toInt()
            }
        })

        binding.ticketVP.setPageTransformer { page, position ->
            val myOffset = position * -(5 * offsetBetweenPages)
            if (position < -1) {
                page.translationX = -myOffset
            } else if (position <= 1) {
                // Paging 시 Y축 Animation 배경색을 약간 연하게 처리
                val scaleFactor = 0.8f.coerceAtLeast(1 - kotlin.math.abs(position))
                page.translationX = myOffset
                page.scaleY = scaleFactor
                page.alpha = scaleFactor
            } else {
                page.alpha = 0f
                page.translationX = myOffset
            }
        }

        binding.ticketVP.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (viewModel.ticketList.value!!.isNotEmpty()) {
                    Glide.with(this@HomeActivity)
                        .load(R.drawable.ticket_bg)
                        .apply(multiOptions)
                        .into(binding.bgImg)
                }
            }
        })
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
                val url = BuildConfig.BASE_NFC_URL + byteArrayToStringWithNDEF(record.payload)
//                getDecryptionTicket(url)
            } else if (Arrays.equals(record.type, NdefRecord.RTD_URI)) {
                val url = record.toUri().toString()
                getDecryptionTicket(url)
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

    private fun getDecryptionTicket(url: String) {
        val splitUrl = "?nfc="
        var originUrl = url.replace(splitUrl, "")
        originUrl = originUrl.replace("https://", "")
        CoroutineScope(Dispatchers.IO).launch {
            val jsonObject = JsonObject().apply {
                addProperty("url", originUrl)
            }
            val requestBody = RequestBody.create(
                "application/json".toMediaTypeOrNull(),
                jsonObject.toString()
            )
            val nfcDeferred = async { nfcService.postNFCTicket(requestBody) }
            val nfcResponse = nfcDeferred.await()
            if (nfcResponse.isSuccessful) {
                val body = nfcResponse.body()
                val data = NFCData(body!!.eventId.toInt(), body.nfcId.toInt(), body.seatNumber, url)
                val postTicketDeferred =
                    async { ticketService.postTicket(accessToken, refreshToken, data) }
                val postTicketResponse = postTicketDeferred.await()
                if (postTicketResponse.isSuccessful) {
                    val list = mutableListOf<Ticket>()
                    viewModel.ticketList.value?.let { list.addAll(it) }
                    val body = postTicketResponse.body()!!
                    list.add(body)
                    withContext(Dispatchers.Main) {
                        viewModel.setType("insert")
                        viewModel.setTicketList(list)
                    }
                } else {
                    Log.d(
                        "postTicketResponse faill",
                        postTicketResponse.errorBody()?.string().toString()
                    )
                }
            } else {
                Log.d("nfcResponse fail", nfcResponse.errorBody()?.string().toString())
            }
        }
    }

    override fun getSelectedTicketId(id: Int, position: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val deleteDeferred =
                async { ticketService.deleteTicket(accessToken, refreshToken, id.toString()) }
            val deleteResponse = deleteDeferred.await()
            if (deleteResponse.isSuccessful) {
                viewModel.setType(position.toString())
                val list = viewModel.ticketList.value
                list!!.removeAt(position)
                viewModel.setTicketList(list)
            } else {
                Log.d("deleteResponse fail", deleteResponse.errorBody()?.string().toString())
            }
        }
    }

    override fun getSelectedTicketUrl(url: String) {
        val intent = Intent(this, WebviewActivity::class.java)
        intent.putExtra("url", url)
        startActivity(intent)
    }

    private fun animatedBtn(handler: Handler, thread: Thread, anim: Animation) {
        binding.nfcBtn.startAnimation(anim)

        if (state == 1) {
            handler.removeCallbacks(thread)
        } else {
            handler.postDelayed(thread, 1500)
        }
    }
}