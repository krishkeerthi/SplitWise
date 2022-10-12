package com.example.splitwise.ui.fragment.addexpense

import android.content.ContentValues.TAG
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
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentAddExpenseBinding
import com.example.splitwise.ui.fragment.adapter.CategoryAdapter
import com.example.splitwise.ui.fragment.adapter.MembersCheckboxAdapter
import com.example.splitwise.ui.fragment.adapter.PayerAdapter
import com.example.splitwise.util.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale

class AddExpenseFragment : Fragment() {

    private lateinit var binding: FragmentAddExpenseBinding
    private val args: AddExpenseFragmentArgs by navArgs()

    private val viewModel: AddExpenseViewModel by viewModels {
        AddExpenseViewModelFactory(requireContext(), args.groupId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        sharedElementEnterTransition = MaterialContainerTransform().apply {
//            drawingViewId = R.id.nav_host_fragment_container
//            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
//            scrimColor = resources.getColor(R.color.view_color)//Color.TRANSPARENT
//            setAllContainerColors(resources.getColor(R.color.background))
//        }
        enterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }


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

        // Rv
        val membersCheckboxAdapter = MembersCheckboxAdapter { memberId: Int, isChecked: Boolean ->
            if (isChecked) {
                viewModel.memberIds.add(memberId)
                Log.d(TAG, "onViewCreated: expense members size ${viewModel.memberIds.size}")
            }
            else {
                viewModel.memberIds.remove(memberId)
                Log.d(TAG, "onViewCreated: expense members size ${viewModel.memberIds.size}")
            }
        }

        val spanCount = if(resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) 3 else 8
        binding.membersRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = membersCheckboxAdapter
        }

        // Livedata
        viewModel.members.observe(viewLifecycleOwner) { members ->

            if (members != null) {
                // Updating member check box adapter
                membersCheckboxAdapter.updateMembers(members, viewModel.memberIds.toList())

                // Updating payer adapter
                binding.choosePayerCard.setOnClickListener {
                    openPayerBottomSheet(members)
                }

            }
        }

        //retain category and payer upon orientation change

        if(viewModel.category != null){
            binding.chooseCategoryText.text = viewModel.category!!.name.lowercase().titleCase().translate(requireContext())

            // category set
            binding.categoryImageHolder.visibility = View.VISIBLE
            binding.categoryImageView.visibility = View.VISIBLE
            binding.categoryImageView.setImageResource(getCategoryDrawableResource(viewModel.category!!.ordinal))
            binding.categoryHolderImageView.visibility = View.INVISIBLE

        }

        if(viewModel.payer != null){
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
            && viewModel.category != null && viewModel.payer != null
        ) {
            if(viewModel.memberIds.isNotEmpty()){

                val builder = AlertDialog.Builder(requireContext())

                builder.setMessage(getString(R.string.confirm_adding_expense))

                builder.setPositiveButton(getString(R.string.confirm)){ dialog, which ->
                    viewModel.createExpense(
                        binding.expenseNameText.text.toString(),
                        viewModel.category!!.ordinal,
                        viewModel.payer!!.memberId,
                        binding.expenseAmountText.text.toString().toFloat(),
                        viewModel.memberIds.toList()
                    ) {
                        gotoExpenseFragment()
                    }
                    Snackbar.make(binding.root, getString(R.string.expense_added), Snackbar.LENGTH_SHORT).show()
                }

                builder.setNegativeButton(getString(R.string.cancel), null)

                builder.show()

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
        categoryBottomSheetDialog.setContentView(R.layout.new_rv_bottom_sheet)

        categoryBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val categoryTitle =
            categoryBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val categoryRV = categoryBottomSheetDialog.findViewById<RecyclerView>(R.id.bottom_sheet_list)
        val categories = Category.values().toList()

        categoryTitle?.text = getString(R.string.select_category)

        //Adapter
        //val categoryAdapter = CategoryArrayAdapter(requireContext(), R.layout.icon_bottom_sheet_item, categories)
        val categoryAdapter = CategoryAdapter(categories){ category ->
            viewModel.category = category
            binding.chooseCategoryText.text = category.name.lowercase().titleCase().translate(requireContext())

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
        payerBottomSheetDialog.setContentView(R.layout.new_rv_bottom_sheet)

        payerBottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        val payerTitle = payerBottomSheetDialog.findViewById<TextView>(R.id.bottom_sheet_title)
        val payerRv = payerBottomSheetDialog.findViewById<RecyclerView>(R.id.bottom_sheet_list)

        payerTitle?.text = getString(R.string.select_payer)
        //Adapter
//        val payerAdapter = PayerArrayAdapter(requireContext(), R.layout.icon_bottom_sheet_item, payers)
        val payerAdapter = PayerAdapter(payers){ payer ->
            viewModel.payer = payer
            binding.choosePayerText.text = payer.name
            // profile set
            binding.payerImageView.visibility = View.VISIBLE
            binding.payerImageView.setImageBitmap(
                getRoundedCroppedBitmap(
                    decodeSampledBitmapFromUri(
                        binding.root.context, payer.memberProfile, 30.dpToPx(resources.displayMetrics), 30.dpToPx(resources.displayMetrics)
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