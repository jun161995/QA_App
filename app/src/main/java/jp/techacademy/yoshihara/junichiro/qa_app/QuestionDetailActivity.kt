package jp.techacademy.yoshihara.junichiro.qa_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_question_detail.*

class QuestionDetailActivity : AppCompatActivity() {
    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef: DatabaseReference
    private var isFavorite: Boolean = false
    // 回答表示画面から戻ってきた時に回答が追加されているように、onChildAddedの時にDBからgetしておく
    private val mEventListener = object : ChildEventListener{
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<*, *>

            val answerUid =  dataSnapshot.key ?: ""

            for(answer in mQuestion.answers){
                // 同じAnswerUidのものが存在しているときは何もしない
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
    private val fEvantListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
            // このonChildAddedにくるということはデータが存在するということなので、お気に入りだと確定する。よってisFavoriteをtrueして画像変えるだけで良い。
            isFavorite = true
            star.setImageResource(R.drawable.ic_star)
            //Log.d("check", "onChildAdded")

        }

        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            TODO("Not yet implemented")
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {
            //Log.d("check", "onChiledRemoved done")
        }

        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            TODO("Not yet implemented")
        }

        override fun onCancelled(error: DatabaseError) {
            TODO("Not yet implemented")
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        isFavorite = false
        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras!!.get("question") as Question

        title = mQuestion.title

        // ListViewの準備
        mAdapter = QuestionDetailListAdapter(this, mQuestion)
        listView.adapter = mAdapter
        mAdapter.notifyDataSetChanged()

        fab.setOnClickListener {
            // ログイン済みのユーザーを取得する
            val user = FirebaseAuth.getInstance().currentUser

            if (user == null) {
                // ログインしていなければログイン画面に遷移させる
                val intent = Intent(applicationContext, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Questionを渡して回答作成画面を起動する
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question",mQuestion)
                startActivity(intent)
            }
        }
        val dataBaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = dataBaseReference.child(ContentsPATH).child(mQuestion.genre.toString()).child(mQuestion.questionUid).child(AnswersPATH)
        mAnswerRef.addChildEventListener(mEventListener)

        val user = FirebaseAuth.getInstance().currentUser
        val uid = FirebaseAuth.getInstance().uid

        mFavoriteRef = dataBaseReference.child(FavoritesPATH).child(uid.toString()).child(mQuestion.questionUid)
        mFavoriteRef.addChildEventListener(fEvantListener)


        //Log.d("check" , user.toString())
        //Log.d("check", uid.toString())
        if (user == null) {
            star.hide()
        }
        else{
            // お気に入りボタンの機能実装
            star.setOnClickListener {
                if(isFavorite){
                    onDeleteFavorite(uid.toString(), mQuestion.questionUid)
                    star.setImageResource(R.drawable.ic_star_border)
                    isFavorite = false
                }
                else{
                    onAddFavorite(uid.toString(), mQuestion.questionUid, mQuestion.genre.toString())
                    star.setImageResource(R.drawable.ic_star)
                    isFavorite = true
                }
            }
        }

    }


override fun onResume() {
    super.onResume()
    Log.d("check", "onResume")
}

private fun onAddFavorite(uid: String, qUid: String, genre: String){
    Log.d("check", "onAdd done")
    val dataBaseReference = FirebaseDatabase.getInstance().reference
    val FavRef = dataBaseReference.child(FavoritesPATH).child(uid).child(qUid)
    val map =  HashMap<String,String>()
    map["genre"] = genre
    FavRef.setValue(map)
}

private fun onDeleteFavorite(uid: String, qUid: String){
    Log.d("check", "onDelete done")
    val dataBaseReference = FirebaseDatabase.getInstance().reference
    val FavRef = dataBaseReference.child(FavoritesPATH).child(uid).child(qUid)
    FavRef.removeValue()
}

}