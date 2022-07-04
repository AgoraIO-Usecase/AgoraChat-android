package io.stipop.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class StipopConfigData(
    @SerializedName("api_key") var apiKey: String = "",
    @SerializedName("StickerIcon") var spvIcon: String = "ic_sticker_border_3",
    @SerializedName("Theme") var theme: Theme = Theme(),
    @SerializedName("Search") var searchConfig: SearchConfig = SearchConfig(),
    @SerializedName("LiteStore") var liteStoreConfig: LiteStoreConfig = LiteStoreConfig(),
    @SerializedName("MySticker") var myStickerConfig: MyStickerConfig = MyStickerConfig(),
    @SerializedName("Keyboard") var keyboardConfig: KeyboardConfig = KeyboardConfig(),
    @SerializedName("StorePolicy") var policyConfig: StorePolicyConfig = StorePolicyConfig(),
    @SerializedName("Sticker") var stickerConfig: StickerConfig = StickerConfig(),
    @SerializedName("Send") var previewConfig: PreviewConfig = PreviewConfig()
) {
    @Keep
    data class Theme(
        @SerializedName("useLightMode") var useLightMode: Boolean = true,
        @SerializedName("backgroundColor") var backgroundColor: DayNightColor = DayNightColor(
            light = "#FFFFFF",
            "#171B1C"
        ),
        @SerializedName("groupedContentBackgroundColor") var groupedContentBackgroundColor: DayNightColor = DayNightColor(
            light = "#F7F8F9",
            dark = "#2E363A"
        ),
        @SerializedName("mainColor") var mainColor: DayNightColor = DayNightColor(
            light = "#FF501E",
            dark = "#FF8558"
        ),
        @SerializedName("iconColor") var iconColor: IconColor = IconColor(
            normalColor = DayNightColor(
                light = "#414141",
                dark = "#646F7C"
            ), tintColor = DayNightColor(light = "#FF5D1E", dark = "#FF855B")
        ),
        @SerializedName("font") var font: FontTheme = FontTheme()
    ) {
        @Keep
        data class IconColor(
            @SerializedName("normalColor") var normalColor: DayNightColor,
            @SerializedName("tintColor") var tintColor: DayNightColor
        )

        @Keep
        data class FontTheme(
            @SerializedName("family") var family: String = "system",
            @SerializedName("weight") var weight: Int = 400,
            @SerializedName("character") var character: Float = 0f
        )
    }

    @Keep
    data class DayNightColor(
        @SerializedName("light") var light: String,
        @SerializedName("dark") var dark: String
    ) {
        fun getDayNightKey(isLightMode: Boolean): String {
            return when (isLightMode) {
                true -> light
                false -> dark
            }
        }
    }

    @Keep
    data class DayNightName(
        @SerializedName("light") var light: String,
        @SerializedName("dark") var dark: String
    ) {
        fun getDayNightKey(isLightMode: Boolean): String {
            return when (isLightMode) {
                true -> light
                false -> dark
            }
        }
    }

    @Keep
    data class SearchConfig(
        @SerializedName("searchbarIcon") var searchBarIcon: String = "icon_search",
        @SerializedName("searchbarDeleteIcon") var searchbarDeleteIcon: String = "ic_erase_border_3",
        @SerializedName("searchbarRadius") var searchbarRadius: Int = 10,
        @SerializedName("numOfColumns") var columnCount: Int = 3,
        @SerializedName("searchTags") var visibleSearchTags: SearchTagConfig = SearchTagConfig(),
    ) {
        @Keep
        data class SearchTagConfig(
            @SerializedName("hidden") var isHidden: Boolean = false
        )
    }

    @Keep
    data class LiteStoreConfig(
        @SerializedName("trending") var trendingConfig: TrendingConfig = TrendingConfig(),
        @SerializedName("listType") var listType: String = "horizontal",
        @SerializedName("downloadIcon") var downloadIcon: String = "ic_download_border_3",
        @SerializedName("completeIcon") var completeIcon: String = "ic_downloaded_border_3",
        @SerializedName("bottomOfSearch") var bottomOfSearch: String = "recommendedTags"
    ) {

        fun isRecommendTagShow() = bottomOfSearch == "recommendedTags"

        @Keep
        data class TrendingConfig(
            @SerializedName("useBackgroundColor") var isUseBackgroundColor: Boolean = false,
            @SerializedName("backgroundColor") var backgroundColor: String = "#eeeeee",
            @SerializedName("opacity") var opacity: Double = 0.7
        )
    }

    @Keep
    data class MyStickerConfig(
        @SerializedName("orderIcon") var orderIcon: String = "ic_move_border_3",
        @SerializedName("hideIcon") var hideIcon: String = "ic_hide_border_3",
    )

    @Keep
    data class KeyboardConfig(
        @SerializedName("liteStoreIcon") var liteStoreIcon: String = "ic_em_store",
        @SerializedName("liteSearchIcon") var liteSearchIcon: String = "ic_em_store",
        @SerializedName("numOfColumns") var columnCount: Int = 3,
    )

    @Keep
    data class StorePolicyConfig(
        @SerializedName("allowPremium") var allowPremium: String = "N",
        @SerializedName("price") var price: PricingConfig = PricingConfig(),
    ) {
        @Keep
        data class PricingConfig(
            @SerializedName("png") var png: Double = 0.99,
            @SerializedName("gif") var gif: Double = 1.99
        )
    }

    @Keep
    data class StickerConfig(
        @SerializedName("backIcon") var backIcon: String = "ic_back_border_3",
        @SerializedName("closeIcon") var closeIcon: String = "ic_close_border_3",
        @SerializedName("numOfColumns") var columnCount: Int = 3,
    )

    @Keep
    data class PreviewConfig(
        @SerializedName("preview") var preview: Boolean = false,
        @SerializedName("previewPadding") var previewPadding: Int = 100,
        @SerializedName("favoritesOnIcon") var favoritesOnIcon: DayNightName = DayNightName(
            light = "ic_favorites_on",
            dark = "ic_favorites_on"

        ),
        @SerializedName("favoritesOffIcon") var favoritesOffIcon: DayNightName = DayNightName(
            light = "ic_favorites_off",
            dark = "ic_favorites_off"
        ),
        @SerializedName("closeIcon") var closeIcon: DayNightName = DayNightName(
            light = "ic_cancel",
            dark = "ic_cancel"
        ),
    )
}