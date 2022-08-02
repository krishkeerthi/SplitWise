package com.example.splitwise.ui.fragment.groups

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentGroupsBinding
import com.example.splitwise.ui.fragment.adapter.FilterArrayAdapter
import com.example.splitwise.ui.fragment.adapter.GroupsAdapter
import com.example.splitwise.util.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.*

class GroupsFragment : Fragment() {

    private lateinit var binding: FragmentGroupsBinding
    private val viewModel: GroupsViewModel by viewModels {
        GroupsViewModelFactory(requireContext())
    }

    //private lateinit var viewModel: GroupsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Chip visibility on view created
//        val view = inflater.inflate(R.layout.fragment_groups, container, false)
//        binding = FragmentGroupsBinding.bind(view)

        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchData() // fetching data in viewmodel init{} does not get called while returning from back stack
        //viewModel = ViewModelProvider(this, GroupsViewModelFactory(requireContext()))[GroupsViewModel::class.java]
        binding = FragmentGroupsBinding.bind(view)


        // Rv
        val groupsAdapter = GroupsAdapter{ groupId: Int ->
            goToExpenseFragment(groupId)
        }
//            { groupId: Int ->
//                if (groupId == 12345 || groupId == 54321)
//                    Toast.makeText(
//                        requireContext(),
//                        "This is dummy group. Create actual group to add members",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                else
//                    goToCreateEditGroupFragment(groupId)
//            },


        binding.groupsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupsAdapter
        }

        // Livedata
        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            Log.d(TAG, "onViewCreated: groups livedata ${groups}")
            if (groups != null) {
                groupsAdapter.updateGroups(groups)
            }
        }

        // Button click
        binding.addGroupFab.setOnClickListener {
            goToCreateEditGroupFragment()
        }

        // Menu
        setHasOptionsMenu(true)
//        requireActivity().addMenuProvider(object : MenuProvider {
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menuInflater.inflate(R.menu.groups_fragment_menu, menu)
//            }
//
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//                return when (menuItem.itemId) {
//                    R.id.group_fragment_search -> {
//                        goToSearchGroupFragment()
//                        true
//                    }
//                    R.id.group_fragment_filter -> {
//                        openFilterBottomSheet()
//                        true
//                    }
//                    else ->
//                        false
//                }
//            }
//
//        })


        requireActivity().title = "Groups"
