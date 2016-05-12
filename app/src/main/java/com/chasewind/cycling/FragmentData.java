package com.chasewind.cycling;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by yyerg on 2016/5/13.
 */
public class FragmentData extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static FragmentData newInstance(int sectionNumber) {
        FragmentData fragment = new FragmentData();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentData() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_data, container, false);

        return rootView;
    }
}
