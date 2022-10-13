package com.example.splitwise.ui.fragment.feedback

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.splitwise.R
import com.example.splitwise.databinding.FragmentFeedbackBinding
import com.example.splitwise.util.recipients
import com.example.splitwise.util.titleCase
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.MaterialSharedAxis

class FeedbackFragment : Fragment() {

    private lateinit var binding: FragmentFeedbackBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //sharedElementEnterTransition = MaterialContainerTransform()

        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment_container
            duration = resources.getInteger(R.integer.reply_motion_duration_large).toLong()
            scrimColor = resources.getColor(R.color.background)//Color.TRANSPARENT
            setAllContainerColors(resources.getColor(R.color.background))
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.feedback)
        return inflater.inflate(R.layout.fragment_feedback, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFeedbackBinding.bind(view)

        // options menu
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)

        inflater.inflate(R.menu.feedback_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.send_feedback -> {
                sendEmail()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun sendEmail() {
        val message = binding.feedbackTextView.text.trim().toString().titleCase()

        if (message != "") {
            composeEmail(recipients(), "SplitWise Feedback", message)
        } else {
            Snackbar.make(binding.root, getString(R.string.empty_feedback), Snackbar.LENGTH_SHORT)
                .show()
        }
    }

    private fun composeEmail(recipients: Array<String>, subject: String, message: String) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            //type="*/*"
            putExtra(Intent.EXTRA_EMAIL, recipients)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, message)
            // file uri exposed, need to use file provider
//            putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(Uri.parse("file:///storage/emulated/0/Android/data/com.example.splitwise/files/Pictures/IMG1662724548156.png")))
//            //putExtra(Intent.EXTRA_STREAM, arrayOf(Uri.parse("file:///storage/emulated/0/Android/data/com.example.splitwise/files/Pictures/IMG1662724548156.png")))
        }


        startActivity(intent)
        navigateBack()

    }

    private fun navigateBack() {
        NavHostFragment.findNavController(this).popBackStack()
    }
}