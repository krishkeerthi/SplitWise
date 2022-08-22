package com.example.splitwise.ui.fragment.searchimage

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentSearchImageBinding
import com.example.splitwise.ui.fragment.adapter.UnsplashPhotoAdapter
import com.example.splitwise.ui.fragment.adapter.UnsplashPhotoLoadStateAdapter
import com.example.splitwise.ui.fragment.settings.SettingsViewModelFactory
import com.example.splitwise.util.removeIrrelevantWords

class SearchImageFragment : Fragment() {
    private lateinit var binding: FragmentSearchImageBinding
    private val viewModel: SearchImageViewModel by viewModels() //{
//        SettingsViewModelFactory(requireContext())
//    }
    private val args: SearchImageFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search_image, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.showRelatedGroupIcons(removeIrrelevantWords(args.groupName))

        binding = FragmentSearchImageBinding.bind(view)

        val adapter = UnsplashPhotoAdapter { photo ->
            photo?.let {
                gotoSetImageFragment(it.urls.regular)
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

    private fun gotoSetImageFragment(imageUrl: String) {
        val action =
            SearchImageFragmentDirections.actionSearchImageFragmentToSetImageFragment(imageUrl, args.groupId)
        view?.findNavController()?.navigate(action)
    }

}