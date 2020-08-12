package jp.techacademy.natsumi.tokuchi.qa_app

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_question_detail.*

import java.util.HashMap

class QuestionDetailActivity : AppCompatActivity() {

    private lateinit var mQuestion: Question
    private lateinit var mAdapter: QuestionDetailListAdapter
    private lateinit var mAnswerRef: DatabaseReference
    private lateinit var mFavoriteRef: DatabaseReference

    private var mIsFavorites = false

    private val mEventListener = object : ChildEventListener {
        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

            val answerUid = dataSnapshot.key ?: ""

            for (answer in mQuestion.answers) {
                // 同じanswerUidのものが存在しているときは何もしない
                if (answerUid == answer.answerUid) {
                    return
                }
            }

            val body = dataSnapshot.key ?: ""
            val name = dataSnapshot.key ?: ""
            val uid = dataSnapshot.key ?: ""

            val answer = Answer(body, name, uid, answerUid)
            mQuestion.answers.add(answer)
            mAdapter.notifyDataSetChanged()
        }

        override fun onCancelled(databaseError: DatabaseError) {

        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {

        }
    }

    private val favEventListener = object : ChildEventListener {
        override fun onCancelled(databaseError: DatabaseError) {}

        override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}

        override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
            val questionUid = dataSnapshot.value
            if (questionUid == mQuestion.questionUid) {
                mIsFavorites = true
                favorite_button.setImageResource(R.drawable.baseline_star_white_24)
            }
        }

        override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_detail)

        // 渡ってきたQuestionのオブジェクトを保持する
        val extras = intent.extras
        mQuestion = extras.get("question") as Question

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
                // Questionを渡して回答作成画面を起動させる
                val intent = Intent(applicationContext, AnswerSendActivity::class.java)
                intent.putExtra("question", mQuestion)
                startActivity(intent)
            }
        }

        val databaseReference = FirebaseDatabase.getInstance().reference
        mAnswerRef = databaseReference.child(ContentsPATH).child(mQuestion.genre.toString())
            .child(mQuestion.questionUid).child(
                AnswersPATH
            )

        mAnswerRef.addChildEventListener(mEventListener)

        val user = FirebaseAuth.getInstance().currentUser
        mFavoriteRef =
            databaseReference.child(FavoritePATH).child(user!!.uid).child(mQuestion.questionUid)
        mFavoriteRef.addChildEventListener(favEventListener)

        favorite_button.setOnClickListener {
            if (mIsFavorites) {
                mFavoriteRef.removeValue()
                favorite_button.setImageResource(R.drawable.baseline_star_border_white_24)
                mIsFavorites = false
            } else {
                mFavoriteRef.push().setValue(mQuestion.questionUid)
                favorite_button.setImageResource(R.drawable.baseline_star_white_24)
            }
        }

}

    override fun onResume() {
        super.onResume()
        val user = FirebaseAuth.getInstance().currentUser
        val favoriteButton = findViewById<View>(R.id.favorite_button)

        if (user == null) {
            favoriteButton.visibility = View.GONE
        } else {
            favoriteButton.visibility = View.VISIBLE
        }
    }
}