package com.example.planthealth

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color.parseColor
import android.net.Uri

import android.os.Bundle
import android.provider.MediaStore

import androidx.appcompat.app.AppCompatActivity
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

import okhttp3.MediaType.Companion.toMediaTypeOrNull

import okhttp3.OkHttpClient
import okhttp3.Request

import java.io.*

import android.util.Base64
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import com.example.planthealth.databinding.ActivityMainBinding
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class MainActivity2 : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding
    private var current = 0
    private var encodedFile: String? = null
    private val classId = arrayOf(
        arrayOf(6, 7, 4, 5),
        arrayOf(18, 19, 21, 20),
        arrayOf(35, 33, 34),
        arrayOf(24, 30, 23, 26, 29, 22, 28, 31, 25, 27),
        arrayOf(15, 16, 17),
        arrayOf(1, 0, 3, 2)
    )

    // private val apiUrl = "https://detect.roboflow.com/"
    private val apiKey = "6jQh39N374utn2mZbjkd"
    private val modelId = "bd_plant_diseases/1"

    private val plants = arrayOf("Corn", "Rice", "Wheat", "Tomato", "Potato", "Cauliflower")


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        //imageView = findViewById(R.id.imageView)
        val ad = ArrayAdapter(this, android.R.layout.simple_spinner_item, plants)
        ad.setDropDownViewResource(androidx.appcompat.R.layout.support_simple_spinner_dropdown_item)
        b.spinner.adapter = ad

        b.spinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                current = position
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

        b.tryagain.setOnClickListener {
            b.progressbar.visibility = View.GONE
            b.plantname.visibility = View.GONE
            b.condition.visibility = View.GONE
            b.symptompslabel.visibility = View.GONE
            b.symptoms.visibility = View.GONE
            b.preventionlabel.visibility = View.GONE
            b.prevention.visibility = View.GONE
            b.curelabel.visibility = View.GONE
            b.cure.visibility = View.GONE
            b.tryagain.visibility = View.GONE
            b.sorry.visibility=View.GONE
            b.btnSelectImage.visibility = View.VISIBLE
            b.btnUploadImage.visibility = View.VISIBLE
            b.selecttext.visibility = View.VISIBLE
            b.spinner.visibility = View.VISIBLE
            current = 0
            b.spinner.setSelection(0)
        }


        // Button to select image
        b.btnSelectImage.setOnClickListener {
            ImagePicker.with(this@MainActivity2).cropSquare().start()
        }

        // Button to upload image
        b.btnUploadImage.setOnClickListener {
            b.progressbar.visibility = View.VISIBLE
            encodedFile?.let {
                uploadImageToAPI(it)
            } ?: run {
                Toast.makeText(
                    this@MainActivity2, "Please select an image first", Toast.LENGTH_SHORT
                ).show()
            }
        }
        b.symptompslabel.setOnClickListener {
            if (b.symptoms.isVisible) {
                b.symptoms.visibility = View.GONE
                b.symptompslabel.text = "Symptomps (click to show)"
            } else {
                b.symptoms.visibility = View.VISIBLE
                b.symptompslabel.text = "Symptomps (click to hide)"
            }
        }
        b.preventionlabel.setOnClickListener {
            if (b.prevention.isVisible) {
                b.prevention.visibility = View.GONE
                b.preventionlabel.text = "Suggestions for Prevention (click to show)"
            } else {
                b.prevention.visibility = View.VISIBLE
                b.preventionlabel.text = "Suggestions for Prevention (click to hide)"
            }
        }
        b.curelabel.setOnClickListener {
            if (b.cure.isVisible) {
                b.cure.visibility = View.GONE
                b.curelabel.text = "Suggestions for Cure (click to show)"
            } else {
                b.cure.visibility = View.VISIBLE
                b.curelabel.text = "Suggestions for Cure (click to hide)"
            }
        }

    }

    // Handle image selection
    //@RequiresApi(Build.VERSION_CODES.O)
    @Deprecated("Deprecated in Java")
    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == RESULT_OK && data != null) {
            val uri: Uri? = data.data
            uri?.let {
                val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                b.imageView.setImageBitmap(bitmap)
                //fun bitmapToByteArray(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): ByteArray {
                val byteArrayOutputStream = ByteArrayOutputStream()

                // Compress the bitmap and write the bytes to the output stream
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

                val bytes = byteArrayOutputStream.toByteArray()

                // selectedImageFile = ImagePicker.getFile(data)!!
                encodedFile = Base64.encodeToString(bytes, Base64.DEFAULT)
                //  uploadImageToAPI(encodedFile)
            }

        }
    }

    private fun uploadImageToAPI(image: String) {
        val baseURL = "https://detect.roboflow.com/$modelId"
        val urlBuilder = baseURL.toHttpUrlOrNull()?.newBuilder()
        urlBuilder?.addQueryParameter("api_key", apiKey)
        urlBuilder?.addQueryParameter("confidence", "25")

// Construct the final URL
        val uploadURL = urlBuilder?.build().toString()

        val client = OkHttpClient()

        // Create the request body
        val requestBody =
            image.toRequestBody("application/x-www-form-urlencoded".toMediaTypeOrNull())

        // Create the request
        val request = Request.Builder().url(uploadURL).post(requestBody)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .addHeader("Content-Language", "en-US").build()

        // Run the network call on a background thread
        GlobalScope.launch(Dispatchers.IO) {
            try {
                // Execute the request synchronously
                val response = client.newCall(request).execute()
                response.use {
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected code $response")
                    }

                    // Print the response
                    val r = response.body?.string()
                    println(r)
                    if (r != null) {
                        handleResult(r)
                    }

                }
            } catch (e: Exception) {

                Toast.makeText(
                    this@MainActivity2,
                    "Can't connect to the server. Please check your connection.",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }
    }

    private fun handleResult(result: String) {
        val json = JSONObject(result)
        val prediction = json.getJSONObject("predictions")
        //val value = prediction.getJSONObject("Cauliflower_Healthy")
        var predicted_id = -1
        var predicted_score: Double = -1.0
        for (names in prediction.keys()) {
            val p = prediction.getJSONObject(names)
            val id = p.getString("class_id").toInt()
            if (id in classId[current]) {
                val confi = p.getString("confidence").toDouble()
                if (confi > 0.25 && confi > predicted_score) {
                    predicted_score = confi
                    predicted_id = id
                }
            }
        }
        if (predicted_id == -1) {
            runOnUiThread {
                b.sorry.visibility = View.VISIBLE
                b.tryagain.visibility = View.VISIBLE
                b.progressbar.visibility=View.GONE
                b.btnSelectImage.visibility = View.GONE
                b.btnUploadImage.visibility = View.GONE
                b.selecttext.visibility = View.GONE
                b.spinner.visibility = View.GONE
            }
        } else {
            val flat = classId.flatten()

            // println(value.toString())

            val raw = readJsonFromRaw(this@MainActivity2, R.raw.output)
            val array = JSONArray(raw)
            val o = array.getJSONObject(flat.indexOf(predicted_id))
            println(o.getString("Plant"))
            println(o.getString("Disease"))


            // println(o.getString("Symptoms"))
            // println(o.getString("Recommendations(for prevention)"))
            runOnUiThread {
                b.btnSelectImage.visibility = View.GONE
                b.btnUploadImage.visibility = View.GONE
                b.selecttext.visibility = View.GONE
                b.spinner.visibility = View.GONE
                b.progressbar.visibility = View.GONE
                b.tryagain.visibility = View.VISIBLE
                b.plantname.visibility = View.VISIBLE
                b.condition.visibility = View.VISIBLE
                b.symptompslabel.visibility = View.VISIBLE
                b.preventionlabel.visibility = View.VISIBLE
                b.curelabel.visibility = View.VISIBLE

                b.plantname.text = "Plant name: ${o.getString("Plant")}"
                b.condition.text = "Condition: ${o.getString("Disease")}"
                b.symptoms.text = o.getString("Symptoms")
                b.prevention.text = o.getString("Recommendations(for prevention)")
                b.cure.text = o.getString("Recommendation(For cure and take care)")
                if (o.getString("Disease")=="Healthy"){
                    b.condition.setTextColor(parseColor("#4B7B11"))
                }
                else{
                    b.condition.setTextColor(parseColor("#EA1F12"))
                }

            }
        }

    }

    private fun readJsonFromRaw(context: Context, resourceId: Int): String {
        return context.resources.openRawResource(resourceId).bufferedReader().use { it.readText() }
    }


}
