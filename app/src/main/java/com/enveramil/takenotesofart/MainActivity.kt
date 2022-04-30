package com.enveramil.takenotesofart

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.enveramil.takenotesofart.databinding.ActivityMainBinding
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private lateinit var noteList : ArrayList<NotesModel>
    private lateinit var notesAdapter : NoteAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        noteList = ArrayList<NotesModel>()
        getDataFromDatabase()

    }

    private fun getDataFromDatabase(){
        notesAdapter = NoteAdapter(noteList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = notesAdapter

        try {
            val database = this.openOrCreateDatabase("TakeNotes", MODE_PRIVATE,null)
            val cursor = database.rawQuery("SELECT * FROM notes",null)
            val idIndex = cursor.getColumnIndex("id")
            val nameIndex = cursor.getColumnIndex("imageName")

            while (cursor.moveToNext()){
                val id = cursor.getInt(idIndex)
                val name = cursor.getString(nameIndex)
                val notes = NotesModel(id, name)
                noteList.add(notes)
            }
            // notifyDataSetChanged() : Veri setimiz değişti kendine gel metodu
            notesAdapter.notifyDataSetChanged()
            cursor.close()
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val menuInflater = getMenuInflater()
        menuInflater.inflate(R.menu.menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_notes){
            val intent = Intent(this@MainActivity, DetailsActivity::class.java)
            intent.putExtra("info","new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

}