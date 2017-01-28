package io.github.droidkaigi.confsched2017.view.fragment;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Locale;

import javax.inject.Inject;

import io.github.droidkaigi.confsched2017.databinding.FragmentSessionDetailBinding;
import io.github.droidkaigi.confsched2017.repository.sessions.SessionsRepository;
import io.github.droidkaigi.confsched2017.viewmodel.SessionDetailViewModel;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class SessionDetailFragment extends BaseFragment implements SessionDetailViewModel.Callback {

    private static final String TAG = SessionDetailFragment.class.getSimpleName();

    private static final String ARG_SESSION_ID = "session_id";

    @Inject
    SessionsRepository repository;

    @Inject
    SessionDetailViewModel viewModel;

    @Inject
    CompositeDisposable compositeDisposable;

    private FragmentSessionDetailBinding binding;

    public static SessionDetailFragment create(int sessionId) {
        SessionDetailFragment fragment = new SessionDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SESSION_ID, sessionId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final int sessionId = getArguments().getInt(ARG_SESSION_ID);
        final String languageId = Locale.getDefault().getLanguage().toLowerCase();
        Disposable disposable = repository.find(sessionId, languageId)
                .subscribe(
                        session -> {
                            viewModel.setSession(session);
                            initTheme();
                        },
                        throwable -> Log.e(TAG, "Failed to find session.", throwable)
                );
        compositeDisposable.add(disposable);
    }

    @Override
    public void onStop() {
        super.onStop();
        compositeDisposable.dispose();
    }

    private void initTheme() {
        Activity activity = getActivity();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Change theme by topic
            activity.setTheme(viewModel.getTopicThemeResId());

            ActivityManager.TaskDescription taskDescription =
                    new ActivityManager.TaskDescription(viewModel.getSessionTitle(), null,
                            ContextCompat.getColor(activity, viewModel.getSessionVividColorResId()));
            activity.setTaskDescription(taskDescription);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSessionDetailBinding.inflate(inflater, container, false);
        viewModel.setCallback(this);
        binding.setViewModel(viewModel);
        setHasOptionsMenu(true);
        initToolbar();
        initScroll();
        return binding.getRoot();
    }

    private void initScroll() {
        binding.nestedScroll.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    binding.fab.hide();
                }
                if (scrollY < oldScrollY) {
                    binding.fab.show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.destroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getComponent().inject(this);
    }

    private void initToolbar() {
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        activity.setSupportActionBar(binding.toolbar);
        ActionBar bar = activity.getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
            bar.setDisplayShowTitleEnabled(false);
            bar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public void onClickFab() {
        //
    }
}
