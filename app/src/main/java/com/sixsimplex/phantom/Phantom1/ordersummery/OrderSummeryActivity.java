package com.sixsimplex.phantom.Phantom1.ordersummery;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.sixsimplex.phantom.R;

import java.util.Objects;

public class OrderSummeryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summery);
        Objects.requireNonNull(getSupportActionBar()).hide();
    }
}