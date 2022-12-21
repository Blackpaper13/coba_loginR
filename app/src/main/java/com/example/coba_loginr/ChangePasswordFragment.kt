package com.example.coba_loginr

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.coba_loginr.databinding.FragmentChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

class ChangePasswordFragment : Fragment() {

    private var _binding : FragmentChangePasswordBinding? = null

    private val binding get() = _binding

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        binding?.layoutPassword?.visibility = View.VISIBLE
        binding?.layoutNewPassword?.visibility = View.GONE

        //fungsi untuk mengecek kondisi Pasword masih sama dengan Email
        //yang terdaftar
        binding?.btnAuth?.setOnClickListener {
            val passwordCheck = binding!!.etPassword.text.toString().trim()

            if (passwordCheck.isEmpty()){
                binding!!.etPassword.error = "Password was Wrong. Please fill Right Password"
                binding!!.etPassword.requestFocus()
                return@setOnClickListener
            }

            user?.let {
                val userCredential = EmailAuthProvider.getCredential(it.email!!, passwordCheck)
                it.reauthenticate(userCredential).addOnCompleteListener {
                    if (it.isSuccessful) {
                        binding!!.layoutNewPassword.visibility = View.VISIBLE
                        binding!!.layoutPassword.visibility = View.GONE
                    } else if (it.exception is FirebaseAuthInvalidCredentialsException) {
                        binding!!.etPassword.error = "Your Password Was Wrong, Please Fill Right"
                        binding!!.etPassword.requestFocus()
                    }else {
                        Toast.makeText(activity, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        binding?.btnUpdateNewPassword?.setOnClickListener { view ->
            val newPassword = binding!!.etNewPassword.text.toString().trim()
            val confirmNewPassword = binding!!.ConfirmNewPassword.text.toString().trim()

            if (newPassword.isEmpty() || newPassword.length < 6) {
                binding!!.etNewPassword.error = "Password wasn't Valid"
                binding!!.etNewPassword.requestFocus()
                return@setOnClickListener
            }

            if (newPassword != confirmNewPassword) {
                binding!!.etNewPassword.error = "Password wasn't Valid"
                binding!!.etNewPassword.requestFocus()
                return@setOnClickListener
            }

            user?.let {
                user.updatePassword(newPassword).addOnCompleteListener {
                    if (it.isSuccessful) {
                        val actionPasswordUpdated = ChangePasswordFragmentDirections.changePasswordUpdated()
                        Navigation.findNavController(view).navigate(actionPasswordUpdated)
                    }else {
                        Toast.makeText(activity, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}