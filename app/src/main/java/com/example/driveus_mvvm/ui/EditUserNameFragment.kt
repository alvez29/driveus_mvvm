package com.example.driveus_mvvm.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentEditUserNameBinding
import com.example.driveus_mvvm.ui.enums.SignUpFormEnum
import com.example.driveus_mvvm.view_model.UserViewModel
import com.google.firebase.firestore.DocumentSnapshot

class EditUserNameFragment : Fragment() {

    private var viewBinding: FragmentEditUserNameBinding? = null
    private val userViewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private val formErrorObserver = Observer<Map<SignUpFormEnum, Int>> {
        it.forEach { (k,v) ->
            when(k) {
                SignUpFormEnum.NAME -> viewBinding?.fragmentEditUserNameInputNameEdit?.error = getString(v)
                SignUpFormEnum.SURNAME -> viewBinding?.fragmentEditUserNameInputSurnameEdit?.error = getString(v)
            }
        }
    }

    private val userObserver = Observer<DocumentSnapshot> {
        if (it != null) {
            viewBinding?.fragmentEditUserNameInputNameEdit?.setText(it.getString("name"))
            viewBinding?.fragmentEditUserNameInputSurnameEdit?.setText(it.getString("surname"))
        }
    }


    private val redirectObserver = Observer<Pair<Boolean, String>> {
        if (it.first) {
            findNavController().popBackStack()
        }
    }

    private fun getInputs(): MutableMap<SignUpFormEnum, String> {
        return mutableMapOf(
                SignUpFormEnum.NAME to viewBinding?.fragmentEditUserNameInputNameEdit?.text.toString(),
                SignUpFormEnum.SURNAME to viewBinding?.fragmentEditUserNameInputSurnameEdit?.text.toString()
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentEditUserNameBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val userId = sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")

        if (userId != null) {
            userViewModel.getUserById(userId).observe(viewLifecycleOwner, userObserver)

            viewBinding?.fragmentEditUserNameButtonSave?.setOnClickListener {
                val inputs = getInputs()
                userViewModel.editNameAndSurname(userId, inputs)
            }
        }

        userViewModel.getRedirect().observe(viewLifecycleOwner, redirectObserver)
        userViewModel.getFormErrors().observe(viewLifecycleOwner, formErrorObserver)
    }
}