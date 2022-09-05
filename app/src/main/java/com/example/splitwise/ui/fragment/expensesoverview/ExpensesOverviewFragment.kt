package com.example.splitwise.ui.fragment.expensesoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentExpensesOverviewBinding
import com.example.splitwise.ui.fragment.adapter.ExpensesOverviewAdapter

class ExpensesOverviewFragment : Fragment() {

    private lateinit var binding: FragmentExpensesOverviewBinding
    private val args: ExpensesOverviewFragmentArgs by navArgs()

    private val viewModel: ExpensesOverviewViewModel by viewModels {
        ExpensesOverviewViewModelFactory(requireContext(), args.groupId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.expense_overview)
        return inflater.inflate(R.layout.fragment_expenses_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentExpensesOverviewBinding.bind(view)

        requireActivity().title = "Expenses Overview"

        // Rv
        val expensesAdapter = ExpensesOverviewAdapter()

        binding.expensesOverviewRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expensesAdapter
        }

        // Livedata Expenses overview
        viewModel.expenses.observe(viewLifecycleOwner) { expenses ->
            if (expenses != null) {
                expensesAdapter.updateExpenses(expenses)
            }
        }
    }
}