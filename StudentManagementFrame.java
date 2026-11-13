import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

@SuppressWarnings("serial")
public final class StudentManagementFrame extends JFrame {
    private static final String EMAIL_PATTERN = "[^\\s@]+@[^\\s@]+\\.[^\\s@]+";

    private final Map<String, Student> students;
    private final Map<String, Course> courses;
    private final Map<String, Map<String, Double>> enrollments;
    private final DefaultTableModel studentModel = createModel("Student ID", "Name", "Email");
    private final DefaultTableModel courseModel = createModel("Course ID", "Course name");
    private final DefaultTableModel enrollmentModel =
            createModel("Student ID", "Student", "Course ID", "Course", "Grade");
    private final JLabel summary = new JLabel();
    private JTable studentTable;
    private JTable courseTable;

    public StudentManagementFrame(
            Map<String, Student> students,
            Map<String, Course> courses,
            Map<String, Map<String, Double>> enrollments) {
        super("Student Management System");
        this.students = students;
        this.courses = courses;
        this.enrollments = enrollments;
        buildWindow();
        refreshTables();
    }

    private void buildWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(780, 500));
        setSize(980, 620);
        setLocationRelativeTo(null);

        JPanel content = new JPanel(new BorderLayout(16, 16));
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));
        content.add(buildHeader(), BorderLayout.NORTH);
        content.add(buildTabs(), BorderLayout.CENTER);
        summary.setBorder(BorderFactory.createEmptyBorder(4, 2, 0, 0));
        content.add(summary, BorderLayout.SOUTH);
        setContentPane(content);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Student Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        JLabel subtitle = new JLabel("Manage students, courses, enrollments, and grades");
        subtitle.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        JPanel labels = new JPanel(new GridLayout(0, 1));
        labels.add(title);
        labels.add(subtitle);
        header.add(labels, BorderLayout.WEST);
        return header;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Students", buildStudentsTab());
        tabs.addTab("Courses", buildCoursesTab());
        tabs.addTab("Enrollments & grades", buildEnrollmentsTab());
        return tabs;
    }

    private JPanel buildStudentsTab() {
        studentTable = createTable(studentModel);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(button("Add student", this::addStudent));
        actions.add(button("Edit selected", this::editStudent));
        actions.add(button("Delete selected", this::deleteStudent));
        actions.add(button("Search", this::searchStudents));
        actions.add(button("Show all", () -> refreshStudentTable("")));
        return buildTableTab(studentTable, actions);
    }

    private JPanel buildCoursesTab() {
        courseTable = createTable(courseModel);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(button("Add course", this::addCourse));
        actions.add(button("Delete selected", this::deleteCourse));
        return buildTableTab(courseTable, actions);
    }

    private JPanel buildEnrollmentsTab() {
        JTable table = createTable(enrollmentModel);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(button("Enroll student", this::enrollStudent));
        actions.add(button("Record grade", this::recordGrade));
        actions.add(button("Student report", this::showStudentReport));
        return buildTableTab(table, actions);
    }

    private JPanel buildTableTab(JTable table, JPanel actions) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));
        panel.add(actions, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    private JButton button(String label, Runnable action) {
        JButton button = new JButton(label);
        button.addActionListener(event -> action.run());
        return button;
    }

    private JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(26);
        table.setFillsViewportHeight(true);
        return table;
    }

    private static DefaultTableModel createModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void addStudent() {
        JTextField id = new JTextField();
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JPanel form = form("Student ID", id, "Name", name, "Email", email);
        if (!confirmForm("Add student", form)) {
            return;
        }
        String studentId = id.getText().trim();
        String studentName = name.getText().trim();
        String studentEmail = email.getText().trim();
        if (studentId.isEmpty() || studentName.isEmpty() || studentEmail.isEmpty()) {
            showError("All fields are required.");
        } else if (students.containsKey(studentId)) {
            showError("A student with that ID already exists.");
        } else if (!studentEmail.matches(EMAIL_PATTERN)) {
            showError("Enter a valid email address.");
        } else {
            students.put(studentId, new Student(studentId, studentName, studentEmail));
            refreshTables();
        }
    }

    private void editStudent() {
        String id = selectedId(studentModel, 0, "Select a student to edit.");
        if (id == null) {
            return;
        }
        Student student = students.get(id);
        JTextField name = new JTextField(student.getName());
        JTextField email = new JTextField(student.getEmail());
        JPanel form = form("Name", name, "Email", email);
        if (!confirmForm("Edit student", form)) {
            return;
        }
        String studentName = name.getText().trim();
        String studentEmail = email.getText().trim();
        if (studentName.isEmpty() || studentEmail.isEmpty()) {
            showError("All fields are required.");
        } else if (!studentEmail.matches(EMAIL_PATTERN)) {
            showError("Enter a valid email address.");
        } else {
            student.setName(studentName);
            student.setEmail(studentEmail);
            refreshTables();
        }
    }

    private void deleteStudent() {
        String id = selectedId(studentModel, 0, "Select a student to delete.");
        if (id == null || !confirmDelete("Delete this student and their enrollments?")) {
            return;
        }
        students.remove(id);
        enrollments.remove(id);
        refreshTables();
    }

    private void searchStudents() {
        String query = JOptionPane.showInputDialog(this, "Search by student ID or name:");
        if (query != null) {
            refreshStudentTable(query.trim());
        }
    }

    private void addCourse() {
        JTextField id = new JTextField();
        JTextField name = new JTextField();
        JPanel form = form("Course ID", id, "Course name", name);
        if (!confirmForm("Add course", form)) {
            return;
        }
        String courseId = id.getText().trim();
        String courseName = name.getText().trim();
        if (courseId.isEmpty() || courseName.isEmpty()) {
            showError("All fields are required.");
        } else if (courses.containsKey(courseId)) {
            showError("A course with that ID already exists.");
        } else {
            courses.put(courseId, new Course(courseId, courseName));
            refreshTables();
        }
    }

    private void deleteCourse() {
        String id = selectedId(courseModel, 0, "Select a course to delete.");
        if (id == null || !confirmDelete("Delete this course and its enrollments?")) {
            return;
        }
        courses.remove(id);
        for (Map<String, Double> studentEnrollments : enrollments.values()) {
            studentEnrollments.remove(id);
        }
        refreshTables();
    }

    private void enrollStudent() {
        if (students.isEmpty() || courses.isEmpty()) {
            showError("Add at least one student and one course before enrolling.");
            return;
        }
        JComboBox<Student> studentChoice = new JComboBox<>(students.values().toArray(new Student[0]));
        JComboBox<Course> courseChoice = new JComboBox<>(courses.values().toArray(new Course[0]));
        JPanel form = form("Student", studentChoice, "Course", courseChoice);
        if (!confirmForm("Enroll student", form)) {
            return;
        }
        Student student = (Student) studentChoice.getSelectedItem();
        Course course = (Course) courseChoice.getSelectedItem();
        if (student == null || course == null) {
            showError("Choose a student and course.");
            return;
        }
        Map<String, Double> studentEnrollments =
                enrollments.computeIfAbsent(student.getId(), key -> new LinkedHashMap<>());
        if (studentEnrollments.containsKey(course.getId())) {
            showError("Student is already enrolled in this course.");
            return;
        }
        studentEnrollments.put(course.getId(), null);
        refreshTables();
    }

    private void recordGrade() {
        List<EnrollmentChoice> choices = getEnrollmentChoices();
        if (choices.isEmpty()) {
            showError("There are no enrollments to grade.");
            return;
        }
        JComboBox<EnrollmentChoice> enrollmentChoice =
                new JComboBox<>(choices.toArray(new EnrollmentChoice[0]));
        JPanel form = form("Enrollment", enrollmentChoice);
        if (JOptionPane.showConfirmDialog(
                this, form, "Record grade", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) {
            return;
        }
        EnrollmentChoice selected = (EnrollmentChoice) enrollmentChoice.getSelectedItem();
        if (selected == null) {
            showError("Choose an enrollment.");
            return;
        }
        String value = JOptionPane.showInputDialog(this, "Enter a grade from 0 to 100:");
        if (value == null) {
            return;
        }
        try {
            double grade = Double.parseDouble(value.trim());
            if (!Double.isFinite(grade) || grade < 0 || grade > 100) {
                showError("Grade must be a number from 0 to 100.");
                return;
            }
            enrollments.get(selected.studentId).put(selected.courseId, grade);
            refreshTables();
        } catch (NumberFormatException exception) {
            showError("Enter a valid number from 0 to 100.");
        }
    }

    private void showStudentReport() {
        if (students.isEmpty()) {
            showError("There are no students to report.");
            return;
        }
        JComboBox<Student> studentChoice = new JComboBox<>(students.values().toArray(new Student[0]));
        JPanel form = form("Student", studentChoice);
        if (JOptionPane.showConfirmDialog(
                this, form, "Student report", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                != JOptionPane.OK_OPTION) {
            return;
        }
        Student student = (Student) studentChoice.getSelectedItem();
        if (student == null) {
            showError("Choose a student.");
            return;
        }

        StringBuilder report = new StringBuilder("Report for ").append(student).append("\n\n");
        Map<String, Double> studentEnrollments = enrollments.get(student.getId());
        double total = 0;
        int gradedCourses = 0;
        if (studentEnrollments == null || studentEnrollments.isEmpty()) {
            report.append("No course enrollments.");
        } else {
            for (Map.Entry<String, Double> enrollment : studentEnrollments.entrySet()) {
                Course course = courses.get(enrollment.getKey());
                if (course == null) {
                    continue;
                }
                Double grade = enrollment.getValue();
                report.append(course).append(" - ")
                        .append(grade == null ? "Not graded" : String.format("%.2f/100", grade))
                        .append('\n');
                if (grade != null) {
                    total += grade;
                    gradedCourses++;
                }
            }
            if (gradedCourses > 0) {
                report.append(String.format("%nAverage grade: %.2f/100", total / gradedCourses));
            } else {
                report.append("\nNo grades recorded yet.");
            }
        }
        JOptionPane.showMessageDialog(this, report.toString(), "Student report", JOptionPane.INFORMATION_MESSAGE);
    }

    private List<EnrollmentChoice> getEnrollmentChoices() {
        List<EnrollmentChoice> choices = new ArrayList<>();
        for (Map.Entry<String, Map<String, Double>> studentEntry : enrollments.entrySet()) {
            Student student = students.get(studentEntry.getKey());
            if (student == null) {
                continue;
            }
            for (String courseId : studentEntry.getValue().keySet()) {
                Course course = courses.get(courseId);
                if (course != null) {
                    choices.add(new EnrollmentChoice(student, course));
                }
            }
        }
        return choices;
    }

    private void refreshTables() {
        refreshStudentTable("");
        courseModel.setRowCount(0);
        for (Course course : courses.values()) {
            courseModel.addRow(new Object[] {course.getId(), course.getName()});
        }
        enrollmentModel.setRowCount(0);
        for (Map.Entry<String, Map<String, Double>> studentEntry : enrollments.entrySet()) {
            Student student = students.get(studentEntry.getKey());
            if (student == null) {
                continue;
            }
            for (Map.Entry<String, Double> enrollment : studentEntry.getValue().entrySet()) {
                Course course = courses.get(enrollment.getKey());
                if (course != null) {
                    Double grade = enrollment.getValue();
                    enrollmentModel.addRow(new Object[] {
                            student.getId(), student.getName(), course.getId(), course.getName(),
                            grade == null ? "Not graded" : String.format("%.2f", grade)
                    });
                }
            }
        }
        summary.setText(students.size() + " students    " + courses.size() + " courses");
    }

    private void refreshStudentTable(String query) {
        studentModel.setRowCount(0);
        String normalizedQuery = query.toLowerCase(java.util.Locale.ROOT);
        for (Student student : students.values()) {
            if (student.getId().toLowerCase(java.util.Locale.ROOT).contains(normalizedQuery)
                    || student.getName().toLowerCase(java.util.Locale.ROOT).contains(normalizedQuery)) {
                studentModel.addRow(new Object[] {student.getId(), student.getName(), student.getEmail()});
            }
        }
    }

    private String selectedId(DefaultTableModel model, int column, String message) {
        JTable table = model == studentModel ? studentTable : courseTable;
        int row = table.getSelectedRow();
        if (row < 0) {
            showError(message);
            return null;
        }
        return String.valueOf(model.getValueAt(row, column));
    }

    private JPanel form(Object... fields) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        for (int i = 0; i < fields.length; i += 2) {
            panel.add(new JLabel(String.valueOf(fields[i])));
            panel.add((java.awt.Component) fields[i + 1]);
        }
        panel.setPreferredSize(new Dimension(340, fields.length / 2 * 36));
        return panel;
    }

    private boolean confirmForm(String title, JPanel form) {
        return JOptionPane.showConfirmDialog(
                this, form, title, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE)
                == JOptionPane.OK_OPTION;
    }

    private boolean confirmDelete(String message) {
        return JOptionPane.showConfirmDialog(
                this, message, "Confirm deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)
                == JOptionPane.YES_OPTION;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Student Management", JOptionPane.ERROR_MESSAGE);
    }

    private static class EnrollmentChoice {
        private final String studentId;
        private final String courseId;
        private final String label;

        private EnrollmentChoice(Student student, Course course) {
            studentId = student.getId();
            courseId = course.getId();
            label = student + " / " + course;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
