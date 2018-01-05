package com.example.nayan.appanalysis2.fragment;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.nayan.appanalysis2.R;
import com.example.nayan.appanalysis2.base.GetCursorTask;
import com.example.nayan.appanalysis2.base.RecycleViewCursorFragment;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.everything.providers.android.telephony.Sms;
import me.everything.providers.android.telephony.TelephonyProvider;
import me.everything.providers.core.Data;

public class SmsesFragment extends RecycleViewCursorFragment<Sms> {
    SimpleDateFormat formatter = new SimpleDateFormat(
            "dd-MMM-yyyy HH:mm");

    @Override
    protected String getTitle() {
        return "SMS(es)";
    }


    @Override
    protected void bindEntity(Sms sms, TextView title, final TextView details, TextView date) {
        String receiveDate = formatter.format(new Date(Long
                .parseLong(String.valueOf(sms.receivedDate))));
        title.setText(sms.address);
        details.setText(sms.body);
        date.setText(receiveDate);
        details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogShow(getContext(), details.getText().toString());
            }
        });
    }

    @Override
    protected GetCursorTask.DataFetcher<Sms> getFetcher() {
        return new GetCursorTask.DataFetcher<Sms>() {
            @Override
            public Data<Sms> getData() {
                TelephonyProvider provider = new TelephonyProvider(getApplicationContext());
                return provider.getSms(TelephonyProvider.Filter.ALL);
            }
        };
    }

    private void dialogShow(Context context, String s) {
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dia_full_message);
        TextView fullMessage = (TextView) dialog.findViewById(R.id.fullMessage);
        fullMessage.setText(s);
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        dialog.show();
    }
}
