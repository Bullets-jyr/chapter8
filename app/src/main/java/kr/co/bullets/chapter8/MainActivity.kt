package kr.co.bullets.chapter8

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import kr.co.bullets.chapter8.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val imageLoadLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uriList ->
        updateImages(uriList)
    }
    private lateinit var imageAdapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loadImageButton.setOnClickListener {
            checkPermission()
        }

        binding.navigateFrameActivityButton.setOnClickListener {
            navigateToFrameActivity()
        }

        initRecyclerView()
    }

    private fun navigateToFrameActivity() {
        val images = imageAdapter.currentList.filterIsInstance<ImageItems.Image>().map {
            it.uri.toString()
        }.toTypedArray()
        val intent = Intent(this, FrameActivity::class.java).putExtra("images", images)
        startActivity(intent)
    }

    private fun initRecyclerView() {
        imageAdapter = ImageAdapter(object : ImageAdapter.ItemClickListener {
            override fun onLoadMoreClick() {
                checkPermission()
            }
        })

        binding.imageRecyclerView.apply {
            adapter = imageAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> {
                Log.e("checkPermission", "===== 1 =====")
                loadImage()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                Log.e("checkPermission", "===== 2 =====")
                showPermissionInfoDialog()
            }
            else -> {
                Log.e("checkPermission", "===== 3 =====")
                requestReadExternalStorage()
            }
        }
    }

    private fun showPermissionInfoDialog() {
        AlertDialog.Builder(this).apply {
            setMessage("이미지를 가져오기 위해서, 외부 저장소 읽기 권한이 필요합니다.")
            setNegativeButton("취소", null)
            setPositiveButton("동의") { _, _ ->
                requestReadExternalStorage()
            }
        }.show()
    }

    private fun loadImage() {
//        Toast.makeText(this, "이미지를 가져올 예정", Toast.LENGTH_SHORT).show()
        imageLoadLauncher.launch("image/*")
    }

    private fun requestReadExternalStorage() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_READ_EXTERNAL_STORAGE)
    }

    private fun updateImages(uriList: List<Uri>) {
//        Log.i("updateImages", "$uriList")
        val images = uriList.map {
            ImageItems.Image(it)
        }
        val updatedImages = imageAdapter.currentList.toMutableList().apply {
            addAll(images)
        }
        imageAdapter.submitList(updatedImages)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_READ_EXTERNAL_STORAGE -> {
                val resultCode = grantResults.firstOrNull() ?: PackageManager.PERMISSION_DENIED
                if (resultCode == PackageManager.PERMISSION_GRANTED) {
                    loadImage()
                }
            }
        }
    }

    companion object {
        const val REQUEST_READ_EXTERNAL_STORAGE = 100
    }
}