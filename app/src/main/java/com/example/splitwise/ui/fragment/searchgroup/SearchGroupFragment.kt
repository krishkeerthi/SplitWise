package com.example.splitwise.ui.fragment.searchgroup

import android.app.SearchManager
import android.content.ContentValues.TAG
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.doOnPreDraw
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentSearchGroupBinding
import com.example.splitwise.ui.fragment.adapter.FilteredGroupsAdapter
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.hideKeyboard
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis
import java.lang.reflect.Field


class SearchGroupFragment : Fragment() {

    private lateinit var binding: FragmentSearchGroupBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val viewModel: SearchGroupViewModel by activityViewModels {
        SearchGroupViewModelFactory(requireContext())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }


        val callback = object : OnBackPressedCallback(true /* enabled by default */) {

            override fun handleOnBackPressed() {
                viewModel.textEntered = ""

                Log.d(TAG, "handleOnBackPressed: back pressed called")
                // to hide keyboard
                binding.root.hideKeyboard()

                // for navigation
                NavHostFragment.findNavController(this@SearchGroupFragment)
                    .popBackStack()
            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.search)
        return inflater.inflate(R.layout.fragment_search_group, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        sharedPreferences = requireActivity().getSharedPreferences(
//            "com.example.splitwise.sharedprefkey",
//            Context.MODE_PRIVATE
//        )

        // start enter transition only when data loaded, and just started to draw
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        //


        binding = FragmentSearchGroupBinding.bind(view)

        // Rv
        val groupsAdapter = FilteredGroupsAdapter({ groupId: Int, searchGroupView: View ->
            goToExpenseFragment(groupId, searchGroupView)
        },
            { groupId: Int, groupIcon: String?, groupName: String, searchGroupImageView: View ->
                gotoGroupIconFragment(
                    groupId,
                    groupIcon,
                    groupName,
                    searchGroupImageView
                )
            }
        )

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
                binding.noGroupsTextView.visibility = View.GONE
            } else if (groups != null && groups.isEmpty()) { // to handle when query not entered
                Log.d(TAG, "onViewCreated:$$$ groups != null && groups.isEmpty()")
                groupsAdapter.updateGroups(listOf(), "")
                binding.searchGroupsRecyclerView.visibility = View.GONE
                binding.noResultImage.visibility = View.VISIBLE
                binding.noGroupsTextView.visibility = View.VISIBLE
            } else {
                Log.d(TAG, "onViewCreated:$$$ else")
                binding.searchGroupsRecyclerView.visibility = View.GONE
                binding.noResultImage.visibility = View.GONE
                binding.noGroupsTextView.visibility = View.GONE
            }

        }


        // Search bar
//        val watcher = object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
//            }
//
//            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//            }
//
//            override fun afterTextChanged(s: Editable?) {
//                val query = binding.toolbar.searchGroupText.text?.trim().toString()
//                    Log.d(TAG, "afterTextChanged:search query changes")
//                    viewModel.textEntered = query
//                    viewModel.fetchData()
//            }
//        }
//        binding.toolbar.searchGroupText.addTextChangedListener(watcher)
//
//
//        // back button
//        binding.toolbar.materialButton.setOnClickListener {
//            goToGroupsFragment()
//        }

        // menu
        setHasOptionsMenu(true)

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.search_group_menu, menu)

        val searchItem = menu.findItem(R.id.search_menu)
        val searchView = searchItem?.actionView as SearchView

        searchItem.expandActionView()
//        searchItem.setIcon(null)
//        searchView.invalidateOutline()
        searchView.updatePadding(left = (-16).dpToPx(resources.displayMetrics))
        Log.d(TAG, "onCreateOptionsMenu: ${searchItem.itemId}")
        // for expanded state
        //searchView.setIconifiedByDefault(false) // it shows search icon
        searchView.isIconified = false

        val closeButton =
            searchView.findViewById<ImageView>(androidx.appcompat.R.id.search_close_btn)

        searchView.requestFocus()

        // setting searchview cursor color (not working)

//        val manager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
//        searchView.setSearchableInfo(manager.getSearchableInfo(requireActivity().componentName))
//
//        val searchTextView = searchView.findViewById(androidx.appcompat.R.id.search_src_text) as AutoCompleteTextView
//        try {
//            val mCursorDrawableRes: Field =
//                TextView::class.java.getDeclaredField("mCursorDrawableRes")
//            mCursorDrawableRes.isAccessible = true
//            mCursorDrawableRes.set(
//                searchTextView,
//                R.drawable.cursor
//            ) //This sets the cursor resource ID to 0 or @null which will make it visible on white background
//        } catch (e: Exception) {
//        }
        // ends here

        //searchView.fitsSystemWindows = true
        //searchView.isEnabled = true

        searchView.setQuery(viewModel.textEntered, true)

        if (viewModel.textEntered == "")
            closeButton.visibility = View.INVISIBLE

        viewModel.fetchData()

//        val query = sharedPreferences.getString("GROUPQUERY", "")
//        if(query != "")
//            searchView.setQuery(query, true)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.hideKeyboard()
                //viewModel.fetchData()
                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if (query != null) {
                    closeButton.visibility = if (query == "") View.INVISIBLE else View.VISIBLE

                    viewModel.textEntered = query
                    // saving query in shared preference
                    //changeGroupQuery(query)
                    viewModel.fetchData()
                }

                return true
            }
        })
    }

    private fun goToExpenseFragment(groupId: Int, searchGroupView: View) {
        view?.hideKeyboard()

        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }


        val expensesTransitionName = getString(R.string.expenses_transition_name)
        val extras = FragmentNavigatorExtras(searchGroupView to expensesTransitionName)


        val action =
            SearchGroupFragmentDirections.actionSearchGroupFragmentToExpensesFragment(groupId)
        findNavController().navigate(action, extras)
    }

    private fun gotoGroupIconFragment(groupId: Int, groupIcon: String?, groupName: String, searchGroupImageView: View) {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        val transitionName = getString(R.string.group_icon_transition_name)
        val extras = FragmentNavigatorExtras(searchGroupImageView to transitionName)


        val action = SearchGroupFragmentDirections.actionSearchGroupFragmentToGroupIconFragment(
            groupId,
            groupIcon,
            groupName,
            true,
            true,
            false
        )
        findNavController().navigate(action, extras)
    }

    private fun changeGroupQuery(query: String) {
        with(sharedPreferences.edit()) {
            putString("GROUPQUERY", query)
            apply()
        }
    }

    private fun goToGroupsFragment() {
        activity?.onBackPressed()
        //   activity?.supportFragmentManager?.popBackStackImmediate()
//        val action =
//            SearchGroupFragmentDirections.actionSearchGroupFragmentToGroupsFragment()
//        view?.findNavController()?.navigate(action)
    }


//    // Below methods not in use
//    @SuppressLint("RestrictedApi")
//    override fun onResume() {
//        super.onResume()
//        (activity as AppCompatActivity).supportActionBar?.hide()
//
//    }
//
//    @SuppressLint("RestrictedApi")
//    override fun onStop() {
//        super.onStop()
//        (activity as AppCompatActivity).supportActionBar?.show()
//    }

//    override fun onBackPressed() {
//        Log.d(TAG, "onBackPressed: custom back press")
//        //Toast.makeText(requireContext(), "Custom back pressed", Toast.LENGTH_SHORT).show()
//    }

}