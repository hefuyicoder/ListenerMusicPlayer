package io.hefuyi.listener.injector.component;

import dagger.Component;
import io.hefuyi.listener.injector.module.SearchModule;
import io.hefuyi.listener.injector.scope.PerActivity;
import io.hefuyi.listener.ui.fragment.SearchFragment;

/**
 * Created by hefuyi on 2017/1/3.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = SearchModule.class)
public interface SearchComponent {

    void inject(SearchFragment searchFragment);
}
