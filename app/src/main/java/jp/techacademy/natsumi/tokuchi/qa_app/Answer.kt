package jp.techacademy.natsumi.tokuchi.qa_app

import java.io.Serializable

class Answer(val body: String, val name: String, val uid: String, val answerUid: String): Serializable {
}