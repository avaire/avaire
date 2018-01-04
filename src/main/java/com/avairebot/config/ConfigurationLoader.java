package com.avairebot.config;

import com.avairebot.AvaIre;
import com.avairebot.contracts.config.Castable;
import com.avairebot.contracts.config.Configuration;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ConfigurationLoader implements Configuration {

    @Override
    public Castable load(String fileName, Class<?> type) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();

                FileWriter fw = new FileWriter(file.getAbsoluteFile());
                BufferedWriter bw = new BufferedWriter(fw);
                bw.write(this.defaultConfig(fileName));
                bw.close();

                AvaIre.getLogger().info("The {} configuration file is missing!", fileName);
                AvaIre.getLogger().info("Creating file and terminating program...");
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!(file.canRead() || file.canWrite())) {
            System.out.printf("%s config file cannot be read or written to!", fileName);
            System.exit(0);
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file.getAbsoluteFile()))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            return (Castable) AvaIre.GSON.fromJson(sb.toString(), (Type) type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String defaultConfig(String name) {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(name);
        StringBuilder textBuilder = new StringBuilder();

        try (Reader reader = new BufferedReader(new InputStreamReader(stream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int read;
            while ((read = reader.read()) != -1) {
                textBuilder.append((char) read);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return textBuilder.toString();
    }
}
