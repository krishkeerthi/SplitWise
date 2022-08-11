package com.example.splitwise.ui.fragment.expenses

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentExpensesBinding
import com.example.splitwise.ui.fragment.adapter.ExpensesAdapter
import com.example.splitwise.ui.fragment.adapter.MembersProfileAdapter
import com.example.splitwise.ui.fragment.groups.GroupsFragmentDirections
import com.example.splitwise.util.Category
import com.example.splitwise.util.formatDate
import com.example.splitwise.util.roundOff
import com.google.android.material.chip.Chip
import kotlin.math.exp
import kotlin.properties.Delegates

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
    private var clicked: Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_expenses, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clicked = false

       // viewModel.fetchData()

       // requireActivity().title = "Group Detail"

        binding = FragmentExpensesBinding.bind(view)

        // Rv
        val expensesAdapter = ExpensesAdapter { expenseId: Int ->
            gotoExpenseDetailFragment(expenseId)
        }
        val membersAdapter = MembersProfileAdapter()

        binding.expensesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
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
            }
            else{
                binding.expensesRecyclerView.visibility = View.GONE
                binding.noExpenseImageView.visibility = View.VISIBLE
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
            else
                gotoAddExpenseFragment(args.groupId)
        }

        binding.addFabButton.setOnClickListener {
            onAddButtonClicked()
        }

        binding.addMemberButton.setOnClickListener{
            goToCreateEditGroupFragment(args.groupId)
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
        if (args.groupId == 12345 || args.groupId == 54321)
            setHasOptionsMenu(true)

        // Chip selection
        binding.categoryChipGroup.forEach { child ->
            (child as Chip).setOnCheckedChangeListener { buttonView, isChecked ->
                Log.d(TAG, "onViewCreated: checked outside")
                val category = when(buttonView.text){
                    "Food" -> Category.FOOD
                    "Travel" -> Category.TRAVEL
                    "Tickets" -> Category.TICKETS
                    "Rent" -> Category.RENT
                    "Fees" -> Category.FEES
                    "Repair" -> Category.REPAIRS
                    "Entertainment" -> Category.ENTERTAINMENT
                    "Essentials" -> Category.ESSENTIALS
                    else -> Category.OTHERS
                }

                if(isChecked){
                    viewModel.checkedFilters.add(category)
                    Log.d(TAG, "onViewCreated: checked ${buttonView.text}")
                    viewModel.filterByCategory()
                }
                else {
                    viewModel.checkedFilters.remove(category)
                    Log.d(TAG, "onViewCreated: unchecked ${buttonView.text}")
                    viewModel.filterByCategory()
                }
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
    }

    private fun onAddButtonClicked() {
        clicked = !clicked
        setVisibility()
        setAnimation()
    }

    private fun setAnimation() {
        if(clicked){
            binding.addMemberButton.startAnimation(fromBottom)
            binding.addExpenseButton.startAnimation(fromBottom)
            binding.addFabButton.startAnimation(rotateOpen)
        }
        else{
            binding.addMemberButton.startAnimation(toBottom)
            binding.addExpenseButton.startAnimation(toBottom)
            binding.addFabButton.startAnimation(rotateClose)
        }
    }

    private fun setVisibility() {
        if(clicked){
            binding.addMemberButton.visibility = View.VISIBLE
            binding.addExpenseButton.visibility = View.VISIBLE
        }
        else{
            binding.addMemberButton.visibility = View.GONE
            binding.addExpenseButton.visibility = View.GONE
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_detail_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
//            android.R.id.home ->{
//                requireActivity().finish()
//                true
//            }
            R.id.group_delete -> {
                viewModel.deleteGroup(args.groupId)
                gotoGroupsFragment()
                true
            }
            else -> {
                false
            }
        }
    }

    private fun gotoAddExpenseFragment(groupId: Int) {
        val action = ExpensesFragmentDirections.actionExpensesFragmentToAddExpenseFragment(groupId)
        view?.findNavController()?.navigate(action)
    }

    private fun gotoGroupsFragment() {
        val action = ExpensesFragmentDirections.actionExpensesFragmentToGroupsFragment()
        view?.findNavController()?.navigate(action)
    }

    private fun gotoExpenseDetailFragment(expenseId: Int) {
        val action =
            ExpensesFragmentDirections.actionExpensesFragmentToExpenseDetailFragment(expenseId)
        view?.findNavController()?.navigate(action)
    }

    private fun goToCreateEditGroupFragment(groupId: Int = -1) {
        val action = ExpensesFragmentDirections.actionExpensesFragmentToCreateEditGroupFragment(
            groupId,
            null,
            null
        )
        view?.findNavController()?.navigate(action)
    }
}