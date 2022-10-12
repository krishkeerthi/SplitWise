package com.example.splitwise.ui.fragment.editexpense

import android.content.ContentValues
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentEditExpenseBinding
import com.example.splitwise.ui.fragment.adapter.CategoryAdapter
import com.example.splitwise.ui.fragment.adapter.MembersCheckboxAdapter
import com.example.splitwise.ui.fragment.adapter.PayerAdapter
import com.example.splitwise.util.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale

class EditExpenseFragment : Fragment() {

    private lateinit var binding: FragmentEditExpenseBinding
    private val args: EditExpenseFragmentArgs by navArgs()

    private val viewModel: EditExpenseViewModel by viewModels {
        EditExpenseViewModelFactory(requireContext(), args.groupId, args.expense)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.edit_expense)
        return inflater.inflate(R.layout.fragment_edit_expense, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentEditExpenseBinding.bind(view)

        // start enter transition only when data loaded, and just started to draw
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        //

        // transition code starts
//        enterTransition = MaterialContainerTransform().apply {
//            startView = activity?.findViewById(R.id.add_expense_button)
//            endView = binding.root
//            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
//            scrimColor = Color.TRANSPARENT
//            containerColor = resources.getColor(R.color.background)
//            startContainerColor = resources.getColor(R.color.view_color)
//            endContainerColor = resources.getColor(R.color.background)
//        }
//
//        returnTransition = Slide().apply {
//            duration = resources.getInteger(R.integer.reply_motion_duration_medium).toLong()
//            addTarget(binding.root)
//        }
        // transition code ends

        // Load existing data

        binding.expenseNameText.setText(args.expense.expenseName)
        binding.expenseAmountText.setText(args.expense.totalAmount.roundOff())

        viewModel.category = getCategory(args.expense.category)
        binding.chooseCategoryText.text =
            viewModel.category!!.name.lowercase().titleCase().translate(requireContext())

        binding.categoryImageHolder.visibility = View.VISIBLE
        binding.categoryImageView.visibility = View.VISIBLE
        binding.categoryImageView.setImageResource(getCategoryDrawableResource(viewModel.category!!.ordinal))
        binding.categoryHolderImageView.visibility = View.INVISIBLE


        // guaranteed that it was set earlier

        // this is to set expense payees argument only once
        if (!viewModel.membersUpdated) {
            viewModel.membersUpdated = true
            viewModel.memberIds = getMemberIds(args.expensePayees.toList()).toMutableSet()
        }

        // Rv
        val membersCheckboxAdapter = MembersCheckboxAdapter { memberId: Int, isChecked: Boolean ->
            if (isChecked) {
                Log.d(ContentValues.TAG, "onViewCreated: expense members checked")
                viewModel.memberIds.add(memberId)
                Log.d(ContentValues.TAG, "onViewCreated: expense members size ${viewModel.memberIds.size}")

            }
            else {
                Log.d(ContentValues.TAG, "onViewCreated: expense members unchecked")
                viewModel.memberIds.remove(memberId)
                Log.d(ContentValues.TAG, "onViewCreated: expense members size ${viewModel.memberIds.size}")

            }
        }

        val spanCount =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 8
        binding.membersRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = membersCheckboxAdapter
        }

        // Livedata
        viewModel.members.observe(viewLifecycleOwner) { members ->

            if (members != null) {
                // set existing payer
                if (viewModel.payer == null) {
                    val member: Member = viewModel.getPayer(args.expense.payer, members)!!
                    viewModel.payer = member
                    binding.choosePayerText.text = member.name
                    // profile set
                    binding.payerImageView.visibility = View.VISIBLE
                    binding.payerImageView.setImageBitmap(
                        getRoundedCroppedBitmap(
                            decodeSampledBitmapFromUri(
                                binding.root.context, viewModel.payer!!.memberProfile, 30.dpToPx(resources.displayMetrics), 30.dpToPx(resources.displayMetrics)
                            )!!
                        )
                    )
                    binding.payerImageHolderView.visibility = View.INVISIBLE

                }

                // Updating member check box adapter
                membersCheckboxAdapter.updateMembers(members, viewModel.memberIds.toList())

                // Updating payer adapter
                binding.choosePayerCard.setOnClickListener {
                    openPayerBottomSheet(members)
                }

            }
        }

        //retain category and payer upon orientation change
        if (viewModel.category != null) {
            binding.chooseCategoryText.text =
                viewModel.category!!.name.lowercase().titleCase().translate(requireContext())
            // category set
            binding.categoryImageHolder.visibility = View.VISIBLE
            binding.categoryImageView.visibility = View.VISIBLE
            binding.categoryImageView.setImageResource(getCategoryDrawableResource(viewModel.category!!.ordinal))
            binding.categoryHolderImageView.visibility = View.INVISIBLE
        }

