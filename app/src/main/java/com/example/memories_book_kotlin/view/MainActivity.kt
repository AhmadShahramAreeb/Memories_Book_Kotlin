package com.example.memories_book_kotlin.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.example.memories_book_kotlin.R
import com.example.memories_book_kotlin.adapter.CharacterAdapter
import com.example.memories_book_kotlin.databinding.ActivityMainBinding
import com.example.memories_book_kotlin.model.Relation_Character
import com.example.memories_book_kotlin.roomdb.CharacterDatabase
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    //Rxjava Asynchron
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
            val view = binding.root
            setContentView(view)
        val db = Room.databaseBuilder(applicationContext,CharacterDatabase::class.java,"Characters").build()
        val characterDao = db.characterDao()

        compositeDisposable.add(
            characterDao.getAll()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResponse)
        )
    }

    private fun handleResponse(charachterList : List<Relation_Character>){
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = CharacterAdapter(charachterList)
        binding.recyclerView.adapter = adapter
    }


    //Add Item menu Main Activiy'e bağlamak için bu iki fonk. override etmek gerekir
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //burada bağlama işlemi yapılır
        //Infalte - xml ile Activiy bağlayan
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.add_menu,menu)
        return super.onCreateOptionsMenu(menu)

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //menu tıklanırsa hangi işlem yapılsın burada tanımlanır
        //doğru yere tıklama kontrölü
        if (item.itemId == R.id.add_character_item){
            val intent = Intent(this@MainActivity, MemoriesActivity::class.java)
            intent.putExtra("info" , "new")
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}