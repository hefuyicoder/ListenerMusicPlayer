package io.hefuyi.listener.ui.adapter;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.List;

import io.hefuyi.listener.MusicPlayer;
import io.hefuyi.listener.R;
import io.hefuyi.listener.RxBus;
import io.hefuyi.listener.dataloader.SongLoader;
import io.hefuyi.listener.event.MediaUpdateEvent;
import io.hefuyi.listener.mvp.model.FolderInfo;
import io.hefuyi.listener.mvp.model.Song;
import io.hefuyi.listener.util.DensityUtil;
import io.hefuyi.listener.util.ListenerUtil;
import io.hefuyi.listener.util.NavigationUtil;
import io.hefuyi.listener.widget.fastscroller.FastScrollRecyclerView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class FolderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {

    private List<FolderInfo> arraylist;
    private AppCompatActivity mContext;

    public FolderAdapter(AppCompatActivity context, List<FolderInfo> arraylist) {
        if (arraylist == null) {
            this.arraylist = new ArrayList<>();
        } else {
            this.arraylist = arraylist;
        }
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_list_linear_layout_item, viewGroup, false);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemHolder itemHolder = (ItemHolder) holder;
        FolderInfo localItem = arraylist.get(position);
        Drawable image = mContext.getResources().getDrawable(R.drawable.ic_folder_black_48dp);
        image.setColorFilter(mContext.getResources().getColor(R.color.folderTint), PorterDuff.Mode.SRC_IN);
        itemHolder.image.setImageDrawable(image);
        itemHolder.folderName.setText(localItem.folderName);
        itemHolder.songcount.setText(ListenerUtil.makeLabel(mContext, R.plurals.Nsongs, localItem.songCount));
        itemHolder.folderPath.setText(localItem.folderPath);
        itemHolder.folderPath.setMaxWidth(DensityUtil.dip2px(mContext, 240));
        setOnPopupMenuListener(itemHolder, position);
    }



    @Override
    public int getItemCount() {
        return (null != arraylist ? arraylist.size() : 0);
    }

    private void setOnPopupMenuListener(final ItemHolder itemHolder, final int position) {
        itemHolder.popupMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupMenu menu = new PopupMenu(mContext, v);
                int adapterPosition = itemHolder.getAdapterPosition();
                final FolderInfo folderInfo = arraylist.get(adapterPosition);
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.popup_folder_addto_queue:
                                getSongListIdByFolder(folderInfo.folderPath)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<long[]>() {
                                            @Override
                                            public void call(long[] ids) {
                                                MusicPlayer.addToQueue(mContext, ids, -1, ListenerUtil.IdType.Folder);

                                            }
                                        });
                                break;
                            case R.id.popup_folder_addto_playlist:
                                getSongListIdByFolder(folderInfo.folderPath)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new Action1<long[]>() {
                                            @Override
                                            public void call(long[] ids) {
                                                ListenerUtil.showAddPlaylistDialog(mContext, ids);
                                            }
                                        });
                                break;
                            case R.id.popup_folder_delete:
                                new MaterialDialog.Builder(mContext)
                                        .title(mContext.getResources().getString(R.string.delete_folder))
                                        .content("删除文件夹下"+folderInfo.songCount+"首歌曲?")
                                        .positiveText(R.string.delete)
                                        .negativeText(R.string.cancel)
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                getSongListIdByFolder(folderInfo.folderPath)
                                                        .subscribeOn(Schedulers.io())
                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .subscribe(new Action1<long[]>() {
                                                            @Override
                                                            public void call(long[] ids) {
                                                                ListenerUtil.deleteTracks(mContext, ids);
                                                                RxBus.getInstance().post(new MediaUpdateEvent());
                                                            }
                                                        });
                                            }
                                        })
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                                break;
                        }
                        return false;
                    }
                });
                menu.inflate(R.menu.popup_folder);
                menu.show();
            }
        });
    }

    public void setFolderList(List<FolderInfo> arraylist) {
        this.arraylist = arraylist;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        Character ch = arraylist.get(position).folderName.charAt(0);
        if (Character.isDigit(ch)) {
            return "#";
        } else
            return Character.toString(ch);
    }

    private Observable<long[]> getSongListIdByFolder(String path) {
        return SongLoader.getSongListInFolder(mContext, path)
                .map(new Func1<List<Song>, long[]>() {
                    @Override
                    public long[] call(List<Song> songs) {
                        long[] ids = new long[songs.size()];
                        int i = 0;
                        for (Song song : songs) {
                            ids[i] = song.id;
                            i++;
                        }
                        return ids;
                    }
                });
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView image;
        private TextView folderName;
        private TextView songcount;
        private TextView folderPath;
        private ImageView popupMenu;

        public ItemHolder(View view) {
            super(view);
            this.image = (ImageView) view.findViewById(R.id.image);
            this.folderName = (TextView) view.findViewById(R.id.text_item_title);
            this.songcount = (TextView) view.findViewById(R.id.text_item_subtitle);
            this.folderPath = (TextView) view.findViewById(R.id.text_item_subtitle_2);
            this.popupMenu = (ImageView) view.findViewById(R.id.popup_menu);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            NavigationUtil.navigateToFolderSongs(mContext,arraylist.get(getAdapterPosition()).folderPath);
        }
    }
}


