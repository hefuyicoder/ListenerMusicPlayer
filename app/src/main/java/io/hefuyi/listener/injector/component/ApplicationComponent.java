package io.hefuyi.listener.injector.component;

import android.app.Application;

import dagger.Component;
import io.hefuyi.listener.ListenerApp;
import io.hefuyi.listener.injector.module.ApplicationModule;
import io.hefuyi.listener.injector.module.NetworkModule;
import io.hefuyi.listener.injector.scope.PerApplication;
import io.hefuyi.listener.respository.interfaces.Repository;

/**
 * Created by hefuyi on 2016/11/3.
 */
@PerApplication
@Component(modules = {ApplicationModule.class, NetworkModule.class})
public interface ApplicationComponent {

    Application application();

    ListenerApp listenerApplication();

    Repository repository();
}
