package jp.techacademy.yoshihara.junichiro.qa_app

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class FavoriteActivity : AppCompatActivity() {

    private var mFavorite: MutableList<String> = mutableListOf()
    private var keySplit: List<String> = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)
        Log.d("TEST", "CREATE")
        title = "お気に入り"
    }

    override fun onResume() {
        super.onResume()
        load()

    }

    private fun load() {
        val data = getSharedPreferences("favoriteFlags", Context.MODE_PRIVATE)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            for (entry in data.all) {
                val key: String = entry.key
                keySplit = key.split("/")
                if (user.uid.toString() == keySplit[0].toString()) {
                    val value: Any? = entry.value
                    Log.d("TEST", keySplit[1].toString())

                    mFavorite.add(keySplit[1].toString())
                }
            }
        }

        val listView = findViewById<ListView>(R.id.listViewFavorite)
        val adapter = ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, mFavorite)

        listView.adapter = adapter
    }
}