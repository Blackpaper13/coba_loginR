package com.example.coba_loginr

import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.coba_loginr.databinding.FragmentUpdateEmailBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.database.DatabaseReference

class UpdateEmailFragment : Fragment() {

    private var _binding: FragmentUpdateEmailBinding? = null

    private val binding get() = _binding

    private lateinit var auth: FirebaseAuth

    private lateinit var database : DatabaseReference
    var maxid : Long = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUpdateEmailBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        binding?.layoutPassword?.visibility = View.VISIBLE
        binding?.layoutEmail?.visibility = View.GONE

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
                        binding!!.layoutEmail.visibility = View.VISIBLE
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

        binding?.btnUpdate?.setOnClickListener { view ->
            val emailUpdated = binding!!.etEmail.text.toString().trim()

            if (emailUpdated.isEmpty()){
                binding!!.etEmail.error = "Email wasn't Valid"
                binding!!.etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(emailUpdated).matches()){
                binding!!.etEmail.error = "Email wasn't Valid"
                binding!!.etEmail.requestFocus()
                return@setOnClickListener
            }

            user?.let {
                user.updateEmail(emailUpdated).addOnCompleteListener {
                    if (it.isSuccessful){
                        val actionEmailUpdated = UpdateEmailFragmentDirections.actionEmailUpdated()
                        Navigation.findNavController(view).navigate(actionEmailUpdated)
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