package io.github.nayuz.cableclassify

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class AlbumActivity : AppCompatActivity() {

    companion object {
        private const val GALLERY_REQUEST_CODE = 1234
    }

    // ActivityResultLauncher 선언
    private lateinit var galleryLauncher: ActivityResultLauncher<Intent>

    // ListView를 사용하여 앨범 파일을 보여주기 위한 변수 선언
    private lateinit var albumListView: ListView

    // ListView에 보여줄 이미지 파일 목록을 관리하는 어댑터
    private lateinit var albumAdapter: ArrayAdapter<String>

    // 앨범이 저장된 디렉토리의 경로를 설정 (앱의 외부 저장소에 "MyAlbum" 폴더)
    private val albumDirectory: File by lazy {
        File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MyAlbum")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album) // activity_album 레이아웃 설정

        // ListView와 BackButton을 레이아웃에서 찾기
        albumListView = findViewById(R.id.albumListView)
        val openGalleryButton: Button = findViewById(R.id.openGalleryButton)
        val backButton: Button = findViewById(R.id.backButton)


        // 갤러리 열기 런처 초기화
        galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val selectedImageUri: Uri? = result.data?.data // 선택된 이미지의 URI 가져오기
                selectedImageUri?.let {
                    // 선택된 이미지 URI를 결과로 반환
                    val intent = Intent().apply {
                        data = it
                    }
                    setResult(RESULT_OK, intent)
                    finish() // Activity 종료
                }
            }
        }

        // 갤러리 열기 버튼 클릭 이벤트
        openGalleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*" // 이미지만 선택 가능
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            galleryLauncher.launch(intent) // 런처를 통해 갤러리 열기
        }

        // 뒤로 가기 버튼 클릭 시 현재 Activity 종료
        backButton.setOnClickListener {
            finish() // Activity 종료
        }
    }
}