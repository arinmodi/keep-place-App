package com.example.keepplace.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.keepplace.R
import com.example.keepplace.models.PlaceModel
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import kotlinx.android.synthetic.main.activity_map.*


class See_Map : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map : MapboxMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Mapbox.getInstance(this,getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_map);
        setSupportActionBar(toolbar_map);
        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        toolbar_map.setNavigationOnClickListener {
            onBackPressed()
        }
        supportActionBar?.title = "Your Place on Map"
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this)

    }

    override fun onMapReady(it: MapboxMap) {
        map = it
        it.setStyle(Style.MAPBOX_STREETS)
        val placeDetail = intent.getSerializableExtra("data") as PlaceModel
        val options = MarkerOptions();
        options.position(LatLng(placeDetail.latitude,placeDetail.longitude));
        it.addMarker(options)
        it.cameraPosition = CameraPosition.Builder().target(LatLng(placeDetail.latitude,placeDetail.longitude)).zoom(13.0).build()
        start_button.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr=${placeDetail.latitude},${placeDetail.longitude}")
            )
            startActivity(intent)
        }
    }


    public override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    public override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }


}
