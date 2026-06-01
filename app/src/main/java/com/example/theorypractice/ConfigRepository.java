package com.example.theorypractice;

import android.content.res.AssetManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class ConfigRepository {
    private final AssetManager assetManager;
    private final SimpleTomlParser parser = new SimpleTomlParser();

    ConfigRepository(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    List<String> listConfigNames(TheoryMode mode) throws IOException {
        String[] files = assetManager.list("configs/" + mode.directoryName);
        if (files == null) {
            return new ArrayList<>();
        }

        Arrays.sort(files);
        List<String> names = new ArrayList<>();
        for (String file : files) {
            if (file.endsWith(".toml")) {
                names.add(file.substring(0, file.length() - ".toml".length()));
            }
        }
        return names;
    }

    TheoryConfig loadConfig(TheoryMode mode, String configName) throws IOException {
        String path = "configs/" + mode.directoryName + "/" + configName + ".toml";
        return parser.parse(readAssetText(path));
    }

    private String readAssetText(String path) throws IOException {
        try (InputStream inputStream = assetManager.open(path);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
            }
            return outputStream.toString(StandardCharsets.UTF_8.name());
        }
    }
}
