package com.example.splitwise.ui.fragment.expenses

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentExpensesBinding
import com.example.splitwise.ui.fragment.adapter.ExpensesAdapter
import com.example.splitwise.ui.fragment.adapter.GroupsAdapter
import com.example.splitwise.ui.fragment.adapter.MembersAdapter

class ExpensesFragment : Fragment() {

    private lateinit var binding: FragmentExpensesBinding

    private val viewModel: ExpensesViewModel by viewModels {
        ExpensesViewModelFactory(requireContext(), 4)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_expenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentExpensesBinding.bind(view)

        // Rv
        val expensesAdapter = ExpensesAdapter()
        val membersAdapter = MembersAdapter()

        binding.expensesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expensesAdapter
        }

        binding.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
        }

        // Livedata
        viewModel.expenseMembers.observe(viewLifecycleOwner){ expenseMembers ->
            if(expenseMembers != null){
                expensesAdapter.updateExpenseMembers(expenseMembers)
            }
        }

        viewModel.groupMembers.observe(viewLifecycleOwner){ members ->
            if(members != null)
                membersAdapter.updateMembers(members)

        }

        // Button click
        binding.addExpenseFab.setOnClickListener {
            // go to add expense fragment
        }
    }
}