package com.example.splitwise.ui.fragment.expenses

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentExpensesBinding
import com.example.splitwise.ui.fragment.adapter.ExpensesAdapter
import com.example.splitwise.ui.fragment.adapter.GroupsAdapter
import com.example.splitwise.ui.fragment.adapter.MembersAdapter

class ExpensesFragment : Fragment() {
    private lateinit var binding: FragmentExpensesBinding
    private val args: ExpensesFragmentArgs by navArgs()

    private val viewModel: ExpensesViewModel by viewModels {
        ExpensesViewModelFactory(requireContext(), args.groupId)
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
        val expensesAdapter = ExpensesAdapter{ expenseId: Int ->
            gotoExpenseDetailFragment(expenseId)
        }
        val membersAdapter = MembersAdapter()

        binding.expensesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = expensesAdapter
        }

        binding.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = membersAdapter
        }

        // Livedata
        viewModel.expenseMembers.observe(viewLifecycleOwner){ expenseMembers ->
            Log.d(TAG, "onViewCreated:expense list livedata ${expenseMembers}")
            if(expenseMembers != null){
                expensesAdapter.updateExpenseMembers(expenseMembers)
            }
        }

        viewModel.groupMembers.observe(viewLifecycleOwner){ members ->
            Log.d(TAG, "onViewCreated:expense members livedata ${members}")
            if(members != null)
                membersAdapter.updateMembers(members)

        }

        // Button click
        binding.addExpenseFab.setOnClickListener {
            gotoAddExpenseFragment(args.groupId)
        }

    }

    private fun gotoAddExpenseFragment(groupId: Int){
        val action = ExpensesFragmentDirections.actionExpensesFragmentToAddExpenseFragment(groupId)
        view?.findNavController()?.navigate(action)
    }

    private fun gotoExpenseDetailFragment(expenseId: Int){
        val action = ExpensesFragmentDirections.actionExpensesFragmentToExpenseDetailFragment(expenseId)
        view?.findNavController()?.navigate(action)
    }
}