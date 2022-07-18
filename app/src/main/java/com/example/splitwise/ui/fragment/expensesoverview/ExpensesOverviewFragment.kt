package com.example.splitwise.ui.fragment.expensesoverview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentExpensesOverviewBinding
import com.example.splitwise.ui.fragment.adapter.ExpensesOverviewAdapter
import com.example.splitwise.ui.fragment.adapter.GroupsAdapter

class ExpensesOverviewFragment : Fragment() {

    private lateinit var binding: FragmentExpensesOverviewBinding

    private val viewModel: ExpensesOverviewViewModel by viewModels {
        ExpensesOverviewViewModelFactory(requireContext(), 4)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_expenses_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentExpensesOverviewBinding.bind(view)

        // Rv
        val expensesAdapter = ExpensesOverviewAdapter()

        binding.expensesOverviewRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expensesAdapter
        }

        // Livedata
        viewModel.expenses.observe(viewLifecycleOwner){ expenses ->
            if(expenses != null){
                expensesAdapter.updateExpenses(expenses)
            }
        }
    }
}