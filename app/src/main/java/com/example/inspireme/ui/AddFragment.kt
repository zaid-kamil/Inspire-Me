package com.example.inspireme.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.inspireme.AuthActivity
import com.example.inspireme.databinding.FragmentAddBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class AddFragment : Fragment() {

    private var _binding: FragmentAddBinding? = null
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

        _binding = FragmentAddBinding.inflate(inflater, container, false)
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

        cloudViewModel.authState.observe(viewLifecycleOwner) {
            if (it != AuthState.AUTHENTICATED) {
                startActivity(Intent(requireContext(), AuthActivity::class.java))
                requireActivity().finish()
            }
        }
        cloudViewModel.postUploadStatus.observe(viewLifecycleOwner) {
            if (it == PostUploadStatus.IN_PROGRESS) {
                binding.tvPostStatus.text = "uploading post to cloud ☁️"
            }else if (it == PostUploadStatus.SUCCESS) {
                binding.editPostTitle.text?.clear()
                binding.editPostContent.text?.clear()
            }
        }
        binding.fabSave.setOnClickListener {
            cloudViewModel.addPost(
                binding.editPostTitle.text.toString(),
                binding.editPostContent.text.toString()
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}