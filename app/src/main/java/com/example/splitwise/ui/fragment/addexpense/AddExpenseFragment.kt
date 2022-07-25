package com.example.splitwise.ui.fragment.addexpense

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentAddExpenseBinding
import com.example.splitwise.ui.fragment.adapter.CategoryArrayAdapter
import com.example.splitwise.ui.fragment.adapter.MembersCheckboxAdapter
import com.example.splitwise.ui.fragment.adapter.PayerArrayAdapter
import com.example.splitwise.util.Category
import com.google.android.material.bottomsheet.BottomSheetDialog

class AddExpenseFragment : Fragment() {

    private lateinit var binding: FragmentAddExpenseBinding
    private val args: AddExpenseFragmentArgs by navArgs()

    private val viewModel: AddExpenseViewModel by viewModels {
        AddExpenseViewModelFactory(requireContext(),args.groupId)
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

        // Rv
        val membersCheckboxAdapter = MembersCheckboxAdapter{ memberId: Int, isChecked: Boolean ->
            if(isChecked)
                viewModel.memberIds.add(memberId)
            else
                viewModel.memberIds.remove(memberId)
        }

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
                binding.choosePayerCard.setOnClickListener {
                    openPayerBottomSheet(members)
                }
//                val payerAdapter = PayerArrayAdapter(requireContext(), R.layout.dropdown, members)
//                binding.payerSpinner.apply {
//                    adapter = payerAdapter
//                    onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
//                        override fun onItemSelected(
//                            parent: AdapterView<*>?,
//                            view: View?,
//                            position: Int,
//                            id: Long
//                        ) {
//                            viewModel.payerId = members[position].memberId
//                            Log.d(TAG, "onItemSelected: payer selected")
//                        }
//
//                        override fun onNothingSelected(parent: AdapterView<*>?) {
//                        }
//
//                    }
//                }
            }
        }

//        // Dropdown
//        binding.categorySpinner.apply {
//            adapter = CategoryArrayAdapter(requireContext(), R.layout.dropdown, categories)
//            onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
//                override fun onItemSelected(
//                    parent: AdapterView<*>?,
//                    view: View?,
//                    position: Int,
//                    id: Long
//                ) {
//                    viewModel.category = categories[position].ordinal
//                    Log.d(TAG, "onItemSelected: category selected")
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>?) {
//                }
//
//            }
//        }

            // Choose category
        // Dropdown
        binding.chooseCategoryCard.setOnClickListener {
            openCategoryBottomSheet()
        }


        // Button click
        binding.createExpenseFab.setOnClickListener {
            
            if(binding.expenseNameText.text?.trim().toString() != "" && binding.expenseAmountText.text?.trim().toString() != ""
                && viewModel.category != null && viewModel.payerId != null){
                viewModel.createExpense(
                    binding.expenseNameText.text.toString(),
                    viewModel.category!!,
                    viewModel.payerId!!,
                    binding.expenseAmountText.text.toString().toFloat(),
                    viewModel.memberIds.toList()){
                    gotoExpenseFragment()
                }

                Toast.makeText(requireContext(), "Expense Added", Toast.LENGTH_SHORT).show()
            }
            else
                Toast.makeText(requireContext(), "Enter all fields to add expense", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openCategoryBottomSheet() {
        val categoryBottomSheetDialog = BottomSheetDialog(requireContext())
        categoryBottomSheetDialog.setContentView(R.layout.bottom_sheet)

        val categoryTitle = categoryBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val categoryList = categoryBottomSheetDialog.findViewById<ListView>(R.id.bottom_sheet_list)
        val categories = Category.values().toList()

        categoryTitle?.text = "Select Category"

        //Adapter
        val categoryAdapter = CategoryArrayAdapter(requireContext(), R.layout.dropdown, categories)
        categoryList?.apply {
            Log.d(TAG, "openCategoryBottomSheet: list adapter set")
            adapter = categoryAdapter
            onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    viewModel.category = categories[position].ordinal
                    binding.chooseCategoryText.text = categories[position].name
                    categoryBottomSheetDialog.dismiss()
                }
        }

        categoryBottomSheetDialog.show()
    }

    private fun openPayerBottomSheet(payers: List<Member>) {
        val payerBottomSheetDialog = BottomSheetDialog(requireContext())
        payerBottomSheetDialog.setContentView(R.layout.bottom_sheet)

        val payerTitle = payerBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val payerList = payerBottomSheetDialog.findViewById<ListView>(R.id.bottom_sheet_list)

        payerTitle?.text = "Select Payer"
        //Adapter
        val payerAdapter = PayerArrayAdapter(requireContext(), R.layout.dropdown, payers)
        payerList?.apply {
            Log.d(TAG, "openPayerBottomSheet: list adapter set")
            adapter = payerAdapter
            onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->
                    viewModel.payerId = payers[position].memberId
                    binding.choosePayerText.text = payers[position].name
                    payerBottomSheetDialog.dismiss()
                }
        }

        payerBottomSheetDialog.show()
    }

    private fun gotoExpenseFragment(){
        val action = AddExpenseFragmentDirections.actionAddExpenseFragmentToExpensesFragment(args.groupId)
        view?.findNavController()?.navigate(action)

    }
}