        if (viewModel.payer != null) {
            binding.choosePayerText.text = viewModel.payer!!.name
            // profile set
            binding.payerImageView.visibility = View.VISIBLE
            binding.payerImageView.setImageBitmap(
                getRoundedCroppedBitmap(
                    decodeSampledBitmapFromUri(
                        binding.root.context, viewModel.payer!!.memberProfile, 30.dpToPx(resources.displayMetrics), 30.dpToPx(resources.displayMetrics)
                    )!!
                )
            )
            binding.payerImageHolderView.visibility = View.INVISIBLE
        }


        // Choose category
        // Dropdown
        binding.chooseCategoryCard.setOnClickListener {
            openCategoryBottomSheet()
        }

        // Menu
        setHasOptionsMenu(true)

    }

    private fun updateExpense() {
        if (binding.expenseNameText.text?.trim()
                .toString() != "" && binding.expenseAmountText.text?.trim().toString() != ""
            && viewModel.category != null && viewModel.payer != null
        ) {
            if (viewModel.memberIds.isNotEmpty()) {

                if (checkEdited()) {
                    val builder = AlertDialog.Builder(requireContext())

                    builder.setMessage(getString(R.string.confirm_editing_expense))

                    builder.setPositiveButton(getString(R.string.confirm)) { dialog, which ->

                        viewModel.updateExpense(
                            binding.expenseNameText.text.toString(),
                            viewModel.category!!.ordinal,
                            viewModel.payer!!.memberId,
                            binding.expenseAmountText.text.toString().toFloat(),
                            viewModel.memberIds.toList()
                        ) { expenseId: Int ->
                            gotoExpenseDetailFragment(expenseId)
                        }
                        Snackbar.make(
                            binding.root,
                            getString(R.string.expense_updated),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                    builder.setNegativeButton(getString(R.string.cancel), null)

                    builder.show()

                }
                else{
                    Snackbar.make(
                        binding.root,
                        getString(R.string.expense_not_edited),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.atleast_1_payee),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        } else
            Snackbar.make(
                binding.root,
                getString(R.string.enter_all_fields_expense),
                Snackbar.LENGTH_SHORT
            ).show()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_expense_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.update_expense -> { //
                updateExpense()
                true
            }
            else -> {
                false
            }
        }
    }

    private fun openCategoryBottomSheet() {
        val categoryBottomSheetDialog = BottomSheetDialog(requireContext())
        categoryBottomSheetDialog.setContentView(R.layout.new_rv_bottom_sheet)

        categoryBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val categoryTitle =
            categoryBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val categoryRV =
            categoryBottomSheetDialog.findViewById<RecyclerView>(R.id.bottom_sheet_list)
        val categories = Category.values().toList()

        categoryTitle?.text = getString(R.string.select_category)

        val categoryAdapter = CategoryAdapter(categories) { category ->
            viewModel.category = category
            binding.chooseCategoryText.text =
                category.name.lowercase().titleCase().translate(requireContext())

            binding.categoryImageHolder.visibility = View.VISIBLE
            binding.categoryImageView.visibility = View.VISIBLE
            binding.categoryImageView.setImageResource(getCategoryDrawableResource(category.ordinal))
            binding.categoryHolderImageView.visibility = View.INVISIBLE
            categoryBottomSheetDialog.dismiss()
        }

        categoryRV?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryAdapter
        }

        categoryBottomSheetDialog.show()
    }

    private fun openPayerBottomSheet(payers: List<Member>) {
        val payerBottomSheetDialog = BottomSheetDialog(requireContext())
        payerBottomSheetDialog.setContentView(R.layout.new_rv_bottom_sheet)

        payerBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val payerTitle = payerBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val payerRv = payerBottomSheetDialog.findViewById<RecyclerView>(R.id.bottom_sheet_list)

        payerTitle?.text = getString(R.string.select_payer)

        val payerAdapter = PayerAdapter(payers) { payer ->
            viewModel.payer = payer
            binding.choosePayerText.text = payer.name
            // profile set
            binding.payerImageView.visibility = View.VISIBLE
            binding.payerImageView.setImageBitmap(
                getRoundedCroppedBitmap(
                    decodeSampledBitmapFromUri(
                        binding.root.context, viewModel.payer!!.memberProfile, 30.dpToPx(resources.displayMetrics), 30.dpToPx(resources.displayMetrics)
                    )!!
                )
            )
            binding.payerImageHolderView.visibility = View.INVISIBLE

            payerBottomSheetDialog.dismiss()
        }

        payerRv?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = payerAdapter
        }

        payerBottomSheetDialog.show()
    }

    private fun gotoExpenseDetailFragment(expenseId: Int) {
        val action =
            EditExpenseFragmentDirections.actionEditExpenseFragmentToExpenseDetailFragment(
                expenseId,
                args.groupId
            )
        findNavController().navigate(action)

    }

    private fun checkEdited(): Boolean {
        return !((args.expense.expenseName == binding.expenseNameText.text.toString()) &&
                (args.expense.totalAmount == binding.expenseAmountText.text.toString().toFloat()) &&
                (args.expense.category == viewModel.category!!.ordinal) &&
                (args.expense.payer == viewModel.payer!!.memberId) &&
                (getMemberIds(args.expensePayees.toList()).toList() == viewModel.memberIds.toList()))
    }

}