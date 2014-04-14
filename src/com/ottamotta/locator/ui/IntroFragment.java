package com.ottamotta.locator.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;
import com.ottamotta.locator.R;

public class IntroFragment extends SherlockFragment {

    public static IntroFragment newInstance() {
        IntroFragment fragment = new IntroFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root;
        root = inflater.inflate(R.layout.intro_fragment_2, null);
        View buttonGo = root.findViewById(R.id.go_button);
        buttonGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof PaneHost) {
                    ((LocatorContactsActivity) getActivity()).openPane();
                }
            }
        });
        return root;
    }

}
