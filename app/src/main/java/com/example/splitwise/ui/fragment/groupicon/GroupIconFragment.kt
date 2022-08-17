package com.example.splitwise.ui.fragment.groupicon

import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.splitwise.R
import com.example.splitwise.databinding.BottomSheetBinding
import com.example.splitwise.databinding.FragmentGroupIconBinding
import com.example.splitwise.ui.fragment.groups.GroupsFragmentDirections
import com.google.android.material.bottomsheet.BottomSheetDialog

class GroupIconFragment : Fragment() {

    private lateinit var binding: FragmentGroupIconBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_icon, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentGroupIconBinding.bind(view)


        // menu
        setHasOptionsMenu(true)
    }

    private fun openBottomSheet(){
        val groupIconBottomSheet = BottomSheetDialog(requireContext())
        groupIconBottomSheet.setContentView(R.layout.edit_group_icon)

        val webImage = groupIconBottomSheet.findViewById<CardView>(R.id.web_image_card)
        val cameraImage = groupIconBottomSheet.findViewById<CardView>(R.id.camera_image_card)
        val galleryImage = groupIconBottomSheet.findViewById<CardView>(R.id.gallery_image_card)

        webImage?.setOnClickListener{
            gotoSearchImageFragment()
            groupIconBottomSheet.dismiss()
        }

        cameraImage?.setOnClickListener {
            Toast.makeText(requireContext(), "Camera Clicked", Toast.LENGTH_SHORT).show()
        }

        galleryImage?.setOnClickListener {
            Toast.makeText(requireContext(), "Gallery Clicked", Toast.LENGTH_SHORT).show()
        }

        groupIconBottomSheet.show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.group_icon_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.edit_menu ->{
                openBottomSheet()
                true
            }
            else ->{
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun gotoSearchImageFragment(){
        val action = GroupIconFragmentDirections.actionGroupIconFragmentToSearchImageFragment()
        view?.findNavController()?.navigate(action)
    }
}