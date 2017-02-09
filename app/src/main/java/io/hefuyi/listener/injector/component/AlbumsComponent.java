package io.hefuyi.listener.injector.component;

import dagger.Component;
import io.hefuyi.listener.injector.module.ActivityModule;
import io.hefuyi.listener.injector.module.AlbumsModule;
import io.hefuyi.listener.injector.scope.PerActivity;
import io.hefuyi.listener.ui.fragment.AlbumFragment;

/**
 * Created by hefuyi on 2016/11/12.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = {ActivityModule.class, AlbumsModule.class})
public interface AlbumsComponent {

    void inject(AlbumFragment albumFragment);
}
