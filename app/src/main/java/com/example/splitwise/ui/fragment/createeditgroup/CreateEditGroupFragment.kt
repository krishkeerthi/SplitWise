package com.example.splitwise.ui.fragment.createeditgroup

import android.content.ContentValues.TAG
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.transition.Slide
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentCreateEditGroupBinding
import com.example.splitwise.ui.activity.main.MainActivity
import com.example.splitwise.ui.fragment.adapter.GroupMembersAdapter
import com.example.splitwise.ui.fragment.groups.GroupsFragment
import com.example.splitwise.ui.fragment.viewmodel.CreateEditGroupActivityViewModel
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.nameCheck
import com.example.splitwise.util.themeColor
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis

class CreateEditGroupFragment : Fragment() {

    private lateinit var binding: FragmentCreateEditGroupBinding
    private val args: CreateEditGroupFragmentArgs by navArgs()

    //    private lateinit var viewModel: CreateEditGroupViewModel
    private val viewModel: CreateEditGroupViewModel by activityViewModels { // checking with activity viewmodel
        CreateEditGroupViewModelFactory(requireContext(), args.groupId, args.selectedMembers)
    }

    private val activityViewModel: CreateEditGroupActivityViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // viewmodel initialization with provider
//        val factory = CreateEditGroupViewModelFactory(requireContext(), args.groupId, args.selectedMembers)
//        viewModel = ViewModelProvider(requireActivity(), factory)[CreateEditGroupViewModel::class.java]
        // ends here

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment_container
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor = resources.getColor(R.color.view_color)//Color.TRANSPARENT
            setAllContainerColors(resources.getColor(R.color.background))
        }

        val callback = object : OnBackPressedCallback(true /* enabled by default */) {

            override fun handleOnBackPressed() {
                if (viewModel.change) {
                    val builder = AlertDialog.Builder(requireContext())

                    builder.setTitle(getString(R.string.discard));
                    builder.setMessage(getString(R.string.discard_changes))
                    builder.setPositiveButton(
                        getString(R.string.discard)
                    ) { dialog, which ->

                        // clear activity viewmodel selected members
                        //      activityViewModel.selectedMembers = listOf()

                        viewModel.reset()
                        //  Toast.makeText(requireContext(), "custom back pressed", Toast.LENGTH_SHORT).show()
                        NavHostFragment.findNavController(this@CreateEditGroupFragment)
                            .popBackStack()
                    }
                    builder.setNegativeButton(getString(R.string.cancel), null)
                    builder.show()
                } else {
                    viewModel.reset()
                    Log.d(TAG, "handleOnBackPressed: reset back pressed")
                    // reset viewmodel data, because this viewmodel is activity viewmodel now
                    NavHostFragment.findNavController(this@CreateEditGroupFragment)
                        .popBackStack()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        if (args.groupId != -1)
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                getString(R.string.edit_group)
        else
            (requireActivity() as AppCompatActivity).supportActionBar?.title =
                getString(R.string.create_group)

        val binding = FragmentCreateEditGroupBinding.inflate(inflater, container, false)

        return binding.root.apply {
            transitionName = "create_edit_group_transition"
        }
        //return inflater.inflate(R.layout.fragment_create_edit_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        // start enter transition only when data loaded, and just started to draw
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        //

        binding = FragmentCreateEditGroupBinding.bind(view)


        // transition code starts

//        val startingView = getStartView()
//        enterTransition = MaterialContainerTransform().apply {
//            startView = startingView
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

        // corner color blue fix
//        binding.groupImageHolder.background = ShapeDrawable(RectShape()).also {
//            it.paint.color = resources.getColor(R.color.view_color)
//        }
        // ends

        //viewModel.fetchData()
        // many problem arises if it is not called from here
        viewModel.updatedFetchData(args.groupId, args.selectedMembers?.toList())

        // not works
//        (requireActivity() as MainActivity).actionBar?.title = if(args.groupId == -1) "Create group"
//        else "Edit group"

        Log.d(
            TAG,
            "onCreateDialog: membercheck inside create edit grop , ${viewModel.members.value}"
        )

        Log.d(TAG, "onViewCreated: createdit group id : ${args.groupId}")
        super.onViewCreated(view, savedInstanceState)

        if (args.groupId == -1)
            requireActivity().title = "Create Group"
        else
            requireActivity().title = "Edit Group"

        // Rv
        val membersAdapter = GroupMembersAdapter { memberId, memberView ->
            memberClicked(memberId, memberView)
        }

        binding.groupMembersRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = membersAdapter
        }

        // Livedata Members
        viewModel.members.observe(viewLifecycleOwner) { members ->
            Log.d(TAG, "onViewCreated: members livedata $members")
            if (members != null) {
                Log.d(TAG, "onViewCreated: addMember observed $members")
                membersAdapter.updateMembers(members)

                // Also update selected members this stores the added members before creating group
                //       activityViewModel.selectedMembers = members
            }
        }

        // Livedata member exists
        viewModel.exists.observe(viewLifecycleOwner) { exists ->
            if (exists) {
                Snackbar.make(
                    binding.root,
                    getString(R.string.member_exists_already),
                    Snackbar.LENGTH_SHORT
                ).show()
                viewModel.exists.value = false
            }
        }

        //Group name
//        viewModel.groupName.observe(viewLifecycleOwner) { groupName ->
//            Log.d(TAG, "onViewCreated: groupName livedata $groupName")
//            groupName?.let {
//                binding.groupNameText.setText(it)
//            }
//        }

        //      group icon click (when not set)
        binding.groupImageHolder.setOnClickListener {
            if (binding.groupImageHolderImage.isVisible)
                gotoGroupIconFragment(binding.groupImageHolderImage)
            Log.d(TAG, "onViewCreated: img click holder")
        }

//        binding.groupImageHolderImage.setOnClickListener {
//            gotoGroupIconFragment(binding.groupImageHolderImage)
//        }
        // group icon click (when set)
        binding.groupImageView.setOnClickListener {
            gotoGroupIconFragment(binding.groupImageView)
            Log.d(TAG, "onViewCreated: img click image")
        }

        viewModel.group.observe(viewLifecycleOwner) { group ->
            if (group != null) {
                Log.d(TAG, "onViewCreated: name set")
                binding.groupNameText.setText(group.groupName)

                if (group.groupIcon != null) {
                    Log.d(TAG, "onViewCreated: image set")
                    ///binding.groupImageView.setImageURI(group.groupIcon)

                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.groupImageView.setImageBitmap(
                            decodeSampledBitmapFromUri(
                                binding.root.context,
                                group.groupIcon,
                                160.dpToPx(resources.displayMetrics),
                                160.dpToPx(resources.displayMetrics)
                            )
                        )
                    }, resources.getInteger(R.integer.reply_motion_duration_large).toLong())

                    binding.groupImageHolder.visibility = View.INVISIBLE
                    binding.groupImageHolderImage.visibility = View.INVISIBLE
                    binding.groupImageView.visibility = View.VISIBLE
                } else {
                    binding.groupImageHolder.visibility = View.VISIBLE
                    binding.groupImageHolderImage.visibility = View.VISIBLE
                    binding.groupImageView.visibility = View.INVISIBLE
                }
            }
        }

        // set image while creating group
