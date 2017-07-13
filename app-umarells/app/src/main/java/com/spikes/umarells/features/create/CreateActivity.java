/*
 * Copyright 2017.  Luca Rossi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *
 */

package com.spikes.umarells.features.create;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;

import com.spikes.jodatimeutils.JodaDatePickerDialog;
import com.spikes.umarells.R;
import com.spikes.umarells.shared.AppCompatActivityExt;

import butterknife.BindView;
import butterknife.OnClick;

public class CreateActivity extends AppCompatActivityExt {

    public static Intent getStartIntent(Context context){
        Intent startIntent = new Intent(context, CreateActivity.class);
        return startIntent;
    }

    @BindView(R.id.edit_name)
    TextInputEditText mEditName;
    @BindView(R.id.edit_description)
    TextInputEditText mEditDescription;
    @BindView(R.id.text_dates)
    AppCompatTextView mTextDates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
    }

    @OnClick(R.id.text_dates)
    void openDatePicker(){
        new JodaDatePickerDialog(CreateActivity.this, (datePicker, date) -> {
            mTextDates.setText(date.toDate().toString());
        }).show();
    }
}
