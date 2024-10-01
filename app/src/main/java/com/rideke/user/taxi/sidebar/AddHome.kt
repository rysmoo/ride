package com.rideke.user.taxi.sidebar

/**
 * @package com.cloneappsolutions.cabmeuser
 * @subpackage Side_Bar
 * @category AddHome
 * @author SMR IT Solutions
 * 
 */

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView

import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveCanceledListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveListener
import com.google.android.gms.maps.GoogleMap.OnCameraMoveStartedListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.gson.Gson
import com.rideke.user.R
import com.rideke.user.common.configs.SessionManager
import com.rideke.user.common.datamodels.JsonResponse
import com.rideke.user.common.helper.Permission
import com.rideke.user.common.helper.ReturnValues
import com.rideke.user.common.interfaces.ApiService
import com.rideke.user.common.interfaces.ServiceListener
import com.rideke.user.common.network.AppController
import com.rideke.user.common.utils.CommonMethods
import com.rideke.user.common.utils.RequestCallback
import com.rideke.user.taxi.views.customize.CustomDialog
import com.rideke.user.taxi.views.search.GoogleMapPlaceSearchAutoCompleteRecyclerView
import com.rideke.user.taxi.views.search.PlaceSearchActivity

import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.Locale

import javax.inject.Inject

import butterknife.ButterKnife
import butterknife.BindView
import butterknife.OnClick

import com.rideke.user.common.utils.CommonKeys.API_KEY_HOME
import com.rideke.user.common.utils.CommonKeys.API_KEY_LATITUDE
import com.rideke.user.common.utils.CommonKeys.API_KEY_LONGITUDE
import com.rideke.user.common.utils.CommonKeys.API_KEY_TOKEN
import com.rideke.user.common.utils.CommonKeys.API_KEY_WORK
import com.rideke.user.common.utils.CommonMethods.Companion.DebuggableLogD
import com.rideke.user.common.utils.CommonMethods.Companion.DebuggableLogE
import com.rideke.user.common.utils.CommonMethods.Companion.DebuggableLogI
import com.rideke.user.common.utils.CommonMethods.Companion.DebuggableLogV
import com.rideke.user.common.utils.Enums.REQ_UPDATE_LOCATION
import com.rideke.user.common.views.CommonActivity
import kotlinx.android.synthetic.main.app_activity_add_home.*

/* ************************************************************
   Update rider home and work location while search nearest car
    *********************************************************** */
class AddHome : CommonActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnCameraMoveStartedListener, OnCameraMoveListener, OnCameraMoveCanceledListener, OnCameraIdleListener, ServiceListener, GoogleMapPlaceSearchAutoCompleteRecyclerView.AutoCompleteAddressTouchListener {

    lateinit var dialog: AlertDialog
    @Inject
    lateinit var sessionManager: SessionManager
    @Inject
    lateinit var commonMethods: CommonMethods
    @Inject
    lateinit var apiService: ApiService
    @Inject
    lateinit var customDialog: CustomDialog
    @Inject
    lateinit var gson: Gson

    lateinit var mapFragment: SupportMapFragment

    lateinit var locationHashMap: HashMap<String, String>
    @BindView(R.id.destclose)
    lateinit var destclose: TextView
    @BindView(R.id.destadddress)
    lateinit var destadddress: EditText
    @BindView(R.id.address_search)
    lateinit var address_search: ScrollView
    @BindView(R.id.pin_map)
    lateinit var pin_map: ImageView
    @BindView(R.id.drop_done)
    lateinit var drop_done: TextView

