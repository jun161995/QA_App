package jp.techacademy.yoshihara.junichiro.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import io.grpc.InternalChannelz.id
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.list_question_detail.view.*
import io.grpc.InternalChannelz.id

class QuestionDetailListAdapter(context: Context, private val mQuestion: Question) : BaseAdapter() {
    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }

    private var mLayoutInflater: LayoutInflater? = null
    private var isFavorite = false
    private var titleFFlag = ""

    private lateinit var mQuestionReference: DatabaseReference

    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return 1 + mQuestion.answers.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            TYPE_QUESTION
        } else {
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }

    override fun getItem(position: Int): Any {
        return mQuestion
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view



        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                if(FirebaseAuth.getInstance().currentUser != null) {
                    convertView = mLayoutInflater!!.inflate(R.layout.list_question_detail_logined, parent, false)!!
                }else {
                    convertView =
                        mLayoutInflater!!.inflate(R.layout.list_question_detail, parent, false)!!
                }
            }
            val body = mQuestion.body
            val name = mQuestion.name

            val bodyTextView = convertView.bodyTextView as TextView
            bodyTextView.text = body

            val nameTextView = convertView.nameTextView as TextView
            nameTextView.text = name

            val bytes = mQuestion.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size).copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }


            var context = convertView.context

            val data = context.getSharedPreferences("favoriteFlags", Context.MODE_PRIVATE)
            isFavorite = data.getBoolean(mQuestion.uid+"-"+titleFFlag,false)
            var fireStoreQuestion = FireStoreQuestion()


                    if (FirebaseAuth.getInstance().currentUser != null) {
                // Firebas
                mQuestionReference = FirebaseDatabase.getInstance().reference
                var favoriteImageView = convertView.findViewById<ImageView>(R.id.favoriteImageView)
                favoriteImageView.setImageResource(if (isFavorite) R.drawable.ic_star else R.drawable.ic_star_border)
                favoriteImageView.setOnClickListener {
                    val edit = data.edit()
                    if(isFavorite) {
                        isFavorite = false
                        //ここから
                        FirebaseFirestore.getInstance().collection(ContentsPATH).document(fireStoreQuestion.id).delete()

                        val favRef = mQuestionReference.child(FavoritesPATH).child(mQuestion.uid)
                        val data = fireStoreQuestion.id
                        favRef.setValue(data)
                        //ここまで
                    }else {
                        isFavorite = true
                        //ここから
                        FirebaseFirestore.getInstance().collection(ContentsPATH).document(fireStoreQuestion.id).set(fireStoreQuestion)

                        val favRef = mQuestionReference.child(FavoritesPATH).child(mQuestion.uid)
                        val data = fireStoreQuestion.id
                        favRef.setValue(data)
                        //ここまで
                    }
                    edit.commit()
                    favoriteImageView.setImageResource(if (isFavorite) R.drawable.ic_star else R.drawable.ic_star_border)
                }
            }
        } else {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_answer, parent, false)!!
            }

            val answer = mQuestion.answers[position - 1]
            val body = answer.body
            val name = answer.name

            val bodyTextView = convertView.bodyTextView as TextView
            bodyTextView.text = body

            val nameTextView = convertView.nameTextView as TextView
            nameTextView.text = name
        }

        return convertView
    }
}