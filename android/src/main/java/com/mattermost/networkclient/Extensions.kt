package com.mattermost.networkclient

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.ReadableType
import com.facebook.react.bridge.WritableMap
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject

var Response.retriesExhausted: Boolean? by NetworkClient.RequestRetriesExhausted

/**
 * Parses the response data into the format expected by the App
 *
 * @return WriteableMap for passing back to App
 */
fun Response.returnAsWriteableMap(): WritableMap {
    val headers = Arguments.createMap();
    this.headers.forEach { k -> headers.putString(k.first, k.second) }

    val map = Arguments.createMap()
    map.putMap("headers", headers)
    map.putString("data", this.body!!.string())
    map.putInt("code", this.code)
    map.putBoolean("ok", this.isSuccessful)
    map.putString("lastRequestedUrl", this.request.url.toString())

    if (this.retriesExhausted != null) {
        map.putBoolean("retriesExhausted", this.retriesExhausted!!)
    }

    return map;
}

/**
 * Parses options passed in over the bridge for individual requests
 *
 * @param options ReadableMap of options from the App
 */
fun Request.Builder.applyHeaders(headers: ReadableMap?): Request.Builder {
    if (headers != null){
        for ((k, v) in headers.toHashMap()) {
            this.removeHeader(k);
            this.addHeader(k, v as String);
        }
    }

    return this;
}

/**
 * Transforms the "body" for a POST/PATCH/PUT/DELETE request to a Request Body
 *
 * @return RequestBody
 */
fun ReadableMap.bodyToRequestBody(): RequestBody {
    if (!this.hasKey("body")) return "".toRequestBody()
    return if (this.getType("body") === ReadableType.Map) {
        JSONObject(this.getMap("body")!!.toHashMap()).toString().toRequestBody()
    } else {
        this.getString("body")!!.toRequestBody()
    }
}
