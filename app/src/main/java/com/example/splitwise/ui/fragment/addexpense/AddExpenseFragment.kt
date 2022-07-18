package com.example.splitwise.ui.fragment.addexpense

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentAddExpenseBinding
import com.example.splitwise.ui.fragment.adapter.CategoryArrayAdapter
import com.example.splitwise.ui.fragment.adapter.MembersCheckboxAdapter
import com.example.splitwise.ui.fragment.adapter.PayerArrayAdapter
import com.example.splitwise.util.Category

class AddExpenseFragment : Fragment() {

    private lateinit var binding: FragmentAddExpenseBinding

    private val viewModel: AddExpenseViewModel by viewModels {
        AddExpenseViewModelFactory(requireContext(),4)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAddExpenseBinding.bind(view)
        val categories = Category.values().toList()

        // Rv
        val membersCheckboxAdapter = MembersCheckboxAdapter()

        binding.membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersCheckboxAdapter
        }

        // Livedata
        viewModel.members.observe(viewLifecycleOwner){ members ->

            if(members != null){
                // Updating member check box adapter
                membersCheckboxAdapter.updateMembers(members)

                // Updating payer adapter
                val payerAdapter = PayerArrayAdapter(requireContext(), R.layout.dropdown, members)
                binding.payerDropdown.apply {
                    setAdapter(payerAdapter)
//                    onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
//                        override fun onItemSelected(
//                            parent: AdapterView<*>?,
//                            view: View?,
//                            position: Int,
//                            id: Long
//                        ) {
//                            TODO("Not yet implemented")
//                        }
//
//                        override fun onNothingSelected(parent: AdapterView<*>?) {
//                            TODO("Not yet implemented")
//                        }
//
//                    }
                }
            }
        }

        // Dropdown
        binding.categoriesDropdown.setAdapter(
            CategoryArrayAdapter(requireContext(), R.layout.dropdown, categories)
        )


        val members = mutableListOf<Int>()
        // Button click
//        binding.createExpenseFab.setOnClickListener {
//            viewModel.createExpense(
//                binding.expenseNameText.text.toString(),
//                binding.categoriesDropdown.text.toString(),
//                binding.payerDropdown.text.toString(),
//                binding.expenseAmountText.text.toString().toFloat(),
//                members
//            )
//        }
    }
}