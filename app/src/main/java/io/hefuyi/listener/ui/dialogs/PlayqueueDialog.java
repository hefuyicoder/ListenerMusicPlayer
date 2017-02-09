package io.hefuyi.listener.ui.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.hefuyi.listener.ListenerApp;
import io.hefuyi.listener.MusicPlayer;
import io.hefuyi.listener.MusicService;
import io.hefuyi.listener.R;
import io.hefuyi.listener.injector.component.ApplicationComponent;
import io.hefuyi.listener.injector.component.DaggerPlayqueueSongComponent;
import io.hefuyi.listener.injector.component.PlayqueueSongComponent;
import io.hefuyi.listener.injector.module.PlayqueueSongModule;
import io.hefuyi.listener.mvp.contract.PlayqueueSongContract;
import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.ui.adapter.PlayqueueSongsAdapter;
import io.hefuyi.listener.util.ColorUtil;
import io.hefuyi.listener.widget.DividerItemDecoration;

/**
 * Created by hefuyi on 2016/12/27.
 */

public class PlayqueueDialog extends DialogFragment implements PlayqueueSongContract.View{

    @Inject
    PlayqueueSongContract.Presenter mPresenter;
    @BindView(R.id.tv_play_mode)
    TextView tvPlayMode;
    @BindView(R.id.iv_play_mode)
    ImageView ivPlayMode;
    @BindView(R.id.clear_all)
    ImageView clearAll;
    @BindView(R.id.recycler_view_songs)
    RecyclerView recyclerView;
    @BindView(R.id.bottomsheet)
    LinearLayout root;

    private PlayqueueSongsAdapter mAdapter;
    private Palette.Swatch mSwatch;
    private PlayMode mPlayMode;

    public enum PlayMode {
        REPEATALL,
        CURRENT,
        SHUFFLE
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.gravity = Gravity.BOTTOM;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(params);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        injectDependences();
        mPresenter.attachView(this);
        mAdapter = new PlayqueueSongsAdapter((AppCompatActivity) getActivity(), null);
    }

    private void injectDependences() {
        ApplicationComponent applicationComponent = ((ListenerApp) getActivity().getApplication()).getApplicationComponent();
        PlayqueueSongComponent playqueueSongComponent = DaggerPlayqueueSongComponent.builder()
                .applicationComponent(applicationComponent)
                .playqueueSongModule(new PlayqueueSongModule())
                .build();
        playqueueSongComponent.inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_playqueue,container,false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (mSwatch != null) {
            root.setBackgroundColor(mSwatch.getRgb());
            mAdapter.setPaletteSwatch(mSwatch);
            int blackWhiteColor = ColorUtil.getBlackWhiteColor(mSwatch.getRgb());
            tvPlayMode.setTextColor(blackWhiteColor);
            ivPlayMode.setColorFilter(blackWhiteColor);
            clearAll.setColorFilter(blackWhiteColor);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST, false));

        int shuffleMode = MusicPlayer.getShuffleMode();
        int repeatMode = MusicPlayer.getRepeatMode();
        if (shuffleMode == MusicService.SHUFFLE_NONE) {
            if (repeatMode == MusicService.REPEAT_CURRENT) {
                //单曲播放模式
                ivPlayMode.setImageDrawable(getResources().getDrawable(R.drawable.ic_one_shot));
                tvPlayMode.setText(R.string.repeat_current);
                mPlayMode = PlayMode.CURRENT;
            }else{
                //顺序播放模式
                ivPlayMode.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_repeat));
                tvPlayMode.setText(R.string.repeat_all);
                mPlayMode = PlayMode.REPEATALL;
            }
        } else if (shuffleMode == MusicService.SHUFFLE_NORMAL||shuffleMode==MusicService.SHUFFLE_AUTO) {
            //随机播放模式
            ivPlayMode.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_shuffle));
            tvPlayMode.setText(R.string.shuffle_all);
            mPlayMode = PlayMode.SHUFFLE;
        }

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                if (mAdapter.getItemCount() == 0) {
                    dismiss();
                }
            }
        });

        mPresenter.subscribe();

    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        super.onActivityCreated(arg0);
        getDialog().getWindow()
                .getAttributes().windowAnimations = R.style.DialogAnimation;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.unsubscribe();
    }

    @Override
    public void showSongs(List<Song> songs) {
        mAdapter.setSongList(songs);
    }

    public void setPaletteSwatch(Palette.Swatch swatch) {
        if (swatch == null) {
            return;
        }
        mSwatch = swatch;
        if (root != null) {
            root.setBackgroundColor(mSwatch.getRgb());
            int blackWhiteColor = ColorUtil.getBlackWhiteColor(mSwatch.getRgb());
            tvPlayMode.setTextColor(blackWhiteColor);
            ivPlayMode.setColorFilter(blackWhiteColor);
            clearAll.setColorFilter(blackWhiteColor);
            mAdapter.setPaletteSwatch(mSwatch);
        }
    }

    public void dismiss() {
        getDialog().dismiss();
    }

    @OnClick(R.id.iv_play_mode)
    public void onPlayModeClick() {
        if (mPlayMode == PlayMode.REPEATALL) {
            ivPlayMode.setImageDrawable(getResources().getDrawable(R.drawable.ic_one_shot));
            tvPlayMode.setText(R.string.repeat_current);
            MusicPlayer.setShuffleMode(MusicService.SHUFFLE_NONE);
            MusicPlayer.setRepeatMode(MusicService.REPEAT_CURRENT);
            mPlayMode = PlayMode.CURRENT;
        } else if (mPlayMode == PlayMode.CURRENT) {
            ivPlayMode.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_shuffle));
            tvPlayMode.setText(R.string.shuffle_all);
            MusicPlayer.setShuffleMode(MusicService.SHUFFLE_NORMAL);
            MusicPlayer.setRepeatMode(MusicService.REPEAT_ALL);
            mPlayMode = PlayMode.SHUFFLE;
        } else if (mPlayMode == PlayMode.SHUFFLE) {
            ivPlayMode.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_repeat));
            tvPlayMode.setText(R.string.repeat_all);
            MusicPlayer.setShuffleMode(MusicService.SHUFFLE_NONE);
            mPlayMode = PlayMode.REPEATALL;
        }
    }

    @OnClick(R.id.clear_all)
    public void onClearAllClick() {
        new MaterialDialog.Builder(getActivity())
                .title(getActivity().getResources().getString(R.string.clear_song_queue) + "?")
                .positiveText(R.string.sure)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dismiss();
                        MusicPlayer.clearQueue();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
