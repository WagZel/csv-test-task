package ru.zelinov;

import ru.zelinov.properties.Environments;
import ru.zelinov.service.ProductService;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {
        Environments env = Environments.getInstance();
        ProductService service = new ProductService(env);

        File dir = new File(env.getCsvDirPath());
        File[] files = dir.listFiles();

        if (files == null)
            return;

        ExecutorService executorService = Executors.newFixedThreadPool(env.getCoreCount());

        for (File f : files) {
            executorService.submit(() -> service.findCheapest(f));
        }

        executorService.shutdown();

        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        service.createCheapestCsv();
    }
}
