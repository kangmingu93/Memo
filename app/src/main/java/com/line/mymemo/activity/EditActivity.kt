package com.line.mymemo.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.dialog.MaterialDialogs
import com.line.mymemo.R
import com.line.mymemo.adapter.ImageRecyclerAdapter
import com.line.mymemo.database.RoomDatabase
import com.line.mymemo.entity.ImageEntity
import com.line.mymemo.entity.MemoEntity
import com.line.mymemo.entity.MemoWithImageEntity
import gun0912.tedimagepicker.builder.TedImagePicker
import gun0912.tedimagepicker.builder.type.MediaType
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.layout_custom_dialog.*
import kotlinx.android.synthetic.main.layout_custom_dialog.view.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EditActivity : AppCompatActivity() {

    private var db : RoomDatabase? = null
    private lateinit var actionbar: ActionBar
    private lateinit var adapter: ImageRecyclerAdapter
    private var item: MemoWithImageEntity? = null

    companion object {
        private val TAG = EditActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)
        // 화면 초기화
        initView()
    }

    private fun initView() {
        // 데이터베이스
        db = RoomDatabase.getInstance(this)
        // 액션바 설정
        setActionBar()
        // 리사이클뷰 어댑터 설정
        setRVAdapter()
        // 리사이클뷰 레이아웃 매니저 설정
        setRVLayoutManager()
        // 이미지 추가 버튼 클릭 이벤트
        setFABListener()
        // 전달 받은 데이터 확인
        if (intent.hasExtra("item")) { // 메모 수정
            item = intent.getSerializableExtra("item") as MemoWithImageEntity
            editMemo()
        } else { // 새 메모 추가
            addMemo()
        }
    }

    // 액션바 설정
    private fun setActionBar() {
        // 액션바
        setSupportActionBar(toolbar)
        actionbar = supportActionBar!!
        // 액션바 뒤로가기 버튼 활성화 설정
        actionbar.setDisplayHomeAsUpEnabled(true)
    }

    // 리사이클뷰 어댑터 설정
    private fun setRVAdapter() {
        adapter = ImageRecyclerAdapter()
        recycler_view.adapter = adapter
    }

    // 리사이클뷰 레이아웃 매니저 설정
    private fun setRVLayoutManager() {
        recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler_view.setHasFixedSize(true)
        adapter.itemClick = object: ImageRecyclerAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {
                // 이미지 삭제 버튼
                showPopupRemoveImage(position)
            }
        }
    }

    // 메모 작성 버튼 클릭 이벤트
    private fun setFABListener() {
        floating_action_button.setOnClickListener {
            showSelectImageDialog()
        }
    }

    // 이미지 선택 확인창
    private fun showSelectImageDialog() {
        var checkId: Int = 0
        var urlStr: String? = null
        val mDialogView = LayoutInflater.from(this).inflate(R.layout.layout_custom_dialog, null)
        val mBuilder = MaterialAlertDialogBuilder(this)
            .setTitle("이미지 추가")
            .setView(mDialogView)
            .setNegativeButton("취소", null)
            .setPositiveButton("확인") { dialog, which ->
                when (checkId) {
                    0 -> {
                        TedImagePicker.with(this)
                        // 미디어 타입 : 이미지
                        .mediaType(MediaType.IMAGE)
                        .startMultiImage { uriList -> addMultiImage(uriList) }
                    }
                    1 -> {
                        NetworkTask(this, urlStr).execute()
                    }
                }
            }

        mDialogView.radio_group.setOnCheckedChangeListener { group, checkedId ->
            when (group.checkedRadioButtonId) {
                R.id.radio_picture -> {
                    checkId = 0
                    mDialogView.edit_text_url.visibility = View.GONE
                    mDialogView.image_view_thumnail.visibility = View.GONE
                }
                R.id.radio_url -> {
                    checkId = 1
                    mDialogView.edit_text_url.visibility = View.VISIBLE
                    mDialogView.image_view_thumnail.visibility = View.VISIBLE
                }
            }
        }

        mDialogView.edit_text_url.addTextChangedListener (object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                urlStr = s.toString()
                Glide.with(mDialogView.context)
                    .load(s.toString())
                    .thumbnail(0.1f)
                    .error(R.drawable.ic_no_photo)
                    .into(mDialogView.image_view_thumnail)
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        mBuilder.show()
    }

    // 다중 이미지 추가
    private fun addMultiImage(uriList: List<Uri>) {
        uriList.forEach {
            Log.i(TAG, "uri: $it")
            adapter.addData(it.path!!)
        }
        if (adapter.itemCount > 0) recycler_view.visibility = View.VISIBLE
    }

    // 이미지 추가
    private fun addImage(path: String?) {
        if (path?.trim().isNullOrEmpty()) {
            Toast.makeText(this, "URL 경로가 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
        } else {
            adapter.addData(path!!)
            if (adapter.itemCount > 0) recycler_view.visibility = View.VISIBLE
        }
    }

    // 네트워크 상태 확인
    private class NetworkTask(val context: EditActivity, val path: String?) : AsyncTask<Void, Void, Int>() {
        override fun doInBackground(vararg params: Void?): Int? {
            if (path?.trim().isNullOrEmpty()) {
                return HttpURLConnection.HTTP_BAD_REQUEST
            }
            val connection = URL(path).openConnection() as HttpURLConnection // URL 경로 데이터 가져오기
            return connection.responseCode
        }

        override fun onPostExecute(result: Int?) {
            result?.let {
                if (it != HttpURLConnection.HTTP_OK) {
                    context.showToastMessage("URL 경로가 잘못되었습니다.")
                } else {
                    Log.i(TAG, "str : $path")
                    path?.let { it1 -> context.addImage(it1) }
                }
            }
        }
    }

    // 이미지 제거 확인창
    private fun showPopupRemoveImage(position: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("삭제하시겠습니까?")
            .setPositiveButton("확인") { dialog, which -> if(adapter.itemCount > 0) {
                Log.i(TAG, "position: $position")
                adapter.removeData(position)
                if (adapter.itemCount <= 0) recycler_view.visibility = View.GONE
            } }
            .setNegativeButton("취소", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_complete -> {
                // 저장 확인 팝업창
                showPopupForSave()
                true
            }
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        // 취소 확인 팝업창
        showPopupForCancel()
    }

    // 새 메모 추가
    private fun addMemo() {
        // 액션바 제목 설정
        actionbar.title = "메모 작성"
    }

    // 메모 수정
    private fun editMemo() {
        // 액션바 제목 설정
        actionbar.title = "메모 수정"
        item?.let {
            // 제목
            edit_text_title.text = it.memo?.title?.toEditable()
            // 내용
            edit_text_contents.text = it.memo?.contents?.toEditable()
            // 이미지
            if (it.images != null) {
                showImageList(it.images)
            }
        }
    }

    // 이미지 표시
    private fun showImageList(imageList: List<ImageEntity>) {
        imageList.forEach {
            Log.i(TAG, "Image Path: ${it.path}")
            it.path?.let { it1 -> adapter.addData(it1) }
        }
        if (adapter.itemCount > 0) recycler_view.visibility = View.VISIBLE
    }

    // 저장 확인 팝업창
    private fun showPopupForSave() {
        MaterialAlertDialogBuilder(this)
            .setTitle("저장하시겠습니까?")
            .setPositiveButton("확인") { dialog, which -> saveOrUpdate() }
            .setNegativeButton("취소", null)
            .show()
    }

    // 취소 확인 팝업창
    private fun showPopupForCancel() {
        MaterialAlertDialogBuilder(this)
            .setTitle("취소하시겠습니까?")
            .setPositiveButton("확인") { dialog, which -> finish() }
            .setNegativeButton("취소", null)
            .show()
    }

    // saveOrUpdate
    private fun saveOrUpdate() {
        if (item != null) {
            updateDatabase()
        } else {
            saveDatabase()
        }
    }

    // 데이터 저장
    private fun saveDatabase() {

        val memo = MemoEntity(null, edit_text_title.text.toString(), edit_text_contents.text.toString(), toDateString(Date()))
        InsertMemoTask(this, memo).execute()
    }

    // 데이터 수정
    private fun updateDatabase() {
        item?.memo?.let {
            it.title = edit_text_title.text.toString()
            it.contents = edit_text_contents.text.toString()
            it.regdate = toDateString(Date())
            UpdateTask(this, it).execute()
        }
    }

    // 이전 페이지로 이동
    private fun returActivity(rowId: Long) {
        intent = Intent()
        intent.putExtra("rowId", rowId)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    // 데이터 베이스에 정보 추가
    private class InsertMemoTask(var context: EditActivity, var memo: MemoEntity) : AsyncTask<Void, Void, Long>() {
        override fun doInBackground(vararg params: Void?): Long {
            // 메모 추가 및 기본키 반환
            val rowId = context.db?.memoDao()?.insertMemo(memo)
            return rowId!!
        }

        override fun onPostExecute(result: Long?) {
            InsertImagesTask(context, result).execute()
        }
    }

    // 데이터 베이스에 이미지 정보 추가
    private class InsertImagesTask(val context: EditActivity, val rowId: Long?) : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean? {
            val images: ArrayList<ImageEntity> = ArrayList()
            context.adapter.getAllData().forEach {
                images.add(ImageEntity(null, rowId, it))
            }
            // 이미지 초기화
            context.db?.imageDao()?.deleteAllImages(rowId)
            // 이미지 추가
            context.db?.imageDao()?.insertAllImages(images)
            return true
        }

        override fun onPostExecute(result: Boolean?) {
            if (result!!) {
                rowId?.let { context.returActivity(it) }
            }
        }
    }

    // 데이터 베이스에 정보 수정
    private class UpdateTask(val context: EditActivity, val memo: MemoEntity) : AsyncTask<Void, Void, Boolean>() {
        override fun doInBackground(vararg params: Void?): Boolean? {
            // 메모 수정
            context.db?.memoDao()?.updateMemo(memo)
            return true
        }
        override fun onPostExecute(result: Boolean?) {
            if (result!!) {
                InsertImagesTask(context, memo.id).execute()
            }
        }
    }

    // 메시지 출력(Toast)
    private fun showToastMessage(msg: String) {
        val t = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
        t.setGravity(Gravity.BOTTOM, 0, 50)
        t.show()
    }

    // Editable 변환
    private fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    // 데이터 포맷
    @SuppressLint("SimpleDateFormat")
    private fun toDateString(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return formatter.format(date)
    }
}
