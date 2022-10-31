package com.example.splitwise.ui.fragment.groupdirectsettleup

import android.content.ContentValues.TAG
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.TextAppearanceSpan
import android.util.Log
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.toSpannable
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.splitwise.R
import com.example.splitwise.data.local.entity.Member
import com.example.splitwise.databinding.FragmentGroupDirectSettleUpBinding
import com.example.splitwise.util.decodeSampledBitmapFromUri
import com.example.splitwise.util.dpToPx
import com.example.splitwise.util.getRoundedCroppedBitmap
import com.example.splitwise.util.playPaymentSuccessSound
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.android.material.transition.MaterialSharedAxis
import java.util.*

class GroupDirectSettleUpFragment : Fragment() {
    private lateinit var binding: FragmentGroupDirectSettleUpBinding
    private val args: GroupDirectSettleUpFragmentArgs by navArgs()
    private val viewModel: GroupDirectSettleUpViewModel by viewModels {
        GroupDirectSettleUpViewModelFactory(requireContext(), args.groupId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enterTransition = MaterialSharedAxis(MaterialSharedAxis.Z, true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        returnTransition = MaterialSharedAxis(MaterialSharedAxis.Z, false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.direct_settle)
        return inflater.inflate(R.layout.fragment_group_direct_settle_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentGroupDirectSettleUpBinding.bind(view)

        viewModel.payer.observe(viewLifecycleOwner){ payer ->
            if(payer != null){
                binding.payerTextView.text = payer.name

                if (payer.memberProfile != null) {
                    ///binding.payerImageView.setImageURI(member.memberProfile)
                    // Handler(Looper.getMainLooper()).postDelayed({
                    binding.payerImageView.setImageBitmap(
                        getRoundedCroppedBitmap(
                            decodeSampledBitmapFromUri(
                                binding.root.context,
                                payer.memberProfile,
                                40.dpToPx(resources.displayMetrics),
                                40.dpToPx(resources.displayMetrics)
                            )!!
                        )
                    )
                    //}, resources.getInteger(R.integer.reply_motion_duration_large).toLong())

                    binding.payerImageView.visibility = View.VISIBLE
                    binding.payerImageHolder.visibility = View.INVISIBLE
                    binding.payerImageHolderImage.visibility = View.INVISIBLE
                }
            }
        }

        viewModel.recipient.observe(viewLifecycleOwner){ recipient ->
            if(recipient != null){
                binding.recipientTextView.text = recipient.name

                if (recipient.memberProfile != null) {
                    ///binding.recipientImageView.setImageURI(member.memberProfile)
                    // Handler(Looper.getMainLooper()).postDelayed({
                    binding.recipientImageView.setImageBitmap(
                        getRoundedCroppedBitmap(
                            decodeSampledBitmapFromUri(
                                binding.root.context,
                                recipient.memberProfile,
                                40.dpToPx(resources.displayMetrics),
                                40.dpToPx(resources.displayMetrics)
                            )!!
                        )
                    )
                    //}, resources.getInteger(R.integer.reply_motion_duration_large).toLong())

                    binding.recipientImageView.visibility = View.VISIBLE
                    binding.recipientImageHolder.visibility = View.INVISIBLE
                    binding.recipientImageHolderImage.visibility = View.INVISIBLE
                }
            }
        }

        viewModel.groupMembers.observe(viewLifecycleOwner){ members ->
            if(members != null){

                binding.payerSelectionImageView.setOnClickListener {
                    gotoDirectSettleUpMemberFragment(removedMembers(viewModel.recipientId, members), true)
                }

                binding.recipientSelectionImageView.setOnClickListener {
                    gotoDirectSettleUpMemberFragment(removedMembers(viewModel.payerId, members), false)
                }
            }
        }

        viewModel.owedText.observe(viewLifecycleOwner){
            if(!it.isNullOrBlank()){
                binding.owesTextView.text = getSpannable(it, getAmount(it)) //it

                if(viewModel.totalPayable > 0)
                    binding.outlinedAmountTextField.visibility = View.VISIBLE
                else
                    binding.outlinedAmountTextField.visibility = View.INVISIBLE
            }
            else{
                binding.outlinedAmountTextField.visibility = View.INVISIBLE
            }
        }

        if(args.payerId != -1){
            viewModel.payerId = args.payerId
            viewModel.fetchMember(args.payerId, true)
        }

        if(args.recipientId != -1){
            viewModel.recipientId = args.recipientId
            viewModel.fetchMember(args.recipientId, false)
        }

        if(args.payerId != -1 && args.recipientId != -1){
            Log.d(TAG, "onViewCreated: both are selected")
            viewModel.fetchOwedAmountText()
        }

        // option menu
        setHasOptionsMenu(true)
    }

    private fun settle(){
        if(viewModel.payerId == -1 || viewModel.recipientId == -1){
            Snackbar.make(
                binding.root,
                getString(R.string.payer_recipient_should_not_empty),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        if(viewModel.totalPayable == 0f){
            Snackbar.make(
                binding.root,
                getString(R.string.owes_nothing),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        if(binding.amountText.text.isNullOrBlank()) {
            Snackbar.make(
                binding.root,
                getString(R.string.field_empty),
                Snackbar.LENGTH_SHORT
            ).show()
            return
        }

        val amount = binding.amountText.text.toString().toFloat()

        if(amount > 0){
            if(amount <= viewModel.totalPayable) {
                val builder = AlertDialog.Builder(requireContext())

                builder.setMessage(getString(R.string.confirm_settle_up))

                builder.setPositiveButton(getString(R.string.confirm)) { dialog, which ->
                    viewModel.settle(amount) {
                        gotoGroupSplitwiseFragment()
                    }
                    playPaymentSuccessSound(requireContext())
                }

                builder.setNegativeButton(getString(R.string.cancel), null)

                builder.show()

            }
            else
                Snackbar.make(binding.root, getString(R.string.amount_exceeds), Snackbar.LENGTH_SHORT).show()
        }
        else{
            Snackbar.make(binding.root, getString(R.string.amount_greater_than_0), Snackbar.LENGTH_SHORT).show()
        }
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_direct_settle_up_fragment_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            R.id.direct_settle ->{
                settle()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }

    }

    private fun gotoDirectSettleUpMemberFragment(
        members: List<Member>, isPayerSelected: Boolean
    ){

        exitTransition = MaterialElevationScale(false).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }
        reenterTransition = MaterialElevationScale(true).apply {
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
        }

        val action = GroupDirectSettleUpFragmentDirections.
        actionGroupDirectSettleUpFragmentToGroupSettleUpMemberFragment(
            args.groupId, members.toTypedArray(), isPayerSelected, args.payerId, args.recipientId)

        findNavController().navigate(action)
    }

    private fun removedMembers(memberId: Int, members: List<Member>): List<Member>{
        if(memberId == -1)
            return members

        val updatedMembers = mutableListOf<Member>()

        for(member in members){
            if(member.memberId != memberId){
                updatedMembers.add(member)
            }
        }
        return updatedMembers
    }

    private fun gotoGroupSplitwiseFragment(){
        val action = GroupDirectSettleUpFragmentDirections.actionGroupDirectSettleUpFragmentToGroupSplitwiseFragment(args.groupId)
        findNavController().navigate(action)
    }

    private fun getAmount(wholeText: String): String{
        wholeText.split(" ").forEach {
            if(it.startsWith("₹"))
                return it
        }
        return ""
    }

    private fun getSpannable(data: String, query: String): Spannable {
        val startPos =
            data.lowercase(Locale.getDefault()).indexOf(query.lowercase(Locale.getDefault()))
        val endPos = startPos + query.length

        return if (startPos != -1) {
            val spannable = SpannableString(data)
            val colorStateList = ColorStateList(
                Array(query.length) { IntArray(query.length) },
                IntArray(query.length) {
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.red
                    )
                }
            )

            val textAppearanceSpan = TextAppearanceSpan(
                null,
                Typeface.BOLD_ITALIC,
                32.dpToPx(resources.displayMetrics),
                colorStateList,
                null
            )
            spannable.setSpan(
                textAppearanceSpan,
                startPos,
                endPos,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            spannable
        } else
            data.toSpannable()
    }
}