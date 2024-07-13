package com.ajou.xive.setting

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ajou.xive.BuildConfig
import com.ajou.xive.R
import com.ajou.xive.UserDataStore
import com.ajou.xive.databinding.FragmentSettingBinding
import com.ajou.xive.home.view.HomeActivity
import com.ajou.xive.network.RetrofitInstance
import com.ajou.xive.network.api.UserService
import com.google.gson.JsonObject
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject

class SettingFragment : Fragment() {
    private var _binding : FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private var mContext : Context? = null
    private val dataStore = UserDataStore()
    private lateinit var dialog: SignOutDialog
    private lateinit var viewModel : KakaoSignUpViewModel
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
        _binding = FragmentSettingBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[KakaoSignUpViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var accessToken: String? = null
        var refreshToken: String? = null
        var loginType: String? = null
        val dataStore = UserDataStore()
        var nickname: String? = null
        CoroutineScope(Dispatchers.IO).launch {
            accessToken = dataStore.getAccessToken()
            refreshToken = dataStore.getRefreshToken()
            loginType = dataStore.getLoginType()
            nickname = dataStore.getNickname()
            withContext(Dispatchers.Main){
                if(loginType == "NON_LOGIN") {
                    binding.accountIcon.setImageResource(R.drawable.setting_non_small)
                }else{
                    binding.switchBtn.isSelected = true
                }
                binding.name.text = nickname
            }
        }

        binding.switchBtn.setOnClickListener {
            if(!binding.switchBtn.isSelected) {
                // 비회원상태
                binding.switchBtn.isSelected = true
                kakaoLogin()
                binding.switchBtn.isEnabled = false
            }
        }

        viewModel.kakaoToken.observe(viewLifecycleOwner, Observer {
            if (viewModel.kakaoToken.value!!.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
                    val jsonObject = JsonObject().apply {
                        addProperty("accessToken", viewModel.kakaoToken.value.toString())
                    }
                    val requestBody = RequestBody.create(
                        "application/json".toMediaTypeOrNull(),
                        jsonObject.toString()
                    )
                    val loginDeferred = async { userService.login(requestBody) }
                    val loginResponse = loginDeferred.await()
                    if (loginResponse.isSuccessful){
                        val tokenBody = loginResponse.body()
                        dataStore.saveAccessToken(tokenBody!!.accessToken)
                        dataStore.saveRefreshToken(tokenBody.refreshToken)
                        val getMemberInfoDeferred = async { userService.getMemberInfo(tokenBody.accessToken, tokenBody.refreshToken) }
                        val getMemberInfoResponse = getMemberInfoDeferred.await()
                        if (getMemberInfoResponse.isSuccessful) {
                            val memberInfoBody = JSONObject(getMemberInfoResponse.body()?.string())
                            dataStore.saveLoginType(memberInfoBody.getString("loginType"))
                            dataStore.saveNickname(memberInfoBody.getString("nickname"))
                            withContext(Dispatchers.Main){
                                binding.name.text = memberInfoBody.getString("nickname")
                                binding.switchBtn.isSelected = true
                                binding.accountIcon.setImageResource(R.drawable.setting_kakao_small)
                            }
                        }
                    }else{
                        Log.d("kakao login fail",loginResponse.errorBody()?.string().toString())
                    }
                }

            }
        })

        val versionName = "Ver "+BuildConfig.VERSION_NAME
        binding.version.text = versionName

        binding.backBtn.setOnClickListener {
            val intent = Intent(mContext, HomeActivity::class.java)
            startActivity(intent)
        }

        binding.textAsk.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_contactFragment)
        }
        binding.tac.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_termsAndConditionFragment)
        }
        binding.privacyPolicy.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_privacyPolicyFragment)
        }

        binding.logout.setOnClickListener {
            // TODO 로그아웃 dialog show
            dialog = SignOutDialog(mContext!!)
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.show()
        }

        binding.withdrawal.setOnClickListener {
            findNavController().navigate(R.id.action_settingFragment_to_withdrawalFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(::dialog.isInitialized) dialog.dismiss()
    }

    private fun kakaoLogin(){
        // 카카오톡 설치 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(mContext!!)) {
            UserApiClient.instance.loginWithKakaoTalk(mContext!!) { token, error ->
                // 로그인 실패 부분
                if (error != null) {
                    Log.e(ContentValues.TAG, "로그인 실패 $error")
                    // 사용자가 취소
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        return@loginWithKakaoTalk
                    }
                    // 다른 오류
                    else {
                        UserApiClient.instance.loginWithKakaoAccount(
                            mContext!!,
                            callback = mCallback
                        ) // 카카오 이메일 로그인
                    }
                }
                // 로그인 성공 부분
                else if (token != null) {
                    UserApiClient.instance.me { user, error ->
                        CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
                            dataStore.saveNickname(user?.kakaoAccount?.profile?.nickname.toString())
                            viewModel.setKakaoToken(token.accessToken)
                        }
                    }
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(
                mContext!!,
                callback = mCallback
            ) // 카카오 이메일 로그인
        }
    }

    private val mCallback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Log.e(ContentValues.TAG, "카카오계정으로 로그인 실패", error)
        } else if (token != null) {
            CoroutineScope(Dispatchers.IO).launch(exceptionHandler) {
                viewModel.setKakaoToken(token.accessToken)
            }
            Log.i(ContentValues.TAG, "카카오계정으로 로그인 성공 ${token.accessToken}")
        }
    }
    val exceptionHandler = CoroutineExceptionHandler { _, exception ->
        showErrorDialog()
    }
    private fun showErrorDialog() {
        requireActivity().runOnUiThread {
            AlertDialog.Builder(mContext!!).apply {
                setTitle("Error")
                setMessage("Network request failed.")
                setPositiveButton("종료") { dialog, _ ->
                    dialog.dismiss()
                    requireActivity().finish()
                }
                setCancelable(false)
                show()
            }
        }
    }
}