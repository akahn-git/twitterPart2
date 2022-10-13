package com.codepath.apps.restclienttemplate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import okhttp3.Headers

class ComposeActivity : AppCompatActivity() {

    lateinit var etCompose:EditText
    lateinit var btnTweet:Button

    lateinit var client:TwitterClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compose)

        etCompose=findViewById(R.id.etTweetCompose)
        btnTweet=findViewById(R.id.btnTweet)

        client=TwitterApplication.getRestClient(this)

        //handling the users click on the button
        btnTweet.setOnClickListener{

            //grab the content of edittext
            val tweetContent=etCompose.text.toString()

            //1.tweet isnt empty
            if(tweetContent.isEmpty()){
                Toast.makeText(this,"Empty tweet",Toast.LENGTH_SHORT).show()
            }
            //2.tweet isnt over character count
            else if(tweetContent.length>280){
                Toast.makeText(this,"Tweet over character limit 280",Toast.LENGTH_SHORT).show()
            }
            else {
                //make an api call to publish the tweet
                client.publishTweet(tweetContent,object : JsonHttpResponseHandler(){

                    override fun onFailure(
                        statusCode: Int,
                        headers: Headers?,
                        response: String?,
                        throwable: Throwable?
                    ) {
                        Log.e("ComposeActivity","failed to publish tweet",throwable)
                    }

                    override fun onSuccess(statusCode: Int, headers: Headers?, json: JSON) {
                        Log.i("ComposeActivity","Succesfully published")
                        //send back the tweet
                        val tweet = Tweet.fromJson(json.jsonObject)

                        val intent = Intent()
                        intent.putExtra("tweet",tweet)
                        setResult(RESULT_OK,intent)
                        finish()
                    }

                })
            }
        }
    }
}