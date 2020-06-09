import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class MainCalculator {
    private static final int VARIANT = 15;
    private static final int THREAD_COUNT = 5;
    private static final int FILE_NUMBER1 = 12500;
    private static final int FILE_NUMBER2 = 50000;

    public static void main(String[] args) throws InterruptedException {

        List<String> paths = new ArrayList<>();

        paths.add("D:\\aclImdb\\aclImdb\\test\\neg");
        paths.add("D:\\aclImdb\\aclImdb\\test\\pos");
        paths.add("D:\\aclImdb\\aclImdb\\train\\neg");
        paths.add("D:\\aclImdb\\aclImdb\\train\\pos");
        paths.add("D:\\aclImdb\\aclImdb\\unsup");

        HashMap<String, HashSet<File>> invertedIndex = new HashMap<>();

        List<File> files = new ArrayList<>();

        for(int i = 0; i < paths.size(); i++) {
            files.addAll(i == paths.size() - 1
                    ? getFiles(VARIANT, FILE_NUMBER2, paths.get(i))
                    : getFiles(VARIANT, FILE_NUMBER1, paths.get(i)));
        }

        final int SIZE = files.size();
        int filesPerThread = SIZE / THREAD_COUNT;

        IndexCalculator[] threadArray = new IndexCalculator[THREAD_COUNT];

        long start = System.currentTimeMillis();

        for(int i = 0; i < THREAD_COUNT; i++) {
            threadArray[i] = new IndexCalculator(files, filesPerThread * i, filesPerThread);
            threadArray[i].start();
        }

        for(int i = 0; i < THREAD_COUNT; i++) {
            threadArray[i].join();
        }

        for(int threadN = 0; threadN < THREAD_COUNT; threadN++){
            threadArray[threadN]
                    .getInvertedIndex()
                    .forEach((k, v) -> invertedIndex.merge(k, v, (v1, v2) -> {
                        HashSet<File> set = new HashSet<>(v1);
                        set.addAll(v2);
                        return set;
                    }));
        }

        System.out.println("time: "+(System.currentTimeMillis() - start));

        System.out.println(invertedIndex);
    }

    public static List<File> getFiles(int variant, int fileNumber, String directoryName) {
        int firstIndex = fileNumber / 50 * (variant - 1);
        int lastIndex = fileNumber / 50 * variant;
        List<File> files = new ArrayList<>();
        try {
            files = Files.walk(Paths.get(directoryName))
                    .map(Path::toFile)
                    .sorted(new Comparator<>(){
                        @Override
                        public int compare(File o1, File o2) {
                            int n1 = extractNumber(o1.getName());
                            int n2 = extractNumber(o2.getName());
                            return n1-n2;
                        }
                        private int extractNumber(String name) {
                            int i = 0;
                            try {
                                String[] strings = name.split("_");
                                i = Integer.parseInt(strings[0]);
                            } catch(Exception e) {
                                i = 0;
                            }
                            return i;
                        }
                    })
                    .skip(firstIndex)
                    .limit(lastIndex - firstIndex)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

}
