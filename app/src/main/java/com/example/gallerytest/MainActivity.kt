package com.example.gallerytest

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import java.util.*
import java.text.SimpleDateFormat;


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

        //カメラ起動ボタン
        var cameraButton : Button=findViewById(R.id.launchCameraButton)

        //カメラ起動ボタンのイベントリスナー
        cameraButton.setOnClickListener {
            Log.i("cameraButton","Button Pushed")
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).resolveActivity(packageManager)?.let {
                if (checkPermission()) {
                    takePicture()
                } else {
                    grantCameraPermission()
                }
            } ?: Toast.makeText(this, "camera app error", Toast.LENGTH_LONG).show()
        }

        //Twitterシェアボタン
        var shareOnTwitterButton : ImageButton = findViewById(R.id.shareOnTwitterImageButton)
        //Twitterシェアボタンのイベントリスナー
        shareOnTwitterButton.setOnClickListener {
            openApplicationToShare("test text",Uri.parse(imgUri),"com.twitter.android","https://twitter.com/")
        }
        //Instagramシェアボタン
        var shareOnInstagramButton : ImageButton = findViewById(R.id.shareOnInstagramImageButton)
        //Twitterシェアボタンのイベントリスナー
        shareOnInstagramButton.setOnClickListener {
            openApplicationToShare("test text",Uri.parse(imgUri),"com.instagram.android","https://instagram.com/")
        }
        //他アプリシェアボタン
        var shareOnOtherAppsButton : ImageButton = findViewById(R.id.shareOnOtherAppsImageButton)
        //他アプリシェアボタンのイベントリスナー
        shareOnOtherAppsButton.setOnClickListener {
            openChooserToShare("test string",Uri.parse(imgUri))
        }

        //ハッシュタグのチェックボックス初期化
        initCheckBoxes()

        //テストテキスト
        val taglist = listOf("#春から中大あいあああああああああああああ","#花から中大あああいああああああああああああああいああああああああああああああいあああああああああああああ")//"#ウマだいすきあいあああああああああああああ","#Aoiちゃん","#いきてる","#Aoiちゃん","#いきてる","test tags")
        setTextOnCheckBoxes(taglist)
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
        return try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    //チェックボックスの初期化
    private fun initCheckBoxes(){
        for(i in 1..8){
            val s:String= "tagbox$i"
            val boxid=resources.getIdentifier(s,"id",packageName)
            val checkBox=findViewById<CheckBox>(boxid)
            checkBox.isChecked=false
            checkBox.isVisible=true
            checkBox.setText("")
        }
    }

    //チェックボックスにハッシュタグを入れる
    private fun setTextOnCheckBox(tag:String, boxNumber:Int){
        val s:String= "tagbox$boxNumber"
        val boxid=resources.getIdentifier(s,"id",packageName)
        val tagBox=findViewById<CheckBox>(boxid)
        if(tagBox==null) println("there is no pointer of tagbox "+boxNumber)
        else if(tag.length>20){
            tagBox.setText("ハッシュタグが長すぎます")
            tagBox.isChecked=true
            tagBox.isVisible=true
        }
        else{
            tagBox.setText(tag)
            tagBox.isChecked=true
            tagBox.isVisible=true
        }

    }

    //チェックボックスのリストにハッシュタグを入れる
    private fun setTextOnCheckBoxes(tagList: List<String>){
        var cnt=1
        for(tag in tagList){
            setTextOnCheckBox(tag,cnt)
            if(tagList.size<=4) cnt+=2
            else cnt++
        }
    }

    //チェックボックスのタグをテキスト化して返す
    private fun getSelectedTags(): String {
        var tags=""
        for(i in 1..8){
            val s:String= "tagbox$i"
            val boxid=resources.getIdentifier(s,"id",packageName)
            val checkBox=findViewById<CheckBox>(boxid)
            if(checkBox.isChecked){
                tags+=checkBox.text
                tags+=" "
            }
        }
        return tags
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
        private const val CAMERA_REQUEST_CODE = 1
        private const val CAMERA_PERMISSION_REQUEST_CODE = 2
        private const val READ_REQUEST_CODE:Int=42
    }

    //カメラアプリを起動し、写真を撮る関数
    private fun takePicture() {
        Log.i("cameraButton","start camera")
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            putExtra(MediaStore.EXTRA_OUTPUT, createSaveFileUri())
        }

        startActivityForResult(intent, CAMERA_REQUEST_CODE)
        Log.i("cameraButton","end camera")
    }

    //アクセス許可を確認する関数
    private fun checkPermission(): Boolean {
        Log.i("cameraButton","check permission")
        val cameraPermission = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val extraStoragePermission = PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        Log.i("camera permission",cameraPermission.toString())
        Log.i("exstorage permission",extraStoragePermission.toString())
        return cameraPermission && extraStoragePermission
    }

    //アクセス許可を申請する関数
    private fun grantCameraPermission(){
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), CAMERA_PERMISSION_REQUEST_CODE)
    }

    //カメラで撮った画像のUriを返す関数
    private fun createSaveFileUri(): Uri {
        //タイムスタンプ
        val timeStamp:String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.JAPAN).format(Date())
        val imageFileName:String = "DigTag_$timeStamp"

        //保存先のディレクトリ指定
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_DCIM + "/DigTag")
        Log.i("timestamp",timeStamp)
        Log.i("imageFileName",imageFileName)

        //ディレクトリがない場合作成
        if (storageDir != null) {
            Log.i("storageDir",storageDir.absolutePath)
            Log.i("dir exist",storageDir.exists().toString())
            if (!storageDir.exists()) {
                storageDir.mkdir()
                Log.i("create dir",storageDir.exists().toString())
            }
        }

        //キャッシュファイルを作成
        val file = File.createTempFile(imageFileName,".jpg",storageDir)
        imgUri = file.absolutePath

        //ギャラリーからアクセスできるようにする
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            mediaScanIntent.data = Uri.fromFile(file)
            sendBroadcast(mediaScanIntent)
        }

        /*
        Intent(Intent.ACTION_).also { mediaScanIntent ->
            mediaScanIntent.data = Uri.fromFile(file)
            sendBroadcast(mediaScanIntent)
        }
         */
        return FileProvider.getUriForFile(this, "DigTag", file)
    }

    //許可を受け取ったとき呼ばれる関数
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        var isGranted = true
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty()) {
                grantResults.forEach {
                    if (it != PackageManager.PERMISSION_GRANTED) {
                        isGranted = false
                    }
                }
            } else {
                isGranted = false
            }
        } else {
            isGranted = false
        }

        if (isGranted) {
            takePicture()
        } else {
            grantCameraPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if(resultCode!= RESULT_OK){
            return
        }

        when(requestCode){
            //カメラアプリから帰ってきたとき
            CAMERA_REQUEST_CODE->{
                try{
                    //カメラアプリから画像の情報を受け取る
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put("_data", imgUri)
                    }
                    contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    val inputStream = FileInputStream(File(imgUri))
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val ib=findViewById<ImageView>(R.id.imageButton)
                    ib.setImageBitmap(bitmap)
                }
                catch (e:Exception){
                    Toast.makeText(this,"error was occurred while taking picture",Toast.LENGTH_LONG).show()
                }
            }

            //画像を選択したとき
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
                    Toast.makeText(this,"error was occurred while selecting image",Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}