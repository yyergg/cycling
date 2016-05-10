package com.chasewind.cycling;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

/**
 * Created by yyerg on 2016/5/4.
 */
public class FragmentConnect extends Fragment {
    private static final String ARG_SECTION_NUMBER = "section_number";



    public static FragmentConnect newInstance(int sectionNumber) {
        FragmentConnect fragment = new FragmentConnect();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public FragmentConnect() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_connect, container, false);
        return rootView;
    }
}
