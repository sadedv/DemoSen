import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Main class
 */

public class Practice {

    static int limitSentencesForOneWord = 5;//количество предложений в тренеровке содержащих изучаемое слово
    static int hundred = 50;//ограничение уникальных предложений для одного слова
    static int countOfHundred;
    static String firstLang;
    static String secondLang;
    static Set<String> inputWords = new HashSet<>();
    static String dataBase = "EngRusSentencesDemo.txt";
    static String wordsBase = "EngWordsDemo.txt";
    static String currentTraining = "currentTraining.txt";
    static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    static Queue<String> queueOfStudiedWords = new LinkedBlockingQueue<>();
    static Map<String, List<String>> wordEngTextRusText = new LinkedHashMap<>();

    private Practice() {

    }

    public static void main(String[] args) throws IOException {
        selectLanguage();
        selectWords();

        wordFilter();

        searchEngSentences();
        buffering();
        practice();
    }


    /**
     * Отсеевает введеные пользователем слова, которых нет в базе данных,
     * те что остались будем тренеровать по частоте их встречаемости в английском языке
     */
    private static void wordFilter() throws IOException {

        BufferedReader fileReader = new BufferedReader(new FileReader(wordsBase));

        String line;
        while (fileReader.ready()) {
            if (queueOfStudiedWords.size() >= 100)
                break;//ограничим максимальное количество слов для изучения за тренеровку

            line = fileReader.readLine();

            String[] arr = line.split("\\t");

            //String id = arr[0];
            String wordInFile = arr[1];
            //String ratingOfWord = arr[2];

            if (inputWords.contains(wordInFile)) {
                queueOfStudiedWords.add(wordInFile);
            }


        }
        fileReader.close();
    }

    /**
     * Тренеровка, непосредственно
     */
    private static void practice() throws IOException {

        //reader = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader fileReader = new BufferedReader(new FileReader(currentTraining));

        String line;
        while (fileReader.ready()) {
            line = fileReader.readLine();

            String[] arr = line.split("\\t");

            String engText = arr[0];
            String rusText = arr[1];


            System.out.print(engText);
//            System.out.println("Показать перевод? y/n");
            String answer1 = reader.readLine();

            if ("".equals(answer1)) {
                System.out.println(rusText);
            }

//            if ("y".equals(answer1)) {
//                System.out.println(rusText);
//                System.out.println();
//            }

//            System.out.println("Нажмите ввод чтобы двигаться дальше!");
            reader.readLine();
        }
        reader.close();
        fileReader.close();
    }

    /**
     * запись найденых предложений во временный файл, для оффлайн тренеровки
     */
    private static void buffering() throws FileNotFoundException {

        PrintWriter printWriter = new PrintWriter(new File(currentTraining));
        Random rnd = new Random(System.currentTimeMillis());

        for (Map.Entry<String, List<String>> pair : wordEngTextRusText.entrySet()) {

            //String word = pair.getKey();

            List<String> engTextRusText = pair.getValue();

            if (engTextRusText.size() < limitSentencesForOneWord) {
                for (int i = 0; i < engTextRusText.size(); i++) {
                    printWriter.println(engTextRusText.get(i));
                }
            } else {
                for (int i = 0; i < limitSentencesForOneWord; i++) {
                    int random = rnd.nextInt(engTextRusText.size());
                    String currentSentence = engTextRusText.get(random);
                    printWriter.println(currentSentence);
                    engTextRusText.remove(random);
                }
            }
        }
        printWriter.close();
    }


    /**
     * поиск иностранных предолжений, содержащих изучаемое слово, и добавление их в карту
     */
    private static void searchEngSentences() {
        try {
            for (String word : queueOfStudiedWords) {

                List<String> engTextRusText = new ArrayList<>();
                BufferedReader fileReader = new BufferedReader(new FileReader(dataBase));

                countOfHundred = 0;
                String line;
                while (fileReader.ready()) {
                    //загружаю 100 первых предложений из базы данных содержащих изучаемое слово,
                    //потом я выберу 5 случайных из сотни, для разнообразия тренеровок
                    if (countOfHundred >= hundred) {
                        break;
                    }
                    line = fileReader.readLine();

                    String sentenceWithTheWord = engSentencesMapper(word, line);

                    if (!"".equals(sentenceWithTheWord)) {
                        engTextRusText.add(sentenceWithTheWord);
                    }
                    wordEngTextRusText.put(word, engTextRusText);
                }
                fileReader.close();
            }
        } catch (Exception e) {

        }
    }

    /**
     * Поиск английского предложения по слову
     */
    private static String engSentencesMapper(String word, String line) {

        String result = "";

        String[] arr = line.split("\\t");
        if (arr.length == 5) {
            //String key = arr[0];
            //String engLang = arr[1];
            String engText = arr[2];
            //String rusLang = arr[3];
            String rusText = arr[4];

            word = word.toLowerCase();

            String[] textArr = engText.replaceAll("(?!')\\W+", " ").replaceAll("\\p{Digit}+", " ").toLowerCase().split(" ");

            for (int i = 0; i < textArr.length; i++) {
                if (textArr[i].equals(word)) {
                    countOfHundred++;
                    result = engText + "\t" + rusText;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Выбираем слова для изучения
     */
    private static void selectWords() throws IOException {
        //reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Введите слова для изучения или скопируйте текст. Для продолжения всегда нажимайте Enter.");

        String line;
        while ((line = reader.readLine()) != null) {

            if (line.isEmpty())
                break;

            String[] textArr = line.replaceAll("\\n", " ").replaceAll("(?!')\\W+", " ").replaceAll("\\p{Digit}+", " ").toLowerCase().split(" ");
            for (String word : textArr) {
                if (!"".equals(word)) {
                    inputWords.add(word);
                }
            }
        }
    }

    /**
     * Выбираем язык, пока что русский и английский
     */
    private static void selectLanguage() {
//        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
//        System.out.println("Выберите свой родной язык. Например rus или eng");
        // firstLang = reader.readLine();
        firstLang = "rus";
//        System.out.println("Выберите язык для изучения. Например rus или eng");
        //secondLang = reader.readLine();
        secondLang = "eng";
    }
}
/***/