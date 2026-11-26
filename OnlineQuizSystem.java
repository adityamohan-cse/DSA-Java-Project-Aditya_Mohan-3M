import java.util.*;
import java.io.*;

public class OnlineQuizSystem {

    // ─── Question class ──────────────────────────────────────────────────────
    static class Question {
        String question;
        String optA, optB, optC, optD;
        String correctAnswer;

        Question(String question, String optA, String optB, String optC, String optD, String correctAnswer) {
            this.question = question;
            this.optA = optA;
            this.optB = optB;
            this.optC = optC;
            this.optD = optD;
            this.correctAnswer = correctAnswer;
        }
    }

    // ─── Answered Question (for stack review) ────────────────────────────────
    static class AnsweredQuestion {
        String question;
        String playerAnswer;
        String correctAnswer;

        AnsweredQuestion(String question, String playerAnswer, String correctAnswer) {
            this.question = question;
            this.playerAnswer = playerAnswer;
            this.correctAnswer = correctAnswer;
        }
    }

    // ─── Score class (for leaderboard) ───────────────────────────────────────
    static class Score {
        String playerName;
        int scored;
        int total;

        Score(String playerName, int scored, int total) {
            this.playerName = playerName;
            this.scored = scored;
            this.total = total;
        }
    }

    // ─── Global data ─────────────────────────────────────────────────────────
    static Queue<Question> questionQueue = new LinkedList<>();
    static Stack<AnsweredQuestion> reviewStack = new Stack<>();
    static ArrayList<Score> leaderboard = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);

    // ─────────────────────────────────────────────────────────────────────────
    public static void main(String[] args) {

        loadQuestionsFromFile("quiz.txt");
        loadScoresFromFile("scores.txt");

        System.out.println("=============================");
        System.out.println("    Welcome to Quiz System   ");
        System.out.println("=============================");

        boolean running = true;
        while (running) {
            System.out.println("\n--- MENU ---");
            System.out.println("1. Start Quiz");
            System.out.println("2. Review Last Answers");
            System.out.println("3. Leaderboard");
            System.out.println("4. Exit");
            System.out.print("Choose option: ");

            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                startQuiz();
            } else if (choice.equals("2")) {
                reviewAnswers();
            } else if (choice.equals("3")) {
                showLeaderboard();
            } else if (choice.equals("4")) {
                saveScoresToFile("scores.txt");
                System.out.println("Goodbye! Scores saved.");
                running = false;
            } else {
                System.out.println("Invalid option. Please choose 1, 2, 3 or 4.");
            }
        }
    }

    // ─── Load questions from quiz.txt ────────────────────────────────────────
    // Format of each line: question|optA|optB|optC|optD|correctAnswer
    static void loadQuestionsFromFile(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists()) {
                System.out.println("quiz.txt not found! Please create it.");
                return;
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                String[] parts = line.split("\\|");
                if (parts.length == 6) {
                    Question q = new Question(
                            parts[0], parts[1], parts[2], parts[3], parts[4], parts[5].toUpperCase());
                    questionQueue.add(q);
                }
            }
            br.close();
            System.out.println("Questions loaded: " + questionQueue.size());
        } catch (IOException e) {
            System.out.println("Error reading quiz.txt: " + e.getMessage());
        }
    }

    // ─── Load scores from scores.txt ─────────────────────────────────────────
    // Format of each line: playerName,scored,total
    static void loadScoresFromFile(String filename) {
        try {
            File file = new File(filename);
            if (!file.exists())
                return;

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty())
                    continue;

                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String name = parts[0];
                    int scored = Integer.parseInt(parts[1]);
                    int total = Integer.parseInt(parts[2]);
                    leaderboard.add(new Score(name, scored, total));
                }
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Error reading scores.txt: " + e.getMessage());
        }
    }

    // ─── Save scores to scores.txt ───────────────────────────────────────────
    static void saveScoresToFile(String filename) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            for (Score s : leaderboard) {
                bw.write(s.playerName + "," + s.scored + "," + s.total);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            System.out.println("Error saving scores.txt: " + e.getMessage());
        }
    }

    // ─── Start Quiz ──────────────────────────────────────────────────────────
    static void startQuiz() {
        if (questionQueue.isEmpty()) {
            System.out.println("No questions available! Check quiz.txt.");
            return;
        }

        System.out.print("Enter your name: ");
        String playerName = sc.nextLine().trim();

        // Clear the review stack for the new player
        reviewStack.clear();

        // Make a temporary copy of the queue so original is preserved
        Queue<Question> tempQueue = new LinkedList<>(questionQueue);

        int score = 0;
        int totalQ = tempQueue.size();
        int questionNo = 1;

        while (!tempQueue.isEmpty()) {
            Question q = tempQueue.poll();

            // Keep asking until a valid answer is given
            String playerAnswer = "";
            while (true) {
                System.out.println("\nQ" + questionNo + ": " + q.question);
                System.out.println("  " + q.optA);
                System.out.println("  " + q.optB);
                System.out.println("  " + q.optC);
                System.out.println("  " + q.optD);
                System.out.print("Your answer (A/B/C/D): ");

                playerAnswer = sc.nextLine().trim().toUpperCase();

                if (playerAnswer.equals("A") || playerAnswer.equals("B")
                        || playerAnswer.equals("C") || playerAnswer.equals("D")) {
                    break; // valid answer – move on
                } else {
                    System.out.println("Invalid option. Enter A, B, C or D.");
                    // Loop again – same question is asked again
                }
            }

            // Check answer
            if (playerAnswer.equals(q.correctAnswer)) {
                score++;
            } else {
                System.out.println("Wrong! Correct answer: " + q.correctAnswer);
            }

            // Push to review stack
            reviewStack.push(new AnsweredQuestion("Q" + questionNo, playerAnswer, q.correctAnswer));
            questionNo++;
        }

        System.out.println("\nQuiz over. " + playerName + " scored " + score + "/" + totalQ + ".");

        // Save to leaderboard
        leaderboard.add(new Score(playerName, score, totalQ));
        sortLeaderboard();

        // Auto-save after each quiz
        saveScoresToFile("scores.txt");
    }

    // ─── Review Answers (uses Stack – latest first) ───────────────────────────
    static void reviewAnswers() {
        if (reviewStack.isEmpty()) {
            System.out.println("No answers to review. Play a quiz first!");
            return;
        }

        System.out.println("\n--- Review (Latest First) ---");

        // We use a temporary stack so the original is not destroyed
        Stack<AnsweredQuestion> temp = new Stack<>();
        temp.addAll(reviewStack);

        while (!temp.isEmpty()) {
            AnsweredQuestion aq = temp.pop();
            System.out.println(aq.question
                    + " | Your answer: " + aq.playerAnswer
                    + " | Correct: " + aq.correctAnswer);
        }
    }

    // ─── Leaderboard (sorted by score descending) ────────────────────────────
    static void showLeaderboard() {
        if (leaderboard.isEmpty()) {
            System.out.println("Leaderboard is empty. No scores yet.");
            return;
        }

        System.out.println("\n--- Leaderboard ---");
        for (int i = 0; i < leaderboard.size(); i++) {
            Score s = leaderboard.get(i);
            System.out.println((i + 1) + ". " + s.playerName + " - " + s.scored + "/" + s.total);
        }
    }

    // ─── Sort leaderboard by score descending (Bubble Sort – DSA) ────────────
    static void sortLeaderboard() {
        int n = leaderboard.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                Score a = leaderboard.get(j);
                Score b = leaderboard.get(j + 1);
                if (a.scored < b.scored) {
                    // Swap
                    leaderboard.set(j, b);
                    leaderboard.set(j + 1, a);
                }
            }
        }
    }
}
