import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FileTypeAnalyzer {
    static ExecutorService executor;
    static ArrayList<Future<?>> futureArrayList = new ArrayList<>();
    static ArrayList<FileType> fileTypeArrayList = new ArrayList<>();


    public static void main(String[] args) throws IOException {

        final String dirNotFoundMsg = "This directory does not exit.";
        String folderPath = args[0];
        String databasePath = args[1];


        executor = Executors.newCachedThreadPool();

        File dir = new File(folderPath);
        if (!dir.exists()) {
            System.out.println(dirNotFoundMsg);
        }

        loadData(databasePath);



       processFiles(dir);
//        Thread.sleep(4000);
        while (true) {
            boolean allDone = true;
            for (Future<?> f : futureArrayList) {
                allDone &= f.isDone();
            }
            if (allDone) break;

        }
        executor.shutdown();


    }

    public static void loadData(String path) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(path));
            String str;
            while ((str = in.readLine()) != null) {
                String[] arr = str.replaceAll("\"","").split(";");
                FileType type = new FileType(Integer.parseInt(arr[0]),arr[1],arr[2]);
//                type.listData();
                fileTypeArrayList.add(type);
            }
            in.close();
        } catch (IOException e) {
        }
        sortData();
//        for (FileType f : fileTypeArrayList) {
//            f.listData();
//        }
    }

    static void sortData() {
        fileTypeArrayList.sort( // reverse order
                (f1, f2) -> Integer.compare(f2.getPriority(), f1.getPriority())
        );
    }

    static void processFiles(File root) throws IOException {
        File[] dirs = root.listFiles(
                File::isDirectory
        );
        File[] files = root.listFiles(
                (file) -> !file.isDirectory()
        );

        assert dirs != null;
        if (dirs.length > 0) {
//            System.out.println(dirs);

            for (File dir : dirs
            ) {
                processFiles(dir);
            }
        }
        assert files != null;
        if (files.length > 0) {
//            System.out.println(files);
            for (File file : files
            ) {

                Execute execute = new Execute(file, fileTypeArrayList);
                futureArrayList.add(executor.submit(execute)) ;


            }
        }
    }
}

class FileType {
    private final int priority;
    private final String pattern;
    private final String description;

    public FileType(int priority, String pattern, String description) {
        this.priority = priority;
        this.pattern = pattern;
        this.description = description;
    }

    public int getPriority() {
        return priority;
    }

    public String getPattern() {
        return pattern;
    }

    public String getDescription() {
        return description;
    }

    public void listData() {
        System.out.println("priority: " + this.priority);
        System.out.println("pattern: " + this.pattern);
        System.out.println("description: " + this.description + "\n");


    }
}



class Execute extends Thread {
    private final File file;
    private final String text;
    private final ArrayList<FileType> list;
    private boolean matched = false;

    Execute (File file, ArrayList<FileType> list) throws IOException {
        this.file = file;
        this.list = list;
        byte[] data = Files.readAllBytes(file.toPath());
        this.text = new String(data);
    }

    boolean naive(int i) {
        matched = text.contains(list.get(i).getPattern());
//        if (!matched) System.out.println("Not " + list.get(i).getDescription());
        return matched;
    }

    boolean kmp(String text, String pattern) {
        int[] p = prefixFunction(pattern);
        int j = 0;
        for (int i = 0; i < text.length(); i++) {
            while (j > 0 && text.charAt(i) != pattern.charAt(j)) {
                j = p[j - 1];
            }
            if (text.charAt(i) == pattern.charAt(j)) {
                j += 1;
            }
            if (j == pattern.length()) {
                return true;
            }
        }
        return false;
    }


    int[] prefixFunction(String s) {
        int size = s.length();
        int[] p = new int[size];
        Arrays.fill(p, 0, size, 0);
        for(int i = 1; i < size; i++) {
            int j = p[i - 1];
            while (j > 0 && s.charAt(i) != s.charAt(j)) {
                j = p[j - 1];
            }
            if (s.charAt(i) == s.charAt(j)) {
                j += 1;
            }
            p[i] = j;
        }
        return p;
    }

    void showResult(boolean check, int i) {
        if (check) {
            System.out.println(file.getName() + ": " + list.get(i).getDescription());
//            System.out.println(file.getName() + ": " + list.get(i).getDescription() + " (" + i + ")");
        }
        else {
            System.out.println(file.getName() + ": " + "Unknown file type" + " (" + i + ")");
        }
    }

    @Override
    public void run() {
        int index = 0;
        for (int i = 0; i < list.size(); i++) {
            index = i;
            matched = kmp(text,list.get(i).getPattern());
//            matched = naive(i);
            if (matched) {

                break;
            }
        }
        showResult(matched,index);

    }

}

class PolyHash {
    static int ph(String sub) {
        final int a = 3;
        final int m = 11;
        int hash = 0;
        sub = sub.toLowerCase();
        for (int i = 0; i < sub.length(); i++) {
            hash += (sub.codePointAt(i) - 96) * (int) Math.pow(a, i);
        }
        hash %= m;
        return hash;
    }

    static int ph(String sub, int a , int m) {
        int hash = 0;
        sub = sub.toLowerCase();
        for (int i = 0; i < sub.length(); i++) {
            hash += (sub.codePointAt(i) - 96) * (int) Math.pow(a, i);
        }
        hash %= m;
        return hash;
    }

//    static int rolling(int previous, char out, char in) {
//        int hash;
//
//        return hash;
//    }
}

