import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class DiaryApp {
    static final int MAX_ENTRIES = 50;
    static LocalDateTime[] dates = new LocalDateTime[MAX_ENTRIES];
    static String[] entries = new String[MAX_ENTRIES];
    static String displayFormat = "yyyy-MM-dd HH:mm:ss";
    static DateTimeFormatter storageFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Відкрити існуючий щоденник? (так/ні): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("так")) {
            System.out.print("Введіть шлях до файлу: ");
            String path = scanner.nextLine();
            loadFromFile(path);
        }

        System.out.print("Введіть бажаний формат відображення дати (наприклад, dd.MM.yyyy HH:mm): ");
        String userFormat = scanner.nextLine();
        try {
            DateTimeFormatter test = DateTimeFormatter.ofPattern(userFormat);
            displayFormat = userFormat;
        } catch (IllegalArgumentException e) {
            System.out.println("Невірний формат. Використано стандартний.");
        }

        boolean running = true;
        while (running) {
            System.out.println("\n=== МІЙ ЩОДЕННИК ===");
            System.out.println("1. Додати запис");
            System.out.println("2. Видалити запис");
            System.out.println("3. Переглянути всі записи");
            System.out.println("4. Вийти");
            System.out.print("Оберіть дію: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    addEntry(scanner);
                    break;
                case "2":
                    deleteEntry(scanner);
                    break;
                case "3":
                    viewEntries();
                    break;
                case "4":
                    running = false;
                    break;
                default:
                    System.out.println("Невірний вибір. Спробуйте ще раз.");
            }
        }

        System.out.print("Зберегти щоденник перед виходом? (так/ні): ");
        if (scanner.nextLine().trim().equalsIgnoreCase("так")) {
            System.out.print("Введіть шлях для збереження: ");
            String savePath = scanner.nextLine();
            saveToFile(savePath);
        }

        scanner.close();
        System.out.println("До побачення!");
    }

    static void addEntry(Scanner scanner) {
        if (getEntryCount() >= MAX_ENTRIES) {
            System.out.println("Щоденник заповнений!");
            return;
        }

        System.out.print("Введіть дату та час (yyyy-MM-dd HH:mm:ss): ");
        String dateInput = scanner.nextLine();
        LocalDateTime dateTime;
        try {
            dateTime = LocalDateTime.parse(dateInput, storageFormatter);
        } catch (DateTimeParseException e) {
            System.out.println("Невірний формат дати!");
            return;
        }

        System.out.println("Введіть текст запису (введіть 'END' на новому рядку, щоб завершити):");
        StringBuilder text = new StringBuilder();
        while (true) {
            String line = scanner.nextLine();
            if (line.equals("END")) break;
            text.append(line).append("\n");
        }

        for (int i = 0; i < MAX_ENTRIES; i++) {
            if (dates[i] == null) {
                dates[i] = dateTime;
                entries[i] = text.toString().trim();
                System.out.println("Запис додано!");
                return;
            }
        }
    }

    static void deleteEntry(Scanner scanner) {
        System.out.print("Введіть дату запису (yyyy-MM-dd HH:mm:ss): ");
        String input = scanner.nextLine();
        try {
            LocalDateTime dateTime = LocalDateTime.parse(input, storageFormatter);
            for (int i = 0; i < MAX_ENTRIES; i++) {
                if (dateTime.equals(dates[i])) {
                    dates[i] = null;
                    entries[i] = null;
                    System.out.println("Запис видалено.");
                    return;
                }
            }
        } catch (DateTimeParseException e) {
            System.out.println("Невірний формат дати!");
        }

        System.out.println("Запис не знайдено.");
    }

    static void viewEntries() {
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern(displayFormat);
        boolean hasAny = false;
        for (int i = 0; i < MAX_ENTRIES; i++) {
            if (dates[i] != null) {
                hasAny = true;
                System.out.println("\nДата: " + dates[i].format(displayFormatter));
                System.out.println("Запис:\n" + entries[i]);
            }
        }
        if (!hasAny) System.out.println("Записів немає.");
    }

    static int getEntryCount() {
        int count = 0;
        for (LocalDateTime d : dates) {
            if (d != null) count++;
        }
        return count;
    }

    static void saveToFile(String path) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(path))) {
            for (int i = 0; i < MAX_ENTRIES; i++) {
                if (dates[i] != null) {
                    writer.println(dates[i].format(storageFormatter));
                    writer.println(entries[i]);
                    writer.println(); // порожній рядок
                }
            }
        } catch (IOException e) {
            System.out.println("Помилка збереження у файл.");
        }
    }

    static void loadFromFile(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            int index = 0;

            while ((line = reader.readLine()) != null && index < MAX_ENTRIES) {
                if (line.trim().isEmpty()) continue;

                LocalDateTime date = LocalDateTime.parse(line.trim(), storageFormatter);
                StringBuilder entryText = new StringBuilder();
                while ((line = reader.readLine()) != null && !line.trim().isEmpty()) {
                    entryText.append(line).append("\n");
                }

                dates[index] = date;
                entries[index] = entryText.toString().trim();
                index++;
            }

        } catch (IOException | DateTimeParseException e) {
            System.out.println("Помилка при зчитуванні файлу.");
        }
    }
}
