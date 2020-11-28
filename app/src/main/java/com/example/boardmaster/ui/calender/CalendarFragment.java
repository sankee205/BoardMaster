package com.example.boardmaster.ui.calender;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.example.boardmaster.CurrentUser;
import com.example.boardmaster.R;
import com.example.boardmaster.User;
import com.example.boardmaster.calendardayview.CalendarDayView;
import com.example.boardmaster.calendardayview.CdvDecorationDefault;
import com.example.boardmaster.calendardayview.EventView;
import com.example.boardmaster.calendardayview.IEvent;
import com.example.boardmaster.game.Game;
import com.example.boardmaster.game.JoinBottomDialogFragment;
import com.example.boardmaster.retrofit.ApiClient;
import com.example.boardmaster.retrofit.JsonPlaceHolderApi;

import com.github.sundeepk.compactcalendarview.CompactCalendarView;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import static android.content.ContentValues.TAG;


/**
 * this is the calenderfragment, it has a normal calender and a daycalender by hour.
 */
public class CalendarFragment extends Fragment {
    private  CompactCalendarView compactCalendarView;

    private JsonPlaceHolderApi api = ApiClient.getClient().create(JsonPlaceHolderApi.class);
    private ArrayList<Game> items = new ArrayList<>();
    private SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM- yyyy", Locale.getDefault());
    TextView calenderMonth, calenderDate;

    ImageView left_button, right_button;

    CalendarDayView dayView;

    ArrayList<IEvent> events;
    ArrayList<Event> eventArrayList;
    List<Game> gameList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calenderMonth = view.findViewById(R.id.calender_month);
        left_button = view.findViewById(R.id.left_calender_button);
        right_button = view.findViewById(R.id.right_calender_button);
        calenderDate = view.findViewById(R.id.game_calender_date);

        compactCalendarView = (CompactCalendarView) view.findViewById(R.id.compactcalendar_view);
        compactCalendarView.setFirstDayOfWeek(Calendar.MONDAY);
        calenderMonth.setText(dateFormatMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));
        setItemsList();
        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                List<com.github.sundeepk.compactcalendarview.domain.Event> events = compactCalendarView.getEvents(dateClicked);
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                String strDate = dateFormat.format(dateClicked.getTime());
                calenderDate.setText(strDate);
                setDayCalendar(dateClicked);
                dayView.setVisibility(View.VISIBLE);
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

        events = new ArrayList<>();

        dayView = (CalendarDayView) view.findViewById(R.id.dayView);
        dayView.setLimitTime(9, 22);
        dayView.setVisibility(View.GONE);
        

        ((CdvDecorationDefault) (dayView.getDecoration())).setOnEventClickListener(
                new EventView.OnEventClickListener() {
                    @Override
                    public void onEventClick(EventView view, IEvent data) {
                        Log.e("TAG", "onEventClick:" + data.getName());
                    }

                    @RequiresApi(api = Build.VERSION_CODES.P)
                    @Override
                    public void onEventViewClick(View view, EventView eventView, IEvent data) {
                        String name = data.getName();
                        Log.e("TAG", "onEventViewClick:" + name);
                        Game game = null;
                        for(int i = 0; i < gameList.size(); i ++){
                            if(gameList.get(i).getGameName().contains(name)){
                                game = gameList.get(i);
                            }
                        }
                        List<User> playerlist = game.getPlayers();
                        String players = "";
                        for(int i = 0; i< playerlist.size(); i++){
                            User user = playerlist.get(i);
                            players += user.getUsername()+ " ";

                        }
                        JoinBottomDialogFragment fragment = new JoinBottomDialogFragment();
                        fragment.setParameters(game.getId(),game.getGameName(),game.getTitle(), game.getDescription(), players, game.getDate(), game.getTime(), game.getProfileImages().get(0).getId());
                        fragment.show(getActivity().getSupportFragmentManager(), "PurchaseBottomDialogFragment");


                    }
                });



        return view;
    }


    /**
     * gets the list of games
     */
    public void setItemsList() {
        if(CurrentUser.getInstance().isUserLogedIn()){
            Call<ArrayList<Game>> call = api.listUsersGames(CurrentUser.getInstance().getToken());

            call.enqueue(new Callback<ArrayList<Game>>(){
                @Override
                public void onResponse(Call<ArrayList<Game>> call, Response<ArrayList<Game>> response){
                    if(!response.isSuccessful()){
                        System.out.println("code:"+response.code());
                        return;
                    }
                    items = response.body();
                    addEvent(items);

                }

                @Override
                public void onFailure(Call<ArrayList<Game>> call,Throwable t){
                }
            });
        }

    }


    /**
     * creates event for each game
     * @param games
     */
    public void addEvent(List<Game> games) {
        gameList = games;
        if(!games.isEmpty()){
            for (int i = 0; i < games.size(); i++) {
                Game game = games.get(i);

                String date = game.getDate();
                String time = game.getTime();
                String[] timearray = time.split(":");
                String mydate = date + " " + time + ":00";
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                Date somedate = null;
                try {
                    somedate = sdf.parse(mydate);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                long millis = somedate.getTime();

                String title = game.getTitle();
                String gameName = game.getGameName();
                String data = gameName + "," + title + "," + time;

                com.github.sundeepk.compactcalendarview.domain.Event ev1 = new com.github.sundeepk.compactcalendarview.domain.Event(Color.GREEN, millis, data);
                compactCalendarView.addEvent(ev1);

            }
        }


    }

    /**
     * sets the daycalender by a date
     * @param date
     */
    public void setDayCalendar(Date date){
        ArrayList<IEvent> newEvents = new ArrayList<>();
        ArrayList<Event> someEventList = new ArrayList<>();
        List<com.github.sundeepk.compactcalendarview.domain.Event> eventList = compactCalendarView.getEvents(date);

        for(int i=0; i< eventList.size(); i++){
            com.github.sundeepk.compactcalendarview.domain.Event someEvent = eventList.get(i);
            String data = someEvent.getData().toString();

            String[] dataArray = data.split(",");
            String[] time = dataArray[2].split(":");
            int hour = Integer.parseInt(time[0]);
            int min = Integer.parseInt(time[1]);
            String name = dataArray[0];


            int eventColor = getResources().getColor(R.color.orange);
            Calendar timeStart = Calendar.getInstance();
            timeStart.set(Calendar.HOUR_OF_DAY,hour);
            timeStart.set(Calendar.MINUTE,min);

            Calendar timeEnd = (Calendar) timeStart.clone();
            timeEnd.add(Calendar.HOUR_OF_DAY, 1);
            timeEnd.add(Calendar.MINUTE,30);
            Event event = new Event(i, timeStart, timeEnd, name,  eventColor);
            newEvents.add(event);
            someEventList.add(event);

        }
        events = newEvents;

        eventArrayList = someEventList;
        dayView.setEvents(events);
    }
}