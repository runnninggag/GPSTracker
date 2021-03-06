package net.gpstrackapp.geomodel.track;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import net.gpstrackapp.GPSComponent;
import net.gpstrackapp.geomodel.GeoModelManager;
import net.gpstrackapp.geomodel.GeoModelStorage;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TrackModelManager extends GeoModelManager<Track> implements GeoModelStorage<Track> {
    private static final String SUBDIR_NAME = "tracks";
    public void mergeTracks(Context ctx, Set<Track> tracksToMerge, String newTrackName) {
        List<TrackSegment> trackSegments = tracksToMerge.stream()
                .flatMap(track -> track.getTrackSegments().stream())
                .collect(Collectors.toList());
        Track track = new Track(null, newTrackName,
                GPSComponent.getGPSComponent().getASAPApplication().getOwnerName(),
                LocalDateTime.now(), trackSegments);
        addGeoModel(track);
        Toast.makeText(ctx, "The Tracks have been successfully merged.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void saveGeoModelToFile(Context ctx, Track trackToSave) {
        try {
            File dir = new File(ctx.getFilesDir(), SUBDIR_NAME);
            dir.mkdirs();
            File fileToSave = new File(dir, trackToSave.getObjectId().toString());
            Log.d(getLogStart(), fileToSave.getAbsolutePath());
            FileOutputStream fos = new FileOutputStream(fileToSave);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(trackToSave);
            oos.close();
            Log.d(getLogStart(), "Saved track: " + trackToSave.getObjectName());
        } catch (IOException e) {
            Log.e(getLogStart(), "A problem occurred while trying to save a track." + System.lineSeparator() + e.getLocalizedMessage());
            Toast.makeText(ctx, "A problem occurred while trying to save a track.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void saveGeoModelsToFiles(Context ctx, Set<Track> tracksToSave) {
        Iterator<Track> iterator = tracksToSave.iterator();
        while (iterator.hasNext()) {
            Track trackToSave = iterator.next();
            saveGeoModelToFile(ctx, trackToSave);
        }
        Toast.makeText(ctx, "The Tracks have been successfully saved.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void deleteGeoModelFromFile(Context ctx, Track trackToDelete) {
        File dir = new File(ctx.getFilesDir(), SUBDIR_NAME);
        dir.mkdirs();
        File fileToDelete = new File(dir, trackToDelete.getObjectId().toString());
        // just returns false if file does not exist
        fileToDelete.delete();
        Log.d(getLogStart(), "Deleted track: " + trackToDelete.getObjectName());
    }

    @Override
    public void deleteGeoModelsFromFiles(Context ctx, Set<Track> tracksToDelete) {
        Iterator<Track> iterator = tracksToDelete.iterator();
        while (iterator.hasNext()) {
            Track trackToDelete = iterator.next();
            deleteGeoModelFromFile(ctx, trackToDelete);
        }
        Toast.makeText(ctx, "The Tracks have been successfully deleted.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void loadAllGeoModelsFromFiles(Context ctx) {
        File dir = new File(ctx.getFilesDir(), SUBDIR_NAME);
        Log.d(getLogStart(), dir.getAbsolutePath());
        dir.mkdirs();
        File[] files = dir.listFiles(pathname -> pathname.isFile());
        Log.d(getLogStart(), "Attempt to load " + files.length + " tracks from storage");
        Set<Track> tracks = new HashSet<>();
        for (int i = 0; i < files.length; i++) {
            try {
                FileInputStream fis = new FileInputStream(files[i]);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Track loadedTrack = (Track) ois.readObject();
                tracks.add(loadedTrack);
                Log.d(getLogStart(), "Loaded track with UUID: " + loadedTrack.getObjectId());
            } catch (IOException | ClassNotFoundException e) {
                Log.e(getLogStart(), "A problem occurred while trying to load a track." + System.lineSeparator() + e.getLocalizedMessage());
            }
        }

        Iterator<Track> iterator = tracks.iterator();
        while (iterator.hasNext()) {
            Track track = iterator.next();
            this.addGeoModel(track);
        }
    }

    private String getLogStart() {
        return this.getClass().getSimpleName();
    }
}
