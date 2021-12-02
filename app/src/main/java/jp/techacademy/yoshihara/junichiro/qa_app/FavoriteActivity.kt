package jp.techacademy.yoshihara.junichiro.qa_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_favorite.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.content_main.*

class FavoriteActivity : AppCompatActivity() {

    private var mFavorite: MutableList<String> = mutableListOf()
    private var keySplit: List<String> = ArrayList<String>()
    private var result: MutableList<FireStoreQuestion> = mutableListOf()

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter

    private var mGenreRef: DatabaseReference? = null
    private var snapshotListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)
        Log.d("TEST", "CREATE")
        title = "お気に入り"
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        listViewFavorite.setOnItemClickListener{parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        mFavorite.clear()
        load()

    }

    private fun load() {
        val data = getSharedPreferences("favoriteFlags", Context.MODE_PRIVATE)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            for (entry in data.all) {
                val key: String = entry.key
                keySplit = key.split("-")
                val value: Any? = entry.value
                Log.d("test_key", key)
                if (entry.value == true) {
                    mFavorite.add(keySplit[0].toString())
                }
            }
        }

        val listView = findViewById<ListView>(R.id.listViewFavorite)
//        val adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mFavorite)

//        listView.adapter = adapter
        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        listView.adapter = mAdapter

        snapshotListener?.remove()


        // 選択したジャンルにリスナーを登録する
        snapshotListener = FirebaseFirestore.getInstance()
            .collection(ContentsPATH)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    // 取得エラー
                    return@addSnapshotListener
                }
                var questions = listOf<Question>()
                val results = querySnapshot?.toObjects(FireStoreQuestion::class.java)
                results?.also {
                    result.clear()
                    for (value in it) {
                        for (value2 in mFavorite) {
                            if (value.title == value2) {
                                result.add(value)
                                Log.d("test", "notremove!!")
                            }
                        }
                    }
                    Log.d("test", "removeend!!")
                    questions = result.map { firestoreQuestion ->
                        val bytes =
                            if (firestoreQuestion.image.isNotEmpty()) {
                                Base64.decode(firestoreQuestion.image, Base64.DEFAULT)
                            } else {
                                byteArrayOf()
                            }
                        Question(firestoreQuestion.title, firestoreQuestion.body, firestoreQuestion.name, firestoreQuestion.uid,
                            firestoreQuestion.id, firestoreQuestion.genre, bytes, firestoreQuestion.answers)
                    }
                }

                mQuestionArrayList.clear()
                mQuestionArrayList.addAll(questions)
                mAdapter.notifyDataSetChanged()
            }
    }
}
