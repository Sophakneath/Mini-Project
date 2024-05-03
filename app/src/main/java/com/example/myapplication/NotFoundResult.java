package com.example.myapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
public class NotFoundResult extends Fragment {
    public NotFoundResult() {
        // Required empty public constructor
    }
    public static NotFoundResult newInstance(String param1, String param2) {
        NotFoundResult fragment = new NotFoundResult();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_not_found_result, container, false);
    }
}