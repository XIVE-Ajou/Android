package com.ajou.xive.setting

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.widget.Button
import androidx.annotation.NonNull
import com.ajou.xive.R
import com.ajou.xive.UserDataStore
import com.ajou.xive.auth.SignUpActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignOutDialog(@NonNull context : Context) : Dialog(context) {
    init {
        setContentView(R.layout.dialog_signout)

        val stayBtn = findViewById<Button>(R.id.stayBtn)
        val signOutBtn = findViewById<Button>(R.id.exitBtn)

        signOutBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                UserDataStore().deleteAll() // 유저 데이터 삭제
            }
            val intent = Intent(context, SignUpActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            dismiss()
        }

        stayBtn.setOnClickListener {
            dismiss()
        }

    }
}