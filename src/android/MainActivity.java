package com.rewaa.printhtml.plugin;


import static android.text.TextUtils.isEmpty;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import capacitor.android.plugins.R;


public class MainActivity extends Activity {

  private static final String TAG = MainActivity.class.getSimpleName();
  WebView webView;
  ProgressBar progressBar;
  Boolean isPrinted = false;

  String url = "file:///android_asset/img/test.html";

//    final String filename= URLUtil.guessFileName(URLUtil.guessUrl(url));

  @SuppressLint("SetJavaScriptEnabled")
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String package_name = getApplication().getPackageName();
    setContentView(getApplication().getResources().getIdentifier("activity_webview", "layout", package_name));

//        getSupportActionBar().hide();

    webView = findViewById(R.id.web);
    progressBar = findViewById(R.id.progress);
    progressBar.setVisibility(View.GONE);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setSupportZoom(false);
    webView.getSettings().setDomStorageEnabled(true);
    webView.setWebViewClient(new myWebViewclient());
    String html = "";
    if (getIntent().hasExtra("url")) {
      String url = getIntent().getStringExtra("url");

      if (isEmpty(url))
        this.finish();

      webView.loadUrl(url);
    } else {
      SharedPreferences sharedPreferences = getSharedPreferences("itemData", 0);
      String items = sharedPreferences.getString("items", "");
      if (isEmpty(items)) {
        Log.e(TAG, "ERROR: EMPTY ITEMS");
      }
      try {
        html = URLEncoder.encode(items, "utf-8").replaceAll("\\+", " ");
      } catch (StackOverflowError | Exception e) {
        Log.d(TAG, "StackOverflowError: ");
        e.printStackTrace();
      }
      webView.loadData(html, "text/html; charset=utf-8", "UTF-8");
    }


    //  ==================== START HERE: THIS CODE BLOCK IS TO ENABLE FILE DOWNLOAD FROM THE WEB. YOU CAN COMMENT IT OUT IF YOUR APPLICATION DOES NOT REQUIRE FILE DOWNLOAD. IT WAS ADDED ON REQUEST ======//

//        webView.setDownloadListener(new DownloadListener() {
//            String fileName = MimeTypeMap.getFileExtensionFromUrl(url);
//            @Override
//            public void onDownloadStart(String url, String userAgent,
//                                        String contentDisposition, String mimetype,
//                                        long contentLength) {
//
//                DownloadManager.Request request = new DownloadManager.Request(
//                        Uri.parse(url));
//
//                request.allowScanningByMediaScanner();
//                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
//                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
//                DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
//                dm.enqueue(request);
//                Toast.makeText(getApplicationContext(), "Downloading File", //To notify the Client that the file is being downloaded
//                        Toast.LENGTH_LONG).show();
//
//            }
//        });
    //  ==================== END HERE: THIS CODE BLOCK IS TO ENABLE FILE DOWNLOAD FROM THE WEB. YOU CAN COMMENT IT OUT IF YOUR APPLICATION DOES NOT REQUIRE FILE DOWNLOAD. IT WAS ADDED ON REQUEST ======//


  }


  public class myWebViewclient extends WebViewClient {

    private void closeAndPrint(WebView view){
      try {
        super.onPageFinished(view, url);
        progressBar.setVisibility(View.GONE);
        if(!isPrinted)
          createWebPrintJob(view);
      }catch (Exception e){
        e.printStackTrace();
      }
  }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      view.loadUrl(url);
      return true;
    }

    @Override
    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
//      Toast.makeText(getApplicationContext(),
//        "WebView Error" + errorResponse.getReasonPhrase(),
//        Toast.LENGTH_SHORT).show();
      super.onReceivedHttpError(view, request, errorResponse);
      closeAndPrint(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
      super.onReceivedError(view, request, error);
      Log.e(TAG,"Error occured "+ "." +error.getDescription().toString());
      closeAndPrint(view);
//      Toast.makeText(getApplicationContext(),
//        "WebView onReceivedError" + error.toString(),
//        Toast.LENGTH_SHORT).show();
    }



    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//      view.loadUrl("about:blank");
      Log.e(TAG," Error occured while loading the web page at Url"+ failingUrl+"." +description);
      super.onReceivedError(view, errorCode, description, failingUrl);
//      Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_LONG).show();
      closeAndPrint(view);
//      webView.loadUrl("file:///android_asset/lost.html");
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
      super.onReceivedSslError(view, handler, error);
      progressBar.setVisibility(View.GONE);
      handler.cancel();
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      super.onPageStarted(view, url, favicon);
      progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      closeAndPrint(view);
    }
  }


  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {

    if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
      webView.goBack();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  public static class PrintDocumentAdapterWrapper extends PrintDocumentAdapter {
    String TAG = "PrintDocumentAdapterWrapper";
    private final PrintDocumentAdapter delegate;
    private final Activity activity;

    PrintDocumentAdapterWrapper(Activity activity, PrintDocumentAdapter adapter) {
      super();
      this.activity = activity;
      this.delegate = adapter;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
      delegate.onLayout(oldAttributes, newAttributes, cancellationSignal, callback, extras);
      Log.d(TAG, "onLayout");
    }

    @Override
    public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
      delegate.onWrite(pages, destination, cancellationSignal, callback);
      Log.d(TAG, "onWrite");
    }

    @Override
    public void onFinish() {
      delegate.onFinish();
      activity.finish();
      Log.d(TAG, "onFinish");
    }

  }

  //create a function to create the print job
  private void createWebPrintJob(WebView webView) {

    //create object of print manager in your device
    PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);

    //create object of print adapter
    PrintDocumentAdapterWrapper printAdapter = new PrintDocumentAdapterWrapper(MainActivity.this, webView.createPrintDocumentAdapter());

    //provide name to your newly generated pdf file
    String jobName = "Print Invoice";

    //open print dialog
    if (printManager != null) {
      printManager.print(jobName, printAdapter, new PrintAttributes.Builder().setMinMargins(new PrintAttributes.Margins(0, 0, 0, 0)).build());
      isPrinted = true;
    } else {
      Log.e("createWebPrintJob", "printManager null");
    //webView.loadData(String.format(Locale.US, htmlHead, fontSize, printFontSize) + "PrintManager is null" + htmlFooter, "text/html; charset=utf-8", "utf-8");

    }
  }


}
