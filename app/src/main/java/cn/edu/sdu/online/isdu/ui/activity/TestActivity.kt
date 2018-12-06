package cn.edu.sdu.online.isdu.ui.activity

import android.graphics.BitmapFactory
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import cn.edu.sdu.online.isdu.R
import kotlinx.android.synthetic.main.activity_test.*
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class TestActivity : AppCompatActivity() {


    private var mSocket: WebSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

//        button.setOnClickListener {
//            val client = OkHttpClient.Builder()
//                    .connectTimeout(5, TimeUnit.SECONDS)
//                    .writeTimeout(5, TimeUnit.SECONDS)
//                    .readTimeout(5, TimeUnit.SECONDS)
//                    .build()
//            val request = Request.Builder()
//                    .url("ws://192.168.0.101:8080/ws/jzz")
//                    .build()
//            client.newWebSocket(request, MyWebSocketListener())
////        client.dispatcher().executorService().shutdown()
//        }

    }
//
//    inner class MyWebSocketListener : WebSocketListener() {
//        override fun onOpen(webSocket: WebSocket?, response: Response?) {
//            super.onOpen(webSocket, response)
//            mSocket = webSocket
//            Log.d("Jzz", "onOpen")
//        }
//
//        override fun onFailure(webSocket: WebSocket?, t: Throwable?, response: Response?) {
//            super.onFailure(webSocket, t, response)
//            t?.printStackTrace()
//            Log.d("Jzz", "onFailure")
//        }
//
//        override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
//            super.onClosing(webSocket, code, reason)
//            Log.d("Jzz", "onClosing")
//        }
//
//        override fun onMessage(webSocket: WebSocket?, text: String?) {
//            super.onMessage(webSocket, text)
//            text_view?.text = text
//            Log.d("Jzz", "onMessage")
//        }
//
//        override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
//            super.onMessage(webSocket, bytes)
//            Log.d("Jzz", "onMessage")
//        }
//
//        override fun onClosed(webSocket: WebSocket?, code: Int, reason: String?) {
//            super.onClosed(webSocket, code, reason)
//            Log.d("Jzz", "onClosed")
//        }
//    }
}
