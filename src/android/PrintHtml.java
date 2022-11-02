package com.rewaa.printhtml.plugin;

import android.content.Context;
import android.content.Intent;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class echoes a string called from JavaScript.
 */
public class PrintHtml extends CordovaPlugin {

  Context context;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

    context = cordova.getActivity().getApplicationContext();
    if (action.equals("new_activity")) {
      String message = args.getString(0);
      this.openNewActivity(message);
      return true;
    }
    return false;
  }

  private void openNewActivity(String data) {

    Intent intent = new Intent(context, MainActivity.class);
    if (data.startsWith("http")) {
      intent.putExtra("url", data);
    } else {
      intent.putExtra("data", data);
    }
    this.cordova.getActivity().startActivity(intent);
  }

}
