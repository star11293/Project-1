/*
 * Name: Akene Crosse
 * Class: Computer Science I (CMSC203)
 * Professor: Thai
 * Project: Assignment 1 - Grade Calculator
 * Date: 09/25/2026
 * Platform/compiler: Java (JDK 21)
 * Pledge: I have not copied code from another student or given my code to any student.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Reads a grading setup and one student's scores, then works out the
 * category averages, overall average and letter grade, and saves a report.
 */
public class GradeCalculator {

    /**
     * Runs the grade calculator.
     *
     * @param args not used
     */
    public static void main(String[] args) {

        // default setup, used if gradeconfig.txt is missing or wrong
        String courseName = "CMSC203 Computer Science I";
        String categories = "Projects 40\nQuizzes 30\nExams 30\n"; // "name weight" on each line
        boolean usedDefault = false;
        String configProblem = "";

        System.out.println("========================================");
        System.out.println("   CMSC203 Project 1 - Grade Calculator");
        System.out.println("========================================");

        // ---------------- read gradeconfig.txt ----------------
        System.out.println("Loading configuration from gradeconfig.txt ...");
        try {
            Scanner config = new Scanner(new File("gradeconfig.txt"));
            String newCourse = "";
            String newCategories = "";
            int numCategories = 0;
            int totalWeight = 0;

            if (config.hasNextLine()) {
                newCourse = config.nextLine().trim();
            }
            if (newCourse.equals("")) {
                configProblem = "course name is missing";
            }

            // number of categories
            if (configProblem.equals("")) {
                String line = "";
                if (config.hasNextLine()) {
                    line = config.nextLine().trim();
                }
                try {
                    numCategories = Integer.parseInt(line);
                    if (numCategories < 1) {
                        configProblem = "number of categories must be at least 1";
                    }
                } catch (NumberFormatException e) {
                    configProblem = "number of categories is not a whole number";
                }
            }

            // each category line looks like "Projects 40"
            int count = 0;
            while (configProblem.equals("") && count < numCategories) {
                if (!config.hasNextLine()) {
                    configProblem = "expected " + numCategories + " categories but found " + count;
                } else {
                    String line = config.nextLine().trim();
                    Scanner lineReader = new Scanner(line);
                    String name = "";
                    String weightText = "";
                    if (lineReader.hasNext()) {
                        name = lineReader.next();
                    }
                    if (lineReader.hasNext()) {
                        weightText = lineReader.next();
                    }

                    if (name.equals("") || weightText.equals("") || lineReader.hasNext()) {
                        configProblem = "bad category line \"" + line + "\"";
                    } else if (("\n" + newCategories.toLowerCase()).contains("\n" + name.toLowerCase() + " ")) {
                        configProblem = name + " is listed twice";
                    } else {
                        try {
                            int weight = Integer.parseInt(weightText);
                            if (weight < 1 || weight > 100) {
                                configProblem = "weight for " + name + " must be 1 to 100";
                            } else {
                                newCategories += name + " " + weight + "\n";
                                totalWeight += weight;
                                count++;
                            }
                        } catch (NumberFormatException e) {
                            configProblem = "weight for " + name + " is not a whole number";
                        }
                    }
                    lineReader.close();
                }
            }

            // extra lines after the categories are not allowed
            while (configProblem.equals("") && config.hasNextLine()) {
                if (!config.nextLine().trim().equals("")) {
                    configProblem = "more categories than the number given";
                }
            }

            if (configProblem.equals("") && totalWeight != 100) {
                configProblem = "weights add up to " + totalWeight + " instead of 100";
            }
            config.close();

            if (configProblem.equals("")) {
                courseName = newCourse;
                categories = newCategories;
            }
        } catch (FileNotFoundException e) {
            configProblem = "gradeconfig.txt was not found";
        }

        if (configProblem.equals("")) {
            System.out.println("Configuration loaded successfully.");
        } else {
            usedDefault = true;
            System.out.println("Configuration problem: " + configProblem);
            System.out.println("Using DEFAULT configuration: Projects 40, Quizzes 30, Exams 30");
        }

        // ---------------- read grades_input.txt ----------------
        System.out.println("Using input file: grades_input.txt");
        System.out.println("Using output file: grades_report.txt");

        Scanner input;
        try {
            input = new Scanner(new File("grades_input.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: grades_input.txt is missing or can't be read. No report was made.");
            System.out.println("Programmer: Akene Crosse");
            return;
        }

        System.out.println("Reading student scores...");
        String firstName = "";
        String lastName = "";
        if (input.hasNextLine()) {
            firstName = input.nextLine().trim();
        }
        if (input.hasNextLine()) {
            lastName = input.nextLine().trim();
        }
        if (firstName.equals("") || lastName.equals("")) {
            System.out.println("ERROR: student name is missing from grades_input.txt. No report was made.");
            System.out.println("Programmer: Akene Crosse");
            input.close();
            return;
        }

        String results = "";   // category lines for the summary
        String problems = "";  // errors found in the input file
        String done = "\n";    // categories already added
        double overall = 0;

        // each category takes 3 lines: name, number of scores, the scores
        while (input.hasNextLine()) {
            String catName = input.nextLine().trim();
            if (catName.equals("")) {
                continue; // skip blank lines
            }
            String countLine = "";
            String scoreLine = "";
            if (input.hasNextLine()) {
                countLine = input.nextLine().trim();
            }
            if (input.hasNextLine()) {
                scoreLine = input.nextLine().trim();
            }

            // find the weight for this category
            int weight = 0;
            Scanner catReader = new Scanner(categories);
            while (catReader.hasNext()) {
                String name = catReader.next();
                int w = catReader.nextInt();
                if (name.equalsIgnoreCase(catName)) {
                    catName = name;
                    weight = w;
                }
            }
            catReader.close();

            int numScores = 0;
            try {
                numScores = Integer.parseInt(countLine);
            } catch (NumberFormatException e) {
                numScores = 0;
            }

            if (weight == 0) {
                problems += "  ERROR: " + catName + " is not a category in the config - skipped\n";
            } else if (done.contains("\n" + catName.toLowerCase() + "\n")) {
                problems += "  ERROR: " + catName + " is listed twice - skipped\n";
            } else if (numScores < 1) {
                problems += "  ERROR: " + catName + " has a bad number of scores (\"" + countLine + "\") - skipped\n";
            } else {
                // add up the scores
                Scanner scoreReader = new Scanner(scoreLine);
                double sum = 0;
                int good = 0;
                int found = 0;
                while (scoreReader.hasNext()) {
                    String s = scoreReader.next();
                    found++;
                    try {
                        double score = Double.parseDouble(s);
                        if (score >= 0 && score <= 100) {
                            sum += score;
                            good++;
                        } else {
                            problems += "  WARNING: " + catName + " score " + s + " is not 0-100 - ignored\n";
                        }
                    } catch (NumberFormatException e) {
                        problems += "  WARNING: " + catName + " score \"" + s + "\" is not a number - ignored\n";
                    }
                }
                scoreReader.close();

                if (found != numScores) {
                    problems += "  WARNING: " + catName + " should have " + numScores
                            + " scores but has " + found + "\n";
                }

                if (good == 0) {
                    problems += "  ERROR: " + catName + " has no valid scores - skipped\n";
                } else {
                    double average = sum / good;
                    overall += average * weight / 100;
                    done += catName.toLowerCase() + "\n";
                    results += "  " + catName + " (" + weight + "%): average = "
                            + String.format("%.2f", average) + "\n";
                }
            }
        }
        input.close();

        // any category that never got scores counts as 0
        Scanner catReader = new Scanner(categories);
        while (catReader.hasNext()) {
            String name = catReader.next();
            int w = catReader.nextInt();
            if (!done.contains("\n" + name.toLowerCase() + "\n")) {
                results += "  " + name + " (" + w + "%): no valid scores - counted as 0.00\n";
            }
        }
        catReader.close();

        // round to 2 decimals so the grade matches what is printed
        // (the + 0.001 fixes tiny decimal errors, like 88.575 being stored as 88.57499...)
        overall = Math.round(overall * 100 + 0.001) / 100.0;

        // ---------------- show results ----------------
        String summary = "Student: " + firstName + " " + lastName + "\n";
        summary += "Course: " + courseName + "\n";
        if (usedDefault) {
            summary += "Configuration: DEFAULT was used (" + configProblem + ")\n";
        } else {
            summary += "Configuration: loaded from gradeconfig.txt\n";
        }
        summary += "Category Results:\n" + results;
        if (!problems.equals("")) {
            summary += "Problems in input file:\n" + problems;
        }

        System.out.println();
        System.out.print(summary);
        System.out.println();

        // ---------------- ask about +/- ----------------
        Scanner keyboard = new Scanner(System.in);
        String answer = "";
        while (!answer.equalsIgnoreCase("Y") && !answer.equalsIgnoreCase("N")) {
            System.out.print("Apply +/- grading? (Y/N): ");
            if (!keyboard.hasNextLine()) {
                answer = "N"; // no input at all, so just don't use +/-
            } else {
                answer = keyboard.nextLine().trim();
                if (!answer.equalsIgnoreCase("Y") && !answer.equalsIgnoreCase("N")) {
                    System.out.println("Invalid input. Please enter Y or N.");
                }
            }
        }
        keyboard.close();
        boolean plusMinus = answer.equalsIgnoreCase("Y");

        // ---------------- letter grade ----------------
        // A 90-100, B 80-89.99, C 70-79.99, D 60-69.99, F below 60
        int band = (int) (overall / 10);
        String letter;
        switch (band) {
            case 10:
            case 9:
                letter = "A";
                break;
            case 8:
                letter = "B";
                break;
            case 7:
                letter = "C";
                break;
            case 6:
                letter = "D";
                break;
            default:
                letter = "F";
        }

        // +/- : top 3 points of a band get "+", bottom 3 points get "-", F gets none
        String finalLetter = letter;
        if (plusMinus && !letter.equals("F")) {
            double spot = overall - band * 10;
            if (overall >= 100 || spot >= 7) {
                finalLetter = letter + "+";
            } else if (spot < 3) {
                finalLetter = letter + "-";
            }
        }

        String ending = "Overall numeric average: " + String.format("%.2f", overall) + "\n";
        ending += "Base letter grade: " + letter + "\n";
        if (plusMinus) {
            ending += "+/- grading: yes\n";
        } else {
            ending += "+/- grading: no\n";
        }
        ending += "Final letter grade: " + finalLetter + "\n";
        System.out.print(ending);

        // ---------------- save the report ----------------
        try {
            PrintWriter out = new PrintWriter("grades_report.txt");
            out.print(summary);
            out.print(ending);
            out.println("Programmer: Akene Crosse");
            out.close();
            System.out.println("Summary written to grades_report.txt");
        } catch (FileNotFoundException e) {
            System.out.println("ERROR: could not write grades_report.txt");
        }

        System.out.println("Program complete. Goodbye!");
        System.out.println("Programmer: Akene Crosse");
    }
}
