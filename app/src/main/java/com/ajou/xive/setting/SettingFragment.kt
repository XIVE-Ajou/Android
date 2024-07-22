package com.ajou.xive.setting

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ajou.xive.BuildConfig
import com.ajou.xive.R
import com.ajou.xive.UserDataStore
import com.ajou.xive.databinding.FragmentSettingBinding
import com.ajou.xive.home.view.HomeActivity
import kotlinx.coroutines.*

class SettingFragment : Fragment() {
    private var _binding : FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private var mContext : Context? = null
    private lateinit var dialog: SignOutDialog

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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var accessToken: String? = null
        var refreshToken: String? = null
        val dataStore = UserDataStore()
        var nickname: String? = null
        CoroutineScope(Dispatchers.IO).launch {
            accessToken = dataStore.getAccessToken()
            refreshToken = dataStore.getRefreshToken()
            nickname = dataStore.getNickname()
        }

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
}