package com.ajou.xive.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KakaoSignUpViewModel : ViewModel(){
    private val _kakaoToken = MutableLiveData<String>()
    val kakaoToken : LiveData<String>
        get() = _kakaoToken

    fun setKakaoToken(string : String) = viewModelScope.launch(Dispatchers.IO) {
        _kakaoToken.postValue(string)
    }
}