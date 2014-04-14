package com.ottamotta.locator.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.ottamotta.locator.R;

public class MessageDialogFragment extends SherlockDialogFragment {

    protected static final String ARG_TITLE = "title";
    protected static final String ARG_MESSAGE = "message";
    protected static final String ARG_SCROLLABLE = "srcollable";

    protected String message;
    protected String title;
    protected boolean scrollable;
    protected View rootView;
    protected int customLayoutResourceId;

    public static MessageDialogFragment newInstance(String message, String title) {
        MessageDialogFragment dialogFragment = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_TITLE, title);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    private static MessageDialogFragment newInstanceScrollable(String message, String title) {
        MessageDialogFragment dialogFragment = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);
        args.putString(ARG_TITLE, title);
        args.putBoolean(ARG_SCROLLABLE, true);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        message = getArguments().getString(ARG_MESSAGE);
        title = getArguments().getString(ARG_TITLE);
        scrollable = getArguments().containsKey(ARG_SCROLLABLE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = getDialogView(inflater, container);
        initMessage();
        Button buttonClose = (Button) rootView.findViewById(R.id.button_close);
        buttonClose.setOnClickListener(getOnCloseListener());
        return rootView;
    }

    public int getCustomLayoutResourceId() {
        return customLayoutResourceId;
    }

    public void setCustomLayoutResourceId(int customLayoutResourceId) {
        this.customLayoutResourceId = customLayoutResourceId;
    }

    protected View.OnClickListener getOnCloseListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        };
    }

    protected void initMessage() {
        TextView messageView = (TextView) rootView.findViewById(R.id.message);
        messageView.setText(message);

        TextView titleView = (TextView) rootView.findViewById(R.id.title);
        if (title == null) {
            titleView.setVisibility(View.GONE);
        } else {
            titleView.setText(title);
        }

    }

    protected View getDialogView(LayoutInflater inflater, ViewGroup container) {
        int layoutResource;
        if (customLayoutResourceId == 0) {
            layoutResource = scrollable ? R.layout.dialog_fragment_scrollable : R.layout.dialog_fragment;
        } else {
            layoutResource = customLayoutResourceId;
        }
        return inflater.inflate(layoutResource, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

}
