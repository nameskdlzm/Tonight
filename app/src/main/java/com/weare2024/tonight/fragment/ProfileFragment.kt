package com.weare2024.tonight.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.google.firebase.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import com.weare2024.tonight.G
import com.weare2024.tonight.G.Companion.uid
import com.weare2024.tonight.R
import com.weare2024.tonight.activites.BoardDetailActivity
import com.weare2024.tonight.activites.ChangeProfileActivity
import com.weare2024.tonight.activites.LoginActivity
import com.weare2024.tonight.adapter.BoardDetailPagerAdapter
import com.weare2024.tonight.adapter.ProfilePagerAdapter
import com.weare2024.tonight.data.Images
import com.weare2024.tonight.data.ProfileImages
import com.weare2024.tonight.data.UserData
import com.weare2024.tonight.databinding.FragmentProfileBinding
import com.weare2024.tonight.firebase.FBAuth
import com.weare2024.tonight.firebase.FBRef
import com.weare2024.tonight.network.RetrofitHelper
import com.weare2024.tonight.network.RetrofitService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    lateinit var drawerLayout: DrawerLayout
    private val spf by lazy { activity?.getSharedPreferences("loginSave", AppCompatActivity.MODE_PRIVATE) }
    private val spf2 by lazy { activity?.getSharedPreferences("userInfo", AppCompatActivity.MODE_PRIVATE) }
    private val spfEdt by lazy { spf?.edit() }
    private val spf2Edt by lazy { spf2?.edit() }
    private val imgs = mutableListOf<String>()
    private val adapter by lazy { ProfilePagerAdapter(requireContext(), imgs) }
    private val boardNo = mutableListOf<Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)

        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.vp.offscreenPageLimit = 3
        binding.vp.getChildAt(0).overScrollMode = View.OVER_SCROLL_NEVER
        binding.vp.adapter = ProfilePagerAdapter(requireContext(), imgs)

        val transform = CompositePageTransformer()
        transform.addTransformer(MarginPageTransformer(8))

        transform.addTransformer(ViewPager2.PageTransformer { view: View, fl: Float ->
            val v = 1-Math.abs(fl)
            view.scaleY = 0.8f + v * 0.2f
        })

        binding.vp.setPageTransformer(transform)

        binding.toolbar.setOnClickListener { clickToolbar() }
        binding.nav.setNavigationItemSelectedListener(object :OnNavigationItemSelectedListener{
            override fun onNavigationItemSelected(p0: MenuItem): Boolean {
                if (p0.itemId == R.id.menu_aa){
                    Toast.makeText(requireContext(), "로그아웃", Toast.LENGTH_SHORT).show()
                    AlertDialog.Builder(requireContext()).setTitle("로그아웃").setMessage("로그아웃 하시겠습니까?").setPositiveButton("확인"){dialog , id  ->
                        FBAuth.auth.signOut()
                        spfEdt?.putBoolean("isLogin", false)
                        spf2Edt?.clear()
                        spfEdt?.apply()
                        spf2Edt?.apply()
                        G.uid =""
                        G.nickname =""
                        startActivity(Intent(requireContext(),LoginActivity::class.java))
                        activity?.finish()

                    }.setNegativeButton("취소"){ dialog ,id ->
                        dialog.dismiss()

                    }.create().show()


                }  else if (p0.itemId == R.id.menu_change) {
                    startActivity(Intent(context, ChangeProfileActivity::class.java))
                    return true
                }
                return false
            }

        })

        //닉네임, 프로필이미지 불러오기
        FBRef.userRef.whereEqualTo("uid", uid).get().addOnSuccessListener {
            binding.tv.text = ""
            for (snap in it) {
                val userData: UserData? = snap.toObject(UserData::class.java)
                userData?.apply {

                    binding.tv.text = nickname

                    val uri = uid
//                    Toast.makeText(context, "$uri", Toast.LENGTH_SHORT).show()
                    val imgRef: StorageReference = Firebase.storage.getReference("usersImg/" + uri)
                    imgRef.downloadUrl.addOnSuccessListener(object : OnSuccessListener<Uri> {
                        override fun onSuccess(p0: Uri?) {
                            Glide.with(this@ProfileFragment).load(p0).into(binding.iv)
                        }

                    })
                }
            }
        }

//        adapter.setOnItemClickListner(object : ProfilePagerAdapter.OnItemClickListner {
//            override fun onItemClick(view: View?, position: Int) {
//                val intent = Intent(requireContext(), BoardDetailActivity::class.java)
//                intent.putExtra("boardNo", this@ProfileFragment.boardNo[position])
//                startActivity(intent)
//            }
//
//        })

    }

    override fun onResume() {
        super.onResume()
        selectDB()
    }

    private fun clickToolbar() {
        val tvDrawer = view?.findViewById<TextView>(R.id.tv_drawer)
        val ivDrawer = view?.findViewById<ImageView>(R.id.iv_drawer)

//        drawerLayout = binding.drawerLayout

        binding.drawerLayout.openDrawer(GravityCompat.END)

        FBRef.userRef.whereEqualTo("uid", uid).get().addOnSuccessListener {
            tvDrawer?.text = ""
            for (snap in it) {
                val userData: UserData = snap.toObject(UserData::class.java)
                userData.apply {
                    tvDrawer?.text = nickname

                    val uri = uid
                    val imgRef: StorageReference = Firebase.storage.getReference("usersImg/$uri")
                    imgRef.downloadUrl.addOnSuccessListener(object : OnSuccessListener<Uri> {
                        override fun onSuccess(p0: Uri?) {
                            Glide.with(this@ProfileFragment).load(p0).into(ivDrawer!!)
                        }
                    })
                }
            }
        }

    }

    private fun selectDB() {
        val retrofit = RetrofitHelper.getRetrofitInstance("http://weare2024.dothome.co.kr")
        val retrofitService = retrofit.create(RetrofitService::class.java)

        retrofitService.selectProfileImgs(uid).enqueue(object : Callback<List<ProfileImages>> {
            override fun onResponse(p0: Call<List<ProfileImages>>, p1: Response<List<ProfileImages>>) {
                val data = p1.body()

                imgs.clear()

                for (i in 0 until data!!.size) {
                    imgs.add("http://weare2024.dothome.co.kr/Tonight/board/${data[i].imgsUri}")
                    boardNo.add(data[i].boardNo)
                }

                binding.vp.adapter!!.notifyDataSetChanged()
                AlertDialog.Builder(requireContext()).setMessage("$data").create().show()

            }

            override fun onFailure(p0: Call<List<ProfileImages>>, p1: Throwable) {
                Log.d("error", "${p1.message}")
            }

        })
    }
}