package com.enveramil.takenotesofart

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.enveramil.takenotesofart.databinding.ActivityDetailsBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.jar.Manifest

/**
 * ActivityResultLauncher : Geriye dönecek veriye göre işlem yapmaktadır.
 */
class DetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailsBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap : Bitmap? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        // Register Launcher

        registerLaunchers()
    }
    fun selectImage(view: View){
        if (ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            // izin verilmediğindeki durumu kontrol ediyoruz.
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@DetailsActivity,android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Permission needed",Snackbar.LENGTH_INDEFINITE).setAction("Give permission",View.OnClickListener {
                    // izin istenilecek
                    permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            }else{
                // izin istenecek
                permissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }else{
            // izin verildiği zamanki durumu kontrol ediyoruz.
            val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }
    }

    /**
     * Veritabanına görseli kaydetmeden önce resmi küçültme işlemi yapmamız gerekmektedir.
     */
    fun save(view: View){

        var imageName = binding.imageName.text.toString()
        var imageOwner = binding.imageOwner.text.toString()
        var imageYear = binding.imageYear.text.toString()

        if (selectedBitmap != null){
            val getSmallerBitmap = makeSmallerBitmap(selectedBitmap!!,300)

            var outputStream = ByteArrayOutputStream()
            getSmallerBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            var byteArray = outputStream.toByteArray()

            try {
                val database = this.openOrCreateDatabase("TakeNotes", MODE_PRIVATE,null)
                database.execSQL("CREATE TABLE IF NOT EXISTS notes (id INTEGER PRIMARY KEY, imageName VARCHAR, imageOwner VARCHAR, imageYear VARCHAR, image BLOB)")
                val sqlString = "INSERT INTO notes (imageName, imageOwner, imageYear,image) VALUES (?,?,?,?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,imageName)
                statement.bindString(2,imageOwner)
                statement.bindString(3,imageYear)
                statement.bindString(4, byteArray.toString())
                statement.execute()
            }catch (e : Exception){
                e.printStackTrace()
            }

            // Arka planda ne kadar aktivite varsa temizlenir ve istenilen yere gider
            val intent = Intent(this@DetailsActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }
    }

    private fun makeSmallerBitmap(image : Bitmap, maximumSize : Int) : Bitmap{
        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()
        if (bitmapRatio > 1){
            // landscape
            width = maximumSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }else {
            // portrait
            height = maximumSize
            val scaledHeight = height * bitmapRatio
            width = scaledHeight.toInt()
        }

        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    private fun registerLaunchers(){
        // Launcher for go to gallery and select the image
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if (result.resultCode == RESULT_OK){
                val intentFromdata = result.data
                if (intentFromdata != null){
                    val imageData = intentFromdata.data
                    //binding.imageView.setImageURI(imageData)
                    if (imageData != null) {
                        try {
                            // URI -> Bitmap çevirme işlemi
                            // ImageDecoder : Verileri decode edip görsele çeviren bir sınıftır.
                                // SDK kontrolü yapıyoruz.
                            if (Build.VERSION.SDK_INT >= 28) {
                                val source = ImageDecoder.createSource(this@DetailsActivity.contentResolver, imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }else{
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }

                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            result ->

            if (result){
                // permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                // permission denied
                Toast.makeText(this@DetailsActivity, "Permission needed",Toast.LENGTH_LONG).show()
            }
        }


    }
}