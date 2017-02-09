package io.hefuyi.listener.injector.component;

import dagger.Component;
import io.hefuyi.listener.injector.module.FolderModule;
import io.hefuyi.listener.injector.scope.PerActivity;
import io.hefuyi.listener.ui.fragment.FoldersFragment;

/**
 * Created by hefuyi on 2016/12/11.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = FolderModule.class)
public interface FoldersComponent {

    void inject(FoldersFragment foldersFragment);
}
