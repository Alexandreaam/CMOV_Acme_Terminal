package com.acme_industries.cmov_acme_terminal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.zxing.integration.android.IntentIntegrator
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    val TAG: String = "TAG"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.message)
        button.setOnClickListener {
            scanQRCode()
        }
    }

    private fun scanQRCode(){
        val integrator = IntentIntegrator(this).apply {
            captureActivity = CaptureActivity::class.java
            setOrientationLocked(false)
            setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
            setPrompt("Scanning Code")
        }
        integrator.initiateScan()
    }

    // Get the results:
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            else parseRequest(result.contents)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun parseRequest(message: String) {
        try {
            val contents = JSONObject(message)
            if(contents.has("Products") && contents.has("Vouchers")
                && contents.has("userid") && contents.has("Total")){

                val url = Constants.serverUrl + "order"
                val queue = Volley.newRequestQueue(this)

                //TODO (Simplify translation to json array)
                val orderTest = JSONObject()
                orderTest.put("Products",contents.get("Products"))
                orderTest.put("Vouchers",contents.get("Vouchers"))
                orderTest.put("userid", contents.get("userid"))
                orderTest.put("Total", contents.get("Total"))

                val jsonObjectRequest = JsonObjectRequest(
                    Request.Method.POST, url, orderTest ,
                    { response ->
                        println("Response is: $response")
                    },
                    { error ->
                        println("That didn't work: ${error.message}")
                    })
                queue.add(jsonObjectRequest)
            } else {
                Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_LONG).show()
        }
    }
}