//        if(args.groupIcon != null)
//            binding.groupImageView.setImageURI(Uri.parse(args.groupIcon))

        Log.d(TAG, "onViewCreated: group Icon create edit${args.groupIcon}")

        if (args.groupIcon != null) {
            ///binding.groupImageView.setImageURI(Uri.parse(args.groupIcon))
            Handler(Looper.getMainLooper()).postDelayed({
                binding.groupImageView.setImageBitmap(
                    decodeSampledBitmapFromUri(
                        binding.root.context,
                        Uri.parse(args.groupIcon),
                        160.dpToPx(resources.displayMetrics),
                        160.dpToPx(resources.displayMetrics)
                    )
                )
            }, resources.getInteger(R.integer.reply_motion_duration_large).toLong())

            binding.groupImageHolder.visibility = View.INVISIBLE
            binding.groupImageHolderImage.visibility = View.INVISIBLE
            binding.groupImageView.visibility = View.VISIBLE

        } else {
            binding.groupImageHolder.visibility = View.VISIBLE
            binding.groupImageHolderImage.visibility = View.VISIBLE
            binding.groupImageView.visibility = View.INVISIBLE
        }

        // adding data   if (args.groupIcon != null && viewModel.members.value == null)
        ////            if (activityViewModel.selectedMembers.isNotEmpty())
        ////                viewto activity viewmodel only when not added previously
//      Model.updateMembers(activityViewModel.selectedMembers)

        // retains group name while returning from choose members screen
        if (args.groupName != null) {
            binding.groupNameText.setText(args.groupName)
//            if (args.groupName.toString().trim() != "")
//                binding.createGroupButton.visibility = View.VISIBLE
        }
//
//        // retains group image while returning from choose members screen
//        viewModel.group.value?.let { group ->
//            binding.groupImageView.setImageURI(group.groupIcon)
//            binding.groupImageHolder.visibility = View.INVISIBLE
//            binding.groupImageHolderImage.visibility = View.INVISIBLE
//            binding.groupImageView.visibility = View.VISIBLE
//        }

        // Button click
//        binding.addMemberButton.setOnClickListener {
//            AddMemberDialog(viewModel).show(childFragmentManager, "Add Member Alert Dialog")
//            // open dialog to add new member.
//            // Choosing existing member has to be implemented
//        }

        binding.chooseMemberButton.setOnClickListener {
            gotoChooseMembersFragment()
        }


        // if group id is not null
        // make sure group name is not editable
        if (args.groupId != -1) {
            binding.groupNameText.isClickable = false
            binding.groupNameText.isFocusable = false
        }


