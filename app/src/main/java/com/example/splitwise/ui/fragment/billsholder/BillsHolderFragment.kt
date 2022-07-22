package com.example.splitwise.ui.fragment.billsholder

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentBillsHolderBinding
import com.example.splitwise.ui.fragment.bill.BillFragment

class BillsHolderFragment : Fragment() {

    private lateinit var binding: FragmentBillsHolderBinding
    private val args: BillsHolderFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_bills_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBillsHolderBinding.bind(view)

        Log.d(TAG, "onViewCreated: viewpager position is ${args.position}")
        val viewPager = binding.pager
        viewPager.adapter = BillsAdapter(this)

        if(viewPager.currentItem == 0) {
            Log.d(TAG, "onViewCreated: set position to ${args.position}")
            viewPager.setCurrentItem(args.position, false)
        }

    }

    inner class BillsAdapter(fragment: Fragment): FragmentStateAdapter(fragment){
        override fun getItemCount(): Int{
            return args.bills.size
        }

        override fun createFragment(position: Int): Fragment {
            val fragment = BillFragment()
            fragment.arguments = Bundle().apply {
                putString("ARG_URI", args.bills[position])
            }

            return fragment
        }
    }
}

