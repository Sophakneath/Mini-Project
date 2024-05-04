package com.example.myapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.myapplication.utils.Product;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link UnsafeResult#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UnsafeResult extends Fragment {

    private static final String PRODUCT = "product";
    private Product product;
    TextView productName, allergen;
    public static UnsafeResult newInstance(String param1, String param2) {
        UnsafeResult fragment = new UnsafeResult();
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

        String allergenInfo = product.getAllergenList().get(0);
        for (int i = 1; i < product.getAllergenList().size() - 1; i++) {
            allergenInfo += ", " + product.getAllergenList().get(i);
        }
        allergenInfo += ", and " + product.getAllergenList().get(product.getAllergenList().size() - 1);

        View view = inflater.inflate(R.layout.fragment_unsafe_result, container, false);
        productName = view.findViewById(R.id.productName);
        allergen = view.findViewById(R.id.allergen);
        productName.setText(product.getEnglishName() + "\n" + product.getKoreanName());
        allergen.setText("Unsafe to consume as it contains \n" + allergenInfo);
        return view;
    }
}