package com.example.splitwise.ui.fragment.expensedetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentExpenseDetailBinding
import com.example.splitwise.ui.fragment.adapter.BillsAdapter
import com.example.splitwise.ui.fragment.adapter.MembersAdapter
import com.example.splitwise.ui.fragment.adapter.MembersCheckboxAdapter
import com.example.splitwise.util.Category

class ExpenseDetailFragment : Fragment() {

    private lateinit var binding: FragmentExpenseDetailBinding
    private val args: ExpenseDetailFragmentArgs by navArgs()

    private val viewModel: ExpenseDetailViewModel by viewModels {
        ExpenseDetailViewModelFactory(requireContext(), args.expenseId)
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

        // Livedata payees
        viewModel.payees.observe(viewLifecycleOwner){ members ->
            if(members != null){
                membersAdapter.updateMembers(members)
            }
        }

        // bills
        viewModel.bills.observe(viewLifecycleOwner){ bills ->
            if(bills != null){
                billsAdapter.updateBills(bills)
            }
        }

        // Expense
        viewModel.expense.observe(viewLifecycleOwner){ expense ->
            expense?.let {
                binding.expenseNameTextView.text = it.expenseName
                binding.totalAmountTextView.text = it.totalAmount.toString()
                binding.splitAmountTextView.text = it.splitAmount.toString()
                binding.expenseCategoryTextView.text = Category.values()[it.category].name
            }
        }

        // Payer
        viewModel.payer.observe(viewLifecycleOwner){ payer ->
            payer?.let {
                binding.expensePayerTextView.text = it.name
            }

        }
    }
}