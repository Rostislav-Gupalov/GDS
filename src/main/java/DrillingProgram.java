import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.GridLayout;

public class DrillingProgram extends JFrame {
    private JTable table;
    private DefaultTableModel model;
    private JButton addButton;
    private List<String> drillingStages = new ArrayList<>();

    public DrillingProgram() {
        setTitle("Программа бурения");
        setSize(800, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Модель таблицы
        String[] columns = {"Наименование этапа", "Начало этапа (ЧЧ:00 ДД/ММ/ГГГГ)", "Конец этапа (ЧЧ:00 ДД/ММ/ГГГГ)", "Длительность этапа"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 3; // Запрещаем редактирование столбца "Длительность"
            }
        };
        table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        //Инициализация списка этапов
        updateDrillingStages();

        // Кнопка добавления этапа
// Старая кнопка
        addButton = new JButton("Добавить этап");
        addButton.addActionListener(e -> addNewRow());

// Новая кнопка перехода
        JButton nextButton = new JButton("Перейти к таблице \"Материалы\"");
        nextButton.addActionListener(e -> openMaterialsTable());

// Панель с двумя кнопками
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(addButton);
        buttonPanel.add(nextButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Валидация данных при редактировании
        table.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 1 || e.getColumn() == 2) {
                validateDates(e.getFirstRow());
            }
        });
    }

    private void addNewRow() {
            int selectedRow = table.getSelectedRow();
            int insertRow = (selectedRow == -1) ? model.getRowCount() : selectedRow + 1;
            model.insertRow(insertRow, new Object[]{"Новый этап", "", "", ""});

            // Обновляем список этапов
            updateDrillingStages();
        }

    private void updateDrillingStages() {
        drillingStages.clear();
        for (int i = 0; i < model.getRowCount(); i++) {
            drillingStages.add(model.getValueAt(i, 0).toString());
        }
    }

    private void openMaterialsTable() {
        if (drillingStages.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Добавьте хотя бы один этап бурения!",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        new MaterialsTable(drillingStages).setVisible(true);
        this.dispose(); // Закрываем текущее окно
    }

    private void validateDates(int row) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
            Date startDate = sdf.parse(model.getValueAt(row, 1).toString());
            Date endDate = sdf.parse(model.getValueAt(row, 2).toString());

            if (startDate.after(endDate)) {
                JOptionPane.showMessageDialog(this, "Ошибка: Начало этапа не может быть позже конца этапа!", "Ошибка", JOptionPane.ERROR_MESSAGE);
                model.setValueAt("", row, 2);
                return;
            }

            // Расчет длительности с минутами
            long durationMillis = endDate.getTime() - startDate.getTime();
            long days = durationMillis / (1000 * 60 * 60 * 24);
            long hours = (durationMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
            long minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60);

            model.setValueAt(days + " суток " + hours + " часов " + minutes + " минут", row, 3);

        } catch (ParseException e) {
            // Игнорируем незаполненные поля
        }
    }
}