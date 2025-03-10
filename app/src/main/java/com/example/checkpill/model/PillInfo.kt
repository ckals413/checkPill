package com.example.checkpill.model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

// API 응답 루트 객체
data class PillResponse(
    @SerializedName("header")
    val header: Header,

    @SerializedName("body")
    val body: Body
) {
    data class Header(
        @SerializedName("resultCode")
        val resultCode: String,

        @SerializedName("resultMsg")
        val resultMsg: String
    )

    data class Body(
        @SerializedName("items")
        val items: List<PillInfo> = emptyList(),

        @SerializedName("numOfRows")
        val numOfRows: Int = 0,

        @SerializedName("pageNo")
        val pageNo: Int = 0,

        @SerializedName("totalCount")
        val totalCount: Int = 0
    )
}

// 알약 정보 모델
data class PillInfo(
    @SerializedName("itemSeq")
    val itemSeq: String, // 품목일련번호

    @SerializedName("itemName")
    val itemName: String, // 제품명

    @SerializedName("entpName")
    val entpName: String, // 업체명

    @SerializedName("itemImage")
    val itemImage: String? = null, // 이미지 URL

    @SerializedName("efcyQesitm")
    val efficacy: String? = null, // 효능

    @SerializedName("useMethodQesitm")
    val useMethod: String? = null, // 사용법

    @SerializedName("atpnWarnQesitm")
    val atpnWarn: String? = null, // 주의사항 경고

    @SerializedName("atpnQesitm")
    val atpn: String? = null, // 주의사항

    @SerializedName("intrcQesitm")
    val interaction: String? = null, // 상호작용

    @SerializedName("seQesitm")
    val sideEffect: String? = null, // 부작용

    @SerializedName("depositMethodQesitm")
    val depositMethod: String? = null, // 보관법

    @SerializedName("openDe")
    val openDate: String? = null, // 공개일자

    @SerializedName("updateDe")
    val updateDate: String? = null, // 수정일자

    @SerializedName("etcOtcName")
    val etcOtcName: String? = null, // 전문/일반 구분

    @SerializedName("bizrno")
    val bizrno: String? = null // 제조사 사업자등록번호
) : Parcelable {

    constructor(parcel: Parcel) : this(
        itemSeq = parcel.readString() ?: "",
        itemName = parcel.readString() ?: "",
        entpName = parcel.readString() ?: "",
        itemImage = parcel.readString(),
        efficacy = parcel.readString(),
        useMethod = parcel.readString(),
        atpnWarn = parcel.readString(),
        atpn = parcel.readString(),
        interaction = parcel.readString(),
        sideEffect = parcel.readString(),
        depositMethod = parcel.readString(),
        openDate = parcel.readString(),
        updateDate = parcel.readString(),
        etcOtcName = parcel.readString(),
        bizrno = parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(itemSeq)
        parcel.writeString(itemName)
        parcel.writeString(entpName)
        parcel.writeString(itemImage)
        parcel.writeString(efficacy)
        parcel.writeString(useMethod)
        parcel.writeString(atpnWarn)
        parcel.writeString(atpn)
        parcel.writeString(interaction)
        parcel.writeString(sideEffect)
        parcel.writeString(depositMethod)
        parcel.writeString(openDate)
        parcel.writeString(updateDate)
        parcel.writeString(etcOtcName)
        parcel.writeString(bizrno)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PillInfo> {
        override fun createFromParcel(parcel: Parcel): PillInfo {
            return PillInfo(parcel)
        }

        override fun newArray(size: Int): Array<PillInfo?> {
            return arrayOfNulls(size)
        }
    }
}