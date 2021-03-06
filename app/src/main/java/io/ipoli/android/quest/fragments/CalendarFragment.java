package io.ipoli.android.quest.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.Constants;
import io.ipoli.android.MainActivity;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.events.CalendarDayChangedEvent;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.help.HelpDialog;
import io.ipoli.android.app.ui.FabMenuView;
import io.ipoli.android.app.ui.events.FabMenuTappedEvent;
import io.ipoli.android.app.ui.events.ToolbarCalendarTapEvent;
import io.ipoli.android.quest.activities.AgendaActivity;
import io.ipoli.android.quest.events.ScrollToTimeEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 4/29/16.
 */
public class CalendarFragment extends BaseFragment implements View.OnClickListener {

    public static final int MID_POSITION = 49;
    public static final int MAX_VISIBLE_DAYS = 100;

    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;

    @BindView(R.id.toolbar_expand_container)
    View toolbarExpandContainer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.calendar_pager)
    ViewPager calendarPager;

    @BindView(R.id.fab_menu)
    FabMenuView fabMenu;

    @Inject
    Bus eventBus;

    private FragmentStatePagerAdapter adapter;

    private LocalDate currentMidDate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        ButterKnife.bind(this, view);
        App.getAppComponent(getContext()).inject(this);

        ((MainActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((MainActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        ((MainActivity) getActivity()).actionBarDrawerToggle.syncState();

        toolbarExpandContainer.setOnClickListener(this);

        currentMidDate = new LocalDate();

        changeTitle(currentMidDate);

        fabMenu.addFabClickListener(name -> eventBus.post(new FabMenuTappedEvent(name, EventSource.CALENDAR)));

        adapter = createAdapter();

        calendarPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                LocalDate date = currentMidDate.plusDays(position - MID_POSITION);
                changeTitle(date);
                eventBus.post(new CalendarDayChangedEvent(date, CalendarDayChangedEvent.Source.SWIPE));
            }
        });

        calendarPager.setAdapter(adapter);
        calendarPager.setCurrentItem(MID_POSITION);

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today:
                eventBus.post(new CalendarDayChangedEvent(new LocalDate(), CalendarDayChangedEvent.Source.MENU));
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.calendar_menu, menu);
    }

    @Override
    protected boolean useOptionsMenu() {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        eventBus.register(this);
    }

    @Override
    public void onPause() {
        eventBus.unregister(this);
        super.onPause();
    }

    private void changeTitle(LocalDate date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getString(getToolbarText(date)), Locale.getDefault());
        toolbarTitle.setText(simpleDateFormat.format(date.toDate()));
    }

    private int getToolbarText(LocalDate date) {
        if (date.isEqual(new LocalDate().minusDays(1))) {
            return R.string.yesterday_calendar_format;
        }
        if (date.isEqual(new LocalDate())) {
            return R.string.today_calendar_format;
        }
        if (date.isEqual(new LocalDate().plusDays(1))) {
            return R.string.tomorrow_calendar_format;
        }
        return R.string.calendar_format;
    }

    @Subscribe
    public void onCurrentDayChanged(CalendarDayChangedEvent e) {
        if (e.source == CalendarDayChangedEvent.Source.SWIPE) {
            return;
        }

        currentMidDate = e.date;
        changeTitle(currentMidDate);
        adapter.notifyDataSetChanged();

        calendarPager.setCurrentItem(MID_POSITION, false);
        if (e.time != null) {
            eventBus.post(new ScrollToTimeEvent(e.time));
        }
    }

    private FragmentStatePagerAdapter createAdapter() {
        return new FragmentStatePagerAdapter(getChildFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                int plusDays = position - MID_POSITION;
                return DayViewFragment.newInstance(currentMidDate.plusDays(plusDays));
            }

            @Override
            public int getCount() {
                return MAX_VISIBLE_DAYS;
            }

            @Override
            public int getItemPosition(Object object) {
                return POSITION_NONE;
            }
        };
    }

    @Override
    protected void showHelpDialog() {
        HelpDialog.newInstance(R.layout.fragment_help_dialog_calendar, R.string.help_dialog_calendar_title, "calendar").show(getActivity().getSupportFragmentManager());
    }

    @Override
    public void onClick(View v) {
        LocalDate currentDate = currentMidDate.plusDays(calendarPager.getCurrentItem() - MID_POSITION);
        Intent i = new Intent(getContext(), AgendaActivity.class);
        i.putExtra(Constants.CURRENT_SELECTED_DAY_EXTRA_KEY, currentDate.toDate().getTime());
        startActivity(i);
        getActivity().overridePendingTransition(R.anim.slide_in_top, android.R.anim.fade_out);
        eventBus.post(new ToolbarCalendarTapEvent());
    }
}