package com.example.nayan.appanalysis2.fragment;

import android.provider.CallLog;
import android.widget.TextView;

import com.example.nayan.appanalysis2.base.CallViewFragment;
import com.example.nayan.appanalysis2.base.GetCursorTask;

import java.text.SimpleDateFormat;
import java.util.Date;

import me.everything.providers.android.calllog.Call;
import me.everything.providers.android.calllog.CallsProvider;
import me.everything.providers.core.Data;


/**
 * Created by sromku.
 */
public class CallsFragment extends CallViewFragment<Call> {
    SimpleDateFormat formatter = new SimpleDateFormat(
            "dd-MMM-yyyy HH:mm");
    private String unknown = "Unknown";

    private String[] mColumns = new String[]{
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE,
            CallLog.Calls.CACHED_NAME
    };

    @Override
    protected String getTitle() {
        return "Calls";
    }

    @Override
    protected void bindEntity(Call call, TextView title, TextView details, TextView duration, TextView date, TextView name) {
        title.setText("number: " + call.number);
        duration.setText("duration: " + call.duration);
        String dateString = formatter.format(new Date(Long
                .parseLong(String.valueOf(call.callDate))));
        date.setText("date: " + dateString);
        details.setText("type: " + call.type.toString());

        if (call.name == null || call.name == "null") {
            name.setText("name: " + unknown);
        } else {
            name.setText("name: " + call.name);
        }


    }


    @Override
    protected String[] getProjectionColumns() {
        return mColumns;
    }

    @Override
    protected GetCursorTask.DataFetcher<Call> getFetcher() {
        return new GetCursorTask.DataFetcher<Call>() {
            @Override
            public Data<Call> getData() {
                CallsProvider callsProvider = new CallsProvider(getApplicationContext());
                return callsProvider.getCalls();
            }
        };
    }


}
