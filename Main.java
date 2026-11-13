import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

public class Main {
    private final Scanner scanner = new Scanner(System.in);
    private final Map<String, Student> students = new LinkedHashMap<>();
    private final Map<String, Course> courses = new LinkedHashMap<>();
    private final Map<String, Map<String, Double>> enrollments = new LinkedHashMap<>();

    public static void main(String[] args) {
        Main app = new Main();
        if (GraphicsEnvironment.isHeadless()) {
            app.run();
        } else {
            SwingUtilities.invokeLater(() ->
                    new StudentManagementFrame(app.students, app.courses, app.enrollments).setVisible(true));
        }
    }

    private void run() {
        System.out.println("=== Student Management System ===");
        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ", 1, 12);
            switch (choice) {
                case 1:
                    addStudent();
                    break;
                case 2:
                    listStudents();
                    break;
                case 3:
                    searchStudent();
                    break;
                case 4:
                    updateStudent();
                    break;
                case 5:
                    deleteStudent();
                    break;
                case 6:
                    addCourse();
                    break;
                case 7:
                    listCourses();
                    break;
                case 8:
                    deleteCourse();
                    break;
                case 9:
                    enrollStudent();
                    break;
                case 10:
                    recordGrade();
                    break;
                case 11:
                    showStudentReport();
                    break;
                case 12:
                    running = false;
                    break;
                default:
                    throw new IllegalStateException("Unexpected menu choice: " + choice);
            }
        }
        System.out.println("Goodbye.");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Add student");
        System.out.println("2. List students");
        System.out.println("3. Search student");
        System.out.println("4. Update student");
        System.out.println("5. Delete student");
        System.out.println("6. Add course");
        System.out.println("7. List courses");
        System.out.println("8. Delete course");
        System.out.println("9. Enroll student in a course");
        System.out.println("10. Record or update a grade");
        System.out.println("11. View student report");
        System.out.println("12. Exit");
    }

    private void addStudent() {
        String id = readRequired("Student ID: ");
        if (students.containsKey(id)) {
            System.out.println("A student with that ID already exists.");
            return;
        }
        String name = readRequired("Student name: ");
        String email = readRequired("Student email: ");
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            System.out.println("Enter a valid email address.");
            return;
        }
        students.put(id, new Student(id, name, email));
        System.out.println("Student added.");
    }

    private void listStudents() {
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }
        students.values().forEach(System.out::println);
    }

    private void searchStudent() {
        String query = readRequired("Enter a student ID or name: ");
        List<Student> matches = new ArrayList<>();
        String normalizedQuery = query.toLowerCase(Locale.ROOT);
        for (Student student : students.values()) {
            if (student.getId().equalsIgnoreCase(query)
                    || student.getName().toLowerCase(Locale.ROOT).contains(normalizedQuery)) {
                matches.add(student);
            }
        }
        if (matches.isEmpty()) {
            System.out.println("No matching students found.");
            return;
        }
        matches.forEach(System.out::println);
    }

    private void updateStudent() {
        Student student = findStudent(readRequired("Student ID to update: "));
        if (student == null) {
            return;
        }
        String name = readRequired("New name: ");
        String email = readRequired("New email: ");
        if (!email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            System.out.println("Enter a valid email address.");
            return;
        }
        student.setName(name);
        student.setEmail(email);
        System.out.println("Student updated.");
    }

    private void deleteStudent() {
        String id = readRequired("Student ID to delete: ");
        if (students.remove(id) == null) {
            System.out.println("Student not found.");
            return;
        }
        enrollments.remove(id);
        System.out.println("Student and their enrollments deleted.");
    }

    private void addCourse() {
        String id = readRequired("Course ID: ");
        if (courses.containsKey(id)) {
            System.out.println("A course with that ID already exists.");
            return;
        }
        String name = readRequired("Course name: ");
        courses.put(id, new Course(id, name));
        System.out.println("Course added.");
    }

    private void listCourses() {
        if (courses.isEmpty()) {
            System.out.println("No courses found.");
            return;
        }
        courses.values().forEach(System.out::println);
    }

    private void deleteCourse() {
        String id = readRequired("Course ID to delete: ");
        if (courses.remove(id) == null) {
            System.out.println("Course not found.");
            return;
        }
        for (Map<String, Double> studentEnrollments : enrollments.values()) {
            studentEnrollments.remove(id);
        }
        System.out.println("Course and related enrollments deleted.");
    }

    private void enrollStudent() {
        Student student = findStudent(readRequired("Student ID: "));
        if (student == null) {
            return;
        }
        Course course = findCourse(readRequired("Course ID: "));
        if (course == null) {
            return;
        }
        Map<String, Double> studentEnrollments =
                enrollments.computeIfAbsent(student.getId(), key -> new LinkedHashMap<>());
        if (studentEnrollments.containsKey(course.getId())) {
            System.out.println("Student is already enrolled in this course.");
            return;
        }
        studentEnrollments.put(course.getId(), null);
        System.out.println("Student enrolled in " + course.getName() + ".");
    }

    private void recordGrade() {
        Student student = findStudent(readRequired("Student ID: "));
        if (student == null) {
            return;
        }
        Map<String, Double> studentEnrollments = enrollments.get(student.getId());
        if (studentEnrollments == null || studentEnrollments.isEmpty()) {
            System.out.println("Student is not enrolled in any courses.");
            return;
        }
        Course course = findCourse(readRequired("Course ID: "));
        if (course == null) {
            return;
        }
        if (!studentEnrollments.containsKey(course.getId())) {
            System.out.println("Student is not enrolled in this course.");
            return;
        }
        double grade = readDouble("Grade (0-100): ", 0, 100);
        studentEnrollments.put(course.getId(), grade);
        System.out.println("Grade recorded.");
    }

    private void showStudentReport() {
        Student student = findStudent(readRequired("Student ID: "));
        if (student == null) {
            return;
        }
        System.out.println("Report for " + student);
        Map<String, Double> studentEnrollments = enrollments.get(student.getId());
        if (studentEnrollments == null || studentEnrollments.isEmpty()) {
            System.out.println("No course enrollments.");
            return;
        }

        double total = 0;
        int gradedCourses = 0;
        for (Map.Entry<String, Double> enrollment : studentEnrollments.entrySet()) {
            Course course = courses.get(enrollment.getKey());
            if (course == null) {
                continue;
            }
            Double grade = enrollment.getValue();
            System.out.println(course + " - " + (grade == null ? "Not graded" : grade + "/100"));
            if (grade != null) {
                total += grade;
                gradedCourses++;
            }
        }
        if (gradedCourses > 0) {
            System.out.printf("Average grade: %.2f/100%n", total / gradedCourses);
        } else {
            System.out.println("No grades recorded yet.");
        }
    }

    private Student findStudent(String id) {
        Student student = students.get(id);
        if (student == null) {
            System.out.println("Student not found.");
        }
        return student;
    }

    private Course findCourse(String id) {
        Course course = courses.get(id);
        if (course == null) {
            System.out.println("Course not found.");
        }
        return course;
    }

    private String readRequired(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    private int readInt(String prompt, int minimum, int maximum) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                int number = Integer.parseInt(value);
                if (number >= minimum && number <= maximum) {
                    return number;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.printf("Enter a number from %d to %d.%n", minimum, maximum);
        }
    }

    private double readDouble(String prompt, double minimum, double maximum) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                double number = Double.parseDouble(value);
                if (Double.isFinite(number) && number >= minimum && number <= maximum) {
                    return number;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.printf("Enter a number from %.0f to %.0f.%n", minimum, maximum);
        }
    }
}