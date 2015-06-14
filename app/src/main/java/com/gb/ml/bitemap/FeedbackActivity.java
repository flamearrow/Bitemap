package com.gb.ml.bitemap;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.gb.ml.bitemap.network.NetworkConstants;
import com.gb.ml.bitemap.network.VolleyNetworkAccessor;

import java.util.HashMap;
import java.util.Map;


public class FeedbackActivity extends BitemapActionBarActivity {

    private EditText mName, mEmail, mComments;

    private static final String TAG = "FeedbackActivity";

    private static final String SUCCESS = "success";

    private static final String EMAIL_FORMATTER = "[\\w._%+-]+@\\w+\\.\\w{2,4}";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.feedback);
        setContentView(R.layout.activity_feedback);
        mName = (EditText) findViewById(R.id.name);
        mEmail = (EditText) findViewById(R.id.email);
        mComments = (EditText) findViewById(R.id.comments);
    }

    private void clearFields() {
        mName.setText("");
        mEmail.setText("");
        mComments.setText("");
    }

    public void submit(View view) {
        if (!validateInput()) {
            Toast.makeText(this, "Please no empty fields and valid email address!", Toast.LENGTH_SHORT).show();
            return;
        }
        String name = mName.getText().toString();
        String email = mEmail.getText().toString();
        String comments = mComments.getText().toString();
        VolleyNetworkAccessor.getInstance(this).addToRequestQueue(new PostComments(name, email, comments,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        if (SUCCESS.equals(response)) {
                            Toast.makeText(FeedbackActivity.this, "Done!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(FeedbackActivity.this,
                                    "Something is wrong with server, please try again later",
                                    Toast.LENGTH_SHORT).show();
                        }
                        clearFields();
                    }
                }));
    }

    private boolean validateInput() {
        CharSequence name = mName.getText();
        CharSequence email = mEmail.getText();
        CharSequence comments = mComments.getText();
        return name.length() > 0 && email.length() > 0 && email.toString().matches(EMAIL_FORMATTER)
                && comments.length() > 0;
    }

    private class PostComments extends Request<String> {

        private String mName, mEmail, mComments;

        private Response.Listener mListener;

        public PostComments(String name, String email, String comments, Response.Listener listener) {
            super(Method.POST, NetworkConstants.POST_COMMENTS, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d(TAG, "Posting comments failed!");
                }
            });
            mName = name;
            mEmail = email;
            mComments = comments;
            mListener = listener;
        }

        @Override
        protected Map<String, String> getParams() throws AuthFailureError {
            Map<String, String> params = new HashMap<>();
            params.put(NetworkConstants.POST_PARAM_NAME, mName);
            params.put(NetworkConstants.POST_PARAM_EMAIL, mEmail);
            params.put(NetworkConstants.POST_PARAM_COMMENT, mComments);
            return params;
        }

        @Override
        protected Response<String> parseNetworkResponse(NetworkResponse response) {
            if (response.statusCode == 200) {
                Log.d(TAG, "comments successfully sent");
                return Response.success("success", HttpHeaderParser.parseCacheHeaders(response));
            } else {
                Log.d(TAG, "Comment isn't sent successfully");
                return Response.error(new VolleyError("Comment isn't sent successfully"));

            }
        }

        @Override
        protected void deliverResponse(String response) {
            mListener.onResponse(response);
        }
    }
}
