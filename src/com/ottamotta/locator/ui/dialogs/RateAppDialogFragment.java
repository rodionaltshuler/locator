package com.ottamotta.locator.ui.dialogs;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ottamotta.locator.R;
import com.ottamotta.locator.appRater.apprater.AppRater;

public class RateAppDialogFragment extends InfoDialogFragment {

    protected static final String NEGATIVE_TEXT_EXTRA = "negative";
    protected static final String NEGATIVE_TEXT_STRING_EXTRA = "negative_string";

    protected int negativeTextResourceId;
    private String negativeButtonTitle;

    private Button negativeButton;
    private SharedPreferences.Editor editor;

    public static RateAppDialogFragment newInstance(int questionResourceId, int positiveResourceId, int negativeResourceId) {
        RateAppDialogFragment dialogFragment = new RateAppDialogFragment();
        Bundle args = new Bundle();
        args.putInt(MESSAGE_EXTRA, questionResourceId);
        args.putInt(POSITIVE_TEXT_EXTRA, positiveResourceId);
        args.putInt(NEGATIVE_TEXT_EXTRA, negativeResourceId);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    public static RateAppDialogFragment newInstance(String title, String message, String positiveButtonTitle, String negativeButtonString) {
        RateAppDialogFragment dialogFragment = new RateAppDialogFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE_STRING_EXTRA, message);
        args.putString(POSITIVE_STRING_TEXT_EXTRA, positiveButtonTitle);
        args.putString(TITLE_STRING, title);
        args.putString(NEGATIVE_TEXT_STRING_EXTRA, negativeButtonString);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(NEGATIVE_TEXT_EXTRA)) {
            negativeTextResourceId = getArguments().getInt(NEGATIVE_TEXT_EXTRA);
        }
        else {
            negativeButtonTitle = getArguments().getString(NEGATIVE_TEXT_STRING_EXTRA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);

        negativeButton = (Button) root.findViewById(R.id.negative);
        if (negativeTextResourceId != 0) {
            negativeButton.setText(negativeTextResourceId);
        }
        else if (negativeButtonTitle != null) {
            negativeButton.setText(negativeButtonTitle);
        }

        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNegativeButtonClick();
            }
        });

        getPositiveButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPositiveButtonClick();
            }
        });
        return root;
    }

    protected View getNegativeButton() {
        return negativeButton;
    }

    @Override
    protected int getDefaultResultCodeOnDismiss() {
        return Activity.RESULT_OK;
    }

    @Override
    protected View getView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.rate_dialog_fragment, container, false);
    }

    public void setEditor(SharedPreferences.Editor editor) {
        this.editor = editor;
    }

    protected void onNegativeButtonClick() {
        AppRater.onRateLaterPressed(getActivity());
        dismiss();
    }

    protected void onPositiveButtonClick() {
        AppRater.rateNow(getActivity());
        AppRater.onRateNowPressed(getActivity());
        dismiss();
    }
}
