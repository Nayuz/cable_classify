package io.github.nayuz.cableclassify

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class AlbumActivity : AppCompatActivity() {
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
        val backButton: Button = findViewById(R.id.backButton)

        // 앨범 디렉토리에서 ".jpg" 확장자를 가진 파일만 필터링하여 가져오기
        val imageFiles = albumDirectory.listFiles { _, name -> name.endsWith(".jpg") }

        // 가져온 이미지 파일 목록에서 파일 이름만 추출
        val imageFileNames = imageFiles?.map { it.name } ?: emptyList()

        // 이미지 파일 목록을 표시할 어댑터 설정 (단순히 파일 이름만 표시)
        albumAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, imageFileNames)

        // ListView에 어댑터 연결
        albumListView.adapter = albumAdapter

        // ListView에서 항목을 클릭했을 때 처리
        albumListView.setOnItemClickListener { _, _, position, _ ->
            // 클릭된 이미지 파일을 Uri 형태로 변환
            val selectedImageFile = imageFiles[position]
            val selectedImageUri = Uri.fromFile(selectedImageFile)

            // 결과 Intent에 선택한 이미지 파일의 URI를 담아서 반환
            val intent = Intent()
            intent.data = selectedImageUri
            setResult(RESULT_OK, intent)
            finish() // Activity 종료
        }

        // 뒤로 가기 버튼 클릭 시 현재 Activity 종료
        backButton.setOnClickListener {
            finish() // Activity 종료
        }
    }
}
