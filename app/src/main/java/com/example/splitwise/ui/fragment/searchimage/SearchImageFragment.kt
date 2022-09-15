package com.example.splitwise.ui.fragment.searchimage

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentSearchImageBinding
import com.example.splitwise.ui.fragment.adapter.UnsplashPhotoAdapter
import com.example.splitwise.ui.fragment.adapter.UnsplashPhotoLoadStateAdapter
import com.example.splitwise.ui.fragment.settings.SettingsViewModelFactory
import com.example.splitwise.util.removeIrrelevantWords
import com.google.android.material.transition.MaterialElevationScale

class SearchImageFragment : Fragment() {
    private lateinit var binding: FragmentSearchImageBinding
    private val viewModel: SearchImageViewModel by viewModels() {
        SearchImageViewModelFactory(args.groupName)
    }
    private val args: SearchImageFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.search)
        return inflater.inflate(R.layout.fragment_search_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // start enter transition only when data loaded, and just started to draw
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        //

        // Moving it to viewmodel init
        //viewModel.showRelatedGroupIcons(removeIrrelevantWords(getGroupName()))

        binding = FragmentSearchImageBinding.bind(view)

        val adapter = UnsplashPhotoAdapter { photo, View ->
            photo?.let {
                gotoSetImageFragment(it.urls.regular, View)
            }
        }

        binding.apply {
            recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            recyclerView.adapter = adapter.withLoadStateHeaderAndFooter(
                header = UnsplashPhotoLoadStateAdapter { adapter.retry() },
                footer = UnsplashPhotoLoadStateAdapter { adapter.retry() }
            )
        }

        adapter.addLoadStateListener { loadState ->
            binding.apply {
                progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                recyclerView.isVisible = loadState.source.refresh is LoadState.NotLoading
                buttonRetry.isVisible = loadState.source.refresh is LoadState.Error
                textViewError.isVisible = loadState.source.refresh is LoadState.Error

                // empty view
                if (loadState.source.refresh is LoadState.NotLoading &&
                    loadState.append.endOfPaginationReached &&
                    adapter.itemCount < 1
                ) {
                    recyclerView.isVisible = false
                    textViewEmpty.isVisible = true
                } else {
                    textViewEmpty.isVisible = false
                }
            }
        }


        viewModel.photos.observe(viewLifecycleOwner) { photos ->
//            Log.d(ContentValues.TAG, "onCreate: ${photos}")
//            val x = listOf(photos)
//            Log.d(ContentValues.TAG, "onCreate: ${x.size}")
            adapter.submitData(this.lifecycle, photos)
        }

        // menu
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.search_image_menu, menu)

        val searchItem = menu.findItem(R.id.search_menu)
        val searchView = searchItem?.actionView as SearchView

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                if (query != null) {
                    binding.recyclerView.scrollToPosition(0)
                    viewModel.searchPhotos(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    private fun gotoSetImageFragment(imageUrl: String, imageView: View) {

        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        val transitionName = getString(R.string.set_image_transition_name)
        val extras = FragmentNavigatorExtras(imageView to transitionName)
        val action =
            SearchImageFragmentDirections.actionSearchImageFragmentToSetImageFragment(imageUrl, args.groupId,
                args.fromGroupsFragment, args.groupName, args.fromGroupsSearchFragment)
        findNavController().navigate(action, extras)
    }

}