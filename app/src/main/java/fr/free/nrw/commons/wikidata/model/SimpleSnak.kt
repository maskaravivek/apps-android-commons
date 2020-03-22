package fr.free.nrw.commons.wikidata.model

import com.google.gson.annotations.SerializedName

data class SimpleSnak(
        @SerializedName("snaktype")
        var snaktype: String? = null,
        @SerializedName("property")
        var property: String? = null,
        @SerializedName("datavalue")
        var datavalue: SimpleDataValue? = null)