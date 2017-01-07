package io.ipoli.android.quest.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.ipoli.android.R;
import io.ipoli.android.app.App;
import io.ipoli.android.app.BaseFragment;
import io.ipoli.android.app.ui.CategoryView;
import io.ipoli.android.app.utils.KeyboardUtils;
import io.ipoli.android.quest.data.Category;
import io.ipoli.android.quest.events.NewQuestCategoryChangedEvent;

/**
 * Created by Venelin Valkov <venelin@curiousily.com>
 * on 1/7/17.
 */

public class QuestNameFragment extends BaseFragment implements CategoryView.OnCategoryChangedListener {

    @Inject
    Bus eventBus;

    @BindView(R.id.root_container)
    ViewGroup rootContainer;

    @BindView(R.id.quest_name)
    TextInputEditText name;

    @BindView(R.id.quest_category)
    CategoryView category;

    private Unbinder unbinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        App.getAppComponent(getContext()).inject(this);
        View view = inflater.inflate(R.layout.fragment_wizard_quest_name, container, false);
        unbinder = ButterKnife.bind(this, view);
        category.addCategoryChangedListener(this);
        KeyboardUtils.showKeyboardForced(getContext());
        return view;
    }

    @Override
    public void onDestroyView() {
        category.removeCategoryChangedListener(this);
        unbinder.unbind();
        super.onDestroyView();
    }

    @Override
    protected boolean useOptionsMenu() {
        return false;
    }

    @Override
    public void onCategoryChanged(Category category) {
        eventBus.post(new NewQuestCategoryChangedEvent(category));
    }
}