    lateinit var searchlocation: String
     var lat: String?=null
     var log: String?=null
     var googleMap: GoogleMap?=null
    lateinit var Listlat: String
    lateinit var Listlong: String
     var pickipCountry: String?=null
    lateinit var destCountry: String
    //var markerPoints: MutableList<Any> = ArrayList()
    //var markerPoints = arrayListOf<Any>()
    var markerPoints = ArrayList<Any>()
    var oldstring = ""
     var workaddress: String?=null
     var searchwork: String?=null
     var searchhome: String?=null
     var settinghome: String?=null
     var settingwork: String?=null
    @BindView(R.id.location_placesearch)
    lateinit var mRecyclerView: RecyclerView
    protected lateinit var mGoogleApiClient: GoogleApiClient
    protected var isInternetAvailable: Boolean = false
    //private PlacesAutoCompleteAdapter mAutoCompleteAdapter;
    private val addressAutoCompletePredictions = ArrayList<AutocompletePrediction>()
    internal lateinit var googleMapPlaceSearchAutoCompleteRecyclerView: GoogleMapPlaceSearchAutoCompleteRecyclerView
    internal lateinit var placesClient: PlacesClient
    /**
     * Textwatcher for place search
     */
    protected var PlaceTextWatcher: TextWatcher = object : TextWatcher {

        internal var count = 0

        override fun beforeTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
            if (s.toString() == "") {
                mRecyclerView.visibility = View.GONE
            } else {
               // mRecyclerView.visibility = View.VISIBLE
            }
        }

