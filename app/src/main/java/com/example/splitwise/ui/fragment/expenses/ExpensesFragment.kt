package com.example.splitwise.ui.fragment.expenses

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentExpensesBinding
import com.example.splitwise.ui.fragment.adapter.ExpensesAdapter
import com.example.splitwise.ui.fragment.adapter.MembersProfileAdapter
import com.example.splitwise.util.Category
import com.example.splitwise.util.roundOff
import com.google.android.material.chip.Chip
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale


class ExpensesFragment : Fragment() {
    private lateinit var binding: FragmentExpensesBinding
    private val args: ExpensesFragmentArgs by navArgs()

    private val viewModel: ExpensesViewModel by viewModels {
        ExpensesViewModelFactory(requireContext(), args.groupId)
    }

    // animations

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.to_bottom_anim) }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //sharedElementEnterTransition = MaterialContainerTransform()

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment_container
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor =  resources.getColor(R.color.view_color)//Color.TRANSPARENT //R.color.view_color
            setAllContainerColors(resources.getColor(R.color.background))
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.expenses)
        return inflater.inflate(R.layout.fragment_expenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //viewModel.clicked = false

        // start enter transition only when data loaded, and just started to draw
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        //

        //viewModel.fetchData()
        viewModel.loadMembers()

       // requireActivity().title = "Group Detail"

        binding = FragmentExpensesBinding.bind(view)

        setVisibility()

        // Rv
        val expensesAdapter = ExpensesAdapter { expenseId: Int, expenseView: View ->
            gotoExpenseDetailFragment(expenseId, expenseView)
        }
        val membersAdapter = MembersProfileAdapter()

        binding.expensesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                // list reversing is happening in  view model
//                reverseLayout = true // it reverses but scrolled down to the last item
//                stackFromEnd = true // corrects above problem
            }
            adapter = expensesAdapter
        }

        binding.membersRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = membersAdapter
        }

        // Livedata
        viewModel.group.observe(viewLifecycleOwner) { group ->
            if (group != null) {
                binding.groupNameTextView.text = group.groupName
                //requireActivity().title = group.groupName
                //binding.groupCreationDateTextView.text = formatDate(group.creationDate)
                binding.groupExpenseTextView.text = "₹" + group.totalExpense.roundOff()
            }
        }

        viewModel.expenseMembers.observe(viewLifecycleOwner) { expenseMembers ->
            Log.d(TAG, "onViewCreated:expense list livedata $expenseMembers")
            if (expenseMembers != null && expenseMembers.isNotEmpty()) {
                expensesAdapter.updateExpenseMembers(expenseMembers)
                binding.expensesRecyclerView.visibility = View.VISIBLE
                binding.noExpenseImageView.visibility = View.GONE
                binding.noExpenseTextView.visibility = View.GONE
            }
            else{
                binding.expensesRecyclerView.visibility = View.GONE
                binding.noExpenseImageView.visibility = View.VISIBLE
                binding.noExpenseTextView.visibility = View.VISIBLE
            }
        }

        viewModel.groupMembers.observe(viewLifecycleOwner) { members ->
            Log.d(TAG, "onViewCreated:expense members livedata $members")
            if (members != null)
                membersAdapter.updateMembers(members)

        }

        // Button click
        binding.addExpenseButton.setOnClickListener {
            if (args.groupId == 12345 || args.groupId == 54321)
                Toast.makeText(
                    requireContext(),
                    "This is dummy group. Create actual group to add expense",
                    Toast.LENGTH_SHORT
                ).show()
            else {
                gotoAddExpenseFragment(args.groupId)
                onAddButtonClicked()
            }
        }

        binding.addFabButton.setOnClickListener {
            onAddButtonClicked()
        }

        binding.addMemberButton.setOnClickListener{
            goToCreateEditGroupFragment(args.groupId)
            onAddButtonClicked()
        }


        // Collapsing cardview
//        binding.membersLayout.setOnClickListener {
//            if (binding.membersRecyclerView.isVisible) {
//                binding.groupMembersDropDown.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_down_24)
//                binding.membersRecyclerView.visibility = View.GONE
//            } else {
//                binding.groupMembersDropDown.setBackgroundResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
//                binding.membersRecyclerView.visibility = View.VISIBLE
//            }
//        }

        // Menu
