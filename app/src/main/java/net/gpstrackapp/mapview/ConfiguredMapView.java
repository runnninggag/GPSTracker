package net.gpstrackapp.mapview;

import android.content.Context;
import android.util.Log;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ConfiguredMapView extends MapView {
    private static ITileSource defaultTileSource = TileSourceFactory.OpenTopo;
    private ITileSource selectedTileSource = defaultTileSource;
    // add Tile Sources to use for the maps here, make sure to read the terms of service before adding them
    private static Set<ITileSource> validTileSources = new HashSet<>(Arrays.asList(
            // MAPNIK and WIKIMEDIA are not available for downloads
            TileSourceFactory.MAPNIK,
            // not included in this version of osmdroid yet, problems with newer osmdroid versions are explained in build.gradle above osmdroid dependency
            //TileSourceFactory.WIKIMEDIA,

            TileSourceFactory.OpenTopo
            /*
            At the moment the USGS TileSources return a Not Found error for tiles on zoom levels 9 or
            higher, as can be checked on these sites by trying to load the Start Tile of Level ID 9 or higher:
            TileSourceFactory.USGS_TOPO: https://basemap.nationalmap.gov/arcgis/rest/services/USGSTopo/MapServer
            TileSourceFactory.USGS_SAT: https://basemap.nationalmap.gov/arcgis/rest/services/USGSImageryTopo/MapServer
             */
    ));

    public ConfiguredMapView(Context ctx) {
        super(ctx);
        this.setTileSource(selectedTileSource);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(getLogStart(), "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(getLogStart(), "onPause");
    }

    @Override
    public void setTileSource(ITileSource tileSource) {
        if (validTileSources.contains(tileSource)) {
            this.selectedTileSource = tileSource;
            super.setTileSource(tileSource);

            double minZoom = tileSource.getMinimumZoomLevel();
            double maxZoom = tileSource.getMaximumZoomLevel();
            // adjust zoom level, otherwise tiles won't render because they don't exist for these zoom levels
            if (getZoomLevelDouble() < minZoom) {
                getController().setZoom(minZoom);
            } else if (getZoomLevelDouble() > maxZoom) {
                getController().setZoom(maxZoom);
            }
        } else {
            Log.e(getLogStart(), "The passed TileSource parameter is invalid." + System.lineSeparator() +
                    "For policy reasons only the following TileSources are valid:" + System.lineSeparator() +
                    getValidTileSourcesAsString());
        }
    }

    public static void setDefaultTileSource(ITileSource defaultTileSource) {
        if (validTileSources.contains(defaultTileSource)) {
            ConfiguredMapView.defaultTileSource = defaultTileSource;
        } else {
            Log.e(getLogStart(), "The passed TileSource parameter is invalid." + System.lineSeparator() +
                    "For policy reasons only the following TileSources are valid:" + System.lineSeparator() +
                    getValidTileSourcesAsString());
        }
    }

    public static ITileSource getDefaultTileSource() {
        return defaultTileSource;
    }

    public static Set<ITileSource> getValidTileSources() {
        return validTileSources;
    }

    public static String getValidTileSourcesAsString() {
        String tileSourceString = "";
        // basically the foreach implementation
        for (Iterator<ITileSource> i = validTileSources.iterator(); i.hasNext();) {
            ITileSource tileSource = i.next();
            tileSourceString += tileSource.name();
            if (i.hasNext()) {
                tileSourceString += "," + System.lineSeparator();
            }
        }
        return tileSourceString;
    }

    private static String getLogStart() {
        return ConfiguredMapView.class.getSimpleName();
    }
}
