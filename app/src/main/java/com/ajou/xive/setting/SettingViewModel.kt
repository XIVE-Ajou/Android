package com.ajou.xive.setting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingViewModel: ViewModel() {
    private var _count = MutableLiveData<Int>()
    val count : LiveData<Int>
        get() = _count

    private var _done = MutableLiveData<Boolean>()
    val done : LiveData<Boolean>
        get() = _done

    fun setCount(num: Int) {
        _count.postValue(num)
    }

    fun setDone(flag:Boolean) {
        _done.postValue(flag)
    }
}