package com.example.keepplace.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.keepplace.R
import com.example.keepplace.adapters.PlaceAdpter
import com.example.keepplace.database.DatabaseHandler
import com.example.keepplace.models.PlaceModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        var UPDATE_DATA = 1;
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == UPDATE_DATA){
                Toast.makeText(this,"Place Added...",Toast.LENGTH_LONG).show();
                getData()
            }else if(requestCode == 2){
                Toast.makeText(this,"Place Updated...",Toast.LENGTH_LONG).show();
                getData()
            }else{
                Log.e("Activity","Cancelled or Back Pressed")
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FABUTTON.setOnClickListener {
            val intent = Intent(this, addHappyPlace::class.java);
            startActivityForResult(intent,UPDATE_DATA);
        }

        val uneven_gird  = GridLayoutManager(this,2,LinearLayoutManager.VERTICAL,false)
        rv_places_list.layoutManager = uneven_gird

        getData();

    }

    private fun getData(){
        val dbHandler  = DatabaseHandler(this);
        val data : ArrayList<PlaceModel> = dbHandler.getPlaces();
        if(data.size > 0){
            rv_places_list.visibility = View.VISIBLE;
            text.visibility = View.GONE
            noplace.visibility = View.GONE
            setData(data);
        }else{
            rv_places_list.visibility = View.GONE;
            text.visibility = View.VISIBLE
            noplace.visibility = View.VISIBLE
        }
    }

    private fun setData(placelist : ArrayList<PlaceModel>){
        val placesAdpter = PlaceAdpter(this,placelist);
        rv_places_list.adapter = placesAdpter

        placesAdpter.setOnClickListener(object : PlaceAdpter.OnClickListener{
            override fun onClick(position: Int, model: PlaceModel) {
                print("Clicked")
                val intent = Intent(this@MainActivity,place::class.java);
                intent.putExtra("Place Details",model);
                startActivity(intent)
            }

            override fun onMoreClick(position: Int, model: PlaceModel, v : View,list : ArrayList<PlaceModel>) {
                val popup = PopupMenu(this@MainActivity,v);
                val menu = popup.menuInflater;
                menu.inflate(R.menu.item_pop_up,popup.menu);
                popup.show()
                popup.setOnMenuItemClickListener{
                    onMenuItemClick(it,model,this@MainActivity,position,placesAdpter,list)
                }
            }
        })

    }

    private fun onMenuItemClick(Item : MenuItem, model : PlaceModel, context: Context, position: Int,adpter : PlaceAdpter,list : ArrayList<PlaceModel>) : Boolean{

        when(Item.itemId){
            R.id.edit -> {
                val intent = Intent(context,addHappyPlace::class.java);
                intent.putExtra("data",model);
                startActivityForResult(intent,2);
                adpter.notifyItemChanged(position);
                return true
            }

            R.id.delete -> {
                val Dbhabdler = DatabaseHandler(context);
                val isDeleted = Dbhabdler.deletPlace(model);
                if(isDeleted > 0){
                    list.removeAt(position);
                    adpter.notifyItemRemoved(position)
                    Toast.makeText(context,"Place deleted",Toast.LENGTH_LONG).show()
                    getData()
                    return true
                }else{
                    return true
                }
            }

            else -> {
                return false
            }
        }

    }

}

