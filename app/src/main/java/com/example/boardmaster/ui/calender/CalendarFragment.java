package com.example.boardmaster.ui.calender;

import android.app.ActionBar;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.boardmaster.R;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;


public class CalendarFragment extends Fragment {

    private SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM- yyyy", Locale.getDefault());
    TextView calenderMonth;

    ImageView left_button, right_button;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calenderMonth = view.findViewById(R.id.calender_month);
        left_button = view.findViewById(R.id.left_calender_button);
        right_button = view.findViewById(R.id.right_calender_button);

        final CompactCalendarView compactCalendarView = (CompactCalendarView) view.findViewById(R.id.compactcalendar_view);
        // Set first day of week to Monday, defaults to Monday so calling setFirstDayOfWeek is not necessary
        // Use constants provided by Java Calendar class
        compactCalendarView.setFirstDayOfWeek(Calendar.MONDAY);

        // Add event 1 on Sun, 07 Jun 2015 18:20:51 GMT
        Event ev1 = new Event(Color.GREEN, 1433701251000L, "Some extra data that I want to store.");
        compactCalendarView.addEvent(ev1);

        // Added event 2 GMT: Sun, 07 Jun 2015 19:10:51 GMT
        Event ev2 = new Event(Color.GREEN, 1433704251000L);
        compactCalendarView.addEvent(ev2);

        // Query for events on Sun, 07 Jun 2015 GMT.
        // Time is not relevant when querying for events, since events are returned by day.
        // So you can pass in any arbitary DateTime and you will receive all events for that day.
        List<Event> events = compactCalendarView.getEvents(1433701251000L); // can also take a Date object

        calenderMonth.setText(dateFormatMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));

        // events has size 2 with the 2 events inserted previously
        Log.d(TAG, "Events: " + events);

        // define a listener to receive callbacks when certain events happen.
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                List<Event> events = compactCalendarView.getEvents(dateClicked);
                Log.d(TAG, "Day was clicked: " + dateClicked + " with events " + events);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                Log.d(TAG, "Month was scrolled to: " + firstDayOfNewMonth);
                calenderMonth.setText(dateFormatMonth.format(firstDayOfNewMonth));
            }
        });

        left_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compactCalendarView.scrollLeft();
            }
        });

        right_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                compactCalendarView.scrollRight();
            }
        });

        return view;
    }


}