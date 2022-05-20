package io.stipop.models.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import io.stipop.models.StickerPackage

@Keep
internal data class MyStickerResponse(
    @SerializedName("header") val header: ResponseHeader,
    @SerializedName("body") val body: ResponseBody,
) {
    @Keep
    data class ResponseBody(
        @SerializedName("packageList") val packageList: List<StickerPackage>,
        @SerializedName("pageMap") val pageMap: PageMapInfo
    )

    @Keep
    data class PageMapInfo(
        @SerializedName("pageNumber") val pageNumber: Int,
        @SerializedName("onPageCountRow") val onPageCountRow: Int,
        @SerializedName("totalCount") val totalCount: Int,
        @SerializedName("pageCount") val pageCount: Int,
        @SerializedName("groupCount") val groupCount: Int,
        @SerializedName("groupNumber") val groupNumber: Int,
        @SerializedName("pageGroupCount") val pageGroupCount: Int,
        @SerializedName("startPage") val startPage: Int,
        @SerializedName("endPage") val endPage: Int,
        @SerializedName("startRow") val startRow: Int,
        @SerializedName("endRow") val endRow: Int,
        @SerializedName("modNum") val modNum: Int,
        @SerializedName("listStartNumber") val listStartNumber: Int
    )
}
