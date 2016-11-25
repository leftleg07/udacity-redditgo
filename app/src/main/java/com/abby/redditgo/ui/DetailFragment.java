package com.abby.redditgo.ui;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.abby.redditgo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private static final String TAG = DetailFragment.class.getSimpleName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DETAIL_PARAM = "_arg_detail_param";

    // TODO: Rename and change types of parameters
    private Uri mUri;

    @BindView(R.id.swiperefresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    /**
     * A reference to the {@link WebView}.
     */
    @BindView(R.id.web_view)
    WebView mWebView;

    @BindView(R.id.web_progress_bar)
    ProgressBar progressBar;

    /**
     * This field stores the {@link PermissionRequest} from the web application until it is allowed
     * or denied by user.
     */
    private PermissionRequest mPermissionRequest;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment DetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DetailFragment newInstance(Uri param1) {
        DetailFragment fragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_DETAIL_PARAM, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUri = getArguments().getParcelable(ARG_DETAIL_PARAM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        // Here, we use #mWebChromeClient with implementation for handling PermissionRequests.
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setWebViewClient(mWebViewClient);
        configureWebSettings(mWebView.getSettings());

        Spanned spanned = null;
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(mUri.toString());
        } else {
            spanned = Html.fromHtml(mUri.toString(), Html.FROM_HTML_MODE_LEGACY);
        }
        mWebView.loadUrl(spanned.toString());

        /*
        * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
        * performs a swipe-to-refresh gesture.
        */
        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        updateOperation();
                    }
                }
        );

    }

    private void updateOperation() {
        mWebView.reload();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private static void configureWebSettings(WebSettings settings) {
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
    }

    /**
     * This {@link WebChromeClient} has implementation for handling {@link PermissionRequest}.
     */
    private WebChromeClient mWebChromeClient = new WebChromeClient() {

        // This method is called when the web content is requesting permission to access some
        // resources.
        @Override
        public void onPermissionRequest(PermissionRequest request) {
            Log.i(TAG, "onPermissionRequest");
            mPermissionRequest = request;
        }

        // This method is called when the permission request is canceled by the web content.
        @Override
        public void onPermissionRequestCanceled(PermissionRequest request) {
            Log.i(TAG, "onPermissionRequestCanceled");
            // We dismiss the prompt UI here as the request is no longer valid.
            mPermissionRequest = null;
        }

        @Override
        public boolean onConsoleMessage(@NonNull ConsoleMessage message) {
            switch (message.messageLevel()) {
                case TIP:
                    Log.v(TAG, message.message());
                    break;
                case LOG:
                    Log.i(TAG, message.message());
                    break;
                case WARNING:
                    Log.w(TAG, message.message());
                    break;
                case ERROR:
                    Log.e(TAG, message.message());
                    break;
                case DEBUG:
                    Log.d(TAG, message.message());
                    break;
            }
            return true;
        }

        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progressBar.setProgress(newProgress);
            if (newProgress >= 86) {
                progressBar.setVisibility(View.GONE);
            }
        }
    };

    private WebViewClient mWebViewClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
        }
    };

}
