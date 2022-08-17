package com.example.splitwise.ui.fragment.searchgroup

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentSearchGroupBinding
import com.example.splitwise.ui.fragment.adapter.FilteredGroupsAdapter
import com.example.splitwise.ui.fragment.adapter.GroupsAdapter
import com.example.splitwise.util.CustomOnBackPressed

class SearchGroupFragment : Fragment(), CustomOnBackPressed {

    private lateinit var binding: FragmentSearchGroupBinding
    private val viewModel: SearchGroupViewModel by viewModels {
        SearchGroupViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_group, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //viewModel.fetchData()

        binding = FragmentSearchGroupBinding.bind(view)

        // Rv
        val groupsAdapter = FilteredGroupsAdapter { groupId: Int ->
            goToExpenseFragment(groupId)
        }

        binding.searchGroupsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = groupsAdapter
        }

        // Livedata
        viewModel.groups.observe(viewLifecycleOwner) { groups ->
            if (groups != null && groups.isNotEmpty()) {
                Log.d(TAG, "onViewCreated:$$$ groups != null && groups.isNotEmpty()")
                groupsAdapter.updateGroups(groups, viewModel.textEntered)
                binding.searchGroupsRecyclerView.visibility = View.VISIBLE
                binding.noResultImage.visibility = View.GONE
            } else if(groups != null && groups.isEmpty() ) { // to handle when query not entered
                Log.d(TAG, "onViewCreated:$$$ groups != null && groups.isEmpty()")
                groupsAdapter.updateGroups(listOf(), "")
                binding.searchGroupsRecyclerView.visibility = View.GONE
                binding.noResultImage.visibility = View.VISIBLE
            }
            else{
                Log.d(TAG, "onViewCreated:$$$ else")
                binding.searchGroupsRecyclerView.visibility = View.GONE
                binding.noResultImage.visibility = View.GONE
            }

        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val query = binding.toolbar.searchGroupText.text?.trim().toString()
                    Log.d(TAG, "afterTextChanged:search query changes")
                    viewModel.textEntered = query
                    viewModel.fetchData()
            }
        }
        binding.toolbar.searchGroupText.addTextChangedListener(watcher)

        binding.toolbar.materialButton.setOnClickListener {
            goToGroupsFragment()
        }

//        binding.groupSearchView.setOnQueryTextListener( object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                Log.d(TAG, "onQueryTextSubmit: query submitted")
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                Log.d(TAG, "onQueryTextChange: query text changed")
//                return true
//                // false if the SearchView should perform the default action of showing any suggestions
//            // if available, true if the action was handled by the listener.
//            }
//
//        })
    }

    private fun goToExpenseFragment(groupId: Int) {
        val action =
            SearchGroupFragmentDirections.actionSearchGroupFragmentToExpensesFragment(groupId)
        view?.findNavController()?.navigate(action)
    }

    private fun goToGroupsFragment() {
        activity?.onBackPressed()
     //   activity?.supportFragmentManager?.popBackStackImmediate()
//        val action =
//            SearchGroupFragmentDirections.actionSearchGroupFragmentToGroupsFragment()
//        view?.findNavController()?.navigate(action)
    }



    @SuppressLint("RestrictedApi")
    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.hide()

    }

    @SuppressLint("RestrictedApi")
    override fun onStop() {
        super.onStop()
        (activity as AppCompatActivity).supportActionBar?.show()
    }

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed: custom back press")
        Toast.makeText(requireContext(), "Custom back pressed", Toast.LENGTH_SHORT).show()
    }

}