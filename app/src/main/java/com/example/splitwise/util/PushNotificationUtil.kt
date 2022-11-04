package com.example.splitwise.util

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import com.example.splitwise.data.local.SplitWiseRoomDatabase
import com.example.splitwise.data.repository.GroupRepository
import com.example.splitwise.data.repository.MemberRepository
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

var accessToken = ""

suspend fun storeFcmToken(context: Context){

    Log.d(TAG, "storeFcmToken: method called")
    val database = SplitWiseRoomDatabase.getInstance(context)
    val memberRepository = MemberRepository(database)

    val member = memberRepository.getMember(1)

    if(member != null){
        
        val user = hashMapOf(
            "username" to member.name.substring(0, member.name.length - 5),
            "token" to accessToken
        )

        val db = Firebase.firestore
        db.collection("fcm_tokens")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d(ContentValues.TAG, "token added to firestore: ${documentReference.id}")
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "failed to add token to firestore: $exception ")
            }
    }
    else{
        Log.d(TAG, "storeFcmToken: token  member is null")
    }


}