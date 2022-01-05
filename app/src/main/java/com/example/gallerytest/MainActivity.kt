package com.example.gallerytest

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    //画像のUri
    private var imgUri:String=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoTitleBar);
        setContentView(R.layout.activity_main)

        //画像選択ボタン
        var imageSelectButton : ImageButton = findViewById(R.id.imageButton)

        //画像選択ボタンのイベントリスナ
        imageSelectButton.setOnClickListener{
            val intent=Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type="image/*"
            }
            startActivityForResult(intent, READ_REQUEST_CODE)
        }

        //Twitterシェアボタン
        var shareOnTwitterButton : Button = findViewById(R.id.shareOnTwitterButton)
        //Twitterシェアボタンのイベントリスナー
        shareOnTwitterButton.setOnClickListener {
            openApplicationToShare("test text",Uri.parse(imgUri),"com.twitter.android","https://twitter.com/")
        }
        //他アプリシェアボタン
        var shareOnOtherAppsButton : Button = findViewById(R.id.shareOnOtherAppsButton)
        //他アプリシェアボタンのイベントリスナー
        shareOnOtherAppsButton.setOnClickListener {
            openChooserToShare("test string",Uri.parse(imgUri))
        }

        //
        val taglist = listOf("#春から中大あいあああああああああああああ","#花から中大あああいああああああああああああああいああああああああああああああいあああああああああああああ","#ウマだいすきあいあああああああああああああ","#Aoiちゃん","#いきてる")
        val tagcheckList = arrayOf(0,0,0,0,0,0)
        /*
        val tagcheckBox = listOf(
            findViewById<CheckBox>(R.id.checkBox1),
            findViewById<CheckBox>(R.id.checkBox2),
            findViewById<CheckBox>(R.id.checkBox3),
            findViewById<CheckBox>(R.id.checkBox4),
            findViewById<CheckBox>(R.id.checkBox5),
            findViewById<CheckBox>(R.id.checkBox6)
        )
        setCheckBoxes(taglist,tagcheckBox,tagcheckList);
        */
    }

    //暗黙的インテント
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

    //明示的インテント(Uriで送られたアプリを起動)
    private fun openApplicationToShare(str:String,img:Uri,appuri:String,appadd:String){
        if (isApplicationInstalled(this,appuri)){
            val intent=Intent(Intent.ACTION_SEND,Uri.parse(appadd)).apply {
                setPackage(appuri)
                putExtra(Intent.EXTRA_TEXT,str)
                putExtra(Intent.EXTRA_STREAM,img)
                type="*/*"
            }
            startActivity(intent)
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

    //チェックボックス管理
    private fun setCheckBoxes(l: List<String>, checkbox:List<CheckBox>, checklist:Array<Int>){
        var showCounter = 0
        var skippedCounter = 0
        for( i in 0..5){
            if ( showCounter < l.size){

                if(l[showCounter].length>8){
                    if(i%2==0){
                        checkbox[i].setText(l[showCounter])
                        showCounter++;
                        skippedCounter++;
                        continue;

                    }else{
                        checkbox[i].setVisibility(View.GONE);
                        checklist[i] = -1
                        if(l[showCounter-1].length>8) continue
                        skippedCounter++;
                        continue;
                    }
                }
                if(i==(showCounter+skippedCounter)){
                    checkbox[i].setText(l[showCounter])
                    showCounter++;
                }else{
                    checkbox[i].setVisibility(View.GONE);
                    checklist[i] = -1
                }
            }else{
                checkbox[i].setVisibility(View.GONE);
                checklist[i] = -1
            }
        }
        for ( i in 0..5){
            checkbox[i].setOnClickListener {
                Toast.makeText(this, checkbox[i].isChecked.toString(), Toast.LENGTH_SHORT).show()

                if(checkbox[i].isChecked){
                    checklist[i] = 1
                }else{
                    checklist[i] = 0
                }
            }
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
                        val ib=findViewById<ImageView>(R.id.imageButton)
                        ib.setImageBitmap(image)
                        //ここでAIにBitmapの情報を流す
                    }
                }catch (e:Exception){
                    Toast.makeText(this,"error was occurred",Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}