package com.example.inspireme.ui

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.inspireme.AuthActivity
import com.example.inspireme.R
import com.example.inspireme.adapters.PostAdapter
import com.example.inspireme.databinding.FragmentDisplayBinding
import com.example.inspireme.models.Post
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DisplayFragment : Fragment() {

    private var _binding: FragmentDisplayBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val auth by lazy { Firebase.auth }
    private val db by lazy { Firebase.firestore }
    private val cloudViewModel: CloudViewModel by activityViewModels {
        CloudViewModelFactory(auth, db)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDisplayBinding.inflate(inflater, container, false)
        binding.cloudViewModel = cloudViewModel
        binding.lifecycleOwner = this
        return binding.root

    }

    override fun onStart() {
        super.onStart()
        cloudViewModel.checkAuthentication()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cloudViewModel.getPosts()

        cloudViewModel.authState.observe(viewLifecycleOwner) {
            if (it != AuthState.AUTHENTICATED) {
                startActivity(Intent(requireContext(), AuthActivity::class.java))
                requireActivity().finish()
            }
        }

        cloudViewModel.postLoadingStatus.observe(viewLifecycleOwner) {
            if (it == PostLoadingStatus.IN_PROGRESS) {
                binding.progressBar.visibility = View.VISIBLE
            } else {
                if (it == PostLoadingStatus.SUCCESS) {
                    binding.rvPostList.layoutManager = LinearLayoutManager(requireContext())
                    val postAdapter = PostAdapter {
                        showPostDialog(it)
                    }
                    binding.rvPostList.adapter = postAdapter
                    postAdapter.submitList(cloudViewModel.posts.value)
                }
                binding.progressBar.visibility = View.GONE
            }
        }

        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
    }

    private fun showPostDialog(it: Post) {
        val dialog = AlertDialog.Builder(requireContext())
        dialog.setTitle(it.title)
        dialog.setMessage(it.content)
        dialog.setPositiveButton("OK") { _, _ -> }
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}