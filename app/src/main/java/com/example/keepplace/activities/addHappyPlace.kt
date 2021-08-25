package com.example.keepplace.activities

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.keepplace.R
import com.example.keepplace.database.DatabaseHandler
import com.example.keepplace.models.PlaceModel
import com.example.keepplace.utils.NetworkChangeReceiver
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import kotlinx.android.synthetic.main.activity_add_happy_place.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class addHappyPlace : AppCompatActivity(), View.OnClickListener {

    private var cal = Calendar.getInstance();
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener;
    private var saveImageToInternalStorage : Uri? = null;
    private var longitude : Double = 0.0;
    private var latitude : Double = 0.0;
    private var PlaceDetails : PlaceModel? = null;
    private lateinit var userCurruntLocation : FusedLocationProviderClient
    lateinit var networkChangeReciver : NetworkChangeReceiver
    lateinit var no_intenet : no_internet_dialog

    companion object {
        private const val CAMERA_PERMISSION_CODE = 1;
        private const val CAMERA_CODE = 2;
        private const val GALLERY_PERMISSION_CODE = 3;
        private const val GALLERY_CODE = 4;
        private const val IMAGE_DIRECTORY = "KEEP-PLACE"
        private const val REQUEST_CODE_AUTOCOMPLETE = 5
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_add_happy_place);

        no_intenet = no_internet_dialog(this)

        networkChangeReciver = NetworkChangeReceiver(object : NetworkChangeReceiver.NetworkChangeListener {
            override fun onNetworkConnectedStateChanged(connected: Boolean) {
                if(connected){
                    no_intenet.dismiss()
                }else{
                    no_intenet.show()
                }
            }
        })

        registerReceiver(networkChangeReciver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))

        if(intent.hasExtra("data")){
            PlaceDetails = intent.getSerializableExtra("data") as PlaceModel
        }
        setSupportActionBar(toolbar);
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener {
            onBackPressed();
        }

        userCurruntLocation = LocationServices.getFusedLocationProviderClient(this)

        dateSetListener = DatePickerDialog.OnDateSetListener {
                view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            UpdateDate()
        }

        UpdateDate()

        if(PlaceDetails != null){
            supportActionBar?.title = "Edit Place"

            place_title.setText(PlaceDetails!!.title)
            Description.setText(PlaceDetails!!.description)
            Date.setText(PlaceDetails!!.date)
            LOCATION.setText(PlaceDetails!!.location)
            latitude = PlaceDetails!!.latitude
            longitude = PlaceDetails!!.longitude

            saveImageToInternalStorage = Uri.parse(PlaceDetails!!.image);
            seletedimage.setImageURI(saveImageToInternalStorage);
            btn_txt.text = "UPDATE"

        }

        Date.setOnClickListener(this);
        gallery.setOnClickListener(this);
        camera.setOnClickListener(this);
        save.setOnClickListener(this);
        LOCATION.setOnClickListener(this);
        current_location.setOnClickListener(this);

    }

    private fun saveImageToInternalStorage(bitmap : Bitmap) : Uri{
        val wrapper = ContextWrapper(applicationContext);
        var file = wrapper.getDir(IMAGE_DIRECTORY,Context.MODE_PRIVATE);
        file = File(file, "${UUID.randomUUID()}.jpg");

        try{
            val stream : OutputStream = FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e : IOException){
            e.printStackTrace()
        }

        return Uri.parse(file.absolutePath);
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == CAMERA_CODE){
                if(data!! != null){
                    val image : Bitmap = data!!.extras!!.get("data") as Bitmap
                    saveImageToInternalStorage = saveImageToInternalStorage(image)
                    seletedimage.setPadding(0,0,0,0);
                    seletedimage.setImageBitmap(image);
                }else{
                    Toast.makeText(this,"something bad happen",Toast.LENGTH_LONG).show()
                }
            }

            if(requestCode == GALLERY_CODE){
                if(data!! != null){
                    val imageuri = data!!.data
                    try{
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageuri);
                        saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap);
                        seletedimage.setPadding(0,0,0,0);
                        seletedimage.setImageBitmap(selectedImageBitmap);
                    }catch (e : IOException){
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(this,"something bad happen",Toast.LENGTH_LONG).show()
                }
            }

            if(requestCode == REQUEST_CODE_AUTOCOMPLETE){
                val feature = PlaceAutocomplete.getPlace(data)
                println(feature)
                if(feature != null){
                    LOCATION.setText(feature.text())
                    val gc = Geocoder(this@addHappyPlace,Locale.getDefault());
                    try {

                        var address : List<Any> = gc.getFromLocationName(feature.text(), 1)
                        if(address != null && address.size > 0){
                            val add : Address = address.get(0) as Address
                            latitude = add.latitude
                            longitude = add.longitude
                        }
                    }catch (e : IOException){
                        LOCATION.setText("")
                        Toast.makeText(this@addHappyPlace, " please select a landmark",Toast.LENGTH_LONG).show()
                    }
                }else{
                    Toast.makeText(this@addHappyPlace, "Netwrok Error",Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == CAMERA_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,
                    CAMERA_CODE
                )
            }else{
                Toast.makeText(this, "Permission Denied",Toast.LENGTH_LONG).show();
            }
        }

        if(requestCode == GALLERY_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,
                    GALLERY_CODE
                )
            }else{
                Toast.makeText(this, "Permission Denied",Toast.LENGTH_LONG).show();
            }
        }

    }

    private fun accessCurruntLocation(){
        progress.visibility = View.VISIBLE
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000
        locationRequest.numUpdates = 1
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        userCurruntLocation.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            val mlastlocation = p0!!.lastLocation
            latitude = mlastlocation.latitude
            longitude = mlastlocation.longitude
            val gc = Geocoder(this@addHappyPlace,Locale.getDefault());
            var address = gc.getFromLocation(latitude,longitude,1).get(0)
            var string = StringBuilder();
            for(i in 0..address.maxAddressLineIndex){
                string.append(address.getAddressLine(i)).append(" ")
            }
            string.deleteCharAt(string.length - 1)
            LOCATION.setText(string)
            progress.visibility = View.GONE
        }
    }

    private fun isLocationEnabled() : Boolean {
        val locationManger : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager;
        return locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManger.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onClick(v: View?) {
        when(v!!.id){

            R.id.Date -> {
                DatePickerDialog(this@addHappyPlace, dateSetListener,cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
            }

            R.id.gallery -> {
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    val intent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent,
                        GALLERY_CODE
                    )
                }else{
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                        GALLERY_PERMISSION_CODE
                    );
                }
            }

            R.id.camera -> {
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,
                        CAMERA_CODE
                    )
                }else{
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                        CAMERA_PERMISSION_CODE
                    );
                }
            }

            R.id.LOCATION -> {
                val intent = PlaceAutocomplete.IntentBuilder()
                    .accessToken(resources.getString(R.string.mapbox_access_token))
                    .placeOptions(PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10).hint("Serch Location")
                        .build(PlaceOptions.MODE_CARDS))
                    .build(this)
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE)

            }

            R.id.current_location -> {
                if(!isLocationEnabled()){
                    Toast.makeText(this,"Location Provider is off, please turn on",Toast.LENGTH_LONG).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }else{
                    Dexter.withActivity(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object : MultiplePermissionsListener{
                        override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                            if(p0!!.areAllPermissionsGranted()){
                                accessCurruntLocation()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            p0: MutableList<PermissionRequest>?,
                            p1: PermissionToken?
                        ) {
                            Toast.makeText(this@addHappyPlace,"Permission Denied",Toast.LENGTH_LONG).show()
                        }

                    }).onSameThread().check()
                }
            }

            R.id.save -> {
                when {
                    place_title.text.isNullOrEmpty() -> {
                        var snakebar = Snackbar.make(v, "Title empty...", Snackbar.LENGTH_LONG);
                        snakebar.setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                        snakebar.show()
                    }

                    Description.text.isNullOrEmpty() -> {
                        var snakebar = Snackbar.make(v, "Description empty...", Snackbar.LENGTH_LONG);
                        snakebar.setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                        snakebar.show()
                    }

                    LOCATION.text.isNullOrEmpty() -> {
                        var snakebar = Snackbar.make(v, "Select a Location...", Snackbar.LENGTH_LONG);
                        snakebar.setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                        snakebar.show()
                    }

                    saveImageToInternalStorage == null -> {
                        var snakebar = Snackbar.make(v, "Select a Image...", Snackbar.LENGTH_LONG);
                        snakebar.setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                        snakebar.show()
                    }

                    else -> {
                        val happyPlaceModel = PlaceModel(
                            if(PlaceDetails == null) 0 else PlaceDetails!!.id,
                            place_title.text.toString(),
                            saveImageToInternalStorage.toString(),
                            Description.text.toString(),
                            Date.text.toString(),
                            LOCATION.text.toString(),
                            latitude,
                            longitude
                        )

                        val dbHandler = DatabaseHandler(this);
                        if(PlaceDetails == null){
                            val result = dbHandler.addPlace(happyPlaceModel);

                            if(result > 0){
                                setResult(Activity.RESULT_OK)
                                dbHandler.close()
                                finish()
                            }else{
                                var snakebar = Snackbar.make(v, "Something Bad Happen Try Again...", Snackbar.LENGTH_LONG);
                                snakebar.setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                                snakebar.show()
                            }
                        }else{
                            val result = dbHandler.updatePlace(happyPlaceModel);

                            if(result > 0){
                                setResult(Activity.RESULT_OK)
                                dbHandler.close()
                                finish()
                            }else{
                                var snakebar = Snackbar.make(v, "Something Bad Happen Try Again...", Snackbar.LENGTH_LONG);
                                snakebar.setActionTextColor(resources.getColor(android.R.color.holo_red_light))
                                snakebar.show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(networkChangeReciver)
        no_intenet.dismiss()
        super.onDestroy()
    }

    private fun UpdateDate(){
        val Formate = "dd-MM-yyyy";
        val sdf = SimpleDateFormat(Formate, Locale.getDefault());
        Date.setText(sdf.format(cal.time).toString());
    }

}
