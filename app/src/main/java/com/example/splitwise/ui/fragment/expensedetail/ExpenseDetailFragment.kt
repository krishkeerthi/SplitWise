package com.example.splitwise.ui.fragment.expensedetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentExpenseDetailBinding
import com.example.splitwise.ui.fragment.adapter.BillsAdapter
import com.example.splitwise.ui.fragment.adapter.MembersAdapter
import com.example.splitwise.ui.fragment.adapter.MembersCheckboxAdapter

class ExpenseDetailFragment : Fragment() {

    private lateinit var binding: FragmentExpenseDetailBinding

    private val viewModel: ExpenseDetailViewModel by viewModels {
        ExpenseDetailViewModelFactory(requireContext(), 3)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_expense_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentExpenseDetailBinding.bind(view)

        // Rv
        val membersAdapter = MembersAdapter()
        val billsAdapter = BillsAdapter()

        binding.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
        }

        binding.billsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = billsAdapter
        }

        // Livedata
        viewModel.payees.observe(viewLifecycleOwner){ members ->
            if(members != null){
                membersAdapter.updateMembers(members)
            }
        }

        viewModel.bills.observe(viewLifecycleOwner){ bills ->
            if(bills != null){
                billsAdapter.updateBills(bills)
            }
        }
    }
}