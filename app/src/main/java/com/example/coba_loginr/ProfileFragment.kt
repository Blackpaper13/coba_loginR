@file:Suppress("DEPRECATION")

package com.example.coba_loginr

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.coba_loginr.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream


class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null

    private val binding get() = _binding!!

    companion object{
        private const val CAMERA_REQUEST_CODE = 100
    }

    private lateinit var imageUri: Uri

    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser

        if (user != null) {
            if (user.photoUrl != null) {
                Picasso.get().load(user.photoUrl).into(binding.ivProfile)
            }else {
                Picasso.get().load("https://picsum.photos/id/237/200/300").into(binding.ivProfile)
            }

            binding.etName.setText(user.displayName)
            binding.etEmail.setText(user.email)

            if (user.isEmailVerified){
                binding.verifiedEmail.visibility = View.VISIBLE
            }else {
                binding.unverifiedEmail.visibility = View.VISIBLE
            }

            if (user.phoneNumber.isNullOrEmpty()) {
                binding.etPhone.setText("Masukkan No HP")
            }else {
                binding.etPhone.setText(user.phoneNumber)
            }

        }

        binding.ivProfile.setOnClickListener {
            intentGallery()
        }

        binding.btnUpdate.setOnClickListener {
            val photo = when {
                ::imageUri.isInitialized -> imageUri
                user?.photoUrl == null -> Uri.parse("https://picsum.photos/id/237/200/300")
                else -> user.photoUrl
            }
            val name = binding.etName.text.toString().trim()

            if (name.isEmpty()){
                binding.etName.error = "Silakan isi nama"
                binding.etName.requestFocus()
                return@setOnClickListener
            }

            UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(photo)
                .build().also {
                    user?.updateProfile(it)?.addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(activity, "Berhasil Update Profil", Toast.LENGTH_SHORT).show()
                        }else {
                            Toast.makeText(activity, "ERROR ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }

        binding.unverifiedEmail.setOnClickListener {
            user?.sendEmailVerification()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(activity, "Berhasil Kirim Verifikasi Email", Toast.LENGTH_SHORT).show()
                }else {
                    Toast.makeText(activity, "Email Belum Verifikasi", Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.etEmail.setOnClickListener {
            val actionUpdateEmail = ProfileFragmentDirections.actionUpdateEmail()
            Navigation.findNavController(it).navigate(actionUpdateEmail)
        }

        binding.etChangePassword.setOnClickListener {
            val actionChangePassword = ProfileFragmentDirections.actionChangePassword()
            Navigation.findNavController(it).navigate(actionChangePassword)
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun intentGallery() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            activity?.packageManager?.let {
                intent.resolveActivity(it).also {
                    startActivityForResult(intent, CAMERA_REQUEST_CODE)
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK){
            val imgBitmap = data?.extras?.get("data") as Bitmap
            uploadImage(imgBitmap)
        }
    }

    private fun uploadImage(imgBitmap: Bitmap) {
        val output = ByteArrayOutputStream()
        val ref = FirebaseStorage.getInstance().reference.child("img/${FirebaseAuth.getInstance().currentUser?.uid}")

        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 70,output)
        val image = output.toByteArray()

        ref.putBytes(image)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    ref.downloadUrl.addOnCompleteListener {
                        it.result?.let {
                            imageUri = it
                            binding.ivProfile.setImageBitmap(imgBitmap)
                        }
                    }
                }
            }
    }


}