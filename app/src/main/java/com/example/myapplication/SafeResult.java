package com.example.myapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.utils.Product;

public class SafeResult extends Fragment {
    private static final String PRODUCT = "product";
    private static final String KOREAN_NAME = "koreanName";
    private Product product;
    TextView productName;
    public SafeResult() {
        // Required empty public constructor
    }

    public static SafeResult newInstance(String param1, String param2) {
        SafeResult fragment = new SafeResult();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = getArguments().getParcelable(PRODUCT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_safe_result, container, false);
        productName = view.findViewById(R.id.name);
        productName.setText(product.getEnglishName() + "\n" + product.getKoreanName());
        return view;
    }
}