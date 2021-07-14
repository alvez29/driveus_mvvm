package com.example.driveus_mvvm.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentProfileBinding
import com.example.driveus_mvvm.model.entities.User
import com.example.driveus_mvvm.model.entities.Vehicle
import com.example.driveus_mvvm.ui.adapter.VehicleListAdapter
import com.example.driveus_mvvm.view_model.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage


class ProfileFragment : Fragment() {

    private var viewBinding: FragmentProfileBinding? = null
    private val viewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }

    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private val vehicleListListener = object: VehicleListAdapter.VehicleListAdapterListener{
        override fun onDeleteButtonClick(vehicleId: String, model: String) {
            val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_car, null)
            val mBuilder = AlertDialog.Builder(context)
                .setView(mDialogView)
                .setTitle(model)

            val mAlertDialog = mBuilder.show()
            mDialogView.findViewById<View>(R.id.dialog_delete_car__button__cancel).setOnClickListener {
                mAlertDialog.dismiss()
            }
            mDialogView.findViewById<View>(R.id.dialog_delete_car__button__accept).setOnClickListener {
                sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
                    ?.let { it1 -> viewModel.deleteVehicleById(it1, vehicleId) }
                mAlertDialog.dismiss()
            }
        }
    }

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        it?.let {
            sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
                ?.let { it1 -> viewModel.uploadProfileImageToFirebase(it, it1) }
        }
    }

    private val userObserver = Observer<DocumentSnapshot> { document ->
        val user: User? = document.toObject(User::class.java)
        val fullName = user?.name + " " + user?.surname
        viewBinding?.profileFragmentLabelUsername?.text = user?.username
        viewBinding?.profileFragmentLabelFullName?.text = fullName
        viewBinding?.profileFragmentLabelEmail?.text = user?.email

        val adapter = VehicleListAdapter(vehicleListListener)
        viewBinding?.fragmentProfileRecycleView?.adapter = adapter
        viewBinding?.fragmentProfileRecycleView?.layoutManager = LinearLayoutManager(context)
        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            ?.let { viewModel.getVehiclesByUserId(it).observe(viewLifecycleOwner, vehiclesObserver(adapter)) }

        if (user?.isDriver == false) {
            viewBinding?.fragmentProfileLayoutCars?.visibility = View.GONE
            viewBinding?.fragmentProfileLayoutNoCars?.visibility = View.VISIBLE
        } else {
            viewBinding?.fragmentProfileLayoutCars?.visibility = View.VISIBLE
            viewBinding?.fragmentProfileLayoutNoCars?.visibility = View.GONE
        }
    }

    private val imageTriggerObserver = Observer<Boolean> {
       showImage()
    }

    private fun vehiclesObserver(adapter: VehicleListAdapter) = Observer<Map<String, Vehicle>> { vehicles ->
        vehicles?.let {
            adapter.submitList(it.toList())
        }
    }

    private fun logOut() {
        FirebaseAuth.getInstance().signOut()
        sharedPref?.edit()?.clear()?.apply()
        val authIntent = Intent(activity, AuthActivity::class.java)
        authIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(authIntent)
        activity?.finish()
    }

    private fun addNewCar() {
        viewBinding?.profileFragmentButtonAddCar?.setOnClickListener {
            val actionNoDriver = ProfileFragmentDirections.actionProfileFragmentToAddCarFragment2()
            findNavController().navigate(actionNoDriver)
        }

        viewBinding?.profileFragmentButtonAddCarDriver?.setOnClickListener {
            val actionDriver = ProfileFragmentDirections.actionProfileFragmentToAddCarFragment2()
            findNavController().navigate(actionDriver)
        }
    }

    private fun showImage() {
        val imageName = sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
        FirebaseStorage.getInstance().reference.child("users/$imageName").downloadUrl.addOnSuccessListener {
            viewBinding?.fragmentProfileImage?.let { it1 ->
                Glide.with(this)
                    .load(it.toString())
                    .circleCrop()
                    .into(it1)
            }
        }.addOnFailureListener {
            viewBinding?.fragmentProfileImage?.let { it1 ->
                Glide.with(this)
                    .load(R.drawable.ic_action_name)
                    .circleCrop()
                    .into(it1)
            }
        }
    }

    @SuppressLint("ResourceType")
    private fun pictureOptions() {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_profile_pic, null)
        val mBuilder = AlertDialog.Builder(context)
            .setView(mDialogView)
            .setTitle(getString(R.string.dialog_profile_pic__label__title))

        val mAlertDialog = mBuilder.show()

        mDialogView.findViewById<View>(R.id.dialog_profile_pic_cancel).setOnClickListener{
            mAlertDialog.dismiss()
        }

        mDialogView.findViewById<View>(R.id.dialog_profile_pic_delete).setOnClickListener{
            sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
                ?.let { it1 -> viewModel.deleteImageByUserId(it1) }
            mAlertDialog.dismiss()
        }

        mDialogView.findViewById<View>(R.id.dialog_profile_pic_change).setOnClickListener{
            getImage.launch("image/*")
            mAlertDialog.dismiss()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_top_bar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_top_bar__item__log_out) {
            logOut()
        }
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentProfileBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showImage()
        addNewCar()

        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            ?.let { it1 -> viewModel.getUserById(it1).observe(viewLifecycleOwner, userObserver) }

        viewModel.getImageTrigger().observe(viewLifecycleOwner, imageTriggerObserver)

        viewBinding?.fragmentProfileImage?.setOnClickListener {
            pictureOptions()
        }
    }
}