//        binding.groupsToolbar.inflateMenu(R.menu.groups_fragment_menu)
//
//        // Menu click
//        binding.groupsToolbar.setOnMenuItemClickListener { item ->
//            when(item.itemId){
//                R.id.group_fragment_search ->{
//                    goToSearchGroupFragment()
//                    true
//                }
//                R.id.group_fragment_filter ->{
//                    openFilterBottomSheet()
//                    true
//                }
//                else ->
//                    false
//            }
//        }

        if (viewModel.filterModel.amountFilterModel != null) {
            val amountFilterModel = viewModel.filterModel.amountFilterModel
            binding.amountFilterChip.text = "${
                amountFilterModel!!.amountFilter.name.lowercase().titleCase()
            } ${amountFilterModel!!.amount}"
            binding.amountFilterChip.visibility = View.VISIBLE
        }

        if (viewModel.filterModel.dateFilterModel != null) {
            val dateFilterModel = viewModel.filterModel.dateFilterModel
            binding.dateFilterChip.text =
                "${dateFilterModel!!.dateFilter.name.lowercase().titleCase()} ${
                    formatDate(dateFilterModel!!.date)
                }"
            binding.dateFilterChip.visibility = View.VISIBLE
        }

        // Chip closing
        binding.dateFilterChip.setOnCloseIconClickListener {
            Log.d(TAG, "onViewCreated: date filter closed")
            viewModel.removeDateFilter()
            it.visibility = View.GONE
        }

        binding.amountFilterChip.setOnCloseIconClickListener {
            Log.d(TAG, "onViewCreated: amount filter closed")
            viewModel.removeAmountFilter()
            it.visibility = View.GONE
        }


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.groups_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.group_fragment_search -> {
                goToSearchGroupFragment()
                true
            }
            R.id.group_fragment_filter -> {
                openFilterBottomSheet()
                true
            }
            else ->
                false
        }
    }

    private fun goToSearchGroupFragment() {
        val action = GroupsFragmentDirections.actionGroupsFragmentToSearchGroupFragment()
        view?.findNavController()?.navigate(action)
    }

    private fun goToExpenseFragment(groupId: Int) {
        val action = GroupsFragmentDirections.actionGroupsFragmentToExpensesFragment(groupId)
        view?.findNavController()?.navigate(action)
    }

    // Bottom Sheet Dialogs
    private fun openFilterBottomSheet() {
        val filterBottomSheetDialog = BottomSheetDialog(requireContext())
        filterBottomSheetDialog.setContentView(R.layout.bottom_sheet)

        val filterTitle = filterBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val filterList = filterBottomSheetDialog.findViewById<ListView>(R.id.bottom_sheet_list)
        val filters = viewModel.remainingFilters

        filterTitle?.text = getString(R.string.filter_by)

        //Adapter
        val filterAdapter =
            FilterArrayAdapter<GroupFilter>(requireContext(), R.layout.dropdown, filters)
        filterList?.apply {
            Log.d(TAG, "openCategoryBottomSheet: list adapter set")
            adapter = filterAdapter
            onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->

                    when (filters[position]) {
                        GroupFilter.AMOUNT -> {
                            openAmountFilterBottomSheet()
                        }
                        GroupFilter.DATE -> {
                            openDateFilterBottomSheet()
                        }
                    }

                    filterBottomSheetDialog.dismiss()
                }
        }

        filterBottomSheetDialog.show()
    }

    private fun goToCreateEditGroupFragment(groupId: Int = -1) {
        val action = GroupsFragmentDirections.actionGroupsFragmentToCreateEditGroupFragment(
            groupId,
            null,
            null
        )
        view?.findNavController()?.navigate(action)
    }

    private fun openAmountFilterBottomSheet() {
        val amountFilterBottomSheetDialog = BottomSheetDialog(requireContext())
        amountFilterBottomSheetDialog.setContentView(R.layout.bottom_sheet)

        val amountFilterTitle =
            amountFilterBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val amountFilterList =
            amountFilterBottomSheetDialog.findViewById<ListView>(R.id.bottom_sheet_list)
        val amountFilters = AmountFilter.values().toList()

        amountFilterTitle?.text = getString(R.string.filter_by_amount)

        //Adapter
        val filterAdapter =
            FilterArrayAdapter<AmountFilter>(requireContext(), R.layout.dropdown, amountFilters)
        amountFilterList?.apply {
            Log.d(TAG, "openCategoryBottomSheet: list adapter set")
            adapter = filterAdapter
            onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->

                    viewModel.selectedAmountFilter = amountFilters[position]
                    openAmountDialog()
                    amountFilterBottomSheetDialog.dismiss()
                }
        }

        amountFilterBottomSheetDialog.show()
    }

    private fun openDateFilterBottomSheet() {
        val dateFilterBottomSheetDialog = BottomSheetDialog(requireContext())
        dateFilterBottomSheetDialog.setContentView(R.layout.bottom_sheet)

        val dateFilterTitle =
            dateFilterBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val dateFilterList =
            dateFilterBottomSheetDialog.findViewById<ListView>(R.id.bottom_sheet_list)
        val dateFilters = DateFilter.values().toList()

        dateFilterTitle?.text = getString(R.string.filter_by_date)

        //Adapter
        val filterAdapter =
            FilterArrayAdapter<DateFilter>(requireContext(), R.layout.dropdown, dateFilters)
        dateFilterList?.apply {
            Log.d(TAG, "openCategoryBottomSheet: list adapter set")
            adapter = filterAdapter
            onItemClickListener =
                AdapterView.OnItemClickListener { parent, view, position, id ->

                    viewModel.selectedDateFilter = dateFilters[position]
                    openDatePicker()

                    dateFilterBottomSheetDialog.dismiss()
                }
        }

        dateFilterBottomSheetDialog.show()
    }

    private val dateSetListener =
        DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->  // SAM
            //Toast.makeText(requireContext(), "$dayOfMonth/${month+1}/$year", Toast.LENGTH_SHORT).show()
            // put logic
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }

            val date = calendar.time

            viewModel.applyDateFilter(date)
            createDateFilterChip(viewModel.selectedDateFilter, date)
        }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)

        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), dateSetListener, year, month, day).show()
    }

    private fun openAmountDialog() {
        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle("Enter Amount")

        val amountDialog = layoutInflater.inflate(R.layout.add_amount_dialog, null)
        builder.setView(amountDialog)

        val amountEditText = amountDialog.findViewById<TextInputEditText>(R.id.amount_text)
        val amountLayout =
            amountDialog.findViewById<TextInputLayout>(R.id.outlined_amount_text_field)

        builder.setPositiveButton("Save") { dialogInterface, _ ->
            val amount = amountEditText.text.toString().toFloat()

            viewModel.applyAmountFilter(amount)
            createAmountFilterChip(viewModel.selectedAmountFilter, amount)
        }

        val dialog = builder.create()
        dialog.show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false

        val amountWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val amount = amountEditText.text.toString()

                if (amount.isEmpty())
                    amountLayout.error = "Enter Amount"
                else {
                    amountLayout.error = null
                    amountLayout.isErrorEnabled = false
                }

                dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = amount.isNotEmpty()
            }
        }
        amountEditText.addTextChangedListener(amountWatcher)

    }

    private fun createDateFilterChip(dateFilter: DateFilter, date: Date) {
//        val chipGroup = binding.filterChipGroup
//        val chip = Chip(requireContext()).apply {
//            text = "${dateFilter.name} ${formatDate(date)}"
//            setCloseIconResource(R.drawable.ic_baseline_close_24)
//            isCloseIconVisible = true
//            setOnCloseIconClickListener { View.OnClickListener {
//                viewModel.removeDateFilter()
//                chipGroup.removeView(it)
//            } }
//        }
//
//        chipGroup.addView(chip)

        binding.dateFilterChip.text = "${dateFilter.name} ${formatDate(date)}"
        binding.dateFilterChip.visibility = View.VISIBLE
    }

    private fun createAmountFilterChip(amountFilter: AmountFilter, amount: Float) {
        binding.amountFilterChip.text = "${amountFilter.name} $amount"
        binding.amountFilterChip.visibility = View.VISIBLE
    }


}