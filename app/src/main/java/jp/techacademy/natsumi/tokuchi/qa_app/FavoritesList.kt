package jp.techacademy.natsumi.tokuchi.qa_app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import android.util.Base64
import android.widget.ListView
import com.google.firebase.database.*

class FavoritesList : AppCompatActivity() {

    private lateinit var mFavoriteRef: DatabaseReference
    private lateinit var mListView: ListView
    private var mQuestionArrayList = ArrayList<Question>()
    private lateinit var mAdapter: FavoritesListAdapter
    private var mGenre = 0

    private val mEventListener = object : ChildEventListener {
        override fun onCancelled(databaseError: DatabaseError) {
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {
        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val map = dataSnapshot.value as Map<String, String>
            val questionUid = map["questionUid"] ?: ""
            val genre = map["genre"] ?: ""
            mGenre = genre.toInt()

            val databaseReference = FirebaseDatabase.getInstance().reference
            val mQuestionRef = databaseReference.child(ContentsPATH).child(genre)
            mQuestionRef.addChildEventListener( object: ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    val favQuestionUid = dataSnapshot.key ?: ""
                    if (favQuestionUid == questionUid) {
                        val map = dataSnapshot.value as Map<String, String>
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

                        val question = Question(
                            title,
                            body,
                            name,
                            uid,
                            dataSnapshot.key ?: "",
                            mGenre,
                            bytes,
                            answerArrayList
                        )
                        mQuestionArrayList.add(question)
                        mAdapter.notifyDataSetChanged()
                    }
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                    val map = dataSnapshot.value as Map<String, String>

                    // 変更があったQuestionを探す
                    for (question in mQuestionArrayList) {
                        if (dataSnapshot.key.equals(question.questionUid)) {
                            // このアプリで変更があるのは回答（Answer）のみ
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
                            mAdapter.notifyDataSetChanged()
                        }
                    }
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                }

                override fun onCancelled(p0: DatabaseError) {
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {
                }
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites_list)

        // タイトルを設定
        title = "お気に入り"

        // ListViewの準備
        mListView = findViewById(R.id.fav_ListView)
        mAdapter = FavoritesListAdapter(this)
        mQuestionArrayList = ArrayList<Question>()
        mAdapter.notifyDataSetChanged()

        mListView.setOnItemClickListener { parent, view, position, id ->
            // Questionのインスタンスを渡して質問詳細画面に移行する
            val intent = Intent(applicationContext, QuestionDetailActivity::class.java)
            intent.putExtra("question", mQuestionArrayList[position])
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        // Firebase
        val user = FirebaseAuth.getInstance()!!.currentUser
        val databaseReference = FirebaseDatabase.getInstance().reference
        mFavoriteRef = databaseReference.child(FavoritePATH).child(user!!.uid)
        mFavoriteRef.addChildEventListener(mEventListener)

        // 質問リストをクリアしてから、再度Adapterをセットし、AdapterをListViewにセットし直す
        mQuestionArrayList.clear()
        mAdapter.setQuestionArrayList(mQuestionArrayList)
        mListView.adapter = mAdapter
    }
}
