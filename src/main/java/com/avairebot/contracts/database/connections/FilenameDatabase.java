package com.avairebot.contracts.database.connections;


import com.avairebot.contracts.database.Database;
import com.avairebot.database.exceptions.DatabaseException;

import java.io.File;

public abstract class FilenameDatabase extends Database {

    /**
     * The directory/folder the database is stored in.
     */
    private String directory;

    /**
     * The name of the database file.
     */
    private String filename;

    /**
     * The extension of the database file.
     */
    private String extension;

    /**
     * The database file.
     */
    private File file;

    /**
     * Creates a new filename database instance.
     */
    public FilenameDatabase() {
        file = null;
    }

    /**
     * Returns the folder name the database is stored in.
     *
     * @return the database directory
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * Sets the folder name the database is stored in.
     *
     * @param directory the directory the database is stored in
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }

    /**
     * Returns the database file name.
     *
     * @return the database file name
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Sets the database file name.
     *
     * @param filename the database file name
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Returns the database extension of the file name.
     *
     * @return the database extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Sets the database file name extension.
     *
     * @param extension the database extension
     */
    public void setExtension(String extension) {
        if (!extension.startsWith(".")) {
            extension = "." + extension;
        }

        this.extension = extension;
    }

    /**
     * Returns the database file object.
     *
     * @return File
     */
    public File getFile() {
        return file;
    }

    /**
     * Sets and creates the database file object from the given values.
     *
     * @param directory The folder the database is stored in.
     * @param filename  The database file name.
     * @param extension The database file extension.
     * @throws DatabaseException
     */
    protected void setFile(String directory, String filename, String extension) throws DatabaseException {
        setExtension(extension);
        setDirectory(directory);
        setFilename(filename);

        File folder = new File(getDirectory());
        if (!folder.exists()) {
            folder.mkdir();
        }

        file = new File(folder.getAbsolutePath() + File.separator + getFilename() + getExtension());
    }
}
