
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main extends JFrame {
    private DefaultListModel<Task> listModel;
    private JList<Task> taskList;
    private JTextField taskInput;
    private JButton addButton;
    private JButton completeButton;
    private JButton deleteButton;
    private JButton saveButton;
    private JButton loadButton;
    private JButton settingsButton;
    private JButton massdeleteButton;
    private JButton aboutButton;
    private final String SAVE_FILE = "tasks.txt";
    private final String MASS_DELETE_FILE = "massdelete.txt";
    private final String SETTINGS_FILE = "settings.properties";
    
    // Settings
    private Properties settings;
    private boolean autoSave = true;
    private boolean showConfirmDialogs = true;
    private String currentTheme = "Default";
    private Color primaryColor = new Color(46, 125, 50);
    private Color secondaryColor = new Color(25, 118, 210);

    public Main() {
        loadSettings();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadTasksFromFile();
        applyTheme();
    }

    private void loadSettings() {
        settings = new Properties();
        try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
            settings.load(fis);
            autoSave = Boolean.parseBoolean(settings.getProperty("autoSave", "true"));
            showConfirmDialogs = Boolean.parseBoolean(settings.getProperty("showConfirmDialogs", "true"));
            currentTheme = settings.getProperty("theme", "Default");
            
            // Load custom colors
            String primaryColorStr = settings.getProperty("primaryColor", "46,125,50");
            String secondaryColorStr = settings.getProperty("secondaryColor", "25,118,210");
            
            String[] primaryRGB = primaryColorStr.split(",");
            String[] secondaryRGB = secondaryColorStr.split(",");
            
            primaryColor = new Color(Integer.parseInt(primaryRGB[0]), 
                                   Integer.parseInt(primaryRGB[1]), 
                                   Integer.parseInt(primaryRGB[2]));
            secondaryColor = new Color(Integer.parseInt(secondaryRGB[0]), 
                                     Integer.parseInt(secondaryRGB[1]), 
                                     Integer.parseInt(secondaryRGB[2]));
        } catch (IOException e) {
            // Use defaults if settings file doesn't exist
        }
    }

    private void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            settings.setProperty("autoSave", String.valueOf(autoSave));
            settings.setProperty("showConfirmDialogs", String.valueOf(showConfirmDialogs));
            settings.setProperty("theme", currentTheme);
            settings.setProperty("primaryColor", primaryColor.getRed() + "," + 
                               primaryColor.getGreen() + "," + primaryColor.getBlue());
            settings.setProperty("secondaryColor", secondaryColor.getRed() + "," + 
                               secondaryColor.getGreen() + "," + secondaryColor.getBlue());
            
            settings.store(fos, "To-Do List Settings");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving settings: " + e.getMessage(), 
                                        "Settings Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeComponents() {
        setTitle("To-Do List Manager v2.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);

        // Initialize components
        listModel = new DefaultListModel<>();
        taskList = new JList<>(listModel);
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskList.setCellRenderer(new TaskRenderer());

        taskInput = new JTextField(20);
        addButton = new JButton("Add Task");
        completeButton = new JButton("Mark Complete");
        deleteButton = new JButton("Delete Task");
        saveButton = new JButton("Save Tasks");
        loadButton = new JButton("Load Tasks");
        settingsButton = new JButton("Settings");
        massdeleteButton = new JButton("Mass Delete");
        aboutButton = new JButton("About");

        // Style buttons with current theme
        applyButtonStyle();
    }

    private void applyButtonStyle() {
        styleButton(addButton, primaryColor);
        styleButton(completeButton, secondaryColor);
        styleButton(deleteButton, new Color(211, 47, 47));
        styleButton(saveButton, new Color(156, 39, 176));
        styleButton(loadButton, new Color(255, 152, 0));
        styleButton(settingsButton, new Color(96, 125, 139));
        styleButton(massdeleteButton, new Color(59, 147, 240));
        styleButton(aboutButton, new Color(121, 85, 72));
    }

    private void applyTheme() {
        Color backgroundColor;
        Color textColor;
        
        // Use primary color as background for custom themes
        if (!currentTheme.equals("Default") && !currentTheme.equals("Light") && 
            !currentTheme.equals("Dark") && !currentTheme.equals("Blue")) {
            backgroundColor = primaryColor;
            textColor = isColorDark(primaryColor) ? Color.WHITE : Color.BLACK;
        } else {
            switch (currentTheme) {
                case "Dark":
                    backgroundColor = new Color(45, 45, 45);
                    textColor = Color.WHITE;
                    break;
                case "Light":
                    backgroundColor = Color.WHITE;
                    textColor = Color.BLACK;
                    break;
                case "Blue":
                    backgroundColor = new Color(240, 248, 255);
                    textColor = new Color(25, 25, 112);
                    break;
                default:
                    // For default theme, use primary color as background
                    backgroundColor = primaryColor;
                    textColor = isColorDark(primaryColor) ? Color.WHITE : Color.BLACK;
            }
        }
        
        // Apply background color to all components except buttons
        getContentPane().setBackground(backgroundColor);
        taskList.setBackground(backgroundColor);
        taskList.setForeground(textColor);
        taskInput.setBackground(backgroundColor);
        taskInput.setForeground(textColor);
        
        // Apply to all panels
        applyBackgroundToAllPanels(backgroundColor);
        
        repaint();
    }
    
    private boolean isColorDark(Color color) {
        // Calculate relative luminance
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance < 0.5;
    }
    
    private void applyBackgroundToAllPanels(Color backgroundColor) {
        // Apply background to all child components recursively
        applyBackgroundToContainer(this, backgroundColor);
    }
    
    private void applyBackgroundToContainer(Container container, Color backgroundColor) {
        for (Component component : container.getComponents()) {
            if (component instanceof JPanel) {
                component.setBackground(backgroundColor);
                if (component instanceof Container) {
                    applyBackgroundToContainer((Container) component, backgroundColor);
                }
            } else if (component instanceof JScrollPane) {
                component.setBackground(backgroundColor);
                JScrollPane scrollPane = (JScrollPane) component;
                if (scrollPane.getViewport() != null) {
                    scrollPane.getViewport().setBackground(backgroundColor);
                }
            } else if (component instanceof JLabel) {
                Color textColor = isColorDark(backgroundColor) ? Color.WHITE : Color.BLACK;
                component.setForeground(textColor);
            }
        }
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(120, 35));
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Top panel for input
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Add New Task:");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        topPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBorder(new EmptyBorder(5, 0, 0, 0));
        inputPanel.add(taskInput, BorderLayout.CENTER);
        inputPanel.add(addButton, BorderLayout.EAST);
        topPanel.add(inputPanel, BorderLayout.CENTER);

        // Center panel for task list
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(new EmptyBorder(0, 10, 0, 10));
        
        JLabel listLabel = new JLabel("Your Tasks:");
        listLabel.setFont(new Font("Arial", Font.BOLD, 14));
        centerPanel.add(listLabel, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setPreferredSize(new Dimension(580, 350));
        scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel for action buttons
        JPanel bottomPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottomPanel.add(completeButton);
        bottomPanel.add(deleteButton);
        bottomPanel.add(saveButton);
        bottomPanel.add(loadButton);
        bottomPanel.add(settingsButton);
        bottomPanel.add(aboutButton);
        bottomPanel.add(massdeleteButton);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        // Add task button and Enter key
        ActionListener addTaskAction = e -> addTask();
        addButton.addActionListener(addTaskAction);
        taskInput.addActionListener(addTaskAction);

        // Complete task button
        completeButton.addActionListener(e -> markTaskComplete());

        // Delete task button
        deleteButton.addActionListener(e -> deleteTask());

        // Save tasks button
        saveButton.addActionListener(e -> saveTasksToFile());

        // Load tasks button
        loadButton.addActionListener(e -> loadTasksFromFile());

        // Settings button
        settingsButton.addActionListener(e -> showSettingsDialog());

        // About button
        aboutButton.addActionListener(e -> showAboutDialog());
        // Mass delete button
        massdeleteButton.addActionListener(e -> massDeleteTasks());
    }

    private void massDeleteTasks() {
        if (listModel.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No tasks to delete!", "Mass Delete", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        boolean confirmMassDelete = true;
        if (showConfirmDialogs) {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete ALL tasks?\nThis will permanently remove all tasks from the list.", "Confirm Mass Delete", JOptionPane.YES_NO_OPTION);
            confirmMassDelete = (confirm == JOptionPane.YES_OPTION);
        }
        
        if (confirmMassDelete) {
            // Save all tasks to mass delete file before deletion
            try (PrintWriter writer = new PrintWriter(new FileWriter(MASS_DELETE_FILE, true))) {
                writer.println("=== Mass Delete Session - " + new java.util.Date() + " ===");
                for (int i = 0; i < listModel.getSize(); i++) {
                    Task task = listModel.getElementAt(i);
                    writer.println(task.getDescription() + "|" + task.isCompleted());
                }
                writer.println("=== End Session ===");
                writer.println();

                // Clear all tasks from the list
                listModel.clear();

                if (autoSave) {
                    saveTasksToFile();
                }

                JOptionPane.showMessageDialog(this, "All tasks have been deleted!", "Mass Delete Complete", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving mass delete tasks: " + e.getMessage(), "Mass Delete Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showSettingsDialog() {
        JDialog settingsDialog = new JDialog(this, "Settings", true);
        settingsDialog.setSize(450, 400);
        settingsDialog.setLocationRelativeTo(this);
        settingsDialog.setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // General Settings Tab
        JPanel generalPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JCheckBox autoSaveCheckBox = new JCheckBox("Auto-save tasks", autoSave);
        JCheckBox confirmDialogsCheckBox = new JCheckBox("Show confirmation dialogs", showConfirmDialogs);

        gbc.gridx = 0; gbc.gridy = 0;
        generalPanel.add(autoSaveCheckBox, gbc);
        gbc.gridy = 1;
        generalPanel.add(confirmDialogsCheckBox, gbc);

        tabbedPane.addTab("General", generalPanel);

        // Theme Settings Tab
        JPanel themePanel = new JPanel(new GridBagLayout());
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        JLabel themeLabel = new JLabel("Theme:");
        String[] themes = {"Default", "Light", "Dark", "Blue"};
        JComboBox<String> themeComboBox = new JComboBox<>(themes);
        themeComboBox.setSelectedItem(currentTheme);

        JButton primaryColorButton = new JButton("Primary Color");
        primaryColorButton.setBackground(primaryColor);
        primaryColorButton.setForeground(Color.WHITE);

        JButton secondaryColorButton = new JButton("Secondary Color");
        secondaryColorButton.setBackground(secondaryColor);
        secondaryColorButton.setForeground(Color.WHITE);

        gbc.gridx = 0; gbc.gridy = 0;
        themePanel.add(themeLabel, gbc);
        gbc.gridx = 1;
        themePanel.add(themeComboBox, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        themePanel.add(new JLabel("Colors:"), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        themePanel.add(primaryColorButton, gbc);
        gbc.gridx = 1;
        themePanel.add(secondaryColorButton, gbc);

        // Color chooser events
        primaryColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(settingsDialog, "Choose Primary Color", primaryColor);
            if (newColor != null) {
                primaryColor = newColor;
                primaryColorButton.setBackground(newColor);
            }
        });

        secondaryColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(settingsDialog, "Choose Secondary Color", secondaryColor);
            if (newColor != null) {
                secondaryColor = newColor;
                secondaryColorButton.setBackground(newColor);
            }
        });

        tabbedPane.addTab("Theme", themePanel);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveSettingsButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        JButton resetButton = new JButton("Reset to Defaults");

        styleButton(saveSettingsButton, new Color(46, 125, 50));
        styleButton(cancelButton, new Color(158, 158, 158));
        styleButton(resetButton, new Color(211, 47, 47));

        saveSettingsButton.addActionListener(e -> {
            autoSave = autoSaveCheckBox.isSelected();
            showConfirmDialogs = confirmDialogsCheckBox.isSelected();
            currentTheme = (String) themeComboBox.getSelectedItem();
            
            saveSettings();
            applyButtonStyle();
            applyTheme();
            settingsDialog.dispose();
            
            JOptionPane.showMessageDialog(this, "Settings saved successfully!", 
                                        "Settings", JOptionPane.INFORMATION_MESSAGE);
        });

        cancelButton.addActionListener(e -> settingsDialog.dispose());

        resetButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(settingsDialog, 
                "Reset all settings to defaults?", "Confirm Reset", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                autoSave = true;
                showConfirmDialogs = true;
                currentTheme = "Default";
                primaryColor = new Color(46, 125, 50);
                secondaryColor = new Color(25, 118, 210);
                
                autoSaveCheckBox.setSelected(autoSave);
                confirmDialogsCheckBox.setSelected(showConfirmDialogs);
                themeComboBox.setSelectedItem(currentTheme);
                primaryColorButton.setBackground(primaryColor);
                secondaryColorButton.setBackground(secondaryColor);
            }
        });

        buttonPanel.add(saveSettingsButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(resetButton);

        settingsDialog.add(tabbedPane, BorderLayout.CENTER);
        settingsDialog.add(buttonPanel, BorderLayout.SOUTH);
        settingsDialog.setVisible(true);
    }

    private void showAboutDialog() {
        JDialog aboutDialog = new JDialog(this, "About", true);
        aboutDialog.setSize(350, 250);
        aboutDialog.setLocationRelativeTo(this);
        aboutDialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel titleLabel = new JLabel("To-Do List Manager");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        
        JLabel versionLabel = new JLabel("Version 2.0");
        versionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        
        JLabel descLabel = new JLabel("<html><center>A simple and elegant task management<br>application with customizable themes<br>and persistent storage.</center></html>");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel authorLabel = new JLabel("Built with Java Swing");
        authorLabel.setFont(new Font("Arial", Font.ITALIC, 10));

        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(titleLabel, gbc);
        gbc.gridy = 1;
        contentPanel.add(versionLabel, gbc);
        gbc.gridy = 2;
        contentPanel.add(descLabel, gbc);
        gbc.gridy = 3;
        contentPanel.add(authorLabel, gbc);

        JButton closeButton = new JButton("Close");
        styleButton(closeButton, new Color(96, 125, 139));
        closeButton.addActionListener(e -> aboutDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(closeButton);

        aboutDialog.add(contentPanel, BorderLayout.CENTER);
        aboutDialog.add(buttonPanel, BorderLayout.SOUTH);
        aboutDialog.setVisible(true);
    }

    private void addTask() {
        String taskText = taskInput.getText().trim();
        if (!taskText.isEmpty()) {
            Task newTask = new Task(taskText);
            listModel.addElement(newTask);
            taskInput.setText("");
            taskInput.requestFocus();
            
            if (autoSave) {
                saveTasksToFile();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please enter a task description!", 
                                        "Empty Task", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void markTaskComplete() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            Task selectedTask = listModel.getElementAt(selectedIndex);
            selectedTask.setCompleted(!selectedTask.isCompleted());
            taskList.repaint();
            
            if (autoSave) {
                saveTasksToFile();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to mark as complete!", 
                                        "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            boolean confirmDelete = true;
            if (showConfirmDialogs) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Are you sure you want to delete this task?", 
                    "Confirm Delete", JOptionPane.YES_NO_OPTION);
                confirmDelete = (confirm == JOptionPane.YES_OPTION);
            }
            
            if (confirmDelete) {
                listModel.removeElementAt(selectedIndex);
                if (autoSave) {
                    saveTasksToFile();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to delete!", 
                                        "No Selection", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void saveTasksToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(SAVE_FILE))) {
            for (int i = 0; i < listModel.getSize(); i++) {
                Task task = listModel.getElementAt(i);
                writer.println(task.getDescription() + "|" + task.isCompleted());
            }
            if (!autoSave) {
                JOptionPane.showMessageDialog(this, "Tasks saved successfully!", 
                                            "Save Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving tasks: " + e.getMessage(), 
                                        "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasksFromFile() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            return; // No saved tasks file
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(SAVE_FILE))) {
            listModel.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 2) {
                    Task task = new Task(parts[0]);
                    task.setCompleted(Boolean.parseBoolean(parts[1]));
                    listModel.addElement(task);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading tasks: " + e.getMessage(), 
                                        "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Task class to represent individual tasks
    private static class Task {
        private String description;
        private boolean completed;

        public Task(String description) {
            this.description = description;
            this.completed = false;
        }

        public String getDescription() {
            return description;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }

        @Override
        public String toString() {
            return description;
        }
    }

    // Custom renderer for the task list
    private static class TaskRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Task) {
                Task task = (Task) value;
                String displayText = task.getDescription();
                
                if (task.isCompleted()) {
                    displayText = "✓ " + displayText;
                    setForeground(isSelected ? Color.WHITE : new Color(100, 100, 100));
                    setFont(getFont().deriveFont(Font.ITALIC));
                } else {
                    displayText = "○ " + displayText;
                    setForeground(isSelected ? Color.WHITE : Color.BLACK);
                    setFont(getFont().deriveFont(Font.PLAIN));
                }
                
                setText(displayText);
            }
            
            return this;
        }
    }

    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default if system look and feel fails
        }

        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}
