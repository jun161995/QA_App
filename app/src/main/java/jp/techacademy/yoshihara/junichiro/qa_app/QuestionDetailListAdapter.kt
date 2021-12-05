package jp.techacademy.yoshihara.junichiro.qa_app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
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
import kotlinx.android.synthetic.main.list_answer.view.*
import kotlinx.android.synthetic.main.list_question_detail.view.nameTextView
import java.util.HashMap


class QuestionDetailListAdapter(context: Context, private val mQuestion: Question) : BaseAdapter() {
    companion object {
        private val TYPE_QUESTION = 0
        private val TYPE_ANSWER = 1
    }
    private var mLayoutInflater: LayoutInflater? = null
    init {
        mLayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }
    override fun getView(position: Int, view: View?, viewGroup: ViewGroup?): View {
        var convertView = view

        if (getItemViewType(position) == TYPE_QUESTION) {
            if (convertView == null) {
                convertView =
                    mLayoutInflater!!.inflate(R.layout.list_question_detail, viewGroup, false)!!
            }
            val body = mQuestion.body
            val name = mQuestion.name

            val bodyTextView = convertView.bodyTextView as TextView
            bodyTextView.text = body
            val nameTextView = convertView.nameTextView as TextView
            nameTextView.text = name

            val bytes = mQuestion.imageBytes
            if (bytes.isNotEmpty()) {
                val image = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    .copy(Bitmap.Config.ARGB_8888, true)
                val imageView = convertView.findViewById<View>(R.id.imageView) as ImageView
                imageView.setImageBitmap(image)
            }
        } else {
            if (convertView == null) {
                convertView = mLayoutInflater!!.inflate(R.layout.list_question_detail, viewGroup, false)!!
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


    override fun getItem(p0: Int): Any {
        return mQuestion
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return 1 + mQuestion.answers.size
    }

    // 質問か回答か
    override fun getItemViewType(position: Int): Int {
        return if(position == 0){
            TYPE_QUESTION
        }else{
            TYPE_ANSWER
        }
    }

    override fun getViewTypeCount(): Int {
        return 2
    }
}