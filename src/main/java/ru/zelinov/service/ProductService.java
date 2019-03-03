package ru.zelinov.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.apache.commons.collections4.SortedBag;
import org.apache.commons.collections4.bag.SynchronizedSortedBag;
import org.apache.commons.collections4.bag.TreeBag;
import ru.zelinov.entity.Product;
import ru.zelinov.exception.HighPriceException;
import ru.zelinov.properties.Environments;
import ru.zelinov.spliterator.CsvSpliterator;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ProductService {

    private String dir;
    private int maxSize;
    private int maxSameId;
    private boolean withHeader;
    private SynchronizedSortedBag<Product> cheapest;
    private Map<Integer, SortedBag<Product>> idCounts;

    public ProductService(Environments env) {
        dir = env.getCsvDirPath();
        maxSize = env.getMaxSize();
        maxSameId = env.getMaxSameId();
        withHeader = env.isWithHeader();
        cheapest = SynchronizedSortedBag.synchronizedSortedBag(new TreeBag<>());
        idCounts = new ConcurrentHashMap<>();
    }

    public void findCheapest(File file) {
        float cheap = cheapest.size() == maxSize ? cheapest.first().getPrice() : Float.MAX_VALUE;
        try (FileInputStream fis = new FileInputStream(file)) {
            csvStream(fis)
                    .parallel()
                    .skip(withHeader ? 1 : 0)
                    .map(it -> new Product(Integer.parseInt(it[0]), it[1], it[2], it[3], Float.parseFloat(it[4])))
                    .filter(p -> p.getPrice() < cheap)
                    .sorted()
                    .limit(maxSize)
                    .forEachOrdered(p -> {
                        if (cheapest.size() != maxSize) {
                            addProduct(p);
                            return;
                        }
                        Product lp = cheapest.last();
                        if (lp.getPrice() < p.getPrice())
                            throw new HighPriceException();
                        addProduct(p);
                        if (cheapest.size() > maxSize) {
                            cheapest.remove(lp);
                            idCounts.get(lp.getId()).remove(lp);
                        }
                    });
        } catch(FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } catch (HighPriceException ignore) {}
    }

    public void createCheapestCsv() {
        Path path = Paths.get((dir.charAt(dir.length() - 1) == '/' ? dir : dir + "/") + "cheapest.csv");
        try (Writer writer = Files.newBufferedWriter(path)) {
            new StatefulBeanToCsvBuilder<Product>(writer)
                    .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                    .build()
                    .write(new ArrayList<>(cheapest));
        } catch (IOException | CsvDataTypeMismatchException | CsvRequiredFieldEmptyException e) {
            e.printStackTrace();
        }
    }

    private void addProduct(Product p) {
        SortedBag<Product> sameIdBag = idCounts.get(p.getId());
        if (sameIdBag == null) {
            sameIdBag = new TreeBag<>();
            idCounts.put(p.getId(), sameIdBag);
        }
        if (sameIdBag.size() == maxSameId) {
            if (sameIdBag.last().getPrice() < p.getPrice())
                return;
        }
        sameIdBag.add(p);
        if (sameIdBag.size() > maxSameId) {
            while (sameIdBag.size() != maxSameId) {
                cheapest.remove(sameIdBag.last(), 1);
                sameIdBag.remove(sameIdBag.last(), 1);
            }
        }
        cheapest.add(p);
    }

    private Stream<String[]> csvStream(InputStream in) {
        final CSVReader cr = new CSVReader(new InputStreamReader(in));
        return StreamSupport.stream(new CsvSpliterator(cr), false).onClose(() -> {
            try { cr.close(); } catch (IOException e) { throw new UncheckedIOException(e); }
        });
    }
}
