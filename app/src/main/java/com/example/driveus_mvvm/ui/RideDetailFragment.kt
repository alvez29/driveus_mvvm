package com.example.driveus_mvvm.ui

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.driveus_mvvm.R
import com.example.driveus_mvvm.databinding.FragmentRideDetailBinding
import com.example.driveus_mvvm.model.entities.Channel
import com.example.driveus_mvvm.model.entities.Ride
import com.example.driveus_mvvm.model.entities.Vehicle
import com.example.driveus_mvvm.model.repository.FirestoreRepository
import com.example.driveus_mvvm.model.toLatLng
import com.example.driveus_mvvm.ui.utils.ImageUtils
import com.example.driveus_mvvm.ui.utils.LocationUtils
import com.example.driveus_mvvm.ui.utils.NetworkUtils
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

    private val firebaseStorage: FirebaseStorage = FirestoreRepository.getFirebaseStorageInstance()

    private val askForPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            activateMyLocation()
        }
    }

    private val meetingPointObserver = Observer<GeoPoint> {
        setupMapMarker(it)
        setupCenterButton(it)
    }

    private val rideObserver = Observer<Ride> { rideObj ->
        if (rideObj != null) {
            viewBinding?.rideDetailContainerContent?.visibility = View.VISIBLE
            viewBinding?.rideDetailContainerNoContent?.visibility = View.GONE
            setupBasicInformation(rideObj)
            rideObj.vehicle?.let { vehicleRef ->
                rideObj.driver?.let { driverRef ->
                    (childFragmentManager.findFragmentById(R.id.ride_detail__map__meeting_point) as? SupportMapFragment)?.getMapAsync(this)
                    userViewModel.getUserById(driverRef.id).observe(viewLifecycleOwner, userObserver)
                    userViewModel.getVehicleById(vehicleRef.id, driverRef.id).observe(viewLifecycleOwner, vehicleObserver)
                    showButtons(driverRef.id, rideObj.date, rideObj.passengers.map { it.id }, rideObj.capacity)
                    setupPayoutButton()
                }
            }
        } else {
            viewBinding?.rideDetailContainerContent?.visibility = View.GONE
            viewBinding?.rideDetailContainerNoContent?.visibility = View.VISIBLE
        }
    }

    private fun showButtons(driverId: String?, rideDate: Timestamp?, passengers: List<String>, capacity: Int?) {
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
        } else if (driverId == currentUserId) {
            viewBinding?.rideDetailButtonPayoutsList?.visibility = View.VISIBLE
            if (rideDate?.toDate()?.after(Timestamp.now().toDate()) == true){
                viewBinding?.rideDetailButtonDeleteRide?.visibility = View.VISIBLE
            } else {
                viewBinding?.rideDetailButtonDeleteRide?.visibility = View.GONE
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

    private fun setupPayoutButton() {
        viewBinding?.rideDetailButtonPayoutsList?.setOnClickListener {
            val action = channelId?.let { it1 ->
                rideId?.let { it2 ->
                    RideDetailFragmentDirections.actionRideDetailFragmentToRidePayoutsListFragment()
                            .setChannelId(it1)
                            .setRideId(it2)
                }
            }

            if (action != null) {
                findNavController().navigate(action)
            }
        }
    }

    private fun loadProfilePicture(userId: String?, imageView: ImageView) {
        context?.let { ImageUtils.loadProfilePicture(userId, imageView, it, firebaseStorage ) }
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

    private fun dialogJoin() {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_join_ride_button, null)
        val mBuilder = AlertDialog.Builder(context)
                .setView(mDialogView)
                .setTitle(getString(R.string.dialog_join_ride_title))

        val mAlertDialog = mBuilder.show()

        mDialogView.findViewById<View>(R.id.dialog_join_ride__button__cancel).setOnClickListener {
            mAlertDialog.dismiss()
        }

        mDialogView.findViewById<View>(R.id.dialog_join_ride__button__accept).setOnClickListener {
            channelId?.let { channelId ->
                rideId?.let { rideId ->
                    sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
                            ?.let { userId -> rideViewModel.addPassengerInARide(channelId, rideId, userId)
                            }
                }
            }
            mAlertDialog.dismiss()
            viewBinding?.rideDetailButtonJoin?.visibility = View.GONE
        }
    }

    private fun dialogDisjoin() {
        val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_disjoin_ride_button, null)
        val mBuilder = AlertDialog.Builder(context)
                .setView(mDialogView)
                .setTitle(getString(R.string.dialog_disjoin_ride_title))

        val mAlertDialog = mBuilder.show()

        mDialogView.findViewById<View>(R.id.dialog_disjoin_ride__button__cancel).setOnClickListener {
            mAlertDialog.dismiss()
        }

        mDialogView.findViewById<View>(R.id.dialog_disjoin_ride__button__accept).setOnClickListener {
            channelId?.let { channelId ->
                rideId?.let { rideId ->
                    sharedPref?.getString(getString(R.string.shared_pref_doc_id_key), "")
                            ?.let { userId -> rideViewModel.removePassengerInARide(channelId, rideId, userId, "")
                            }
                }
            }
            mAlertDialog.dismiss()
        }
    }

    private fun setupJoinsButton() {
        viewBinding?.rideDetailButtonJoin?.setOnClickListener {
            if (!NetworkUtils.hasConnection(context)) {
                Toast.makeText(context, getString(R.string.connection_failed_message), Toast.LENGTH_SHORT).show()

            } else {
                dialogJoin()
            }
        }
        viewBinding?.rideDetailButtonNotJoin?.setOnClickListener {
            if (!NetworkUtils.hasConnection(context)) {
                Toast.makeText(context, getString(R.string.connection_failed_message), Toast.LENGTH_SHORT).show()

            } else {
                dialogDisjoin()
            }
        }
    }

    private fun setupDeleteButton() {
        viewBinding?.rideDetailButtonDeleteRide?.setOnClickListener {
            if (!NetworkUtils.hasConnection(context)) {
                Toast.makeText(context, getString(R.string.connection_failed_message), Toast.LENGTH_SHORT).show()

            } else {
                val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_delete_ride, null)
                val mBuilder = AlertDialog.Builder(context)
                        .setView(mDialogView)
                        .setTitle("Eliminar viaje")

                val mAlertDialog = mBuilder.show()

                mDialogView.findViewById<View>(R.id.dialog_delete_ride__button__cancel).setOnClickListener {
                    mAlertDialog.dismiss()
                }

                mDialogView.findViewById<View>(R.id.dialog_delete_ride__button__accept).setOnClickListener {
                    mAlertDialog.dismiss()
                    rideViewModel.deleteRide(channelId, rideId)

                }
            }

        }
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
        if (context?.let { ActivityCompat.checkSelfPermission(it, ACCESS_FINE_LOCATION) }
                != PackageManager.PERMISSION_GRANTED) {
            askForPermission.launch(ACCESS_FINE_LOCATION)
        } else {
            activateMyLocation()
        }
    }

    private val redirectObserver = Observer<Boolean> {
        if (it) {
            findNavController().popBackStack()
        }
    }


    // MAIN FUNCTIONS -------------------------------------

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu_top_bar_ride_details, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_top_bar_ride_details__item__help -> {
                val mDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_help_fragment, null)
                val mBuilder = AlertDialog.Builder(context)
                    .setView(mDialogView)
                    .setTitle("Ayuda")

                val text1 = R.string.dialog_help_ride_details1
                val text2 = R.string.dialog_help_ride_details2
                val text3 = R.string.dialog_help_ride_details3
                val textList: List<Int> = listOf(text1, text2, text3)
                var pointer: Int = 0

                mDialogView.findViewById<TextView>(R.id.dialog_help__text__).setText(text1)
                mDialogView.findViewById<TextView>(R.id.dialog_help__text__n_views).setText("${pointer+1}/${textList.size}")

                val mAlertDialog = mBuilder.show()
                mDialogView.findViewById<View>(R.id.dialog_help__button__accept).setOnClickListener {
                    mAlertDialog.dismiss()
                }
                mDialogView.findViewById<ImageButton>(R.id.dialog_help__image__arrow_left).setOnClickListener {
                    if (pointer == 0) {
                        pointer = textList.size - 1
                    } else {
                        pointer -= 1
                    }
                    mDialogView.findViewById<TextView>(R.id.dialog_help__text__).setText(textList[pointer])
                    mDialogView.findViewById<TextView>(R.id.dialog_help__text__n_views).setText("${pointer+1}/${textList.size}")
                }
                mDialogView.findViewById<ImageButton>(R.id.dialog_help__image__arrow_right).setOnClickListener {
                    if (pointer == textList.size - 1) {
                        pointer = 0
                    } else {
                        pointer += 1
                    }
                    mDialogView.findViewById<TextView>(R.id.dialog_help__text__).setText(textList[pointer])
                    mDialogView.findViewById<TextView>(R.id.dialog_help__text__n_views).setText("${pointer+1}/${textList.size}")
                }
            }
        }
        return true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = FragmentRideDetailBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rideViewModel.getRedirectDelete().observe(viewLifecycleOwner, redirectObserver)
        rideViewModel.getRideById(channelId, rideId).observe(viewLifecycleOwner, rideObserver)

        channelId?.let { channelViewModel.getChannelById(it).observe(viewLifecycleOwner, channelObserver) }

        setupMapToggleButton()
        setupCarDescriptionBehaviour()

        channelId?.let { chId ->
            rideId?.let { rdId ->
                setupPassengersList(chId, rdId)
            }
        }

        setupDeleteButton()
        setupJoinsButton()
    }

    override fun onMapReady(p0: GoogleMap) {
        actualGoogleMap = p0
        activateMyLocationButton()
        rideViewModel.getMeetingPoint().observe(viewLifecycleOwner, meetingPointObserver)
    }

}