package com.example.splitwise.ui.fragment.billsholder

import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.navArgs
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.MarginPageTransformer
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentBillsHolderBinding
import com.example.splitwise.ui.fragment.bill.BillFragment
import com.example.splitwise.util.dpToPx
import com.google.android.material.transition.MaterialContainerTransform

class BillsHolderFragment : Fragment() {

    private lateinit var binding: FragmentBillsHolderBinding
    private val args: BillsHolderFragmentArgs by navArgs()

    private val viewModel: BillsHolderViewModel by viewModels {
        BillsHolderViewModelFactory(requireContext(), args.expenseId, args.position)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment_container
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor = resources.getColor(R.color.view_color)//Color.TRANSPARENT
            setAllContainerColors(resources.getColor(R.color.background))
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.bills)
        return inflater.inflate(R.layout.fragment_bills_holder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentBillsHolderBinding.bind(view)

        //Log.d(TAG, "onViewCreated: viewpager position is ${args.position}")
        val viewPager = binding.pager

        viewModel.bills.observe(viewLifecycleOwner){ bills ->
            if(bills != null && bills.isNotEmpty()){
                val bills = viewModel.getBills()
                viewPager.adapter = BillsAdapter(this, bills)

                if (viewPager.currentItem == 0) {
                    //Log.d(TAG, "onViewCreated: set position to ${args.position}")
                    viewPager.setCurrentItem(viewModel.position, false)
                }
                binding.emptyBillsTextView.visibility = View.GONE
                binding.pager.visibility = View.VISIBLE
            }
            else{
                binding.emptyBillsTextView.visibility = View.VISIBLE
                binding.pager.visibility = View.GONE
            }

        }

        //viewPager.adapter = BillsAdapter(this)

        val margin = 16.dpToPx(resources.displayMetrics)
        viewPager.setPageTransformer(MarginPageTransformer(margin))
        //viewPager.setPadding(0, 0, 10, 0)

//        if (viewPager.currentItem == 0) {
//            Log.d(TAG, "onViewCreated: set position to ${args.position}")
//            viewPager.setCurrentItem(args.position, false)
//        }


        // Menu
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.bills_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.delete_bill ->{
                //Toast.makeText(requireContext(), "${binding.pager.currentItem}", Toast.LENGTH_SHORT).show()
                deleteDialog()

                true
            }
            else ->{
                false
            }
        }
    }

    private fun deleteDialog(){
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.delete);
        builder.setMessage(R.string.delete_message)
        builder.setPositiveButton(R.string.delete) { dialog, which ->
            viewModel.deleteBill(binding.pager.currentItem)
        }
        builder.setNegativeButton(R.string.cancel, null)
        builder.show()
    }

    inner class BillsAdapter(fragment: Fragment, private val bills: List<String>) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int {
            return bills.size //args.bills.size
        }

        override fun createFragment(position: Int): Fragment {
            val fragment = BillFragment()
            fragment.arguments = Bundle().apply {
                putString("ARG_URI", bills[position])
                //putString("ARG_URI", args.bills[position])
            }

            return fragment
        }
    }
}

