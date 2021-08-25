package com.example.keepplace.activities

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.keepplace.R
import com.example.keepplace.models.PlaceModel
import kotlinx.android.synthetic.main.activity_add_happy_place.*
import kotlinx.android.synthetic.main.activity_place.*
import kotlin.collections.Map

class place : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place)
        setSupportActionBar(toolbar_place);

        var placeDetail : PlaceModel? = null;
        if(intent.hasExtra("Place Details")){
            placeDetail = intent.getSerializableExtra("Place Details") as PlaceModel
        }

        if(placeDetail != null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true);
            toolbar_place.setNavigationOnClickListener {
                onBackPressed();
            }
            supportActionBar?.title = placeDetail.title

            place_date.setText(placeDetail.date)
            place_img.setImageURI(Uri.parse(placeDetail.image))
            place_description.setText(placeDetail.description)
            place_title_into.setText(placeDetail.title)

            seeOnMap.setOnClickListener{
                val conMan = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetwork = conMan.activeNetworkInfo
                val connected = activeNetwork != null && activeNetwork.isConnected
                if(connected == true){
                    val intent = Intent(this@place, See_Map::class.java)
                    intent.putExtra("data",placeDetail)
                    startActivity(intent);
                }else{
                    Toast.makeText(this@place,"Turn on the internet",Toast.LENGTH_LONG).show()
                }
            }
        }


    }
}
