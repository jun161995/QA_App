package jp.techacademy.yoshihara.junichiro.qa_app

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
//import com.google.firebase.database.R
import java.util.*
import android.view.Gravity

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private var mGenre = 0

    private lateinit var mFavoriteArrayList: ArrayList<FavoriteQuestion>
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mQuestionArrayList: ArrayList<Question>
    private lateinit var mAdapter: QuestionsListAdapter

    private var mGenreRef: DatabaseReference? = null
    private var mFavoriteRef: DatabaseReference? = null
    private var mFavoriteQuestionRef: DatabaseReference? = null
    private var isFavorite: Boolean = false
    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(Snapshot: DataSnapshot, s: String?) {
            val map = Snapshot.value as Map<String, String>
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = map["image"] ?: ""
            val bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }

            val answerArrayList = ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String, String>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }

            val question = Question(title, body, name, uid, Snapshot.key ?: "",
                mGenre, bytes, answerArrayList)
            mQuestionArrayList.add(question)
            mAdapter.notifyDataSetChanged()
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>

            // 変更があったQuestionを探す
            for (question in mQuestionArrayList) {
                if (dataSnapshot.key!!.equals(question.questionUid)) {
                    // このアプリで変更がある可能性があるのは回答（Answer)のみ
                    question.answers.clear()
                    val answerMap = map["answers"] as Map<String, String>?
                    if (answerMap != null) {
                        for (key in answerMap.keys) {
                            val temp = answerMap[key] as Map<String, String>
                            val answerBody = temp["body"] ?: ""
                            val answerName = temp["name"] ?: ""
                            val answerUid = temp["uid"] ?: ""
                            val answer = Answer(answerBody, answerName, answerUid, key)
                            question.answers.add(answer)
                        }
                    }

                }
            }

            mAdapter.notifyDataSetChanged()
        }

        override fun onChildRemoved(p0: DataSnapshot) {

        }

        override fun onChildMoved(p0: DataSnapshot, p1: String?) {

        }

        override fun onCancelled(p0: DatabaseError) {

        }

    }
    private val favoriteListener = object: ChildEventListener{
        override fun onCancelled(error: DatabaseError) {

        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("Favorite", snapshot.value.toString())
        }

        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
            Log.d("tag", snapshot.value.toString())
            val map = snapshot.value as Map<String,String>;
            val uid = map["uid"] ?: ""
            val questionUid = snapshot.key as String
            val genreId = map["genre"].toString()
            val favoriteQuestion = FavoriteQuestion(genreId,uid, questionUid)
            mFavoriteArrayList.add(favoriteQuestion)
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
        }

    }
    // お気に入り質問を取得するときのリスナー
    private val favoriteQuestionListener = object: ValueEventListener {
        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val map = dataSnapshot.value as Map<String,String>
            //エルビス演算子
            val title = map["title"] ?: ""
            val body = map["body"] ?: ""
            val name = map["name"] ?: ""
            val uid = map["uid"] ?: ""
            val imageString = map["image"] ?: ""
            val bytes =
                if (imageString.isNotEmpty()) {
                    Base64.decode(imageString, Base64.DEFAULT)
                } else {
                    byteArrayOf()
                }
            val answerArrayList = java.util.ArrayList<Answer>()
            val answerMap = map["answers"] as Map<String,String>?
            if (answerMap != null) {
                for (key in answerMap.keys) {
                    val temp = answerMap[key] as Map<String, String>
                    val answerBody = temp["body"] ?: ""
                    val answerName = temp["name"] ?: ""
                    val answerUid = temp["uid"] ?: ""
                    val answer = Answer(answerBody, answerName, answerUid, key)
                    answerArrayList.add(answer)
                }
            }
            val favoriteQuestion = Question(title, body, name, uid, dataSnapshot.key ?: "",
                mGenre, bytes, answerArrayList)
            mQuestionArrayList.add(favoriteQuestion)
            mAdapter.notifyDataSetChanged()
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))

        val user = FirebaseAuth.getInstance().currentUser

        fab.setOnClickListener { view ->
            // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
            if (mGenre == 0) {
                Snackbar.make(
                    view,
                    getString(R.string.question_no_select_genre),
                    Snackbar.LENGTH_LONG
                ).show()
            } else {

            }

            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(applicationContext,QuestionSendActivity::class.java)
                intent.putExtra("genre",mGenre)
                startActivity(intent)
            }

        }
        // ナビゲーションドロワーの設定
        val toggle = ActionBarDrawerToggle(this, drawer_layout, toolbar, R.string.app_name, R.string.app_name)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        if(user == null){
            nav_view.menu.getItem(4).isVisible = false
        }
        nav_view.setNavigationItemSelectedListener(this)

        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        // ListViewの準備
        mAdapter = QuestionsListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mFavoriteArrayList = ArrayList<FavoriteQuestion>()
//        val user = FirebaseAuth.getInstance().currentUser
        if(user != null){
            mFavoriteRef = mDatabaseReference.child("favorites").child(user!!.uid)
            mFavoriteRef!!.addChildEventListener(favoriteListener)
            mAdapter.notifyDataSetChanged()
        }

        listView.setOnItemClickListener{parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面を起動する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            val user = FirebaseAuth.getInstance().currentUser
            if(user != null){
                isFavorite = false
                for (data in mFavoriteArrayList) {
                    if(data.questionUid == mQuestionArrayList[position].questionUid){
                        isFavorite = true
                    }
                }
            }
            intent.putExtra("isFavorite", isFavorite)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
//        val user = FirebaseAuth.getInstance().currentUser
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
//        if (user == null) {
//            navigationView.menu.findItem(R.id.action_favorite).isVisible = false
//        } else {
//            navigationView.menu.findItem(R.id.action_favorite).isVisible = true
//        }
        // 1:趣味を既定の選択とする
        if (mGenre == 0) {
            onNavigationItemSelected(navigationView.menu.getItem(0))
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.action_settings) {
            val intent = Intent(applicationContext, SettingActivity::class.java)
            startActivity(intent)

            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if (id == R.id.nav_hobby) {
            toolbar.title = getString(R.string.menu_hobby_label)
            mGenre = 1
        } else if (id == R.id.nav_life) {
            toolbar.title = getString(R.string.menu_life_label)
            mGenre = 2
        } else if (id == R.id.nav_health) {
            toolbar.title = getString(R.string.menu_health_label)
            mGenre = 3
        } else if (id == R.id.nav_compter) {
            toolbar.title = getString(R.string.menu_compter_label)
            mGenre = 4
        }else if(id == R.id.action_favorite){
            toolbar.title = "お気に入り"
            mGenre = 5
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        // お気に入り以外の時
        if(mGenre < 5) {
            mQuestionArrayList.clear()
            mAdapter.setQuestionArrayList(mQuestionArrayList)
            listView.adapter = mAdapter
            if (mGenreRef != null) {
                mGenreRef!!.removeEventListener(mEventListener)
            }
            mGenreRef = mDatabaseReference.child(ContentsPATH).child(mGenre.toString())
            mGenreRef!!.addChildEventListener(mEventListener)
        }else{
            mQuestionArrayList.clear()
            mAdapter.setQuestionArrayList(mQuestionArrayList)
            listView.adapter = mAdapter
            for(data in mFavoriteArrayList){
                mFavoriteQuestionRef =  mDatabaseReference.child(ContentsPATH).child(data.genreId).child(data.questionUid)
                mFavoriteQuestionRef!!.addValueEventListener(favoriteQuestionListener)
            }
        }
        return true
    }
}