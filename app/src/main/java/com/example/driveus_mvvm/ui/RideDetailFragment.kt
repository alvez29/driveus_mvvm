package com.example.driveus_mvvm.ui

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentRideDetailBinding
import com.example.driveus_mvvm.model.entities.Channel
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.model.entities.Vehicle
import com.example.driveus_mvvm.model.toLatLng
import com.example.driveus_mvvm.ui.utils.ImageUtils
import com.example.driveus_mvvm.ui.utils.LocationUtils
import com.example.driveus_mvvm.view_model.ChannelViewModel
import com.example.driveus_mvvm.view_model.RideViewModel
import com.example.driveus_mvvm.view_model.UserViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat


class RideDetailFragment : Fragment(), OnMapReadyCallback {

    private var viewBinding: FragmentRideDetailBinding? = null
    private val rideViewModel : RideViewModel by lazy { ViewModelProvider(this)[RideViewModel::class.java] }
    private val userViewModel : UserViewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }
    private val channelViewModel : ChannelViewModel by lazy { ViewModelProvider(this)[ChannelViewModel::class.java] }
    private val sharedPref by lazy { activity?.getSharedPreferences(getString(R.string.prefs_file), Context.MODE_PRIVATE) }
    private val rideId by lazy { arguments?.getString("rideId") }
    private val channelId by lazy { arguments?.getString("channelId") }
    private var actualGoogleMap: GoogleMap? = null

    private val meetingPointObserver = Observer<GeoPoint> {
        setupMapMarker(it)
        setupCenterButton(it)
    }

    private val rideObserver = Observer<Ride> { rideObj ->
        if (rideObj != null) {
            setupBasicInformation(rideObj)
            rideObj.vehicle?.let { vehicleRef ->
                rideObj.driver?.let { driverRef ->
                    (childFragmentManager.findFragmentById(R.id.ride_detail__map__meeting_point) as? SupportMapFragment)?.getMapAsync(this)
                    userViewModel.getUserById(driverRef.id).observe(viewLifecycleOwner, userObserver)
                    userViewModel.getVehicleById(vehicleRef.id, driverRef.id).observe(viewLifecycleOwner, vehicleObserver)
                    showJoinButton(driverRef.id, rideObj.date, rideObj.passengers.map { it.id }, rideObj.capacity)
                }
            }
        }
    }

    private fun showJoinButton(driverId: String?, rideDate: Timestamp?, passengers: List<String>, capacity: Int?) {
        val currentUserId = sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")

        if (capacity != null
            && driverId != currentUserId
            && rideDate?.toDate()?.after(Timestamp.now().toDate()) == true) {
            if ( passengers.contains(currentUserId).not()
                && capacity > passengers.size) {
                viewBinding?.rideDetailButtonJoin?.visibility = View.VISIBLE
                viewBinding?.rideDetailButtonNotJoin?.visibility = View.GONE
            } else if(passengers.contains(currentUserId)){
                viewBinding?.rideDetailButtonJoin?.visibility = View.GONE
                viewBinding?.rideDetailButtonNotJoin?.visibility = View.VISIBLE
            }
        }
    }

    private val userObserver = Observer<DocumentSnapshot> {
        setupUserName(it["username"] as String?)
    }

    private val vehicleObserver = Observer<Vehicle> {
        it?.let {
            setupCarInformation(it)
        }
    }

    private val channelObserver = Observer<Channel> {
        it?.let {
            setupChannelInformation(it)
        }
    }

    private fun setupPassengersList(channelId: String, rideId: String) {
        val dialogFragment = PassengersDialogFragment(channelId, rideId, rideViewModel)

        viewBinding?.rideDetailLabelCapacity?.setOnClickListener {
            dialogFragment.show(childFragmentManager, "dialog")
        }

    }

    //TODO: método duplicado
    private fun loadProfilePicture(userId: String?, imageView: ImageView) {
        FirebaseStorage.getInstance().reference.child("users/$userId").downloadUrl.addOnSuccessListener {
            context?.let { it1 ->
                Glide.with(it1)
                    .load(it)
                    .circleCrop()
                    .into(imageView)
            }
        }.addOnFailureListener {
            context?.let { it1 ->
                Glide.with(it1)
                    .load(R.drawable.ic_action_name)
                    .circleCrop()
                    .into(imageView)
            }

            Log.d(getString(R.string.profile_picture_not_found_tag), getString(R.string.profile_picture_not_found_message))
        }
    }

    private fun setupUserName(username: String?) {
        username?.let {
            viewBinding?.rideDetailLabelUsername?.text = it
        }
    }

    private fun setupCarInformation(it: Vehicle) {
        val brandAndModelAux = "${it.brand} ${it.model}"

        viewBinding?.rideDetailLabelBrandAndModel?.text = brandAndModelAux
        viewBinding?.rideDetailLabelColor?.text = it.color
        viewBinding?.rideDetailTextCarDescription?.text = it.description

    }

    private fun setupChannelInformation(channel: Channel) {
        viewBinding?.rideDetailLabelOriginZone?.text = channel.originZone.toString()
        viewBinding?.rideDetailLabelDestinationZone?.text = channel.destinationZone.toString()
    }

    private fun setupBasicInformation(ride: Ride) {
        val pattern = "HH:mm dd-MM-yyyy"
        val simpleDateFormat = SimpleDateFormat(pattern)
        val priceAux = ride.price.toString() + " €"
        val capacityAux =  "${ride.passengers.size}/${ride.capacity.toString()}"
        val dateAux = simpleDateFormat.format(ride.date?.toDate()!!).split(" ")
        val timeAux = dateAux[0]
        val shortDateAux = dateAux[1]

        viewBinding?.rideDetailLabelPrice?.text = priceAux
        viewBinding?.rideDetailLabelCapacity?.text = capacityAux
        viewBinding?.rideDetailLabelTime?.text = timeAux
        viewBinding?.rideDetailLabelDate?.text = shortDateAux

        viewBinding?.rideDetailImageProfilePicture?.let { it1 -> loadProfilePicture(ride.driver?.id, it1) }
        viewBinding?.rideDetailImageCapacityIndicator?.let { it1 -> loadCapacityIndicator(ride.capacity, ride.passengers.size, it1) }
    }

    //TODO: metodo duplicado
    private fun loadCapacityIndicator(capacity: Int?, actual: Int, imageView: ImageView) {
        val capacityPercentage = capacity?.toDouble()
            ?.let { actual.toDouble() / it }
        var capacityDrawable: Int = 0

        capacityPercentage?.let {
            capacityDrawable = if (capacityPercentage == 1.0) {
                R.drawable.ic_baseline_red_circle_24

            } else if (capacityPercentage < 1.0 && capacityPercentage >= 0.75) {
                R.drawable.ic_baseline_orange_circle_24

            } else {
                R.drawable.ic_baseline_green_circle_24

            }
        }

        context?.let {
            Glide.with(it)
                .load(capacityDrawable)
                .circleCrop()
                .into(imageView)
        }

    }

    private fun setupMapToggleButton() {
        viewBinding?.rideDetailButtonSwitch?.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                viewBinding?.rideDetailMapMeetingPoint?.visibility = View.VISIBLE
                viewBinding?.rideDetailButtonCenterMeetingPoint?.visibility = View.VISIBLE
                viewBinding?.rideDetailContainerCarCard?.visibility = View.GONE
            } else {
                viewBinding?.rideDetailMapMeetingPoint?.visibility = View.GONE
                viewBinding?.rideDetailButtonCenterMeetingPoint?.visibility = View.GONE
                viewBinding?.rideDetailContainerCarCard?.visibility = View.VISIBLE
            }

        }

    }

    private fun setupCarDescriptionBehaviour() {
        var descriptionShown = false

        viewBinding?.trigger?.setOnLongClickListener {
            descriptionShown = if (descriptionShown) {
                changeDescriptionVisibility(View.GONE)
                changeCarInfoVisibility(View.VISIBLE)
                false
            } else {
                changeDescriptionVisibility(View.VISIBLE)
                changeCarInfoVisibility(View.GONE)
                true
            }

            return@setOnLongClickListener true
        }

    }

    private fun changeDescriptionVisibility(status: Int) {
        viewBinding?.rideDetailTextCarDescription?.visibility = status
    }

    private fun changeCarInfoVisibility(status: Int) {
        viewBinding?.rideDetailImageCarIcon?.visibility = status
        viewBinding?.rideDetailLabelBrandAndModel?.visibility = status
        viewBinding?.rideDetailImageColorIcon?.visibility = status
        viewBinding?.rideDetailLabelColor?.visibility = status
    }


    //AUXILIAR MAP FUNCTIONS ------------------------------

    private fun centerCamera(meetingPoint: GeoPoint) {
        val cameraPosition = CameraPosition.Builder()
                .target(meetingPoint.toLatLng())
                .zoom(15f)
                .bearing(0f)
                .build()

        actualGoogleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

    }

    private fun setupCenterButton(meetingPoint: GeoPoint) {
        viewBinding?.rideDetailButtonCenterMeetingPoint?.setOnClickListener {
            centerCamera(meetingPoint)
        }
    }

    private fun setupMapMarker(meetingPoint: GeoPoint) {
        actualGoogleMap?.clear()

        actualGoogleMap?.addMarker(MarkerOptions()
                .position(meetingPoint.toLatLng())
                .title("Punto de encuentro")
                .snippet(context?.let { it1 -> LocationUtils.getAddress(meetingPoint, it1) })
                .icon(context?.let { it1 -> ImageUtils.convertBitmapFromVector(it1, R.drawable.ic_origin_zone_24) }))

        centerCamera(meetingPoint)
    }

    @SuppressLint("MissingPermission")
    private fun activateMyLocation() {
        actualGoogleMap?.isMyLocationEnabled = true
    }

    private fun activateMyLocationButton() {
        if (context?.let {
                    ActivityCompat.checkSelfPermission(it, ACCESS_FINE_LOCATION) }
                != PackageManager.PERMISSION_GRANTED
                && context?.let { ActivityCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) }
                != PackageManager.PERMISSION_GRANTED) {
            //TODO: Utilizar request for activity result
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION), 1)

        } else {
            activateMyLocation()
        }
    }

    private fun setUpJoinsButton() {
        val userId = sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
        viewBinding?.rideDetailButtonJoin?.setOnClickListener {
                channelId?.let { channelId ->
                    rideId?.let { rideId ->
                        if (userId != null) {
                            rideViewModel.addPassengerInARide(channelId, rideId, userId)
                        }
                    }
                }
            viewBinding?.rideDetailButtonJoin?.visibility = View.GONE
        }
        viewBinding?.rideDetailButtonNotJoin?.setOnClickListener {
            if (userId != null) {
                channelId?.let {
                        channelId ->
                    rideId?.let { rideId ->
                        rideViewModel.removePassengerInARide(channelId, rideId, userId)
                    }
                }
            }
        }
    }

    // MAIN FUNCTIONS -------------------------------------

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentRideDetailBinding.inflate(inflater, container, false)

        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rideViewModel.getRideById(channelId, rideId).observe(viewLifecycleOwner, rideObserver)

        channelId?.let { channelViewModel.getChannelById(it).observe(viewLifecycleOwner, channelObserver) }

        setupMapToggleButton()
        setupCarDescriptionBehaviour()
        channelId?.let { chId ->
            rideId?.let { rdId ->
                setupPassengersList(chId, rdId)
            }
        }

        setUpJoinsButton()

    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            val permissionsList = permissions.toList()
            if (permissionsList.contains(ACCESS_FINE_LOCATION)
                    && permissionsList.contains(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    activateMyLocation()
                }
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        actualGoogleMap = p0
        activateMyLocationButton()
        rideViewModel.getMeetingPoint().observe(viewLifecycleOwner, meetingPointObserver)
    }


}