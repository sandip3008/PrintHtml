package com.rewaa.printhtml.plugin;

import static android.text.TextUtils.isEmpty;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.os.TransactionTooLargeException;
import android.util.AndroidException;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class echoes a string called from JavaScript.
 */
public class PrintHtml extends CordovaPlugin {

  private static final String TAG = PrintHtml.class.getSimpleName();
  Context context;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

    context = cordova.getActivity().getApplicationContext();
    if (action.equals("new_activity")) {
      String message = null;
      try {
        message = args.getString(0);
      } catch (JSONException e) {
        e.printStackTrace();
      }
      if(!isEmpty(message)) {
        this.openNewActivity(message);
      }else{
        Log.d(TAG, "execute: ERROR EMPTY DATA");
      }
      return true;
    }
    return false;
  }

  private void openNewActivity(String data) {
    try {
      Intent intent = new Intent(context, MainActivity.class);
      if (data.startsWith("http")) {
        intent.putExtra("url", data);
      } else {
        SharedPreferences sharedPreferences = context.getSharedPreferences("itemData",0);
        SharedPreferences.Editor myEdit = sharedPreferences.edit();
        myEdit.putString("items", data);
        myEdit.commit();
      }
      this.cordova.getActivity().startActivity(intent);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
