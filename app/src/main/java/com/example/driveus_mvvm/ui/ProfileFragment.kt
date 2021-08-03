package com.example.driveus_mvvm.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
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
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.ui.adapter.VehicleListAdapter
import com.example.driveus_mvvm.ui.utils.ImageUtils
import com.example.driveus_mvvm.ui.utils.NetworkUtils
import com.example.driveus_mvvm.view_model.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage


class ProfileFragment : Fragment() {

    private var viewBinding: FragmentProfileBinding? = null
    private val viewModel: UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }
    private val firebaseStorage: FirebaseStorage = FirestoreRepository.getFirebaseStorageInstance()

    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }

    private val vehicleListListener = object: VehicleListAdapter.VehicleListAdapterListener{
        override fun onDeleteButtonClick(vehicleId: String, model: String) {
            if (!NetworkUtils.hasConnection(context)) {
                Toast.makeText(context, getString(R.string.connection_failed_message), Toast.LENGTH_SHORT).show()

            } else {
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

        override fun navigateToEditVehicle(vehicleId: String) {
            val action = ProfileFragmentDirections.actionProfileFragmentToEditVehicleFragment(vehicleId)
            findNavController().navigate(action)
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

    private fun setupChangePassword() {
        val action = ProfileFragmentDirections.actionProfileFragmentToChangePasswordFragment()
        findNavController().navigate(action)
    }

    private fun addNewCar() {
        viewBinding?.profileFragmentButtonAddCarDriver?.setOnClickListener {
            val actionDriver = ProfileFragmentDirections.actionProfileFragmentToAddCarFragment2()
            findNavController().navigate(actionDriver)
        }
    }

    private fun addFirstCar() {
        viewBinding?.profileFragmentButtonAddCar?.setOnClickListener {
            val actionDriver = ProfileFragmentDirections.actionProfileFragmentToAddCarFragment2()
            findNavController().navigate(actionDriver)
        }
    }

    private fun showImage() {
        val imageName = sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")

        context?.let {
            viewBinding?.fragmentProfileImage?.let {
                view -> ImageUtils.loadProfilePicture(imageName, view, it, firebaseStorage )
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

    private fun navigateToEditUser() {
        val action = ProfileFragmentDirections.actionProfileFragmentToEditUserNameFragment()
        findNavController().navigate(action)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_top_bar, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_top_bar__item__log_out -> {
                (activity as MainActivity).logOut()
                onDestroy()
            }
            R.id.menu_top_bar__item__change_password -> setupChangePassword()
            R.id.menu_top_bar__item__edit_user -> navigateToEditUser()
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
        addFirstCar()

        sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
            ?.let { it1 -> viewModel.getUserById(it1).observe(viewLifecycleOwner, userObserver) }

        viewModel.getImageTrigger().observe(viewLifecycleOwner, imageTriggerObserver)

        viewBinding?.fragmentProfileImage?.setOnClickListener {
            pictureOptions()
        }
    }

}