import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class IndexCalculator extends Thread {
    private Map<String, HashSet<File>> invertedIndex;
    private List<File> files;
    private int startIdx;
    private int filesNumber;

    public IndexCalculator(List<File> files, int startIdx, int filesNumber) {
        this.files = files;
        this.startIdx = startIdx;
        this.filesNumber = filesNumber;
    }

    public Map<String, HashSet<File>> getInvertedIndex() {
        return invertedIndex;
    }

    @Override
    public void run() {
        invertedIndex = new HashMap<>();
        List<File> filesToCompute = files.subList(startIdx, startIdx + filesNumber);
        for (File file : filesToCompute) {
            try (FileReader fileReader = new FileReader(file)) {
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                while ((line = (bufferedReader.readLine())) != null) {
                    List<String> wordsList = Arrays.asList(line.replaceAll("<br />", " ")
                            .toLowerCase()
                            .replaceAll("\\W", " ")
                            .replaceAll(" +", " ")
                            .replaceAll("[\\\\.$|,|;|']", " ")
                            .trim()
                            .split(" "));
                    for(String word : wordsList) {
                        if (invertedIndex.containsKey(word)) {
                            invertedIndex.get(word).add(file);
                        } else {
                            HashSet<File> filesToPut = new HashSet<>();
                            filesToPut.add(file);
                            invertedIndex.put(word, filesToPut);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}