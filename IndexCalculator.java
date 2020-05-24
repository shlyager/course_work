import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

class IndexCalculator implements Runnable {
    private Map<String, List<Integer>> wordIndexMap;
    private String word;
    private List<File> files;

    public IndexCalculator(String word, List<File> files, Map<String, List<Integer>> wordIndexMap) {
        this.wordIndexMap = wordIndexMap;
        this.word = word;
        this.files = files;
    }

    @Override
    public void run() {
        HashMap<String, List<Integer>> wordIndexMap = new HashMap<>();
        for (File file : files) {
            try (FileReader fileReader = new FileReader(file)) {
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String b;
                while ((b = (bufferedReader.readLine())) != null) {
                    List<String> wordsList = Arrays.asList(b.replaceAll("<br />", " ")
                            .replaceAll("\\W", " ")
                            .replaceAll(" +", " ")
                            .split(" "));
                    if (wordsList.contains(word)) {
                        if (!this.wordIndexMap.containsKey(file.getName())) {
                            this.wordIndexMap.put(file.getName(), new ArrayList<>());
                        }
                        this.wordIndexMap.get(file.getName()).add(wordsList.indexOf(word));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}