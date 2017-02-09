package io.hefuyi.listener.injector.component;

import dagger.Component;
import io.hefuyi.listener.injector.module.PlaylistSongModule;
import io.hefuyi.listener.injector.scope.PerActivity;
import io.hefuyi.listener.ui.fragment.PlaylistDetailFragment;

/**
 * Created by hefuyi on 2016/12/6.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = PlaylistSongModule.class)
public interface PlaylistSongComponent {

    void inject(PlaylistDetailFragment playlistDetailFragment);
}