        override fun onTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
            if (s.toString() == "") {
                mRecyclerView.visibility = View.GONE
            } else {
              //  mRecyclerView.visibility = View.VISIBLE
            }
        }

        override fun afterTextChanged(s: Editable) {


            if (s.toString() != "") {

                Handler().postDelayed({
                    if (oldstring != s.toString()) {
                        oldstring = s.toString()
                        count++
                        // Toast.makeText(getApplicationContext(), count + " search " + s.toString(), Toast.LENGTH_SHORT).show();
                        //mAutoCompleteAdapter.getFilter().filter(s.toString());
                        getFullAddressUsingEdittextStringFromGooglePlaceSearchAPI(s.toString())
                        //mRecyclerView.visibility = View.VISIBLE
                    }
                }, 3000)

            } else {
                googleMapPlaceSearchAutoCompleteRecyclerView.clearAddresses()
                mRecyclerView.visibility = View.GONE
            }

        }
    }

    fun getFullAddressUsingEdittextStringFromGooglePlaceSearchAPI(queryAddress: String) {
        val token = AutocompleteSessionToken.newInstance()
        val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery(queryAddress)
                .build()
        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    if(response.autocompletePredictions.size>0){
                        googleMapPlaceSearchAutoCompleteRecyclerView.updateList(response.autocompletePredictions)
                        mRecyclerView.visibility = View.VISIBLE
                    }else{
                        mRecyclerView.visibility = View.GONE
                    }
                }
                .addOnFailureListener { exception ->
                    if (exception is ApiException) {
                        DebuggableLogE(TAG, "Place not found: " + exception.statusCode)
                        //CommonMethods.showUserMessage("place not found "+ apiException.getStatusCode());
                    }
                    exception.printStackTrace()
                }
    }

    @OnClick(R.id.arrow)
    fun onArrow() {
        onBackPressed()
    }

    @OnClick(R.id.destclose)
    fun destclose() {
        /**
         * Destination address close button
         */
        val ll = LatLng(1.0, 1.0)
        markerPoints.set(1, ll)
        destadddress.setText("")
        destadddress.requestFocus()
        destadddress.addTextChangedListener(PlaceTextWatcher)
    }

    @OnClick(R.id.drop_done)
    fun drop_done() {
        val destup = destadddress.text.toString()



        if (destup.matches("".toRegex())) {
            address_search.visibility = View.VISIBLE
            mapFragment.view!!.visibility = View.GONE
            pin_map.visibility = View.GONE
            drop_done.visibility = View.GONE

            if (destup.matches("".toRegex())) {
                destadddress.requestFocus()
            }

        } else {


            sessionManager.isrequest=true
            address_search.visibility = View.VISIBLE
            mapFragment.view!!.visibility = View.GONE
            pin_map.visibility = View.GONE
            drop_done.visibility = View.GONE
            destadddress.removeTextChangedListener(PlaceTextWatcher)
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(destadddress.windowToken, 0)


            if (workaddress != null) {

                /**
                 * Update rider work address
                 */
                locationHashMap = HashMap()
                locationHashMap[API_KEY_LATITUDE] = lat.toString()
                locationHashMap[API_KEY_LONGITUDE] = log.toString()
                locationHashMap[API_KEY_WORK] = destup
                locationHashMap[API_KEY_TOKEN] = sessionManager.accessToken.toString()
                updateRiderLoc()

                val intent = Intent(this, Setting::class.java)
                intent.putExtra("worktextstr", destup)
                sessionManager.workAddress = destup
                startActivity(intent)
                finish()
            } else if (searchhome != null && searchhome == "searchhome") {

                /**
                 * Update rider home address
                 */
                getLocationFromAddress(destup)
                locationHashMap = HashMap()
                locationHashMap[API_KEY_LATITUDE] = lat.toString()
                locationHashMap[API_KEY_LONGITUDE] = log.toString()
                locationHashMap[API_KEY_HOME] = destup
                locationHashMap[API_KEY_TOKEN] = sessionManager.accessToken.toString()
                updateRiderLoc()
                val intent = Intent(this, PlaceSearchActivity::class.java)
                // intent.putExtra("searchhome",addressstring);
                val objLatLng = getIntent().extras?.getParcelable<LatLng>("Latlng")
                intent.putExtra("Latlng", objLatLng)
                intent.putExtra("Country", getIntent().getStringExtra("Country"))
                intent.putExtra("Work", getIntent().getStringExtra("Work"))
                intent.putExtra("Home", destup)
                intent.putExtra("Schedule", getIntent().getStringExtra("Schedule"))
                intent.putExtra("PickupAddress", getIntent().getStringExtra("PickupAddress"))
                intent.putExtra("DropAddress", getIntent().getStringExtra("DropAddress"))
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                sessionManager.homeAddress = destup
                startActivity(intent)
                finish()

            } else if (searchwork != null && searchwork == "searchwork") {

                getLocationFromAddress(destup)
                locationHashMap = HashMap()
                locationHashMap[API_KEY_LATITUDE] = lat.toString()
                locationHashMap[API_KEY_LONGITUDE] = log.toString()
                locationHashMap[API_KEY_WORK] = destup
                locationHashMap[API_KEY_TOKEN] = sessionManager.accessToken.toString()
                updateRiderLoc()

                val intent = Intent(this, PlaceSearchActivity::class.java)
                val objLatLng = getIntent().extras?.getParcelable<LatLng>("Latlng")
                intent.putExtra("Latlng", objLatLng)
                intent.putExtra("Country", getIntent().getStringExtra("Country"))
                intent.putExtra("Work", destup)
                intent.putExtra("Home", getIntent().getStringExtra("Home"))
                intent.putExtra("Schedule", getIntent().getStringExtra("Schedule"))
                intent.putExtra("PickupAddress", getIntent().getStringExtra("PickupAddress"))
                intent.putExtra("DropAddress", getIntent().getStringExtra("DropAddress"))
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                sessionManager.homeAddress = destup
                startActivity(intent)
                finish()

            } else {

                getLocationFromAddress(destup)
                locationHashMap = HashMap()
                locationHashMap[API_KEY_LATITUDE] = lat.toString()
                locationHashMap[API_KEY_LONGITUDE] = log.toString()
                locationHashMap[API_KEY_HOME] = destup
                locationHashMap[API_KEY_TOKEN] = sessionManager.accessToken.toString()
                updateRiderLoc()

                val intent = Intent(this, Setting::class.java)
                intent.putExtra("hometextstr", destup)
                sessionManager.homeAddress = destup
                startActivity(intent)
                finish()
            }


        }
    }

    @OnClick(R.id.setlocaion_onmap)
    fun setlocaion_onmap() {

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(destadddress.windowToken, 0)
        address_search.visibility = View.GONE
        mapFragment.view!!.visibility = View.VISIBLE
        pin_map.visibility = View.VISIBLE
        drop_done.visibility = View.VISIBLE
    }

    fun getIntentval() {
        /**
         * Get Address data from other activity
         */
        val intent = intent
        pickipCountry = getIntent().getStringExtra("Country")
        workaddress = intent.getStringExtra("workaddress")
        searchwork = intent.getStringExtra("searchwork")
        searchhome = intent.getStringExtra("searchhome")
        settinghome = intent.getStringExtra("settinghome")
        settingwork = intent.getStringExtra("settingwork")
        //destadddress.setText(getIntent().getStringExtra("DropAddress"))

        if (getIntent().getSerializableExtra("PickupDrop") != null) {
            markerPoints = getIntent().getSerializableExtra("PickupDrop") as ArrayList<Any>
            val lll = markerPoints.get(0) as LatLng
            destCountry = getCountry(lll.latitude, lll.longitude)
        }

    }

    fun setHints() {
        DebuggableLogD(TAG, "searchwork" + searchwork)
        if (searchwork != null && searchhome != null) {
            if ("searchwork" == searchwork) {
                destadddress.hint = getString(R.string.enterworkaddress)
            } else if ("searchhome" == searchhome) {
                destadddress.hint = getString(R.string.enterhomeaddress)
            }
        } else {

            if (workaddress != null && "workaddress" == workaddress) {
                destadddress.hint = getString(R.string.enterworkaddress)
            } else if (settinghome != null && "settinghome" == settinghome) {
                destadddress.hint = getString(R.string.enterhomeaddress)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildGoogleApiClient()
        setContentView(R.layout.app_activity_add_home)
        ButterKnife.bind(this)
        AppController.appComponent.inject(this)

        dialog = commonMethods.getAlertDialog(this)
        placesClient = com.google.android.libraries.places.api.Places.createClient(this)
        googleMapPlaceSearchAutoCompleteRecyclerView = GoogleMapPlaceSearchAutoCompleteRecyclerView(addressAutoCompletePredictions, this, this)
        showPermissionDialog()
        getIntentval()
        setHints()
        setHeader()
        initRecycleview()

        try {
            // Loading map
            initilizeMap()
        } catch (e: Exception) {
            e.printStackTrace()
        }


        /**
         * Destination address focus listener
         */
        destadddress.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                destadddress.addTextChangedListener(PlaceTextWatcher)
                address_search.visibility = View.VISIBLE
            }
        }

        /**
         * Place search list item click
         */


        isInternetAvailable = commonMethods.isOnline(applicationContext)

        if (!isInternetAvailable) {
            commonMethods.showMessage(this, dialog, getString(R.string.no_connection))
        }

    }

    private fun setHeader() {
        if(destadddress.hint == getString(R.string.enterhomeaddress)){
            commonMethods.setheaderText(resources.getString(R.string.add_home_location),common_header)
        }else{
            commonMethods.setheaderText(resources.getString(R.string.add_work_location),common_header)
        }
    }

    fun initRecycleview() {
        /**
         * Place search list
         */
        mRecyclerView.visibility = View.GONE
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.isNestedScrollingEnabled = false
        val mLinearLayoutManager = LinearLayoutManager(this)
        mRecyclerView.layoutManager = mLinearLayoutManager
        mRecyclerView.adapter = googleMapPlaceSearchAutoCompleteRecyclerView
        destadddress.addTextChangedListener(PlaceTextWatcher)
    }

    fun onTouchList(item: AutocompletePrediction?) {
        if (item != null) {

            val placeId = item.placeId
            DebuggableLogI("TAG", "Autocomplete item selected: " + item.getFullText(null))

            searchlocation = item.getFullText(null).toString()
            destadddress.setText(item.getFullText(null))
            val addressstring = searchlocation
            destadddress.removeTextChangedListener(PlaceTextWatcher)
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(destadddress.windowToken, 0)


            if (workaddress != null) {

                /**
                 * Update rider work address
                 */
                locationHashMap = HashMap()
                locationHashMap["latitude"] = lat.toString()
                locationHashMap["longitude"] = log.toString()
                locationHashMap["work"] = addressstring
                locationHashMap["token"] = sessionManager.accessToken.toString()
                updateRiderLoc()

                val intent = Intent(this, Setting::class.java)
                intent.putExtra("worktextstr", addressstring)
                sessionManager.workAddress = addressstring
                startActivity(intent)
                finish()
            } else if (searchhome != null && searchhome == "searchhome") {

                /**
                 * Update rider home address
                 */
                getLocationFromAddress(addressstring)
                locationHashMap = HashMap()
                locationHashMap["latitude"] = lat.toString()
                locationHashMap["longitude"] = log.toString()
                locationHashMap["home"] = addressstring
                locationHashMap["token"] = sessionManager.accessToken.toString()
                updateRiderLoc()
                val objLatLng = intent.extras?.getParcelable<LatLng>("Latlng")
                val intent = Intent(this, PlaceSearchActivity::class.java)
                // intent.putExtra("searchhome",addressstring);
                intent.putExtra("Latlng", objLatLng)
                intent.putExtra("Country", getIntent().getStringExtra("Country"))
                intent.putExtra("Work", getIntent().getStringExtra("Work"))
                intent.putExtra("Home", addressstring)
                intent.putExtra("Schedule", getIntent().getStringExtra("Schedule"))
                intent.putExtra("PickupAddress", getIntent().getStringExtra("PickupAddress"))
                intent.putExtra("DropAddress", getIntent().getStringExtra("DropAddress"))
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                sessionManager.homeAddress = addressstring
                startActivity(intent)
                finish()

            } else if (searchwork != null && searchwork == "searchwork") {

                getLocationFromAddress(addressstring)
                locationHashMap = HashMap()
                locationHashMap["latitude"] = lat.toString()
                locationHashMap["longitude"] = log.toString()
                locationHashMap["work"] = addressstring
                locationHashMap["token"] = sessionManager.accessToken.toString()
                updateRiderLoc()
                val objLatLng = intent.extras?.getParcelable<LatLng>("Latlng")
                val intent = Intent(this, PlaceSearchActivity::class.java)
                intent.putExtra("Latlng", objLatLng)
                intent.putExtra("Country", getIntent().getStringExtra("Country"))
                intent.putExtra("Work", addressstring)
                intent.putExtra("Home", getIntent().getStringExtra("Home"))
                intent.putExtra("Schedule", getIntent().getStringExtra("Schedule"))
                intent.putExtra("PickupAddress", getIntent().getStringExtra("PickupAddress"))
                intent.putExtra("DropAddress", getIntent().getStringExtra("DropAddress"))
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                sessionManager.homeAddress = addressstring
                startActivity(intent)
                finish()

            } else {

                getLocationFromAddress(addressstring)
                locationHashMap = HashMap()
                locationHashMap[API_KEY_LATITUDE] = lat.toString()
                locationHashMap[API_KEY_LONGITUDE] = log.toString()
                locationHashMap[API_KEY_HOME] = addressstring
                locationHashMap[API_KEY_TOKEN] = sessionManager.accessToken.toString()
                updateRiderLoc()

                val intent = Intent(this, Setting::class.java)
                intent.putExtra("hometextstr", addressstring)
                sessionManager.homeAddress = addressstring
                startActivity(intent)
                finish()
            }


            /*
               Issue a request to the Places Geo Data API to retrieve a Place object with additional details about the place.
                         */

            val placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId)
            placeResult.setResultCallback { places ->
                if (places.count == 1) {
                    val returnValues = getLocationFromAddress(this@AddHome, searchlocation)
                    val cur_Latlng = returnValues!!.latLng
                    Listlat = cur_Latlng.latitude.toString()
                    Listlong = cur_Latlng.longitude.toString()
                }
            }
            DebuggableLogI("TAG", "Clicked: " + item.getFullText(null))
            DebuggableLogI("TAG", "Called getPlaceById to get Place details for " + item.placeId)
        }
    }

    /**
     * get Address from location
     */
    fun getLocationFromAddress(context: Context, strAddress: String): ReturnValues? {

        var country: String? = null
        val coder = Geocoder(context)
        val address: List<Address>?
        var p1: LatLng? = null

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return null
            }
            country = address[0].countryName
            val location = address[0]
            location.latitude
            location.longitude

            p1 = LatLng(location.latitude, location.longitude)

        } catch (ex: IOException) {

            ex.printStackTrace()
        } catch (ex: ArrayIndexOutOfBoundsException) {

            ex.printStackTrace()
        }

        return ReturnValues(p1!!, country!!)
        //return p1;
    }

    /**
     * Google API client connect
     */
    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .build()
    }

    override fun onConnected(bundle: Bundle?) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        val mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient)

        if (mLastLocation != null) {
            changeMap(mLastLocation)
            DebuggableLogD(TAG, "ON connected")

        } else
            try {
                val mLocationRequest = LocationRequest()
                mLocationRequest.interval = 10000
                mLocationRequest.fastestInterval = 5000
                mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            } catch (e: Exception) {
                e.printStackTrace()
            }

    }

    override fun onConnectionSuspended(i: Int) {
        DebuggableLogI(TAG, "Connection suspended")
        mGoogleApiClient.connect()
    }


    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        DebuggableLogI(TAG, "Connection suspended")
    }

    /**
     * Move map to current location
     */
    private fun changeMap(location: Location) {

        DebuggableLogD(TAG, "Reaching map" + googleMap)


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }

        // check if map is created successfully or not
        if (googleMap != null) {
            googleMap?.uiSettings?.isZoomControlsEnabled = false
            val latLong: LatLng


            latLong = LatLng(location.latitude, location.longitude)

            val cameraPosition = CameraPosition.Builder()
                    .target(latLong).zoom(16.5f).tilt(0f).build()

            googleMap?.isMyLocationEnabled = true
            googleMap?.uiSettings?.isMyLocationButtonEnabled  = true
            googleMap?.moveCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition))

            // startIntentService(location);

        }

    }

    public override fun onResume() {
        super.onResume()
        if (!mGoogleApiClient.isConnected && !mGoogleApiClient.isConnecting) {
            DebuggableLogV("Google API", "Connecting")
            mGoogleApiClient.connect()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (mGoogleApiClient.isConnected) {
            DebuggableLogV("Google API", "Dis-Connecting")
            mGoogleApiClient.disconnect()
        }
    }

    @SuppressLint("UseRequireInsteadOfGet")
    private fun initilizeMap() {
        if (googleMap == null) {
            mapFragment = (supportFragmentManager.findFragmentById(R.id.search_map) as SupportMapFragment?)!!
            mapFragment.getMapAsync(this)
            mapFragment.view!!.visibility = View.GONE
            pin_map.visibility = View.GONE
            drop_done.visibility = View.GONE

        }
    }

    /**
     * Function to load map. If map is not created it will create it for you
     */
    override fun onMapReady(Map: GoogleMap) {
        googleMap = Map

        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = googleMap?.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style))

            if (!success!!) {
                DebuggableLogE("Cabme", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            DebuggableLogE("Cabme", "Can't find style. Error: ", e)
        }

        googleMap?.setOnCameraIdleListener(this)
        googleMap?.setOnCameraMoveStartedListener(this)
        googleMap?.setOnCameraMoveListener(this)
        googleMap?.setOnCameraMoveCanceledListener(this)

    }


    /**
     * Get full address details
     */

    private fun getCompleteAddressString(LATITUDE: Double?, LONGITUDE: Double?): String {
        // String strAdd = "";
        var address = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = LATITUDE?.let { LONGITUDE?.let { it1 -> geocoder.getFromLocation(it, it1, 1) } }
            if (addresses != null) {
                val test1 = addresses[0].countryName
                val test2 = addresses[0].adminArea
                val test3 = addresses[0].countryCode
                val test4 = addresses[0].featureName
                val test5 = addresses[0].maxAddressLineIndex
                val test6 = addresses[0].url
                address = addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                // String city = addresses.get(0).getLocality();
                // String state = addresses.get(0).getAdminArea();
                val country = addresses[0].countryName
                // String postalCode = addresses.get(0).getPostalCode();

                destCountry = country

                /*if (address != null) {
                    strAdd = address + " ";
                }

                if (city != null) {
                    strAdd = strAdd + city + " ";
                }

                if (state != null) {
                    strAdd = strAdd + state + " ";
                }
                if (country != null) {
                    strAdd = strAdd + country + " ";
                }

                if (postalCode != null) {
                    strAdd = strAdd + postalCode + " ";
                }*/

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return address
    }

    private fun getCountry(LATITUDE: Double, LONGITUDE: Double): String {
        var country = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                country = addresses[0].countryName
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Log.w("My Current loction address", "Canont get Address!");
        }

        return country
    }


    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.ub__slide_in_left, R.anim.ub__slide_out_right)
    }

    /**
     * Check location permission
     */
    private fun showPermissionDialog() {
        if (!Permission.checkPermission(this)) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION),
                    4)
        }
    }


    /**
     * Map move camera listener
     */
    override fun onCameraMoveStarted(reason: Int) {
        //Toast.makeText(this, "The camera moving Started.",Toast.LENGTH_SHORT).show();
    }

    override fun onCameraMove() {
        //Toast.makeText(this, "The camera is moving.",Toast.LENGTH_SHORT).show();
    }

    override fun onCameraMoveCanceled() {
        // Toast.makeText(this, "Camera movement canceled.",Toast.LENGTH_SHORT).show();
    }

    override fun onCameraIdle() {
        // Toast.makeText(this, "The camera has stopped moving.",Toast.LENGTH_SHORT).show();

        val cameraPosition = googleMap?.cameraPosition

        destadddress.removeTextChangedListener(PlaceTextWatcher)
        val address = getCompleteAddressString(cameraPosition?.target?.latitude, cameraPosition?.target?.longitude)

        destadddress.setText(address)

        Listlat = cameraPosition?.target?.latitude.toString()
        Listlong = cameraPosition?.target?.longitude.toString()
        DebuggableLogI("centerLat", cameraPosition?.target?.latitude.toString())
        DebuggableLogI("centerLong", cameraPosition?.target?.longitude.toString())
        val cur_Latlng = cameraPosition?.target?.latitude?.let { cameraPosition?.target?.longitude?.let { it1 -> LatLng(it, it1) } }
        if (intent.getSerializableExtra("PickupDrop") != null) {
            markerPoints = intent.getSerializableExtra("PickupDrop") as ArrayList<Any>
            val lll = markerPoints.get(0) as LatLng
            destCountry = getCountry(lll.latitude, lll.longitude)
        }
        // markerPoints.set(0, cur_Latlng);
        destadddress.addTextChangedListener(PlaceTextWatcher)

    }

    override fun onSuccess(jsonResp: JsonResponse, data: String) {
        if (jsonResp.isSuccess) {
            commonMethods.hideProgressDialog()
            if (jsonResp.statusCode == "2")
                commonMethods.hideProgressDialog()
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        } else if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
            // snackBar(jsonResp.getStatusMsg(), "hi", false, 2);
        }
    }

    override fun onFailure(jsonResp: JsonResponse, data: String) {
        if (!TextUtils.isEmpty(jsonResp.statusMsg)) {
            commonMethods.hideProgressDialog()
            commonMethods.showMessage(this, dialog, jsonResp.statusMsg)
        }
    }

    /**
     * Update Rider Location
     */
    fun updateRiderLoc() {
        commonMethods.showProgressDialog(this)
        apiService.uploadRiderLocation(locationHashMap).enqueue(RequestCallback(REQ_UPDATE_LOCATION, this))

    }

    /**
     * Get location from address
     */

    fun getLocationFromAddress(strAddress: String) {

        val coder = Geocoder(this)
        val address: List<Address>?
        var p1: LatLng? = null

        try {
            // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return
            }

            if (!address.isEmpty()) {
                val location = address[0]
                location.latitude
                location.longitude

                lat = location.latitude.toString()
                log = location.longitude.toString()

                p1 = LatLng(location.latitude, location.longitude)
            }

        } catch (ex: IOException) {

            ex.printStackTrace()
        }

        //return p1;
    }

    override fun selectedAddress(autocompletePrediction: AutocompletePrediction?) {
        onTouchList(autocompletePrediction)
    }

    companion object {

        private val BOUNDS_INDIA = LatLngBounds(
                LatLng(-0.0, 0.0), LatLng(0.0, 0.0))
        private val TAG = "MAP LOCATION"
    }
}