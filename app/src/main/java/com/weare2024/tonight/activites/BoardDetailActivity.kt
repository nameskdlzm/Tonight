package com.weare2024.tonight.activites

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar.OnMenuItemClickListener
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.weare2024.tonight.G
import com.weare2024.tonight.R
import com.weare2024.tonight.adapter.BoardDetailPagerAdapter
import com.weare2024.tonight.data.BoardDetailData
import com.weare2024.tonight.databinding.ActivityBoardDetailBinding
import com.weare2024.tonight.network.RetrofitHelper
import com.weare2024.tonight.network.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BoardDetailActivity : AppCompatActivity() {
    private val binding by lazy { ActivityBoardDetailBinding.inflate(layoutInflater) }

//    private val bsb: BottomSheetBehavior<View> by lazy { BottomSheetBehavior.from(bs) }
    private val rl_title: View by lazy { binding.rlTitle }
    private val itemList = mutableMapOf<String, String>()
    private val imgs = mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.tvComment.setOnClickListener { clickComment() }
//        binding.chat.setOnClickListener { clickChat() }
        binding.rlTitle.setOnClickListener { clickTitle() }
//        binding.rl.background = null
        binding.toolbar.setOnMenuItemClickListener(object : OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                if (item!!.itemId == R.id.more112) {
                    showBottomSheet()
                } else if (item!!.itemId == R.id.send) {
                    Toast.makeText(this@BoardDetailActivity, "채팅 액티비티 이동", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        })
        binding.viewPager.adapter = BoardDetailPagerAdapter(this@BoardDetailActivity, imgs)

        sendBoardNo()
    }

    private fun sendBoardNo() {
        val retrofit = RetrofitHelper.getRetrofitInstance("http://weare2024.dothome.co.kr")
        val retrofitService = retrofit.create(RetrofitService::class.java)
        val boardNo = intent.getIntExtra("boardNo", 0)

//        Toast.makeText(this, "$boardNo", Toast.LENGTH_SHORT).show()
//        retrofitService.boardNoSend(boardNo).enqueue(object : Callback<String> {
//            override fun onResponse(p0: Call<String>, p1: Response<String>) {
//                val s = p1.body()
//                AlertDialog.Builder(this@BoardDetailActivity).setMessage("$s").create().show()
//            }
//
//            override fun onFailure(p0: Call<String>, p1: Throwable) {
//                Log.d("qwer", "${p1.message}")
//            }
//
//        })

        retrofitService.boardNoSend2(boardNo).enqueue(object : Callback<BoardDetailData> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(p0: Call<BoardDetailData>, p1: Response<BoardDetailData>) {
                val data = p1.body()
                binding.nickname.text = data?.nickname
                binding.tvReview.text = data?.content
                for (i in 0 until data?.imgs!!.size) {
                    imgs.add("http://weare2024.dothome.co.kr/Tonight/board/${data.imgs[i]}")
                }
                binding.viewPager.adapter!!.notifyDataSetChanged()
//                AlertDialog.Builder(this@BoardDetailActivity).setMessage("${data?.imgs?.get(0)}").create().show()
            }

            override fun onFailure(p0: Call<BoardDetailData>, p1: Throwable) {
                Log.d("qwer", "${p1.message}")
            }

        })
    }

    private fun showBottomSheet() {
        val dailog = BottomSheetDialog(this@BoardDetailActivity)
        val view = layoutInflater.inflate(R.layout.more112, null)
        dailog.setContentView(view)
        val singo1 = view.findViewById<TextView>(R.id.singo_1)
        singo1.setOnClickListener {
            Toast.makeText(this@BoardDetailActivity, "신고가 접수 되었습니다", Toast.LENGTH_SHORT).show()
        }
        val singo2 = view.findViewById<TextView>(R.id.singo_2)
        singo2.setOnClickListener {
            Toast.makeText(this@BoardDetailActivity, "신고가 접수 되었습니다", Toast.LENGTH_SHORT).show()
        }
        val singo3 = view.findViewById<TextView>(R.id.singo_3)
        singo3.setOnClickListener {
            Toast.makeText(this@BoardDetailActivity, "신고가 접수 되었습니다", Toast.LENGTH_SHORT).show()
        }
        val singo4 = view.findViewById<TextView>(R.id.singo_4)
        singo4.setOnClickListener {
            Toast.makeText(this@BoardDetailActivity, "신고가 접수 되었습니다", Toast.LENGTH_SHORT).show()
        }
        val singo5 = view.findViewById<TextView>(R.id.singo_5)
        singo5.setOnClickListener {
            Toast.makeText(this@BoardDetailActivity, "신고가 접수 되었습니다", Toast.LENGTH_SHORT).show()
        }
        dailog.show()
    }

    private fun clickComment() {
        val boardNo = intent.getIntExtra("boardNo", 0)
        val intent2 = Intent(this, CommentActivity::class.java)
        intent2.putExtra("boardNo", boardNo)
        startActivity(intent2)

    }

    private fun clickChat() {
        Toast.makeText(this, "채팅 채널로 연결 됩니다.", Toast.LENGTH_SHORT).show()
    }

    private fun clickTitle() {
        val imageView = ImageView(this@BoardDetailActivity)
        imageView.setImageResource(R.drawable.baseline_image_post_sample)
        val builder = AlertDialog.Builder(this@BoardDetailActivity)
        builder.setView(imageView)
        val alertDialog = builder.create()
        alertDialog.show()
    }
}