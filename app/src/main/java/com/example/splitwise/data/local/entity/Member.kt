package com.example.splitwise.data.local.entity

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
): Parcelable {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "member_id")
    var memberId: Int = 0

    override fun toString(): String {
        return name
    }
}
