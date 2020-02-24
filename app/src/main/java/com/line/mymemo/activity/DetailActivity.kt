package com.line.mymemo.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.line.mymemo.R
import com.line.mymemo.adapter.ViewPagerAdapter
import com.line.mymemo.database.RoomDatabase
import com.line.mymemo.entity.MemoWithImageEntity
import kotlinx.android.synthetic.main.activity_detail.*

class DetailActivity : AppCompatActivity() {
    private val EDIT_REQUEST_CODE = 100

    private var db : RoomDatabase? = null
    private lateinit var actionbar : ActionBar
    private lateinit var item: MemoWithImageEntity
    private var position: Int = -1

    companion object {
        private val TAG = DetailActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        // 이전 화면에서 데이터 가져오기
        if (intent.hasExtra("position")) {
            position = intent.getIntExtra("position", -1)
        }
        if (intent.hasExtra("item")){
            item = intent.getSerializableExtra("item") as MemoWithImageEntity
            // 화면 초기화
            initView()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                showPopupForEdit()
                true
            }
            R.id.action_delete -> {
                showPopupForDelete()
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
        returnActivity(false)
    }

    // 화면 구성
    private fun initView() {
        // 데이터베이스
        db = RoomDatabase.getInstance(this)
        // 액션바
        setSupportActionBar(toolbar)
        actionbar = supportActionBar!!
        // 액션바 타이틀 설정
        actionbar.title = "상세페이지"
        // 액션바 타이틀 활성화 설정
        actionbar.setDisplayShowTitleEnabled(true)
        // 액션바 뒤로가기 버튼 활성화 설정
        actionbar.setDisplayHomeAsUpEnabled(true)
        // 축소 레이아웃 상태별 제목 색상 설정
        collapsing_toolbar_Layout.setExpandedTitleColor(Color.TRANSPARENT)
        collapsing_toolbar_Layout.setCollapsedTitleTextColor(Color.WHITE)
        // 데이터 설정
        setEditData()
    }

    // 데이터 설정
    private fun setEditData() {
        val temp = item
        temp.let {
            // 이미지
            temp.images?.let {
                if (temp.images.isNotEmpty()) {
                    Log.i(TAG, "Images : ${temp.images}")
                    val viewPagerAdapter = ViewPagerAdapter(false)
                    viewPagerAdapter.addImageList(temp.images)
                    view_pager.adapter = viewPagerAdapter
                    viewPagerAdapter.itemClick = object: ViewPagerAdapter.ItemClick {
                        override fun onClick(view: View, position: Int) {
                            Log.i(TAG, "Image Click!!!")
                        }
                    }
                }
            }
            // 제목
            temp.memo?.title?.let {
                Log.i(TAG, "Title :  ${temp.memo.title}")
                text_view_title.text = temp.memo.title
            }
            // 내용
            temp.memo?.contents?.let {
                Log.i(TAG, "Contents : ${temp.memo.contents}")
                text_view_contents.text = temp.memo.contents
            }
            // 작성일
            temp.memo?.regdate?.let {
                Log.i(TAG, "Date : ${temp.memo.regdate}")
                text_view_regdate.text = temp.memo.regdate
            }
        }
    }

    // 작성 확인 팝업창
    private fun showPopupForEdit() {
        MaterialAlertDialogBuilder(this)
            .setTitle("수정페이지로 이동하시겠습니까?")
            .setPositiveButton("확인") { dialog, which -> moveEditActivity(item) }
            .setNegativeButton("취소", null)
            .show()
    }

    // 삭제 확인 팝업창
    private fun showPopupForDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle("삭제하시겠습니까?")
            .setPositiveButton("확인") { dialog, which -> returnActivity(true) }
            .setNegativeButton("취소", null)
            .show()
    }

    // 수정 페이지 이동
    private fun moveEditActivity(item: MemoWithImageEntity) {
        intent = Intent(this, EditActivity::class.java)
        intent.putExtra("item", item)
        startActivityForResult(intent, EDIT_REQUEST_CODE)
    }

    // 메인 페이지 이동
    private fun returnActivity(bool: Boolean) {
        intent = Intent()
        intent.putExtra("position", position)
        intent.putExtra("item", item)
        if (bool) intent.putExtra("bool", bool)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    // 단일 데이터 정보 로드
    private class GetDataFormDb(val context: DetailActivity, val rowId: Long) : AsyncTask<Void, Void, MemoWithImageEntity>() {
        override fun doInBackground(vararg params: Void?): MemoWithImageEntity? {
            return context.db?.memoDao()?.getMemo(rowId)
        }

        override fun onPostExecute(result: MemoWithImageEntity?) {
            result?.let {
                context.item = result
                context.setEditData()
                context.showToastMessage("데이터 수정...")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                EDIT_REQUEST_CODE -> { // Result EditActivity
                    data?.let {
                        if (data.hasExtra("rowId")) {
                            GetDataFormDb(this, data.getLongExtra("rowId", 0)).execute()
                        }
                    }
                }
            }
        }
    }

    // 메시지 출력(Toast)
    private fun showToastMessage(msg: String) {
        val t = Toast.makeText(this, msg, Toast.LENGTH_SHORT)
        t.setGravity(Gravity.BOTTOM, 0, 50)
        t.show()
    }
}
