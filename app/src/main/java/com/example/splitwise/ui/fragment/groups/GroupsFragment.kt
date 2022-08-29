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
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentGroupsBinding
import com.example.splitwise.ui.fragment.adapter.*
import com.example.splitwise.util.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
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
        val groupsAdapter = GroupsAdapter({ groupId: Int ->
            goToExpenseFragment(groupId)
        },
            { groupId: Int, groupIcon: String?, groupName: String ->
                gotoGroupIconFragment(
                    groupId,
                    groupIcon,
                    groupName
                )
            }
        )
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
            layoutManager = LinearLayoutManager(requireContext()).apply {
                reverseLayout = true // it reverses but scrolled down to the last item
                stackFromEnd = true // corrects above problem
            }

            adapter = groupsAdapter
        }

        // Livedata<item name="android:actionBarStyle">@style/ActionBarTheme</item>a
        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            if (groups != null && groups.isNotEmpty()) {
                Log.d(TAG, "onViewCreated: groups livedata ${groups.size}")
                groupsAdapter.updateGroups(groups)
                binding.groupsRecyclerView.visibility = View.VISIBLE
                binding.emptyGroupImageView.visibility = View.GONE
                binding.dateTextView.visibility = View.VISIBLE

            } else {
                Log.d(TAG, "onViewCreated: groups livedata null")
                binding.groupsRecyclerView.visibility = View.GONE
                binding.emptyGroupImageView.visibility = View.VISIBLE
                binding.dateTextView.visibility = View.GONE
            }
        }


        binding.groupsRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