//        binding.createGroupButton.setOnClickListener {
//            createGroup()
//        }

        val nameWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (nameCheck(binding.groupNameText.text?.trim().toString())) {
                    binding.outlinedGroupNameTextField.error = null
                    binding.outlinedGroupNameTextField.isErrorEnabled = false

                    if (args.groupId == -1)
                        viewModel.change = true
                } else {
                    binding.outlinedGroupNameTextField.error = getString(R.string.enter_valid_name)

                    if (!viewModel.memberCountChange)
                        viewModel.change = false
                }

//                binding.createGroupButton.visibility =
//                    if (nameCheck(
//                            binding.groupNameText.text?.trim().toString()
//                        ) && args.groupId == -1
//                    )
//                        View.VISIBLE
//                    else
//                        View.GONE
            }

        }

        binding.groupNameText.addTextChangedListener(nameWatcher)

        // Menu

        setHasOptionsMenu(true)

    }

    private fun getStartView(): View? {

        return if (args.groupId == -1)
            activity?.findViewById(R.id.add_group_fab)
        else
            activity?.findViewById(R.id.add_member_button)
    }

    private fun memberClicked(memberId: Int, memberView: View) {
        if (args.groupId != -1) {

            gotoMemberProfileFragment(memberId, memberView)
        }
    }

    private fun gotoMemberProfileFragment(memberId: Int, memberView: View) {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        val transitionName = getString(R.string.member_profile_transition_name)
        val extras = FragmentNavigatorExtras(memberView to transitionName)

        val action =
            CreateEditGroupFragmentDirections.actionCreateEditGroupFragmentToMemberProfileFragment(
                memberId,
                args.groupId,
                args.groupName,
                args.groupIcon
            )
        findNavController().navigate(action, extras)
    }

    private fun createGroup() {
        if (nameCheck(binding.groupNameText.text?.trim().toString())) {

            if (viewModel.getMembersSize() > 1) {
                val memberId = requireActivity().getSharedPreferences(
                    MainActivity.KEY,
                    Context.MODE_PRIVATE
                )
                    .getInt("MEMBERID", -1)

                viewModel.createGroup(
                    binding.groupNameText.text.toString(),
                    memberId,
                    args.groupIcon
                ) {
                    gotoGroupsFragment()
                }

                Snackbar.make(
                    binding.root,
                    getString(R.string.group_created_successfully),
                    Snackbar.LENGTH_SHORT
                ).show()

                // activityViewModel.selectedMembers = listOf()
                // gotoGroupsFragment()
                // calling here goes to groups fragment without completing the group creation process
            } else
                Snackbar.make(
                    binding.root,
                    getString(R.string.add_atleast_2_group),
                    Snackbar.LENGTH_SHORT
                ).show()

        } else {
            Snackbar.make(
                binding.root,
                getString(R.string.group_name_missing),
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.create_edit_fragment_menu, menu)

        menu.findItem(R.id.create_group).isVisible = args.groupId == -1

        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_member -> {
                //AddMemberDialog(viewModel).show(childFragmentManager, "Add Member Alert Dialog")
                gotoAddMemberFragment()
                true
            }
            R.id.create_group -> {
                createGroup()
                true
            }
            else -> {
                false
            }
        }
    }

    private fun gotoAddMemberFragment() {

        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        val action =
            CreateEditGroupFragmentDirections.actionCreateEditGroupFragmentToAddMemberFragment(
                args.groupId,
                binding.groupNameText.text.toString(), //args.groupName,
                args.groupIcon
            )

        view?.findNavController()?.navigate(action)
    }

    private fun gotoChooseMembersFragment() {
        exitTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        Log.d(TAG, "onCreateDialog: membercheck choose member clicked, ${viewModel.members.value}")
        val action =
            CreateEditGroupFragmentDirections.actionCreateEditGroupFragmentToChooseMembersFragment(
                args.groupId,
                viewModel.members.value?.toTypedArray(),
                binding.groupNameText.text.toString(),
                args.groupIcon
            )
        view?.findNavController()?.navigate(action)
    }

    private fun gotoGroupsFragment() {
        // reset after creating group
        viewModel.reset()
        Log.d(TAG, "gotoGroupsFragment: called group created")
        val action =
            CreateEditGroupFragmentDirections.actionCreateEditGroupFragmentToGroupsFragment()
        view?.findNavController()?.navigate(action)
    }

    private fun gotoGroupIconFragment(imageView: View) {
        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        val groupName = binding.groupNameText.text?.trim().toString()
        val groupIcon = if (args.groupId != -1) {
            if (viewModel.group.value != null) {
                if (viewModel.group.value!!.groupIcon != null)
                    viewModel.group.value!!.groupIcon.toString()
                else
                    null
            } else {
                null
            }

        } else
            args.groupIcon  // when group icon already set before creating group

        val groupIconTransitionName = getString(R.string.group_icon_transition_name)
        val extras = FragmentNavigatorExtras(imageView to groupIconTransitionName)

        val action =
            CreateEditGroupFragmentDirections.actionCreateEditGroupFragmentToGroupIconFragment(
                args.groupId,
                groupIcon,
                groupName,
                false,
                false
            )
        findNavController().navigate(action, extras)
    }

}