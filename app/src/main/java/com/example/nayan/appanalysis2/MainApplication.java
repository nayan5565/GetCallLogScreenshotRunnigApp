package com.example.nayan.appanalysis2;

import android.app.Application;
import android.content.Context;

import com.example.nayan.appanalysis2.custom.Post;
import com.example.nayan.appanalysis2.custom.PostsProvider;
import com.facebook.stetho.Stetho;

import me.everything.providers.core.Data;
import me.everything.providers.stetho.ProvidersStetho;


public class MainApplication extends Application {
    public static Context context;
    public void onCreate() {
        super.onCreate();

        context = this;
        ProvidersStetho providersStetho = new ProvidersStetho(context);
        providersStetho.enableDefaults();

        // register custom provider if you want - this is sample one
        providersStetho.registerProvider("provider-custom", "posts", new ProvidersStetho.QueryExecutor<Post>() {
            @Override
            public Data<Post> onQuery(String query) {
                PostsProvider provider = new PostsProvider(getApplicationContext());
                return provider.getPosts();
            }
        });

        // stetho init
        Stetho.initialize(
                Stetho.newInitializerBuilder(context)
                        .enableDumpapp(Stetho.defaultDumperPluginsProvider(context))
                        .enableWebKitInspector(providersStetho.defaultInspectorModulesProvider())
                        .build());
    }


}
