import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MainCalculator {
    private static final int VARIANT = 15;
    private static final int THREAD_COUNT = 1;
    private static final String DIRECTORY_NAME = "D:\\aclImdb\\aclImdb\\test\\neg";
    private static final String WORD = "was";
    private static final int FILE_NUMBER = 12500;

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        test();
        System.out.println("time: "+(System.currentTimeMillis()- start));
    }


    public static void test() {
        Map<String, List<Integer>> pathIndexMap = new HashMap<>();
        pathIndexMap = Collections.synchronizedMap(pathIndexMap);
        List<Thread> threads = new ArrayList<>();
        List<File> files = getFiles(VARIANT, FILE_NUMBER, DIRECTORY_NAME);
        int part = files.size() / THREAD_COUNT;
        int sum = 0;
        for (int a = 0; a < THREAD_COUNT; a++){
            Thread thread;
            if(a == THREAD_COUNT-1) {
                thread = new Thread(new IndexCalculator(WORD, files.stream().skip(a * part).collect(Collectors.toList()), pathIndexMap));
            }else {
                thread = new Thread(new IndexCalculator(WORD, files.stream().skip(a * part).limit(part).collect(Collectors.toList()), pathIndexMap));
            }
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads){
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (Entry<String, List<Integer>>  entry : pathIndexMap.entrySet()){
            System.out.println("In file "+ entry.getKey() + "Indexes :");
            entry.getValue().stream().forEachOrdered(x -> System.out.println( x + "  "));
        }
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
