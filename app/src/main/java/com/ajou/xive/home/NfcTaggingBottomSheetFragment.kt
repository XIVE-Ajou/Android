package com.ajou.xive.home

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ajou.xive.R
import com.ajou.xive.databinding.FragmentNfcTaggingBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NfcTaggingBottomSheetFragment : BottomSheetDialogFragment() {
    private var mContext: Context? = null
    private var _binding: FragmentNfcTaggingBottomSheetBinding? = null
    private val binding get() = _binding!!

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
        _binding = FragmentNfcTaggingBottomSheetBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(mContext!!, R.style.CustomDialog)
        dialog.setOnShowListener {
            val bottomSheetDialog = it as BottomSheetDialog
            setupRatio(bottomSheetDialog)
            dialog.setCanceledOnTouchOutside(true)
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gifDrawable = pl.droidsonroids.gif.GifDrawable(mContext!!.resources, R.drawable.nfc_guide_animation)
        binding.gif.setImageDrawable(gifDrawable)

        binding.closeBtn.setOnClickListener {
            dismiss()
        }

    }

    private fun setupRatio(bottomSheetDialog: BottomSheetDialog) {
        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as View
        val behavior = BottomSheetBehavior.from<View>(bottomSheet)
        val layoutParams = bottomSheet!!.layoutParams
        layoutParams.height = getBottomSheetDialogDefaultHeight()
        bottomSheet.layoutParams = layoutParams
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun getBottomSheetDialogDefaultHeight(): Int {
        return getWindowHeight() * 36 / 100
    }

    private fun getWindowHeight(): Int {
        // Calculate window height for fullscreen use
        val displayMetrics = DisplayMetrics()
        (context as Activity?)!!.windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    override fun onPause() {
        super.onPause()
        dialog?.dismiss()
    }
    override fun onDestroy() {
        super.onDestroy()
//        if(::dialog.isInitialized) dialog?.dismiss()
    }
}