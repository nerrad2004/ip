import java.util.Scanner;

/**
 * Entry point for the Nerrad chatbot.
 */
public class Nerrad {
    /**
     * Starts the chatbot and processes commands until the user enters {@code bye}.
     *
     * @param args command-line arguments; not used
     */
    public static void main(String[] args) {
        String separator = "____________________________________________________________";
        String banner = " _   _                         _ \n"
                + "| \\ | | ___ _ __ _ __ __ _  __| |\n"
                + "|  \\| |/ _ \\ '__| '__/ _` |/ _` |\n"
                + "| |\\  |  __/ |  | | | (_| | (_| |\n"
                + "|_| \\_|\\___|_|  |_|  \\__,_|\\__,_|\n";

        System.out.println(separator);
        System.out.print(banner);
        System.out.println("\nHello! I'm Nerrad. My actual name is that backwards! xD");
        System.out.println("What can I do for you?");
        System.out.println(separator);

        String[] tasks = new String[100];
        int taskCount = 0;

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You: ");
            String input = scanner.nextLine();

            if (input.equals("bye")) {
                System.out.println("\n  Bye! Hope to see you again soon!!!");
                System.out.println(separator);
                break;
            }

            if (input.equals("list")) {
                System.out.println("\n  Here are the tasks in your list:");
                for (int i = 0; i < taskCount; i++) {
                    System.out.println("  " + (i + 1) + ". " + tasks[i]);
                }
                System.out.println(separator);
                continue;
            }

            tasks[taskCount] = input;
            taskCount++;
            System.out.println("\n  added: " + input);
            System.out.println(separator);
        }
        scanner.close();
    }
}
