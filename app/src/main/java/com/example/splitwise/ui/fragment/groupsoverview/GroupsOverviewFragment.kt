package com.example.splitwise.ui.fragment.groupsoverview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentGroupsOverviewBinding
import com.example.splitwise.ui.fragment.adapter.GroupsOverviewAdapter
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.transition.MaterialElevationScale

class GroupsOverviewFragment : Fragment() {

    private lateinit var binding: FragmentGroupsOverviewBinding

    private val viewModel: GroupsOverviewViewModel by viewModels {
        GroupsOverviewViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.group_overview)
        return inflater.inflate(R.layout.fragment_groups_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentGroupsOverviewBinding.bind(view)

//        requireActivity().title = "Groups Overview"
//
//        // Rv
//        val groupsAdapter = GroupsOverviewAdapter { groupId: Int ->
//            gotoExpensesOverviewFragment(groupId)
//        }
//
//        binding.groupOverviewRecyclerView.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = groupsAdapter
//        }
//
//        // Livedata Groups overview
//        viewModel.groups.observe(viewLifecycleOwner) { groups ->
//            if (groups != null) {
//                groupsAdapter.updateGroups(groups)
//            }
//        }
//
//        binding.searchButton.setOnClickListener {
//           // gotoSearchImageFragment()
//        }
//
//
//        viewModel.pieEntries.observe(viewLifecycleOwner){ pieEntries ->
//            if(pieEntries != null && pieEntries.isNotEmpty()){
//                val dataSet = PieDataSet(pieEntries, "Categories")
//                dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList()
//                val data = PieData(dataSet)
//
//                binding.piechart.apply {
//                    holeRadius = 0F
//                    transparentCircleRadius = 0F
//                }
//
//                binding.piechart.data = data
//                binding.piechart.invalidate()
//
//                binding.progressBar.visibility = View.INVISIBLE
//                binding.piechart.visibility = View.VISIBLE
//            }
//            else{
//                binding.progressBar.visibility = View.VISIBLE
//                binding.piechart.visibility = View.INVISIBLE
//            }
//        }
    }

    private fun gotoExpensesOverviewFragment(groupId: Int) {
        val action =
            GroupsOverviewFragmentDirections.actionGroupsOverviewFragmentToExpensesOverviewFragment(
                groupId
            )
        view?.findNavController()?.navigate(action)
    }

//    private fun gotoSearchImageFragment() {
//        val action =
//            GroupsOverviewFragmentDirections.actionGroupsOverviewFragmentToSearchImageFragment()
//        view?.findNavController()?.navigate(action)
//    }

}