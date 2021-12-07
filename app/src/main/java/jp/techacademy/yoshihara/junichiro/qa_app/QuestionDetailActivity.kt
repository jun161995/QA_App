package jp.techacademy.yoshihara.junichiro.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*

class QuestionDetailActivity : AppCompatActivity() {
    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private var isFavorite: Boolean = false
    private var loginState: Boolean = false
    private val mEventListener = object : ChildEventListener{
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val answerUid =  dataSnapshot.key ?: ""

            for(answer in mQuestion.answers){
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = map["body"] as? String ?:""
            val name = map["name"] as? String ?: ""

            val uid = map["uid"] as? String ?: ""
            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }
        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onCancelled(databaseError: DatabaseError) {

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()
        loginRefresh()
        fab.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question",mQuestion)
                startActivity(intent)
            }
        }


        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString())
            .child(mQuestion.questionUid).child(
                AnswersPATH
            )
        mAnswerRef.addChildEventListener(mEventListener)

        btnFav.setOnClickListener {
            val databaseReference = FirebaseDatabase.getInstance().reference
            val favoriteRef = databaseReference.child(FavoritesPATH)
                .child(FirebaseAuth.getInstance().currentUser!!.uid).child(mQuestion.questionUid)

            favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.value as Map<*, *>?
                    if (data == null) {
                        val data = HashMap<String, String>()
                        data["genre"] = mQuestion.genre.toString()

                        favoriteRef.setValue(data)
                    } else {
                        favoriteRef.removeValue()
                    }
                    favStateSearch()
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }

    private fun btnAppearanceRefresh() {
            if (isFavorite) {
                btnFav.text = "お気に入り追加"
            } else {
                btnFav.text = "お気に入り削除"
            }
    }

    private fun favStateSearch() {
        val databaseReference = FirebaseDatabase.getInstance().reference
        val favoriteRef = databaseReference.child(FavoritesPATH)
            .child(FirebaseAuth.getInstance().currentUser!!.uid).child(mQuestion.questionUid)
        favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val data = snapshot.value as Map<*, *>?
                isFavorite = data != null //
                btnAppearanceRefresh()
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    private fun loginRefresh() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            loginState = false
            btnFav.visibility = View.GONE
        } else {
            loginState = true
            btnFav.visibility = View.VISIBLE
            favStateSearch()
        }
    }

    // 別画面から戻ってきた時にloginRefresh()を実行する
    override fun onResume() {
        super.onResume()
        loginRefresh()
    }
}