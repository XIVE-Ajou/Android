package com.ajou.xive.setting

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.ajou.xive.IndentLeadingMarginSpan
import com.ajou.xive.R
import com.ajou.xive.databinding.FragmentTermsAndConditionBinding

class TermsAndConditionFragment : Fragment() {
    private var _binding : FragmentTermsAndConditionBinding? = null
    private val binding get() = _binding!!
    private var mContext : Context? = null

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
        _binding = FragmentTermsAndConditionBinding.inflate(layoutInflater, container, false)
        val originalText2 = getString(R.string.tc_2)
        binding.text2.text = SpannableStringBuilder(originalText2).apply {
            setSpan(IndentLeadingMarginSpan(), 0, length, 0)
        }
        val originalText5 = getString(R.string.tc_5)
        binding.text5.text = SpannableStringBuilder(originalText5).apply {
            setSpan(IndentLeadingMarginSpan(), 0, length, 0)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}