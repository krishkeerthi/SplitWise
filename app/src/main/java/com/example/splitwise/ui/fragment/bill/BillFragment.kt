package com.example.splitwise.ui.fragment.bill

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentBillBinding

class BillFragment : Fragment() {
    private lateinit var binding: FragmentBillBinding
    private var scaleFactor = 1f

    private lateinit var scaleGestureDetector: ScaleGestureDetector


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_bill, container, false)

        view.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            true
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentBillBinding.bind(view)

        arguments?.takeIf { it.containsKey("ARG_URI") }?.apply {
            val uri = Uri.parse(getString("ARG_URI"))
            Log.d(TAG, "onViewCreated: bill icon ${uri}")
            binding.billImageView.setImageURI(uri)
        }

        scaleGestureDetector = ScaleGestureDetector(
            requireContext(),
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scaleFactor *= scaleGestureDetector.scaleFactor
                    scaleFactor = 0.1f.coerceAtLeast(scaleFactor.coerceAtMost(10.0f))

                    binding.billImageView.scaleX = scaleFactor
                    binding.billImageView.scaleY = scaleFactor
                    return true
                }

            })
    }


}