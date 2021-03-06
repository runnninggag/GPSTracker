package net.gpstrackapp.format;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public interface ImportFileFormat extends FileFormat {
    void importFromFile(Context ctx, InputStream inputStream) throws IOException;
}
