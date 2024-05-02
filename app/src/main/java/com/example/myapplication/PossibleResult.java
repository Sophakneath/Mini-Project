package com.example.myapplication;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.myapplication.utils.Recognition;

import java.util.List;

public class PossibleResult extends Fragment {

    private static final String PRODUCTS = "products";
    private List<Recognition> products;
    TextView pName1, pName2, pName3;
    FrameLayout pFrame1, pFrame2, pFrame3;
    ImageView pIcon1, pIcon2, pIcon3;

    public PossibleResult() {
        // Required empty public constructor
    }

    public static PossibleResult newInstance(String param1, String param2) {
        PossibleResult fragment = new PossibleResult();
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
            products = getArguments().getParcelableArrayList(PRODUCTS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_possible_result, container, false);
        pName1 = view.findViewById(R.id.pName1);
        pName2 = view.findViewById(R.id.pName2);
        pName3 = view.findViewById(R.id.pName3);
        pFrame1 = view.findViewById(R.id.pFrame1);
        pFrame2 = view.findViewById(R.id.pFrame2);
        pFrame3 = view.findViewById(R.id.pFrame3);
        pIcon1 = view.findViewById(R.id.pIcon1);
        pIcon2 = view.findViewById(R.id.pIcon2);
        pIcon3 = view.findViewById(R.id.pIcon3);

        updateUI(pName1, pFrame1, pIcon1, products.get(0));
        updateUI(pName2, pFrame2, pIcon2, products.get(1));
        updateUI(pName3, pFrame3, pIcon3, products.get(2));
        return view;
    }

    public int dpToPixels(int dp) {
        float scale = PossibleResult.this.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private void updateUI(TextView name, FrameLayout layout, ImageView icon, Recognition recognition) {
        name.setText(recognition.getEnglishName() + "\n" + recognition.getKoreanName());
        FrameLayout.LayoutParams layoutParams;
        if (recognition.getSafe()) {
            layout.setBackgroundResource(R.drawable.green_trans_round_safe_stroke);
            icon.setImageResource(R.drawable.check);
        }else {
            layout.setBackgroundResource(R.drawable.red_trans_round_unsafe_stroke);
            icon.setImageResource(R.drawable.close);
        }
    }
}