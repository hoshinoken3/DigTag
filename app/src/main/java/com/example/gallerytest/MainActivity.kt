package com.example.gallerytest

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ShareCompat
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    //画像のUri
    private var imgUri:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var pictureButton : Button = findViewById(R.id.button)

        //画像選択ボタンのイベントリスナー
        pictureButton.setOnClickListener {
            val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type="image/*"
            }
            startActivityForResult(intent, READ_REQUEST_CODE)
        }

        var shareButton : Button = findViewById(R.id.button2)
        //シェアボタンのイベントリスナー
        shareButton.setOnClickListener {
            openChooserToShare("test string",Uri.parse(imgUri))
        }

    }

    //暗黙的インテント(テスト済)
    private fun openChooserToShare(str:String,img:Uri){
        val sendIntent = Intent().apply {
            action=Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT,str)
            putExtra(Intent.EXTRA_STREAM,img)
            type="*/*"
        }
        val shareIntent=Intent.createChooser(sendIntent,null)
        startActivity(shareIntent)
    }

    //明示的インテント(Uriで送られたアプリを起動)(twitter:com.twitter.android)
    private fun openApplicationToShare(str:String,img:Uri,uri:String){
        if (isApplicationInstalled(this,uri)){
            val sendIntent = Intent().apply {
                action=Intent.ACTION_SEND
                data=Uri.parse(uri)
                putExtra(Intent.EXTRA_TEXT,str)
                putExtra(Intent.EXTRA_STREAM,img)
                type="*/*"
            }
            startActivity(sendIntent)
        }
        else{
            Toast.makeText(this, "there is not selected apps.", Toast.LENGTH_SHORT).show()
        }
    }

    //Uriで渡されたアプリが存在するか判定する関数
    private fun isApplicationInstalled(context:Context,uri:String): Boolean{
        val pm = context.packageManager
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
            return false
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
                        //resultDataのUriを取得
                        imgUri=uri.toString()
                        val inputStream=contentResolver?.openInputStream(uri)
                        val image=BitmapFactory.decodeStream(inputStream)
                        val imageView=findViewById<ImageView>(R.id.imageView)
                        imageView.setImageBitmap(image)
                        //ここでAIにBitmapの情報を流す
                    }
                }catch (e:Exception){
                    Toast.makeText(this,"error was occurred",Toast.LENGTH_LONG).show()
                }
            }
        }
    }

}