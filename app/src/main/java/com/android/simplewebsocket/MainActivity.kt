package com.android.simplewebsocket

import android.os.Bundle
import android.util.Log.d
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okio.ByteString
import org.json.JSONObject



class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Listener класс
        class EchoWebSocketListener : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                webSocket.send(subscription) //после коннекта подписывается на определенный канал
                                             //в данном случае: ticker, BTC-USD
            }

            override fun onMessage(webSocket: WebSocket, text: String){
                super.onMessage(webSocket, text)
                outputData("Receiving $text")

                //подготовка jsonObject для дальнейшего получения инфы от него
                var jsonObject: JSONObject? = null

                try {
                    jsonObject = JSONObject(text)
                } catch (e:Exception) {
                    e.printStackTrace()
                }

                if (jsonObject!=null) {
                    try {
                        //получает инфу только о getString("price")
                        var price:String = jsonObject.getString("price")

                        //обновляет textview в соотв. с ценой на биткоин (price)
                        runOnUiThread(Runnable {
                            run {
                                tv_btcPrice.setText("= "+price+" USD")
                            }
                        })

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }


            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                super.onMessage(webSocket, bytes)
                outputData("Receiving bytes : " + bytes.hex())

            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosing(webSocket, code, reason)
                outputData("$code $reason")
            }


            private fun outputData(outputString: String) {
                //логи
                d("web socket", outputString)
            }

            //https://docs.pro.coinbase.com/#channels
            //копипаста из доков
            val subscription:String = "{\n" +
                    "    \"type\": \"subscribe\",\n" +
                    "    \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"BTC-USD\"] }]\n" +
                    "}"


        }

        //создание подключения к GDAX websocket feed + корутины
        val listener = EchoWebSocketListener()
        GlobalScope.launch(Dispatchers.IO){
            val httpClient = OkHttpClient()
            val request = Request.Builder()
                .url("wss://ws-feed.pro.coinbase.com")
                .build()

            val webSocket = httpClient.newWebSocket(request, listener) //webSocket
            httpClient.dispatcher.executorService.shutdown()
        }
    }
}