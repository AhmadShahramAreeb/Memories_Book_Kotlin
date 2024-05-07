package com.example.memories_book_kotlin.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.memories_book_kotlin.R
import com.example.memories_book_kotlin.databinding.ActivityMemoriesBinding
import com.example.memories_book_kotlin.model.Relation_Character
import com.example.memories_book_kotlin.roomdb.CharacterDao
import com.example.memories_book_kotlin.roomdb.CharacterDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.io.ByteArrayOutputStream

class MemoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemoriesBinding

    //ActivityResultLauncher
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent> // Intent yapmak için ve veriyi almak için
    private lateinit var permissionLauncher : ActivityResultLauncher<String> // İzinleri istemek için , izinler String tipinde'dir

    //Bitmap değişkeni tanımlama
    var selectedBitmap : Bitmap? = null

    //Database
    private lateinit var db : CharacterDatabase
    private lateinit var characterDao : CharacterDao

    //Rxjava Asynchron
    val compositeDisposable = CompositeDisposable()   // işlem sonunca gelen verileri temizler

    //Character From Main
    var characterFromMain : Relation_Character? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemoriesBinding.inflate(layoutInflater)
            val view = binding.root
            setContentView(view)

        //ActivityResultLauncher Initialization
        registerLauncher()

        //Database Initialization
        db = Room.databaseBuilder(applicationContext,CharacterDatabase::class.java,"Characters").build()
        characterDao = db.characterDao()

        val intent = intent

        val info = intent.getStringExtra("info")
        if (info.equals("new")){
            binding.fullNameText.setText("")
            binding.firstMemoryText.setText("")
            binding.secondMemoryText.setText("")
            binding.saveButton.visibility = View.VISIBLE
            binding.deleteButton.visibility = View.GONE
            binding.imageView.setImageResource(R.drawable.uploadimagehere)
            } else  {
                binding.saveButton.visibility = View.GONE
                characterFromMain = intent.getSerializableExtra("selectedCharacter") as? Relation_Character

                characterFromMain?.let {
                    val byteArray = it.image
                    val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray!!.size)
                    binding.imageView.setImageBitmap(bitmap)
                    binding.fullNameText.setText(it.fullName)
                    binding.firstMemoryText.setText(it.firstMemory)
                    binding.secondMemoryText.setText(it.secondMemory)
                    binding.poemText.setText(it.poem)
                }

        }
    }

    fun selectImage(view:View){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
        // Android 33+ -> READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                    //İzin verilmedi mi ? Evet izin verilmemiş daha önce

                        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                            //rational - automatic permission checker , android will decide
                            Snackbar.make(view,"Permission requires for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Allow Permission",View.OnClickListener {
                                //request permission
                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            }).show()

                        } else {
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }
                    } else  {
                        //İzin daha önce alınmış
                        val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        activityResultLauncher.launch(intentToGallery)
                    }
        } else {
            // Android 32 -> READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    //İzin verilmedi mi ? Evet izin verilmemiş daha önce

                        if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                            //rational - automatic permission checker , android will decide
                            Snackbar.make(view,"Permission requires for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Allow Permission",View.OnClickListener {
                                //request permission
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }).show()

                        } else {
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                    } else  {
                        //İzin daha önce alınmış
                        val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        activityResultLauncher.launch(intentToGallery)
                    }
        }

    }

    fun saveButtonClicked(view:View){
        val fullName = binding.fullNameText.text.toString()
        val relation = binding.relationshipText.text.toString()
        val firstMemory = binding.firstMemoryText.text.toString()
        val poem = binding.poemText.text.toString()
        var imageByteArray : ByteArray = ByteArray(0)
        var secondMemory : String=""
        try {
            secondMemory=binding.secondMemoryText.text.toString()
        } catch (e : Exception){
            e.printStackTrace()
        }

        if (selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            imageByteArray = outputStream.toByteArray()
        }

        if(fullName != null && relation != null && firstMemory != null && secondMemory != null){
            val relationCharacter = Relation_Character(imageByteArray,fullName,relation,firstMemory,secondMemory,poem)
            //characterDao.insert(relationCharacter)
            compositeDisposable.add(
                characterDao.insert(relationCharacter)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse /*bu fonk. referans verildi*/)
            )
        }

    }

    private fun handleResponse(){
        // save fonk. tamamlaninca bu işlemler yapılacak
        val intent = Intent(this,MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)

    }

    fun deleteButtonClicked(view:View){
        characterFromMain.let {
            compositeDisposable.add(
                        characterDao.delete(it!!)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::handleResponse)
                    )
        }


    }

    override fun onDestroy() {
        //Büyük uygulamalarda gerekli akışta olan veriler çok fazla yer kaplar
        super.onDestroy()
        compositeDisposable.clear()
    }

    private fun makeSmallerBitmap(image : Bitmap, maximumSize : Int) : Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble()/height.toDouble()

        if (bitmapRatio > 1){
            //lanscape
            width = maximumSize
            val scaledHeight = width/bitmapRatio
            height = scaledHeight.toInt()
        } else {
            //portrait
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        //küçülten veya büyüten Bitmap
        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(/*bir aktivite başlatır gallerye gider*/ ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) { // Gallery den görsel seçme işlemi yapıldı mı? kontrölü
                //görsel adresini alma işlemi
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    val imageUri = intentFromResult.data
                    //binding.imageView.setImageURI(imageUri) bitMap kullanmak gerek
                    if (imageUri != null) {
                        try {
                            if (Build.VERSION.SDK_INT >= 28){
                                val source = ImageDecoder.createSource(this@MemoriesActivity.contentResolver,imageUri)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageUri)
                                binding.imageView.setImageBitmap(selectedBitmap)


                            }


                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                }

            }
        }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission(),){ result ->
            if (result){
                //permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            } else {
                //permission denied
                Toast.makeText(this@MemoriesActivity,"Permission is Necessary",Toast.LENGTH_LONG).show()
            }
        }

    }
}