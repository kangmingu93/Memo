package com.line.mymemo.activity

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.line.mymemo.R
import com.line.mymemo.adapter.RecyclerAdapter
import com.line.mymemo.database.RoomDatabase
import com.line.mymemo.databinding.ActivityMainBinding
import com.line.mymemo.entity.MemoWithImageEntity

class MainActivity : AppCompatActivity() {
    private val EDIT_REQUEST_CODE = 100
    private val DETAIL_REQUEST_CODE = 200

    private var db : RoomDatabase? = null
    private lateinit var adapter : RecyclerAdapter
    private lateinit var binding : ActivityMainBinding

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 화면 초기화
        initView()
    }

    override fun onDestroy() {
        RoomDatabase.destoryInstance()
        db = null
        super.onDestroy()
    }

    // 화면 초기화
    private fun initView() {
        // 데이터 바인딩
        binding = DataBindingUtil.setContentView(this,
            R.layout.activity_main
        )
        // 데이터베이스
        db = RoomDatabase.getInstance(this)
        // 리사이클뷰 어댑터 설정
        setRVAdapter()
        // 리사이클뷰 레이아웃 매니저 설정
        setRVLayoutManager()
        // 데이터 설정
        setItemsData()
        // 메모 작성 버튼 클릭 이벤트
        setFABListener()
    }

    // 리사이클뷰 어댑터 설정
    private fun setRVAdapter() {
        adapter = RecyclerAdapter()
        binding.recyclerView.adapter = adapter
    }

    // 리사이클뷰 레이아웃 매니저 설정
    private fun setRVLayoutManager() {
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
    }

    // 데이터 설정
    private fun setItemsData() {
        LoadDataFormDb(this).execute()
        adapter.itemClick = object: RecyclerAdapter.ItemClick {
            override fun onClick(view: View, position: Int) {
                this@MainActivity.moveDetailActivity(position)
            }
        }
    }

    // 메모 작성 버튼 클릭 이벤트
    private fun setFABListener() {
        binding.floatingActionButton.setOnClickListener {
            val intent = Intent(this, EditActivity::class.java)
            startActivityForResult(intent, EDIT_REQUEST_CODE)
        }
    }

    // 상세 페이지 이동
    private fun moveDetailActivity(position: Int) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra("position", position)
        intent.putExtra("item", adapter.getItem(position))
        startActivityForResult(intent, DETAIL_REQUEST_CODE)
    }

    // 데이터 정보 로드
    private class LoadDataFormDb(val context: MainActivity) : AsyncTask<Void, Void, List<MemoWithImageEntity>>() {
        override fun doInBackground(vararg params: Void?): List<MemoWithImageEntity>? {
            return context.db?.memoDao()?.getAllMemos()
        }

        override fun onPostExecute(result: List<MemoWithImageEntity>?) {
            result?.let {
                context.adapter.clear()
                context.adapter.addList(it)
            }
        }
    }

    // 단일 데이터 정보 로드
    private class GetDataFormDb(val context: MainActivity, val rowId: Long) : AsyncTask<Void, Void, MemoWithImageEntity>() {
        override fun doInBackground(vararg params: Void?): MemoWithImageEntity? {
            return context.db?.memoDao()?.getMemo(rowId)
        }

        override fun onPostExecute(result: MemoWithImageEntity?) {
            result?.let {
                context.adapter.addItem(it)
                context.binding.recyclerView.smoothScrollToPosition(0)
            }
        }
    }

    // 단일 데이터 정보 삭제
    private class RemoveDataFormDb(val context: MainActivity, val item: MemoWithImageEntity, val position: Int) : AsyncTask<Void, Void, Int>() {
        override fun doInBackground(vararg params: Void?): Int? {
            return item.memo?.let { context.db?.memoDao()?.deleteMemo(it) }
        }

        override fun onPostExecute(result: Int?) {
            result?.let {
                Log.i(TAG, "result: $it")
                // 리스트뷰 데이터 제거 및 갱신
                context.adapter.removeItem(position)
                // 메시지 출력
                context.showToastMessage("데이터 삭제...")
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
                            val rowId = data.getLongExtra("rowId", 0)
                            Log.i(TAG, "rowId : $rowId")
                            // 단일 데이터 로드
                            GetDataFormDb(this, rowId).execute()
                            // 메시지 출력
                            showToastMessage("데이터 추가...")
                        }
                    }
                }
                DETAIL_REQUEST_CODE -> { // Result DetailActivity
                    data?.let {
                        var position: Int = -1
                        var item: MemoWithImageEntity? = null
                        if (data.hasExtra("position")) {
                            position = data.getIntExtra("position", -1)
                            Log.i(TAG, "position : $position")
                        }
                        if (data.hasExtra("item")) {
                            item = data.getSerializableExtra("item") as MemoWithImageEntity
                            Log.i(TAG, item.toString())
                        }
                        if (data.hasExtra("bool")) {
                            // 데이터 삭제 및 리스트뷰 갱신
                            item?.let { RemoveDataFormDb(this, item, position).execute() }
                        } else {
                            LoadDataFormDb(this).execute()
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
