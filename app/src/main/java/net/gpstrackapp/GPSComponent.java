package net.gpstrackapp;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import net.gpstrackapp.geomodel.track.TrackModelManager;
import net.sharksystem.asap.ASAPException;
import net.sharksystem.asap.android.apps.ASAPApplication;
import net.sharksystem.asap.android.apps.ASAPApplicationComponent;
import net.sharksystem.asap.android.apps.ASAPApplicationComponentHelper;
import net.sharksystem.asap.android.apps.ASAPComponentNotYetInitializedException;

import org.osmdroid.config.Configuration;
import org.osmdroid.config.IConfigurationProvider;

public class GPSComponent implements ASAPApplicationComponent {
    private final ASAPApplicationComponentHelper asapComponentHelper;
    private static GPSComponent instance = null;
    private static TrackModelManager trackModelManager;

    private GPSComponent(ASAPApplication asapApplication) {
        this.asapComponentHelper = new ASAPApplicationComponentHelper();
        this.asapComponentHelper.setASAPApplication(asapApplication);

        getTrackModelManager().loadAllGeoModelsFromFiles(getASAPApplication().getActivity());
        Log.d(getLogStart(), "Finished loading tracks from storage.");
    }

    public static GPSComponent initialize(ASAPApplication asapApplication) {
        GPSComponent.instance = new GPSComponent(asapApplication);

        try {
            Context ctx = GPSComponent.instance.getContext().getApplicationContext();

            IConfigurationProvider conf = Configuration.getInstance();
            conf.load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
            conf.setUserAgentValue(BuildConfig.APPLICATION_ID);

            // Debug options
            //conf.setDebugMode(true);
            //conf.setDebugTileProviders(true);
            //conf.setDebugMapTileDownloader(true);
            //conf.setDebugMapView(true);

        } catch (ASAPException e) {
            Log.e(getLogStart(), e.getLocalizedMessage());
        }

        return GPSComponent.instance;
    }

    public static synchronized GPSComponent getGPSComponent() throws ASAPComponentNotYetInitializedException {
        if (instance == null) {
            throw new ASAPComponentNotYetInitializedException("GPSComponent not yet initialized");
        }
        return instance;
    }

    @Override
    public Context getContext() throws ASAPException {
        return this.asapComponentHelper.getContext();
    }

    @Override
    public ASAPApplication getASAPApplication() {
        return this.asapComponentHelper.getASAPApplication();
    }

    public TrackModelManager getTrackModelManager() {
        if (this.trackModelManager == null) {
            this.trackModelManager = new TrackModelManager();
        }
        return this.trackModelManager;
    }

    private static String getLogStart() {
        return GPSComponent.class.getSimpleName();
    }
}
