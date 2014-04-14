package com.ottamotta.locator.ui.dialogs;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.ottamotta.locator.R;

public class InfoDialogFragment extends SherlockDialogFragment {

    protected static final String MESSAGE_EXTRA = "question";
    protected static final String POSITIVE_TEXT_EXTRA = "positive";

    protected static final String MESSAGE_STRING_EXTRA = "questionString";
    protected static final String POSITIVE_STRING_TEXT_EXTRA = "positiveString";
    protected static final String TITLE_STRING = "titleString";

    protected int messageResourceId;
    protected int positiveTextResourceId;

    protected String message;
    protected String title;
    protected String positiveButtonTitle;

    private int resultCodeOnDismiss;

    private Button btnPositiveButton;

    public static InfoDialogFragment newInstance(int questionResourceId, int positiveResourceId) {
        InfoDialogFragment dialogFragment = new InfoDialogFragment();
        Bundle args = new Bundle();
        args.putInt(MESSAGE_EXTRA, questionResourceId);
        args.putInt(POSITIVE_TEXT_EXTRA, positiveResourceId);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    public static InfoDialogFragment newInstance(String title, String message, String positiveButtonTitle) {
        InfoDialogFragment dialogFragment = new InfoDialogFragment();
        Bundle args = new Bundle();
        args.putString(MESSAGE_STRING_EXTRA, message);
        args.putString(POSITIVE_STRING_TEXT_EXTRA, positiveButtonTitle);
        args.putString(TITLE_STRING, title);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(MESSAGE_EXTRA)) {
            messageResourceId = getArguments().getInt(MESSAGE_EXTRA);
            positiveTextResourceId = getArguments().getInt(POSITIVE_TEXT_EXTRA);
        }
        else {
            message = getArguments().getString(MESSAGE_STRING_EXTRA);
            positiveButtonTitle = getArguments().getString(POSITIVE_STRING_TEXT_EXTRA);
            title = getArguments().getString(TITLE_STRING);
        }
        setResultCodeOnDismiss(getDefaultResultCodeOnDismiss());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = getView(inflater, container);
        TextView tvDialogTitle = (TextView) root.findViewById(R.id.title);
        btnPositiveButton = (Button) root.findViewById(R.id.positive);
        TextView tvMessage = (TextView) root.findViewById(R.id.message);

        if (positiveTextResourceId != 0) {
            btnPositiveButton.setText(positiveTextResourceId);
            tvMessage.setText(messageResourceId);
        }
        else {
            btnPositiveButton.setText(positiveButtonTitle);
            tvMessage.setText(message);
            tvDialogTitle.setText(title);
        }

        btnPositiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResultCodeOnDismiss(Activity.RESULT_OK);
                dismiss();
            }
        });
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return root;
    }

    protected View getPositiveButton() {
        return btnPositiveButton;
    }

    protected int getDefaultResultCodeOnDismiss() {
        return Activity.RESULT_OK;
    }

    protected int getResultCodeOnDismiss() {
        return resultCodeOnDismiss;
    }

    protected void setResultCodeOnDismiss(int resultCodeOnDismiss) {
        this.resultCodeOnDismiss = resultCodeOnDismiss;
    }

    protected View getView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.info_dialog_fragment, container, false);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), getResultCodeOnDismiss(), getActivity().getIntent());
        }
        else {
            onActivityResult(getTargetRequestCode(), getResultCodeOnDismiss(), getActivity().getIntent());
        }
    }
}
