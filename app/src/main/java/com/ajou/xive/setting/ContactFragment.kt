package com.ajou.xive.setting

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.FocusFinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ajou.xive.R
import com.ajou.xive.UserDataStore
import com.ajou.xive.databinding.FragmentContactBinding
import com.ajou.xive.home.TicketViewModel
import com.ajou.xive.network.RetrofitInstance
import com.ajou.xive.network.api.InquireService
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody

class ContactFragment : Fragment() {
    private var _binding : FragmentContactBinding? = null
    private val binding get() = _binding!!
    private var mContext : Context? = null
    private val inquireService = RetrofitInstance.getInstance().create(InquireService::class.java)
    private lateinit var viewModel : SettingViewModel
    private val dataStore = UserDataStore()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentContactBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[SettingViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var match = false

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btn.setOnClickListener {
            postInquireApi()
        }

        binding.contents.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(str: Editable?) {
                binding.count.text = String.format(resources.getString(R.string.word_count),binding.contents.text.length)
                if(str!!.isNotEmpty()){
                    viewModel.setDone(true)
                }else{
                    viewModel.setDone(false)
                }
            }
        })

        binding.email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(str: Editable?) {
                if(str!!.isNotEmpty()){
                    viewModel.setDone(true)
                    match = android.util.Patterns.EMAIL_ADDRESS.matcher(str.toString()).matches()
                }else{
                    viewModel.setDone(false)
                }
            }
        })

        binding.contents.onFocusChangeListener =
            OnFocusChangeListener { p0, p1 -> binding.contentLine.isSelected = p1 }

        binding.email.onFocusChangeListener =
            OnFocusChangeListener { view, b -> binding.emailLine.isSelected = b }

        binding.contents.setOnEditorActionListener { view, id, keyEvent ->
            if (id == EditorInfo.IME_ACTION_DONE){
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(requireActivity().window.decorView.applicationWindowToken, 0)

                if (binding.btn.isEnabled) postInquireApi()

                return@setOnEditorActionListener true
            }else return@setOnEditorActionListener false
        }

        viewModel.done.observe(viewLifecycleOwner, Observer {
            binding.btn.isEnabled = binding.email.text.isNotEmpty() && binding.contents.text.isNotEmpty() && match
            if (binding.btn.isEnabled){
                binding.btn.setTextColor(resources.getColor(R.color.white))
            }else{
                binding.btn.setTextColor(resources.getColor(R.color.gray100))
            }
        })
    }

    private fun postInquireApi(){
        CoroutineScope(Dispatchers.IO).launch {
            val accessToken = dataStore.getAccessToken()
            val refreshToken = dataStore.getRefreshToken()
            val jsonObject = JsonObject().apply {
                addProperty("email", binding.email.text.toString())
                addProperty("contents",binding.contents.text.toString())
            }
            val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())
            val inquireDeferred = async { inquireService.postInquire(accessToken!!, refreshToken!!,requestBody) }
            val inquireResponse = inquireDeferred.await()
            if (inquireResponse.isSuccessful){
                withContext(Dispatchers.Main){
                    findNavController().popBackStack()
                }
            }else{

            }
        }
    }
}