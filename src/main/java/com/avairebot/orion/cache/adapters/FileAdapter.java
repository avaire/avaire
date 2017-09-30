package com.avairebot.orion.cache.adapters;

import com.avairebot.orion.Constants;
import com.avairebot.orion.cache.CacheItem;
import com.avairebot.orion.contracts.cache.CacheAdapter;
import com.avairebot.orion.contracts.cache.CacheClosure;
import com.google.gson.Gson;
import net.dv8tion.jda.core.utils.SimpleLog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class FileAdapter extends CacheAdapter {

    private final File storagePath;
    private final Gson gson;

    public FileAdapter() {
        storagePath = new File(Constants.STORAGE_PATH, "cache");
        gson = new Gson();

        if (!storagePath.exists() && !storagePath.mkdirs()) {
            SimpleLog.getLog(FileAdapter.class).fatal("Failed to create cache storage folder, exiting application...");
            System.exit(0);
        }
    }

    @Override
    public boolean put(String token, Object value, int seconds) {
        return writeTo(generateCacheFile(token), value, seconds);
    }

    @Override
    public Object remember(String token, int seconds, CacheClosure closure) {
        if (has(token)) {
            return get(token);
        }

        writeTo(generateCacheFile(token), closure.run(), seconds);
        return get(token);
    }

    @Override
    public boolean forever(String token, Object value) {
        return writeTo(generateCacheFile(token), value, Integer.MAX_VALUE);
    }

    @Override
    public Object get(String token) {
        File cacheFile = generateCacheFile(token);
        if (!cacheFile.exists()) {
            return null;
        }

        try {
            CacheItem item = gson.fromJson(new String(Files.readAllBytes(cacheFile.toPath())), CacheItem.class);
            if (item == null) {
                return false;
            }

            if (item.getTime() > System.currentTimeMillis()) {
                return item.getValue();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public CacheItem getRaw(String token) {
        File cacheFile = generateCacheFile(token);
        if (!cacheFile.exists()) {
            return null;
        }

        try {
            return gson.fromJson(new String(Files.readAllBytes(cacheFile.toPath())), CacheItem.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean has(String token) {
        File cacheFile = generateCacheFile(token);
        if (!cacheFile.exists()) {
            return false;
        }

        try {
            CacheItem item = gson.fromJson(new String(Files.readAllBytes(cacheFile.toPath())), CacheItem.class);
            if (item == null) {
                return false;
            }

            return item.getTime() > System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public CacheItem forget(String token) {
        File cacheFile = generateCacheFile(token);
        if (!cacheFile.exists()) {
            return null;
        }

        try {
            CacheItem item = gson.fromJson(new String(Files.readAllBytes(cacheFile.toPath())), CacheItem.class);
            if (item == null) {
                return null;
            }
            cacheFile.delete();

            return item;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean flush() {
        deleteRecursively(storagePath, true);

        return true;
    }

    private void deleteRecursively(File folder, boolean isRoot) {
        File[] files = folder.listFiles();
        if (files != null) { //some JVMs return null for empty dirs
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteRecursively(f, false);
                } else {
                    f.delete();
                }
            }
        }

        if (!isRoot) {
            folder.delete();
        }
    }

    private boolean writeTo(File file, Object value, long seconds) {
        try {
            if (!file.exists() && !file.createNewFile()) {
                return false;
            }
        } catch (IOException ignored) {
        }

        Map<String, Object> cacheItem = new HashMap<>();
        cacheItem.put("time", System.currentTimeMillis() + (seconds * 1000));
        cacheItem.put("value", value);
        cacheItem.put("key", file.getName());

        FileWriter fw = null;
        BufferedWriter bw = null;

        try {
            fw = new FileWriter(file, false);
            bw = new BufferedWriter(fw);

            bw.write(gson.toJson(cacheItem) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }

                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return true;
    }

    private File generateCacheFile(String string) {
        String cacheToken = encrypt(string);
        File cachePath = new File(storagePath,
                cacheToken.substring(0, 2) + File.separator
                        + cacheToken.substring(2, 4) + File.separator
        );

        if (!cachePath.exists()) {
            cachePath.mkdirs();
        }

        return new File(cachePath, cacheToken.substring(4, cacheToken.length()));
    }

    private String encrypt(String string) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(string.trim().toLowerCase().getBytes());

            byte[] digest = md5.digest();

            StringBuilder sb = new StringBuilder();
            for (byte aDigest : digest) {
                sb.append(Integer.toString((aDigest & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
