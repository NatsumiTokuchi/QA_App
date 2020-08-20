package jp.techacademy.natsumi.tokuchi.qa_app

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class Favorite(val favorites: ArrayList<Favorite>) {
}