//        if (args.groupId == 12345 || args.groupId == 54321)
//            setHasOptionsMenu(true)

        // Chip selection
        binding.categoryChipGroup.forEach { child ->
            (child as Chip).setOnCheckedChangeListener { buttonView, isChecked ->
               // Log.d(TAG, "onViewCreated: checked outside")
                val category = when(buttonView.text){
                    getString(R.string.food) -> Category.FOOD
                    getString(R.string.travel) -> Category.TRAVEL
                    getString(R.string.tickets) -> Category.TICKETS
                    getString(R.string.rent) -> Category.RENT
                    getString(R.string.fees) -> Category.FEES
                    getString(R.string.repairs) -> Category.REPAIRS
                    getString(R.string.entertainment) -> Category.ENTERTAINMENT
                    getString(R.string.essentials) -> Category.ESSENTIALS
                    else -> Category.OTHERS
                }

                if(isChecked){
                    buttonView.setTextColor(requireActivity().resources.getColor(R.color.background))
                    viewModel.incrementCheckedFiltersCount()
                    viewModel.checkedFilters.add(category)
                    Log.d(TAG, "onViewCreated: checked ${viewModel.checkedFilters.toString()}")
                    viewModel.filterByCategory()
                }
                else {
                    buttonView.setTextColor(requireActivity().resources.getColor(R.color.invert_color))
                    viewModel.decrementCheckedFiltersCount()
                    viewModel.checkedFilters.remove(category)
                    Log.d(TAG, "onViewCreated: unchecked ${viewModel.checkedFilters}")
                    viewModel.filterByCategory()
                }
            }
        }

        // clear button click
        binding.clearExpensesFilter.setOnClickListener {
            binding.categoryChipGroup.forEach { child ->
                val chip = child as Chip

                viewModel.checkedFilters.clear()

                if(chip.isChecked){
                    chip.isChecked = false
                }
            }
//            viewModel.checkedFilters.clear()
//            viewModel.filterByCategory()
        }

        // show filter and clear when expenses is not null
        viewModel.expenseCount.observe(viewLifecycleOwner){ count ->
            if(count > 0){
                binding.horizontalScrollView.visibility = View.VISIBLE
            }
            else{
                binding.horizontalScrollView.visibility = View.GONE
            }
        }

        // show clear filter when at least one filter is checked
        viewModel.checkedFiltersCount.observe(viewLifecycleOwner){ count ->
            if(count > 0){
                binding.clearExpensesFilter.visibility = View.VISIBLE
            }
            else{
                binding.clearExpensesFilter.visibility = View.INVISIBLE
            }
        }

        // Pending work
//        viewModel.running.observe(viewLifecycleOwner){ running ->
//            if(running == false && viewModel.pending){
//                viewModel.pending = false
//                viewModel.filterByCategory(viewModel.pendingCategory)
//                viewModel.pendingCategory = null
//            }
//
//        }

//        requireActivity().actionBar?.setDisplayHomeAsUpEnabled(true)

        // Menu
        setHasOptionsMenu(true)

        // show and hide fab on scrollview scrolling

        binding.expensesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Hide fab button
                if(dy >0 && binding.addFabButton.visibility == View.VISIBLE)
                    binding.addFabButton.hide()
                else if(dy < 0 && binding.addFabButton.visibility != View.VISIBLE)
                    binding.addFabButton.show()

            }

        })
