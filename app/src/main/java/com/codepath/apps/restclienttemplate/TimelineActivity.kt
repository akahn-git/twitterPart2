package com.codepath.apps.restclienttemplate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.codepath.apps.restclienttemplate.models.Tweet
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler
import com.google.android.material.floatingactionbutton.FloatingActionButton
import okhttp3.Headers
import org.json.JSONException

class TimelineActivity : AppCompatActivity() {

    lateinit var client: TwitterClient

    lateinit var rvTweets:RecyclerView

    lateinit var adapter: TweetsAdapter

    lateinit var swipeContainer:SwipeRefreshLayout

    lateinit var scrollListener:EndlessRecyclerViewScrollListener

    lateinit var fab:FloatingActionButton

    val tweets = ArrayList<Tweet>()

    var maxId: Long = Long.MAX_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        client = TwitterApplication.getRestClient(this)

        swipeContainer=findViewById(R.id.swipeContainer)

        swipeContainer.setOnRefreshListener {
            Log.i(TAG,"Refreshing timeline")
            populateHomeTimeLine()
        }

        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light);

        rvTweets=findViewById(R.id.rvTweets)

        fab= findViewById(R.id.fab)
        fab.setOnClickListener{
            val intent = Intent(this,ComposeActivity::class.java)
            startActivityForResult(intent,10)
        }

        adapter= TweetsAdapter(tweets)

        rvTweets.layoutManager=LinearLayoutManager(this)
        scrollListener = object : EndlessRecyclerViewScrollListener(rvTweets.layoutManager as LinearLayoutManager) {
            override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView?) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                loadMoreData()
            }
        }
        rvTweets.adapter=adapter
        rvTweets.addOnScrollListener(scrollListener)

        populateHomeTimeLine()
    }

    private fun loadMoreData() {
        client.getNextPageOfTweets(object: JsonHttpResponseHandler(){
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.i(TAG,"onFailure loadMoreData $statusCode maxId $maxId")
            }

            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i(TAG,"onSuccess loadMoreData")

                val jsonArray =json.jsonArray

                try {
                    val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)
                    tweets.addAll(listOfNewTweetsRetrieved)
                    getMaxId()
                    Log.i(TAG,"maxId $maxId tweetsSize ${tweets.size} newTweetsSize ${listOfNewTweetsRetrieved.size} jsonArray $jsonArray")
                    adapter.notifyDataSetChanged()
                    swipeContainer.setRefreshing(false)
                    //scrollListener.resetState()
                }catch(e:JSONException){
                    Log.e(TAG,"JSON Exception $e")
                }
            }

        },maxId.toLong())
    }

    fun getMaxId() {
        for (i in 0 until tweets.size){
            if(tweets[i].id<maxId)
                maxId= tweets[i].id
        }
    }

    //removed to add floating action button
    /*
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main,menu)
        return true
    }


    //handles clicks on menu item
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.compose){
            //navigate to compose screen
            val intent = Intent(this,ComposeActivity::class.java)
            startActivityForResult(intent,10)
        }
        return super.onOptionsItemSelected(item)
    }*/

    //called when we come back from compose activity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode== RESULT_OK && requestCode == REQUEST_CODE){

            //get data from intent
            val tweet = data?.getParcelableExtra("tweet") as Tweet

            //update timeline
            //modify data source of tweet
            tweets.add(0,tweet)

            //update adapter
            adapter.notifyItemInserted(0)
            rvTweets.smoothScrollToPosition(0)
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    fun populateHomeTimeLine(){
        client.getHomeTimeline(object: JsonHttpResponseHandler(){
            override fun onFailure(
                statusCode: Int,
                headers: Headers?,
                response: String?,
                throwable: Throwable?
            ) {
                Log.i(TAG,"onFailure $statusCode")
            }

            override fun onSuccess(statusCode: Int, headers: Headers, json: JSON) {
                Log.i(TAG,"onSuccess populate")

                val jsonArray =json.jsonArray

                try {
                    adapter.clear()
                    val listOfNewTweetsRetrieved = Tweet.fromJsonArray(jsonArray)
                    tweets.addAll(listOfNewTweetsRetrieved)
                    getMaxId()
                    adapter.notifyDataSetChanged()
                    swipeContainer.setRefreshing(false)
                }catch(e:JSONException){
                    Log.e(TAG,"JSON Exception $e")
                }
            }

        })
    }

    companion object{
        val TAG="TimeLineActivity"
        val REQUEST_CODE=10
    }
}