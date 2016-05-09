package io.ipoli.android.quest.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Bus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ipoli.android.R;
import io.ipoli.android.app.events.EventSource;
import io.ipoli.android.app.utils.Time;
import io.ipoli.android.quest.data.Quest;
import io.ipoli.android.quest.events.CompleteUnscheduledQuestRequestEvent;
import io.ipoli.android.quest.events.EditQuestRequestEvent;
import io.ipoli.android.quest.events.MoveQuestToCalendarRequestEvent;
import io.ipoli.android.quest.events.ScheduleQuestRequestEvent;
import io.ipoli.android.quest.events.ShowQuestEvent;
import io.ipoli.android.quest.viewmodels.UnscheduledQuestViewModel;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/8/16.
 */
public class UnscheduledQuestsAdapter extends RecyclerView.Adapter<UnscheduledQuestsAdapter.ViewHolder> {

    private Context context;
    private List<UnscheduledQuestViewModel> viewModels;
    private final Bus eventBus;
    private final Time.RelativeTime relativeTime;

    public UnscheduledQuestsAdapter(Context context, List<UnscheduledQuestViewModel> viewModels, Bus eventBus, Time.RelativeTime relativeTime) {
        this.context = context;
        this.viewModels = viewModels;
        this.eventBus = eventBus;
        this.relativeTime = relativeTime;
    }

    @Override
    public UnscheduledQuestsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                  int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.unscheduled_quest_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final UnscheduledQuestViewModel vm = viewModels.get(position);
        Quest q = vm.getQuest();
        holder.itemView.setOnClickListener(view -> {
            if (relativeTime == Time.RelativeTime.PRESENT) {
                eventBus.post(new ShowQuestEvent(q, EventSource.CALENDAR_UNSCHEDULED_SECTION));
            } else if (relativeTime == Time.RelativeTime.FUTURE && !q.isHabit()) {
                eventBus.post(new EditQuestRequestEvent(q, EventSource.CALENDAR_UNSCHEDULED_SECTION));
            } else if (relativeTime == Time.RelativeTime.FUTURE && q.isHabit()) {
                Toast.makeText(holder.itemView.getContext(), R.string.cannot_edit_future_habits, Toast.LENGTH_SHORT).show();
            }

        });

        GradientDrawable drawable = (GradientDrawable) holder.indicator.getBackground();
        drawable.setColor(ContextCompat.getColor(context, vm.getContextColor()));

        if (vm.isStarted()) {
            Animation blinkAnimation = AnimationUtils.loadAnimation(context, R.anim.blink);
            holder.indicator.startAnimation(blinkAnimation);
        }

        holder.name.setText(vm.getName());
        holder.itemView.setOnLongClickListener(view -> {
            eventBus.post(new MoveQuestToCalendarRequestEvent(vm, holder.getAdapterPosition()));
            return true;
        });

        holder.check.setOnCheckedChangeListener(null);
        holder.check.setChecked(false);
        if (relativeTime == Time.RelativeTime.FUTURE && q.isHabit()) {
            holder.check.setClickable(false);
        } else {
            holder.check.setClickable(true);
            holder.check.setOnCheckedChangeListener((compoundButton, checked) -> {
                if (checked) {
                    eventBus.post(new CompleteUnscheduledQuestRequestEvent(vm));
                }
            });
        }

        holder.schedule.setOnClickListener(v -> {
            eventBus.post(new ScheduleQuestRequestEvent(vm));
        });
    }

    @Override
    public int getItemCount() {
        return viewModels.size();
    }

    public void addQuest(int position, UnscheduledQuestViewModel viewModel) {
        viewModels.add(position, viewModel);
        notifyItemInserted(position);
    }

    public void removeQuest(UnscheduledQuestViewModel viewModel) {
        int position = viewModels.indexOf(viewModel);
        if (viewModel.getRemainingCount() > 1) {
            viewModel.decreaseRemainingCount();
            notifyItemChanged(position);
            return;
        }
        viewModels.remove(viewModel);
        notifyItemRemoved(position);
    }

    public void updateQuests(List<UnscheduledQuestViewModel> viewModels) {
        this.viewModels = viewModels;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.quest_check)
        CheckBox check;

        @BindView(R.id.quest_text)
        TextView name;

        @BindView(R.id.quest_context_indicator)
        View indicator;

        @BindView(R.id.quest_schedule)
        View schedule;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }
}