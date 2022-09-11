package com.example.splitwise.data.local.entity

import android.net.Uri
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
@Entity
data class Member(
    val name: String,
    val phone: Long,
    @ColumnInfo(name =  "member_profile")
    var memberProfile: Uri?
): Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "member_id")
    var memberId: Int = 0

    override fun toString(): String {
        return name
    }
}
