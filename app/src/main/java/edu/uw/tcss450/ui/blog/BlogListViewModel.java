package edu.uw.tcss450.ui.blog;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

import edu.uw.tcss450.R;
import edu.uw.tcss450.ui.model.UserInfoViewModel;

public class BlogListViewModel extends AndroidViewModel {

    private MutableLiveData<List<BlogPost>> mBlogList;

    public BlogListViewModel(@NonNull Application application) {
        super(application);
        mBlogList = new MutableLiveData<>();
        mBlogList.setValue(new ArrayList<>());
    }

    public void addBlogListObserver(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<? super List<BlogPost>> observer) {
        mBlogList.observe(owner, observer);
    }

    private void handleError(final VolleyError error) {
        //you should add much better error handling in a production release.
        //i.e. YOUR PROJECT
        Log.e("CONNECTION ERROR", error.getLocalizedMessage()); throw new IllegalStateException(error.getMessage());
    }

    private void handleResult(final JSONObject result) {
        IntFunction<String> getString =
                getApplication().getResources()::getString;
        try {
            JSONObject root = result;
            if (root.has(getString.apply(R.string.keys_json_blogs_response))) {
                JSONObject response =
                        root.getJSONObject(getString.apply(
                                R.string.keys_json_blogs_response));
                if (response.has(getString.apply(R.string.keys_json_blogs_data))) {
                    JSONArray data = response.getJSONArray(
                            getString.apply(R.string.keys_json_blogs_data));
                    for(int i = 0; i < data.length(); i++) {
                        JSONObject jsonBlog = data.getJSONObject(i);
                        BlogPost post = new BlogPost.Builder(
                                jsonBlog.getString(
                                        getString.apply(
                                                R.string.keys_json_blogs_pubdate)),
                                jsonBlog.getString(
                                        getString.apply(
                                                R.string.keys_json_blogs_title)))
                                .addTeaser(jsonBlog.getString(
                                        getString.apply(
                                                R.string.keys_json_blogs_teaser)))
                                .addUrl(jsonBlog.getString(
                                        getString.apply(
                                                R.string.keys_json_blogs_url)))
                                .build();
                        if (!mBlogList.getValue().contains(post)) {
                            mBlogList.getValue().add(post); }
                    }
                } else {
                    Log.e("ERROR!", "No data array");
                }
            } else {
                Log.e("ERROR!", "No response");
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("ERROR!", e.getMessage());
        }
        mBlogList.setValue(mBlogList.getValue());
    }

    public void connectGet() {
        String url =
                "https://cfb3-tcss450-labs-2021sp.herokuapp.com/phish/blog/get";
        Request request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null, //no body for this get request
                this::handleResult,
                this::handleError) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                // add headers <key,value>
//                headers.put("Authorization", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJl" +
//                        "bWFpbCI6ImNmYjMxQGZha2UuZW1haWwuY29tIiwibWVtYmVyaWQiOjMsImlhdCI6MTY" +
//                        "xODI2ODY2OSwiZXhwIjoxNjIzNDUyNjY5fQ.Y5t-1ibUMChZPe9eiavwCA3XbHhGdiM" +
//                        "NEdpSmCxI1Ow");
                headers.put("Authorization", UserInfoViewModel.getmJwt());
                Log.d("Check JWT",UserInfoViewModel.getmJwt());
                return headers;
            } };
        request.setRetryPolicy(new DefaultRetryPolicy(
                10_000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        //Instantiate the RequestQueue and add the request to the queue
        Volley.newRequestQueue(getApplication().getApplicationContext())
                .add(request);
    }


}
