package com.example.splitwise.ui.fragment.groups

import android.app.DatePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.metrics.performance.JankStats
import androidx.metrics.performance.PerformanceMetricsState
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentGroupsBinding
import com.example.splitwise.ui.fragment.adapter.*
import com.example.splitwise.ui.fragment.addamount.AddAmountDialog
import com.example.splitwise.util.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis
import java.util.*

class GroupsFragment : Fragment() {

    private lateinit var binding: FragmentGroupsBinding
    private val viewModel: GroupsViewModel by viewModels {
        GroupsViewModelFactory(requireContext())
    }

    //private lateinit var viewModel: GroupsViewModel
    private lateinit var jankStats: JankStats

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // this animation not working fine in my device(1+9)
//        reenterTransition = MaterialElevationScale(true).apply {
//            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
//        }
//        exitTransition = MaterialElevationScale(false).apply {
//            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
//        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // Chip visibility on view created
//        val view = inflater.inflate(R.layout.fragment_groups, container, false)
//        binding = FragmentGroupsBinding.bind(view)

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.groups)
        return inflater.inflate(R.layout.fragment_groups, container, false)
    }

    private val jankFrameListener = JankStats.OnFrameListener { frameData ->
        Log.d(
            TAG, "JankStats: ----------------\n " +
                    "is Jank: ${frameData.isJank} \n" +
                    "frameDurationUiNanos: ${frameData.frameDurationUiNanos} \n" +
                    "frameStartNanos: ${frameData.frameStartNanos}\n" +
                    "states: ${frameData.states}\n" +
                    "Overall: ${frameData.toString()}" +
                    "-------------------------\n"
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // start enter transition only when data loaded, and just started to draw
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        //


        //viewModel.fetchData() // fetching data in viewmodel init{} does not get called while returning from back stack
        //viewModel = ViewModelProvider(this, GroupsViewModelFactory(requireContext()))[GroupsViewModel::class.java]
        binding = FragmentGroupsBinding.bind(view)

//        Toast.makeText(requireContext(),
//            String.format(resources.getString(R.string.test), "1234"), Toast.LENGTH_SHORT).show()

        // Jank stat start

        val metricStateHolder = PerformanceMetricsState.getHolderForHierarchy(binding.root)
        jankStats = JankStats.createAndTrack(requireActivity().window, jankFrameListener)
        metricStateHolder.state?.putState("Fragment", this.toString())
        // janks stat ends


        // Rv
        val groupsAdapter = GroupsAdapter({ groupId: Int, groupView: View ->
            goToExpenseFragment(groupId, groupView)
        },
            { groupId: Int, groupIcon: String?, groupName: String, groupView: View ->
                gotoGroupIconFragment(
                    groupId,
                    groupIcon,
                    groupName,
                    groupView
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
//                reverseLayout = true // it reverses but scrolled down to the last item
//                stackFromEnd = true // corrects above problem
                //scrollToPosition(1)
            }

            adapter = groupsAdapter
        }

        // Testing swipe in groups recyclerview
        val swipeToDeleteCallback = object : SwipeToDeleteCallback() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                val swipeDirection = if (ItemTouchHelper.LEFT == direction) "Left"
//                else if (ItemTouchHelper.RIGHT == direction) "Right"
//                else "Unknown"
//
//                Toast.makeText(
//                    requireContext(),
//                    "swiped $swipeDirection, rv position ${viewHolder.absoluteAdapterPosition}",
//                    Toast.LENGTH_SHORT
//                ).show()

                viewModel.groups.value?.let {
                    goToExpenseFragment(it[viewHolder.absoluteAdapterPosition].groupId, viewHolder.itemView)
                }
            }

        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(binding.groupsRecyclerView)
        // test ends

        // Livedata<item name="android:actionBarStyle">@style/ActionBarTheme</item>a
        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            if (groups != null && groups.isNotEmpty()) {
                Log.d(TAG, "onViewCreated: groups livedata ${groups.size}")
                groupsAdapter.updateGroups(groups)
                binding.groupsRecyclerView.visibility = View.VISIBLE
                binding.emptyGroupImageView.visibility = View.GONE
                binding.noGroupsTextView.visibility = View.GONE
                binding.dateTextView.visibility = View.VISIBLE

//                Log.d(TAG, "onViewCreated: rv ${binding.groupsRecyclerView.hasPendingAdapterUpdates()}")
//                Log.d(TAG, "onViewCreated: rv ${binding.groupsRecyclerView.invalidate()}")
//                Log.d(TAG, "onViewCreated: rv ${binding.groupsRecyclerView.bringToFront()}")
//                binding.groupsRecyclerView.postInvalidateDelayed(10)
//                binding.groupsRecyclerView.forceLayout()

                // experimental because during orientation change filter not showing
                updateAmountFilter()
                updateDateFilter()
            } else {
                Log.d(TAG, "onViewCreated: groups livedata null")
                binding.groupsRecyclerView.visibility = View.GONE
                binding.emptyGroupImageView.visibility = View.VISIBLE
                binding.noGroupsTextView.visibility = View.VISIBLE
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
                // Hide fab button
                if (dy > 0 && binding.addGroupFab.visibility == View.VISIBLE)
                    binding.addGroupFab.hide()
                else if (dy < 0 && binding.addGroupFab.visibility != View.VISIBLE)
                    binding.addGroupFab.show()

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

        updateAmountFilter()

        updateDateFilter()

        // Chip closing
        binding.dateFilterChip.setOnCloseIconClickListener {
            Log.d(TAG, "onViewCreated: date filter closed")
            viewModel.removeDateFilter()
            it.visibility = View.GONE

            if (viewModel.filterModel.amountFilterModel == null)
                binding.horizontalView.visibility = View.GONE
        }
        binding.dateFilterChip.isClickable = false

        binding.amountFilterChip.setOnCloseIconClickListener {
            Log.d(TAG, "onViewCreated: amount filter closed")
            viewModel.removeAmountFilter()
            it.visibility = View.GONE

            if (viewModel.filterModel.dateFilterModel == null)
                binding.horizontalView.visibility = View.GONE
        }
        binding.amountFilterChip.isClickable = false


        // ripple effect

//        val attrs = IntArray(androidx.appcompat.R.attr.selectableItemBackground)
//        val typedArray = requireActivity().obtainStyledAttributes(attrs)
//        val bgRes = typedArray.getResourceId(0, 0)
//        binding.root.setBackgroundResource(bgRes)

    }

    override fun onResume() {
        super.onResume()
        jankStats.isTrackingEnabled = true
    }

    override fun onPause() {
        super.onPause()
        jankStats.isTrackingEnabled = false
    }

    private fun updateDateFilter() {
        if (viewModel.filterModel.dateFilterModel != null) {
            val dateFilterModel = viewModel.filterModel.dateFilterModel
            binding.dateFilterChip.text = String.format(
                getString(R.string.date_filter),
                dateFilterModel!!.dateFilter.name.lowercase().titleCase()
                    .translate(requireContext()),
                formatDate(dateFilterModel!!.date, dateOnly = true)
            )
//                "${dateFilterModel!!.dateFilter.name.lowercase().titleCase().translate(requireContext())} ${
//                    formatDate(dateFilterModel!!.date, dateOnly = true)
//                }"
            binding.dateFilterChip.visibility = View.VISIBLE
            binding.horizontalView.visibility = View.VISIBLE
        }
    }

    private fun updateAmountFilter() {
        if (viewModel.filterModel.amountFilterModel != null) {
            val amountFilterModel = viewModel.filterModel.amountFilterModel
            binding.amountFilterChip.text = String.format(
                getString(R.string.amount_filter),
                amountFilterModel!!.amountFilter.name.lowercase().titleCase()
                    .translate(requireContext()),
                amountFilterModel!!.amount.roundOff()
            )
//                "${
//                amountFilterModel!!.amountFilter.name.lowercase().titleCase().translate(requireContext())
//            } " +
//                    "₹${amountFilterModel!!.amount}"
            binding.amountFilterChip.visibility = View.VISIBLE
            binding.horizontalView.visibility = View.VISIBLE
        }
    }

    private fun gotoGroupIconFragment(
        groupId: Int,
        groupIcon: String?,
        groupName: String,
        groupImageView: View
    ) {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        val transitionName = getString(R.string.group_icon_transition_name)
        val extras = FragmentNavigatorExtras(groupImageView to transitionName)
        val action = GroupsFragmentDirections.actionGroupsFragmentToGroupIconFragment(
            groupId,
            groupIcon,
            groupName,
            true,
            false
        )
        view?.findNavController()?.navigate(action, extras)
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
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }


        val action = GroupsFragmentDirections.actionGroupsFragmentToSearchGroupFragment()
        view?.findNavController()?.navigate(action)
    }

    private fun goToExpenseFragment(groupId: Int, groupView: View) {

        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }


        val expensesTransitionName = getString(R.string.expenses_transition_name)
        val extras = FragmentNavigatorExtras(groupView to expensesTransitionName)
        val action = GroupsFragmentDirections.actionGroupsFragmentToExpensesFragment(groupId)
        findNavController().navigate(action, extras)
    }

    // Bottom Sheet Dialogs
    private fun openFilterBottomSheet() {
        val filterBottomSheetDialog = BottomSheetDialog(requireContext())
        filterBottomSheetDialog.setContentView(R.layout.group_filter_bottom_sheet)

        filterBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

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
            binding.horizontalView.visibility = View.GONE
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
        // transition
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        val transitionName = getString(R.string.create_edit_group_transition_name)
        val extras = FragmentNavigatorExtras(binding.addGroupFab to transitionName)

        val action = GroupsFragmentDirections.actionGroupsFragmentToCreateEditGroupFragment(
            groupId,
            null,
            null,
            null
        )
        findNavController().navigate(action, extras)

    }

    private fun openAmountFilterBottomSheet() {
        val amountFilterBottomSheetDialog = BottomSheetDialog(requireContext())
        amountFilterBottomSheetDialog.setContentView(R.layout.rv_bottom_sheet)

        amountFilterBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val amountFilterTitle =
            amountFilterBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val amountFilterRv =
            amountFilterBottomSheetDialog.findViewById<RecyclerView>(R.id.bottom_sheet_list)
        val amountFilters = AmountFilter.values().toList()

        amountFilterTitle?.text = getString(R.string.filter_by_amount)

        //Adapter
//        val filterAdapter =
//            AmountFilterArrayAdapter(
//                requireContext(),
//                R.layout.icon_bottom_sheet_item,
//                amountFilters
//            )
//        amountFilterList?.apply {
//            Log.d(TAG, "openCategoryBottomSheet: list adapter set")
//            adapter = filterAdapter
//            onItemClickListener =
//                AdapterView.OnItemClickListener { parent, view, position, id ->
//
//                    viewModel.selectedAmountFilter = amountFilters[position]
//                    openAmountDialog()
//                    amountFilterBottomSheetDialog.dismiss()
//                }
//        }

        val filterAdapter = AmountFilterAdapter(
            amountFilters
        ) { filter ->
            amountFilterClicked(filter)
            amountFilterBottomSheetDialog.dismiss()
        }

        amountFilterRv?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = filterAdapter
        }

        amountFilterBottomSheetDialog.show()
    }

    private fun amountFilterClicked(filter: AmountFilter) {
        viewModel.selectedAmountFilter = filter
        openAmountDialog()
    }

    private fun openDateFilterBottomSheet() {
        val dateFilterBottomSheetDialog = BottomSheetDialog(requireContext())
        dateFilterBottomSheetDialog.setContentView(R.layout.rv_bottom_sheet)

        dateFilterBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val dateFilterTitle =
            dateFilterBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val dateFilterRv =
            dateFilterBottomSheetDialog.findViewById<RecyclerView>(R.id.bottom_sheet_list)
        val dateFilters = DateFilter.values().toList()

        dateFilterTitle?.text = getString(R.string.filter_by_date)

        //Adapter
//        val filterAdapter =
//            DateFilterArrayAdapter(requireContext(), R.layout.icon_bottom_sheet_item, dateFilters)
//        dateFilterList?.apply {
//            Log.d(TAG, "openCategoryBottomSheet: list adapter set")
//            adapter = filterAdapter
//            onItemClickListener =
//                AdapterView.OnItemClickListener { parent, view, position, id ->
//
//                    viewModel.selectedDateFilter = dateFilters[position]
//                    openDatePicker()
//
//                    dateFilterBottomSheetDialog.dismiss()
//                }
//        }

        val filterAdapter = DateFilterAdapter(
            dateFilters
        ) { filter ->
            dateFilterClicked(filter)
            dateFilterBottomSheetDialog.dismiss()
        }

        dateFilterRv?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = filterAdapter
        }
        dateFilterBottomSheetDialog.show()
    }

    private fun dateFilterClicked(filter: DateFilter) {
        viewModel.selectedDateFilter = filter
        openDatePicker()
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
        AddAmountDialog(viewModel) { amountFilter, amount, dialogContext ->
            createAmountFilterChip(amountFilter, amount, dialogContext)
        }.show(childFragmentManager, "Add Amount Alert Dialog")
//        val builder = AlertDialog.Builder(requireContext())
//
//        builder.setTitle("Enter Amount")
//
//        val amountDialog = layoutInflater.inflate(R.layout.add_amount_dialog, null)
//        builder.setView(amountDialog)
//
//        val amountEditText = amountDialog.findViewById<TextInputEditText>(R.id.amount_text)
//        val amountLayout =
//            amountDialog.findViewById<TextInputLayout>(R.id.outlined_amount_text_field)
//
//        builder.setPositiveButton(getString(R.string.save)) { dialogInterface, _ ->
//            val amount = amountEditText.text.toString().toFloat()
//
//            viewModel.applyAmountFilter(amount)
//            createAmountFilterChip(viewModel.selectedAmountFilter, amount)
//        }
//
//        builder.setNegativeButton(getString(R.string.cancel)){ dialogInterface, _ ->
//            dialogInterface.cancel()
//        }
//
//        val dialog = builder.create()
//        dialog.show()
//
//
//        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
//
//        val amountWatcher = object : TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            }
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                val amountText = amountEditText.text.toString()
//
//                val amount = if(amountText.isNotEmpty()) amountText.toInt() else 0
//
//
//
//                if (amount == 0 || amountText.isEmpty())
//                    amountLayout.error = "Enter Amount"
//                else {
//                    amountLayout.error = null
//                    amountLayout.isErrorEnabled = false
//                }
//
//                dialog.getButton(Dialog.BUTTON_POSITIVE).isEnabled = amountText.isNotEmpty()
//            }
//        }
//        amountEditText.addTextChangedListener(amountWatcher)

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

        binding.dateFilterChip.text =
            String.format(
                getString(R.string.date_filter),
                dateFilter.name.lowercase().titleCase().translate(requireContext()),
                formatDate(date, dateOnly = true)
            )
        //    "${dateFilter.name.lowercase().titleCase().translate(requireContext())} ${formatDate(date, dateOnly = true)}"
        binding.dateFilterChip.visibility = View.VISIBLE

        binding.horizontalView.visibility = View.VISIBLE
    }

    private fun createAmountFilterChip(
        amountFilter: AmountFilter,
        amount: Float,
        dialogContext: Context
    ) {
        binding.amountFilterChip.text =
            String.format(
                dialogContext.getString(R.string.amount_filter),
                amountFilter.name.lowercase().titleCase().translate(dialogContext),
                amount.roundOff()
            )

        //viewModel.applyAmountFilter(amount)
        // Error: Fragment not attached to a context, when calling getString() without requireActivity()
        //          "${amountFilter.name.lowercase().titleCase().translate(requireContext())} ₹${amount.roundOff()}"
        binding.amountFilterChip.visibility = View.VISIBLE

        binding.horizontalView.visibility = View.VISIBLE
    }


}