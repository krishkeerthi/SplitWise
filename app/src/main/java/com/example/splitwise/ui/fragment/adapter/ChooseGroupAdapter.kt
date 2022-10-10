package com.example.splitwise.ui.fragment.adapter

import android.content.ContentValues.TAG
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Group
import com.example.splitwise.databinding.ChooseGroupCardBinding
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.getRoundedCroppedBitmap

class ChooseGroupAdapter(val onItemChecked: (Group, Boolean) -> Unit) :
    RecyclerView.Adapter<ChooseGroupViewHolder>() {
    private var groups = listOf<Group>()
    private var selectedAllGroups: Boolean = false
    private var selectedGroupIds = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChooseGroupViewHolder {
        val view = LayoutInflater.from(parent.context)
        val binding = ChooseGroupCardBinding.inflate(view, parent, false)

        return ChooseGroupViewHolder(binding).apply {
            itemView.setOnClickListener {
                val isChecked = binding.selectedCheckBox.isChecked
                binding.selectedCheckBox.isChecked = !isChecked
            }

            binding.selectedCheckBox.setOnCheckedChangeListener { compoundButton, isChecked ->
                // Log.d(TAG, "onCreateViewHolder: group checkbox checked $isChecked")
                if (isChecked) {
                    //binding.selectedCheckBox.isChecked = true
                    onItemChecked(groups[adapterPosition], true)
                    selectedGroupIds.add(groups[absoluteAdapterPosition].groupId)
                } else {
                    //binding.selectedCheckBox.isChecked = false
                    onItemChecked(groups[adapterPosition], false)
                    selectedGroupIds.remove(groups[absoluteAdapterPosition].groupId)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ChooseGroupViewHolder, position: Int) {
        val group = groups[position]

        if (!selectedAllGroups)
            holder.bind(group, selectedGroupIds.toList())
        else
            holder.bindAndCheck(group)
    }

    override fun getItemCount(): Int {
        return groups.size
    }

    fun updateGroups(groups: List<Group>, selectedGroupIds: List<Int>) {
        this.groups = groups
        this.selectedGroupIds = selectedGroupIds.toMutableSet()
        notifyDataSetChanged()
    }

    fun selectAllGroups() {
        selectedAllGroups = true

        // this is added, because on landscape only visible views are checked so count is incorrect
        for(group in groups) {
            onItemChecked(group, true)
            selectedGroupIds.add(group.groupId)
        }

        notifyDataSetChanged()
    }
}

class ChooseGroupViewHolder(val binding: ChooseGroupCardBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val resources = binding.root.resources

    fun bind(group: Group, selectedGroupIds: List<Int>) {
        binding.groupNameTextView.text = group.groupName

        binding.selectedCheckBox.isChecked = group.groupId in selectedGroupIds

        Log.d(TAG, "bind: checking inside bind")
        if (group.groupIcon != null) {
            ///binding.groupImageView.setImageURI(group.groupIcon)

            binding.groupImageView.setImageBitmap(null)
          //  Handler(Looper.getMainLooper()).postDelayed({

                binding.groupImageView.setImageBitmap(
                    getRoundedCroppedBitmap(
                    decodeSampledBitmapFromUri(
                        binding.root.context, group.groupIcon, 40.dpToPx(resources.displayMetrics), 40.dpToPx(resources.displayMetrics)
                    )!!
                )
                )
         //   }, resources.getInteger(R.integer.reply_motion_duration_large).toLong())

            binding.groupImageHolder.visibility = View.INVISIBLE
            binding.groupImageHolderImage.visibility = View.INVISIBLE
            binding.groupImageView.visibility = View.VISIBLE
        } else {
            binding.groupImageHolder.visibility = View.VISIBLE
            binding.groupImageHolderImage.visibility = View.VISIBLE
            binding.groupImageView.visibility = View.INVISIBLE
        }


//
//        if (group.groupId in selectedGroupIds) {
//            Log.d(TAG, "bind: checking inside is checked")
//            binding.selectedCheckBox.isChecked = true
//        }
//        else{
//            binding.selectedCheckBox.isChecked = false
//            // setting default checked state to unchecked, otherwise while recycling view, previously selected checked states are shown
//
//        }
    }

    fun bindAndCheck(group: Group) {
        binding.groupNameTextView.text = group.groupName
        binding.selectedCheckBox.isChecked = true
    }
}