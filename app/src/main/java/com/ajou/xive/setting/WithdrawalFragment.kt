package com.ajou.xive.setting

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ajou.xive.R
import com.ajou.xive.UserDataStore
import com.ajou.xive.auth.SignUpActivity
import com.ajou.xive.databinding.FragmentWithdrawalBinding
import com.ajou.xive.network.RetrofitInstance
import com.ajou.xive.network.api.UserService
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
class WithdrawalFragment : Fragment() {
    private var mContext : Context? = null
    private var _binding : FragmentWithdrawalBinding? = null
    private val binding get() = _binding!!
    private val userService = RetrofitInstance.getInstance().create(UserService::class.java)

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
        _binding = FragmentWithdrawalBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var reason : String ?= null
        var otherReason : String ?= null
        var accessToken : String? = null
        var refreshToken : String? = null
        val dataStore = UserDataStore()
        var nickname : String? = null
        CoroutineScope(Dispatchers.IO).launch {
            accessToken = dataStore.getAccessToken()
            refreshToken = dataStore.getRefreshToken()
            nickname = dataStore.getNickname()
            binding.withdrawalText.text = String.format(resources.getString(R.string.withdrawal),nickname,nickname)
        }
        binding.textView23.setOnClickListener {
            binding.agreeCheck.isChecked = !binding.agreeCheck.isChecked
//            binding.withdrawalBtn.isEnabled = binding.agreeCheck.isChecked
            if (binding.agreeCheck.isChecked){
                binding.withdrawalBtn.setTextColor(resources.getColor(R.color.white))
                binding.withdrawalLayout.visibility = View.VISIBLE
            } else{
                binding.withdrawalBtn.setTextColor(resources.getColor(R.color.gray100))
                binding.withdrawalLayout.visibility = View.INVISIBLE
            }
        }

        binding.option6.setOnClickListener {
            if(binding.option6.isChecked){
                binding.opinion.visibility = View.VISIBLE
            }else{
                binding.opinion.visibility = View.GONE
            }
        }
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.withdrawalBtn.setOnClickListener {
            val jsonObject = JsonObject().apply {
                addProperty("withdrawalOption", reason)
                addProperty("content", otherReason)
            }
            val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), jsonObject.toString())

            CoroutineScope(Dispatchers.IO).launch {
                val withdrawalDeferred = async { userService.withdrawal(accessToken, refreshToken,requestBody) }
                val withdrawalResponse = withdrawalDeferred.await()
                if (withdrawalResponse.isSuccessful) {
                    dataStore.deleteAll() // 유저 데이터 삭제
                    withContext(Dispatchers.Main){
                        val intent = Intent(mContext!!,SignUpActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    }
                } else{
                    Log.d("error withdrawl",withdrawalResponse.errorBody()?.string().toString())
                }
            }
        }
        binding.radioGroup.setOnCheckedChangeListener{ group, checkId ->
            binding.withdrawalBtn.isEnabled = true
            when(checkId){
                R.id.option1 -> reason = "OPTION1"
                R.id.option2 -> reason = "OPTION2"
                R.id.option3 -> reason = "OPTION3"
                R.id.option4 -> reason = "OPTION4"
                R.id.option5 -> reason = "OPTION5"
                R.id.option6 -> reason = "OTHER_OPTION"
            }
            if(reason != "OTHER_OPTION"){
                binding.opinion.text = null
                binding.opinion.visibility = View.GONE
            }else binding.opinion.visibility = View.VISIBLE
        }

        binding.opinion.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun afterTextChanged(str: Editable?) {
                otherReason = str.toString()
            }
        })
    }

}