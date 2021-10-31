package com.example.gallerytest

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var pictureButton : Button = findViewById(R.id.button)

        //ボタンのイベントリスナー
        pictureButton.setOnClickListener {
            val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type="image/*"
            }
            startActivityForResult(intent, READ_REQUEST_CODE)
        }
    }

    companion object{
        private const val READ_REQUEST_CODE:Int=42
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if(resultCode!= RESULT_OK){
            return
        }

        when(requestCode){
            READ_REQUEST_CODE->{
                try{
                    resultData?.data?.also {uri->
                        val inputStream=contentResolver?.openInputStream(uri)
                        val image=BitmapFactory.decodeStream(inputStream)
                        val imageView=findViewById<ImageView>(R.id.imageView)
                        imageView.setImageBitmap(image)
                    }
                }catch (e:Exception){
                    Toast.makeText(this,"error was occurred",Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}