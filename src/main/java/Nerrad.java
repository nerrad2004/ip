import java.util.Scanner;

/**
 * Entry point for the Nerrad chatbot.
 */
public class Nerrad {
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

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("You: ");
            String input = scanner.nextLine();

            if (input.equals("bye")) {
                System.out.println("\nNerrad: Bye! Hope to see you again soon!!!");
                System.out.println(separator);
                break;
            }

            System.out.println("\n" + "Nerrad: " + input);
            System.out.println(separator);
        }
        scanner.close();
    }
}