// turned it off for now
//                val layout = recyclerView.layoutManager as LinearLayoutManager
//                val position = layout.findLastCompletelyVisibleItemPosition()
//
//                if (viewModel.groups.value != null) {
//                    binding.dateTextView.visibility = View.VISIBLE
//                    binding.dateTextView.text =
//                        getDateStringResource(formatDate(viewModel.groups.value!![position].creationDate))
//                }
//
//
//                // Hide fab button
//                if (dy > 0 && binding.addGroupFab.visibility == View.VISIBLE)
//                    binding.addGroupFab.hide()
//                else if (dy < 0 && binding.addGroupFab.visibility != View.VISIBLE)
//                    binding.addGroupFab.show()

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
//                binding.dateTextView.visibility = View.GONE
            }
        })

        // Button click
        binding.addGroupFab.setOnClickListener {
            goToCreateEditGroupFragment()
        }

        // Menu
        setHasOptionsMenu(true)
        requireActivity().title = "Groups"

        if (viewModel.filterModel.amountFilterModel != null) {
            val amountFilterModel = viewModel.filterModel.amountFilterModel
            binding.amountFilterChip.text = "${
                amountFilterModel!!.amountFilter.name.lowercase().titleCase()} " +
                    "${amountFilterModel!!.amount}"
            binding.amountFilterChip.visibility = View.VISIBLE
        }

        if (viewModel.filterModel.dateFilterModel != null) {
            val dateFilterModel = viewModel.filterModel.dateFilterModel
            binding.dateFilterChip.text =
                "${dateFilterModel!!.dateFilter.name.lowercase().titleCase()} ${
                    formatDate(dateFilterModel!!.date)}"
            binding.dateFilterChip.visibility = View.VISIBLE
        }

        // Chip closing
        binding.dateFilterChip.setOnCloseIconClickListener {
            Log.d(TAG, "onViewCreated: date filter closed")
            viewModel.removeDateFilter()
            it.visibility = View.GONE
        }
        binding.dateFilterChip.isClickable = false

        binding.amountFilterChip.setOnCloseIconClickListener {
            Log.d(TAG, "onViewCreated: amount filter closed")
            viewModel.removeAmountFilter()
            it.visibility = View.GONE
        }
        binding.amountFilterChip.isClickable = false


    }

    private fun gotoGroupIconFragment(groupId: Int, groupIcon: String?, groupName: String) {
        val action = GroupsFragmentDirections.actionGroupsFragmentToGroupIconFragment(
            groupId,
            groupIcon,
            groupName,
            true
        )
        view?.findNavController()?.navigate(action)
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

    private fun getDateStringResource(formatDate: String): String {
        return when (formatDate) {
            //"Today" -> getString(R.string.today)
            "Yesterday" -> getString(R.string.yesterday)
            else -> formatDate
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
        filterBottomSheetDialog.setContentView(R.layout.group_filter_bottom_sheet)

        val filterTitle = filterBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val filterRV = filterBottomSheetDialog.findViewById<RecyclerView>(R.id.bottom_sheet_list)
        val clearButton = filterBottomSheetDialog.findViewById<MaterialButton>(R.id.clear_filter)

        filterTitle?.text = getString(R.string.filter_by)

        val filters = GroupFilter.values().toList()
        val remainingFilters = viewModel.remainingFilters.toList()

        //Adapter
        val filterAdapter =
            GroupFilterAdapter(filters, remainingFilters) { filter ->
                filterClicked(filter)
                filterBottomSheetDialog.dismiss()
            }

        filterRV?.apply {
            Log.d(TAG, "openCategoryBottomSheet: list adapter set")
            layoutManager = LinearLayoutManager(requireContext())
            adapter = filterAdapter
        }

        // clear button visibility
        clearButton?.visibility = if (filters.size != remainingFilters.size) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }

        clearButton?.setOnClickListener {
            viewModel.resetFilters()
            binding.dateFilterChip.visibility = View.GONE
            binding.amountFilterChip.visibility = View.GONE
            filterBottomSheetDialog.dismiss()
            //binding.groupsRecyclerView.smoothScrollToPosition(0)
            //openFilterBottomSheet()
        }

        filterBottomSheetDialog.show()
    }

    private fun filterClicked(filter: GroupFilter) {
        when (filter) {
            GroupFilter.DATE -> openDateFilterBottomSheet()
            GroupFilter.AMOUNT -> openAmountFilterBottomSheet()
        }
    }

//    private fun openFilterBottomSheet() {
//        val filterBottomSheetDialog = BottomSheetDialog(requireContext())
//        filterBottomSheetDialog.setContentView(R.layout.bottom_sheet)
//
//        val filterTitle = filterBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
//        val filterList = filterBottomSheetDialog.findViewById<ListView>(R.id.bottom_sheet_list)
//        val filters = viewModel.remainingFilters
//
//        filterTitle?.text = getString(R.string.filter_by)
//
//        //Adapter
//        val filterAdapter =
//            GroupFilterArrayAdapter(requireContext(), R.layout.icon_bottom_sheet_item, filters)
//        filterList?.apply {
//            Log.d(TAG, "openCategoryBottomSheet: list adapter set")
//            adapter = filterAdapter
//            onItemClickListener =
//                AdapterView.OnItemClickListener { parent, view, position, id ->
//
//                    when (filters[position]) {
//                        GroupFilter.AMOUNT -> {
//                            openAmountFilterBottomSheet()
//                        }
//                        GroupFilter.DATE -> {
//                            openDateFilterBottomSheet()
//                        }
//                    }
//
//                    filterBottomSheetDialog.dismiss()
//                }
//        }
//
//        filterBottomSheetDialog.show()
//    }

    private fun goToCreateEditGroupFragment(groupId: Int = -1) {
        val action = GroupsFragmentDirections.actionGroupsFragmentToCreateEditGroupFragment(
            groupId,
            null,
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
            AmountFilterArrayAdapter(
                requireContext(),
                R.layout.icon_bottom_sheet_item,
                amountFilters
            )
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
            DateFilterArrayAdapter(requireContext(), R.layout.icon_bottom_sheet_item, dateFilters)
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

        builder.setPositiveButton(getString(R.string.save)) { dialogInterface, _ ->
            val amount = amountEditText.text.toString().toFloat()

            viewModel.applyAmountFilter(amount)
            createAmountFilterChip(viewModel.selectedAmountFilter, amount)
        }

        builder.setNegativeButton(getString(R.string.cancel)){ dialogInterface, _ ->
            dialogInterface.cancel()
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
                val amountText = amountEditText.text.toString()

                val amount = if(amountText.isNotEmpty()) amountText.toInt() else 0



                if (amount == 0 || amountText.isEmpty())
                    amountLayout.error = "Enter Amount"
                else {
                    amountLayout.error = null
                    amountLayout.isErrorEnabled = false
                }

                dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = amountText.isNotEmpty()
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

        binding.dateFilterChip.text = "${dateFilter.name.lowercase().titleCase()} ${formatDate(date, dateOnly = true)}"
        binding.dateFilterChip.visibility = View.VISIBLE
    }

    private fun createAmountFilterChip(amountFilter: AmountFilter, amount: Float) {
        binding.amountFilterChip.text = "${amountFilter.name.lowercase().titleCase()} ${amount.roundOff()}"
        binding.amountFilterChip.visibility = View.VISIBLE
    }


}