//        binding.scrollView.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->
//
//            if(scrollY > oldScrollY)
//                binding.addExpenseButton.hide()
//            else
//                binding.addExpenseButton.show()
//
//        }

        binding.shadowView.setOnClickListener {
            onAddButtonClicked()
        }
    }

    private fun onAddButtonClicked() {
        viewModel.clicked = !viewModel.clicked
        setVisibility()
        //setAnimation()

    }

    private fun setAnimation() {
        if(viewModel.clicked){
            binding.addMemberButton.startAnimation(fromBottom)
            binding.addExpenseButton.startAnimation(fromBottom)
            binding.addFabButton.startAnimation(rotateOpen)
        }
        else{
            binding.addMemberButton.startAnimation(toBottom)
            binding.addExpenseButton.startAnimation(toBottom)
            binding.addFabButton.startAnimation(rotateClose)
        }

//        binding.addMemberButton.postInvalidateOnAnimation()
//        binding.addExpenseButton.postInvalidateOnAnimation()
//        binding.addFabButton.postInvalidateOnAnimation()
//
//        binding.addMemberButton.postInvalidate()
//        binding.addExpenseButton.postInvalidate()
//        binding.addFabButton.postInvalidate()
//
//        binding.addMemberButton.invalidate()
//        binding.addExpenseButton.invalidate()
//        binding.addFabButton.invalidate()
    }

    private fun setVisibility() {
        if(viewModel.clicked){
            binding.addMemberButton.visibility = View.VISIBLE
            binding.addExpenseButton.visibility = View.VISIBLE

            binding.shadowView.visibility = View.VISIBLE

            binding.addFabButton.setImageResource(R.drawable.ic_baseline_close_24)
        }
        else{

//            val p = binding.addMemberButton.layoutParams as CoordinatorLayout.LayoutParams
//            p.anchorId = View.NO_ID
//            binding.addMemberButton.layoutParams = p
            binding.addMemberButton.visibility = View.GONE
            //binding.addMemberButton.visibility = View.GONE

            //binding.addMemberButton
            //binding.addMemberButton.isFocusable = false
            //binding.addMemberButton.clipToOutline = false
//            binding.addMemberButton.isActivated = false
//            binding.addMemberButton.isEnabled = false
//            binding.addMemberButton.isSelected = false

//            val p1 = binding.addExpenseButton.layoutParams as CoordinatorLayout.LayoutParams
//            p1.anchorId = View.NO_ID
//            binding.addExpenseButton.layoutParams = p1
            binding.addExpenseButton.visibility = View.GONE

          //  binding.addExpenseButton.visibility = View.GONE
            //binding.addExpenseButton.hide()
//            binding.addExpenseButton.isActivated = false
//            binding.addExpenseButton.isEnabled = false
//            binding.addExpenseButton.isSelected = false


            //binding.addExpenseButton.isFocusable = false
            binding.shadowView.visibility = View.GONE

//            binding.expensesRecyclerView.isFocusable = true
//            binding.expensesRecyclerView.requestFocus()

            binding.addFabButton.setImageResource(R.drawable.ic_baseline_add_24)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.expense_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share_menu -> {
                viewModel.generateReport { report: String ->
                    shareIntent(report)
                }
                true
            }
            else -> {
                false
            }
        }
    }


    private fun shareIntent(report: String){
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, report)
        }
        startActivity(Intent.createChooser(intent, "Send To"))
    }

    private fun gotoAddExpenseFragment(groupId: Int) {
//        exitTransition = MaterialElevationScale(false).apply {
//            duration = resources.getInteger(R.integer.reply_motion_duration_small).toLong()
//        }

        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

//        val expenseDetailTransitionName = getString(R.string.add_expense_transition_name)
//        val extras = FragmentNavigatorExtras(binding.addExpenseButton to expenseDetailTransitionName)

        val action = ExpensesFragmentDirections.actionExpensesFragmentToAddExpenseFragment(groupId)
        view?.findNavController()?.navigate(action)//, extras)
    }

    private fun gotoGroupsFragment() {
        val action = ExpensesFragmentDirections.actionExpensesFragmentToGroupsFragment()
        view?.findNavController()?.navigate(action)
    }

    private fun gotoExpenseDetailFragment(expenseId: Int, expenseView: View) {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        val expenseDetailTransitionName = getString(R.string.expense_detail_transition_name)
        val extras = FragmentNavigatorExtras(expenseView to expenseDetailTransitionName)
        val action =
            ExpensesFragmentDirections.actionExpensesFragmentToExpenseDetailFragment(expenseId)
        view?.findNavController()?.navigate(action, extras)
    }

    private fun goToCreateEditGroupFragment(groupId: Int = -1) {
//        exitTransition = MaterialElevationScale(false).apply {
//            duration = resources.getInteger(R.integer.reply_motion_duration_small).toLong()
//        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_medium).toLong()
        }

        //val transitionName = getString(R.string.create_edit_group_transition_name)
        //val extras = FragmentNavigatorExtras(binding.addMemberButton to transitionName)

        val action = ExpensesFragmentDirections.actionExpensesFragmentToCreateEditGroupFragment(
            groupId,
            null,
            null,
            null
        )
        findNavController().navigate(action)//, extras)
    }
}