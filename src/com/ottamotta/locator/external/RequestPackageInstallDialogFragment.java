package com.ottamotta.locator.external;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ottamotta.locator.R;
import com.ottamotta.locator.ui.dialogs.RateAppDialogFragment;

public class RequestPackageInstallDialogFragment extends RateAppDialogFragment {

    private String packageName;
    private String appName;

    private final static String PACKAGE_NAME_EXTRA = "packageName";
    private final static String APP_NAME_EXTRA = "appName";

    public static RequestPackageInstallDialogFragment newInstance(String title, String message, String positive, String negative, String packageName, String appName) {
        RequestPackageInstallDialogFragment dialogFragment = new RequestPackageInstallDialogFragment();
        Bundle args = new Bundle();
        args.putString(PACKAGE_NAME_EXTRA, packageName);
        args.putString(APP_NAME_EXTRA, appName);

        args.putString(TITLE_STRING, title);
        args.putString(MESSAGE_STRING_EXTRA, message);
        args.putString(POSITIVE_STRING_TEXT_EXTRA, positive);
        args.putString(NEGATIVE_TEXT_STRING_EXTRA, negative);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        packageName = getArguments().getString(PACKAGE_NAME_EXTRA);
        appName = getArguments().getString(APP_NAME_EXTRA);
    }

    @Override
    protected View getView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.request_install_dialog_fragment, container, false);
    }

    @Override
    protected void onPositiveButtonClick() {
        startActivity(new Intent(Intent.ACTION_VIEW, getMarketUri()));
        dismiss();
    }

    @Override
    protected void onNegativeButtonClick() {
        dismiss();
    }

    private Uri getMarketUri() {
        return Uri.parse("market://details?id=" + packageName);
    }

}
