package ru.zelinov.properties;

import lombok.Getter;

public class Environments {
    private static Environments env = new Environments();

    @Getter
    private final int coreCount;
    @Getter
    private final String csvDirPath;
    @Getter
    private final int maxSize;
    @Getter
    private final int maxSameId;
    @Getter
    private final boolean withHeader;

    private Environments() {
        int cores = 1;
        try {
            int count = Integer.parseInt(System.getenv("CORE_COUNT"));
            if (count < 1)
                throw new Exception();
            cores = count;
        } catch (Exception ignore) {}
        coreCount = cores;
        csvDirPath = System.getenv("CSV_DIR_PATH");
        int maxSize = 1000;
        try {
            int size = Integer.parseInt(System.getenv("MAX_PRODUCTS"));
            if (size < 1)
                throw new Exception();
            maxSize = size;
        } catch (Exception ignore) {}
        this.maxSize = maxSize;
        int maxSameId = 20;
        try {
            int ids = Integer.parseInt(System.getenv("MAX_SAME_ID"));
            if (ids < 1)
                throw new Exception();
            maxSize = ids;
        } catch (Exception ignore) {}
        this.maxSameId = maxSameId;
        boolean withHeader = false;
        try {
            boolean header = Boolean.parseBoolean(System.getenv("WITH_HEADER"));
            withHeader = header;
        } catch (Exception ignore) {}
        this.withHeader = withHeader;
    }

    public static Environments getInstance() {
        return env;
    }
}
