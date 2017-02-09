package io.hefuyi.listener.injector.component;

import dagger.Component;
import io.hefuyi.listener.injector.module.ArtistInfoModule;
import io.hefuyi.listener.injector.scope.PerActivity;
import io.hefuyi.listener.ui.adapter.ArtistAdapter;
import io.hefuyi.listener.ui.fragment.ArtistDetailFragment;

/**
 * Created by hefuyi on 2016/11/13.
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ArtistInfoModule.class)
public interface ArtistInfoComponent {

    void injectForAdapter(ArtistAdapter artistAdapter);

    void injectForFragment(ArtistDetailFragment fragment);
}
