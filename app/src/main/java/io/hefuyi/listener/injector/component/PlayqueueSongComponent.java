package io.hefuyi.listener.injector.component;

import dagger.Component;
import io.hefuyi.listener.injector.module.PlayqueueSongModule;
import io.hefuyi.listener.injector.scope.PerActivity;
import io.hefuyi.listener.ui.dialogs.PlayqueueDialog;

/**
 * Created by hefuyi on 2016/12/27.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = PlayqueueSongModule.class)
public interface PlayqueueSongComponent {

    void inject(PlayqueueDialog playqueueDialog);
}
