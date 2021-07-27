package com.example.driveus_mvvm.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.DialogPayoutUsersListBinding

class PayoutUsernameFilterDialogFragment(
        private val callback: PayoutUserNameFilterDialogFragmentCallback,
        private val usernameList: Set<String>?)
    : DialogFragment() {

    interface PayoutUserNameFilterDialogFragmentCallback {
        fun selectedUserName(username: String)
    }

    private var viewBinding: DialogPayoutUsersListBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = DialogPayoutUsersListBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = parentFragment?.context?.let { ArrayAdapter<String>(it, R.layout.row_username, mutableListOf()) }
        viewBinding?.dialogPayoutUsersListListUsersList?.adapter = adapter

        if (usernameList != null) {
            adapter?.addAll(usernameList)
        }

        viewBinding?.dialogPayoutUsersListListUsersList?.setOnItemClickListener { _, _, position, _ ->
            adapter?.getItem(position)?.let { callback.selectedUserName(it) }
            this.dismiss()
            this.onDestroy()
        }
    }
}