package com.example.splitwise.ui.fragment.addexpense

import android.content.ContentValues.TAG
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentAddExpenseBinding
import com.example.splitwise.ui.fragment.adapter.*
import com.example.splitwise.util.Category
import com.example.splitwise.util.titleCase
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar

class AddExpenseFragment : Fragment() {

    private lateinit var binding: FragmentAddExpenseBinding
    private val args: AddExpenseFragmentArgs by navArgs()

    private val viewModel: AddExpenseViewModel by viewModels {
        AddExpenseViewModelFactory(requireContext(), args.groupId)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.add_expense)
        return inflater.inflate(R.layout.fragment_add_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAddExpenseBinding.bind(view)

        requireActivity().title = "Add Expense"
        // Rv
        val membersCheckboxAdapter = MembersCheckboxAdapter { memberId: Int, isChecked: Boolean ->
            if (isChecked)
                viewModel.memberIds.add(memberId)
            else
                viewModel.memberIds.remove(memberId)
        }

        val spanCount = if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 5
        binding.membersRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = membersCheckboxAdapter
        }

        // Livedata
        viewModel.members.observe(viewLifecycleOwner) { members ->

            if (members != null) {
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

//        binding.chooseCategoryText.setOnClickListener {
//            openCategoryBottomSheet()
//        }

        // Button click

        // Menu
        setHasOptionsMenu(true)
    }

    private fun createExpense() {
        if (binding.expenseNameText.text?.trim()
                .toString() != "" && binding.expenseAmountText.text?.trim().toString() != ""
            && viewModel.category != null && viewModel.payerId != null
        ) {
            if(viewModel.memberIds.isNotEmpty()){
                viewModel.createExpense(
                    binding.expenseNameText.text.toString(),
                    viewModel.category!!,
                    viewModel.payerId!!,
                    binding.expenseAmountText.text.toString().toFloat(),
                    viewModel.memberIds.toList()
                ) {
                    gotoExpenseFragment()
                }
                Snackbar.make(binding.root, getString(R.string.expense_added), Snackbar.LENGTH_SHORT).show()
           }
            else{
                Snackbar.make(binding.root, getString(R.string.atleast_1_payee), Snackbar.LENGTH_SHORT).show()
            }
        }
        else
            Snackbar.make(binding.root, getString(R.string.enter_all_fields_expense), Snackbar.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_expense_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.add_expense ->{
                createExpense()
                true
            }
            else ->{
                false
            }
        }
    }

    private fun openCategoryBottomSheet() {
        val categoryBottomSheetDialog = BottomSheetDialog(requireContext())
        categoryBottomSheetDialog.setContentView(R.layout.rv_bottom_sheet)

        categoryBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val categoryTitle =
            categoryBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val categoryRV = categoryBottomSheetDialog.findViewById<RecyclerView>(R.id.bottom_sheet_list)
        val categories = Category.values().toList()

        categoryTitle?.text = getString(R.string.select_category)

        //Adapter
        //val categoryAdapter = CategoryArrayAdapter(requireContext(), R.layout.icon_bottom_sheet_item, categories)
        val categoryAdapter = CategoryAdapter(categories){ category ->
            viewModel.category = category.ordinal
            binding.chooseCategoryText.text = category.name.lowercase().titleCase()
            categoryBottomSheetDialog.dismiss()
        }

        categoryRV?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }
//        categoryList?.apply {
//            Log.d(TAG, "openCategoryBottomSheet: list adapter set")
//            adapter = categoryAdapter
//            onItemClickListener =
//                AdapterView.OnItemClickListener { parent, view, position, id ->
//                    viewModel.category = categories[position].ordinal
//                    binding.chooseCategoryText.text = categories[position].name.lowercase().titleCase()
//                    categoryBottomSheetDialog.dismiss()
//                }
//        }

        categoryBottomSheetDialog.show()
    }

    private fun openPayerBottomSheet(payers: List<Member>) {
        val payerBottomSheetDialog = BottomSheetDialog(requireContext())
        payerBottomSheetDialog.setContentView(R.layout.rv_bottom_sheet)

        payerBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val payerTitle = payerBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val payerRv = payerBottomSheetDialog.findViewById<RecyclerView>(R.id.bottom_sheet_list)

        payerTitle?.text = getString(R.string.select_payer)
        //Adapter
//        val payerAdapter = PayerArrayAdapter(requireContext(), R.layout.icon_bottom_sheet_item, payers)
        val payerAdapter = PayerAdapter(payers){ payer ->
            viewModel.payerId = payer.memberId
            binding.choosePayerText.text = payer.name
            payerBottomSheetDialog.dismiss()
        }

        payerRv?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = payerAdapter
        }
//        payerList?.apply {
//            Log.d(TAG, "openPayerBottomSheet: list adapter set")
//            adapter = payerAdapter
//            onItemClickListener =
//                AdapterView.OnItemClickListener { parent, view, position, id ->
//                    viewModel.payerId = payers[position].memberId
//                    binding.choosePayerText.text = payers[position].name
//                    payerBottomSheetDialog.dismiss()
//                }
//        }

        payerBottomSheetDialog.show()
    }

    private fun gotoExpenseFragment() {
        val action =
            AddExpenseFragmentDirections.actionAddExpenseFragmentToExpensesFragment(args.groupId)
        view?.findNavController()?.navigate(action)

    }
}